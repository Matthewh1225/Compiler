import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

// Lexical analyzer for Java0 programming language.
// Uses a table-driven deterministic finite automaton to recognize tokens.
public class Lexer {
    private static final int INVALID_STATE = -1;
    private static final int STATE_START = 0;
    private static final int STATE_IDENTIFIER = 1;
    private static final int STATE_NUMBER = 2;
    private static final int STATE_COLON = 3;
    private static final int STATE_LESS_THAN = 4;
    private static final int STATE_GREATER_THAN = 5;
    private static final int STATE_EQUALS = 6;
    private static final int STATE_EXCLAMATION = 7;
    private static final int FINAL_STATE_PLUS = 8;
    private static final int FINAL_STATE_MINUS = 9;
    private static final int FINAL_STATE_STAR = 10;
    private static final int FINAL_STATE_SLASH = 11;
    private static final int FINAL_STATE_LEFT_PAREN = 12;
    private static final int FINAL_STATE_RIGHT_PAREN = 13;
    private static final int FINAL_STATE_COMMA = 14;
    private static final int FINAL_STATE_SEMICOLON = 15;
    private static final int FINAL_STATE_DOT = 16;
    private static final int FINAL_STATE_ASSIGN = 17;
    private static final int FINAL_STATE_EQUALS_EQUALS = 18;
    private static final int FINAL_STATE_LESS_EQUAL = 19;
    private static final int FINAL_STATE_NOT_EQUAL = 20;
    private static final int FINAL_STATE_GREATER_EQUAL = 21;
    private static final int FINAL_STATE_EOF = 22;
    private static final int STATE_COUNT = 23;
    //initialize the state transition table and token type mapping
    private static final int[][] NEXT_STATE_TABLE = buildTable();
    private static final TokenType[] TOKEN_TYPES = setupTokenTypes();

    private static final Map<String, TokenType> KEYWORDS = setupKeywords();

    private final CharacterReader sourceReader;

    public Lexer(String sourceText) {
        this.sourceReader = new CharacterReader(sourceText);
    }

    // Returns the next token
    // LL(1), keeps reading characters as long as they are a valid token.
    // When no valid transition exists, returns the longest valid token found.
    public Token nextToken() {
        StringBuilder lexeme = new StringBuilder();
        int state = STATE_START;
        int lastAccepting = INVALID_STATE;
        int acceptedLen = 0;

        while (true) {
            char ch = sourceReader.readNextCharacter();
            CharacterClass charClass = classifyCharacter(ch);

            int nextState = NEXT_STATE_TABLE[state][charClass.ordinal()];

            if (nextState == INVALID_STATE) {
                // Check if we need to put the char back
                // Dont retract if its EOF or if we had an error at the begining
                boolean errorAtStart = (state == STATE_START && lastAccepting == INVALID_STATE);
                if (charClass != CharacterClass.END_OF_FILE && !errorAtStart) {
                    sourceReader.retractOneCharacter(ch);
                }

                if (lastAccepting != INVALID_STATE) {
                    String tokenString = lexeme.substring(0, acceptedLen);
                    TokenType tokenType = TOKEN_TYPES[lastAccepting];
                    return checkKeyword(tokenType, tokenString);
                }

                if (charClass == CharacterClass.END_OF_FILE) {
                    return new Token(TokenType.EOF, "");
                }

                lexeme.setLength(0);
                state = STATE_START;
                lastAccepting = INVALID_STATE;
                acceptedLen = 0;
                continue;
            }
            state = nextState;
            if (state != STATE_START&& charClass != CharacterClass.END_OF_FILE) {
                lexeme.append(ch);
            }

            if (TOKEN_TYPES[state]!= null) {
                lastAccepting = state;
                acceptedLen = lexeme.length();
            }
        }
    }

    //Check if an tokern a reserved keyword
    private Token checkKeyword(TokenType type, String text) {
        if (type == TokenType.IDENT) {
            String lower =text.toLowerCase();
            TokenType keyword = KEYWORDS.get(lower);
            if (keyword != null) {
                return new Token(keyword, text);
            }
        }
        return new Token(type, text);
    }

