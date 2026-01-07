package com.breakinblocks.painterjs.objects;

import com.breakinblocks.painterjs.math.MathExpression;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Map;
import java.util.function.Consumer;

public abstract class PaintObject {
    public static final StreamCodec<RegistryFriendlyByteBuf, PaintObject> STREAM_CODEC = StreamCodec.of(
            PaintObject::toNetwork,
            PaintObject::fromNetwork);

    public enum AlignX { LEFT, CENTER, RIGHT }
    public enum AlignY { TOP, CENTER, BOTTOM }
    public enum DrawMode { INGAME, GUI }

    public boolean visible = true;
    public float x;
    public String xExpr;
    private MathExpression xMath;
    public float y;
    public String yExpr;
    private MathExpression yMath;
    public float z;
    public String zExpr;
    private MathExpression zMath;
    public float w;
    public String wExpr;
    private MathExpression wMath;
    public float h;
    public String hExpr;
    private MathExpression hMath;

    public String alignX = "left";
    public transient AlignX cachedAlignX = AlignX.LEFT;

    public String alignY = "top";
    public transient AlignY cachedAlignY = AlignY.TOP;

    public String draw = "ingame";
    public transient DrawMode cachedDraw = DrawMode.INGAME;

    public float moveX;
    public String moveXExpr;
    private MathExpression moveXMath;
    public float moveY;
    public String moveYExpr;
    private MathExpression moveYMath;
    public float expandW;
    public String expandWExpr;
    private MathExpression expandWMath;
    public float expandH;
    public String expandHExpr;
    private MathExpression expandHMath;

    public boolean remove;

    public PaintObject() {
    }

    public boolean update(Map<String, Double> vars) {
        boolean changed = false;
        if (xMath != null) { x = (float) xMath.eval(vars); }
        if (yMath != null) { y = (float) yMath.eval(vars); }
        if (zMath != null) {
            float oldZ = z;
            z = (float) zMath.eval(vars);
            if (oldZ != z) changed = true;
        }
        if (wMath != null) { w = (float) wMath.eval(vars); }
        if (hMath != null) { h = (float) hMath.eval(vars); }
        if (moveXMath != null) { moveX = (float) moveXMath.eval(vars); }
        if (moveYMath != null) { moveY = (float) moveYMath.eval(vars); }
        if (expandWMath != null) { expandW = (float) expandWMath.eval(vars); }
        if (expandHMath != null) { expandH = (float) expandHMath.eval(vars); }
        return changed;
    }

