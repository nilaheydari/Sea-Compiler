package compiler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        try {

            String code = Files.readString(
                    Path.of("input.sea")
            );

            Lexer lexer = new Lexer();

            List<Token> tokens = lexer.tokenize(code);

            for (Token token : tokens) {
                System.out.println(token);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}