package robothealth.exceptions;

/**
 * Exception levée lorsqu'une mission urgente ne peut plus être sécurisée énergétiquement.
 */
public class BatterieCritiqueUrgenceException extends RobotException {
    public BatterieCritiqueUrgenceException(String message) {
        super(message);
    }
}