    // clasify each character 
    private static CharacterClass classifyCharacter(char ch) {
        if (ch == CharacterReader.END_OF_FILE_CHAR) {
            return CharacterClass.END_OF_FILE;
        }
        if ((ch >= 'a'&& ch <='z') || (ch >='A' && ch <= 'Z')) {
            return CharacterClass.LETTER;
        }
        if (ch >= '0' &&ch <='9') {
            return CharacterClass.DIGIT;
        }
        if (ch == ' ' ||ch == '\t'|| ch == '\n' || ch == '\r') {
            return CharacterClass.WHITESPACE;
        }

        switch (ch) {
            case '+':
                return CharacterClass.PLUS;
            case '-':
                return CharacterClass.MINUS;
            case '*':
                return CharacterClass.STAR;
            case '/':
                return CharacterClass.SLASH;
            case ':':
                return CharacterClass.COLON;
            case '=':
                return CharacterClass.EQUALS;
            case '!':
                return CharacterClass.EXCLAMATION;
            case '<':
                return CharacterClass.LESS_THAN;
            case '>':
                return CharacterClass.GREATER_THAN;
            case '(':
                return CharacterClass.LEFT_PAREN;
            case ')':
                return CharacterClass.RIGHT_PAREN;
            case ',':
                return CharacterClass.COMMA;
            case ';':
                return CharacterClass.SEMICOLON;
            case '.':
                return CharacterClass.DOT;
            default:
                return CharacterClass.OTHER;
        }
    }

    // Build the transition table for the finite state machine
    private static int[][] buildTable() {
        int[][] table = new int[STATE_COUNT][CharacterClass.values().length];
        for (int state = 0;state < STATE_COUNT;state++) 
         {
            Arrays.fill(table[state], INVALID_STATE);
        }

        // Define all valid state transitions
        table[STATE_START][CharacterClass.WHITESPACE.ordinal()]= STATE_START;
        table[STATE_START][CharacterClass.LETTER.ordinal()] = STATE_IDENTIFIER;
        table[STATE_START][CharacterClass.DIGIT.ordinal()]= STATE_NUMBER;
        table[STATE_START][CharacterClass.PLUS.ordinal()] =FINAL_STATE_PLUS;
        table[STATE_START][CharacterClass.MINUS.ordinal()] = FINAL_STATE_MINUS;
        table[STATE_START][CharacterClass.STAR.ordinal()]= FINAL_STATE_STAR;
        table[STATE_START][CharacterClass.SLASH.ordinal()] = FINAL_STATE_SLASH;
        table[STATE_START][CharacterClass.LEFT_PAREN.ordinal()] = FINAL_STATE_LEFT_PAREN;
        table[STATE_START][CharacterClass.RIGHT_PAREN.ordinal()] = FINAL_STATE_RIGHT_PAREN;
        table[STATE_START][CharacterClass.COMMA.ordinal()]= FINAL_STATE_COMMA;
        table[STATE_START][CharacterClass.SEMICOLON.ordinal()] = FINAL_STATE_SEMICOLON;
        table[STATE_START][CharacterClass.DOT.ordinal()] = FINAL_STATE_DOT;
        table[STATE_START][CharacterClass.COLON.ordinal()] = STATE_COLON;
        table[STATE_START][CharacterClass.EQUALS.ordinal()] =STATE_EQUALS;
        table[STATE_START][CharacterClass.EXCLAMATION.ordinal()] = STATE_EXCLAMATION;
        table[STATE_START][CharacterClass.LESS_THAN.ordinal()] =STATE_LESS_THAN;
        table[STATE_START][CharacterClass.GREATER_THAN.ordinal()] = STATE_GREATER_THAN;
        table[STATE_START][CharacterClass.END_OF_FILE.ordinal()] =FINAL_STATE_EOF;
        table[STATE_IDENTIFIER][CharacterClass.LETTER.ordinal()] = STATE_IDENTIFIER;
        table[STATE_IDENTIFIER][CharacterClass.DIGIT.ordinal()] =STATE_IDENTIFIER;
        table[STATE_NUMBER][CharacterClass.DIGIT.ordinal()] = STATE_NUMBER;
        table[STATE_COLON][CharacterClass.EQUALS.ordinal()]=FINAL_STATE_ASSIGN;
        table[STATE_EQUALS][CharacterClass.EQUALS.ordinal()] = FINAL_STATE_EQUALS_EQUALS;
        table[STATE_EXCLAMATION][CharacterClass.EQUALS.ordinal()] =FINAL_STATE_NOT_EQUAL;
        table[STATE_LESS_THAN][CharacterClass.EQUALS.ordinal()] = FINAL_STATE_LESS_EQUAL;
        table[STATE_LESS_THAN][CharacterClass.GREATER_THAN.ordinal()] =FINAL_STATE_NOT_EQUAL;
        table[STATE_GREATER_THAN][CharacterClass.EQUALS.ordinal()] = FINAL_STATE_GREATER_EQUAL;

        return table;
    }

