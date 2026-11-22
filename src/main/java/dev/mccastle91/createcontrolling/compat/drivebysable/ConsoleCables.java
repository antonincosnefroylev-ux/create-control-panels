//? if neoforge {
package dev.mccastle91.createcontrolling.compat.drivebysable;

import dev.mccastle91.createcontrolling.panel.ConsolePicker;
import dev.mccastle91.createcontrolling.panel.PanelBlockEntity;
import dev.mccastle91.createcontrolling.panel.PanelFaceGeometry;
import dev.mccastle91.createcontrolling.panel.element.PanelElement;
import edn.lakeopossmc.drivebysable.cable.CableNetworkManager;
import edn.lakeopossmc.drivebysable.util.CableOutlineBox;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Console parts on Drive-By-Sable's cable network. A control is a cable
 * source: whatever it sends on its frequency it also puts on its channel.
 * A lamp, meter or nixie is a cable sink: it shows the stronger of the
 * cable's signal and its frequency's. A part needs no frequency to be wired.
 *
 * The cable network keeps connections by channel name (see
 * {@link CableChannels}), so this keeps the two in step: after a console's
 * layout changes, cables on parts that are gone are dropped, and on load the
 * controls' values are sent again and the cables' signals read back.
 */
public final class ConsoleCables {

    /** The channel each part went by when this console was last brought in step; server only. */
    private final Map<UUID, String> known = new HashMap<>();

    /** Consoles whose layout was (re)connected since the last tick. */
    private static final Set<PanelBlockEntity> pending = new LinkedHashSet<>();

    private ConsoleCables() {
    }

    // --- What Drive-By-Sable asks of a console block (see CableConsole) -----

    private static PanelBlockEntity console(Level level, BlockPos pos) {
        return level != null && level.getBlockEntity(pos) instanceof PanelBlockEntity console ? console : null;
    }

    /**
     * The channels of one part ({@code part} is its channel), or of the whole
     * console if null: of its controls if {@code sources} is true, of its
     * lamps and meters if false, of both if null.
     */
    static List<String> channels(Level level, BlockPos pos, String part, Boolean sources) {
        PanelBlockEntity console = console(level, pos);
        if (console == null)
            return List.of();
        List<String> channels = new ArrayList<>();
        CableChannels.of(console.getLayout()).forEach((channel, element) -> {
            if (part != null && !part.equals(channel))
                return;
            if (sources == null || (sources ? element.isTransmitter() : element.isReceiver()))
                channels.add(channel);
        });
        return channels;
    }

    /** The channel after (or before) {@code current}, wrapping; the first if it isn't among them. */
    static String step(List<String> channels, String current, boolean forward) {
        if (channels.isEmpty())
            return null;
        int index = channels.indexOf(current);
        return index < 0 ? channels.get(0) : channels.get(Math.floorMod(index + (forward ? 1 : -1), channels.size()));
    }

