package robothealth.manager;

/**
 * État interne d'une mission planifiée par le gestionnaire.
 * Cet ajout est purement additif et n'altère pas l'architecture existante.
 */
public enum EtatExecutionMission {
    PLANIFIEE,
    EN_COURS,
    INTERROMPUE,
    TERMINEE;

    public boolean estFinal() {
        return this == TERMINEE;
    }
}
