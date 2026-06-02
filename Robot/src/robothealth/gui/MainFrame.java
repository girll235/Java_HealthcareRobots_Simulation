package robothealth.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import robothealth.manager.GestionnaireHospitalier;

/**
 * Fenêtre principale Swing.
 * Elle assemble la carte, les commandes et le panneau de logs.
 */
public class MainFrame extends JFrame {
    private final GestionnaireHospitalier gestionnaire;
    private final DashboardPanel dashboardPanel;
    private final LogPanel logPanel;
    private final JLabel statusLabel;

    public MainFrame(GestionnaireHospitalier gestionnaire) {
        super("Station de Contrôle Médicale - Robot Health");
        this.gestionnaire = gestionnaire;
        this.dashboardPanel = new DashboardPanel(gestionnaire.getRobots());
        this.logPanel = new LogPanel();
        this.statusLabel = new JLabel("Prêt", SwingConstants.LEFT);

        gestionnaire.addJournalListener(logPanel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        add(buildCenter(), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(1360, 900));
        pack();
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                gestionnaire.shutdown();
            }
        });

        Timer infoTimer = new Timer(300, e -> mettreAJourStatut());
        infoTimer.start();
    }

    private JSplitPane buildCenter() {
        JPanel left = new JPanel(new BorderLayout());
        left.add(new JScrollPane(dashboardPanel), BorderLayout.CENTER);

        CommandPanel commandPanel = new CommandPanel(gestionnaire, () -> SwingUtilities.invokeLater(() -> {
            dashboardPanel.repaint();
            mettreAJourStatut();
        }));

        JSplitPane right = new JSplitPane(JSplitPane.VERTICAL_SPLIT, commandPanel, logPanel);
        right.setResizeWeight(0.44);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        mainSplit.setResizeWeight(0.60);
        return mainSplit;
    }

    private void mettreAJourStatut() {
        statusLabel.setText(String.format(
                "Robots : %d | Énergie moyenne : %.1f%% | Heures totales : %d | Missions planifiées/non terminées : %d | Système : %s",
                gestionnaire.getRobots().size(),
                gestionnaire.calculerMoyenneEnergie(),
                gestionnaire.calculerHeuresTotales(),
                gestionnaire.compterMissionsActives(),
                gestionnaire.isSystemeDemarre() ? "démarré" : "arrêté"));
    }
}
