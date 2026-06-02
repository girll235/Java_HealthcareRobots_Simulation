package robothealth.util;

/**
 * Interface simple de diffusion de logs vers la console ou la GUI.
 */
public interface JournalListener {
    void onLog(String message, boolean erreur);
}
