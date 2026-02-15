package com.breakinblocks.painterjs.objects;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class PaintItem extends PaintObject {
    public ItemStack item = ItemStack.EMPTY;
    public boolean overlay = false;
    public String customText = null;
    public float rotation = 0.0f;

    @Override
    public String getType() {
        return "item";
    }

    @Override
    public void deserialize(JsonObject json) {
        super.deserialize(json);
        if (json.has("item")) {
            String itemId = json.get("item").getAsString();
            ResourceLocation id = ResourceLocation.tryParse(itemId);
            if (id != null && BuiltInRegistries.ITEM.containsKey(id)) {
                item = new ItemStack(BuiltInRegistries.ITEM.get(id));
            }
        } else if (json.has("id")) {
            String itemId = json.get("id").getAsString();
            ResourceLocation id = ResourceLocation.tryParse(itemId);
            if (id != null && BuiltInRegistries.ITEM.containsKey(id)) {
                item = new ItemStack(BuiltInRegistries.ITEM.get(id));
            }
        }

        if (json.has("overlay"))
            overlay = json.get("overlay").getAsBoolean();
        if (json.has("customText"))
            customText = json.get("customText").getAsString();
        if (json.has("rotation"))
            rotation = json.get("rotation").getAsFloat();
    }

    @Override
    public void read(RegistryFriendlyByteBuf buf) {
        super.read(buf);
        item = ItemStack.STREAM_CODEC.decode(buf);
        overlay = buf.readBoolean();
        if (buf.readBoolean()) {
            customText = buf.readUtf();
        } else {
            customText = null;
        }
        rotation = buf.readFloat();
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        super.write(buf);
        ItemStack.STREAM_CODEC.encode(buf, item);
        buf.writeBoolean(overlay);
        buf.writeBoolean(customText != null);
        if (customText != null) {
            buf.writeUtf(customText);
        }
        buf.writeFloat(rotation);
    }
}
