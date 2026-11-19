package dev.mccastle91.createcontrolling.panel.element;

import net.minecraft.world.item.DyeColor;

/**
 * Linear fader along its long axis. Horizontal sliders run 0 (left) to 15
 * (right); vertical ones 0 (near edge) to 15 (far edge). Clicking steps it
 * one forward, sneak-clicking one back.
 */
public class SliderElement extends AnalogElement {

    public static final DyeColor DEFAULT_COLOR = DyeColor.WHITE;
    /** Track inset at each end, px: the knob's half-length. */
    public static final float END_MARGIN = 0.75f;
    /** Thickness of the line marking where a limited slider stops, px. */
    public static final float STOP_LINE = 0.2f;

    private final boolean vertical;

    public SliderElement(PanelElementType<? extends SliderElement> type, boolean vertical) {
        super(type, DEFAULT_COLOR);
        this.vertical = vertical;
    }

    @Override
    protected DyeColor defaultColor() {
        return DEFAULT_COLOR;
    }

    public boolean isVertical() {
        return vertical;
    }

    @Override
    public boolean dragsVertically() {
        return vertical;
    }

    /** Where the handle's centre sits for a value, px along the slider from its left (or far) end. */
    public float handleCentre(int value) {
        float length = vertical ? depth() : width();
        float along = END_MARGIN + fractionFor(value) * (length - 2 * END_MARGIN);
        return vertical ? length - along : along; // a vertical slider's 0 is at its near end
    }

    /**
     * Where the stop lines of a limited slider lie, px along it (none for a
     * limit at the scale's own end): just past the handle's edge when it
     * rests at that limit, so the handle comes up against the line rather
     * than covering it.
     */
    public float[] stopLines() {
        float reach = END_MARGIN + STOP_LINE / 2;
        // towards the low end of the scale: left, or the near (+) end of a vertical slider
        float low = handleCentre(min()) + (vertical ? reach : -reach);
        float high = handleCentre(max()) + (vertical ? -reach : reach);
        boolean hasLow = min() > 0, hasHigh = max() < MAX_VALUE;
        if (hasLow && hasHigh)
            return new float[] { low, high };
        return hasLow ? new float[] { low } : hasHigh ? new float[] { high } : new float[0];
    }

    /** Knob position along the track as a 0-1 fraction for a value. */
    public static float fractionFor(int value) {
        return value / (float) MAX_VALUE;
    }
}