    /** The part under the player's crosshair, by its channel; null on the bare face. */
    static String pick(Level level, BlockPos pos, Player player) {
        PanelBlockEntity console = console(level, pos);
        if (console == null || player == null)
            return null;
        Vec3 origin = Vec3.atLowerCornerOf(pos);
        Vec3 eye = player.getEyePosition().subtract(origin);
        Vec3 direction = player.getLookAngle();
        // Through the point the crosshair rests on, as a click on the console is
        // read (see PanelBlock#interact): that also holds on a moving vehicle,
        // where the hit is given in the console's own space
        HitResult hit = player.pick(player.blockInteractionRange() + 1, 1f, false);
        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK
                && blockHit.getBlockPos().equals(pos))
            direction = hit.getLocation().subtract(origin).subtract(eye);
        return ConsolePicker.pick(console.getBlockState(), console.shape(), console.getLayout(), eye, direction,
                        CableChannels::wirable)
                .map(picked -> CableChannels.of(console.getLayout(), picked.element().id()))
                .orElse(null);
    }

    /** The box around a part, for the cable tools' highlight and the end of the drawn cable. */
    static List<CableOutlineBox> outline(Level level, BlockPos pos, String part) {
        PanelBlockEntity console = console(level, pos);
        PanelElement element = console == null || part == null ? null : CableChannels.of(console.getLayout()).get(part);
        if (element == null)
            return List.of();
        PanelFaceGeometry face = PanelFaceGeometry.of(console.getBlockState(), console.shape());
        Vec3 origin = Vec3.atLowerCornerOf(pos);
        double lift = Math.max(ConsolePicker.MIN_HEIGHT, element.height());
        Vec3[] corners = new Vec3[8];
        for (int i = 0; i < 8; i++)
            corners[i] = origin.add(face.point(
                    element.x() + ((i & 1) == 0 ? 0 : element.width()),
                    element.y() + ((i & 4) == 0 ? 0 : element.depth()),
                    (i & 2) == 0 ? 0 : lift));
        return List.of(new CableOutlineBox(corners).inflated(0.002));
    }

    /** A cable's signal arriving at a lamp or meter; false if the console has no such part any more. */
    static boolean applySinkSignal(Level level, BlockPos pos, String channel, int signal) {
        PanelBlockEntity console = console(level, pos);
        if (console == null || level.isClientSide)
            return false;
        PanelElement element = CableChannels.of(console.getLayout()).get(channel);
        return element != null && console.setWiredSignal(element.id(), signal);
    }

    /** The block is gone (broken, replaced, or carried off): nothing sends from here any more. */
    static void consoleRemoved(Level level, BlockPos pos) {
        if (level instanceof ServerLevel)
            CableNetworkManager.get(level).clearSourceSignals(level, pos);
    }

    // --- Keeping the cable network in step with the console -----------------

    /** A control's signal changed: its cable carries the new one. */
    static void signalChanged(PanelBlockEntity console, PanelElement element, int strength) {
        if (!element.isTransmitter() || !(console.getLevel() instanceof ServerLevel level))
            return;
        String channel = CableChannels.of(console.getLayout(), element.id());
        if (channel != null)
            CableNetworkManager.trySetSignalAt(level, console.getBlockPos(), channel, strength);
    }

    /**
     * A console loaded or its layout changed. Bringing the cables in step
     * waits for the tick: this is also called while a chunk is loading, when
     * cables must not reach into other blocks yet.
     */
    static void layoutReady(PanelBlockEntity console) {
        pending.add(console);
    }

    static void syncPending() {
        if (pending.isEmpty())
            return;
        List<PanelBlockEntity> consoles = new ArrayList<>(pending);
        pending.clear();
        for (PanelBlockEntity console : consoles)
            if (!console.isRemoved() && console.getLevel() instanceof ServerLevel level
                    && level.getBlockEntity(console.getBlockPos()) == console)
                console.integrationData(ConsoleCables.class, ignored -> new ConsoleCables()).sync(console, level);
    }

    static void forgetPending() {
        pending.clear();
    }

    private void sync(PanelBlockEntity console, ServerLevel level) {
        BlockPos pos = console.getBlockPos();
        Map<String, PanelElement> channels = CableChannels.of(console.getLayout());
        Map<UUID, String> current = new HashMap<>();
        channels.forEach((channel, element) -> current.put(element.id(), channel));

        // Only what this console itself had a moment ago is ever dropped: a
        // console that has just appeared (placed, loaded, moved onto a vehicle)
        // may not have its parts yet, and its cables must outlast that
        known.forEach((id, channel) -> {
            if (!current.containsKey(id)) {
                CableNetworkManager.removeAllFromSourceChannel(level, pos, channel);
                CableNetworkManager.removeAllToModuleSink(level, pos, channel);
            }
        });
        // a part whose channel changed (see CableChannels: only on a clash of codes) keeps its cables
        known.forEach((id, channel) -> {
            String now = current.get(id);
            if (now != null && !now.equals(channel)) {
                CableNetworkManager.remapSourceChannel(level, pos, channel, now);
                CableNetworkManager.remapModuleSink(level, pos, channel, now);
            }
        });
        known.clear();
        known.putAll(current);

        Map<String, Integer> wired = CableNetworkManager.get(level).getModuleSinkSignals(pos);
        channels.forEach((channel, element) -> {
            if (element.isTransmitter())
                CableNetworkManager.trySetSignalAt(level, pos, channel, PanelBlockEntity.signalOf(element));
            if (element.isReceiver())
                console.setWiredSignal(element.id(), wired.getOrDefault(channel, 0));
        });
    }
}
//?}
