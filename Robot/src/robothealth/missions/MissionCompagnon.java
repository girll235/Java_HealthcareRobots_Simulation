package robothealth.missions;

import robothealth.core.CriticiteMission;
import robothealth.core.ModeAmbiance;

/**
 * Représente une mission d'assistance humaine auprès d'un patient.
 * Le robot compagnon se déplace vers la chambre puis applique le mode d'ambiance demandé.
 */
public class MissionCompagnon implements Mission {
    private final String id;
    private final String patient;
    private final String chambre;
    private final int destinationX;
    private final int destinationY;
    private final int niveauStressInitial;
    private final ModeAmbiance modeAmbiance;
    private final CriticiteMission criticite;
    private final boolean accesDossierPatient;

    public MissionCompagnon(String id, String patient, String chambre, int destinationX, int destinationY,
            int niveauStressInitial, ModeAmbiance modeAmbiance,
            CriticiteMission criticite, boolean accesDossierPatient) {
        this.id = id;
        this.patient = patient;
        this.chambre = chambre;
        this.destinationX = destinationX;
        this.destinationY = destinationY;
        this.niveauStressInitial = niveauStressInitial;
        this.modeAmbiance = modeAmbiance;
        this.criticite = criticite;
        this.accesDossierPatient = accesDossierPatient;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getPatient() {
        return patient;
    }

    public String getChambre() {
        return chambre;
    }

    public int getNiveauStressInitial() {
        return niveauStressInitial;
    }

    public ModeAmbiance getModeAmbiance() {
        return modeAmbiance;
    }

    @Override
    public String getDescription() {
        return "Accompagnement de " + patient + " en " + chambre;
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
        return accesDossierPatient;
    }
}
