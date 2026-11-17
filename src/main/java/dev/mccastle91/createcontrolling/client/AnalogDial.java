package dev.mccastle91.createcontrolling.client;

import com.mojang.math.Axis;
import dev.mccastle91.createcontrolling.panel.element.AnalogElement;
import dev.mccastle91.createcontrolling.panel.element.KnobElement;
import dev.mccastle91.createcontrolling.panel.element.SliderElement;
import dev.mccastle91.createcontrolling.util.McCompat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * A knob or slider drawn flat for the screen, as seen from straight above:
 * the same plates, ticks, body and pointer as the part on the console (see
 * PanelBlockEntityRenderer), from the same textures and in the part's own
 * colour — so the dial window shows the control you are holding, not a
 * different widget.
 */
public final class AnalogDial {

    private static final ResourceLocation COMPONENT = McCompat.id("create_controlling", "textures/block/component.png");
    private static final ResourceLocation BRASS = McCompat.id("create", "textures/block/brass_block.png");
    private static final ResourceLocation IRON = McCompat.id("create", "textures/block/industrial_iron_block.png");

    private static final int WHITE = 0xFFFFFFFF;
    private static final int DARK = 0xFF1C1C1F;
    private static final int POINTER = 0xFFF0F0F0;

    /** Drawing units per face pixel: fine enough for the parts' quarter- and tenth-pixel details. */
    private static final int UNITS = 20;

    private AnalogDial() {
    }

    /**
     * Draws {@code element} showing {@code value}, its top-left corner at
     * ({@code x}, {@code y}), {@code scale} screen pixels per face pixel.
     */
    public static void draw(GuiGraphics graphics, AnalogElement element, int value, float x, float y, float scale) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(scale / UNITS, scale / UNITS, 1f);
        int color = McCompat.dyeRgb(element.color());
        if (element instanceof KnobElement)
            knob(graphics, element.width(), element.depth(), value, color);
        else if (element instanceof SliderElement slider)
            slider(graphics, element.width(), element.depth(), slider.isVertical(), value, color);
        graphics.pose().popPose();
    }

    private static void knob(GuiGraphics graphics, float w, float d, int value, int color) {
        rect(graphics, BRASS, 0, 0, w, d, WHITE);
        float r = w / 2;
        for (int tick : new int[] { 0, AnalogElement.MAX_VALUE / 2, AnalogElement.MAX_VALUE }) {
            turned(graphics, r, d / 2, KnobElement.angleFor(tick));
            rect(graphics, COMPONENT, -0.15f, -r + 0.05f, 0.15f, -r + 0.45f, DARK);
            graphics.pose().popPose();
        }
        turned(graphics, r, d / 2, KnobElement.angleFor(value));
        float body = r - 0.5f, cap = body - 0.4f;
        // the body's shoulder sits lower than its cap: a shade darker reads as that step
        rect(graphics, COMPONENT, -body, -body, body, body, shade(color, 0.8f));
        rect(graphics, COMPONENT, -cap, -cap, cap, cap, color);
        rect(graphics, COMPONENT, -0.25f, -body, 0.25f, -0.3f, POINTER);
        graphics.pose().popPose();
    }

    private static void slider(GuiGraphics graphics, float w, float d, boolean vertical, int value, int color) {
        rect(graphics, IRON, 0, 0, w, d, WHITE);
        float f = SliderElement.fractionFor(value);
        float m = SliderElement.END_MARGIN;
        if (vertical) {
            rect(graphics, COMPONENT, w / 2 - 0.2f, m / 2, w / 2 + 0.2f, d - m / 2, DARK);
            float cy = d - m - f * (d - 2 * m);
            rect(graphics, COMPONENT, 0.15f, cy - m, w - 0.15f, cy + m, color);
            rect(graphics, COMPONENT, 0.15f, cy - 0.12f, w - 0.15f, cy + 0.12f, DARK);
        } else {
            rect(graphics, COMPONENT, m / 2, d / 2 - 0.2f, w - m / 2, d / 2 + 0.2f, DARK);
            float cx = m + f * (w - 2 * m);
            rect(graphics, COMPONENT, cx - m, 0.15f, cx + m, d - 0.15f, color);
            rect(graphics, COMPONENT, cx - 0.12f, 0.15f, cx + 0.12f, d - 0.15f, DARK);
        }
    }

    /** Pushes a pose turned {@code degrees} clockwise about face px ({@code cx}, {@code cy}); the caller pops it. */
    private static void turned(GuiGraphics graphics, float cx, float cy, float degrees) {
        graphics.pose().pushPose();
        graphics.pose().translate(cx * UNITS, cy * UNITS, 0);
        graphics.pose().mulPose(Axis.ZP.rotationDegrees(degrees));
    }

    /** A textured, tinted rectangle in face px, showing as much of the texture as the part on the console does. */
    private static void rect(GuiGraphics graphics, ResourceLocation texture, float x0, float y0, float x1, float y1, int argb) {
        int left = Math.round(x0 * UNITS), top = Math.round(y0 * UNITS);
        int width = Math.round(x1 * UNITS) - left, height = Math.round(y1 * UNITS) - top;
        if (width <= 0 || height <= 0)
            return;
        int texW = Math.max(1, Math.min(16, Math.round(x1 - x0))), texH = Math.max(1, Math.min(16, Math.round(y1 - y0)));
        graphics.setColor(((argb >> 16) & 0xFF) / 255f, ((argb >> 8) & 0xFF) / 255f, (argb & 0xFF) / 255f, 1f);
        graphics.blit(texture, left, top, width, height, 0f, 0f, texW, texH, 16, 16);
        graphics.setColor(1f, 1f, 1f, 1f);
    }

    private static int shade(int argb, float factor) {
        int r = (int) (((argb >> 16) & 0xFF) * factor);
        int g = (int) (((argb >> 8) & 0xFF) * factor);
        int b = (int) ((argb & 0xFF) * factor);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
