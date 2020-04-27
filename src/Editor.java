import Interpreter.Interpreter;
import Lexer.Lexer;
import Parser.Parser;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Stack;

public class Editor extends JFrame implements ActionListener {
    // Text component
    JTextArea t;

    // Frame
    JFrame f;

    // Constructor
    Editor() {
        // Create a frame
        f = new JFrame("Editor");
        f.setDefaultCloseOperation(EXIT_ON_CLOSE);

        try {
            // Set metl look and feel
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");

            // Set theme to ocean
            MetalLookAndFeel.setCurrentTheme(new OceanTheme());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Text component
        t = new JTextArea();
        t.getDocument().addDocumentListener(new MyDocumentListener());
        t.getDocument().putProperty("name", "Text Area");
        // Create a menubar
        JMenuBar mb = new JMenuBar();

        // Create amenu for menu
        JMenu mFile = new JMenu("File");

        // Create menu items
        JMenuItem mi1 = new JMenuItem("New");
        JMenuItem mi2 = new JMenuItem("Open");
        JMenuItem mi3 = new JMenuItem("Save");
        JMenuItem mi9 = new JMenuItem("Print");

        // Add action listener
        mi1.addActionListener(this);
        mi2.addActionListener(this);
        mi3.addActionListener(this);
        mi9.addActionListener(this);

        mFile.add(mi1);
        mFile.add(mi2);
        mFile.add(mi3);
        mFile.add(mi9);

        // Create amenu for menu
        JMenu mEdit = new JMenu("Edit");

        // Create menu items
        JMenuItem mi4 = new JMenuItem("cut");
        JMenuItem mi5 = new JMenuItem("copy");
        JMenuItem mi6 = new JMenuItem("paste");

        // Add action listener
        mi4.addActionListener(this);
        mi5.addActionListener(this);
        mi6.addActionListener(this);

        mEdit.add(mi4);
        mEdit.add(mi5);
        mEdit.add(mi6);

        JMenu mRun = new JMenu("Run");

        JMenuItem mi7 = new JMenuItem("Run...");

        mi7.addActionListener(this);

        mRun.add(mi7);

        mb.add(mFile);
        mb.add(mEdit);
        mb.add(mRun);

        f.setJMenuBar(mb);
        f.add(t);
        f.setSize(500, 500);
        f.setVisible(true);
    }

    class MyDocumentListener implements DocumentListener{

        public void insertUpdate(DocumentEvent e) {
            updateLog(e);
        }

        public void removeUpdate(DocumentEvent e) {
            updateLog(e);
        }

        public void changedUpdate(DocumentEvent e) { }

        private boolean isIF = false;
        private boolean bool;
        private final ArrayList<Character> characters = new ArrayList<>();

        //============================================================================
        //Логика данного метода была такая создаётся список токенов при каждом вводе
        //пользователя, когда пользователь вводит if метод сохраняет это состояние
        //в отдельный список (TokensWithLastTokenIsIf) и меняет знаечение булевой
        //переменной isTokensWithLastTokenIsIf на true после чего из tokens удаляются
        //все другие токены, кроме токенов принадлежащие обрамляющему if это круглые
        //и фигурные скобки и выражения в них. Но реализовать грамотное условие для
        //проверки данного списка на вхождение всех токенов для обрамляющего if не смог.
        //P.S. На счёт синтаксическое дерева,то у меня не получалось создавать его
        //во время каждого пользовательского ввода, т.к. при его составление, сопровождается
        //множеством разных проверок на правильность составления этого дерева.
        //============================================================================

        //private final StringBuilder stringBuilder = new StringBuilder();
        //List<Token> TokensWithLastTokenIsIf = new ArrayList<Token>();
        //boolean isTokensWithLastTokenIsIf = false;
        /*
        public void TokensWithOnlyElementsIf(StringBuilder str) {
            Lexer lexer = Lexer.toLex(stringBuilder.toString());
            List<Token> tokens = lexer.TokensList();
            if(tokens.get(tokens.size() - 2).getTokenType().equals(TokenType.IF)) {
                TokensWithLastTokenIsIf = lexer.TokensList();
                isTokensWithLastTokenIsIf = true;
            }
            if(isTokensWithLastTokenIsIf) {
                tokens.removeAll(TokensWithLastTokenIsIf);
                for (int i = 0; i < tokens.size(); i++) {
                    if (tokens.get(i).getTokenType().equals(TokenType.LEFT_PAREN)) {
                        if (!tokens.get(i).getTokenType().equals(TokenType.RIGHT_PAREN)) continue;
                    }
                    else break;
                    if (tokens.get(i).getTokenType().equals(TokenType.LEFT_BRACE)) {
                        if(!tokens.get(i).getTokenType().equals(TokenType.RIGHT_BRACE)) continue;
                    }
                }
            }
            stringBuilder.setLength(0);
        }
        */
        public void updateLog(DocumentEvent e) throws StringIndexOutOfBoundsException {
            //TokensWithOnlyElementsIf(stringBuilder.append(t.getText()));
            char lastChar = ' ';
            char preLastChar = ' ';

            try {
                lastChar = t.getText().charAt(e.getOffset());
                preLastChar = t.getText().charAt(e.getOffset() - 1);
            }
            catch (StringIndexOutOfBoundsException ignored) {
            }

            if ((preLastChar + "" + lastChar).equals("if")) {
                isIF = true;
            }

            if (isIF) {
                try { characters.add((t.getText().length() - 1) - e.getOffset(), lastChar); }
                catch (IndexOutOfBoundsException ignored) { }

                String reverse = new StringBuffer(characters.toString()).reverse().toString();

                boolean tmp = isParenthesisMatch(reverse);
                if(!bool && tmp) {
                    bool = true;
                    JOptionPane.showMessageDialog(f, "if () {}");
                    isIF = false;
                    characters.clear();
                } else  {
                    bool = tmp;
                }
            }
        }
    }

    public static boolean isParenthesisMatch(String str) {
        boolean isBracketsBalance = false;
        boolean isParenthesesBalance = false;

        if (str.charAt(0) == '{')
            return false;

        Stack<Character> stack = new Stack<>();

        char c;
        for(int i = 0; i < str.length(); i++) {
            c = str.charAt(i);
            if(c == '(')
                stack.push(c);
            else if(c == '{')
                stack.push(c);
            else if(c == ')')
                if(stack.empty())
                    return false;
                else if(stack.peek() == '(') {
                    stack.pop();
                    isBracketsBalance = true;
                }
                else
                    return false;
            else if(c == '}')
                if(stack.empty())
                    return false;
                else if(stack.peek() == '{') {
                    stack.pop();
                    isParenthesesBalance = true;
                }
                else
                    return false;
        }
        return (isBracketsBalance && isParenthesesBalance);
    }


    public void actionPerformed(ActionEvent e)
    {
        String s = e.getActionCommand();

        switch (s) {
            case "cut":
                t.cut();
                break;
            case "copy":
                t.copy();
                break;
            case "paste":
                t.paste();
                break;
            case "Save": {
                // Create an object of JFileChooser class
                JFileChooser j = new JFileChooser("f:");

                // Invoke the showsSaveDialog function to show the save dialog
                int r = j.showSaveDialog(null);

                if (r == JFileChooser.APPROVE_OPTION) {

                    // Set the label to the path of the selected directory
                    File fi = new File(j.getSelectedFile().getAbsolutePath());

                    try {
                        // Create a file writer
                        FileWriter wr = new FileWriter(fi, false);

                        // Create buffered writer to write
                        BufferedWriter w = new BufferedWriter(wr);

                        // Write
                        w.write(t.getText());

                        w.flush();
                        w.close();
                    } catch (Exception evt) {
                        JOptionPane.showMessageDialog(f, evt.getMessage());
                    }
                }
                // If the user cancelled the operation
                else
                    JOptionPane.showMessageDialog(f, "the user cancelled the operation");
                break;
            }
            case "Print":
                try {
                    // print the file
                    t.print();
                } catch (Exception evt) {
                    JOptionPane.showMessageDialog(f, evt.getMessage());
                }
                break;
            case "Open": {
                // Create an object of JFileChooser class
                JFileChooser j = new JFileChooser("f:");

                // Invoke the showsOpenDialog function to show the save dialog
                int r = j.showOpenDialog(null);

                // If the user selects a file
                if (r == JFileChooser.APPROVE_OPTION) {
                    // Set the label to the path of the selected directory
                    File fi = new File(j.getSelectedFile().getAbsolutePath());

                    try {
                        // String
                        String s1;
                        StringBuilder sl;

                        // File reader
                        FileReader fr = new FileReader(fi);

                        // Buffered reader
                        BufferedReader br = new BufferedReader(fr);

                        // Initialize sl
                        sl = new StringBuilder(br.readLine());

                        // Take the input from the file
                        while ((s1 = br.readLine()) != null) {
                            sl.append("\n").append(s1);
                        }

                        // Set the text
                        t.setText(sl.toString());
                    } catch (Exception evt) {
                        JOptionPane.showMessageDialog(f, evt.getMessage());
                    }
                }
                // If the user cancelled the operation
                else
                    JOptionPane.showMessageDialog(f, "the user cancelled the operation");
                break;
            }
            case "New":
                t.setText("");

                break;
            case "close":
                f.setVisible(false);
                break;
            case "Run...":
                String path = "src\\Lexer\\LexerIn.txt";
                File file = new File(path);
                try (PrintWriter printWriter = new PrintWriter(file.getPath())) {
                    printWriter.println(t.getText());
                }
                catch (FileNotFoundException ee) {
                    ee.printStackTrace();
                }
                try {
                    Lexer lexer = Lexer.toLex();
                    Parser parser = new Parser(lexer.TokensList());
                    parser.AST(parser.parse());

                    Interpreter interpreter = Interpreter.toInterpret(parser.printAST());
                    JOptionPane.showMessageDialog(f, interpreter.printInterpret());
                }
                catch (FileNotFoundException ee) {
                    ee.printStackTrace();
                }
                break;

        }
    }

    public static void main(String[] args)
    {
        Editor e = new Editor();
        e.setSize(720, 480);
    }
}
