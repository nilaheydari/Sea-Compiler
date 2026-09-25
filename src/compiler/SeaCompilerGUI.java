
package compiler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

public class SeaCompilerGUI extends JFrame {

    private JTextArea codeEditor;
    private JTextArea outputArea;
    private JTable tokenTable;
    private JPanel astPanel;

    public SeaCompilerGUI() {
        setTitle("Sea Compiler Studio");
        setSize(1400, 850);
        setMinimumSize(new Dimension(1100, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Color background = new Color(15, 23, 42);
        Color panelColor = new Color(30, 41, 59);
        Color textColor = new Color(226, 232, 240);

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBackground(background);
        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );

        JLabel title = new JLabel("🌊 SEA COMPILER STUDIO");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(new Color(56, 189, 248));

        JButton compileButton = new JButton("Compile");
        compileButton.setBackground(new Color(20, 184, 166));
        compileButton.setForeground(Color.BLACK);
        compileButton.setFocusPainted(false);
        compileButton.addActionListener(e -> compileCode());

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);
        header.add(compileButton, BorderLayout.EAST);

        mainPanel.add(header, BorderLayout.NORTH);

        // 1. Source Editor
        codeEditor = new JTextArea(
                "void main() begin\n" +
                        "    int result = 20 + 2 * 3;\n" +
                        "end"
        );
        codeEditor.setFont(new Font("Monospaced", Font.PLAIN, 15));
        codeEditor.setBackground(panelColor);
        codeEditor.setForeground(textColor);
        codeEditor.setCaretColor(Color.WHITE);
        codeEditor.setTabSize(4);
        codeEditor.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        JScrollPane editorScroll = new JScrollPane(codeEditor);

        // 2. Token Inspector
        String[] columns = {"Type", "Value", "Line", "Column"};

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tokenTable = new JTable(model);
        tokenTable.setFont(new Font("Monospaced", Font.PLAIN, 13));
        tokenTable.setRowHeight(24);
        tokenTable.setBackground(panelColor);
        tokenTable.setForeground(textColor);
        tokenTable.setGridColor(new Color(51, 65, 85));
        tokenTable.setSelectionBackground(new Color(14, 116, 144));


        JScrollPane tokenScroll = new JScrollPane(tokenTable);


        tokenTable.getTableHeader().setBackground(
                new Color(51, 65, 85)
        );
        tokenTable.getTableHeader().setForeground(Color.WHITE);
        tokenTable.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 13)
        );
        tokenTable.getTableHeader().setReorderingAllowed(false);

        tokenTable.setShowVerticalLines(false);
        tokenTable.setShowHorizontalLines(true);
        tokenTable.setIntercellSpacing(new Dimension(0, 1));

        tokenTable.setFillsViewportHeight(true);
        tokenScroll.getViewport().setBackground(
                new Color(30, 41, 59)
        );


        // 3. AST Visualizer
        astPanel = new JPanel(new BorderLayout());
        astPanel.setBackground(panelColor);

        JLabel astPlaceholder = new JLabel(
                "AST will appear after compilation",
                SwingConstants.CENTER
        );
        astPlaceholder.setForeground(textColor);
        astPanel.add(astPlaceholder, BorderLayout.CENTER);

        // 4. Compiler Output
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        outputArea.setBackground(panelColor);
        outputArea.setForeground(textColor);
        outputArea.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        JScrollPane outputScroll = new JScrollPane(outputArea);

        // Four-panel layout
        JPanel grid = new JPanel(new GridLayout(2, 2, 12, 12));
        grid.setBackground(background);

        grid.add(createPanel("01  SOURCE EDITOR", editorScroll));
        grid.add(createPanel("02  TOKEN INSPECTOR", tokenScroll));
        grid.add(createPanel("03  AST VISUALIZER", astPanel));
        grid.add(createPanel("04  COMPILER OUTPUT", outputScroll));

        mainPanel.add(grid, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    private JPanel createPanel(String title, Component content) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(new Color(30, 41, 59));

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(51, 65, 85), 1
                        ),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                )
        );

        JLabel label = new JLabel(title);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(new Color(94, 234, 212));

        panel.add(label, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);

        return panel;
    }

    private void compileCode() {
        DefaultTableModel model =
                (DefaultTableModel) tokenTable.getModel();

        model.setRowCount(0);
        outputArea.setText("");

        astPanel.removeAll();

        try {
            String code = codeEditor.getText();

            Lexer lexer = new Lexer();
            List<Token> tokens = lexer.tokenize(code);

            for (Token token : tokens) {
                model.addRow(new Object[]{
                        token.getType(),
                        token.getValue(),
                        token.getLine(),
                        token.getColumn()
                });
            }

            Parser parser = new Parser(tokens);

            ByteArrayOutputStream buffer =
                    new ByteArrayOutputStream();

            PrintStream originalOut = System.out;

            try {
                System.setOut(new PrintStream(buffer));
                parser.parse();
            } finally {
                System.setOut(originalOut);
            }


            String parserOutput = buffer.toString();

            java.util.regex.Matcher matcher =
                    java.util.regex.Pattern
                            .compile("Expression Result\\s*=\\s*([^\\r\\n]+)")
                            .matcher(parserOutput);

            if (matcher.find()) {
                outputArea.setText(
                        "EXPRESSION RESULT: " + matcher.group(1)
                                + "\n"
                                + "--------------------------------\n\n"
                                + parserOutput
                );
            } else {
                outputArea.setText(parserOutput);
            }

            outputArea.setCaretPosition(0);

            ASTNode root = parser.getCurrentAST();


            if (root != null) {
                ASTVisualizer visualizer =
                        new ASTVisualizer(root);

                astPanel.add(
                        visualizer,
                        BorderLayout.CENTER
                );
            } else {
                JLabel message = new JLabel(
                        "No expression AST generated",
                        SwingConstants.CENTER
                );
                message.setForeground(Color.LIGHT_GRAY);
                astPanel.add(message, BorderLayout.CENTER);
            }

        } catch (Exception ex) {
            outputArea.setText(
                    "Compilation error:\n" + ex.getMessage()
            );
        }

        astPanel.revalidate();
        astPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SeaCompilerGUI().setVisible(true);
        });
    }
}
