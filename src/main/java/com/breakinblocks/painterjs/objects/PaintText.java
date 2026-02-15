package com.breakinblocks.painterjs.objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PaintText extends PaintObject {
    public Component text;
    public List<Component> textLines = new ArrayList<>();
    
    private String rawText;
    private List<String> rawTextLines = new ArrayList<>();

    public float scale = 1.0f;
    public boolean shadow = true;
    public boolean centered = false;
    public float lineSpacing = 1.0f;

    @Override
    public String getType() {
        return "text";
    }

    @Override
    public boolean update(Map<String, Double> vars) {
        boolean changed = super.update(vars);
        
        if (rawText != null) {
            text = Component.literal(replaceVars(rawText, vars));
        }
        
        if (!rawTextLines.isEmpty()) {
            textLines.clear();
            for (String line : rawTextLines) {
                textLines.add(Component.literal(replaceVars(line, vars)));
            }
        }
        
        return changed;
    }

    @Override
    public void deserialize(JsonObject json) {
        super.deserialize(json);
        if (json.has("text")) {
            JsonElement t = json.get("text");
            if (t.isJsonArray()) {
                rawTextLines.clear();
                t.getAsJsonArray().forEach(e -> rawTextLines.add(e.getAsString()));
            } else {
                rawText = t.getAsString();
                text = Component.literal(rawText);
            }
        }

        if (json.has("textLines")) {
            JsonArray arr = json.getAsJsonArray("textLines");
            rawTextLines.clear();
            arr.forEach(e -> rawTextLines.add(e.getAsString()));
        }

        if (json.has("scale")) scale = json.get("scale").getAsFloat();
        if (json.has("shadow")) shadow = json.get("shadow").getAsBoolean();
        if (json.has("centered")) centered = json.get("centered").getAsBoolean();
        if (json.has("lineSpacing")) lineSpacing = json.get("lineSpacing").getAsFloat();
    }

    @Override
    public void read(RegistryFriendlyByteBuf buf) {
        super.read(buf);
        if (buf.readBoolean()) {
            rawText = buf.readUtf();
            text = Component.literal(rawText);
        } else {
            rawText = null;
            text = null;
        }

        int lineCount = buf.readInt();
        rawTextLines.clear();
        textLines.clear();
        for (int i = 0; i < lineCount; i++) {
            String line = buf.readUtf();
            rawTextLines.add(line);
            textLines.add(Component.literal(line));
        }

        scale = buf.readFloat();
        shadow = buf.readBoolean();
        centered = buf.readBoolean();
        lineSpacing = buf.readFloat();
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        super.write(buf);
        buf.writeBoolean(rawText != null);
        if (rawText != null) {
            buf.writeUtf(rawText);
        }

        buf.writeInt(rawTextLines.size());
        for (String s : rawTextLines) {
            buf.writeUtf(s);
        }

        buf.writeFloat(scale);
        buf.writeBoolean(shadow);
        buf.writeBoolean(centered);
        buf.writeFloat(lineSpacing);
    }
}