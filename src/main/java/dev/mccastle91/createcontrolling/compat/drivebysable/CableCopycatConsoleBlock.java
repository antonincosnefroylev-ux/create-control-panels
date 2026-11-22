//? if neoforge {
package dev.mccastle91.createcontrolling.compat.drivebysable;

import dev.mccastle91.createcontrolling.compat.create.CopycatConsoleBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** The copycat console block registered when Drive-By-Sable is installed (see {@link CableConsoleBlock}). */
public class CableCopycatConsoleBlock extends CopycatConsoleBlock implements CableConsole {

    public CableCopycatConsoleBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!state.is(newState.getBlock()))
            ConsoleCables.consoleRemoved(level, pos);
    }
}
//?}
