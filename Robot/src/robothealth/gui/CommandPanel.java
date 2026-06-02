package robothealth.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import robothealth.core.CriticiteMission;
import robothealth.core.ModeAmbiance;
import robothealth.core.Robot;
import robothealth.exceptions.RobotException;
import robothealth.manager.GestionnaireHospitalier;
import robothealth.map.LieuHopital;
import robothealth.map.SensLivraison;
import robothealth.missions.MissionCompagnon;
import robothealth.missions.MissionLivraison;

/**
 * Panneau de commandes de l'application.
 * Les missions de livraison et d'accompagnement sont créées ici puis exécutées automatiquement.
 */
public class CommandPanel extends JPanel {
    private final GestionnaireHospitalier gestionnaire;
    private final Runnable onRefresh;

    public CommandPanel(GestionnaireHospitalier gestionnaire, Runnable onRefresh) {
        this.gestionnaire = gestionnaire;
        this.onRefresh = onRefresh;
        setLayout(new BorderLayout());
        add(buildTabs(), BorderLayout.CENTER);
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Livraison", buildLivraisonPanel());
        tabs.addTab("Compagnon", buildCompagnonPanel());
        tabs.addTab("Flotte", buildFleetPanel());
        tabs.addTab("Système", buildSystemPanel());
        return tabs;
    }

