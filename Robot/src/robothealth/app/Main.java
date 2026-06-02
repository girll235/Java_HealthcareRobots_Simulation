package robothealth.app;

import java.awt.GraphicsEnvironment;

import javax.swing.SwingUtilities;

import robothealth.core.CriticiteMission;
import robothealth.core.ModeAmbiance;
import robothealth.core.RobotCompagnon;
import robothealth.core.RobotLivraison;
import robothealth.gui.MainFrame;
import robothealth.manager.GestionnaireHospitalier;
import robothealth.map.LieuHopital;
import robothealth.map.SensLivraison;
import robothealth.missions.MissionCompagnon;
import robothealth.missions.MissionLivraison;
import robothealth.util.SecurityService;

/**
 * Point d'entrée principal de l'application.
 * Il crée le service de sécurité, la flotte de robots et la fenêtre Swing.
 */
public class Main {
    public static void main(String[] args) {
        SecurityService securityService = new SecurityService("PHARM-2025", "PATIENT-2025");
        GestionnaireHospitalier gestionnaire = new GestionnaireHospitalier(securityService);

        RobotLivraison livraison1 = new RobotLivraison("RL-01", 1, 1, 100);
        RobotLivraison livraison2 = new RobotLivraison("RL-02", 2, 8, 78);
        RobotCompagnon compagnon1 = new RobotCompagnon("RC-01", 7, 7, 92);
        RobotCompagnon compagnon2 = new RobotCompagnon("RC-02", 10, 2, 85);

        gestionnaire.ajouterRobot(livraison1);
        gestionnaire.ajouterRobot(livraison2);
        gestionnaire.ajouterRobot(compagnon1);
        gestionnaire.ajouterRobot(compagnon2);

        try {
            // Mission de démonstration : elle est désormais planifiée puis exécutée explicitement.
            gestionnaire.assignerMission(new MissionLivraison(
                    "LIV-DEMO",
                    "Vaccins",
                    LieuHopital.ADMINISTRATION.getLibelle(), LieuHopital.ADMINISTRATION.getX(), LieuHopital.ADMINISTRATION.getY(),
                    LieuHopital.BLOC_OPERATOIRE.getLibelle(), LieuHopital.BLOC_OPERATOIRE.getX(), LieuHopital.BLOC_OPERATOIRE.getY(),
                    CriticiteMission.URGENCE_VITALE,
                    false,
                    SensLivraison.ADMINISTRATION_VERS_PATIENT), "");

            gestionnaire.assignerMission(new MissionCompagnon(
                    "CMP-DEMO",
                    "Mme Sana",
                    LieuHopital.CHAMBRE_A12.getLibelle(),
                    LieuHopital.CHAMBRE_A12.getX(), LieuHopital.CHAMBRE_A12.getY(),
                    8,
                    ModeAmbiance.MUSIQUE,
                    CriticiteMission.PRIORITAIRE,
                    true), "PATIENT-2025");

            gestionnaire.demarrerTous();
            gestionnaire.executerToutesLesMissions();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (GraphicsEnvironment.isHeadless()) {
            gestionnaire.attendreFinMissions();
            System.out.println("=== RAPPORT DE TEST ===");
            System.out.println(gestionnaire.construireRapport());
            gestionnaire.shutdown();
            return;
        }

        SwingUtilities.invokeLater(() -> new MainFrame(gestionnaire).setVisible(true));
    }
}
