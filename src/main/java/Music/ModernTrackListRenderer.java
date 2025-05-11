package Music;

import javax.swing.*;
import java.awt.*;

public class ModernTrackListRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        if (value instanceof MusicTrack) {
            MusicTrack track = (MusicTrack) value;
            setText(String.format("<html><div style='padding:5px;'>" +
                            "<b style='font-size:14px; color:#333;'>%s</b><br>" +
                            "<span style='color:#666; font-size:12px;'>%s • <span style='color:#5e8c31;'>%s</span> • %d:%02d</span>" +
                            "</div></html>",
                    track.getTitle(),
                    track.getArtist(),
                    track.getGenre(),
                    track.getDuration().toMinutes(),
                    track.getDuration().getSeconds() % 60));

            if (isSelected) {
                setBackground(new Color(220, 240, 255));
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(180, 220, 255)),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)));
            } else {
                setBackground(index % 2 == 0 ? new Color(255, 255, 255, 200) : new Color(245, 248, 250, 200));
            }
        }
        return this;
    }
}