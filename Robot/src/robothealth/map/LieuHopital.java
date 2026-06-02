package robothealth.map;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Représente les points fixes visibles dans l'hôpital.
 * Chaque valeur de l'énumération possède un nom lisible et une position fixe
 * dans la grille de l'interface graphique.
 */
public enum LieuHopital {
    PHARMACIE("Pharmacie", 1, 0, false),
    ADMINISTRATION("Administration", 10, 0, false),
    URGENCE("Urgence", 2, 4, true),
    CHAMBRE_A12("Chambre A12", 4, 9, true),
    BLOC_OPERATOIRE("Bloc opératoire", 8, 4, true),
    LABORATOIRE("Laboratoire", 9, 8, true),
    IMAGERIE("Imagerie", 1, 8, true);

    private final String libelle;
    private final int x;
    private final int y;
    private final boolean pointPatient;

    LieuHopital(String libelle, int x, int y, boolean pointPatient) {
        this.libelle = libelle;
        this.x = x;
        this.y = y;
        this.pointPatient = pointPatient;
    }

    public String getLibelle() {
        return libelle;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isPointPatient() {
        return pointPatient;
    }

    /**
     * Retourne uniquement les zones métier pouvant jouer le rôle de patient.
     */
    public static List<LieuHopital> pointsPatient() {
        return Arrays.stream(values())
                .filter(LieuHopital::isPointPatient)
                .collect(Collectors.toList());
    }

    /**
     * Recherche un lieu par son libellé affiché dans la GUI.
     */
    public static LieuHopital depuisLibelle(String libelle) {
        return Arrays.stream(values())
                .filter(lieu -> lieu.getLibelle().equalsIgnoreCase(libelle))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Lieu inconnu : " + libelle));
    }

    @Override
    public String toString() {
        return libelle;
    }
}
