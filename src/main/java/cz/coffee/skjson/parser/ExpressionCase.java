package cz.coffee.skjson.parser;

public enum ExpressionCase {
    STRING("string"),
    NUMBER("number"),
    BOOLEAN("boolean"),
    FUNCTION("function"),
    VARIABLE("variable"),
    EXPRESSION_CASE("expression_case"),
    EXPRESSION("expression"),
    UNKNOWN("unknown");

    ExpressionCase(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    final String value;
}
