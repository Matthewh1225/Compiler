//CharacterReader.java
class CharacterReader {
    String sourceText;
    int position;

    CharacterReader(String sourceText) {
        this.sourceText = sourceText;
        this.position = 0;
    }

    boolean eof() {
        return position >= sourceText.length();
    }
    //returns the next character and advances
    char readNextCharacter() {
        return sourceText.charAt(position++);
    }
    //back one character
    void moveBackOneChar() {
        position--;
    }
}
