
//SymbolTable.java
public class SymbolTable {
    static int START_SIZE = 32;
    // States for state table
    static final int STATE_DEFAULT = 0;
    static final int STATE_PROGRAM_NAME = 1;
    static final int STATE_PROCEDURE_NAME = 2;
    static final int STATE_CONST_NAME = 3;
    static final int STATE_CONST_AFTER_NAME = 4;
    static final int STATE_CONST_VALUE = 5;
    static final int STATE_CONST_AFTER_VALUE = 6;
    static final int STATE_VAR_NAME = 7;
    static final int STATE_VAR_AFTER_NAME = 8;
    // Columns for state table
    static final int COLUMN_CLASS = 0;
    static final int COLUMN_CONST = 1;
    static final int COLUMN_VAR = 2;
    static final int COLUMN_PROCEDURE = 3;
    static final int COLUMN_IDENTIFIER = 4;
    static final int COLUMN_NUMBER = 5;
    static final int COLUMN_ASSIGN = 6;
    static final int COLUMN_COMMA = 7;
    static final int COLUMN_SEMICOLON = 8;
    static final int COLUMN_RIGHT_BRACE = 9;
    static final int COLUMN_EOF = 10;
    static final int COLUMN_OTHER = 11;

    //build state and action tables 
    static int[][] stateTable = createStateTable();
    static Actions[][] actionTable = createActionTable();
    SymbolTableEntry[] symbolEntries;
    int symbolCount;
    int dataAddress;
    int codeAddress;
// //Actions for action table
    enum Actions {
        NONE,
        PROGRAM_NAME,
        PROCEDURE_NAME,
        CONST_NAME,
        CONST_VALUE,
        VAR_NAME,
        NUMBER_LITERAL,
        IDENTIFIER_USE
    }

    static class SymbolTableEntry {
        String symbolName;
        String classification;
        String valueText;
        int memoryAddress;
        String memorySegment;

        SymbolTableEntry(String symbolName, String classification, String valueText, int memoryAddress, String memorySegment) {
            this.symbolName = symbolName;
            this.classification = classification;
            this.valueText = valueText;
            this.memoryAddress = memoryAddress;
            this.memorySegment = memorySegment;
        }
    }

    static int[][] createStateTable() {
        return new int[][] {
            // CLASS, CONST, VAR, PROCEDURE, IDENTIFIER, NUMBER, ASSIGN, COMMA, SEMICOLON, RIGHT_BRACE, EOF, OTHER
            {STATE_PROGRAM_NAME, STATE_CONST_NAME, STATE_VAR_NAME, STATE_PROCEDURE_NAME, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT},
            {STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT},
            {STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT},
            {STATE_CONST_NAME, STATE_CONST_NAME, STATE_CONST_NAME, STATE_CONST_NAME, STATE_CONST_AFTER_NAME, STATE_CONST_NAME, STATE_CONST_NAME, STATE_CONST_NAME, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT, STATE_CONST_NAME},
            {STATE_CONST_AFTER_NAME, STATE_CONST_AFTER_NAME, STATE_CONST_AFTER_NAME, STATE_CONST_AFTER_NAME, STATE_CONST_AFTER_NAME, STATE_CONST_AFTER_NAME, STATE_CONST_VALUE, STATE_CONST_AFTER_NAME, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT, STATE_CONST_AFTER_NAME},
            {STATE_CONST_VALUE, STATE_CONST_VALUE, STATE_CONST_VALUE, STATE_CONST_VALUE, STATE_CONST_VALUE, STATE_CONST_AFTER_VALUE, STATE_CONST_VALUE, STATE_CONST_VALUE, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT, STATE_CONST_VALUE},
            {STATE_CONST_AFTER_VALUE, STATE_CONST_AFTER_VALUE, STATE_CONST_AFTER_VALUE, STATE_CONST_AFTER_VALUE, STATE_CONST_AFTER_VALUE, STATE_CONST_AFTER_VALUE, STATE_CONST_AFTER_VALUE, STATE_CONST_NAME, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT, STATE_CONST_AFTER_VALUE},
            {STATE_VAR_NAME, STATE_VAR_NAME, STATE_VAR_NAME, STATE_VAR_NAME, STATE_VAR_AFTER_NAME, STATE_VAR_NAME, STATE_VAR_NAME, STATE_VAR_NAME, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT, STATE_VAR_NAME},
            {STATE_VAR_AFTER_NAME, STATE_VAR_AFTER_NAME, STATE_VAR_AFTER_NAME, STATE_VAR_AFTER_NAME, STATE_VAR_AFTER_NAME, STATE_VAR_AFTER_NAME, STATE_VAR_AFTER_NAME, STATE_VAR_NAME, STATE_DEFAULT, STATE_DEFAULT, STATE_DEFAULT, STATE_VAR_AFTER_NAME}
        };
    }
// Action table maps to an action  before going to the next state
    static Actions[][] createActionTable() {
        Actions[][] table = new Actions[STATE_VAR_AFTER_NAME + 1][COLUMN_OTHER + 1];
        for (int state = 0; state < table.length; state++) {
            for (int column = 0; column < table[state].length; column++) {
                table[state][column] = Actions.NONE;
            }
        }
        table[STATE_PROGRAM_NAME][COLUMN_IDENTIFIER] = Actions.PROGRAM_NAME;
        table[STATE_PROCEDURE_NAME][COLUMN_IDENTIFIER] = Actions.PROCEDURE_NAME;
        table[STATE_CONST_NAME][COLUMN_IDENTIFIER] = Actions.CONST_NAME;
        table[STATE_CONST_VALUE][COLUMN_NUMBER] = Actions.CONST_VALUE;
        table[STATE_VAR_NAME][COLUMN_IDENTIFIER] = Actions.VAR_NAME;
        table[STATE_DEFAULT][COLUMN_NUMBER] = Actions.NUMBER_LITERAL;
        table[STATE_DEFAULT][COLUMN_IDENTIFIER] = Actions.IDENTIFIER_USE;
        return table;
    }

