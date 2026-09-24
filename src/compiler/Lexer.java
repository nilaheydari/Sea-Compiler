package compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Lexer {

    private static final Set<String> KEYWORDS = Set.of(
            "begin", "bool", "break", "case",
            "char", "class", "do", "double",
            "else", "end", "false", "float",
            "for", "if", "int", "return",
            "switch", "true", "try", "while",
            "void", "string"
    );

    public List<Token> tokenize(String input) {

        List<Token> tokens = new ArrayList<>();

        int line = 1;
        int column = 1;
        int i = 0;

        while (i < input.length()) {

            char current = input.charAt(i);

            if (Character.isWhitespace(current)) {

                if (current == '\n') {
                    line++;
                    column = 1;
                } else {
                    column++;
                }

                i++;
                continue;
            }
            if (current == '/' &&
                    i + 1 < input.length() &&
                    input.charAt(i + 1) == '/') {

                int startColumn = column;

                StringBuilder comment = new StringBuilder();

                while (i < input.length() &&
                        input.charAt(i) != '\n') {

                    comment.append(input.charAt(i));
                    i++;
                    column++;
                }

                tokens.add(
                        new Token(
                                TokenType.COMMENT,
                                comment.toString(),
                                line,
                                startColumn
                        )
                );

                continue;
            }
            if (current == '/' &&
                    i + 1 < input.length() &&
                    input.charAt(i + 1) == '*') {


                int startColumn = column;

                StringBuilder comment = new StringBuilder();

                comment.append("/*");

                i += 2;
                column += 2;

                while (i + 1 < input.length()) {

                    if (input.charAt(i) == '*' &&
                            input.charAt(i + 1) == '/') {

                        comment.append("*/");

                        i += 2;
                        column += 2;

                        break;
                    }

                    if (input.charAt(i) == '\n') {
                        line++;
                        column = 1;
                    } else {
                        column++;
                    }

                    comment.append(input.charAt(i));
                    i++;
                }

                tokens.add(
                        new Token(
                                TokenType.COMMENT,
                                comment.toString(),
                                line,
                                startColumn
                        )
                );

                continue;
            }

            if (current == '"') {

                int startColumn = column;

                StringBuilder text = new StringBuilder();

                text.append('"');

                i++;
                column++;

                while (i < input.length() &&
                        input.charAt(i) != '"') {

                    text.append(input.charAt(i));
                    i++;
                    column++;
                }

                if (i < input.length()) {

                    text.append('"');
                    i++;
                    column++;
                }

                tokens.add(
                        new Token(
                                TokenType.STRING,
                                text.toString(),
                                line,
                                startColumn
                        )
                );

                continue;
            }
            if (Character.isLetter(current) || current == '_') {

                int startColumn = column;

                StringBuilder word = new StringBuilder();

                while (i < input.length() &&
                        (Character.isLetterOrDigit(input.charAt(i))
                                || input.charAt(i) == '_')) {

                    word.append(input.charAt(i));
                    i++;
                    column++;
                }

                String value = word.toString();

                if (KEYWORDS.contains(value)) {
                    tokens.add(
                            new Token(
                                    TokenType.KEYWORD,
                                    value,
                                    line,
                                    startColumn
                            )
                    );
                } else {
                    tokens.add(
                            new Token(
                                    TokenType.IDENTIFIER,
                                    value,
                                    line,
                                    startColumn
                            )
                    );
                }

                continue;
            }

            if (Character.isDigit(current)) {

                int startColumn = column;

                StringBuilder number = new StringBuilder();

                while (i < input.length() &&
                        Character.isDigit(input.charAt(i))) {

                    number.append(input.charAt(i));
                    i++;
                    column++;
                }

                if (i < input.length() && input.charAt(i) == '.') {

                    number.append('.');
                    i++;
                    column++;

                    while (i < input.length() &&
                            Character.isDigit(input.charAt(i))) {

                        number.append(input.charAt(i));
                        i++;
                        column++;
                    }

                    if (i < input.length() &&
                            (input.charAt(i) == 'f' || input.charAt(i) == 'F')) {

                        number.append(input.charAt(i));
                        i++;
                        column++;

                        tokens.add(
                                new Token(
                                        TokenType.FLOAT,
                                        number.toString(),
                                        line,
                                        startColumn
                                )
                        );

                    } else {

                        tokens.add(
                                new Token(
                                        TokenType.DOUBLE,
                                        number.toString(),
                                        line,
                                        startColumn
                                )
                        );
                    }

                } else {

                    tokens.add(
                            new Token(
                                    TokenType.INTEGER,
                                    number.toString(),
                                    line,
                                    startColumn
                            )
                    );
                }

                continue;
            }

            if (i + 1 < input.length()) {

                String twoChars = "" + current + input.charAt(i + 1);

                if (twoChars.equals("==") ||
                        twoChars.equals("!=") ||
                        twoChars.equals("<=") ||
                        twoChars.equals(">=") ||
                        twoChars.equals("++") ||
                        twoChars.equals("--")) {

                    tokens.add(
                            new Token(
                                    TokenType.OPERATOR,
                                    twoChars,
                                    line,
                                    column
                            )
                    );

                    i += 2;
                    column += 2;
                    continue;
                }
            }
            if (current == '.') {

                tokens.add(
                        new Token(
                                TokenType.OPERATOR,
                                ".",
                                line,
                                column
                        )
                );

                i++;
                column++;
                continue;
            }
            if (current == '=' ||
                    current == '+' ||
                    current == '-' ||
                    current == '*' ||
                    current == '/' ||
                    current == '<' ||
                    current == '>' ||
                    current == '%') {

                tokens.add(
                        new Token(
                                TokenType.OPERATOR,
                                String.valueOf(current),
                                line,
                                column
                        )
                );

                i++;
                column++;
                continue;
            }

            if (current == ';' ||
                    current == '(' ||
                    current == ')' ||
                    current == ',' ||
                    current == '[' ||
                    current == ']') {

                tokens.add(
                        new Token(
                                TokenType.SYMBOL,
                                String.valueOf(current),
                                line,
                                column
                        )
                );

                i++;
                column++;
                continue;
            }

            System.out.println(
                    "Unknown Character: "
                            + current
                            + " at line "
                            + line
                            + " column "
                            + column
            );

            i++;
            column++;
        }

        return tokens;
    }
}