package cz.coffee.skriptgson.utils;

public class GsonErrorLogger {
    public final String ERROR_METHOD_IS_NOT_ALLOWED = "Sorry this method (&e a new json from request .... &r) is not allowed at this moment.";
    public final String PARENT_DIRECTORY_NOT_EXIST = "Any of parent directories doesn't exist for your current input, try &f'with force'&e at the end.";
    public final String FILE_NOT_EXIST = "Non exists file &f";
    public final String JSON_SYNTAX_FILE = "Syntax in your JSON file isn't correct.";
    public final String JSON_FILE_EXISTS = "Your inserted path to file already exists";
    public final String JSON_SYNTAX = "Syntax of your inserted json string isn't correct.";
    public final String PARENT_DIRECTORY_EXISTS = "This file path already exists. Try creating a file without the 'with force'";
    public final String ID_GENERIC_NOT_FOUND = "The inserted id isn't exist in the cached Json map";
    public final String VAR_NEED_TO_BE_SINGLE = "You can change only Single variable contains a JSON not a List!";
    public final String VAR_NEED_TO_BE_LIST = "You can map json only to List variable.";
    public final String ONLY_VAR_IS_ALLOWED = "You can change only json variables.";


}
