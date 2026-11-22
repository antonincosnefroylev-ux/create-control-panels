package dev.mccastle91.createcontrolling.panel.element;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

/**
 * Displays its frequency's current strength, or the signal on a cable wired
 * to it (another mod's wiring, see compat/drivebysable) if that is stronger.
 * Both are runtime state: never saved, resent by their networks on load.
 */
public abstract class ReceiverElement extends PanelElement {

    private int receivedStrength;
    private int wiredStrength;

    protected ReceiverElement(PanelElementType<?> type) {
        super(type);
    }

    @Override
    public boolean isTransmitter() {
        return false;
    }

    @Override
    public boolean isReceiver() {
        return true;
    }

    /** What the part shows: the stronger of its frequency's signal and a wired one. */
    public int receivedStrength() {
        return Math.max(receivedStrength, wiredStrength);
    }

    public void setReceivedStrength(int strength) {
        this.receivedStrength = Mth.clamp(strength, 0, 15);
    }

    @Override
    public boolean receiveStrength(int strength) {
        int previous = receivedStrength();
        setReceivedStrength(strength);
        return previous != receivedStrength();
    }

    @Override
    public boolean receiveWired(int strength) {
        int previous = receivedStrength();
        wiredStrength = Mth.clamp(strength, 0, 15);
        return previous != receivedStrength();
    }

    @Override
    public int displayStrength() {
        return receivedStrength();
    }

    @Override
    public void applyDisplayStrength(int strength) {
        setReceivedStrength(strength);
    }

    @Override
    protected void saveOptions(CompoundTag tag, HolderLookup.Provider registries) {
    }

    @Override
    protected void loadOptions(CompoundTag tag, HolderLookup.Provider registries) {
    }
}
