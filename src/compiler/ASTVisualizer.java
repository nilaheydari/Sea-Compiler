
package compiler;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class ASTVisualizer extends JPanel {

    private final ASTNode root;

    private final Color background =
            new Color(30, 41, 59);

    private final Color operatorColor =
            new Color(14, 116, 144);

    private final Color numberColor =
            new Color(13, 148, 136);

    private final Color lineColor =
            new Color(148, 163, 184);

    public ASTVisualizer(ASTNode root) {
        this.root = root;
        setBackground(background);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (root == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        int depth = getTreeDepth(root);
        int leaves = countLeaves(root);

        // Space required by the tree
        double treeWidth = Math.max(160, leaves * 95.0);
        double treeHeight = Math.max(100, depth * 95.0);

        // Available space inside the panel
        double availableWidth =
                Math.max(1, getWidth() - 60.0);

        double availableHeight =
                Math.max(1, getHeight() - 60.0);

        // Scale the tree to fit the panel
        double scale = Math.min(
                1.35,
                Math.min(
                        availableWidth / treeWidth,
                        availableHeight / treeHeight
                )
        );

        // Center the entire tree
        double offsetX =
                (getWidth() - treeWidth * scale) / 2.0;

        double offsetY =
                (getHeight() - treeHeight * scale) / 2.0;

        AffineTransform original =
                g2.getTransform();

        g2.translate(offsetX, offsetY);
        g2.scale(scale, scale);

        // Distribute leaves across the tree width
        drawNode(
                g2,
                root,
                0,
                treeWidth,
                45,
                95
        );

        g2.setTransform(original);
        g2.dispose();
    }

    private double drawNode(
            Graphics2D g,
            ASTNode node,
            double left,
            double right,
            double y,
            double levelGap
    ) {

        if (node instanceof NumberNode) {

            double x = (left + right) / 2.0;

            drawCircle(
                    g,
                    x,
                    y,
                    ((NumberNode) node).getValue(),
                    numberColor
            );

            return x;
        }

        if (node instanceof BinaryOpNode) {

            BinaryOpNode binary =
                    (BinaryOpNode) node;

            int leftLeaves =
                    countLeaves(binary.getLeft());

            int rightLeaves =
                    countLeaves(binary.getRight());

            double split = left +
                    (right - left) *
                            leftLeaves /
                            (leftLeaves + rightLeaves);

            double leftX = drawNode(
                    g,
                    binary.getLeft(),
                    left,
                    split,
                    y + levelGap,
                    levelGap
            );

            double rightX = drawNode(
                    g,
                    binary.getRight(),
                    split,
                    right,
                    y + levelGap,
                    levelGap
            );

            double x = (leftX + rightX) / 2.0;

            // Draw branches
            g.setColor(lineColor);
            g.setStroke(new BasicStroke(2));

            g.drawLine(
                    (int) x,
                    (int) y,
                    (int) leftX,
                    (int) (y + levelGap)
            );

            g.drawLine(
                    (int) x,
                    (int) y,
                    (int) rightX,
                    (int) (y + levelGap)
            );

            drawCircle(
                    g,
                    x,
                    y,
                    binary.getOperator(),
                    operatorColor
            );

            return x;
        }

        return (left + right) / 2.0;
    }

    private int getTreeDepth(ASTNode node) {

        if (node instanceof BinaryOpNode) {

            BinaryOpNode binary =
                    (BinaryOpNode) node;

            return 1 + Math.max(
                    getTreeDepth(binary.getLeft()),
                    getTreeDepth(binary.getRight())
            );
        }

        return 1;
    }

    private int countLeaves(ASTNode node) {

        if (node instanceof BinaryOpNode) {

            BinaryOpNode binary =
                    (BinaryOpNode) node;

            return countLeaves(binary.getLeft()) +
                    countLeaves(binary.getRight());
        }

        return 1;
    }

    private void drawCircle(
            Graphics2D g,
            double x,
            double y,
            String text,
            Color color
    ) {

        int diameter = 46;

        g.setColor(color);

        g.fillOval(
                (int) x - diameter / 2,
                (int) y - diameter / 2,
                diameter,
                diameter
        );

        g.setColor(Color.WHITE);

        g.setFont(
                new Font("SansSerif", Font.BOLD, 16)
        );

        FontMetrics metrics =
                g.getFontMetrics();

        int textX =
                (int) x -
                        metrics.stringWidth(text) / 2;

        int textY =
                (int) y +
                        (metrics.getAscent() -
                                metrics.getDescent()) / 2;

        g.drawString(text, textX, textY);
    }
}
