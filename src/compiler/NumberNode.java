package compiler;

public class NumberNode extends ASTNode {

    private String value;

    public NumberNode(String value) {
        this.value = value;
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + value);
    }
}