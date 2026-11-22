//? if neoforge {
package dev.mccastle91.createcontrolling.compat.drivebysable;

import dev.mccastle91.createcontrolling.compat.create.CopycatConsoleBlock;
import dev.mccastle91.createcontrolling.compat.create.WrenchablePanelBlock;
import dev.mccastle91.createcontrolling.panel.PanelBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Optional Drive-By-Sable integration (NeoForge 1.21.1, the only version that
 * mod exists for): every control, lamp and meter on a console is a cable
 * endpoint of its own, clicked with the Cable like any block. Nothing outside
 * this package refers to Drive-By-Sable's classes, and this class is only
 * ever loaded when the mod is present: check {@link #MOD_ID} first.
 */
public final class DriveBySableCompat {

    public static final String MOD_ID = "drivebysable";

    private DriveBySableCompat() {
    }

    public static void init() {
        PanelBlockEntity.addLayoutListener(ConsoleCables::layoutReady);
        PanelBlockEntity.addSignalListener(ConsoleCables::signalChanged);
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, event -> ConsoleCables.syncPending());
        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class, event -> ConsoleCables.forgetPending());
    }

    // Drive-By-Sable finds its endpoints by interfaces on the block, so with it
    // installed the consoles register as these subclasses; same ids, same block entity

    public static WrenchablePanelBlock consoleBlock(BlockBehaviour.Properties properties) {
        return new CableConsoleBlock(properties);
    }

    public static CopycatConsoleBlock copycatConsoleBlock(BlockBehaviour.Properties properties) {
        return new CableCopycatConsoleBlock(properties);
    }
}
//?}
