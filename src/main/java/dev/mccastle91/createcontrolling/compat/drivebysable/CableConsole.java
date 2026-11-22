//? if neoforge {
package dev.mccastle91.createcontrolling.compat.drivebysable;

import edn.lakeopossmc.drivebysable.cable.ModuleSinkTarget;
import edn.lakeopossmc.drivebysable.cable.MultiChannelCableSource;
import edn.lakeopossmc.drivebysable.cable.SubTargetCableEndpoint;
import edn.lakeopossmc.drivebysable.util.CableOutlineBox;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * What makes a console block a Drive-By-Sable cable endpoint with one
 * channel per part: the three interfaces its cable tools look for on a
 * block, answered by {@link ConsoleCables}. Drive-By-Sable does the same for
 * Dashpanels from its side; a part here is what it calls a sub target, and
 * every part has exactly one channel, named like the part.
 */
public interface CableConsole extends MultiChannelCableSource, ModuleSinkTarget, SubTargetCableEndpoint {

    // --- Sources: controls -----------------------------------------------

    @Override
    default List<String> cable$getChannels(Level level, BlockPos pos) {
        return ConsoleCables.channels(level, pos, null, true);
    }

    @Override
    default List<String> cable$getChannels(Level level, BlockPos pos, String subTarget) {
        return ConsoleCables.channels(level, pos, subTarget, true);
    }

    @Override
    default String cable$nextChannel(Level level, BlockPos pos, String current, boolean forward) {
        return ConsoleCables.step(ConsoleCables.channels(level, pos, null, true), current, forward);
    }

    @Override
    default String cable$nextChannel(Level level, BlockPos pos, String subTarget, String current, boolean forward) {
        return ConsoleCables.step(ConsoleCables.channels(level, pos, subTarget, true), current, forward);
    }

    @Override
    default boolean cable$hasSubTargets() {
        return true;
    }

    // --- Sinks: lamps, meters, nixies ------------------------------------

    @Override
    default List<String> cable$getSinkChannels(Level level, BlockPos pos) {
        return ConsoleCables.channels(level, pos, null, false);
    }

    @Override
    default List<String> cable$getSinkChannels(Level level, BlockPos pos, String subTarget) {
        return ConsoleCables.channels(level, pos, subTarget, false);
    }

    @Override
    default String cable$nextSinkChannel(Level level, BlockPos pos, String subTarget, String current, boolean forward) {
        return ConsoleCables.step(ConsoleCables.channels(level, pos, subTarget, false), current, forward);
    }

    @Override
    default boolean cable$applySinkSignal(Level level, BlockPos pos, String channel, int signal) {
        return ConsoleCables.applySinkSignal(level, pos, channel, signal);
    }

    // --- Parts -----------------------------------------------------------

    @Override
    default String cable$pickSubTarget(Level level, BlockPos pos, Player player) {
        return ConsoleCables.pick(level, pos, player);
    }

    @Override
    default List<String> cable$getSubTargets(Level level, BlockPos pos) {
        return ConsoleCables.channels(level, pos, null, null);
    }

    @Override
    default String cable$subTargetForChannel(Level level, BlockPos pos, String channel) {
        return ConsoleCables.channels(level, pos, null, null).contains(channel) ? channel : null;
    }

    @Override
    default boolean cable$isSourceSubTarget(Level level, BlockPos pos, String subTarget) {
        return ConsoleCables.channels(level, pos, null, true).contains(subTarget);
    }

    @Override
    default boolean cable$isSinkSubTarget(Level level, BlockPos pos, String subTarget) {
        return ConsoleCables.channels(level, pos, null, false).contains(subTarget);
    }

    @Override
    default List<CableOutlineBox> cable$getSubTargetOutline(Level level, BlockPos pos, String subTarget) {
        return ConsoleCables.outline(level, pos, subTarget);
    }
}
//?}
