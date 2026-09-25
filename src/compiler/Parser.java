package compiler;
import java.util.List;

public class Parser {

    private List<Token> tokens;
    private int current = 0;

    private int mainCount = 0;
    private java.util.Map<String,
            java.util.List<String>> functionParameters =
            new java.util.HashMap<>();
    private java.util.Map<String,String> variableTypes =
            new java.util.HashMap<>();
    private java.util.Stack<java.util.Set<String>> scopes =
            new java.util.Stack<>();
    private java.util.Set<String> functions =
            new java.util.HashSet<>();
    private java.util.Set<String> classes =
            new java.util.HashSet<>();
    private ASTNode currentAST;
    private java.util.Map<String, String> classDependencies =
            new java.util.HashMap<>();

    private boolean functionScopePending = false;
    private String currentClass = null;
    private boolean insideFor = false;
    public Parser(List<Token> tokens) {

        this.tokens = tokens;
        scopes.push(new java.util.HashSet<>());
        functions.add("print");
    }

    public ASTNode getCurrentAST() {
        return currentAST;
    }

    public void parse() {

        while (!isAtEnd()) {

            if (match(TokenType.KEYWORD)) {

                Token keyword = previous();
                if (keyword.getValue().equals("begin")) {

                    if (functionScopePending) {

                        functionScopePending = false;
                    }
                    else {

                        enterScope();
                    }
                }
                else if (keyword.getValue().equals("end")) {

                    exitScope();
                }

                else if (keyword.getValue().equals("if")) {

                    System.out.println("Conditional: if");
                    System.out.println();

                    parseCondition();
                }

                else if (keyword.getValue().equals("else")) {

                    if (!isAtEnd()
                            && peek().getType() == TokenType.KEYWORD
                            && peek().getValue().equals("if")) {

                        advance();

                        System.out.println("Conditional: else if");
                        System.out.println();

                        parseCondition();
                    }
                    else {

                        System.out.println("Conditional: else");
                        System.out.println();
                    }
                }
                else if (keyword.getValue().equals("for")) {

                    insideFor = true;

                    System.out.println("Loop: for");
                    System.out.println();

                    parseForHeader();
                }

                else if (keyword.getValue().equals("while")) {

                    System.out.println("Loop: while");
                    System.out.println();

                    parseCondition();
                }

                if (keyword.getValue().equals("class")) {

                    parseClass();
                }

                else if (keyword.getValue().equals("return")) {

                    parseReturn();
                }
                else if (keyword.getValue().equals("int") ||
                        keyword.getValue().equals("float") ||
                        keyword.getValue().equals("double") ||
                        keyword.getValue().equals("string") ||
                        keyword.getValue().equals("bool") ||
                        keyword.getValue().equals("char") ||
                        keyword.getValue().equals("void")) {
                    parseVariableOrFunction(keyword);
                }
            }
            else if (match(TokenType.IDENTIFIER)) {

                Token identifier = previous();

                // User Defined Type
                if (!isAtEnd()
                        && peek().getType() == TokenType.IDENTIFIER) {

                    Token variableName = advance();

                    if (currentClass != null) {

                        classDependencies.put(
                                currentClass,
                                identifier.getValue()
                        );
                    }

                    System.out.println("Variable:");
                    System.out.println("Type = " + identifier.getValue());
                    System.out.println("Name = " + variableName.getValue());
                    System.out.println();

                    continue;
                }

                // Function Call
                if (!isAtEnd()
                        && peek().getType() == TokenType.SYMBOL
                        && peek().getValue().equals("(")) {

                    parseFunctionCall(identifier);
                }
                else if (!isAtEnd()
                        && peek().getType() == TokenType.OPERATOR
                        && peek().getValue().equals("++")) {

                    advance();

                    System.out.println(
                            "Increment:"
                    );

                    System.out.println(
                            identifier.getValue() + "++"
                    );

                    System.out.println();
                }
                else if (!isAtEnd()
                        && peek().getType() == TokenType.OPERATOR
                        && peek().getValue().equals("--")) {

                    advance();

                    System.out.println(
                            "Decrement:"
                    );

                    System.out.println(
                            identifier.getValue() + "--"
                    );

                    System.out.println();
                }
            }
            else {
                advance();
            }
        }
        checkCircularDependency();
        if (mainCount == 0) {

            System.out.println(
                    "Error: No main function found"
            );
        }
    }