    public SymbolTable() {
        symbolEntries = new SymbolTableEntry[START_SIZE];
        symbolCount = 0;
        dataAddress = 0;
        codeAddress = 0;
    }
    // Adds a symbol to the table, resizing x2
    void addSymbol(String name, String classText, String valueText, int address, String segment) {
        if (symbolCount == symbolEntries.length) {
            int newSize = symbolEntries.length == 0 ? 1 : symbolEntries.length * 2;
            SymbolTableEntry[] bigger = new SymbolTableEntry[newSize];
            for (int i = 0; i < symbolEntries.length; i++) {
                bigger[i] = symbolEntries[i];
            }
            symbolEntries = bigger;
        }

        symbolEntries[symbolCount++] = new SymbolTableEntry(name, classText, valueText, address, segment);
    }

    // Maps a token type to a column in the state table
    static int tokenToColumn(TokenType type) {
        switch (type) {
            case CLASS:
                return COLUMN_CLASS;
            case CONST:
                return COLUMN_CONST;
            case VAR:
                return COLUMN_VAR;
            case PROCEDURE:
                return COLUMN_PROCEDURE;
            case IDENT:
                return COLUMN_IDENTIFIER;
            case NUMBER:
                return COLUMN_NUMBER;
            case ASSIGN_OP:
                return COLUMN_ASSIGN;
            case COMMA:
                return COLUMN_COMMA;
            case SEMICOLON:
                return COLUMN_SEMICOLON;
            case RIGHT_BRACE:
                return COLUMN_RIGHT_BRACE;
            case EOF:
                return COLUMN_EOF;
            default:
                return COLUMN_OTHER;
        }
    }

