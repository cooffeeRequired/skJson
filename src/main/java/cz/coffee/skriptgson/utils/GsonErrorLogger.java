package cz.coffee.skriptgson.utils;


import cz.coffee.skriptgson.SkriptGson;

import java.util.logging.Logger;

import static cz.coffee.skriptgson.utils.GsonErrorLogger.ErrorLevel.*;
import static cz.coffee.skriptgson.utils.Utils.color;

public class GsonErrorLogger {

    public enum ErrorLevel {
        INFO(color("&bINFO&r ")),
        ERROR(color("&cERROR&r ")),
        WARNING("&eWARNING&r ");

        public final String label;

        private ErrorLevel(String label) {
            this.label = label;
        }
        @Override
        public String toString() {
            return this.label;
        }

    }


    public static void sendErrorMessage(String textOfError, ErrorLevel errorLevel)
    {
        Logger logger = SkriptGson.logger();
        if (errorLevel.equals(INFO)) {
            logger.info(color(INFO + "" + textOfError));
        } else if (errorLevel.equals(ERROR)) {
            logger.severe(color(ERROR + "" + textOfError));
        } else if (errorLevel.equals(WARNING)) {
            logger.warning(color(WARNING + "" + textOfError));
        }
    }

    public static final String ERROR_METHOD_IS_NOT_ALLOWED = "Sorry this method (&e a new json from request .... &r) is not allowed at this moment.";
    public static final String PARENT_DIRECTORY_NOT_EXIST = "Any of parent directories doesn't exist for your current input, try &f'with force'&e at the end.";
    public static final String FILE_NOT_EXIST = "Non exists file &f";
    public static final String JSON_SYNTAX_FILE = "Syntax in your JSON file isn't correct.";
    public static final String JSON_FILE_EXISTS = "Your inserted path to file already exists";
    public static final String JSON_SYNTAX = "Syntax of your inserted json string isn't correct.";
    public static final String PARENT_DIRECTORY_EXISTS = "This file path already exists. Try creating a file without the 'with force'";
    public static final String ID_GENERIC_NOT_FOUND = "The inserted id isn't exist in the cached Json map";
    public static final String VAR_NEED_TO_BE_SINGLE = "You can change only Single variable contains a JSON not a List!";
    public static final String VAR_NEED_TO_BE_LIST = "You can map json only to List variable.";
    public static final String ONLY_JSONVAR_IS_ALLOWED = "You can change only json variables.";


}
