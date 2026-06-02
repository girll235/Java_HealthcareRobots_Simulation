package robothealth.core;

/**
 * Niveau de priorité d'une mission.
 */
public enum CriticiteMission {
    ROUTINE,
    PRIORITAIRE,
    URGENCE_VITALE;

    /**
     * Retourne vrai uniquement pour les missions critiques.
     */
    public boolean estUrgente() {
        return this == URGENCE_VITALE;
    }
}
