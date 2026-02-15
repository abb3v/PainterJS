package com.breakinblocks.painterjs.objects;

import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class PaintGradient extends PaintObject {
    public final ColorProperty colorTL = new ColorProperty();
    public final ColorProperty colorTR = new ColorProperty();
    public final ColorProperty colorBL = new ColorProperty();
    public final ColorProperty colorBR = new ColorProperty();

    public ResourceLocation texture;
    public float u0 = 0.0f;
    public float v0 = 0.0f;
    public float u1 = 1.0f;
    public float v1 = 1.0f;

    @Override
    public String getType() {
        return "gradient";
    }

    @Override
    public boolean update(Map<String, Double> vars) {
        boolean changed = super.update(vars);
        colorTL.update(vars);
        colorTR.update(vars);
        colorBL.update(vars);
        colorBR.update(vars);
        return changed;
    }

    @Override
    protected void compile() {
        super.compile();
        colorTL.compile();
        colorTR.compile();
        colorBL.compile();
        colorBR.compile();
    }

    @Override
    public void deserialize(JsonObject json) {
        super.deserialize(json);
        if (json.has("color")) {
            colorTL.deserialize(json.get("color"));
            colorTR.deserialize(json.get("color"));
            colorBL.deserialize(json.get("color"));
            colorBR.deserialize(json.get("color"));
        }
        
        // Helpers for top/bottom/left/right
        if (json.has("colorT")) {
            colorTL.deserialize(json.get("colorT"));
            colorTR.deserialize(json.get("colorT"));
        }
        if (json.has("colorB")) {
            colorBL.deserialize(json.get("colorB"));
            colorBR.deserialize(json.get("colorB"));
        }
        if (json.has("colorL")) {
            colorTL.deserialize(json.get("colorL"));
            colorBL.deserialize(json.get("colorL"));
        }
        if (json.has("colorR")) {
            colorTR.deserialize(json.get("colorR"));
            colorBR.deserialize(json.get("colorR"));
        }

        if (json.has("colorTL")) colorTL.deserialize(json.get("colorTL"));
        if (json.has("colorTR")) colorTR.deserialize(json.get("colorTR"));
        if (json.has("colorBL")) colorBL.deserialize(json.get("colorBL"));
        if (json.has("colorBR")) colorBR.deserialize(json.get("colorBR"));

        if (json.has("texture")) {
            texture = ResourceLocation.parse(json.get("texture").getAsString());
        }
        
        if (json.has("u0")) u0 = json.get("u0").getAsFloat();
        if (json.has("v0")) v0 = json.get("v0").getAsFloat();
        if (json.has("u1")) u1 = json.get("u1").getAsFloat();
        if (json.has("v1")) v1 = json.get("v1").getAsFloat();
    }

    @Override
    public void read(RegistryFriendlyByteBuf buf) {
        super.read(buf);
        colorTL.read(buf);
        colorTR.read(buf);
        colorBL.read(buf);
        colorBR.read(buf);
        
        if (buf.readBoolean()) {
            texture = buf.readResourceLocation();
        }
        u0 = buf.readFloat();
        v0 = buf.readFloat();
        u1 = buf.readFloat();
        v1 = buf.readFloat();
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        super.write(buf);
        colorTL.write(buf);
        colorTR.write(buf);
        colorBL.write(buf);
        colorBR.write(buf);
        
        buf.writeBoolean(texture != null);
        if (texture != null) {
            buf.writeResourceLocation(texture);
        }
        buf.writeFloat(u0);
        buf.writeFloat(v0);
        buf.writeFloat(u1);
        buf.writeFloat(v1);
    }
}