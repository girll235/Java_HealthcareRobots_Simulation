package robothealth.missions;

import robothealth.core.CriticiteMission;
import robothealth.map.LieuHopital;
import robothealth.map.PlanHopital;
import robothealth.map.SensLivraison;

/**
 * Décrit une mission de transport de colis entre deux points fixes de l'hôpital.
 * La mission connaît explicitement son point de départ et son point d'arrivée,
 * ce qui permet de calculer un trajet intelligent et d'afficher les étapes.
 */
public class MissionLivraison implements Mission {
    private final String id;
    private final String colis;
    private final String origine;
    private final int origineX;
    private final int origineY;
    private final String destination;
    private final int destinationX;
    private final int destinationY;
    private final CriticiteMission criticite;
    private final boolean accesPharmacie;
    private final SensLivraison sensLivraison;

    /**
     * Constructeur historique conservé pour ne pas casser l'architecture existante.
     * Par défaut, on considère désormais que le départ historique se fait depuis la pharmacie.
     */
    public MissionLivraison(String id, String colis, String destination, int destinationX, int destinationY,
            CriticiteMission criticite, boolean accesPharmacie) {
        this(id, colis,
                LieuHopital.PHARMACIE.getLibelle(), LieuHopital.PHARMACIE.getX(), LieuHopital.PHARMACIE.getY(),
                destination, destinationX, destinationY,
                criticite, accesPharmacie, SensLivraison.PHARMACIE_VERS_PATIENT);
    }

    /**
     * Constructeur complet utilisé par la nouvelle interface graphique.
     */
    public MissionLivraison(String id, String colis, String origine, int origineX, int origineY,
            String destination, int destinationX, int destinationY,
            CriticiteMission criticite, boolean accesPharmacie, SensLivraison sensLivraison) {
        this.id = id;
        this.colis = colis;
        this.origine = origine;
        this.origineX = origineX;
        this.origineY = origineY;
        this.destination = destination;
        this.destinationX = destinationX;
        this.destinationY = destinationY;
        this.criticite = criticite;
        this.accesPharmacie = accesPharmacie;
        this.sensLivraison = sensLivraison;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getColis() {
        return colis;
    }

    public String getOrigine() {
        return origine;
    }

    public int getOrigineX() {
        return origineX;
    }

    public int getOrigineY() {
        return origineY;
    }

    public String getDestination() {
        return destination;
    }

    @Override
    public String getDescription() {
        return "Livraison de " + colis + " : " + origine + " -> " + destination;
    }

    @Override
    public CriticiteMission getCriticite() {
        return criticite;
    }

    @Override
    public int getDestinationX() {
        return destinationX;
    }

    @Override
    public int getDestinationY() {
        return destinationY;
    }

    @Override
    public boolean estSensible() {
        return accesPharmacie;
    }

    public SensLivraison getSensLivraison() {
        return sensLivraison;
    }

    /**
     * Ajout métier : fournit la distance minimale entre le point de départ et la destination finale.
     * Cette méthode réutilise le calcul existant sans changer l'algorithme de déplacement.
     */
    public int getDistanceMinimaleMission() {
        return PlanHopital.distanceMinimale(origineX, origineY, destinationX, destinationY);
    }
}
