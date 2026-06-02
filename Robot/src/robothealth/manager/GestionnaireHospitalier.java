package robothealth.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import robothealth.core.Robot;
import robothealth.core.RobotCompagnon;
import robothealth.core.RobotLivraison;
import robothealth.exceptions.RobotException;
import robothealth.map.PlanHopital;
import robothealth.missions.Mission;
import robothealth.missions.MissionCompagnon;
import robothealth.missions.MissionLivraison;
import robothealth.util.JournalListener;
import robothealth.util.SecurityService;

/**
 * Orchestre toute la flotte hospitalière.
 * Cette classe sélectionne les robots, affecte les missions et lance leur exécution automatique.
 */
public class GestionnaireHospitalier {
    private final List<Robot> robots;
    private final SecurityService securityService;
    private final List<JournalListener> listeners;
    private final ExecutorService executeurMissions;
    private final List<Future<?>> missionsEnCours;
    private final List<ContexteMission> missionsPlanifiees;
    private final Map<String, Future<?>> executionsParMission;
    private volatile boolean systemeDemarre;

    public GestionnaireHospitalier(SecurityService securityService) {
        this.robots = new ArrayList<>();
        this.securityService = securityService;
        this.listeners = new ArrayList<>();
        this.executeurMissions = Executors.newCachedThreadPool();
        this.missionsEnCours = new CopyOnWriteArrayList<>();
        this.missionsPlanifiees = new CopyOnWriteArrayList<>();
        this.executionsParMission = new ConcurrentHashMap<>();
        this.systemeDemarre = true;
    }

    public void ajouterRobot(Robot robot) {
        robots.add(robot);
        for (JournalListener listener : listeners) {
            robot.addJournalListener(listener);
        }
        publierLog("Robot ajouté à la flotte : " + robot, false);
    }

