package dev.mccastle91.createcontrolling.client;

import dev.mccastle91.createcontrolling.panel.element.LabelElement;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;

/**
 * Fits a label's text into its footprint. Only the client has a font to
 * measure with, so this is where line breaks are decided: the designer uses
 * it to limit what can be typed and to tell the server how many lines a label
 * needs, the renderers to draw exactly those lines and nothing past them.
 */
public final class LabelText {

    private LabelText() {
    }

    /** Font units a line of {@code textWidth} px holds at {@code lineHeight} px per font line. */
    private static int capacity(Font font, float textWidth, float lineHeight) {
        return (int) Math.floor(textWidth * font.lineHeight / lineHeight + 1e-3f);
    }

    /** Every line the text breaks into at this width, however many that is. */
    private static List<String> split(Font font, String text, float textWidth, float lineHeight) {
        List<String> lines = new ArrayList<>();
        int capacity = capacity(font, textWidth, lineHeight);
        if (text.isEmpty() || capacity <= 0)
            return lines;
        for (FormattedText line : font.getSplitter().splitLines(text, capacity, Style.EMPTY))
            lines.add(line.getString());
        return lines;
    }

    /** Lines the text needs, wrapped at word breaks (a word too long for a line is broken). */
    public static int linesNeeded(Font font, String text, float textWidth, float lineHeight) {
        return Math.max(1, split(font, text, textWidth, lineHeight).size());
    }

    /** Whether the whole text shows in {@code maxLines} lines. */
    public static boolean fits(Font font, String text, float textWidth, float lineHeight, int maxLines) {
        if (maxLines <= 1)
            return font.width(text) <= capacity(font, textWidth, lineHeight);
        return split(font, text, textWidth, lineHeight).size() <= maxLines;
    }

    /**
     * The lines to draw: at most {@code maxLines}, none wider than the label.
     * Text that doesn't fit is cut off rather than drawn outside.
     */
    public static List<String> lines(Font font, String text, float textWidth, float lineHeight, int maxLines) {
        if (maxLines <= 1) {
            int capacity = capacity(font, textWidth, lineHeight);
            List<String> single = new ArrayList<>();
            if (!text.isEmpty() && capacity > 0)
                single.add(font.width(text) <= capacity ? text : font.plainSubstrByWidth(text, capacity));
            return single;
        }
        List<String> lines = split(font, text, textWidth, lineHeight);
        return lines.size() > maxLines ? lines.subList(0, maxLines) : lines;
    }

    /** The lines a label shows, within the footprint it has. */
    public static List<String> lines(Font font, LabelElement label) {
        return lines(font, label.text(), label.textWidth(), label.lineHeight(), label.lines());
    }
}
