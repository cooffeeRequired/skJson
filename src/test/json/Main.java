package json;

import com.google.gson.JsonParser;

@SuppressWarnings("all")
public class Main {


//    public static void main2(String[] args) {
//        ItemStack itemStack = new ItemStack(org.bukkit.Material.DIAMOND_SWORD, 1);
//
//        String json = gson.toJson(itemStack);
//        System.out.println("Serialized ItemStack:");
//        System.out.println(json);
//
//        ItemStack deserializedItemStack = gson.fromJson(json, ItemStack.class);
//        System.out.println("Deserialized ItemStack:");
//        System.out.println(deserializedItemStack.getType() + " x " + deserializedItemStack.getAmount());
//    }

    public static void main(String[] args) {
        System.out.println(JsonParser.parseString("test raw ?"));
    }
}
