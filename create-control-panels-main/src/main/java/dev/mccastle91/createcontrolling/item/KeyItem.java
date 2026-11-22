package dev.mccastle91.createcontrolling.item;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.HashMap;
import java.util.Map;

public class KeyItem extends Item {
    public KeyItem(Properties properties) {
        super(properties.stacksTo(16).component(DataComponents.CUSTOM_DATA, defaultColor()));
    }
    DataComponentType<String> NBT_COLOR;

    private static CustomData defaultColor(){
        CompoundTag tag = new CompoundTag();
        tag.putString("color", "black");
        return CustomData.of(tag);
    }

    static Map<String, Integer> COLOR_KEY = new HashMap<>();

    static{
        COLOR_KEY.put("black", 0xFF000000);
        COLOR_KEY.put("white", 0xFFFFFFFF);
        COLOR_KEY.put("red", 0xFFFF0000);
        COLOR_KEY.put("lime", 0xFF00FF00);
        COLOR_KEY.put("blue", 0xFF0000FF);
        COLOR_KEY.put("yellow", 0xFFFFFF00);
        COLOR_KEY.put("cyan", 0xFF00FFFF);
        COLOR_KEY.put("magenta", 0xFFFF00FF);
        COLOR_KEY.put("light_gray", 0xFFC0C0C0);
        COLOR_KEY.put("gray", 0xFF808080);
        COLOR_KEY.put("orange", 0xFFF9801D);
        COLOR_KEY.put("pink", 0xFFF38BAA);
        COLOR_KEY.put("green", 0xFF008000);
        COLOR_KEY.put("purple", 0xFF800080);
        COLOR_KEY.put("brown", 0xFF835432);
        COLOR_KEY.put("light_blue", 0xFF3AB3DA);
    }

    public static int getColor(String color){
        return COLOR_KEY.containsKey(color) ? COLOR_KEY.get(color) : COLOR_KEY.get("black");
    }

    public static void setColorKey(ItemStack itemStack, String color){
        String finalColor = COLOR_KEY.containsKey(color) ? color : "black";
        CustomData.update(DataComponents.CUSTOM_DATA, itemStack, tag -> tag.putString("color", finalColor));
    }

    public static void setCodeKey(ItemStack itemStack, Integer code){
        CustomData.update(DataComponents.CUSTOM_DATA, itemStack, tag -> tag.putInt("code", code));
    }

    public static String getColorKey(ItemStack itemStack){
        CustomData item = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return item.copyTag().getString("color");
    }
}
