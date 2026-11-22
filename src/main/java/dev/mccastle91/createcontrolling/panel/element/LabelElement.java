package dev.mccastle91.createcontrolling.panel.element;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

/**
 * Static text annotating a console. Sends and receives nothing, never takes
 * clicks, and may sit over other components as a caption. Unlike every other
 * component its footprint is player-sized: a width, and a scale that sets
 * the text height; the depth is one text line, or up to {@link #MAX_LINES}
 * when the label wraps.
 *
 * Text is measured with the client's font, which a server doesn't have: the
 * designer works out how many lines the text takes and sends that along, and
 * the renderers never draw past the footprint whatever they are given.
 */
public class LabelElement extends PanelElement {

    public static final String TAG_TEXT = "Text";
    public static final String TAG_COLOR = "TextColor";
    public static final String TAG_SCALE = "Scale";
    public static final String TAG_ALIGNMENT = "Align";
    public static final String TAG_WIDTH = "Width";
    public static final String TAG_WRAP = "Wrap";
    public static final String TAG_LINES = "Lines";

    public static final int MAX_TEXT_LENGTH = 64;
    public static final int DEFAULT_COLOR = 0xEDE4D4; // warm off-white, Create-ish
    public static final int MAX_SCALE = 4;
    /** Most lines a wrapping label runs to. */
    public static final int MAX_LINES = 3;
    /** Clear space kept at each end of a line, px. */
    public static final float TEXT_MARGIN = 0.1f;
    public static final float DEFAULT_WIDTH = 6f;
    /** Footprint depth per scale step, px. */
    public static final float DEPTH_PER_SCALE = 1f;

    public enum Alignment {
        LEFT, CENTER, RIGHT;

        public static Alignment byName(String name) {
            for (Alignment value : values())
                if (value.name().equalsIgnoreCase(name))
                    return value;
            return CENTER;
        }
    }

    private String text = "Label";
    private int color = DEFAULT_COLOR;
    private int scale = 1;
    private Alignment alignment = Alignment.CENTER;
    private float labelWidth = DEFAULT_WIDTH;
    private boolean wrap;
    private int lines = 1;

    public LabelElement(PanelElementType<? extends LabelElement> type) {
        super(type);
    }

    @Override
    public boolean isTransmitter() {
        return false;
    }

    @Override
    public boolean isReceiver() {
        return false;
    }

    @Override
    public boolean takesClicks() {
        return false;
    }

    @Override
    public float width() {
        return labelWidth;
    }

    @Override
    public float depth() {
        return lines * lineHeight();
    }

    /** Height of one line of text, px. */
    public float lineHeight() {
        return scale * DEPTH_PER_SCALE;
    }

    /** Width a line of text may fill, px. */
    public float textWidth() {
        return labelWidth - 2 * TEXT_MARGIN;
    }

    public boolean wraps() {
        return wrap;
    }

    /** A label that stops wrapping goes back to one line. */
    public void setWrap(boolean wrap) {
        this.wrap = wrap;
        if (!wrap)
            setLines(1);
    }

    /** Most lines this label may show: one, or {@link #MAX_LINES} when it wraps. */
    public int maxLines() {
        return wrap ? MAX_LINES : 1;
    }

    /** Lines the footprint is deep. */
    public int lines() {
        return lines;
    }

    public void setLines(int lines) {
        this.lines = Mth.clamp(lines, 1, maxLines());
        reclamp();
    }

    public void setLabelWidth(float width) {
        this.labelWidth = Mth.clamp(snap(width), 1f, AREA_MAX - AREA_MIN);
        reclamp();
    }

    public String text() {
        return text;
    }

    public void setText(String text) {
        String safe = text == null ? "" : text;
        this.text = safe.length() > MAX_TEXT_LENGTH ? safe.substring(0, MAX_TEXT_LENGTH) : safe;
    }

    public int color() {
        return color;
    }

    public void setColor(int color) {
        this.color = color & 0xFFFFFF;
    }

    public int scale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = Mth.clamp(scale, 1, MAX_SCALE);
        reclamp();
    }

    public Alignment alignment() {
        return alignment;
    }

    public void setAlignment(Alignment alignment) {
        this.alignment = alignment == null ? Alignment.CENTER : alignment;
    }

    @Override
    protected void saveOptions(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putString(TAG_TEXT, text);
        tag.putInt(TAG_COLOR, color);
        tag.putInt(TAG_SCALE, scale);
        tag.putString(TAG_ALIGNMENT, alignment.name());
        tag.putFloat(TAG_WIDTH, labelWidth);
        if (wrap) {
            tag.putBoolean(TAG_WRAP, true);
            tag.putInt(TAG_LINES, lines);
        }
    }

    @Override
    protected void loadOptions(CompoundTag tag, HolderLookup.Provider registries) {
        setText(tag.getString(TAG_TEXT));
        setColor(tag.contains(TAG_COLOR) ? tag.getInt(TAG_COLOR) : DEFAULT_COLOR);
        setScale(tag.contains(TAG_SCALE) ? tag.getInt(TAG_SCALE) : 1);
        setAlignment(Alignment.byName(tag.getString(TAG_ALIGNMENT)));
        setLabelWidth(tag.contains(TAG_WIDTH) ? tag.getFloat(TAG_WIDTH) : DEFAULT_WIDTH);
        setWrap(tag.getBoolean(TAG_WRAP));
        setLines(tag.contains(TAG_LINES) ? tag.getInt(TAG_LINES) : 1);
    }
}
