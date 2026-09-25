package compiler;

public class Token {

    private TokenType type;
    private String value;
    private int line;
    private int column;

    public Token(TokenType type,
                 String value,
                 int line,
                 int column) {

        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
    }
    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
    @Override
    public String toString() {

        String tokenName;

        switch (type) {

            case KEYWORD:
                tokenName = "Keyword";
                break;

            case STRING:
                tokenName = "String Literal";
                break;

            case IDENTIFIER:
                tokenName = "Identifier";
                break;

            case INTEGER:
                tokenName = "Integer Literal";
                break;

            case FLOAT:
                tokenName = "Float Literal";
                break;

            case DOUBLE:
                tokenName = "Double Literal";
                break;

            case COMMENT:
                tokenName = "Comment";
                break;

            case OPERATOR:

                if (value.equals("="))
                    tokenName = "Assignment Operator";
                else if (value.equals("."))
                    tokenName = "Member Access Operator";
                else
                    tokenName = "Operator";

                break;

            case SYMBOL:

                if (value.equals(";"))
                    tokenName = "Semicolon";
                else if (value.equals("("))
                    tokenName = "Left Parenthesis";
                else if (value.equals(")"))
                    tokenName = "Right Parenthesis";
                else if (value.equals(","))
                    tokenName = "Comma";
                else
                    tokenName = "Symbol";

                break;

            default:
                tokenName = type.toString();
        }

        return tokenName +
                " (" + value + ")" +
                " - Line " + line +
                ", Column " + column;
    }
}