package dev.mccastle91.createcontrolling.panel;

import org.jetbrains.annotations.Nullable;

/**
 * The material a console is dressed in, applied by right-clicking it with the
 * matching Create casing. Purely cosmetic: every casing has the full feature
 * set. The face frame and the body sides take their look from Create's own
 * casing texture, and the metal trim of the parts mounted on it (bezels,
 * housings, pivots, hinges, sockets) from the block of the casing's metal.
 */
public enum ConsoleCasing {

    // id, the item that applies it, body texture, trim texture, flat frame colour
    ANDESITE("andesite", "create:andesite_casing", "create:block/andesite_casing",
            "create:block/andesite_block", 0xFFA6A8A0),
    BRASS("brass", "create:brass_casing", "create:block/brass_casing",
            "create:block/brass_block", 0xFFE3B04C),
    COPPER("copper", "create:copper_casing", "create:block/copper_casing",
            "minecraft:block/copper_block", 0xFFD9845A),
    // train casing is brass-toned: it keeps brass trim
    RAILWAY("railway", "create:railway_casing", "create:block/railway_casing",
            "create:block/brass_block", 0xFFD6A647),
    INDUSTRIAL_IRON("industrial_iron", "create:industrial_iron_block", "create:block/industrial_iron_block",
            "create:block/industrial_iron_block", 0xFF6F7072),
    WEATHERED_IRON("weathered_iron", "create:weathered_iron_block", "create:block/weathered_iron_block",
            "create:block/weathered_iron_block", 0xFF7A6957);

    /** Freshly crafted consoles. */
    public static final ConsoleCasing DEFAULT = ANDESITE;

    private final String id;
    private final String itemId;
    private final String texture;
    private final String trimTexture;
    private final int frameColor;

    ConsoleCasing(String id, String itemId, String texture, String trimTexture, int frameColor) {
        this.id = id;
        this.itemId = itemId;
        this.texture = texture;
        this.trimTexture = trimTexture;
        this.frameColor = frameColor;
    }

    public String id() {
        return id;
    }

    /** Registry id of the Create item that applies this casing. */
    public String itemId() {
        return itemId;
    }

    /** Block-atlas texture used for the body and face frame. */
    public String texture() {
        return texture;
    }

    /**
     * Block-atlas texture for the metal trim of the parts on this console.
     * To change how a casing's trim looks, this is the one place.
     */
    public String trimTexture() {
        return trimTexture;
    }

    /** Opaque ARGB approximating the casing's frame, for flat (GUI) drawing. */
    public int frameColor() {
        return frameColor;
    }

    /** The Create item that applies this casing, if Create provides it. */
    public java.util.Optional<net.minecraft.world.item.Item> item() {
        int colon = itemId.indexOf(':');
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(
                dev.mccastle91.createcontrolling.util.McCompat.id(itemId.substring(0, colon), itemId.substring(colon + 1)));
    }

    public static ConsoleCasing byId(String id) {
        for (ConsoleCasing casing : values())
            if (casing.id.equals(id))
                return casing;
        return DEFAULT;
    }

    @Nullable
    public static ConsoleCasing byItemId(String itemId) {
        for (ConsoleCasing casing : values())
            if (casing.itemId.equals(itemId))
                return casing;
        return null;
    }
}