    private void compile() {
        try {
            if (xExpr != null) xMath = MathExpression.parse(xExpr);
            if (yExpr != null) yMath = MathExpression.parse(yExpr);
            if (zExpr != null) zMath = MathExpression.parse(zExpr);
            if (wExpr != null) wMath = MathExpression.parse(wExpr);
            if (hExpr != null) hMath = MathExpression.parse(hExpr);
            if (moveXExpr != null) moveXMath = MathExpression.parse(moveXExpr);
            if (moveYExpr != null) moveYMath = MathExpression.parse(moveYExpr);
            if (expandWExpr != null) expandWMath = MathExpression.parse(expandWExpr);
            if (expandHExpr != null) expandHMath = MathExpression.parse(expandHExpr);

            updateEnums();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateEnums() {
        cachedAlignX = switch (alignX) {
            case "center" -> AlignX.CENTER;
            case "right" -> AlignX.RIGHT;
            default -> AlignX.LEFT;
        };
        cachedAlignY = switch (alignY) {
            case "center" -> AlignY.CENTER;
            case "bottom" -> AlignY.BOTTOM;
            default -> AlignY.TOP;
        };
        cachedDraw = "gui".equals(draw) ? DrawMode.GUI : DrawMode.INGAME;
    }

    public static PaintObject fromJson(JsonObject json) {
        if (json.has("remove") && json.get("remove").getAsBoolean()) {
            PaintObject obj = new PaintObject() {
                @Override public String getType() { return "remove"; }
                @Override public void write(RegistryFriendlyByteBuf buf) {}
            };
            obj.remove = true;
            return obj;
        }

        String type = json.has("type") ? json.get("type").getAsString() : "rectangle";
        if ("atlas_texture".equals(type)) type = "rectangle";

        PaintObject object = switch (type) {
            case "rectangle" -> new PaintRectangle();
            case "gradient" -> new PaintGradient();
            case "text" -> new PaintText();
            case "item" -> new PaintItem();
            default -> new PaintRectangle();
        };

        object.deserialize(json);
        object.compile();
        return object;
    }

    public static PaintObject fromNetwork(RegistryFriendlyByteBuf buf) {
        String type = buf.readUtf();
        if ("remove".equals(type)) {
            PaintObject obj = new PaintObject() {
                @Override public String getType() { return "remove"; }
                @Override public void write(RegistryFriendlyByteBuf buf) {}
            };
            obj.remove = true;
            return obj;
        }

        PaintObject object = switch (type) {
            case "rectangle" -> new PaintRectangle();
            case "gradient" -> new PaintGradient();
            case "text" -> new PaintText();
            case "item" -> new PaintItem();
            default -> throw new IllegalArgumentException("Unknown paint object type: " + type);
        };

        object.read(buf);
        object.compile();
        return object;
    }

    public static void toNetwork(RegistryFriendlyByteBuf buf, PaintObject object) {
        if (object.remove) {
            buf.writeUtf("remove");
            return;
        }
        buf.writeUtf(object.getType());
        object.write(buf);
    }

    protected void setFloat(JsonElement el, Consumer<Float> setter, Consumer<String> exprSetter) {
        try {
            setter.accept(el.getAsFloat());
        } catch (Exception e) {
            exprSetter.accept(el.getAsString());
        }
    }

    protected void writeFloat(RegistryFriendlyByteBuf buf, float val, String expr) {
        buf.writeBoolean(expr != null);
        if (expr != null) buf.writeUtf(expr);
        else buf.writeFloat(val);
    }

    public void deserialize(JsonObject json) {
        if (json.has("visible")) visible = json.get("visible").getAsBoolean();

        if (json.has("x")) setFloat(json.get("x"), v -> x = v, v -> xExpr = v);
        if (json.has("y")) setFloat(json.get("y"), v -> y = v, v -> yExpr = v);
        if (json.has("z")) setFloat(json.get("z"), v -> z = v, v -> zExpr = v);
        if (json.has("w")) setFloat(json.get("w"), v -> w = v, v -> wExpr = v);
        if (json.has("h")) setFloat(json.get("h"), v -> h = v, v -> hExpr = v);

        if (json.has("alignX")) alignX = json.get("alignX").getAsString();
        if (json.has("alignY")) alignY = json.get("alignY").getAsString();
        if (json.has("draw")) draw = json.get("draw").getAsString();

        if (json.has("moveX")) setFloat(json.get("moveX"), v -> moveX = v, v -> moveXExpr = v);
        if (json.has("moveY")) setFloat(json.get("moveY"), v -> moveY = v, v -> moveYExpr = v);
        if (json.has("expandW")) setFloat(json.get("expandW"), v -> expandW = v, v -> expandWExpr = v);
        if (json.has("expandH")) setFloat(json.get("expandH"), v -> expandH = v, v -> expandHExpr = v);

        // Backwards compatibility for 'align'
        if (json.has("align")) {
            String a = json.get("align").getAsString();
            if (a.contains("bottom")) alignY = "bottom";
            else if (a.contains("center")) alignY = "center";

            if (a.contains("right")) alignX = "right";
            else if (a.contains("center")) alignX = "center";
        }
    }

    public void read(RegistryFriendlyByteBuf buf) {
        visible = buf.readBoolean();
        x = readFloatOrExpr(buf, v -> xExpr = v);
        y = readFloatOrExpr(buf, v -> yExpr = v);
        z = readFloatOrExpr(buf, v -> zExpr = v);
        w = readFloatOrExpr(buf, v -> wExpr = v);
        h = readFloatOrExpr(buf, v -> hExpr = v);

        alignX = buf.readUtf();
        alignY = buf.readUtf();
        draw = buf.readUtf();

        moveX = readFloatOrExpr(buf, v -> moveXExpr = v);
        moveY = readFloatOrExpr(buf, v -> moveYExpr = v);
        expandW = readFloatOrExpr(buf, v -> expandWExpr = v);
        expandH = readFloatOrExpr(buf, v -> expandHExpr = v);
    }

    private float readFloatOrExpr(RegistryFriendlyByteBuf buf, Consumer<String> exprSetter) {
        if (buf.readBoolean()) {
            exprSetter.accept(buf.readUtf());
            return 0;
        } else {
            return buf.readFloat();
        }
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(visible);
        writeFloat(buf, x, xExpr);
        writeFloat(buf, y, yExpr);
        writeFloat(buf, z, zExpr);
        writeFloat(buf, w, wExpr);
        writeFloat(buf, h, hExpr);

        buf.writeUtf(alignX);
        buf.writeUtf(alignY);
        buf.writeUtf(draw);

        writeFloat(buf, moveX, moveXExpr);
        writeFloat(buf, moveY, moveYExpr);
        writeFloat(buf, expandW, expandWExpr);
        writeFloat(buf, expandH, expandHExpr);
    }

    public abstract String getType();
}
