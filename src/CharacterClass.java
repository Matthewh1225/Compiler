//CharacterClass.java
public enum CharacterClass {
    LETTER,
    DIGIT,
    WHITESPACE,
    PLUS,
    MINUS,
    STAR,
    SLASH,
    EQUALS,
    EXCLAMATION,
    LESS_THAN,
    GREATER_THAN,
    LEFT_PAREN,
    RIGHT_PAREN,
    LEFT_BRACE,
    RIGHT_BRACE,
    COMMA,
    SEMICOLON,
    DOT,
    EOF,
    OTHER;
    // Classifies a char int CharacterClass
    public static CharacterClass classifyCharacter(char ch) {
        if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) return LETTER;
        if (ch >= '0' && ch <= '9') return DIGIT;
        if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') return WHITESPACE;
        switch (ch) {
            case '+':
                return PLUS;
            case '-':
                return MINUS;
            case '*':
                return STAR;
            case '/':
                return SLASH;
            case '=':
                return EQUALS;
            case '!':
                return EXCLAMATION;
            case '<':
                return LESS_THAN;
            case '>':
                return GREATER_THAN;
            case '(':
                return LEFT_PAREN;
            case ')':
                return RIGHT_PAREN;
            case '{':
                return LEFT_BRACE;
            case '}':
                return RIGHT_BRACE;
            case ',':
                return COMMA;
            case ';':
                return SEMICOLON;
            case '.':
                return DOT;
            default:
                return OTHER;
        }
    }
}
