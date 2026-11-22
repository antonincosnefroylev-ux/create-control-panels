package dev.mccastle91.createcontrolling.client;

import dev.mccastle91.createcontrolling.util.McCompat;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * The shapes Create's menus are built from, drawn to any size: a coloured
 * title bar, a dark checkered body in a thin frame, and a light grey footer.
 * Create's own menus are fixed-size pictures, so these are drawn here instead,
 * pixel for pixel in the same colours (taken from Create's sequencer,
 * display link and value settings textures); the title and body colours come
 * from the console's {@link CasingTheme}.
 */
public final class ConsoleGui {

    /** Title bar height, including the black line beneath it. */
    public static final int TITLE_HEIGHT = 15;
    public static final int FOOTER_HEIGHT = 24;

    private static final ResourceLocation CHECKER = McCompat.id("create_controlling", "textures/gui/checker.png");

    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int FRAME = 0xFFB8B8B8;
    private static final int FRAME_TOP = 0xFF787878;
    private static final int PANEL = 0xFFC6C6C6;
    private static final int PANEL_SHADOW = 0xFF555555;
    private static final int SLOT_SHADOW = 0xFF373737;
    private static final int SLOT = 0xFF8B8B8B;
    /** Text on the light grey footer and panels. */
    public static final int PANEL_TEXT = 0x3D3C48;

    private ConsoleGui() {
    }

    private static int opaque(int rgb) {
        return 0xFF000000 | rgb;
    }

    /**
     * A whole window: title bar, body and footer, {@code height} tall in all.
     * The body is one pixel narrower than the bars on each side, as Create's is.
     */
    public static void window(GuiGraphics graphics, Font font, CasingTheme theme, Component title,
                              int x, int y, int width, int height) {
        titleBar(graphics, font, theme, title, x, y, width);
        int footerY = y + height - FOOTER_HEIGHT;
        body(graphics, theme, x + 1, y + TITLE_HEIGHT, width - 2, footerY - y - TITLE_HEIGHT);
        panel(graphics, x, footerY, width, FOOTER_HEIGHT);
    }

    public static void titleBar(GuiGraphics graphics, Font font, CasingTheme theme, Component title, int x, int y, int width) {
        graphics.fill(x, y, x + width, y + TITLE_HEIGHT, BLACK);
        graphics.fill(x + 1, y + 1, x + width - 1, y + TITLE_HEIGHT - 1, opaque(theme.titleShadow));
        graphics.fill(x + 1, y + 1, x + width - 3, y + TITLE_HEIGHT - 2, opaque(theme.titleHighlight));
        graphics.fill(x + 3, y + 2, x + width - 3, y + TITLE_HEIGHT - 2, opaque(theme.title));
        graphics.drawString(font, title, x + (width - font.width(title)) / 2, y + 4, theme.titleText, false);
    }

    /** The dark checkered area behind a window's controls, in its thin grey frame. */
    public static void body(GuiGraphics graphics, CasingTheme theme, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, BLACK);
        graphics.fill(x + 1, y, x + width - 1, y + height, FRAME);
        graphics.fill(x + 2, y, x + width - 2, y + 1, FRAME_TOP);
        checker(graphics, theme.body, x + 2, y + 1, width - 4, height - 1);
    }

    /** A small window with no bars: the body alone, closed off all round. */
    public static void plaque(GuiGraphics graphics, CasingTheme theme, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, BLACK);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, FRAME);
        checker(graphics, theme.body, x + 2, y + 2, width - 4, height - 4);
    }

    /** Two close tones of one colour, alternating pixel by pixel. */
    public static void checker(GuiGraphics graphics, int rgb, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0)
            return;
        graphics.setColor(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f, 1f);
        graphics.blit(CHECKER, x, y, 0f, 0f, width, height, 16, 16);
        graphics.setColor(1f, 1f, 1f, 1f);
    }

    /** A raised light grey panel: Create's footer bar and its inventory window. */
    public static void panel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, BLACK);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_SHADOW);
        graphics.fill(x + 1, y + 1, x + width - 3, y + height - 2, WHITE);
        graphics.fill(x + 3, y + 2, x + width - 3, y + height - 2, PANEL);
    }

    /** An item slot, sunk into whatever is behind it. */
    public static void slot(GuiGraphics graphics, int x, int y, int size) {
        graphics.fill(x, y, x + size, y + size, WHITE);
        graphics.fill(x, y, x + size - 1, y + size - 1, SLOT_SHADOW);
        graphics.fill(x + 1, y + 1, x + size - 1, y + size - 1, SLOT);
    }

    /** A sunken grey field for a value or typed text, as in Create's Display Link menu. */
    public static void field(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, SLOT);
        graphics.fill(x, y, x + width, y + 1, SLOT_SHADOW);
        graphics.fill(x, y + height - 1, x + width, y + height, WHITE);
        graphics.fill(x, y + 2, x + width, y + 3, 0xFFA3A3A3);
    }

    /**
     * A button of any size in the look of Create's: grey, blue under the
     * mouse, green while switched on.
     */
    public static void button(GuiGraphics graphics, int x, int y, int width, int height,
                              boolean hovered, boolean green, boolean active) {
        int highlight, fill, shadow;
        if (!active) {
            highlight = 0xFF8B8B8B;
            fill = 0xFF6A6A6A;
            shadow = 0xFF373737;
        } else if (green) {
            highlight = hovered ? 0xFFD4E8A0 : 0xFFAEC57A;
            fill = hovered ? 0xFF2A9A64 : 0xFF218756;
            shadow = 0xFF0E2725;
        } else if (hovered) {
            highlight = WHITE;
            fill = 0xFF8892C9;
            shadow = 0xFF444964;
        } else {
            highlight = WHITE;
            fill = SLOT;
            shadow = BLACK;
        }
        graphics.fill(x, y, x + width, y + height, shadow);
        graphics.fill(x, y, x + width - 1, y + height - 1, highlight);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, fill);
    }
}
