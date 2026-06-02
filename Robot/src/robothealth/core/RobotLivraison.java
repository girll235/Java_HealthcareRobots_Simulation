package robothealth.core;

import java.awt.Point;
import java.util.List;

import robothealth.exceptions.BatterieCritiqueUrgenceException;
import robothealth.exceptions.CheminBloqueException;
import robothealth.exceptions.RobotException;
import robothealth.map.PlanHopital;
import robothealth.missions.MissionLivraison;

/**
 * Robot spécialisé dans les livraisons hospitalières.
 * Il sait charger un colis, calculer un trajet minimal, se connecter au réseau
 * et afficher en temps réel les étapes d'exécution.
 */
public class RobotLivraison extends RobotConnecte {
    public static final int ENERGIE_LIVRAISON = 15;
    public static final int ENERGIE_CHARGEMENT = 5;
    private static final long PAUSE_ETAPE = 180L;

    private String colisActuel;
    private String destination;
    private boolean enLivraison;
    private boolean colisCharge;
    private MissionLivraison missionCourante;

    public RobotLivraison(String id, int x, int y, double energie) {
        super(id, x, y, energie);
        this.colisActuel = null;
        this.destination = null;
        this.enLivraison = false;
        this.colisCharge = false;
        this.missionCourante = null;
    }

    /**
     * Prépare la mission sans lancer l'exécution immédiate.
     * L'exécution réelle sera démarrée automatiquement par le gestionnaire.
     */
    public synchronized void planifierMission(MissionLivraison mission) throws RobotException {
        if (!estDisponible()) {
            throw new RobotException("Le robot est déjà occupé par une autre livraison.");
        }
        verifierMaintenance();
        verifierEnergie(ENERGIE_CHARGEMENT + 10);
        this.missionCourante = mission;
        this.colisActuel = null;
        this.destination = mission.getDestination();
        this.enLivraison = false;
        this.colisCharge = false;
        setStatutMissionAffiche("MISSION LIVRAISON");
        ajouterHistorique("Mission planifiée : " + mission.getDescription() + " [" + mission.getCriticite() + "]");
    }

    /**
     * Déplacement élémentaire d'une case ou d'un petit segment.
     */
    @Override
    public synchronized void deplacer(int nouveauX, int nouveauY) throws RobotException {
        double distance = Math.hypot(nouveauX - getX(), nouveauY - getY());
        if (distance > 100) {
            throw new CheminBloqueException("Distance trop grande pour un seul déplacement : " + distance);
        }

        double coutEnergie = distance * 0.3;
        verifierMaintenance();
        verifierEnergie(coutEnergie);

        if (missionCourante != null && missionCourante.getCriticite().estUrgente() && getEnergie() - coutEnergie < 10) {
            throw new BatterieCritiqueUrgenceException("Batterie critique pendant une mission urgente.");
        }

        consommerEnergie(coutEnergie);
        incrementerHeuresDepuisDistance(distance);
        setPosition(nouveauX, nouveauY);
        ajouterHistorique(String.format("Déplacement livraison vers (%d,%d), distance=%.2f", nouveauX, nouveauY, distance));
    }

    /**
     * Lance automatiquement toute la mission.
     * Cette méthode conserve désormais un ordre strict :
     * effectuerTache() -> chargerColis() -> faireLivraison()
     */
    @Override
    public void effectuerTache() throws RobotException {
        if (!isEnMarche()) {
            throw new RobotException("Le robot doit être démarré pour effectuer une tâche.");
        }
        if (missionCourante == null) {
            setStatutMissionAffiche("AUCUNE MISSION");
            ajouterHistorique("Aucune mission de livraison en attente.");
            return;
        }

        try {
            MissionLivraison mission = missionCourante;
            setStatutMissionAffiche("LIVRAISON : " + mission.getDescription());

            ajouterHistorique("===== DÉBUT MISSION LIVRAISON =====");
            ajouterHistorique("Type de mission : " + mission.getSensLivraison());
            ajouterHistorique("Étape 1/6 : connexion réseau pour tracer la mission.");
            connecter(PlanHopital.RESEAU_HOSPITALIER);
            envoyerDonnees("Mission démarrée pour le colis '" + mission.getColis() + "'.");
            pauseAnimation(PAUSE_ETAPE);

            if (!colisCharge) {
                ajouterHistorique("Étape 2/6 : approche du point de départ " + mission.getOrigine() + ".");
                int distanceVersDepart = PlanHopital.distanceMinimale(getX(), getY(), mission.getOrigineX(), mission.getOrigineY());
                ajouterHistorique("Distance minimale robot -> départ = " + distanceVersDepart + " case(s).");
                suivreTrajet(PlanHopital.calculerTrajetMinimal(getX(), getY(), mission.getOrigineX(), mission.getOrigineY()), "Approche départ");

                ajouterHistorique("Étape 3/6 : chargement du colis au point de départ " + mission.getOrigine() + ".");
                chargerColis(mission.getColis(), mission.getDestination());
                envoyerDonnees("Colis récupéré au point de départ : " + mission.getOrigine());
                pauseAnimation(PAUSE_ETAPE);
            } else {
                // Ajout additif : reprise d'une livraison interrompue sans perte d'état visible.
                ajouterHistorique("Étape 2/6 : reprise d'une livraison interrompue avec colis déjà chargé.");
            }

            ajouterHistorique("Étape 4/6 : préparation de la livraison vers la destination " + mission.getDestination() + ".");
            ajouterHistorique("Distance minimale restante = "
                    + PlanHopital.distanceMinimale(getX(), getY(), mission.getDestinationX(), mission.getDestinationY()) + " case(s).");
            // Amélioration métier : journalisation explicite de la distance totale minimale sans changer l'algorithme existant.
            ajouterHistorique("Distance minimale mission complète = " + mission.getDistanceMinimaleMission() + " case(s).");
           //Step-by-step movement EXACTLY like first segment
            suivreTrajet(
                PlanHopital.calculerTrajetMinimal(
                    getX(), getY(),
                    mission.getDestinationX(), mission.getDestinationY()
                ),
                "Approche destination " + mission.getDestination()
            );

            // THEN finalize delivery
            faireLivraison(mission.getDestinationX(), mission.getDestinationY());
            
            ajouterHistorique("Étape 6/6 : clôture de la mission et archivage.");
            ajouterHistorique("===== FIN MISSION LIVRAISON =====");
        } catch (RobotException e) {
            journaliserErreur(e.getMessage());
            throw e;
        }
    }

