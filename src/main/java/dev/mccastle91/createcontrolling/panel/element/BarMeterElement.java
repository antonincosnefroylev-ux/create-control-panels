package dev.mccastle91.createcontrolling.panel.element;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;

/**
 * Bar meter: a lit bar filling with its frequency's strength. Like sliders it
 * comes as two parts: the horizontal one fills left to right, the vertical
 * one from the near edge up. Its length is set per meter, from
 * {@link #MIN_LENGTH} px to the whole design area's side.
 */
public class BarMeterElement extends ReceiverElement implements ColorableElement {

    public static final String TAG_COLOR = "Color";
    public static final String TAG_LENGTH = "Length";
    /**
     * In 0.2.5 one bar meter part could be turned upright, saved with this
     * flag. Such a meter now loads as the vertical part (see PanelLayout).
     */
    public static final String TAG_LEGACY_VERTICAL = "Vertical";
    public static final DyeColor DEFAULT_COLOR = DyeColor.LIME;
    public static final float MIN_LENGTH = 3f;
    public static final float MAX_LENGTH = AREA_MAX - AREA_MIN;

    private final boolean vertical;
    private DyeColor color = DEFAULT_COLOR;
    private float length;

    public BarMeterElement(PanelElementType<? extends BarMeterElement> type, boolean vertical) {
        super(type);
        this.vertical = vertical;
        length = defaultLength();
    }

    @Override
    public float width() {
        return vertical ? thickness() : length;
    }

    @Override
    public float depth() {
        return vertical ? length : thickness();
    }

    /** The bar's long side, px. */
    public float length() {
        return length;
    }

    /** The bar's short side, px. */
    public float thickness() {
        return vertical ? type().width() : type().depth();
    }

    private float defaultLength() {
        return vertical ? type().depth() : type().width();
    }

    public boolean isVertical() {
        return vertical;
    }

    /** The length a meter may take: clamped and snapped, before any overlap check. */
    public static float clampLength(float length) {
        return Mth.clamp(snap(length), MIN_LENGTH, MAX_LENGTH);
    }

    /** Sets the length as given (see {@link #clampLength}); the caller checks it fits. */
    public void setLength(float length) {
        this.length = clampLength(length);
        reclamp();
    }

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
        tag.putString(TAG_COLOR, color.getName());
        tag.putFloat(TAG_LENGTH, length);
    }

    @Override
    protected void loadOptions(CompoundTag tag, HolderLookup.Provider registries) {
        setColor(DyeColor.byName(tag.getString(TAG_COLOR), DEFAULT_COLOR));
        setLength(tag.contains(TAG_LENGTH) ? tag.getFloat(TAG_LENGTH) : defaultLength());
    }
}
