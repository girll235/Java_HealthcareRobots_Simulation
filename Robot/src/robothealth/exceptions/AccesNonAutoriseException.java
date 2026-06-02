package robothealth.exceptions;

/**
 * Signale un refus d'accès lié à un code de sécurité invalide.
 */
public class AccesNonAutoriseException extends RobotException {
    public AccesNonAutoriseException(String message) {
        super(message);
    }
}
