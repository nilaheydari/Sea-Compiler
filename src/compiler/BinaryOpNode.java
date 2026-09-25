package compiler;

public class BinaryOpNode extends ASTNode {

    private String operator;
    private ASTNode left;
    private ASTNode right;

    public BinaryOpNode(String operator,
                        ASTNode left,
                        ASTNode right) {

        this.operator = operator;
        this.left = left;
        this.right = right;
    }


    public String getOperator() {
        return operator;
    }

    public ASTNode getLeft() {
        return left;
    }

    public ASTNode getRight() {
        return right;
    }


    @Override
    public void print(String indent) {

        System.out.println(indent + operator);

        left.print(indent + "├── ");

        right.print(indent + "└── ");
    }
}