/**
 * This file is part of skJson.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright coffeeRequired nd contributors
 */
package cz.coffee.utils;


import com.google.gson.annotations.Since;
import cz.coffee.SkJson;

import java.util.logging.Logger;

import static cz.coffee.utils.ErrorHandler.Level.*;

@Since(2.0)
public class ErrorHandler {
    public static final String PARENT_DIRECTORY_NOT_EXIST = "Any of parent directories doesn't exist for your current input, try &f'with force'&e at the end.";
    public static final String PARENT_DIRECTORY_EXISTS = "This file path already exists. Try creating a file without the 'with force'";
    public static final String FILE_NOT_EXIST = "The inserted file not exists!";
    public static final String JSON_FILE_EXISTS = "Your inserted path to file already exists";
    public static final String JSON_SYNTAX_FILE = "Syntax in your JSON file isn't correct.";
    public static final String JSON_SYNTAX = "Syntax of your inserted json string isn't correct.";
    public static final String VAR_NEED_TO_BE_SINGLE = "You can change only Single variable contains a JSON not a List!";
    public static final String VAR_NEED_TO_BE_LIST = "You can map json only to List variable.";
    public static final String ONLY_JSONVAR_IS_ALLOWED = "You can change only json variables.";
    public static final String ID_GENERIC_NOT_FOUND = "The inserted id isn't exist in the cached Json map";
    public static final String NESTED_KEY_MISSING = "One of nested object doesn't exist in the inserted Json";
    public static final String WRONG_SPLITTER_PARSER = "didn't you mean to use `;` instead of `:` ?";
    static final SimpleUtil su = new SimpleUtil();

    /**
     * sendMessage with certain error level.
     *
     * @param input {@link Object} anything.toString()
     * @param level {@link Level}
     */
    public static void sendMessage(Object input, Level level) {
        Logger logger = SkJson.logger();
        if (level.equals(INFO)) {
            logger.info(su.color(INFO + "" + input));
        } else if (level.equals(ERROR)) {
            logger.severe(su.color(ERROR + "" + input));
        } else if (level.equals(WARNING)) {
            logger.warning(su.color(WARNING + "" + input));
        }
    }

    /**
     * enum class for Error Level
     */
    public enum Level {
        INFO(su.color("&bINFO&r ")),
        ERROR(su.color("&cERROR&r ")),
        WARNING(su.color("&eWARNING&r "));
        public final String label;

        Level(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return this.label;
        }
    }
}
