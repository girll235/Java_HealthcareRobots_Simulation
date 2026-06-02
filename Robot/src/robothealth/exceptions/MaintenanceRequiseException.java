package robothealth.exceptions;

/**
 * Exception levée lorsque le robot a dépassé son seuil de maintenance.
 */
public class MaintenanceRequiseException extends RobotException {
    public MaintenanceRequiseException(String message) {
        super(message);
    }
}
