package robothealth.map;

/**
 * Définit le sens logique d'une mission de livraison.
 * Le besoin demandé par le projet impose désormais les sens
 * "Patient -> Administration", "Administration -> Patient",
 * "Patient -> Pharmacie" et "Pharmacie -> Patient".
 */
public enum SensLivraison {
    PATIENT_VERS_ADMINISTRATION("Patient → Administration"),
    ADMINISTRATION_VERS_PATIENT("Administration → Patient"),
    PATIENT_VERS_PHARMACIE("Patient → Pharmacie"),
    PHARMACIE_VERS_PATIENT("Pharmacie → Patient");

    private final String libelle;

    SensLivraison(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    /**
     * Détermine le point de départ réel sans casser la logique existante.
     * Le point patient est fourni par l'interface puis combiné avec le sens choisi.
     */
    public LieuHopital determinerOrigine(LieuHopital pointPatient) {
        if (this == ADMINISTRATION_VERS_PATIENT) {
            return LieuHopital.ADMINISTRATION;
        }
        if (this == PHARMACIE_VERS_PATIENT) {
            return LieuHopital.PHARMACIE;
        }
        return pointPatient;
    }

    /**
     * Détermine la destination finale réelle sans modifier l'architecture métier.
     */
    public LieuHopital determinerDestination(LieuHopital pointPatient) {
        if (this == PATIENT_VERS_ADMINISTRATION) {
            return LieuHopital.ADMINISTRATION;
        }
        if (this == PATIENT_VERS_PHARMACIE) {
            return LieuHopital.PHARMACIE;
        }
        return pointPatient;
    }

    /**
     * Indique si la mission implique la pharmacie et nécessite donc le code sécurisé associé.
     */
    public boolean impliquePharmacie() {
        return this == PATIENT_VERS_PHARMACIE || this == PHARMACIE_VERS_PATIENT;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