    // Finds the index of a symbol in the table, or -1 if not found
    static int lookupSymbolIndex(SymbolTable table, String name) {
        for (int i = 0; i < table.symbolCount; i++) {
            if (table.symbolEntries[i].symbolName.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    static boolean containsLiteral(SymbolTable table, String value) {
        for (int i = 0; i < table.symbolCount; i++) {
            SymbolTableEntry entry = table.symbolEntries[i];
            if ("Numeric literal".equals(entry.classification) && entry.valueText.equals(value)) {
                return true;
            }
        }
        return false;
    }

    static void addTempSymbols(SymbolTable table, int maxOperatorCount) {
        int tempCount = maxOperatorCount;
        if (tempCount > 3) {
            tempCount = 3;
        }
        for (int i = 1; i <= tempCount; i++) {
            String tempName = "Temp" + i;
            if (lookupSymbolIndex(table, tempName) < 0) {
                table.addSymbol(tempName, "Var", "?", table.dataAddress, "DS");
                table.dataAddress += 2;
            }
        }
    }

    // Determines the classification of an identifier use based on the symbol table
    static String classificationForIdentifier(SymbolTable table, String name) {
        int index = lookupSymbolIndex(table, name);
        if (index < 0) {
            return "Var";
        }

        // if found, check if its a procedure or program name, default to Var
        String knownClass = table.symbolEntries[index].classification;
        if ("Procedure".equals(knownClass)) {
            return "Procedure";
        }
        if ("$program name".equals(knownClass)) {
            return "$program name";
        }
        return "Var";
    }
    //
    public static SymbolTable buildFromTokens(Token[] tokens) {
        SymbolTable table = new SymbolTable();
        int state = STATE_DEFAULT;
        String pendingConstName = null;

        int currentOperatorCount = 0;
        int maxOperatorCount = 0;

        // Iterate through tokens and build the symbol table based on the state machine
        for (int i = 0; i < tokens.length; i++) {
            Token token = tokens[i];

            if (token.type == TokenType.ADD_OP || token.type == TokenType.SUB_OP || token.type == TokenType.MULT_OP || token.type == TokenType.DIV_OP) {
                currentOperatorCount++;
            }

            if (token.type == TokenType.SEMICOLON || token.type == TokenType.RIGHT_BRACE || token.type == TokenType.THEN || token.type == TokenType.DO || token.type == TokenType.EOF) {
                if (currentOperatorCount > maxOperatorCount) {
                    maxOperatorCount = currentOperatorCount;
                }
                currentOperatorCount = 0;
            }

            int column = tokenToColumn(token.type);
            Actions action = actionTable[state][column];

            // Perform actions based on the current state and token type
            switch (action) {
                case PROGRAM_NAME:
                    token.classification = "$program name";
                    if (lookupSymbolIndex(table, token.lexeme) < 0) {
                        table.addSymbol(token.lexeme, "$program name", "", table.codeAddress, "CS");
                        table.codeAddress += 2;
                    }
                    break;

                case PROCEDURE_NAME:
                    token.classification = "Procedure";
                    if (lookupSymbolIndex(table, token.lexeme) < 0) {
                        table.addSymbol(token.lexeme, "Procedure", "?", table.codeAddress, "CS");
                        table.codeAddress += 2;
                    }
                    break;

                case CONST_NAME:
                    token.classification = "Constvar";
                    pendingConstName = token.lexeme;
                    break;

                case CONST_VALUE:
                    if (pendingConstName != null) {
                        token.classification = "Numeric literal";
                        if (lookupSymbolIndex(table, pendingConstName) < 0) {
                            table.addSymbol(pendingConstName, "Constvar", token.lexeme, table.dataAddress, "DS");
                            table.dataAddress += 2;
                        }
                        pendingConstName = null;
                    }
                    break;
                case VAR_NAME:
                    token.classification = "Var";
                    if (lookupSymbolIndex(table, token.lexeme) < 0) {
                        table.addSymbol(token.lexeme, "Var", "?", table.dataAddress, "DS");
                        table.dataAddress += 2;
                    }
                    break;
                case NUMBER_LITERAL:
                    token.classification = "Numeric literal";
                    if (!containsLiteral(table, token.lexeme)) {
                        table.addSymbol(token.lexeme, "Numeric literal", token.lexeme, table.dataAddress, "DS");
                        table.dataAddress += 2;
                    }
                    break;

                case IDENTIFIER_USE:
                    token.classification = classificationForIdentifier(table, token.lexeme);
                    break;
                case NONE:
                default:
                    break;
            }
            // Transition to the next state
            state = stateTable[state][column];
            if (state == STATE_DEFAULT) {
                pendingConstName = null;
            }
        }
        // After  all tokens, add t1,t2,t3... for intermediate code generation
        addTempSymbols(table, maxOperatorCount);
        return table;
    }
} 