    public void addJournalListener(JournalListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            for (Robot robot : robots) {
                robot.addJournalListener(listener);
            }
        }
    }

    private void publierLog(String message, boolean erreur) {
        for (JournalListener listener : listeners) {
            listener.onLog(message, erreur);
        }
    }

    public List<Robot> getRobots() {
        return Collections.unmodifiableList(robots);
    }

    /**
     * Choisit le robot de livraison le plus proche du point de départ de la mission.
     */
    public Optional<RobotLivraison> selectionnerRobotLivraison(MissionLivraison mission) {
        return selectionnerRobotLivraison(mission, null);
    }

    private Optional<RobotLivraison> selectionnerRobotLivraison(MissionLivraison mission, Robot robotAExclure) {
        return robots.stream()
                .filter(r -> r instanceof RobotLivraison)
                .map(r -> (RobotLivraison) r)
                .filter(r -> r != robotAExclure)
                .filter(RobotLivraison::estDisponible)
                .filter(this::robotOperable)
                .filter(r -> r.getEnergie() >= r.estimerEnergieMission(mission))
                .min(Comparator.comparingInt(
                        r -> PlanHopital.distanceMinimale(r.getX(), r.getY(), mission.getOrigineX(), mission.getOrigineY())));
    }

    public Optional<RobotCompagnon> selectionnerRobotCompagnon(MissionCompagnon mission) {
        return selectionnerRobotCompagnon(mission, null);
    }

    private Optional<RobotCompagnon> selectionnerRobotCompagnon(MissionCompagnon mission, Robot robotAExclure) {
        return robots.stream()
                .filter(r -> r instanceof RobotCompagnon)
                .map(r -> (RobotCompagnon) r)
                .filter(r -> r != robotAExclure)
                .filter(RobotCompagnon::estDisponible)
                .filter(this::robotOperable)
                .filter(r -> r.getEnergie() >= r.estimerEnergieMission(mission))
                .min(Comparator.comparingInt(
                        r -> PlanHopital.distanceMinimale(r.getX(), r.getY(), mission.getDestinationX(), mission.getDestinationY())));
    }

    private boolean robotOperable(Robot robot) {
        return !robot.aBesoinMaintenance();
    }

    /**
     * Affecte une mission puis la place en attente d'exécution.
     * Ajout critique : l'affectation n'efface pas l'état d'une mission déjà planifiée.
     */
    public Robot assignerMission(Mission mission, String codeSecurite) throws RobotException {
        if (mission instanceof MissionLivraison missionLivraison) {
            if (missionLivraison.estSensible()) {
                securityService.verifierCodePharmacie(codeSecurite);
            }
            Optional<RobotLivraison> robotOpt = selectionnerRobotLivraison(missionLivraison);

            if (robotOpt.isEmpty()) {

                //Chercher le robot le plus proche pour diagnostiquer
                Optional<RobotLivraison> candidat = robots.stream()
                        .filter(r -> r instanceof RobotLivraison)
                        .map(r -> (RobotLivraison) r)
                        .min(Comparator.comparingInt(
                                r -> PlanHopital.distanceMinimale(r.getX(), r.getY(),
                                        missionLivraison.getOrigineX(), missionLivraison.getOrigineY())));

                if (candidat.isPresent()) {
                    RobotLivraison r = candidat.get();

                    if (!r.estDisponible()) {
                        throw new RobotException("Aucun robot disponible pour effectuer cette mission");
                    }

                    if (r.getEnergie() < r.estimerEnergieMission(missionLivraison)) {
                        throw new RobotException("Tous les robots ont une charge insuffisante pour cette mission. Vous pouvez les charger .");
                    }
                }

                throw new RobotException("Aucun robot disponible pour effectuer cette mission");
            }

            RobotLivraison robot = robotOpt.get();
            robot.planifierMission(missionLivraison);
            missionsPlanifiees.add(new ContexteMission(missionLivraison, robot));
            publierLog("Mission affectée à " + robot.getId() + " : " + mission.getDescription() + " (en attente d'exécution)", false);
            return robot;
        }

        if (mission instanceof MissionCompagnon missionCompagnon) {
            if (missionCompagnon.estSensible()) {
                securityService.verifierCodePatient(codeSecurite);
            }
            Optional<RobotCompagnon> robotOpt = selectionnerRobotCompagnon(missionCompagnon);

            if (robotOpt.isEmpty()) {

                //Chercher le robot le plus proche pour diagnostiquer
                Optional<RobotCompagnon> candidat = robots.stream()
                        .filter(r -> r instanceof RobotCompagnon)
                        .map(r -> (RobotCompagnon) r)
                        .min(Comparator.comparingInt(
                                r -> PlanHopital.distanceMinimale(r.getX(), r.getY(),
                                        missionCompagnon.getDestinationX(), missionCompagnon.getDestinationY())));

                if (candidat.isPresent()) {
                    RobotCompagnon r = candidat.get();

                    if (!r.estDisponible()) {
                        throw new RobotException("Aucun robot disponible pour effectuer cette mission");
                    }

                    if (r.getEnergie() < r.estimerEnergieMission(missionCompagnon)) {
                        throw new RobotException("Tous les robots ont une charge insuffisante pour cette mission. Vous pouvez les charger .");
                    }
                }

                throw new RobotException("Aucun robot disponible pour effectuer cette mission");
            }

            RobotCompagnon robot = robotOpt.get();
            robot.planifierMission(missionCompagnon);
            missionsPlanifiees.add(new ContexteMission(missionCompagnon, robot));
            publierLog("Mission affectée à " + robot.getId() + " : " + mission.getDescription() + " (en attente d'exécution)", false);
            return robot;
        }

        throw new RobotException("Type de mission non supporté.");
    }

    /**
     * Exécution asynchrone conservée, mais désormais pilotée par un contexte de mission.
     */
    private void executerMissionAutomatique(ContexteMission contexte) throws RobotException {
        Mission mission = contexte.getMission();
        String idMission = mission.getId();
        if (executionsParMission.containsKey(idMission)) {
            return;
        }

        Robot robot = garantirRobotAssigne(contexte, null);
        contexte.incrementerTentatives();
        contexte.setEtat(EtatExecutionMission.EN_COURS);
        contexte.setDernierMessage("Mission en cours sur " + robot.getId());
        publierLog("Exécution planifiée pour la mission " + mission.getId() + " sur le robot " + robot.getId() + ".", false);

        Future<?> future = executeurMissions.submit(() -> {
            try {
                if (!robot.isEnMarche()) {
                    robot.demarrer();
                }
                robot.effectuerTache();
                contexte.setEtat(EtatExecutionMission.TERMINEE);
                contexte.setDernierMessage("Mission terminée avec succès");
                publierLog("Mission terminée : " + mission.getDescription(), false);
            } catch (RobotException e) {
                publierLog("Erreur d'exécution pour la mission " + mission.getId() + " : " + e.getMessage(), true);
                gererEchecExecution(contexte, robot, e);
            } finally {
                executionsParMission.remove(idMission);
            }
        });
        missionsEnCours.add(future);
        executionsParMission.put(idMission, future);
    }

    private Robot garantirRobotAssigne(ContexteMission contexte, Robot robotAExclure) throws RobotException {
        Robot robot = contexte.getRobotAssigne();
        Mission mission = contexte.getMission();

        if (robot != null && robot != robotAExclure && robotOperable(robot)
                && (robot.estDisponible() || robot.getMissionCourante().equals(mission.getDescription()))) {
            return robot;
        }

        if (mission instanceof MissionLivraison missionLivraison) {
            RobotLivraison remplacement = selectionnerRobotLivraison(missionLivraison, robotAExclure)
                    .orElseThrow(() -> new RobotException("Aucun robot disponible pour effectuer cette mission"));
            remplacement.planifierMission(missionLivraison);
            contexte.setRobotAssigne(remplacement);
            contexte.setDernierMessage("Mission réaffectée à " + remplacement.getId());
            publierLog("Mission " + mission.getId() + " réaffectée à " + remplacement.getId() + ".", false);
            return remplacement;
        }

        if (mission instanceof MissionCompagnon missionCompagnon) {
            RobotCompagnon remplacement = selectionnerRobotCompagnon(missionCompagnon, robotAExclure)
                    .orElseThrow(() -> new RobotException("Aucun robot disponible pour effectuer cette mission"));
            remplacement.planifierMission(missionCompagnon);
            contexte.setRobotAssigne(remplacement);
            contexte.setDernierMessage("Mission réaffectée à " + remplacement.getId());
            publierLog("Mission " + mission.getId() + " réaffectée à " + remplacement.getId() + ".", false);
            return remplacement;
        }

        throw new RobotException("Type de mission non supporté.");
    }

    private void gererEchecExecution(ContexteMission contexte, Robot robotEnEchec, RobotException exception) {
        String message = exception.getMessage() == null ? "Erreur inconnue" : exception.getMessage();
        contexte.setEtat(EtatExecutionMission.INTERROMPUE);
        contexte.setDernierMessage(message);

        // Ajout critique : en cas d'arrêt global, on conserve l'état pour une reprise ultérieure.
        if (!systemeDemarre || Thread.currentThread().isInterrupted() || message.contains("interrompue")) {
            publierLog("Mission interrompue et conservée pour reprise : " + contexte.getMission().getDescription(), false);
            return;
        }

        // Ajout critique : tentative de réaffectation automatique sur un autre robot sans perdre la mission.
        try {
            Mission missionDeReprise = construireMissionDeReprise(contexte, robotEnEchec);
            contexte.remplacerMission(missionDeReprise);
            Robot nouveauRobot = garantirRobotAssigne(contexte, robotEnEchec);
            libererRobotApresReaffectation(robotEnEchec, message);
            contexte.setEtat(EtatExecutionMission.PLANIFIEE);
            contexte.setDernierMessage("Mission replanifiée sur " + nouveauRobot.getId());
            publierLog("Réaffectation automatique réussie pour la mission " + missionDeReprise.getId() + ".", false);
            if (systemeDemarre) {
                executerMissionAutomatique(contexte);
            }
        } catch (RobotException reaffectationImpossible) {
            publierLog("Réaffectation impossible : " + reaffectationImpossible.getMessage(), true);
        }
    }

    private Mission construireMissionDeReprise(ContexteMission contexte, Robot robotEnEchec) {
        Mission mission = contexte.getMission();
        if (mission instanceof MissionLivraison missionLivraison && robotEnEchec instanceof RobotLivraison robotLivraison) {
            boolean colisDejaCharge = robotLivraison.isColisCharge() || robotLivraison.getColisActuel() != null;
            String origine = colisDejaCharge ? "Reprise @(" + robotLivraison.getX() + "," + robotLivraison.getY() + ")" : missionLivraison.getOrigine();
            int origineX = colisDejaCharge ? robotLivraison.getX() : missionLivraison.getOrigineX();
            int origineY = colisDejaCharge ? robotLivraison.getY() : missionLivraison.getOrigineY();
            return new MissionLivraison(
                    missionLivraison.getId(),
                    missionLivraison.getColis(),
                    origine, origineX, origineY,
                    missionLivraison.getDestination(), missionLivraison.getDestinationX(), missionLivraison.getDestinationY(),
                    missionLivraison.getCriticite(), missionLivraison.estSensible(), missionLivraison.getSensLivraison());
        }
        return mission;
    }

    private void libererRobotApresReaffectation(Robot robot, String raison) {
        if (robot instanceof RobotLivraison robotLivraison) {
            robotLivraison.libererMissionPourReaffectation(raison);
        } else if (robot instanceof RobotCompagnon robotCompagnon) {
            robotCompagnon.libererMissionPourReaffectation(raison);
        }
    }

    /**
     * Méthode historique conservée : elle relance seulement les robots occupés.
     * Ajout critique : si le système est arrêté, une exception explicite est levée.
     */
    public void executerToutesLesMissions() throws RobotException {
        if (!systemeDemarre) {
            throw new RobotException("Les robots doivent être démarrés avant exécution");
        }

        boolean executionPlanifiee = false;
        for (ContexteMission contexte : missionsPlanifiees) {
            if (!contexte.estTerminee() && !executionsParMission.containsKey(contexte.getMission().getId())) {
                executerMissionAutomatique(contexte);
                executionPlanifiee = true;
            }
        }

        if (!executionPlanifiee) {
            publierLog("Aucune mission planifiée à exécuter.", false);
        }
    }

    public void demarrerTous() {
        systemeDemarre = true;
        for (Robot robot : robots) {
            try {
                robot.demarrer();
            } catch (RobotException e) {
                publierLog("Démarrage impossible pour " + robot.getId() + " : " + e.getMessage(), true);
            }
        }
    }

    public void arreterTous() {
        systemeDemarre = false;
        for (Future<?> future : new ArrayList<>(missionsEnCours)) {
            future.cancel(true);
        }
        for (Robot robot : robots) {
            robot.arreter();
        }
        for (ContexteMission contexte : missionsPlanifiees) {
            if (!contexte.estTerminee()) {
                contexte.setEtat(EtatExecutionMission.INTERROMPUE);
                contexte.setDernierMessage("Interruption demandée par l'utilisateur");
            }
        }
        executionsParMission.clear();
    }

    public void interrompreActivitesSecondaires() {
        for (Robot robot : robots) {
            if (robot instanceof RobotCompagnon && !robot.estDisponible()) {
                robot.arreter();
            }
        }
        publierLog("Alerte générale : activités secondaires interrompues.", false);
    }

    public double calculerMoyenneEnergie() {
        return robots.stream().mapToDouble(Robot::getEnergie).average().orElse(0);
    }

    public int calculerHeuresTotales() {
        return robots.stream().mapToInt(Robot::getHeuresUtilisation).sum();
    }

    public long compterMissionsActives() {
        return missionsPlanifiees.stream().filter(c -> !c.estTerminee()).count();
    }

    public String construireRapport() {
        String etatRobots = robots.stream().map(Robot::toString).collect(Collectors.joining(System.lineSeparator()));
        String etatMissions = missionsPlanifiees.stream().map(ContexteMission::toString).collect(Collectors.joining(System.lineSeparator()));
        return etatRobots + System.lineSeparator() + System.lineSeparator() + "=== MISSIONS ===" + System.lineSeparator() + etatMissions;
    }

    /**
     * Attend la fin de toutes les missions utiles au mode test console.
     */
    public void attendreFinMissions() {
        for (Future<?> future : new ArrayList<>(missionsEnCours)) {
            try {
                future.get();
            } catch (Exception e) {
                publierLog("Attente interrompue : " + e.getMessage(), true);
            } finally {
                missionsEnCours.remove(future);
            }
        }
    }

    /**
     * Ajout additif : création de robot en utilisant uniquement les constructeurs existants.
     */
    public Robot creerRobot(String typeRobot, int x, int y, double energie) throws RobotException {
        String type = typeRobot == null ? "" : typeRobot.trim();
        Robot nouveauRobot;
        if ("RobotLivraison".equalsIgnoreCase(type)) {
            long index = robots.stream().filter(r -> r instanceof RobotLivraison).count() + 1;
            nouveauRobot = new RobotLivraison(String.format("RL-%02d", index), x, y, energie);
        } else if ("RobotCompagnon".equalsIgnoreCase(type)) {
            long index = robots.stream().filter(r -> r instanceof RobotCompagnon).count() + 1;
            nouveauRobot = new RobotCompagnon(String.format("RC-%02d", index), x, y, energie);
        } else {
            throw new RobotException("Type de robot inconnu : " + typeRobot);
        }
        ajouterRobot(nouveauRobot);
        return nouveauRobot;
    }

    /**
     * Ajout additif : recharge ciblée d'un robot par son identifiant.
     */
    public void rechargerRobot(String robotId, int energie) throws RobotException {
        Robot robot = chercherRobot(robotId);
        robot.recharger(energie);
        publierLog("Robot rechargé : " + robotId + " (+" + energie + "%).", false);
    }

    /**
     * Ajout additif : maintenance robuste pilotée par l'interface.
     */
    public String effectuerMaintenanceRobot(String robotId) throws RobotException {
        Robot robot = chercherRobot(robotId);
        if (robot.aBesoinMaintenance()) {
            robot.reinitialiserMaintenance();
            String message = "Robot " + robotId + " : maintenance effectuée et heures réinitialisées";
            publierLog(message, false);
            return message;
        }
        String message = "Robot " + robotId + " : pas besoin de maintenance maintenant";
        publierLog(message, false);
        return message;
    }

    public boolean isSystemeDemarre() {
        return systemeDemarre;
    }

    public List<ContexteMission> getMissionsPlanifiees() {
        return Collections.unmodifiableList(new ArrayList<>(missionsPlanifiees));
    }

    private Robot chercherRobot(String robotId) throws RobotException {
        return robots.stream()
                .filter(robot -> robot.getId().equalsIgnoreCase(robotId))
                .findFirst()
                .orElseThrow(() -> new RobotException("Robot introuvable : " + robotId));
    }

    /**
     * Arrête proprement l'exécuteur lorsque l'application se ferme.
     */
    public void shutdown() {
        executeurMissions.shutdownNow();
    }
}
