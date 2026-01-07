package com.breakinblocks.painterjs.objects;

import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class PaintRectangle extends PaintObject {
    public int color = 0xFFFFFFFF;
    public ResourceLocation texture;
    public float u0 = 0.0f;
    public float v0 = 0.0f;
    public float u1 = 1.0f;
    public float v1 = 1.0f;

    @Override
    public String getType() {
        return "rectangle";
    }

    @Override
    public void deserialize(JsonObject json) {
        super.deserialize(json);
        // w/h handled by super
        if (json.has("color")) {
            String c = json.get("color").getAsString();
            if (c.startsWith("#")) {
                try {
                    long val = Long.parseLong(c.substring(1), 16);
                    if (c.length() == 7)
                        val |= 0xFF000000;
                    color = (int) val;
                } catch (Exception ignored) {
                }
            } else {
                try {
                    color = json.get("color").getAsInt();
                } catch (Exception ignored) {
                }
            }
        }
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

    @Override
    public void read(RegistryFriendlyByteBuf buf) {
        super.read(buf);
        color = buf.readInt();
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
        buf.writeInt(color);
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
