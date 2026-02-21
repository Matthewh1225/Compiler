//Scanner.java
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Scanner {
    static final int INVALID = -1;
    static final int STATE_START = 0;
    static final int STATE_IDENTIFIER = 1;
    static final int STATE_NUMBER = 2;
    static final int STATE_SLASH = 3;
    static final int STATE_COMMENT = 4;
    static final int STATE_COMMENT_STAR = 5;
    static final int STATE_EQUALS = 6;
    static final int STATE_LESS_THAN = 7;
    static final int STATE_GREATER_THAN = 8;
    static final int STATE_EXCLAMATION = 9;
    // Final states for dfsa
    static final int FINAL_STATE_PLUS = 10;
    static final int FINAL_STATE_MINUS = 11;
    static final int FINAL_STATE_STAR = 12;
    static final int FINAL_STATE_LEFT_PAREN = 13;
    static final int FINAL_STATE_RIGHT_PAREN = 14;
    static final int FINAL_STATE_LEFT_BRACE = 15;
    static final int FINAL_STATE_RIGHT_BRACE = 16;
    static final int FINAL_STATE_COMMA = 17;
    static final int FINAL_STATE_SEMICOLON = 18;
    static final int FINAL_STATE_DOT = 19;
    static final int FINAL_STATE_EQUALS_EQUALS = 20;
    static final int FINAL_STATE_LESS_EQUAL = 21;
    static final int FINAL_STATE_GREATER_EQUAL = 22;
    static final int FINAL_STATE_NOT_EQUAL = 23;

    CharacterReader charReader;
    // Transition table,.] Rows are states, columns are character classes.
    static int[][] TransitionTable() {
        return new int[][] {
            /* Columns: LETTER, DIGIT, WHITESPACE, PLUS, MINUS, STAR, SLASH, EQUALS, EXCLAMATION,LESS_THAN,
             GREATER_THAN, LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, COMMA, SEMICOLON, DOT, EOF, OTHER*/ 
            {STATE_IDENTIFIER, STATE_NUMBER, STATE_START, FINAL_STATE_PLUS, FINAL_STATE_MINUS, FINAL_STATE_STAR,STATE_SLASH, STATE_EQUALS, STATE_EXCLAMATION, 
                STATE_LESS_THAN, STATE_GREATER_THAN, FINAL_STATE_LEFT_PAREN,FINAL_STATE_RIGHT_PAREN, FINAL_STATE_LEFT_BRACE, FINAL_STATE_RIGHT_BRACE, FINAL_STATE_COMMA,
                FINAL_STATE_SEMICOLON, FINAL_STATE_DOT, INVALID, STATE_START}, // STATE_START
            {STATE_IDENTIFIER, STATE_IDENTIFIER, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID,
                INVALID, INVALID, INVALID, INVALID, INVALID, INVALID}, // STATE_IDENTIFIER
            {INVALID, STATE_NUMBER, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID,
                INVALID, INVALID, INVALID, INVALID, INVALID}, // STATE_NUMBER
            {INVALID, INVALID, INVALID, INVALID, INVALID, STATE_COMMENT, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID,
                INVALID, INVALID, INVALID, INVALID, INVALID}, // STATE_SLASH
            {STATE_COMMENT, STATE_COMMENT, STATE_COMMENT, STATE_COMMENT, STATE_COMMENT, STATE_COMMENT_STAR,
                STATE_COMMENT, STATE_COMMENT, STATE_COMMENT, STATE_COMMENT, STATE_COMMENT, STATE_COMMENT,
                STATE_COMMENT, STATE_COMMENT, STATE_COMMENT, STATE_COMMENT, STATE_COMMENT, STATE_COMMENT,
                STATE_COMMENT, STATE_COMMENT}, // STATE_COMMENT
            {STATE_COMMENT, STATE_COMMENT, STATE_COMMENT, STATE_COMMENT, STATE_COMMENT, STATE_COMMENT_STAR,
                STATE_START, STATE_COMMENT, STATE_COMMENT, STATE_COMMENT, STATE_COMMENT, STATE_COMMENT,
                STATE_COMMENT, STATE_COMMENT, STATE_COMMENT, STATE_COMMENT, STATE_COMMENT, STATE_COMMENT,
                STATE_COMMENT, STATE_COMMENT}, // STATE_COMMENT_STAR
            {INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, FINAL_STATE_EQUALS_EQUALS, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID,
                INVALID, INVALID, INVALID, INVALID, INVALID, INVALID}, // STATE_EQUALS
            {INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, FINAL_STATE_LESS_EQUAL, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID,
                INVALID, INVALID, INVALID, INVALID, INVALID, INVALID}, // STATE_LESS_THAN
            {INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID,      INVALID, INVALID, INVALID, INVALID, INVALID, INVALID,
                INVALID, INVALID, INVALID, INVALID, INVALID, INVALID}, // STATE_GREATER_THAN
            {INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, FINAL_STATE_NOT_EQUAL, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID,
                INVALID, INVALID, INVALID, INVALID, INVALID, INVALID}, // STATE_EXCLAMATION
            {INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID,
                INVALID, INVALID, INVALID, INVALID}, // STATE_ACCEPT_PLUS
            {INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID,
                INVALID, INVALID, INVALID, INVALID}, // STATE_ACCEPT_MINUS
            {INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID,
                INVALID, INVALID, INVALID, INVALID}, // STATE_ACCEPT_STAR
            {INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID,
                INVALID, INVALID, INVALID, INVALID}, // STATE_ACCEPT_LEFT_PAREN
            {INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID,
                INVALID, INVALID, INVALID, INVALID}, // STATE_ACCEPT_RIGHT_PAREN
            {INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID,
                INVALID, INVALID, INVALID, INVALID}, // STATE_ACCEPT_LEFT_BRACE
            {INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID,
                INVALID, INVALID, INVALID, INVALID}, // STATE_ACCEPT_RIGHT_BRACE
            {INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID,
                INVALID, INVALID, INVALID, INVALID}, // STATE_ACCEPT_COMMA
            {INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID,
                INVALID, INVALID, INVALID, INVALID}, // STATE_ACCEPT_SEMICOLON
            {INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID,
                INVALID, INVALID, INVALID, INVALID}, // STATE_ACCEPT_DOT
            {INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID,
                INVALID, INVALID, INVALID, INVALID}, // STATE_ACCEPT_EQUALS_EQUALS
            {INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID,
                INVALID, INVALID, INVALID, INVALID}, // STATE_ACCEPT_LESS_EQUAL
            {INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID,
                INVALID, INVALID, INVALID, INVALID}, // STATE_ACCEPT_GREATER_EQUAL
            {INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID,
                INVALID, INVALID, INVALID, INVALID} // STATE_ACCEPT_NOT_EQUAL
        };
    }
    // Maps final states to token types
    static TokenType[] FinalSates() {
        TokenType[] table = new TokenType[FINAL_STATE_NOT_EQUAL + 1];
        table[FINAL_STATE_PLUS] = TokenType.ADD_OP;
        table[FINAL_STATE_MINUS] = TokenType.SUB_OP;
        table[FINAL_STATE_STAR] = TokenType.MULT_OP;
        table[FINAL_STATE_LEFT_PAREN] = TokenType.LEFT_PAREN;
        table[FINAL_STATE_RIGHT_PAREN] = TokenType.RIGHT_PAREN;
        table[FINAL_STATE_LEFT_BRACE] = TokenType.LEFT_BRACE;
        table[FINAL_STATE_RIGHT_BRACE] = TokenType.RIGHT_BRACE;
        table[FINAL_STATE_COMMA] = TokenType.COMMA;
        table[FINAL_STATE_SEMICOLON] = TokenType.SEMICOLON;
        table[FINAL_STATE_DOT] = TokenType.DOT;
        table[FINAL_STATE_EQUALS_EQUALS] = TokenType.EQUALS_OP;
        table[FINAL_STATE_LESS_EQUAL] = TokenType.LESS_EQUAL_OP;
        table[FINAL_STATE_GREATER_EQUAL] = TokenType.GREATER_EQUAL_OP;
        table[FINAL_STATE_NOT_EQUAL] = TokenType.NOT_EQUAL_OP;
        return table;
    }
    // Keyword token type
    static String[] KeywordTable() {
        return new String[] {
            "CLASS",
            "CONST",
            "VAR",
            "PROCEDURE",
            "CALL",
            "IF",
            "THEN",
            "WHILE",
            "DO",
            "ODD"
        };
    }
// map keywordsto token type
    static TokenType[] TokenTable() {
        return new TokenType[] {
            TokenType.CLASS,
            TokenType.CONST,
            TokenType.VAR,
            TokenType.PROCEDURE,
            TokenType.CALL,
            TokenType.IF,
            TokenType.THEN,
            TokenType.WHILE,
            TokenType.DO,
            TokenType.ODD
        };
    }

    static int[][] TRANSITION_TABLE = TransitionTable();
    static TokenType[] FinalState = FinalSates();
    static String[] KEYWORDS = KeywordTable();
    static TokenType[] KEYWORD_TOKEN = TokenTable();

    static TokenType TypeFinalState(int state) {
        if (state < 0 || state >= FinalState.length) {
            return null;
        }
        return FinalState[state];
    }
     public Token[] getTokens() {
        Token[] tokenBuffer = new Token[0];
        int count = 0;

        while (true) {
            // Expand buffer if needed x2
            if (count == tokenBuffer.length) {
                int newSize = tokenBuffer.length == 0 ? 1 : tokenBuffer.length * 2;
                Token[] expandedTokenBuffer = new Token[newSize];
                for (int i = 0; i < tokenBuffer.length; i++) {
                    expandedTokenBuffer[i] = tokenBuffer[i];
                }
                tokenBuffer = expandedTokenBuffer;
            }

            Token next = nextToken();
            tokenBuffer[count++] = next;
            if (next.type == TokenType.EOF) {
                break;
            }
        }

        Token[] output = new Token[count];
        for (int i = 0; i < count; i++) {
            output[i] = tokenBuffer[i];
        }
        return output;
    }

    static TokenType keywordTokenType(String text) {
        for (int i = 0; i < KEYWORDS.length; i++) {
            if (KEYWORDS[i].equalsIgnoreCase(text)) {
                return KEYWORD_TOKEN[i];
            }
        }
        return null;
    }

    
    // Returns the next token from source code,Uses transition table and final states.
    public Token nextToken() {
        int state = STATE_START;
        StringBuilder tokenText = new StringBuilder();

        while (true) {
            boolean atEnd = charReader.eof();
            char currentChar = 0;
            CharacterClass charClass;

            if (atEnd) {
                charClass = CharacterClass.EOF;
            } else {
                currentChar = charReader.readNextCharacter();
                charClass = CharacterClass.classifyCharacter(currentChar);
            }
            int nextState = TRANSITION_TABLE[state][charClass.ordinal()];

            switch (state) {
                case STATE_START:
                    if (charClass == CharacterClass.WHITESPACE || charClass == CharacterClass.OTHER) {
                        break;
                    }

                    if (charClass == CharacterClass.EOF) {
                        return new Token(TokenType.EOF, "EOF");
                    }

                    tokenText.append(currentChar);

                    TokenType singleCharTokenType = TypeFinalState(nextState);
                    if (singleCharTokenType != null) {
                        return new Token(singleCharTokenType, tokenText.toString());
                    }
                    break;

                case STATE_IDENTIFIER:
                    if (nextState == INVALID) {
                        if (charClass != CharacterClass.EOF) {
                            charReader.moveBackOneChar();
                        }

                        String text = tokenText.toString();
                        TokenType keywordType = keywordTokenType(text);
                        if (keywordType != null) {
                            return new Token(keywordType, text);
                        }
                        return new Token(TokenType.IDENT, text);
                    }

                    tokenText.append(currentChar);
                    break;

                case STATE_NUMBER:
                    if (nextState == INVALID) {
                        if (charClass != CharacterClass.EOF) {
                            charReader.moveBackOneChar();
                        }
                        return new Token(TokenType.NUMBER, tokenText.toString());
                    }

                    tokenText.append(currentChar);
                    break;

                case STATE_SLASH:
                    if (nextState == STATE_COMMENT) {
                        tokenText.setLength(0);
                        break;
                    }
                    if (nextState == INVALID) {
                        if (charClass != CharacterClass.EOF) {
                            charReader.moveBackOneChar();
                        }
                        return new Token(TokenType.DIV_OP, tokenText.toString());
                    }
                    break;

                case STATE_COMMENT:
                case STATE_COMMENT_STAR:
                    break;

                case STATE_EQUALS:
                    TokenType finalizedType = TypeFinalState(nextState);
                    if (finalizedType != null) {
                        tokenText.append(currentChar);
                        return new Token(finalizedType, tokenText.toString());
                    }

                    if (nextState == INVALID) {
                        if (charClass != CharacterClass.EOF) {
                            charReader.moveBackOneChar();
                        }
                        return new Token(TokenType.ASSIGN_OP, tokenText.toString());
                    }
                    break;

                case STATE_LESS_THAN:
                    TokenType lessFinalizedType = TypeFinalState(nextState);
                    if (lessFinalizedType != null) {
                        tokenText.append(currentChar);
                        return new Token(lessFinalizedType, tokenText.toString());
                    }

                    if (nextState == INVALID) {
                        if (charClass != CharacterClass.EOF) {
                            charReader.moveBackOneChar();
                        }
                        return new Token(TokenType.LESS_OP, tokenText.toString());
                    }
                    break;

                case STATE_GREATER_THAN:
                    TokenType greaterFinalizedType = TypeFinalState(nextState);
                    if (greaterFinalizedType != null) {
                        tokenText.append(currentChar);
                        return new Token(greaterFinalizedType, tokenText.toString());
                    }

                    if (nextState == INVALID) {
                        if (charClass != CharacterClass.EOF) {
                            charReader.moveBackOneChar();
                        }
                        return new Token(TokenType.GREATER_OP, tokenText.toString());
                    }
                    break;

                case STATE_EXCLAMATION:
                    TokenType exclamationFinalizedType = TypeFinalState(nextState);
                    if (exclamationFinalizedType != null) {
                        tokenText.append(currentChar);
                        return new Token(exclamationFinalizedType, tokenText.toString());
                    }
                    break;

                default:
                    break;
            }

            state = nextState;
            if (state == STATE_START) {
                tokenText.setLength(0);
            }
        }
    }

   
    public Scanner(String sourceCode) 
    {
        this.charReader = new CharacterReader(sourceCode);
        }

    public static void main(String[] args)
     {
        try 
        {
            String sourceCode = Files.readString(Path.of("Program_Text.txt"));
            Scanner tokenScanner = new Scanner(sourceCode);
            Token[] AllTokens = tokenScanner.getTokens();
            SymbolTable fileWriteSymtab = SymbolTable.buildFromTokens(AllTokens);
            // Write outputs to files
            TokenWriter.writeToFile(AllTokens, "tokens.txt");
            SymbolTableWriter.writeToFile(fileWriteSymtab, "symbol_table.txt");
        } catch (IOException ioException) 
        {
            throw new UncheckedIOException(ioException);
        }
    }
}
