package com.breakinblocks.painterjs.client;

import com.breakinblocks.painterjs.objects.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;

import java.util.*;
import java.util.stream.Collectors;

public class ClientPainter {
    private static final Map<String, PaintObject> OBJECTS = new HashMap<>();
    private static final List<PaintObject> CACHED_SORTED_LIST = new ArrayList<>();
    private static boolean needsResort = true;

    public static void update(Map<String, PaintObject> newObjects) {
        newObjects.forEach((id, obj) -> {
            if (obj.remove) {
                OBJECTS.remove(id);
            } else {
                OBJECTS.put(id, obj);
            }
        });
        needsResort = true;
    }

    public static void clear() {
        OBJECTS.clear();
        CACHED_SORTED_LIST.clear();
        needsResort = true;
    }

    private static void updateObjects(Map<String, Double> vars) {
        for (PaintObject obj : OBJECTS.values()) {
            if (obj.update(vars)) {
                needsResort = true;
            }
        }

        if (needsResort) {
            CACHED_SORTED_LIST.clear();
            CACHED_SORTED_LIST.addAll(OBJECTS.values());
            CACHED_SORTED_LIST.sort(Comparator.comparingDouble(o -> o.z));
            needsResort = false;
        }
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (OBJECTS.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        double mouseX = mc.mouseHandler.xpos() * screenWidth / (double) mc.getWindow().getScreenWidth();
        double mouseY = mc.mouseHandler.ypos() * screenHeight / (double) mc.getWindow().getScreenHeight();
        float delta = deltaTracker.getGameTimeDeltaTicks();

        Map<String, Double> vars = new HashMap<>();
        vars.put("$screenW", (double) screenWidth);
        vars.put("$screenH", (double) screenHeight);
        vars.put("$delta", (double) delta);
        vars.put("$mouseX", mouseX);
        vars.put("$mouseY", mouseY);

        updateObjects(vars);

        for (PaintObject obj : CACHED_SORTED_LIST) {
            if (!obj.visible || obj.cachedDraw == PaintObject.DrawMode.GUI) continue;
            drawObject(graphics, obj, screenWidth, screenHeight);
        }
    }

    public static void renderScreen(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (OBJECTS.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        Map<String, Double> vars = new HashMap<>();
        vars.put("$screenW", (double) screenWidth);
        vars.put("$screenH", (double) screenHeight);
        vars.put("$delta", (double) partialTick);
        vars.put("$mouseX", (double) mouseX);
        vars.put("$mouseY", (double) mouseY);

        updateObjects(vars);

        for (PaintObject obj : CACHED_SORTED_LIST) {
            if (!obj.visible || obj.cachedDraw == PaintObject.DrawMode.INGAME) continue;
            drawObject(graphics, obj, screenWidth, screenHeight);
        }
    }

    private static void drawObject(GuiGraphics graphics, PaintObject obj, int sw, int sh) {
        float x = obj.x;
        float y = obj.y;

        switch (obj.cachedAlignX) {
            case CENTER -> x += sw / 2f;
            case RIGHT -> x += sw;
        }

        switch (obj.cachedAlignY) {
            case CENTER -> y += sh / 2f;
            case BOTTOM -> y += sh;
        }

        x += obj.moveX;
        y += obj.moveY;

        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);

        if (obj instanceof PaintRectangle rect) {
            drawRectangle(graphics, rect);
        } else if (obj instanceof PaintGradient grad) {
            drawGradient(graphics, grad);
        } else if (obj instanceof PaintText text) {
            drawText(graphics, text);
        } else if (obj instanceof PaintItem item) {
            drawItem(graphics, item);
        }

        graphics.pose().popPose();
    }

    private static void drawRectangle(GuiGraphics graphics, PaintRectangle rect) {
        float w = rect.w + rect.expandW;
        float h = rect.h + rect.expandH;

        if (rect.texture != null) {
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, rect.texture);
            RenderSystem.enableBlend();

            int color = rect.color;
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;
            float a = ((color >> 24) & 0xFF) / 255f;

            Matrix4f matrix = graphics.pose().last().pose();
            BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
                    DefaultVertexFormat.POSITION_TEX_COLOR);
            buffer.addVertex(matrix, 0, h, 0).setColor(r, g, b, a).setUv(rect.u0, rect.v1);
            buffer.addVertex(matrix, w, h, 0).setColor(r, g, b, a).setUv(rect.u1, rect.v1);
            buffer.addVertex(matrix, w, 0, 0).setColor(r, g, b, a).setUv(rect.u1, rect.v0);
            buffer.addVertex(matrix, 0, 0, 0).setColor(r, g, b, a).setUv(rect.u0, rect.v0);
            BufferUploader.drawWithShader(buffer.buildOrThrow());

            RenderSystem.disableBlend();
        } else {
            graphics.fill(0, 0, (int) w, (int) h, rect.color);
        }
    }

    private static void drawGradient(GuiGraphics graphics, PaintGradient grad) {
        float w = grad.w + grad.expandW;
        float h = grad.h + grad.expandH;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        if (grad.texture != null) {
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, grad.texture);
        } else {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
        }

        Matrix4f matrix = graphics.pose().last().pose();
        VertexFormat format = grad.texture != null ? DefaultVertexFormat.POSITION_TEX_COLOR
                : DefaultVertexFormat.POSITION_COLOR;
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, format);

        addVertex(buffer, matrix, 0, h, grad.colorBL, grad.u0, grad.v1, grad.texture != null);
        addVertex(buffer, matrix, w, h, grad.colorBR, grad.u1, grad.v1, grad.texture != null);
        addVertex(buffer, matrix, w, 0, grad.colorTR, grad.u1, grad.v0, grad.texture != null);
        addVertex(buffer, matrix, 0, 0, grad.colorTL, grad.u0, grad.v0, grad.texture != null);

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
    }

    private static void addVertex(BufferBuilder buf, Matrix4f mat, float x, float y, int color, float u, float v,
            boolean tex) {
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;

        buf.addVertex(mat, x, y, 0).setColor(r, g, b, a);
        if (tex)
            buf.setUv(u, v);
    }

    private static void drawText(GuiGraphics graphics, PaintText text) {
        graphics.pose().pushPose();
        graphics.pose().scale(text.scale, text.scale, 1.0f);

        int color = text.color;
        float yOff = 0;

        List<Component> allLines = new ArrayList<>();
        if (text.text != null && !text.text.getString().isEmpty())
            allLines.add(text.text);
        allLines.addAll(text.textLines);

        for (Component comp : allLines) {
            int drawX = 0;
            if (text.centered) {
                drawX -= Minecraft.getInstance().font.width(comp) / 2;
            }

            if (text.shadow) {
                graphics.drawString(Minecraft.getInstance().font, comp, drawX, (int) yOff, color);
            } else {
                graphics.drawString(Minecraft.getInstance().font, comp, drawX, (int) yOff, color, false);
            }

            yOff += (Minecraft.getInstance().font.lineHeight + 1) * text.lineSpacing;
        }

        graphics.pose().popPose();
    }

    private static void drawItem(GuiGraphics graphics, PaintItem item) {
        if (!item.item.isEmpty()) {
            graphics.pose().pushPose();
            if (item.rotation != 0) {
                graphics.pose().translate(8, 8, 0);
                graphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(item.rotation));
                graphics.pose().translate(-8, -8, 0);
            }

            graphics.renderItem(item.item, 0, 0);
            if (item.overlay) {
                graphics.renderItemDecorations(Minecraft.getInstance().font, item.item, 0, 0, item.customText);
            }
            graphics.pose().popPose();
        }
    }
}
