//SymbolTableWriter.java
import java.io.FileWriter;
import java.io.IOException;
//
public class SymbolTableWriter {
    // Writes the symbol table to a file nicely fromatted
    public static void writeToFile(SymbolTable symbolTable, String filename) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("Symbol Table:\n");
            // spacing for columns: Symbol(24)Classification(18),Value (10),Address(8),Segment(7)
            writer.write(String.format("%-24s %-18s %-10s %-8s %-7s\n",
                "Symbol", "Classification", "Value", "Address", "Segment"));
            writer.write("-".repeat(75) + "\n");

            for (int i = 0; i < symbolTable.symbolCount; i++) {
                SymbolTable.SymbolTableEntry entry = symbolTable.symbolEntries[i];
                writer.write(String.format("%-24s %-18s %-10s %-8d %-7s\n",
                    entry.symbolName,
                    entry.classification,
                    entry.valueText,
                    entry.memoryAddress,
                    entry.memorySegment));
            }
            writer.write("-".repeat(75) + "\n");
        }
    }
}
