package robothealth.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

import robothealth.core.Robot;
import robothealth.core.RobotCompagnon;
import robothealth.core.RobotLivraison;
import robothealth.map.LieuHopital;
import robothealth.map.PlanHopital;

/**
 * Tableau de bord graphique affichant la grille, les lieux fixes et l'animation des robots.
 * L'effet de mouvement est obtenu grâce à un Timer qui interpole les positions.
 */
public class DashboardPanel extends JPanel {
    private static final int CELL_SIZE = 55;
    private static final int TOP_BANNER_HEIGHT = 80;

    private final List<Robot> robots;
    private final Map<String, Point2D.Double> displayedPositions;
    private final Timer timer;
    private final ImageIcon livraisonIcon;
    private final ImageIcon compagnonIcon;
    private final ImageIcon pharmacieIcon;
    private final ImageIcon administrationIcon;

    public DashboardPanel(List<Robot> robots) {
        this.robots = robots;
        this.displayedPositions = new HashMap<>();
        this.livraisonIcon = RobotIconFactory.createIcon(new Color(30, 144, 255), "L");
        this.compagnonIcon = RobotIconFactory.createIcon(new Color(46, 160, 67), "C");
        this.pharmacieIcon = RobotIconFactory.createIcon(new Color(214, 69, 65), "🏥");
        this.administrationIcon = RobotIconFactory.createIcon(new Color(116, 84, 247), "🏢");

        int largeur = PlanHopital.LARGEUR_GRILLE * CELL_SIZE + 1;
        int hauteur = TOP_BANNER_HEIGHT + PlanHopital.HAUTEUR_GRILLE * CELL_SIZE + 1;
        setPreferredSize(new Dimension(largeur, hauteur));
        setBackground(Color.WHITE);

        timer = new Timer(30, e -> {
            boolean needsRepaint = false;
            for (Robot robot : robots) {
                Point2D.Double current = displayedPositions.computeIfAbsent(robot.getId(),
                        id -> new Point2D.Double(robot.getX(), robot.getY()));
                double dx = robot.getX() - current.x;
                double dy = robot.getY() - current.y;
                if (Math.abs(dx) > 0.01 || Math.abs(dy) > 0.01) {
                    current.x += dx * 0.30;
                    current.y += dy * 0.30;
                    needsRepaint = true;
                } else {
                    current.x = robot.getX();
                    current.y = robot.getY();
                }
            }
            if (needsRepaint) {
                repaint();
            }
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        dessinerBandeau(g2);
        dessinerGrille(g2);
        dessinerLieuxFixes(g2);
        dessinerRobots(g2);
        g2.dispose();
    }

    private void dessinerBandeau(Graphics2D g2) {
        g2.setColor(new Color(240, 245, 252));
        g2.fillRoundRect(10, 10, getWidth() - 20, TOP_BANNER_HEIGHT - 20, 18, 18);
        g2.setColor(new Color(120, 130, 150));
        g2.drawRoundRect(10, 10, getWidth() - 20, TOP_BANNER_HEIGHT - 20, 18, 18);

        pharmacieIcon.paintIcon(this, g2, 30, 24);
        administrationIcon.paintIcon(this, g2, 220, 24);

        g2.setColor(new Color(35, 45, 60));
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.drawString("Pharmacie", 74, 47);
        g2.drawString("Administration", 264, 47);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2.drawString("Points fixes visibles dans le plan hospitalier", 460, 47);
    }

    private void dessinerGrille(Graphics2D g2) {
        int top = TOP_BANNER_HEIGHT;
        g2.setColor(new Color(235, 239, 244));
        for (int i = 0; i <= PlanHopital.LARGEUR_GRILLE; i++) {
            int p = i * CELL_SIZE;
            g2.drawLine(p, top, p, top + PlanHopital.HAUTEUR_GRILLE * CELL_SIZE);
            g2.drawLine(0, top + p, PlanHopital.LARGEUR_GRILLE * CELL_SIZE, top + p);
        }
        g2.setColor(new Color(180, 190, 200));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(0, top, PlanHopital.LARGEUR_GRILLE * CELL_SIZE, PlanHopital.HAUTEUR_GRILLE * CELL_SIZE);
    }

    private void dessinerLieuxFixes(Graphics2D g2) {
        for (LieuHopital lieu : LieuHopital.values()) {
            Point px = toPixel(lieu.getX(), lieu.getY());
            Color couleur = lieu.isPointPatient() ? new Color(255, 236, 179) : new Color(224, 231, 255);
            g2.setColor(couleur);
            g2.fillRoundRect(px.x - 40, px.y - 18, 80, 36, 12, 12);
            g2.setColor(new Color(120, 130, 150));
            g2.drawRoundRect(px.x - 40, px.y - 18, 80, 36, 12, 12);
            g2.setColor(new Color(50, 60, 70));
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString(lieu.getLibelle(), px.x - 34, px.y + 4);
        }
    }

    private void dessinerRobots(Graphics2D g2) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        for (Robot robot : robots) {
            Point2D.Double logical = displayedPositions.computeIfAbsent(robot.getId(),
                    id -> new Point2D.Double(robot.getX(), robot.getY()));
            Point px = toPixel(logical);
            ImageIcon icon = robot instanceof RobotLivraison ? livraisonIcon : compagnonIcon;
            icon.paintIcon(this, g2, px.x - 18, px.y - 18);

            g2.setColor(Color.DARK_GRAY);
            g2.drawString(robot.getId(), px.x - 18, px.y - 24);
            g2.drawString(String.format("%.0f%%", robot.getEnergie()), px.x - 12, px.y + 32);
            g2.drawString(robot.getStatutMissionAffiche(), Math.max(4, px.x - 60), px.y + 46);

            if (robot instanceof RobotCompagnon compagnon) {
                int stress = compagnon.getNiveauStress();
                g2.setColor(stress > 7 ? new Color(220, 60, 60) : new Color(70, 170, 90));
                g2.fillOval(px.x + 15, px.y - 18, 10, 10);
            }
        }
    }

    private Point toPixel(int logicalX, int logicalY) {
        int x = (int) Math.round(logicalX * CELL_SIZE + CELL_SIZE / 2.0);
        int y = TOP_BANNER_HEIGHT + (int) Math.round(logicalY * CELL_SIZE + CELL_SIZE / 2.0);
        return new Point(x, y);
    }

    private Point toPixel(Point2D.Double logical) {
        int x = (int) Math.round(logical.x * CELL_SIZE + CELL_SIZE / 2.0);
        int y = TOP_BANNER_HEIGHT + (int) Math.round(logical.y * CELL_SIZE + CELL_SIZE / 2.0);
        return new Point(x, y);
    }
}
