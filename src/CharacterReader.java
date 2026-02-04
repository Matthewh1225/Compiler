final class CharacterReader {
    static final char END_OF_FILE_CHAR = '\0';
    private final String ProgramText;
    private int currentIndex;

    CharacterReader(String ProgramText) {
        this.ProgramText = ProgramText;
        this.currentIndex = 0;
    }
    char readNextCharacter() {
        if (currentIndex >= ProgramText.length()) {
            return END_OF_FILE_CHAR;
        }
        char character = ProgramText.charAt(currentIndex);
        currentIndex++;
        return character;
    }
    void retractOneCharacter(char lastCharacterRead) {
        if (lastCharacterRead == END_OF_FILE_CHAR) {
            return;
        }
        if (currentIndex > 0) {
            currentIndex--;
        }
    }
}
