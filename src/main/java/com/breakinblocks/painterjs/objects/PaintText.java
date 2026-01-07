package com.breakinblocks.painterjs.objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import java.util.ArrayList;
import java.util.List;

public class PaintText extends PaintObject {
    public Component text;
    public List<Component> textLines = new ArrayList<>();
    public float scale = 1.0f;
    public boolean shadow = true;
    public int color = 0xFFFFFFFF;
    public boolean centered = false;
    public float lineSpacing = 1.0f;

    @Override
    public String getType() {
        return "text";
    }

    @Override
    public void deserialize(JsonObject json) {
        super.deserialize(json);
        if (json.has("text")) {
            JsonElement t = json.get("text");
            if (t.isJsonArray()) {
                textLines.clear();
                t.getAsJsonArray().forEach(e -> textLines.add(parseComponent(e)));
            } else {
                text = parseComponent(t);
            }
        }

        if (json.has("textLines")) {
            JsonArray arr = json.getAsJsonArray("textLines");
            textLines.clear();
            arr.forEach(e -> textLines.add(parseComponent(e)));
        }

        if (json.has("scale"))
            scale = json.get("scale").getAsFloat();
        if (json.has("shadow"))
            shadow = json.get("shadow").getAsBoolean();
        if (json.has("centered"))
            centered = json.get("centered").getAsBoolean();
        if (json.has("lineSpacing"))
            lineSpacing = json.get("lineSpacing").getAsFloat();

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
    }

    private Component parseComponent(JsonElement e) {
        try {
            return Component.Serializer.fromJson(e.toString(), net.minecraft.core.RegistryAccess.EMPTY);
        } catch (Exception ex) {
            return Component.literal(e.getAsString());
        }
    }

    @Override
    public void read(RegistryFriendlyByteBuf buf) {
        super.read(buf);
        if (buf.readBoolean()) {
            text = ComponentSerialization.STREAM_CODEC.decode(buf);
        } else {
            text = null;
        }

        int lineCount = buf.readInt();
        textLines.clear();
        for (int i = 0; i < lineCount; i++) {
            textLines.add(ComponentSerialization.STREAM_CODEC.decode(buf));
        }

        scale = buf.readFloat();
        shadow = buf.readBoolean();
        color = buf.readInt();
        centered = buf.readBoolean();
        lineSpacing = buf.readFloat();
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        super.write(buf);
        buf.writeBoolean(text != null);
        if (text != null) {
            ComponentSerialization.STREAM_CODEC.encode(buf, text);
        }

        buf.writeInt(textLines.size());
        for (Component c : textLines) {
            ComponentSerialization.STREAM_CODEC.encode(buf, c);
        }

        buf.writeFloat(scale);
        buf.writeBoolean(shadow);
        buf.writeInt(color);
        buf.writeBoolean(centered);
        buf.writeFloat(lineSpacing);
    }
}
