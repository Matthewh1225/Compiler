//TokenWriter.java
import java.io.FileWriter;
import java.io.IOException;

public class TokenWriter {
    public static void writeToFile(Token[] tokens, String filename) throws IOException {
        try (FileWriter tokenWriter = new FileWriter(filename)) {
            tokenWriter.write(String.format("%-24s %s%n", "Token", "Classification"));
            tokenWriter.write("-".repeat(52) + "\n");
            for (int i = 0; i < tokens.length; i++) {
                tokenWriter.write(String.format("%-24s %s%n", tokens[i].lexeme, tokens[i].classification));
            }
        }
    }
}
