package robothealth.missions;

import robothealth.core.CriticiteMission;

/**
 * Contrat commun à toutes les missions du système.
 * Chaque mission expose un identifiant, une description, une criticité et une destination finale.
 */
public interface Mission {
    String getId();
    String getDescription();
    CriticiteMission getCriticite();
    int getDestinationX();
    int getDestinationY();
    boolean estSensible();
}
