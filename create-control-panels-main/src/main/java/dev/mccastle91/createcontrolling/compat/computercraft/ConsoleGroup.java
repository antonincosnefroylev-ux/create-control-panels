package dev.mccastle91.createcontrolling.compat.computercraft;

import dev.mccastle91.createcontrolling.panel.FrequencyPair;
import dev.mccastle91.createcontrolling.panel.PanelBlock;
import dev.mccastle91.createcontrolling.panel.PanelBlockEntity;
import dev.mccastle91.createcontrolling.panel.element.PanelElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * What a computer sees through one console: that console and every console
 * joined to it, each told apart by its name (set in the designer) or its
 * position. Free of CC: Tweaked's classes, so tests can run it without the
 * mod; bad requests throw {@link IllegalArgumentException} with the message
 * the computer should see.
 */
public final class ConsoleGroup {

    /** The most consoles one peripheral reaches, however large the joined group. */
    public static final int MAX_CONSOLES = 64;

    private ConsoleGroup() {
    }

    /** The console a computer touches, then the consoles joined to it, nearest first. */
    public static List<PanelBlockEntity> of(PanelBlockEntity attached) {
        if (attached.getLevel() == null || attached.isRemoved())
            return List.of();
        return PanelBlock.joinedConsoles(attached.getLevel(), attached.getBlockPos(), MAX_CONSOLES);
    }

    /** Whether {@code console} is in the group reached through {@code attached}. */
    public static boolean reaches(PanelBlockEntity attached, PanelBlockEntity console) {
        return attached.getLevel() == console.getLevel() && of(attached).contains(console);
    }

    /**
     * The console a selector names: nil for the attached console, a string
     * for the console with that name, or a table with {@code x}/{@code y}/{@code z}
     * (such as an entry of {@code getConsoles()} or an event's console table)
     * for the console at that position; a table with only {@code name} goes by the name.
     */
    public static PanelBlockEntity select(PanelBlockEntity attached, Object selector) {
        if (selector == null)
            return attached;
        if (selector instanceof String name)
            return byName(attached, name);
        if (selector instanceof Map<?, ?> table) {
            Object x = table.get("x"), y = table.get("y"), z = table.get("z");
            if (x instanceof Number nx && y instanceof Number ny && z instanceof Number nz)
                return byPosition(attached, new BlockPos(nx.intValue(), ny.intValue(), nz.intValue()));
            if (table.get("name") instanceof String name)
                return byName(attached, name);
            throw new IllegalArgumentException("Console table needs x, y and z, or a name");
        }
        throw new IllegalArgumentException("Expected a console name, a console table or nil");
    }

    private static PanelBlockEntity byName(PanelBlockEntity attached, String name) {
        PanelBlockEntity found = null;
        for (PanelBlockEntity console : of(attached)) {
            if (!console.name().equals(name))
                continue;
            if (found != null)
                throw new IllegalArgumentException("More than one console is named '" + name
                        + "'; give them different names or pick one by position");
            found = console;
        }
        if (found == null)
            throw new IllegalArgumentException("No joined console is named '" + name + "'");
        return found;
    }

    private static PanelBlockEntity byPosition(PanelBlockEntity attached, BlockPos pos) {
        for (PanelBlockEntity console : of(attached))
            if (console.getBlockPos().equals(pos))
                return console;
        throw new IllegalArgumentException("No joined console at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
    }

    /** {@code name} (absent when unnamed) and {@code x}/{@code y}/{@code z}: what tells a console apart. */
    public static Map<String, Object> describe(PanelBlockEntity console) {
        Map<String, Object> table = new HashMap<>();
        if (!console.name().isEmpty())
            table.put("name", console.name());
        BlockPos pos = console.getBlockPos();
        table.put("x", pos.getX());
        table.put("y", pos.getY());
        table.put("z", pos.getZ());
        return table;
    }

    /** A console's components a computer can work: every one with a Redstone Link role, in mounting order. */
    public static List<PanelElement> components(PanelBlockEntity console) {
        List<PanelElement> components = new ArrayList<>();
        for (PanelElement element : console.getLayout().elements())
            if (element.isTransmitter() || element.isReceiver())
                components.add(element);
        return components;
    }

    /** Component {@code index} (from 1) of a console. */
    public static PanelElement component(PanelBlockEntity console, int index) {
        List<PanelElement> components = components(console);
        if (index < 1 || index > components.size())
            throw new IllegalArgumentException("No component " + index + " (console has " + components.size() + ")");
        return components.get(index - 1);
    }

    /**
     * A frequency from two item ids, either of which may be nil for an empty
     * slot; both nil unlinks the component.
     */
    public static FrequencyPair frequency(Object first, Object second) {
        return FrequencyPair.of(frequencyItem(first, 1), frequencyItem(second, 2));
    }

    private static ItemStack frequencyItem(Object id, int slot) {
        if (id == null)
            return ItemStack.EMPTY;
        if (!(id instanceof String text))
            throw new IllegalArgumentException("Frequency slot " + slot + ": expected an item id or nil");
        ResourceLocation key = ResourceLocation.tryParse(text);
        Item item = key == null ? null : BuiltInRegistries.ITEM.getOptional(key).orElse(null);
        if (item == null || item == Items.AIR)
            throw new IllegalArgumentException("Frequency slot " + slot + ": no item '" + text + "'");
        return new ItemStack(item);
    }

    /** {@code first}/{@code second} item ids of a frequency, a blank slot absent. */
    public static Map<String, Object> frequencyTable(FrequencyPair frequency) {
        Map<String, Object> table = new HashMap<>();
        putItem(table, "first", frequency.first());
        putItem(table, "second", frequency.second());
        return table;
    }

    private static void putItem(Map<String, Object> table, String key, ItemStack stack) {
        if (!stack.isEmpty())
            table.put(key, BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
    }
}
