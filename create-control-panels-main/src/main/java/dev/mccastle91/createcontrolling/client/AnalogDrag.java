package dev.mccastle91.createcontrolling.client;

import dev.mccastle91.createcontrolling.config.CCConfig;
import dev.mccastle91.createcontrolling.item.PanelBlueprintItem;
import dev.mccastle91.createcontrolling.panel.ConsoleCasing;
import dev.mccastle91.createcontrolling.panel.ConsolePicker;
import dev.mccastle91.createcontrolling.panel.PanelBlock;
import dev.mccastle91.createcontrolling.panel.PanelBlockEntity;
import dev.mccastle91.createcontrolling.panel.element.AnalogElement;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
//? if neoforge {
import net.neoforged.neoforge.client.event.InputEvent;
//?} else {
/*import net.minecraftforge.client.event.InputEvent;
*///?}

import java.util.Set;

/**
 * Holding the use key on a knob or slider: instead of the click going to the
 * server, a {@link AnalogDragScreen} takes over until the key is let go. A
 * quick tap still steps the control up one, as a click always did. Sneaking
 * is left alone (sneak-click steps down, sneak-scroll nudges), so nobody has
 * to sneak — and fall off a seat — to set a value.
 */
public final class AnalogDrag {

    /** Items whose right-click on a console means something else. */
    private static final Set<String> TOOLS = Set.of("create:wrench", "create:clipboard", "create:display_link");

    /** The knob or slider under the crosshair, and the console it is on. */
    public record Aim(BlockPos pos, AnalogElement element) {
    }

    private AnalogDrag() {
    }

    public static Aim aimedAnalog(Minecraft mc) {
        if (mc.level == null || mc.player == null)
            return null;
        if (!(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK)
            return null;
        BlockPos pos = hit.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);
        if (!(state.getBlock() instanceof PanelBlock) || !(mc.level.getBlockEntity(pos) instanceof PanelBlockEntity panel))
            return null;
        Vec3 origin = Vec3.atLowerCornerOf(pos);
        Vec3 eye = mc.player.getEyePosition().subtract(origin);
        Vec3 direction = hit.getLocation().subtract(origin).subtract(eye);
        var pick = ConsolePicker.pick(state, panel.shape(), panel.getLayout(), eye, direction);
        if (pick.isEmpty() || !(pick.get().element() instanceof AnalogElement analog))
            return null;
        return new Aim(pos, analog);
    }

    static void onUseKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem())
            return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null || mc.player.isShiftKeyDown() || mc.player.isSpectator())
            return;
        if (usesConsoleItself(mc.player.getMainHandItem()))
            return;
        Aim aim = aimedAnalog(mc);
        if (aim == null)
            return;
        event.setCanceled(true);
        event.setSwingHand(false);
        mc.setScreen(new AnalogDragScreen(aim.pos(), aim.element(), CCConfig.analogPopup));
    }

    /** Blueprints, casings and Create's tools act on the console, not on the part under them. */
    private static boolean usesConsoleItself(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        if (stack.getItem() instanceof PanelBlueprintItem)
            return true;
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return TOOLS.contains(id) || ConsoleCasing.byItemId(id) != null;
    }
}
