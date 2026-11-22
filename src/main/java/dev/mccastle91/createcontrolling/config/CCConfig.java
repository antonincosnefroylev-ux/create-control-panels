package dev.mccastle91.createcontrolling.config;

/**
 * Runtime config values, populated by the loader module's config system
 * (NeoForge ModConfigSpec). Defaults apply until a config loads.
 */
public final class CCConfig {

    public static int maxElementsPerPanel = 64;
    public static int maxPulseTicks = 200;

    /** Client: holding right-click on a knob or slider opens a dial window instead of dragging in place. */
    public static boolean analogPopup = false;

    private CCConfig() {
    }
}
