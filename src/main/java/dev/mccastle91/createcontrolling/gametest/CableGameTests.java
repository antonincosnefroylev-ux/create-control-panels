//? if neoforge {
package dev.mccastle91.createcontrolling.gametest;

import dev.mccastle91.createcontrolling.CreateControlling;
import dev.mccastle91.createcontrolling.compat.drivebysable.CableChannels;
import dev.mccastle91.createcontrolling.compat.drivebysable.DriveBySableCompat;
import dev.mccastle91.createcontrolling.panel.CostPayer;
import dev.mccastle91.createcontrolling.panel.PanelBlockEntity;
import dev.mccastle91.createcontrolling.panel.PanelEditOps;
import dev.mccastle91.createcontrolling.panel.PanelLayout;
import dev.mccastle91.createcontrolling.panel.element.KnobElement;
import dev.mccastle91.createcontrolling.panel.element.LampElement;
import dev.mccastle91.createcontrolling.panel.element.PanelElement;
import dev.mccastle91.createcontrolling.panel.element.PanelElements;
import dev.mccastle91.createcontrolling.registry.CCContent;
import edn.lakeopossmc.drivebysable.cable.CableNetworkManager;
import edn.lakeopossmc.drivebysable.cable.ModuleSinkTarget;
import edn.lakeopossmc.drivebysable.cable.MultiChannelCableSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Console parts on Drive-By-Sable's real cable network, in a running server.
 * These only mean something with Drive-By-Sable and Sable on the dev runtime:
 * gradlew :1.21.1-neoforge:runGametest -Pcables. Without it they pass at once.
 */
@GameTestHolder(CreateControlling.MOD_ID)
@PrefixGameTestTemplate(false)
public class CableGameTests {

    private static final String EMPTY = "empty";
    private static final BlockPos CONTROLS_POS = new BlockPos(1, 1, 1);
    private static final BlockPos LAMPS_POS = new BlockPos(4, 1, 1);
    private static final BlockPos FLOOR_SINK_POS = new BlockPos(3, 0, 4);

    private static boolean absent(GameTestHelper helper) {
        if (ModList.get().isLoaded(DriveBySableCompat.MOD_ID))
            return false;
        helper.succeed();
        return true;
    }

    /** With Drive-By-Sable installed the console blocks are the ones its cable tools recognise. */
    @GameTest(template = EMPTY)
    public void consolesRegisterAsCableEndpoints(GameTestHelper helper) {
        if (absent(helper))
            return;
        for (var block : new net.minecraft.world.level.block.Block[] {
                CCContent.CONTROL_PANEL.get(), CCContent.COPYCAT_CONTROL_PANEL.get() }) {
            if (!(block instanceof MultiChannelCableSource) || !(block instanceof ModuleSinkTarget))
                throw new GameTestAssertException(block + " is not a cable endpoint");
        }
        helper.succeed();
    }

    /**
     * A knob without a frequency, cabled to a lamp without one on another
     * console and to a plain block face: both follow the knob. The lamp's
     * console reloading its layout keeps the cable and the signal; deleting
     * the knob drops its cables.
     */
    @GameTest(template = EMPTY)
    public void knobCableDrivesLampAndBlockFace(GameTestHelper helper) {
        if (absent(helper))
            return;
        KnobElement knob = PanelElements.KNOB.create();
        LampElement lamp = PanelElements.LAMP.create();
        PanelBlockEntity controls = placeConsoleWith(helper, CONTROLS_POS, knob);
        PanelBlockEntity lamps = placeConsoleWith(helper, LAMPS_POS, lamp);
        ServerLevel level = helper.getLevel();
        BlockPos source = helper.absolutePos(CONTROLS_POS);
        BlockPos sink = helper.absolutePos(LAMPS_POS);
        BlockPos floorSink = helper.absolutePos(FLOOR_SINK_POS);
        String knobChannel = CableChannels.of(controls.getLayout(), knob.id());
        String lampChannel = CableChannels.of(lamps.getLayout(), lamp.id());

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    var toLamp = CableNetworkManager.createConnection(level, source, sink, Direction.UP,
                            knobChannel, lampChannel);
                    if (!toLamp.isSuccess())
                        throw new GameTestAssertException("Knob to lamp cable refused: " + toLamp);
                    var toFace = CableNetworkManager.createConnection(level, source, floorSink, Direction.UP, knobChannel);
                    if (!toFace.isSuccess())
                        throw new GameTestAssertException("Knob to block face cable refused: " + toFace);
                    if (!controls.setSignal(knob.id(), 11))
                        throw new GameTestAssertException("Knob could not be set");
                })
                .thenWaitUntil(() -> {
                    expectLamp(lamps, lamp, 11);
                    int onFace = CableNetworkManager.get(level).getSignalAt(floorSink, Direction.UP);
                    if (onFace != 11)
                        throw new GameTestAssertException("Expected 11 on the cabled block face, got " + onFace);
                })
                // new part objects with the same ids, as after a reload or a blueprint of itself
                .thenExecute(() -> lamps.setLayout(PanelLayout.load(
                        lamps.getLayout().save(level.registryAccess()), level.registryAccess())))
                .thenExecuteAfter(2, () -> expectLamp(lamps, lamp, 11))
                .thenExecute(() -> controls.setSignal(knob.id(), 4))
                .thenWaitUntil(() -> expectLamp(lamps, lamp, 4))
                .thenExecute(() -> {
                    CompoundTag op = new CompoundTag();
                    op.putString(PanelEditOps.KEY_OP, PanelEditOps.OP_DELETE);
                    op.putUUID("Id", knob.id());
                    if (!PanelEditOps.apply(controls, op, level.registryAccess(), CostPayer.FREE))
                        throw new GameTestAssertException("Knob not deleted");
                })
                .thenWaitUntil(() -> {
                    expectLamp(lamps, lamp, 0);
                    if (CableNetworkManager.get(level).getConnections(source).containsKey(knobChannel))
                        throw new GameTestAssertException("The deleted knob's cables are still there");
                })
                .thenSucceed();
    }

    private static void expectLamp(PanelBlockEntity console, LampElement lamp, int strength) {
        PanelElement now = console.getLayout().elementById(lamp.id()).orElseThrow();
        if (now.displayStrength() != strength)
            throw new GameTestAssertException("Expected the cabled lamp at " + strength + ", shows " + now.displayStrength());
    }

    private static PanelBlockEntity placeConsoleWith(GameTestHelper helper, BlockPos pos, PanelElement element) {
        // a floor of its own: face-attached blocks pop off unsupported
        for (int x = 0; x < 7; x++)
            for (int z = 0; z < 7; z++)
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE.defaultBlockState());
        helper.setBlock(pos, CCContent.CONTROL_PANEL.getDefaultState()
                .setValue(FaceAttachedHorizontalDirectionalBlock.FACE, AttachFace.FLOOR));
        if (!(helper.getBlockEntity(pos) instanceof PanelBlockEntity console))
            throw new GameTestAssertException("Console block entity missing at " + pos);
        console.getLayout().addElement(element);
        console.markLayoutChanged();
        return console;
    }
}
//?}