    // Map each state to token type
    private static TokenType[] setupTokenTypes() 
    {
        TokenType[] types = new TokenType[STATE_COUNT];

        types[STATE_IDENTIFIER] =TokenType.IDENT;
        types[STATE_NUMBER] = TokenType.NUMBER;
        types[STATE_EQUALS]= TokenType.EQUALS;
        types[STATE_LESS_THAN] =TokenType.LESS_THAN;
        types[STATE_GREATER_THAN] = TokenType.GREATER_THAN;
        types[FINAL_STATE_PLUS]= TokenType.PLUS;
        types[FINAL_STATE_MINUS] =TokenType.MINUS;
        types[FINAL_STATE_STAR]= TokenType.STAR;
        types[FINAL_STATE_SLASH]= TokenType.SLASH;
        types[FINAL_STATE_LEFT_PAREN] = TokenType.LEFT_PAREN;
        types[FINAL_STATE_RIGHT_PAREN] = TokenType.RIGHT_PAREN;
        types[FINAL_STATE_COMMA] =TokenType.COMMA;
        types[FINAL_STATE_SEMICOLON] = TokenType.SEMICOLON;
        types[FINAL_STATE_DOT] = TokenType.DOT;
        types[FINAL_STATE_ASSIGN] =TokenType.ASSIGN;
        types[FINAL_STATE_EQUALS_EQUALS] = TokenType.EQUALS_EQUALS;
        types[FINAL_STATE_LESS_EQUAL]=TokenType.LESS_EQUAL;
        types[FINAL_STATE_NOT_EQUAL]= TokenType.NOT_EQUAL;
        types[FINAL_STATE_GREATER_EQUAL]= TokenType.GREATER_EQUAL;
        types[FINAL_STATE_EOF] = TokenType.EOF;

        return types;
    }

    // Map reserved words to their token types
    private static Map<String, TokenType> setupKeywords() 
    {
        Map<String, TokenType> keywords = new HashMap<>();
        keywords.put("const", TokenType.CONST);
        keywords.put("var",TokenType.VAR);
        keywords.put("procedure", TokenType.PROCEDURE);
        keywords.put("call",TokenType.CALL);
        keywords.put("begin",TokenType.BEGIN);
        keywords.put("end", TokenType.END);
        keywords.put("if",TokenType.IF);
        keywords.put("then", TokenType.THEN);
        keywords.put("while", TokenType.WHILE);
        keywords.put("do", TokenType.DO);
        keywords.put("odd", TokenType.ODD);
        return keywords;
    }

    private static String loadFile(String file) throws IOException {
        return Files.readString(Path.of(file));
    }

    public static void main(String[] args) throws IOException {
        String programString =loadFile("Program_Text.txt");
        Lexer lexer = new Lexer(programString);
        FileWriter writer = new FileWriter("tokens.txt");
        while (true)
            {
            Token token = lexer.nextToken();
            writer.write(token.tokenType +"|"+ token.tokenString + "\n");
            if (token.tokenType ==TokenType.EOF) {
                System.out.println("Completed,Tokens written to tokens.txt");
                break;
            }
        }
        writer.close();
    }
}