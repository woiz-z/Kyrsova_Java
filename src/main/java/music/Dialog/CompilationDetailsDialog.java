package music.Dialog;

import music.Panel.ButtonPanel;
import music.Manager.DiscManager;
import music.Panel.HeaderPanel;
import music.Music.MusicCompilation;
import music.Panel.TrackListPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CompilationDetailsDialog extends JDialog {
    public final MusicCompilation compilation;
    private final DiscManager discManager;

    public CompilationDetailsDialog(JFrame parent, MusicCompilation compilation) {
        super(parent, "Деталі збірки: " + compilation.getTitle(), true);
        this.compilation = compilation;
        this.discManager = new DiscManager();
        initializeUI();
    }

    private void initializeUI() {
        setSize(1400, 750);
        setLocationRelativeTo(getParent());
        setResizable(true);

        // Main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(245, 248, 250);
                Color color2 = new Color(230, 235, 240);
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        add(mainPanel);

        // Header panel
        HeaderPanel headerPanel = new HeaderPanel(compilation);
        mainPanel.add(headerPanel.getPanel(), BorderLayout.NORTH);

        // Track list panel
        TrackListPanel trackListPanel = new TrackListPanel(this, compilation);
        mainPanel.add(trackListPanel.getPanel(), BorderLayout.CENTER);

        // Button panel
        ButtonPanel buttonPanel = new ButtonPanel(this, compilation, trackListPanel);
        mainPanel.add(buttonPanel.getPanel(), BorderLayout.SOUTH);
    }

    public void updateTracksInDatabase() {

    }
}