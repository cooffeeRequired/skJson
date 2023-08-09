package cz.coffee.skjson.skript.base.nbts;

import com.google.gson.*;
import cz.coffee.skjson.parser.ParserUtil;
import de.tr7zw.changeme.nbtapi.*;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: úterý (08.08.2023)
 */
public class NBTConvert {
    public static JsonElement parse(String key, NBTCompound cmp) {
        try {
            NBTType type = cmp.getType(key);
            if (type.equals(NBTType.NBTTagList)) {
                NBTCustom type0 = NBTCustom.parseList(cmp, key);
                return switch (Objects.requireNonNull(type0)) {
                    case NBTTagLongList -> {
                        final JsonArray array = new JsonArray();
                        final NBTLongList list = (NBTLongList) cmp.getLongList(key);
                        list.forEach(array::add);
                        yield array;
                    }
                    case NBTTagCompoundList -> {
                        final JsonArray array = new JsonArray();
                        final NBTCompoundList list = cmp.getCompoundList(key);
                        for (de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT readWriteNBT : list) {
                            JsonObject o = new JsonObject();
                            for (String readWriteNBTKey : readWriteNBT.getKeys()) {
                                JsonElement value = parse(readWriteNBTKey, (NBTCompound) readWriteNBT);
                                o.add(readWriteNBTKey, value);
                            }
                            array.add(o);
                        }
                        yield array;
                    }
                    case NBTTagDoubleList -> {
                        final JsonArray array = new JsonArray();
                        final NBTDoubleList list = (NBTDoubleList) cmp.getDoubleList(key);
                        list.forEach(array::add);
                        yield array;
                    }
                    case NBTTagFloatList -> {
                        final JsonArray array = new JsonArray();
                        final NBTFloatList list = (NBTFloatList) cmp.getFloatList(key);
                        list.forEach(array::add);
                        yield array;
                    }
                    case NBTTagIntList -> {
                        final JsonArray array = new JsonArray();
                        final NBTIntegerList list = (NBTIntegerList) cmp.getIntegerList(key);
                        list.forEach(array::add);
                        yield array;
                    }
                    case NBTTagStringList -> {
                        final JsonArray array = new JsonArray();
                        final NBTStringList list = (NBTStringList) cmp.getStringList(key);
                        list.forEach(array::add);
                        yield array;
                    }
                    default -> JsonNull.INSTANCE;
                };
            } else {
                NBTCustom type0 = NBTCustom.valueOf(cmp.getType(key).toString());
                return switch (type0) {
                    case NBTTagByte -> {
                        Byte value = cmp.getByte(key);
                        boolean isBool = value == 1 || value == 0;
                        if (isBool) yield JsonParser.parseString(value == 1 ? "true" : "false");
                        yield ParserUtil.parse(value);
                    }
                    case NBTTagShort -> ParserUtil.parse(cmp.getShort(key));
                    case NBTTagInt -> ParserUtil.parse(cmp.getInteger(key));
                    case NBTTagLong -> ParserUtil.parse(cmp.getLong(key));
                    case NBTTagFloat -> ParserUtil.parse(cmp.getFloat(key));
                    case NBTTagDouble -> ParserUtil.parse(cmp.getDouble(key));
                    case NBTTagCompound -> {
                        NBTCompound cmw = cmp.getCompound(key);
                        final JsonObject object = new JsonObject();
                        cmw.getKeys().forEach(key0 -> {
                            JsonElement value = NBTConvert.parse(key0, cmw);
                            object.add(key0, value);
                        });
                        yield object;
                    }
                    case NBTTagString -> new Gson().toJsonTree(cmp.getString(key));
                    case NBTTagByteArray -> {
                        final JsonArray array = new JsonArray();
                        for (byte b : cmp.getByteArray(key)) {
                            array.add(b);
                        }
                        yield array;
                    }
                    case NBTTagIntArray -> {
                        final JsonArray array = new JsonArray();
                        for (int i : cmp.getIntArray(key)) {
                            array.add(i);
                        }
                        yield array;
                    }
                    default -> JsonNull.INSTANCE;
                };
            }
        } catch (Exception Ex) {
            Ex.printStackTrace();
            return null;
        }
    }

    static void setValue(String key, JsonElement value, NBTCompound cont) {
        Object data = ParserUtil.jsonToType(value);
        Class<?> clazz = data.getClass();

        if (clazz.equals(String.class)) {
            cont.setString(key, (String) data);
        } else if (clazz.equals(Byte.class)) {
            cont.setByte(key, (Byte) data);
        } else if (data instanceof Number) {
            if (data instanceof Integer i) {
                cont.setInteger(key, i);
            } else if (data instanceof Double d) {
                cont.setDouble(key, d);
            } else if (data instanceof Float f) {
                cont.setFloat(key, f);
            } else if (data instanceof Long l) {
                cont.setLong(key, l);
            }
        } else if (clazz.equals(Boolean.class)) {
            cont.setBoolean(key, (Boolean) data);
        }
    }



