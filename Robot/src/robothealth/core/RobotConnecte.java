package robothealth.core;

import robothealth.exceptions.RobotException;

/**
 * Extension de Robot pour les appareils capables de se connecter au réseau.
 * Cette classe contient toute la logique demandée autour de connecter() et envoyerDonnees().
 */
public abstract class RobotConnecte extends Robot implements Connectable {
    private boolean connecte;
    private String reseauConnecte;

    protected RobotConnecte(String id, int x, int y, double energie) {
        super(id, x, y, energie);
        this.connecte = false;
        this.reseauConnecte = null;
    }

    /**
     * connecter() :
     * 1) vérifie que le nom du réseau est valide,
     * 2) contrôle l'énergie minimale,
     * 3) mémorise le réseau,
     * 4) consomme l'énergie de connexion,
     * 5) écrit un log explicite.
     */
    @Override
    public synchronized void connecter(String reseau) throws RobotException {
        ajouterHistorique("Tentative de connexion au réseau : " + reseau);

        if (reseau == null || reseau.isBlank()) {
            journaliserErreur("connecter() a reçu un nom de réseau vide.");
            throw new RobotException("Connexion impossible : le nom du réseau est vide.");
        }

        if (connecte && reseau.equalsIgnoreCase(reseauConnecte)) {
            ajouterHistorique("Le robot est déjà connecté au réseau '" + reseauConnecte + "'.");
            return;
        }

        verifierEnergie(5);
        this.reseauConnecte = reseau.trim();
        this.connecte = true;
        consommerEnergie(5);
        ajouterHistorique("Connexion réussie au réseau '" + reseauConnecte + "'.");
    }

    @Override
    public synchronized void deconnecter() {
        String ancienReseau = reseauConnecte == null ? "aucun" : reseauConnecte;
        this.connecte = false;
        this.reseauConnecte = null;
        ajouterHistorique("Déconnexion du réseau '" + ancienReseau + "'.");
    }

    /**
     * envoyerDonnees() :
     * 1) refuse l'envoi si le robot n'est pas connecté,
     * 2) vérifie l'énergie minimale,
     * 3) consomme l'énergie réseau,
     * 4) journalise le contenu envoyé.
     *
     * Les exceptions apparaissent donc principalement dans deux cas :
     * - aucune connexion active,
     * - batterie insuffisante pour l'envoi.
     */
    @Override
    public synchronized void envoyerDonnees(String donnees) throws RobotException {
    	
        ajouterHistorique("Préparation de l'envoi des données...");

        if (!connecte) {
            journaliserErreur("envoyerDonnees() appelé sans connexion réseau active.");
            throw new RobotException("Le robot n'est connecté à aucun réseau.");
        }

        verifierEnergie(3);
        consommerEnergie(3);
        ajouterHistorique("Données envoyées via '" + reseauConnecte + "' : " + donnees);
    }

    public synchronized boolean isConnecte() {
        return connecte;
    }

    public synchronized String getReseauConnecte() {
        return reseauConnecte;
    }
}
