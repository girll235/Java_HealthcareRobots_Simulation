package robothealth.map;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Classe utilitaire centralisant les informations fixes du plan de l'hôpital.
 * Elle sert à la fois au moteur métier et à l'affichage graphique.
 */
public final class PlanHopital {
    public static final String RESEAU_HOSPITALIER = "MED-NET-HOSPITAL";
    public static final int LARGEUR_GRILLE = 12;
    public static final int HAUTEUR_GRILLE = 12;

    private PlanHopital() {
        // Empêche l'instanciation car la classe ne contient que des méthodes statiques.
    }

    /**
     * Calcule une distance de Manhattan, parfaitement adaptée à une grille.
     * Ce choix garantit un nombre minimal de cases parcourues lorsqu'il n'y a pas d'obstacle.
     */
    public static int distanceMinimale(int x1, int y1, int x2, int y2) {
        return Math.abs(x2 - x1) + Math.abs(y2 - y1);
    }

    /**
     * Retourne un trajet minimal case par case.
     * Le robot suit d'abord l'axe X puis l'axe Y, ce qui reste optimal sur une grille sans obstacle.
     */
    public static List<Point> calculerTrajetMinimal(int departX, int departY, int destinationX, int destinationY) {
        List<Point> trajet = new ArrayList<>();
        trajet.add(new Point(departX, departY));

        int xCourant = departX;
        int yCourant = departY;

        while (xCourant != destinationX) {
            xCourant += Integer.compare(destinationX, xCourant);
            trajet.add(new Point(xCourant, yCourant));
        }

        while (yCourant != destinationY) {
            yCourant += Integer.compare(destinationY, yCourant);
            trajet.add(new Point(xCourant, yCourant));
        }

        return Collections.unmodifiableList(trajet);
    }
}
