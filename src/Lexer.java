import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Lexer {
    private static final int INVALID_STATE = -1;
    private static final int STATE_START = 0;
    private static final int STATE_IDENTIFIER = 1;
    private static final int STATE_NUMBER = 2;
    private static final int STATE_COLON = 3;
    private static final int STATE_LESS_THAN = 4;
    private static final int STATE_GREATER_THAN = 5;
    private static final int STATE_EQUALS = 6;
    private static final int FINAL_STATE_PLUS = 7;
    private static final int FINAL_STATE_MINUS = 8;
    private static final int FINAL_STATE_STAR = 9;
    private static final int FINAL_STATE_SLASH = 10;
    private static final int FINAL_STATE_LEFT_PAREN = 11;
    private static final int FINAL_STATE_RIGHT_PAREN = 12;
    private static final int FINAL_STATE_COMMA = 13;
    private static final int FINAL_STATE_SEMICOLON = 14;
    private static final int FINAL_STATE_DOT = 15;
    private static final int FINAL_STATE_ASSIGN = 16;
    private static final int FINAL_STATE_EQUAL = 17;
    private static final int FINAL_STATE_EQUALS_EQUALS = 18;
    private static final int FINAL_STATE_LESS_EQUAL = 19;
    private static final int FINAL_STATE_NOT_EQUAL = 20;
    private static final int FINAL_STATE_GREATER_EQUAL = 21;
    private static final int FINAL_STATE_EOF = 22;
    private static final int STATE_EXCLAMATION = 23;
    private static final int STATE_COUNT = 24;
    private static final int[][] NEXT_STATE_TABLE = createNextStateTable();
    private static final TokenType[] ACCEPTING_TOKEN_TYPE_FOR_STATE = createAcceptingTokenTypes();
    private static final boolean[] IS_IMMEDIATE_FINAL_STATE = setFinalStates();

    private static final Map<String, TokenType> RESERVED_WORDS_LOOKUP = createReservedWordsLookup();

    private final CharacterReader sourceReader;

    public Lexer(String sourceText) {
        this.sourceReader = new CharacterReader(sourceText);
    }


    public Token nextToken() {
        String currentTokenString = "";
        int currentState = STATE_START;
        int lastAcceptingState = INVALID_STATE;
        int acceptedTokenLength = 0;

        while (true) {
            char currentCharacter = sourceReader.readNextCharacter();
            CharacterClass currentCharacterClass = classifyCharacter(currentCharacter);
            int nextState = NEXT_STATE_TABLE[currentState][currentCharacterClass.ordinal()];

            if (nextState == INVALID_STATE) {
                if (currentCharacterClass != CharacterClass.END_OF_FILE && !(currentState == STATE_START && lastAcceptingState == INVALID_STATE)) {
                    sourceReader.retractOneCharacter(currentCharacter);
                }

                if (lastAcceptingState != INVALID_STATE) {
                    String TokenString = currentTokenString.substring(0, acceptedTokenLength);
                    TokenType tokenType = ACCEPTING_TOKEN_TYPE_FOR_STATE[lastAcceptingState];
                    return createTokenWithKeywordCheck(tokenType, TokenString);
                }

                if (currentCharacterClass == CharacterClass.END_OF_FILE) {
                    return new Token(TokenType.EOF, "");
                }

                currentTokenString = "";
                currentState = STATE_START;
                lastAcceptingState = INVALID_STATE;
                acceptedTokenLength = 0;
                continue;
            }

            currentState = nextState;
            if (currentState != STATE_START && currentCharacterClass != CharacterClass.END_OF_FILE) {
                currentTokenString += currentCharacter;
            }

            if (ACCEPTING_TOKEN_TYPE_FOR_STATE[currentState] != null) {
                lastAcceptingState = currentState;
                acceptedTokenLength = currentTokenString.length();
            }

            if (IS_IMMEDIATE_FINAL_STATE[currentState]) {
                TokenType tokenType = ACCEPTING_TOKEN_TYPE_FOR_STATE[currentState];
                String TokenString = currentTokenString;
                return createTokenWithKeywordCheck(tokenType, TokenString);
            }
        }
    }

    private Token createTokenWithKeywordCheck(TokenType tokenType, String tokenString) {
        if (tokenType == TokenType.IDENT) {
            String lowercaseTokenString = tokenString.toLowerCase(Locale.ROOT);
            TokenType keywordType = RESERVED_WORDS_LOOKUP.get(lowercaseTokenString);
            if (keywordType != null) {
                return new Token(keywordType, tokenString);
            }
        }
        return new Token(tokenType, tokenString);
    }

    private static CharacterClass classifyCharacter(char character) {
        if (character == CharacterReader.END_OF_FILE_CHAR) {
            return CharacterClass.END_OF_FILE;
        }
        if ((character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z')) {
            return CharacterClass.LETTER;
        }
        if (character >= '0' && character <= '9') {
            return CharacterClass.DIGIT;
        }
        if (character == ' ' || character == '\t' || character == '\n' || character == '\r') {
            return CharacterClass.WHITESPACE;
        }

        switch (character) {
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

    private static int[][] createNextStateTable() {
        int[][] table = new int[STATE_COUNT][CharacterClass.values().length];
        for (int state = 0; state < STATE_COUNT; state++) {
            Arrays.fill(table[state], INVALID_STATE);
        }

        setTransition(table, STATE_START, CharacterClass.WHITESPACE, STATE_START);
        setTransition(table, STATE_START, CharacterClass.LETTER, STATE_IDENTIFIER);
        setTransition(table, STATE_START, CharacterClass.DIGIT, STATE_NUMBER);
        setTransition(table, STATE_START, CharacterClass.PLUS, FINAL_STATE_PLUS);
        setTransition(table, STATE_START, CharacterClass.MINUS, FINAL_STATE_MINUS);
        setTransition(table, STATE_START, CharacterClass.STAR, FINAL_STATE_STAR);
        setTransition(table, STATE_START, CharacterClass.SLASH, FINAL_STATE_SLASH);
        setTransition(table, STATE_START, CharacterClass.LEFT_PAREN, FINAL_STATE_LEFT_PAREN);
        setTransition(table, STATE_START, CharacterClass.RIGHT_PAREN, FINAL_STATE_RIGHT_PAREN);
        setTransition(table, STATE_START, CharacterClass.COMMA, FINAL_STATE_COMMA);
        setTransition(table, STATE_START, CharacterClass.SEMICOLON, FINAL_STATE_SEMICOLON);
        setTransition(table, STATE_START, CharacterClass.DOT, FINAL_STATE_DOT);
        setTransition(table, STATE_START, CharacterClass.COLON, STATE_COLON);
        setTransition(table, STATE_START, CharacterClass.EQUALS, STATE_EQUALS);
        setTransition(table, STATE_START, CharacterClass.EXCLAMATION, STATE_EXCLAMATION);
        setTransition(table, STATE_START, CharacterClass.LESS_THAN, STATE_LESS_THAN);
        setTransition(table, STATE_START, CharacterClass.GREATER_THAN, STATE_GREATER_THAN);
        setTransition(table, STATE_START, CharacterClass.END_OF_FILE, FINAL_STATE_EOF);
        setTransition(table, STATE_IDENTIFIER, CharacterClass.LETTER, STATE_IDENTIFIER);
        setTransition(table, STATE_IDENTIFIER, CharacterClass.DIGIT, STATE_IDENTIFIER);
        setTransition(table, STATE_NUMBER, CharacterClass.DIGIT, STATE_NUMBER);
        setTransition(table, STATE_COLON, CharacterClass.EQUALS, FINAL_STATE_ASSIGN);
        setTransition(table, STATE_EQUALS, CharacterClass.EQUALS, FINAL_STATE_EQUALS_EQUALS);
        setTransition(table, STATE_EXCLAMATION, CharacterClass.EQUALS, FINAL_STATE_NOT_EQUAL);
        setTransition(table, STATE_LESS_THAN, CharacterClass.EQUALS, FINAL_STATE_LESS_EQUAL);
        setTransition(table, STATE_LESS_THAN, CharacterClass.GREATER_THAN, FINAL_STATE_NOT_EQUAL);
        setTransition(table, STATE_GREATER_THAN, CharacterClass.EQUALS, FINAL_STATE_GREATER_EQUAL);

        return table;
    }

    private static void setTransition(int[][] table, int fromState, CharacterClass characterClass, int toState) {
        table[fromState][characterClass.ordinal()] = toState;
    }

    private static TokenType[] createAcceptingTokenTypes() {
        TokenType[] types = new TokenType[STATE_COUNT];

        types[STATE_IDENTIFIER] = TokenType.IDENT;
        types[STATE_NUMBER] = TokenType.NUMBER;
        types[STATE_EQUALS] = TokenType.EQUALS;
        types[STATE_LESS_THAN] = TokenType.LESS_THAN;
        types[STATE_GREATER_THAN] = TokenType.GREATER_THAN;
        types[FINAL_STATE_PLUS] = TokenType.PLUS;
        types[FINAL_STATE_MINUS] = TokenType.MINUS;
        types[FINAL_STATE_STAR] = TokenType.STAR;
        types[FINAL_STATE_SLASH] = TokenType.SLASH;
        types[FINAL_STATE_LEFT_PAREN] = TokenType.LEFT_PAREN;
        types[FINAL_STATE_RIGHT_PAREN] = TokenType.RIGHT_PAREN;
        types[FINAL_STATE_COMMA] = TokenType.COMMA;
        types[FINAL_STATE_SEMICOLON] = TokenType.SEMICOLON;
        types[FINAL_STATE_DOT] = TokenType.DOT;
        types[FINAL_STATE_ASSIGN] = TokenType.ASSIGN;
        types[FINAL_STATE_EQUAL] = TokenType.EQUALS;
        types[FINAL_STATE_EQUALS_EQUALS] = TokenType.EQUALS_EQUALS;
        types[FINAL_STATE_LESS_EQUAL] = TokenType.LESS_EQUAL;
        types[FINAL_STATE_NOT_EQUAL] = TokenType.NOT_EQUAL;
        types[FINAL_STATE_GREATER_EQUAL] = TokenType.GREATER_EQUAL;
        types[FINAL_STATE_EOF] = TokenType.EOF;

        return types;
    }

    private static boolean[] setFinalStates() {
        boolean[] states = new boolean[STATE_COUNT];

        states[FINAL_STATE_PLUS] = true;
        states[FINAL_STATE_MINUS] = true;
        states[FINAL_STATE_STAR] = true;
        states[FINAL_STATE_SLASH] = true;
        states[FINAL_STATE_LEFT_PAREN] = true;
        states[FINAL_STATE_RIGHT_PAREN] = true;
        states[FINAL_STATE_COMMA] = true;
        states[FINAL_STATE_SEMICOLON] = true;
        states[FINAL_STATE_DOT] = true;
        states[FINAL_STATE_ASSIGN] = true;
        states[FINAL_STATE_EQUAL] = true;
        states[FINAL_STATE_EQUALS_EQUALS] = true;
        states[FINAL_STATE_LESS_EQUAL] = true;
        states[FINAL_STATE_NOT_EQUAL] = true;
        states[FINAL_STATE_GREATER_EQUAL] = true;
        states[FINAL_STATE_EOF] = true;

        return states;
    }

    private static Map<String, TokenType> createReservedWordsLookup() {
        Map<String, TokenType> lookup = new HashMap<>();
        lookup.put("const", TokenType.CONST);
        lookup.put("var", TokenType.VAR);
        lookup.put("procedure", TokenType.PROCEDURE);
        lookup.put("call", TokenType.CALL);
        lookup.put("begin", TokenType.BEGIN);
        lookup.put("end", TokenType.END);
        lookup.put("if", TokenType.IF);
        lookup.put("then", TokenType.THEN);
        lookup.put("while", TokenType.WHILE);
        lookup.put("do", TokenType.DO);
        lookup.put("odd", TokenType.ODD);
        return lookup;
    }

    private static String readSourceProgram(String fileName) throws IOException {
        return Files.readString(Path.of(fileName));
    }

    public static void main(String[] args) throws IOException {
        String sourceText = readSourceProgram("Program_Text.txt");
        Lexer lexer = new Lexer(sourceText);
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("tokens.txt")))) {
            while (true) {
                Token token = lexer.nextToken();
                writer.println(token.tokenType + "|" + token.tokenString);
                if (token.tokenType == TokenType.EOF) {
                    System.out.println("Complete, Tokens written to tokens.txt");
                    break;
                }
            }
        }
    }
}

            int nextState = NEXT_STATE_TABLE[currentState][currentCharacterClass.ordinal()];

            if (nextState == INVALID_STATE) {
                if (currentCharacterClass != CharacterClass.END_OF_FILE && !(currentState == STATE_START && lastAcceptingState == INVALID_STATE)) {
                    sourceReader.retractOneCharacter(currentCharacter);
                }

                if (lastAcceptingState != INVALID_STATE) {
                    String TokenString = currentTokenString.substring(0, acceptedTokenLength);
                    TokenType tokenType = ACCEPTING_TOKEN_TYPE_FOR_STATE[lastAcceptingState];
                    return createTokenWithKeywordCheck(tokenType, TokenString);
                }

                if (currentCharacterClass == CharacterClass.END_OF_FILE) {
                    return new Token(TokenType.EOF, "");
                }

                currentTokenString = "";
                currentState = STATE_START;
                lastAcceptingState = INVALID_STATE;
                acceptedTokenLength = 0;
                continue;
            }

            currentState = nextState;
            if (currentState != STATE_START && currentCharacterClass != CharacterClass.END_OF_FILE) {
                currentTokenString += currentCharacter;
            }

            if (ACCEPTING_TOKEN_TYPE_FOR_STATE[currentState] != null) {
                lastAcceptingState = currentState;
                acceptedTokenLength = currentTokenString.length();
            }

            if (IS_IMMEDIATE_FINAL_STATE[currentState]) {
                TokenType tokenType = ACCEPTING_TOKEN_TYPE_FOR_STATE[currentState];
                String TokenString = currentTokenString;
                return createTokenWithKeywordCheck(tokenType, TokenString);
            }
        }
    }

    private Token createTokenWithKeywordCheck(TokenType tokenType, String tokenString) {
        if (tokenType == TokenType.IDENT) {
            String lowercaseTokenString = tokenString.toLowerCase(Locale.ROOT);
            TokenType keywordType = RESERVED_WORDS_LOOKUP.get(lowercaseTokenString);
            if (keywordType != null) {
                return new Token(keywordType, tokenString);
            }
        }
        return new Token(tokenType, tokenString);
    }

    private static CharacterClass classifyCharacter(char character) {
        if (character == CharacterReader.END_OF_FILE_CHAR) {
            return CharacterClass.END_OF_FILE;
        }
        if ((character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z')) {
            return CharacterClass.LETTER;
        }
        if (character >= '0' && character <= '9') {
            return CharacterClass.DIGIT;
        }
        if (character == ' ' || character == '\t' || character == '\n' || character == '\r') {
            return CharacterClass.WHITESPACE;
        }

        switch (character) {
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

    private static int[][] createNextStateTable() {
        int[][] table = new int[STATE_COUNT][CharacterClass.values().length];
        for (int state = 0; state < STATE_COUNT; state++) {
            Arrays.fill(table[state], INVALID_STATE);
        }

        setTransition(table, STATE_START, CharacterClass.WHITESPACE, STATE_START);
        setTransition(table, STATE_START, CharacterClass.LETTER, STATE_IDENTIFIER);
        setTransition(table, STATE_START, CharacterClass.DIGIT, STATE_NUMBER);
        setTransition(table, STATE_START, CharacterClass.PLUS, FINAL_STATE_PLUS);
        setTransition(table, STATE_START, CharacterClass.MINUS, FINAL_STATE_MINUS);
        setTransition(table, STATE_START, CharacterClass.STAR, FINAL_STATE_STAR);
        setTransition(table, STATE_START, CharacterClass.SLASH, FINAL_STATE_SLASH);
        setTransition(table, STATE_START, CharacterClass.LEFT_PAREN, FINAL_STATE_LEFT_PAREN);
        setTransition(table, STATE_START, CharacterClass.RIGHT_PAREN, FINAL_STATE_RIGHT_PAREN);
        setTransition(table, STATE_START, CharacterClass.COMMA, FINAL_STATE_COMMA);
        setTransition(table, STATE_START, CharacterClass.SEMICOLON, FINAL_STATE_SEMICOLON);
        setTransition(table, STATE_START, CharacterClass.DOT, FINAL_STATE_DOT);
        setTransition(table, STATE_START, CharacterClass.COLON, STATE_COLON);
        setTransition(table, STATE_START, CharacterClass.EQUALS, STATE_EQUALS);
        setTransition(table, STATE_START, CharacterClass.EXCLAMATION, STATE_EXCLAMATION);
        setTransition(table, STATE_START, CharacterClass.LESS_THAN, STATE_LESS_THAN);
        setTransition(table, STATE_START, CharacterClass.GREATER_THAN, STATE_GREATER_THAN);
        setTransition(table, STATE_START, CharacterClass.END_OF_FILE, FINAL_STATE_EOF);
        setTransition(table, STATE_IDENTIFIER, CharacterClass.LETTER, STATE_IDENTIFIER);
        setTransition(table, STATE_IDENTIFIER, CharacterClass.DIGIT, STATE_IDENTIFIER);
        setTransition(table, STATE_NUMBER, CharacterClass.DIGIT, STATE_NUMBER);
        setTransition(table, STATE_COLON, CharacterClass.EQUALS, FINAL_STATE_ASSIGN);
        setTransition(table, STATE_EQUALS, CharacterClass.EQUALS, FINAL_STATE_EQUALS_EQUALS);
        setTransition(table, STATE_EXCLAMATION, CharacterClass.EQUALS, FINAL_STATE_NOT_EQUAL);
        setTransition(table, STATE_LESS_THAN, CharacterClass.EQUALS, FINAL_STATE_LESS_EQUAL);
        setTransition(table, STATE_LESS_THAN, CharacterClass.GREATER_THAN, FINAL_STATE_NOT_EQUAL);
        setTransition(table, STATE_GREATER_THAN, CharacterClass.EQUALS, FINAL_STATE_GREATER_EQUAL);

        return table;
    }

    private static void setTransition(int[][] table, int fromState, CharacterClass characterClass, int toState) {
        table[fromState][characterClass.ordinal()] = toState;
    }

    private static TokenType[] createAcceptingTokenTypes() {
        TokenType[] types = new TokenType[STATE_COUNT];

        types[STATE_IDENTIFIER] = TokenType.IDENT;
        types[STATE_NUMBER] = TokenType.NUMBER;
        types[STATE_EQUALS] = TokenType.EQUALS;
        types[STATE_LESS_THAN] = TokenType.LESS_THAN;
        types[STATE_GREATER_THAN] = TokenType.GREATER_THAN;
        types[FINAL_STATE_PLUS] = TokenType.PLUS;
        types[FINAL_STATE_MINUS] = TokenType.MINUS;
        types[FINAL_STATE_STAR] = TokenType.STAR;
        types[FINAL_STATE_SLASH] = TokenType.SLASH;
        types[FINAL_STATE_LEFT_PAREN] = TokenType.LEFT_PAREN;
        types[FINAL_STATE_RIGHT_PAREN] = TokenType.RIGHT_PAREN;
        types[FINAL_STATE_COMMA] = TokenType.COMMA;
        types[FINAL_STATE_SEMICOLON] = TokenType.SEMICOLON;
        types[FINAL_STATE_DOT] = TokenType.DOT;
        types[FINAL_STATE_ASSIGN] = TokenType.ASSIGN;
        types[FINAL_STATE_EQUAL] = TokenType.EQUALS;
        types[FINAL_STATE_EQUALS_EQUALS] = TokenType.EQUALS_EQUALS;
        types[FINAL_STATE_LESS_EQUAL] = TokenType.LESS_EQUAL;
        types[FINAL_STATE_NOT_EQUAL] = TokenType.NOT_EQUAL;
        types[FINAL_STATE_GREATER_EQUAL] = TokenType.GREATER_EQUAL;
        types[FINAL_STATE_EOF] = TokenType.EOF;

        return types;
    }

    private static boolean[] setFinalStates() {
        boolean[] states = new boolean[STATE_COUNT];

        states[FINAL_STATE_PLUS] = true;
        states[FINAL_STATE_MINUS] = true;
        states[FINAL_STATE_STAR] = true;
        states[FINAL_STATE_SLASH] = true;
        states[FINAL_STATE_LEFT_PAREN] = true;
        states[FINAL_STATE_RIGHT_PAREN] = true;
        states[FINAL_STATE_COMMA] = true;
        states[FINAL_STATE_SEMICOLON] = true;
        states[FINAL_STATE_DOT] = true;
        states[FINAL_STATE_ASSIGN] = true;
        states[FINAL_STATE_EQUAL] = true;
        states[FINAL_STATE_EQUALS_EQUALS] = true;
        states[FINAL_STATE_LESS_EQUAL] = true;
        states[FINAL_STATE_NOT_EQUAL] = true;
        states[FINAL_STATE_GREATER_EQUAL] = true;
        states[FINAL_STATE_EOF] = true;

        return states;
    }

    private static Map<String, TokenType> createReservedWordsLookup() {
        Map<String, TokenType> lookup = new HashMap<>();
        lookup.put("const", TokenType.CONST);
        lookup.put("var", TokenType.VAR);
        lookup.put("procedure", TokenType.PROCEDURE);
        lookup.put("call", TokenType.CALL);
        lookup.put("begin", TokenType.BEGIN);
        lookup.put("end", TokenType.END);
        lookup.put("if", TokenType.IF);
        lookup.put("then", TokenType.THEN);
        lookup.put("while", TokenType.WHILE);
        lookup.put("do", TokenType.DO);
        lookup.put("odd", TokenType.ODD);
        return lookup;
    }

    private static String readSourceProgram(String fileName) throws IOException {
        return Files.readString(Path.of(fileName));
    }

    public static void main(String[] args) throws IOException {
        String sourceText = readSourceProgram("Program_Text.txt");
        Lexer lexer = new Lexer(sourceText);
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("tokens.txt")))) {
            while (true) {
                Token token = lexer.nextToken();
                writer.println(token.tokenType + "|" + token.tokenString);
                if (token.tokenType == TokenType.EOF) {
                    System.out.println("Complete, Tokens written to tokens.txt");
                    break;
                }
            }
        }
    }
}
