//TokenType.java
public enum TokenType {
    IDENT("var"),
    NUMBER("Numeric literal"),

    CLASS("$class"),
    CONST("$Const"),
    VAR("$VARDeclaration"),
    PROCEDURE("$procedure"),
    CALL("$call"),
    IF("$if"),
    THEN("$then"),
    WHILE("$while"),
    DO("$do"),
    ODD("$odd"),

    ADD_OP("$addop"),
    SUB_OP("$addop"),
    MULT_OP("$mop"),
    DIV_OP("$mop"),

    LEFT_PAREN("$LP"),
    RIGHT_PAREN("$RP"),
    LEFT_BRACE("$LB"),
    RIGHT_BRACE("$RB"),
    COMMA("$comma"),
    SEMICOLON("$semi"),
    DOT("$dot"),

    ASSIGN_OP("$="),
    EQUALS_OP("$relop"),
    LESS_OP("$relop"),
    LESS_EQUAL_OP("$relop"),
    NOT_EQUAL_OP("$relop"),
    GREATER_OP("$relop"),
    GREATER_EQUAL_OP("$relop"),

    EOF("$eof");

    String outputname;
    TokenType(String outputname) {
        this.outputname = outputname;
    }
// Returns the string for the token type, used in output files
    public String getOutputName() {
        return outputname;
    }
}
