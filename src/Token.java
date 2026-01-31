public final class Token {
    public final TokenType tokenType;
    public final String tokenString;

    public Token(TokenType tokenType, String tokenString) {
        this.tokenType = tokenType;
        this.tokenString = tokenString;
    }
}
