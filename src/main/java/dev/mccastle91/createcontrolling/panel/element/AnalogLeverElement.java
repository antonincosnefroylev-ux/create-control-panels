package dev.mccastle91.createcontrolling.panel.element;

import net.minecraft.world.item.DyeColor;

/**
 * Analog lever, after Create's own: a handle that swings from pulled back
 * (0, towards the near edge) to pushed forward (15, towards the far edge),
 * with a small lamp on its base that glows brighter the further it is
 * thrown. It is worked like a knob or slider: a click steps it one forward,
 * a sneak-click one back, and holding and dragging up or down sets it.
 */
public class AnalogLeverElement extends AnalogElement {

    public static final DyeColor DEFAULT_COLOR = DyeColor.BROWN;
    /** Half the handle's swing, degrees either side of upright: as far as it leans without leaving its base. */
    public static final float HALF_SWING = 40f;
    /** The pivot's height off the face, px. */
    public static final float PIVOT_HEIGHT = 0.6f;
    /**
     * The handle, px: one thickness from pivot to top, a good deal longer
     * than a toggle switch's lever, its top stretch in the part's colour.
     */
    public static final float HANDLE_LENGTH = 2.6f;
    public static final float HANDLE_HALF_WIDTH = 0.45f;
    public static final float HANDLE_GRIP = 0.9f;

    public AnalogLeverElement(PanelElementType<? extends AnalogLeverElement> type) {
        super(type, DEFAULT_COLOR);
    }

    @Override
    protected DyeColor defaultColor() {
        return DEFAULT_COLOR;
    }

    /** The handle swings along the face's depth: dragging up pushes it forward. */
    @Override
    public boolean dragsVertically() {
        return true;
    }

    /** Handle angle for a value, degrees from upright, positive towards the far edge. */
    public static float angleFor(int value) {
        return -HALF_SWING + value / (float) MAX_VALUE * 2 * HALF_SWING;
    }
}
