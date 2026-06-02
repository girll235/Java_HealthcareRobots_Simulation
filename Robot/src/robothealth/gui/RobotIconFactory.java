package robothealth.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

/**
 * Fabrique de petites icônes dessinées dynamiquement.
 * Cela évite de dépendre d'images externes tout en gardant un rendu visuel clair.
 */
public final class RobotIconFactory {
    private RobotIconFactory() {
        // Classe utilitaire : aucune instance nécessaire.
    }

    public static ImageIcon createIcon(Color color, String label) {
        BufferedImage image = new BufferedImage(36, 36, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(color);
        g2.fillRoundRect(3, 3, 30, 30, 12, 12);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(3, 3, 30, 30, 12, 12);

        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(label);
        int x = (36 - textWidth) / 2;
        int y = ((36 - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(label, x, y);
        g2.dispose();
        return new ImageIcon(image);
    }
}