    private void parseVariable(Token typeToken) {

        if (!match(TokenType.IDENTIFIER)) {
            return;
        }

        Token nameToken = previous();

        System.out.println("Variable:");
        System.out.println("Type = " + typeToken.getValue());
        System.out.println("Name = " + nameToken.getValue());

        if (!isAtEnd()
                && peek().getType() == TokenType.OPERATOR
                && peek().getValue().equals("=")) {

            advance();

            if (!isAtEnd()) {

                Token valueToken = advance();

                System.out.println(
                        "Value = "
                                + valueToken.getValue()
                );
            }
        }

        System.out.println();
    }

    private boolean match(TokenType type) {

        if (check(type)) {
            advance();
            return true;
        }

        return false;
    }

    private boolean check(TokenType type) {

        if (isAtEnd()) {
            return false;
        }

        return peek().getType() == type;
    }

    private Token advance() {

        if (!isAtEnd()) {
            current++;
        }

        return previous();
    }

    private void enterScope() {

        scopes.push(new java.util.HashSet<>());
    }

    private void exitScope() {

        if (!scopes.isEmpty()) {
            scopes.pop();
        }
    }
    private boolean isAtEnd() {
        return current >= tokens.size();
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
    private void parseClass() {

        if (!match(TokenType.IDENTIFIER)) {
            return;
        }

        Token className = previous();
        currentClass = className.getValue();
        if (classes.contains(className.getValue())) {

            System.out.println(
                    "Error: Class "
                            + className.getValue()
                            + " already defined"
            );

            System.out.println();

            return;
        }

        classes.add(className.getValue());

        System.out.println(
                "Class: "
                        + className.getValue()
        );

        System.out.println();
    }
    private void parseVariableOrFunction(Token typeToken) {

        if (!match(TokenType.IDENTIFIER)) {
            return;
        }

        Token nameToken = previous();
        if (currentClass != null &&
                Character.isUpperCase(
                        typeToken.getValue().charAt(0))) {

            classDependencies.put(
                    currentClass,
                    typeToken.getValue()
            );
        }

        if (scopes.peek().contains(nameToken.getValue())) {

            System.out.println(
                    "Error: Variable "
                            + nameToken.getValue()
                            + " already defined"
            );

            System.out.println();

            return;
        }

        if (!isAtEnd()
                && peek().getType() == TokenType.SYMBOL
                && peek().getValue().equals("(")) {

            parseFunction(typeToken, nameToken);
            return;
        }

        if (!insideFor) {
            scopes.peek().add(nameToken.getValue());
        }
        System.out.println("Variable:");
        System.out.println("Type = " + typeToken.getValue());
        System.out.println("Name = " + nameToken.getValue());
        variableTypes.put(
                nameToken.getValue(),
                typeToken.getValue()
        );

        if (!isAtEnd()
                && peek().getType() == TokenType.OPERATOR
                && peek().getValue().equals("=")) {

            advance();
            int expressionStart = current;

            currentAST = buildASTExpression();

            System.out.println("AST:");
            currentAST.print("");

            System.out.println();

            if (typeToken.getValue().equals("int")) {

                int savedCurrent = current;

                current = expressionStart;

                int result = evaluateExpression();

                current = savedCurrent;

                System.out.println(
                        "Expression Result = "
                                + result
                );
            }
        }
        insideFor = false;
        System.out.println();
    }
    private void parseFunction(Token returnType,
                               Token functionName) {
        if (functionName.getValue().equals("main")) {

            mainCount++;

            if (!returnType.getValue().equals("void")) {

                System.out.println(
                        "Error: main function must have void return type"
                );
            }

            if (mainCount > 1) {

                System.out.println(
                        "Error: Multiple main functions found"
                );
            }
        }
        if (functions.contains(functionName.getValue())) {

            System.out.println(
                    "Error: Function "
                            + functionName.getValue()
                            + " already defined"
            );

            System.out.println();

            return;
        }

        functions.add(functionName.getValue());

        System.out.println("Function:");
        System.out.println(
                "Return Type = "
                        + returnType.getValue()
        );

        System.out.println(
                "Name = "
                        + functionName.getValue()
        );

        System.out.println("Parameters:");
        enterScope();

        functionScopePending = true;
        advance(); // consume (
        boolean hasParameters = false;
        java.util.List<String> parameterTypes =
                new java.util.ArrayList<>();

        while (!isAtEnd()) {

            if (peek().getType() == TokenType.SYMBOL &&
                    peek().getValue().equals(")")) {

                advance();
                break;
            }

            if (peek().getType() == TokenType.KEYWORD) {
                hasParameters = true;

                Token paramType = advance();

                if (match(TokenType.IDENTIFIER)) {

                    Token paramName = previous();

                    System.out.println(
                            paramType.getValue()
                                    + " "
                                    + paramName.getValue()
                    );
                    scopes.peek().add(paramName.getValue());
                    parameterTypes.add(
                            paramType.getValue()
                    );
                }
            }
            else {
                advance();
            }
        }
        if (functionName.getValue().equals("main")
                && hasParameters) {

            System.out.println(
                    "Error: main function cannot have parameters"
            );
        }
        functionParameters.put(
                functionName.getValue(),
                parameterTypes
        );

        System.out.println();
    }
    private void parseFunctionCall(Token functionName) {
        if (!functions.contains(functionName.getValue())) {

            System.out.println(
                    "Error: Function "
                            + functionName.getValue()
                            + " not defined"
            );

            System.out.println();

            return;
        }

        System.out.println("Function Call:");
        System.out.println(functionName.getValue());

        System.out.println("Arguments:");

        advance(); // consume (

        java.util.List<String> passedArguments =
                new java.util.ArrayList<>();
        java.util.List<String> passedTypes =
                new java.util.ArrayList<>();

        int argumentCount = 0;
        while (!isAtEnd()) {

            if (peek().getType() == TokenType.SYMBOL &&
                    peek().getValue().equals(")")) {

                advance();
                break;
            }

            if (peek().getType() == TokenType.INTEGER ||
                    peek().getType() == TokenType.FLOAT ||
                    peek().getType() == TokenType.DOUBLE ||
                    peek().getType() == TokenType.IDENTIFIER ||
                    peek().getType() == TokenType.STRING) {

                Token arg = advance();

                System.out.println(arg.getValue());

                passedArguments.add(
                        arg.getValue()
                );
                if (arg.getType() == TokenType.INTEGER) {

                    passedTypes.add("int");
                }
                else if (arg.getType() == TokenType.FLOAT) {

                    passedTypes.add("float");
                }
                else if (arg.getType() == TokenType.DOUBLE) {

                    passedTypes.add("double");
                }
                else if (arg.getType() == TokenType.STRING) {

                    passedTypes.add("string");
                }
                else if (arg.getType() == TokenType.IDENTIFIER) {

                    if (variableTypes.containsKey(
                            arg.getValue())) {

                        passedTypes.add(
                                variableTypes.get(
                                        arg.getValue()
                                )
                        );
                    }
                }
                argumentCount++;
            }
            else {
                advance();
            }
        }

        if (functionParameters.containsKey(
                functionName.getValue())) {

            java.util.List<String> expectedTypes =
                    functionParameters.get(
                            functionName.getValue()
                    );

            int expectedCount =
                    expectedTypes.size();

            if (expectedCount != argumentCount) {

                System.out.println(
                        "Error: Function "
                                + functionName.getValue()
                                + " expects "
                                + expectedCount
                                + " arguments but got "
                                + argumentCount
                );
            }
            else {

                for (int i = 0;
                     i < expectedTypes.size();
                     i++) {

                    if (!expectedTypes.get(i)
                            .equals(passedTypes.get(i))) {

                        System.out.println(
                                "Error: Argument "
                                        + (i + 1)
                                        + " of function "
                                        + functionName.getValue()
                                        + " should be "
                                        + expectedTypes.get(i)
                                        + " but got "
                                        + passedTypes.get(i)
                        );
                    }
                }
            }
        }
        System.out.println();
    }
    private int evaluateExpression() {

        int value = evaluateTerm();

        while (!isAtEnd()
                && peek().getType() == TokenType.OPERATOR
                && (peek().getValue().equals("+")
                || peek().getValue().equals("-"))) {

            String op = advance().getValue();

            int right = evaluateTerm();

            if (op.equals("+")) {
                value += right;
            }
            else {
                value -= right;
            }
        }

        return value;
    }
    private int evaluateTerm() {

        int value = evaluateFactor();

        while (!isAtEnd()
                && peek().getType() == TokenType.OPERATOR
                && (peek().getValue().equals("*")
                || peek().getValue().equals("/"))) {

            String op = advance().getValue();

            int right = evaluateFactor();

            if (op.equals("*")) {
                value *= right;
            }
            else {
                value /= right;
            }
        }

        return value;
    }
    private int evaluateFactor() {

        if (!isAtEnd()
                && peek().getType() == TokenType.SYMBOL
                && peek().getValue().equals("(")) {

            advance(); // consume (

            int value = evaluateExpression();

            if (!isAtEnd()
                    && peek().getType() == TokenType.SYMBOL
                    && peek().getValue().equals(")")) {

                advance(); // consume )
            }

            return value;
        }

        Token token = advance();

        return Integer.parseInt(
                token.getValue()
        );
    }
    private ASTNode buildASTExpression() {

        ASTNode node = buildASTTerm();

        while (!isAtEnd()
                && peek().getType() == TokenType.OPERATOR
                && (peek().getValue().equals("+")
                || peek().getValue().equals("-"))) {

            String op = advance().getValue();

            ASTNode right =
                    buildASTTerm();

            node =
                    new BinaryOpNode(
                            op,
                            node,
                            right
                    );
        }

        return node;
    }
    private ASTNode buildASTTerm() {

        ASTNode node = buildASTFactor();

        while (!isAtEnd()
                && peek().getType() == TokenType.OPERATOR
                && (peek().getValue().equals("*")
                || peek().getValue().equals("/"))) {

            String op = advance().getValue();

            ASTNode right =
                    buildASTFactor();

            node =
                    new BinaryOpNode(
                            op,
                            node,
                            right
                    );
        }

        return node;
    }
    private ASTNode buildASTFactor() {

        if (!isAtEnd()
                && peek().getType() == TokenType.SYMBOL
                && peek().getValue().equals("(")) {

            advance(); // consume (

            ASTNode node = buildASTExpression();

            if (!isAtEnd()
                    && peek().getType() == TokenType.SYMBOL
                    && peek().getValue().equals(")")) {

                advance(); // consume )
            }

            return node;
        }

        Token number = advance();

        return new NumberNode(
                number.getValue()
        );
    }
    private void checkCircularDependency() {

        for (String classA : classDependencies.keySet()) {

            String classB =
                    classDependencies.get(classA);

            if (classDependencies.containsKey(classB)
                    && classDependencies.get(classB)
                    .equals(classA)) {

                System.out.println(
                        "Warning: Circular Dependency Detected"
                );

                return;
            }
        }
    }
    private void parseReturn() {

        System.out.println("Return Statement:");

        if (!isAtEnd()) {

            Token value = advance();

            System.out.println(value.getValue());
        }

        System.out.println();
    }
    private void parseCondition() {

        if (!isAtEnd()
                && peek().getType() == TokenType.SYMBOL
                && peek().getValue().equals("(")) {

            advance(); // (

            StringBuilder condition =
                    new StringBuilder();

            while (!isAtEnd()) {

                if (peek().getType() == TokenType.SYMBOL
                        && peek().getValue().equals(")")) {

                    advance();
                    break;
                }

                condition.append(
                        advance().getValue()
                );

                condition.append(" ");
            }

            System.out.println(
                    "Condition:"
            );

            System.out.println(
                    condition.toString().trim()
            );

            System.out.println();
        }
    }
    private void parseForHeader() {

        if (!isAtEnd()
                && peek().getType() == TokenType.SYMBOL
                && peek().getValue().equals("(")) {

            advance(); // (

            StringBuilder initialization =
                    new StringBuilder();

            StringBuilder condition =
                    new StringBuilder();

            StringBuilder update =
                    new StringBuilder();

            int section = 0;

            while (!isAtEnd()) {

                if (peek().getType() == TokenType.SYMBOL
                        && peek().getValue().equals(")")) {

                    advance();
                    break;
                }

                Token token = advance();

                if (token.getType() == TokenType.SYMBOL
                        && token.getValue().equals(";")) {

                    section++;
                    continue;
                }

                if (section == 0) {

                    initialization
                            .append(token.getValue())
                            .append(" ");
                }
                else if (section == 1) {

                    condition
                            .append(token.getValue())
                            .append(" ");
                }
                else {

                    update
                            .append(token.getValue())
                            .append(" ");
                }
            }

            System.out.println(
                    "Initialization:"
            );

            System.out.println(
                    initialization.toString().trim()
            );

            System.out.println();

            System.out.println(
                    "Condition:"
            );

            System.out.println(
                    condition.toString().trim()
            );

            System.out.println();

            System.out.println(
                    "Update:"
            );

            System.out.println(
                    update.toString().trim()
            );

            System.out.println();
        }
    }
}