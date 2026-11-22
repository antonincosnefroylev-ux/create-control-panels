package dev.mccastle91.createcontrolling.panel.element;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.DeadBushBlock;

public class KeySwitchElement extends PanelElement{
    public static final int MAX_KEYS = 3;
    public static final int MIN_KEYS = 1;

    public static final String TAG_ON = "On";
    public static final String TAG_COLOR = "Color";
    public static final DyeColor DEFAULT_COLOR = DyeColor.GRAY;
    private boolean on;
    private DyeColor color = DEFAULT_COLOR;

    public KeySwitchElement(PanelElementType<? extends KeySwitchElement> type){super(type);}

    @Override
    public boolean isTransmitter() {
        return true;
    }

    @Override
    public boolean isReceiver() {
        return false;
    }

    @Override
    public boolean click(float localX, float localY, boolean sneaking) {
        on = !on;
        sound(PartSound.key(on));
        return true;
    }

    @Override
    public boolean operate(int strength) {
        boolean changed = on != strength > 0;
        on = strength > 0;
        if (changed)
            sound(PartSound.key(on));
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

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public DyeColor color() {
        return color;
    }

    public void setColor(DyeColor color) {
        this.color = color == null ? DEFAULT_COLOR : color;
    }

    @Override
    protected void saveOptions(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean(TAG_ON, on);
        tag.putString(TAG_COLOR, color.getName());
    }

    @Override
    protected void loadOptions(CompoundTag tag, HolderLookup.Provider registries) {
        on = tag.getBoolean(TAG_ON);
        setColor(DyeColor.byName(tag.getString(TAG_COLOR), DEFAULT_COLOR));
    }
}
