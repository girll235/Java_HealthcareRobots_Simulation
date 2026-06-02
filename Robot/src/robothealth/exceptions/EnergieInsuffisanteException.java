package robothealth.exceptions;

/**
 * Indique qu'un robot ne dispose pas d'assez d'énergie pour continuer.
 */
public class EnergieInsuffisanteException extends RobotException {
    public EnergieInsuffisanteException(String message) {
        super(message);
    }
}
