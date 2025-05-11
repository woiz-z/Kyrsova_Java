package music.Renderer;

import music.Music.MusicCompilation;

import javax.swing.*;
import java.awt.*;

public class CompilationListRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof MusicCompilation) {
            MusicCompilation compilation = (MusicCompilation) value;
            setText(String.format("<html><div style='padding:5px;'>" +
                            "<b style='font-size:14px; color:#333;'>%s</b><br>" +
                            "<span style='color:#666; font-size:12px;'>%d треків • %d хв</span>" +
                            "</div></html>",
                    compilation.getTitle(),
                    compilation.getTracks().size(),
                    compilation.calculateTotalDuration().toMinutes()));
            setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

            if (isSelected) {
                setBackground(new Color(220, 240, 255));
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(180, 220, 255)),
                        BorderFactory.createEmptyBorder(5, 15, 5, 15)));
            } else {
                setBackground(index % 2 == 0 ? new Color(255, 255, 255) : new Color(245, 248, 250));
            }
        }
        return this;
    }
}