    static void processList(NBTCompound main, JsonArray array, String mainKey) {
        if (array.size() > 0) {
            if (main == null) return;
            Object single = ParserUtil.jsonToType(array.get(0));
            try {
                if (single instanceof JsonObject) {
                    NBTCompoundList inList = main.getCompoundList(mainKey);
                    inList.clear();
                    for (int i = 0; i < array.size(); i++)
                        inList.addCompound(processJson(array.get(i).getAsJsonObject()));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (single instanceof Float) {
                NBTList<Float> inList = main.getFloatList(mainKey);
                inList.clear();
                for (int i = 0; i < array.size(); i++) {
                    JsonElement unparsed = array.get(i);
                    Object data = ParserUtil.jsonToType(unparsed);
                    if (data instanceof Float f) inList.add(f);
                }
            } else if (single instanceof Double) {
                NBTList<Double> inList = main.getDoubleList(mainKey);
                inList.clear();
                for (int i = 0; i < array.size(); i++) {
                    JsonElement unparsed = array.get(i);
                    Object data = ParserUtil.jsonToType(unparsed);
                    if (data instanceof Double d) inList.add(d);
                }
            } else if (single instanceof Long) {
                NBTList<Long> inList = main.getLongList(mainKey);
                inList.clear();
                for (int i = 0; i < array.size(); i++) {
                    JsonElement unparsed = array.get(i);
                    Object data = ParserUtil.jsonToType(unparsed);
                    if (data instanceof Long l) inList.add(l);
                }
            } else if (single instanceof Integer) {
                NBTList<Integer> inList = main.getIntegerList(mainKey);
                inList.clear();
                for (int i = 0; i < array.size(); i++) {
                    JsonElement unparsed = array.get(i);
                    Object data = ParserUtil.jsonToType(unparsed);
                    if (data instanceof Integer ii) inList.add(ii);
                }
            } else if (single instanceof Byte) {
                byte[] inList = main.getByteArray(mainKey);
                for (int i = 0; i < array.size(); i++) {
                    JsonElement unparsed = array.get(i);
                    Object data = ParserUtil.jsonToType(unparsed);
                    if (data instanceof Byte bb) inList[i] = bb;
                }
            } else if (single instanceof String) {
                NBTList<String> inList = main.getStringList(mainKey);
                inList.clear();
                for (int i = 0; i < array.size(); i++) {
                    JsonElement unparsed = array.get(i);
                    Object data = ParserUtil.jsonToType(unparsed);
                    if (data instanceof String s) inList.add(s);
                }
            } else if (single instanceof Boolean) {
                byte[] inList = main.getByteArray(mainKey);
                for (int i = 0; i < array.size(); i++) {
                    JsonElement unparsed = array.get(i);
                    Object data = ParserUtil.jsonToType(unparsed);
                    if (data instanceof Boolean s)
                        inList[i] = (byte) (s ? 0x1b : 0x0b);
                }
            }
        }
    }

    static void processObject(NBTCompound main, JsonObject object, String mainKey) {
        if (main == null) return;
        main.addCompound(mainKey);
        object.keySet().forEach(key -> {
            JsonElement unparsed = object.get(key);
            if (unparsed.isJsonPrimitive()) {
                setValue(key, unparsed, main.getCompound(mainKey));
            } else if (unparsed.isJsonObject()) {
                processObject(main.getCompound(mainKey).getCompound(key), unparsed.getAsJsonObject(), key);
            } else if (unparsed.isJsonArray()) {
                processList(main.getCompound(mainKey), unparsed.getAsJsonArray(), key);
            }
        });
    }

    static NBTContainer processJson(JsonObject e) {
        Deque<JsonElement> elements = new ArrayDeque<>();
        final NBTContainer container = new NBTContainer();
        JsonElement element;
        elements.add(e);

        while ((element = elements.pollFirst()) != null) {
            if (element instanceof JsonObject compound) {
                compound.entrySet().forEach(entry -> {
                    String key = entry.getKey();
                    JsonElement value = entry.getValue();
                    if (value.isJsonPrimitive()) {
                        setValue(key, value, container);
                    } else if (value.isJsonObject()) {
                        processObject(container, (JsonObject) value, key);
                    } else if (value.isJsonArray()) {
                        processList(container, (JsonArray) value, key);
                    }
                });
            } else if (element instanceof JsonArray list) {
                for (int i = 0; i < list.size(); i++) {
                    JsonElement value = list.get(i);
                    if (value.isJsonObject()) {
                        processObject(container, (JsonObject) value, String.valueOf(i));
                    } else if (value.isJsonArray()) {
                        processList(container, (JsonArray) value, String.valueOf(i));
                    }
                }
            }
        }
        return container;
    }
    public static ItemStack parseFromJson(ItemStack i, JsonObject tags) {
        NBTItem item = new NBTItem(i);
        NBTContainer nbt = processJson(tags);
        item.mergeCompound(nbt);
        return item.getItem();
    }
}