    private JPanel buildLivraisonPanel() {
        JPanel panel = createGridPanel();
        JTextField colisField = new JTextField("Dossier médical");
        JComboBox<SensLivraison> sensBox = new JComboBox<>(SensLivraison.values());
        JComboBox<LieuHopital> pointPatientBox = new JComboBox<>(LieuHopital.pointsPatient().toArray(new LieuHopital[0]));
        JComboBox<CriticiteMission> criticiteBox = new JComboBox<>(CriticiteMission.values());
        JTextField codeField = new JTextField("PHARM-2025");

        panel.add(new JLabel("Colis"));
        panel.add(colisField);
        panel.add(new JLabel("Sens de livraison"));
        panel.add(sensBox);
        panel.add(new JLabel("Point patient"));
        panel.add(pointPatientBox);
        panel.add(new JLabel("Criticité"));
        panel.add(criticiteBox);
        panel.add(new JLabel("Code sécurité"));
        panel.add(codeField);
        panel.add(new JLabel("Planification"));
        panel.add(new JLabel("La mission est planifiée puis exécutée via le bouton système"));

        // Ajout métier : le code pharmacie n'est réellement utilisé que si le trajet implique la pharmacie.
        sensBox.addActionListener(e -> {
            SensLivraison sensSelectionne = (SensLivraison) sensBox.getSelectedItem();
            if (sensSelectionne != null && sensSelectionne.impliquePharmacie()) {
                codeField.setText("PHARM-2025");
            } else {
                codeField.setText("");
            }
        });

        JButton lancer = new JButton("Planifier mission livraison");
        JButton executer = new JButton("Exécuter mission");
        lancer.addActionListener(e -> {
            try {
                SensLivraison sens = (SensLivraison) sensBox.getSelectedItem();
                LieuHopital pointPatient = (LieuHopital) pointPatientBox.getSelectedItem();
                LieuHopital origine = sens.determinerOrigine(pointPatient);
                LieuHopital destination = sens.determinerDestination(pointPatient);

                MissionLivraison mission = new MissionLivraison(
                        "LIV-" + UUID.randomUUID().toString().substring(0, 8),
                        colisField.getText(),
                        origine.getLibelle(), origine.getX(), origine.getY(),
                        destination.getLibelle(), destination.getX(), destination.getY(),
                        (CriticiteMission) criticiteBox.getSelectedItem(),
                        sens.impliquePharmacie(),
                        sens);
                Robot robot = gestionnaire.assignerMission(mission, codeField.getText());
                JOptionPane.showMessageDialog(this,
                        "Mission planifiée sur " + robot.getId() + ". Utilisez 'Exécuter mission' pour démarrer ou reprendre.",
                        "Mission planifiée", JOptionPane.INFORMATION_MESSAGE);
                onRefresh.run();
            } catch (RobotException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        executer.addActionListener(e -> {
            try {
                gestionnaire.executerToutesLesMissions();
                onRefresh.run();
            } catch (RobotException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(lancer);
        panel.add(executer);
        return wrap(panel);
    }

    private JPanel buildCompagnonPanel() {
        JPanel panel = createGridPanel();
        JTextField patientField = new JTextField("Patient 12");
        JComboBox<LieuHopital> chambreBox = new JComboBox<>(LieuHopital.pointsPatient().toArray(new LieuHopital[0]));
        JSpinner stressSpinner = new JSpinner(new SpinnerNumberModel(8, 1, 10, 1));
        JComboBox<ModeAmbiance> ambianceBox = new JComboBox<>(ModeAmbiance.values());
        JComboBox<CriticiteMission> criticiteBox = new JComboBox<>(CriticiteMission.values());
        JTextField codeField = new JTextField("PATIENT-2025");

        panel.add(new JLabel("Patient"));
        panel.add(patientField);
        panel.add(new JLabel("Lieu patient"));
        panel.add(chambreBox);
        panel.add(new JLabel("Stress initial"));
        panel.add(stressSpinner);
        panel.add(new JLabel("Mode ambiance"));
        panel.add(ambianceBox);
        panel.add(new JLabel("Criticité"));
        panel.add(criticiteBox);
        panel.add(new JLabel("Code dossier"));
        panel.add(codeField);
        panel.add(new JLabel("Planification"));
        panel.add(new JLabel("La mission est planifiée puis exécutée via le bouton système"));

        JButton lancer = new JButton("Planifier mission compagnon");
        JButton executer1 = new JButton("Exécuter mission");
        lancer.addActionListener(e -> {
            try {
                LieuHopital lieu = (LieuHopital) chambreBox.getSelectedItem();
                MissionCompagnon mission = new MissionCompagnon(
                        "CMP-" + UUID.randomUUID().toString().substring(0, 8),
                        patientField.getText(),
                        lieu.getLibelle(),
                        lieu.getX(), lieu.getY(),
                        (Integer) stressSpinner.getValue(),
                        (ModeAmbiance) ambianceBox.getSelectedItem(),
                        (CriticiteMission) criticiteBox.getSelectedItem(),
                        true);
                Robot robot = gestionnaire.assignerMission(mission, codeField.getText());
                JOptionPane.showMessageDialog(this,
                        "Mission planifiée sur " + robot.getId() + ". Utilisez 'Exécuter mission' pour démarrer ou reprendre.",
                        "Mission planifiée", JOptionPane.INFORMATION_MESSAGE);
                onRefresh.run();
                
            } catch (RobotException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        executer1.addActionListener(e -> {
            try {
                gestionnaire.executerToutesLesMissions();
                onRefresh.run();
            } catch (RobotException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        

        panel.add(lancer);
        panel.add(executer1);
        return wrap(panel);
    }

    private JPanel buildFleetPanel() {
        JPanel panel = createGridPanel();
        JComboBox<String> typeRobotBox = new JComboBox<>(new String[] { "RobotLivraison", "RobotCompagnon" });
        JSpinner xSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 11, 1));
        JSpinner ySpinner = new JSpinner(new SpinnerNumberModel(1, 0, 11, 1));
        JSpinner energieSpinner = new JSpinner(new SpinnerNumberModel(80, 0, 100, 5));
        JButton creer = new JButton("Créer robot");
        JButton recharger = new JButton("Recharge");
        JButton maintenance = new JButton("Maintenance");

        panel.add(new JLabel("Type de robot"));
        panel.add(typeRobotBox);
        panel.add(new JLabel("Position X"));
        panel.add(xSpinner);
        panel.add(new JLabel("Position Y"));
        panel.add(ySpinner);
        panel.add(new JLabel("Énergie initiale"));
        panel.add(energieSpinner);

        creer.addActionListener(e -> {
            try {
                Robot robot = gestionnaire.creerRobot((String) typeRobotBox.getSelectedItem(),
                        (Integer) xSpinner.getValue(),
                        (Integer) ySpinner.getValue(),
                        ((Integer) energieSpinner.getValue()).doubleValue());
                JOptionPane.showMessageDialog(this, "Robot créé : " + robot.getId(), "Création", JOptionPane.INFORMATION_MESSAGE);
                onRefresh.run();
            } catch (RobotException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        recharger.addActionListener(e -> {
            try {
                String robotId = demanderRobotId();
                if (robotId == null) {
                    return;
                }
                String quantite = JOptionPane.showInputDialog(this, "Énergie à ajouter", "20");
                if (quantite == null) {
                    return;
                }
                gestionnaire.rechargerRobot(robotId, Integer.parseInt(quantite.trim()));
                JOptionPane.showMessageDialog(this, "Recharge enregistrée pour " + robotId, "Recharge", JOptionPane.INFORMATION_MESSAGE);
                onRefresh.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        maintenance.addActionListener(e -> {
            try {
                String robotId = demanderRobotId();
                if (robotId == null) {
                    return;
                }
                String message = gestionnaire.effectuerMaintenanceRobot(robotId);
                JOptionPane.showMessageDialog(this, message, "Maintenance", JOptionPane.INFORMATION_MESSAGE);
                onRefresh.run();
            } catch (RobotException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(creer);
        panel.add(recharger);
        panel.add(maintenance);
        panel.add(new JLabel("Gestion flotte"));
        panel.add(new JLabel("Création, recharge et maintenance robustes"));
        return wrap(panel);
    }

    private JPanel buildSystemPanel() {
        JPanel panel = createGridPanel();
        JButton demarrer = new JButton("Démarrer tous");
        JButton executer = new JButton("Exécuter mission");
        JButton arreter = new JButton("Arrêter tous");
        JButton alerte = new JButton("Alerte générale");
        JButton stats = new JButton("Statistiques");

        demarrer.addActionListener(e -> {
            gestionnaire.demarrerTous();
            onRefresh.run();
        });
        executer.addActionListener(e -> {
            try {
                gestionnaire.executerToutesLesMissions();
                onRefresh.run();
            } catch (RobotException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        arreter.addActionListener(e -> {
            gestionnaire.arreterTous();
            onRefresh.run();
        });
        alerte.addActionListener(e -> {
            gestionnaire.interrompreActivitesSecondaires();
            onRefresh.run();
        });
        stats.addActionListener(e -> JOptionPane.showMessageDialog(this,
                String.format("Énergie moyenne : %.2f%%\nHeures totales : %d\nMissions actives : %d\nSystème démarré : %s\n\n%s",
                        gestionnaire.calculerMoyenneEnergie(),
                        gestionnaire.calculerHeuresTotales(),
                        gestionnaire.compterMissionsActives(),
                        gestionnaire.isSystemeDemarre() ? "Oui" : "Non",
                        gestionnaire.construireRapport()),
                "Statistiques", JOptionPane.INFORMATION_MESSAGE));

        panel.add(demarrer);
        panel.add(executer);
        panel.add(arreter);
        panel.add(alerte);
        panel.add(stats);
        return wrap(panel);
    }

    private String demanderRobotId() {
        if (gestionnaire.getRobots().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aucun robot dans la flotte.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        String[] ids = gestionnaire.getRobots().stream().map(Robot::getId).toArray(String[]::new);
        return (String) JOptionPane.showInputDialog(this,
                "Robot concerné",
                "Sélection robot",
                JOptionPane.QUESTION_MESSAGE,
                null,
                ids,
                ids[0]);
    }

    private JPanel createGridPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        return panel;
    }

    private JPanel wrap(JPanel content) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(content, BorderLayout.NORTH);
        return wrapper;
    }
}
