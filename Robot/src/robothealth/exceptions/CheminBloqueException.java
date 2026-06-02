package robothealth.exceptions;

/**
 * Exception métier utilisée lorsqu'un déplacement est jugé impossible ou anormal.
 */
public class CheminBloqueException extends RobotException {
    public CheminBloqueException(String message) {
        super(message);
    }
}
