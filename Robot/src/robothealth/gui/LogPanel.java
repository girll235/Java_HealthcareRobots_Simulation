package robothealth.gui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import robothealth.util.JournalListener;

/**
 * Panneau de journalisation temps réel.
 * Chaque ligne reçue est colorée selon qu'il s'agit d'un log normal ou d'une erreur.
 */
public class LogPanel extends JPanel implements JournalListener {
    private final JTextPane textPane;

    public LogPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Journal d'exécution"));
        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setBackground(new Color(248, 250, 252));
        add(new JScrollPane(textPane), BorderLayout.CENTER);
    }

    @Override
    public void onLog(String message, boolean erreur) {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = textPane.getStyledDocument();
                SimpleAttributeSet attrs = new SimpleAttributeSet();
                StyleConstants.setForeground(attrs, erreur ? new Color(180, 20, 20) : new Color(30, 30, 30));
                doc.insertString(doc.getLength(), message + System.lineSeparator(), attrs);
                textPane.setCaretPosition(doc.getLength());
            } catch (Exception ignored) {
                // On ignore silencieusement pour ne pas bloquer la GUI.
            }
        });
    }
}
