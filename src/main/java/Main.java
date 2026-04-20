import org.w3c.dom.Document;
import project10.Grammer;
import project10.Tokenizer;
import project11.Assemble;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0) {
            String filename = args[0];
            String newfile = filename.replace("jack", "vm");
            Grammer grammer = new Grammer();
            Tokenizer tokenizer = new Tokenizer();

            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                String line;
                StringBuilder completeFile = new StringBuilder();
                ArrayList<String> tokenizedFile;

                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("//")) {
                        completeFile.append(line.trim());
                    }
                }

                // Tokenize
                tokenizedFile = tokenizer.tokenize(completeFile.toString());

                // Grammer
                Document grammerDoc = grammer.build(tokenizedFile);

                Assemble compiler = new Assemble();
                String vmCode = compiler.compile(grammerDoc);
                System.out.println(vmCode);
            } catch (IOException e) {
                System.err.println("Error reading file: " + e.getMessage());
                e.printStackTrace();
            }

            /*try {
                Path file = Path.of(newfile);
                //Files.writeString(file, content);
                System.out.println("Successfully wrote to the file " + file.toString());
            } catch (IOException e) {
                System.err.println("Error writing to the file: " + e.getMessage());
            }*/
        } else {
            System.out.println("Please enter a file path");
        }
    }
}