    /**
     * Parcourt le trajet case par case afin de produire une animation fluide dans la JFrame.
     */
    private void suivreTrajet(List<Point> trajet, String libelleEtape) throws RobotException {
        if (trajet.size() <= 1) {
            ajouterHistorique(libelleEtape + " : aucun déplacement nécessaire.");
            return;
        }

        for (int i = 1; i < trajet.size(); i++) {
            Point etape = trajet.get(i);
            ajouterHistorique(libelleEtape + " - sous-étape " + i + "/" + (trajet.size() - 1)
                    + " vers (" + etape.x + "," + etape.y + ")");
            deplacer(etape.x, etape.y);
            pauseAnimation(PAUSE_ETAPE);
        }
    }

    /**
     * faireLivraison(int x, int y) :
     * - appelle obligatoirement deplacer()
     * - ne modifie jamais deplacer()
     * - met à jour l'état de livraison
     * - journalise explicitement la destination finale
     */
    public synchronized void faireLivraison(int destX, int destY) throws RobotException {
        if (!isEnMarche()) {
            throw new RobotException("Le robot de livraison doit être démarré avant la livraison.");
        }
        if (!enLivraison || missionCourante == null || !colisCharge) {
            throw new RobotException("Aucune livraison chargée à exécuter.");
        }

        //suivreTrajet(PlanHopital.calculerTrajetMinimal(getX(), getY(), destX, destY), "Livraison vers destination");
        
        verifierEnergie(ENERGIE_LIVRAISON);
        consommerEnergie(ENERGIE_LIVRAISON);
        envoyerDonnees("Livraison terminée à " + destination + ".");
        pauseAnimation(PAUSE_ETAPE);
        ajouterHistorique("Livraison terminée à " + destination);

        colisActuel = null;
        colisCharge = false;
        enLivraison = false;
        MissionLivraison missionTerminee = missionCourante;
        missionCourante = null;
        destination = missionTerminee == null ? null : missionTerminee.getDestination();
        afficherMissionTerminee();
        destination = null;
        deconnecter();
    }

    /**
     * Méthode historique conservée mais enrichie pour respecter l'ordre métier demandé.
     */
    public synchronized void chargerColis(String colis, String destination) throws RobotException {
        if (missionCourante == null) {
            throw new RobotException("Aucune mission planifiée pour le chargement.");
        }
        if (colisCharge) {
            ajouterHistorique("Le colis est déjà chargé : '" + colisActuel + "'.");
            return;
        }
        verifierEnergie(ENERGIE_CHARGEMENT);
        this.colisActuel = colis;
        this.destination = destination;
        this.enLivraison = true;
        this.colisCharge = true;
        consommerEnergie(ENERGIE_CHARGEMENT);
        ajouterHistorique("Colis chargé : '" + colis + "' vers " + destination);
    }

    /**
     * Ajout additif : permet au gestionnaire de libérer proprement le robot lors d'une réaffectation.
     */
    public synchronized void libererMissionPourReaffectation(String raison) {
        ajouterHistorique("Mission libérée pour réaffectation : " + raison);
        this.colisActuel = null;
        this.destination = null;
        this.enLivraison = false;
        this.colisCharge = false;
        this.missionCourante = null;
        reinitialiserStatutAffiche();
    }

    @Override
    public synchronized boolean estDisponible() {
        return missionCourante == null;
    }

    @Override
    public synchronized String getMissionCourante() {
        return missionCourante == null ? "Aucune" : missionCourante.getDescription();
    }

    public synchronized boolean isEnLivraison() {
        return enLivraison;
    }

    public synchronized boolean isColisCharge() {
        return colisCharge;
    }

    public synchronized String getColisActuel() {
        return colisActuel;
    }

    public synchronized String getDestination() {
        return destination;
    }

    public synchronized MissionLivraison getMissionLivraison() {
        return missionCourante;
    }

    /**
     * Ajout additif : estimation utilisée par le gestionnaire pour choisir un robot viable.
     */
    public synchronized double estimerEnergieMission(MissionLivraison mission) {
        int distanceAcces = PlanHopital.distanceMinimale(getX(), getY(), mission.getOrigineX(), mission.getOrigineY());
        int distanceLivraison = mission.getDistanceMinimaleMission();
        return ENERGIE_CHARGEMENT + ENERGIE_LIVRAISON + 8 + ((distanceAcces + distanceLivraison) * 0.3);
    }

    @Override
    public synchronized String toString() {
        return String.format(
                "RobotLivraison [ID=%s, position=(%d,%d), énergie=%.1f%%, heures=%d, colis=%s, destination=%s, connecté=%s, statut=%s]",
                getId(), getX(), getY(), getEnergie(), getHeuresUtilisation(),
                colisActuel == null ? "0" : colisActuel,
                destination == null ? "-" : destination,
                isConnecte() ? "Oui" : "Non",
                getStatutMissionAffiche());
    }
}
