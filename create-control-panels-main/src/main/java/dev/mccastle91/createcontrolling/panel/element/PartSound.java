package dev.mccastle91.createcontrolling.panel.element;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * The sound a part makes when it is worked. A part notes one as its state
 * changes (see {@link PanelElement#takeSound}); the console plays it from its
 * own position, so everyone nearby hears a button go down — whoever, or
 * whatever (a computer), pressed it.
 *
 * All of them are vanilla's own button, lever and trapdoor sounds, at the
 * volume and pitches vanilla's buttons and levers use, so a console sounds
 * like the redstone parts players already know.
 */
public record PartSound(SoundEvent event, float volume, float pitch) {

    private static final float VOLUME = 0.3f;

    /** A button going down, or coming back up; the large button sits a little lower. */
    public static PartSound button(boolean down, boolean large) {
        float pitch = (down ? 0.6f : 0.5f) - (large ? 0.1f : 0f);
        return new PartSound(down ? SoundEvents.STONE_BUTTON_CLICK_ON : SoundEvents.STONE_BUTTON_CLICK_OFF,
                large ? VOLUME + 0.1f : VOLUME, pitch);
    }

    /** A lever thrown on or off, as vanilla's lever. */
    public static PartSound lever(boolean on) {
        return new PartSound(SoundEvents.LEVER_CLICK, VOLUME, on ? 0.6f : 0.5f);
    }

    /** A guarded switch's lid: a small, light hatch. */
    public static PartSound lid(boolean open) {
        return new PartSound(open ? SoundEvents.IRON_TRAPDOOR_OPEN : SoundEvents.IRON_TRAPDOOR_CLOSE, 0.25f, 1.8f);
    }

    /** One step of a knob or slider: a light click, rising a little with the value. */
    public static PartSound detent(int value) {
        return new PartSound(SoundEvents.LEVER_CLICK, VOLUME, detentPitch(value));
    }

    public static PartSound key(boolean open) {
        return new PartSound(open ? SoundEvents.VAULT_INSERT_ITEM : SoundEvents.VAULT_INSERT_ITEM_FAIL, VOLUME, 1.8f);
    }

    /** Shared with the client, which clicks for itself while dragging (see AnalogDragScreen). */
    public static float detentPitch(int value) {
        return 1.1f + value * 0.03f;
    }

    public static float detentVolume() {
        return VOLUME;
    }
}
