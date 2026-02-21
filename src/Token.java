//Token.java
public class Token {
    public TokenType type;
    public String lexeme;
    public String classification;

    public Token(TokenType type, String lexeme) {
        this.type = type;
        this.lexeme = lexeme;
        this.classification = type.getOutputName();
    }
}
