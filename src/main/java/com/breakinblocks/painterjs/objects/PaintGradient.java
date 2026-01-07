package com.breakinblocks.painterjs.objects;

import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class PaintGradient extends PaintObject {
    public int colorT = 0xFFFFFFFF;
    public int colorB = 0xFFFFFFFF;
    public int colorL = 0xFFFFFFFF;
    public int colorR = 0xFFFFFFFF;
    public int colorTL = 0xFFFFFFFF;
    public int colorTR = 0xFFFFFFFF;
    public int colorBL = 0xFFFFFFFF;
    public int colorBR = 0xFFFFFFFF;
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
    public void deserialize(JsonObject json) {
        super.deserialize(json);
        if (json.has("color")) {
            int c = parseColor(json.get("color").getAsString());
            colorT = colorB = colorL = colorR = colorTL = colorTR = colorBL = colorBR = c;
        }
        if (json.has("colorT"))
            colorT = parseColor(json.get("colorT").getAsString());
        if (json.has("colorB"))
            colorB = parseColor(json.get("colorB").getAsString());
        if (json.has("colorL"))
            colorL = parseColor(json.get("colorL").getAsString());
        if (json.has("colorR"))
            colorR = parseColor(json.get("colorR").getAsString());
        if (json.has("colorTL"))
            colorTL = parseColor(json.get("colorTL").getAsString());
        if (json.has("colorTR"))
            colorTR = parseColor(json.get("colorTR").getAsString());
        if (json.has("colorBL"))
            colorBL = parseColor(json.get("colorBL").getAsString());
        if (json.has("colorBR"))
            colorBR = parseColor(json.get("colorBR").getAsString());

        if (json.has("texture")) {
            texture = ResourceLocation.parse(json.get("texture").getAsString());
        }
        if (json.has("u0"))
            u0 = json.get("u0").getAsFloat();
        if (json.has("v0"))
            v0 = json.get("v0").getAsFloat();
        if (json.has("u1"))
            u1 = json.get("u1").getAsFloat();
        if (json.has("v1"))
            v1 = json.get("v1").getAsFloat();
    }

    private int parseColor(String c) {
        if (c.startsWith("#")) {
            try {
                long val = Long.parseLong(c.substring(1), 16);
                if (c.length() == 7)
                    val |= 0xFF000000;
                return (int) val;
            } catch (Exception ignored) {
            }
        }
        return 0xFFFFFFFF;
    }

    @Override
    public void read(RegistryFriendlyByteBuf buf) {
        super.read(buf);
        colorTL = buf.readInt();
        colorTR = buf.readInt();
        colorBL = buf.readInt();
        colorBR = buf.readInt();
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
        // We only really need 4 corners for a quad, T/B/L/R are just helpers for setup
        buf.writeInt(colorTL);
        buf.writeInt(colorTR);
        buf.writeInt(colorBL);
        buf.writeInt(colorBR);
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
