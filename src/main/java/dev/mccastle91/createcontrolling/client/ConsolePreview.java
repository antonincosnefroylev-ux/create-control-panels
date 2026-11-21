package dev.mccastle91.createcontrolling.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.mccastle91.createcontrolling.CreateControlling;
import dev.mccastle91.createcontrolling.panel.CreatePanelBlockEntity;
import dev.mccastle91.createcontrolling.panel.PanelBlock;
import dev.mccastle91.createcontrolling.panel.PanelBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.joml.Matrix4f;
//? if neoforge {
import net.neoforged.neoforge.client.model.data.ModelData;
//?} else {
/*import net.minecraftforge.client.model.data.ModelData;
*///?}

/**
 * The console itself beside its menu, the way every Create menu shows its
 * block: the real body model and the real parts, in the console's casing and
 * at its angle, so it tilts and fills up as the designer changes it.
 *
 * It draws the world's own model and renderer into the screen. Should either
 * ever fail there, the preview switches itself off for the session rather
 * than take the menu down with it.
 */
public final class ConsolePreview {

    /** A joined console is shown whole, with all four walls, not as the piece of a group it is. */
    private static final BooleanProperty[] JOINS = { PanelBlock.CONNECT_N, PanelBlock.CONNECT_S, PanelBlock.CONNECT_W,
            PanelBlock.CONNECT_E, PanelBlock.DIAG_NE, PanelBlock.DIAG_NW, PanelBlock.DIAG_SE, PanelBlock.DIAG_SW };

    private static boolean broken;

    private ConsolePreview() {
    }

    /**
     * Draws {@code panel} centred on ({@code centerX}, {@code centerY}),
     * {@code scale} screen pixels to the block.
     */
    public static void draw(GuiGraphics graphics, PanelBlockEntity panel, int centerX, int centerY, float scale,
                            float partialTick) {
        if (broken)
            return;
        Minecraft minecraft = Minecraft.getInstance();
        BlockState state = panel.getBlockState();
        PoseStack pose = graphics.pose();
        pose.pushPose();
        try {
            pose.translate(centerX, centerY, 150);
            // screen y runs down: flip it, as the game does for items in menus
            ClientCompat.mulPose(pose, new Matrix4f().scaling(1f, -1f, 1f));
            pose.scale(scale, scale, scale);
            pose.mulPose(Axis.XP.rotationDegrees(30f));
            // the console's front (the edge towards whoever uses it) turned towards the window on its left
            float facing = state.hasProperty(HorizontalDirectionalBlock.FACING)
                    ? state.getValue(HorizontalDirectionalBlock.FACING).toYRot() - 180f : 0f;
            pose.mulPose(Axis.YP.rotationDegrees(-25f + facing));
            pose.translate(-0.5f, -0.5f, -0.5f);

            MultiBufferSource.BufferSource buffers = graphics.bufferSource();
            BlockRenderDispatcher blocks = minecraft.getBlockRenderer();
            BlockState whole = state;
            for (BooleanProperty join : JOINS)
                if (whole.hasProperty(join))
                    whole = whole.setValue(join, false);
            BakedModel model = blocks.getBlockModel(whole);
            ModelData data = panel instanceof CreatePanelBlockEntity console ? console.getModelData() : ModelData.EMPTY;
            blocks.getModelRenderer().renderModel(pose.last(), buffers.getBuffer(Sheets.cutoutBlockSheet()), whole, model,
                    1f, 1f, 1f, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, data, null);

            BlockEntityRenderer<BlockEntity> parts = minecraft.getBlockEntityRenderDispatcher().getRenderer(panel);
            if (parts != null)
                parts.render(panel, partialTick, pose, buffers, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            graphics.flush();
        } catch (RuntimeException e) {
            broken = true;
            CreateControlling.LOGGER.warn("Console preview switched off: it could not be drawn", e);
        } finally {
            pose.popPose();
        }
    }
}
