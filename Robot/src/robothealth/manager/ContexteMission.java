package robothealth.manager;

import robothealth.core.Robot;
import robothealth.missions.Mission;

/**
 * Conserve l'état d'une mission afin de permettre la reprise et la réaffectation.
 * Cette classe ajoute une couche de pilotage sans casser la hiérarchie métier existante.
 */
public class ContexteMission {
    private Mission mission;
    private Robot robotAssigne;
    private EtatExecutionMission etat;
    private int tentatives;
    private String dernierMessage;

    public ContexteMission(Mission mission, Robot robotAssigne) {
        this.mission = mission;
        this.robotAssigne = robotAssigne;
        this.etat = EtatExecutionMission.PLANIFIEE;
        this.tentatives = 0;
        this.dernierMessage = "Mission planifiée";
    }

    public synchronized Mission getMission() {
        return mission;
    }

    public synchronized void remplacerMission(Mission mission) {
        this.mission = mission;
    }

    public synchronized Robot getRobotAssigne() {
        return robotAssigne;
    }

    public synchronized void setRobotAssigne(Robot robotAssigne) {
        this.robotAssigne = robotAssigne;
    }

    public synchronized EtatExecutionMission getEtat() {
        return etat;
    }

    public synchronized void setEtat(EtatExecutionMission etat) {
        this.etat = etat;
    }

    public synchronized int getTentatives() {
        return tentatives;
    }

    public synchronized void incrementerTentatives() {
        this.tentatives++;
    }

    public synchronized String getDernierMessage() {
        return dernierMessage;
    }

    public synchronized void setDernierMessage(String dernierMessage) {
        this.dernierMessage = dernierMessage;
    }

    public synchronized boolean estTerminee() {
        return etat.estFinal();
    }

    @Override
    public synchronized String toString() {
        String robotId = robotAssigne == null ? "AUCUN" : robotAssigne.getId();
        return String.format("[%s] %s | robot=%s | tentatives=%d | message=%s",
                etat, mission.getDescription(), robotId, tentatives, dernierMessage);
    }
}
