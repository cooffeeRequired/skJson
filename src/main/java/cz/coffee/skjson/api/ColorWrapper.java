package cz.coffee.skjson.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

/**
 * The type Color wrapper.
 */
public class ColorWrapper {
    /**
     * The Converter.
     */
    static LegacyComponentSerializer converter = LegacyComponentSerializer.builder().useUnusualXRepeatedCharacterHexFormat().character('&').hexColors().build();

    /**
     * Translate component.
     *
     * @param message the message
     * @return the component
     */
    public static Component translate(String message) {
        return converter.deserialize(message);
    }

    /**
     * Translate legacy string.
     *
     * @param message the message
     * @return the string
     */
    public static String translateLegacy(Object message) {
        return ChatColor.translateAlternateColorCodes('&', message.toString());
    }

    /**
     * The enum Colors.
     */
    public enum Colors {
        /**
         * Dark blue colors.
         */
        DARK_BLUE(translateLegacy("&1")),
        /**
         * Dark green colors.
         */
        DARK_GREEN(translateLegacy("&2")),
        /**
         * Dark aqua colors.
         */
        DARK_AQUA(translateLegacy("&3")),
        /**
         * Dark red colors.
         */
        DARK_RED(translateLegacy("&4")),
        /**
         * Dark purple colors.
         */
        DARK_PURPLE(translateLegacy("&5")),
        /**
         * Gold colors.
         */
        GOLD(translateLegacy("&6")),
        /**
         * Gray colors.
         */
        GRAY(translateLegacy("&7")),
        /**
         * Dark gray colors.
         */
        DARK_GRAY(translateLegacy("&8")),
        /**
         * Blue colors.
         */
        BLUE(translateLegacy("&9")),
        /**
         * Green colors.
         */
        GREEN(translateLegacy("&a")),
        /**
         * Aqua colors.
         */
        AQUA(translateLegacy("&b")),
        /**
         * Red colors.
         */
        RED(translateLegacy("&c")),
        /**
         * Purple colors.
         */
        PURPLE(translateLegacy("&d")),
        /**
         * Yellow colors.
         */
        YELLOW(translateLegacy("&e")),
        /**
         * White colors.
         */
        WHITE(translateLegacy("&f"));
        /**
         * The Legacy color.
         */
        public final String legacyColor;

        Colors(String st) {
            legacyColor = st;
        }
    }

}
