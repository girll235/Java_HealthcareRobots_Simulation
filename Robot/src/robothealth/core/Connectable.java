package robothealth.core;

import robothealth.exceptions.RobotException;

/**
 * Contrat des robots capables de dialoguer avec un réseau hospitalier.
 * Les méthodes sont volontairement simples afin de rester cohérentes avec l'architecture d'origine.
 */
public interface Connectable {
    void connecter(String reseau) throws RobotException;
    void deconnecter();
    void envoyerDonnees(String donnees) throws RobotException;
}
