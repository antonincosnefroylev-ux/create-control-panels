package dev.mccastle91.createcontrolling.panel.element;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;

/**
 * Guarded switch: a toggle switch under a lid. The guard and the lever are
 * worked separately:
 * <ul>
 * <li>guard closed — a click lifts it; the lever can't be reached;</li>
 * <li>guard open — a click on the lever flips it, as often as wanted; a click
 * on the raised lid closes the guard;</li>
 * <li>a sneak-click works the guard from anywhere on the part.</li>
 * </ul>
 * Closing the guard switches the lever off, unless this switch is set to keep
 * its state (locking it on under the lid). It sends 15 while the lever is on.
 * The state persists.
 */
public class CoveredSwitchElement extends PanelElement implements ColorableElement {

    public static final String TAG_GUARD_OPEN = "GuardOpen";
    public static final String TAG_ON = "On";
    public static final String TAG_KEEPS_STATE = "KeepsState";
    public static final String TAG_COLOR = "Color";
    /** Saves from before the guard and lever were separate: CLOSED, OPEN or ARMED. */
    private static final String TAG_LEGACY_STATE = "State";
    public static final DyeColor DEFAULT_COLOR = DyeColor.RED;

    /** Click zones (see {@link #pickBoxes}): the switch itself, or the lid standing open. */
    public static final int ZONE_LEVER = 0;
    public static final int ZONE_LID = 1;

    /** Thickness of the lid's plate and walls, px. */
    public static final float LID_THICKNESS = 0.2f;
    /** How thick the open lid's plate is to the click ray, px: a little more than it looks, to be easy to hit. */
    private static final float LID_GRIP = 0.6f;
    /**
     * How far the open lid's top edge counts towards the near side, px. The
     * real edge overhangs most of the switch; counting all of it would make
     * the lever unreachable from steep angles.
     */
    private static final float LID_CAP = 1f;

    private boolean guardOpen;
    private boolean on;
    private boolean keepsState;
    private DyeColor color = DEFAULT_COLOR;

    public CoveredSwitchElement(PanelElementType<? extends CoveredSwitchElement> type) {
        super(type);
    }

    @Override
    public boolean isTransmitter() {
        return true;
    }

    @Override
    public boolean isReceiver() {
        return false;
    }

    // --- Shape ----------------------------------------------------------------

    /** Top of the closed lid off the face, px; the open lid stands on from here. */
    public float lidTop() {
        return type().height();
    }

    /** The open lid stands as tall again as the part is deep. */
    @Override
    public float reach() {
        return lidTop() + depth();
    }

    /**
     * Closed, the whole lid is one target. Open, the switch underneath is one
     * and the raised lid another: its plate, standing along the far edge, and
     * the far part of the wall that is now its top.
     */
    @Override
    public float[][] pickBoxes() {
        if (!guardOpen)
            return super.pickBoxes();
        float w = width(), d = depth(), top = lidTop();
        return new float[][] {
                { 0, 0, 0, w, d, top },
                { 0, 0, top, w, LID_GRIP, top + d },
                { 0, 0, top + d - LID_GRIP, w, LID_CAP, top + d } };
    }

    // --- Behaviour ------------------------------------------------------------

    @Override
    public boolean click(int zone, float localX, float localY, boolean sneaking) {
        if (sneaking)
            return setGuardOpen(!guardOpen);
        if (!guardOpen)
            return setGuardOpen(true);
        if (zone != ZONE_LEVER)
            return setGuardOpen(false);
        on = !on;
        return true;
    }

    /** A plain click with no zone lands on the switch itself. */
    @Override
    public boolean click(float localX, float localY, boolean sneaking) {
        return click(ZONE_LEVER, localX, localY, sneaking);
    }

    /** Sneak-clicking works the guard rather than opening the designer. */
    @Override
    public boolean takesSneakClicks() {
        return true;
    }

    /** Switching on lifts the guard on the way; switching off closes it. */
    @Override
    public boolean operate(int strength) {
        boolean wantOn = strength > 0;
        boolean changed = on != wantOn || guardOpen != wantOn;
        on = wantOn;
        guardOpen = wantOn;
        return changed;
    }

    @Override
    public boolean hasPersistentState() {
        return true;
    }

    @Override
    public int transmittedStrength() {
        return on ? 15 : 0;
    }

    public boolean isGuardOpen() {
        return guardOpen;
    }

    /** Lifts or closes the guard; closing switches the lever off unless it keeps its state. Returns true if anything changed. */
    public boolean setGuardOpen(boolean open) {
        if (open == guardOpen)
            return false;
        guardOpen = open;
        if (!open && !keepsState)
            on = false;
        return true;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    /** Whether the lever stays as it is when the guard closes (instead of switching off). */
    public boolean keepsState() {
        return keepsState;
    }

    /** A switch that stops keeping its state can't stay on under a closed guard. */
    public void setKeepsState(boolean keepsState) {
        this.keepsState = keepsState;
        if (!keepsState && !guardOpen)
            on = false;
    }

    /** The cover's colour. */
    @Override
    public DyeColor color() {
        return color;
    }

    @Override
    public void setColor(DyeColor color) {
        this.color = color == null ? DEFAULT_COLOR : color;
    }

    @Override
    protected void saveOptions(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean(TAG_GUARD_OPEN, guardOpen);
        tag.putBoolean(TAG_ON, on);
        tag.putBoolean(TAG_KEEPS_STATE, keepsState);
        tag.putString(TAG_COLOR, color.getName());
    }

    @Override
    protected void loadOptions(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains(TAG_LEGACY_STATE) && !tag.contains(TAG_GUARD_OPEN)) {
            String legacy = tag.getString(TAG_LEGACY_STATE);
            on = legacy.equalsIgnoreCase("ARMED");
            guardOpen = on || legacy.equalsIgnoreCase("OPEN");
            keepsState = false;
        } else {
            guardOpen = tag.getBoolean(TAG_GUARD_OPEN);
            keepsState = tag.getBoolean(TAG_KEEPS_STATE);
            // only a switch that keeps its state can be on under a closed guard
            on = tag.getBoolean(TAG_ON) && (guardOpen || keepsState);
        }
        setColor(DyeColor.byName(tag.getString(TAG_COLOR), DEFAULT_COLOR));
    }
}
