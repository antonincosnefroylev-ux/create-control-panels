package dev.mccastle91.createcontrolling.panel.element;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;

/**
 * An analog output (knob or slider): a 0-15 value sent continuously on its
 * frequency. Sneak + scroll nudges it by one. The value persists.
 *
 * Each control can be limited to part of that range: a lowest and a highest
 * value it stops at, however it is worked (hand, drag or computer). The
 * handle keeps its place on the full 0-15 scale, so a limited control simply
 * stops short of the ends. The limits can meet but never cross.
 */
public abstract class AnalogElement extends PanelElement implements ColorableElement {

    public static final String TAG_VALUE = "Value";
    public static final String TAG_COLOR = "Color";
    public static final String TAG_MIN = "Min";
    public static final String TAG_MAX = "Max";
    public static final int MAX_VALUE = 15;

    private int value;
    private int min = 0;
    private int max = MAX_VALUE;
    private DyeColor color;

    protected AnalogElement(PanelElementType<?> type, DyeColor defaultColor) {
        super(type);
        this.color = defaultColor;
    }

    protected abstract DyeColor defaultColor();

    @Override
    public boolean isTransmitter() {
        return true;
    }

    @Override
    public boolean isReceiver() {
        return false;
    }

    @Override
    public boolean scroll(int delta) {
        return turnTo(value + delta);
    }

    @Override
    public boolean operate(int strength) {
        return turnTo(strength);
    }

    /**
     * Moves the control as a hand (or a computer) would: like
     * {@link #setValue}, and heard if it moved. Returns true if it moved.
     */
    public boolean turnTo(int requested) {
        boolean changed = setValue(requested);
        if (changed)
            sound(PartSound.detent(value));
        return changed;
    }

    @Override
    public boolean hasPersistentState() {
        return true;
    }

    @Override
    public int transmittedStrength() {
        return value;
    }

    public int value() {
        return value;
    }

    /** Returns true if the value changed. */
    public boolean setValue(int value) {
        int clamped = Mth.clamp(value, min, max);
        boolean changed = clamped != this.value;
        this.value = clamped;
        return changed;
    }

    /** The lowest value this control goes to. */
    public int min() {
        return min;
    }

    /** The highest value this control goes to. */
    public int max() {
        return max;
    }

    /** The limits worth marking on the part: the lowest and highest value, where they aren't the scale's own ends. */
    public int[] stops() {
        boolean low = min > 0, high = max < MAX_VALUE;
        if (low && high)
            return new int[] { min, max };
        return low ? new int[] { min } : high ? new int[] { max } : new int[0];
    }

    /** Whether the control stops short of either end of the 0-15 scale. */
    public boolean isLimited() {
        return min > 0 || max < MAX_VALUE;
    }

    /**
     * Sets the lowest value, anywhere from 0 up to the highest. A request
     * outside that is refused outright, not clamped: nothing changes. The
     * current value moves up to the new limit if it was below it. Returns
     * true if the limit changed.
     */
    public boolean setMin(int requested) {
        if (requested < 0 || requested > max || requested == min)
            return false;
        min = requested;
        value = Math.max(value, min);
        return true;
    }

    /** As {@link #setMin}, for the highest value: anywhere from the lowest up to 15. */
    public boolean setMax(int requested) {
        if (requested > MAX_VALUE || requested < min || requested == max)
            return false;
        max = requested;
        value = Math.min(value, max);
        return true;
    }

    /** Click steps one forward; sneak-click steps one back. */
    @Override
    public boolean click(float localX, float localY, boolean sneaking) {
        return turnTo(value + (sneaking ? -1 : 1));
    }

    /** Sneak-clicking an analog control steps it back rather than opening the designer. */
    @Override
    public boolean takesSneakClicks() {
        return true;
    }

    @Override
    public DyeColor color() {
        return color;
    }

    @Override
    public void setColor(DyeColor color) {
        this.color = color == null ? defaultColor() : color;
    }

    @Override
    protected void saveOptions(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt(TAG_VALUE, value);
        tag.putString(TAG_COLOR, color.getName());
        if (isLimited()) {
            tag.putInt(TAG_MIN, min);
            tag.putInt(TAG_MAX, max);
        }
    }

    @Override
    protected void loadOptions(CompoundTag tag, HolderLookup.Provider registries) {
        // limits first (value is clamped into them); bad data falls back to the full range
        int loadedMin = tag.contains(TAG_MIN) ? tag.getInt(TAG_MIN) : 0;
        int loadedMax = tag.contains(TAG_MAX) ? tag.getInt(TAG_MAX) : MAX_VALUE;
        boolean valid = loadedMin >= 0 && loadedMax <= MAX_VALUE && loadedMin <= loadedMax;
        min = valid ? loadedMin : 0;
        max = valid ? loadedMax : MAX_VALUE;
        setValue(tag.getInt(TAG_VALUE));
        setColor(DyeColor.byName(tag.getString(TAG_COLOR), defaultColor()));
    }
}
