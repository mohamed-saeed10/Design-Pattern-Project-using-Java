package Quiz;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// ==========================================
// 1. FACTORY PATTERN (User & Questions)
// ==========================================
interface User { String getRole(); String getWelcomeMsg(); }
class Admin implements User { 
    public String getRole() { return "Admin"; } 
    public String getWelcomeMsg() { return "You have full access to manage quizzes, users, and settings."; }
}
class Student implements User { 
    public String getRole() { return "Student"; } 
    public String getWelcomeMsg() { return "Welcome! Ready to take your daily quiz?"; }
}

class UserFactory {
    public static User createUser(String email) {
        if (email.toLowerCase().contains("admin")) return new Admin();
        return new Student();
    }
}

class Question {
    String text; String[] opts; int correct;
    public Question(String t, String[] o, int c) { text=t; opts=o; correct=c; }
}

class QuestionFactory {
    public static List<Question> getQuizSet() {
        List<Question> q = new ArrayList<>();
        q.add(new Question("Which pattern is used for a single instance?", new String[]{"Factory", "Singleton", "Proxy", "Observer"}, 1));
        q.add(new Question("Factory pattern is a creational pattern.", new String[]{"True", "False"}, 0));
        q.add(new Question("Who manages quiz progress in this app?", new String[]{"User", "QuizManager", "Database", "JFrame"}, 1));
        // Add more questions to reach 10...
        Collections.shuffle(q);
        return q;
    }
}

// ==========================================
// 2. SINGLETON PATTERN (Managers)
// ==========================================
class QuizManager {
    private static QuizManager instance;
    private List<Question> quiz;
    private int index = 0;
    private QuizManager() {}
    public static QuizManager getInstance() {
        if (instance == null) instance = new QuizManager();
        return instance;
    }
    public void start() { quiz = QuestionFactory.getQuizSet(); index = 0; }
    public Question next() { return (index < quiz.size()) ? quiz.get(index++) : null; }
    public int getPos() { return index; }
}

class ScoreManager {
    private static ScoreManager instance;
    private int score = 0;
    private ScoreManager() {}
    public static ScoreManager getInstance() {
        if (instance == null) instance = new ScoreManager();
        return instance;
    }
    public void reset() { score = 0; }
    public void addPoint() { score++; }
    public int getScore() { return score; }
}

// ==========================================
// 3. THE GUI SYSTEM
// ==========================================
public class QuizSystem extends JFrame {
    private JPanel cards;
    private CardLayout cl = new CardLayout();
    private User currentUser;
    
    // Quiz View Components
    private JLabel qText, qProg;
    private JRadioButton[] opts = new JRadioButton[4];
    private ButtonGroup group = new ButtonGroup();
    private Question currentQ;

    public QuizSystem() {
        setTitle("Quiz Time! Pro Edition");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        cards = new JPanel(cl);
        cards.add(createLogin(), "LOGIN");
        add(cards);
    }

    // --- LOGIN (Your Design) ---
    private JPanel createLogin() {
        JPanel main = new JPanel(new GridLayout(1, 2));
        JPanel left = new JPanel(new GridBagLayout());
        left.setBackground(new Color(78, 52, 199));
        JLabel logo = new JLabel("QUIZ TIME!", JLabel.CENTER);
        logo.setFont(new Font("SansSerif", Font.BOLD, 45));
        logo.setForeground(Color.WHITE);
        left.add(logo);

        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(new Color(245, 247, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField emailIn = new JTextField(15);
        JPasswordField passIn = new JPasswordField(15);
        JCheckBox showPass = new JCheckBox("Show Password");
        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(new Color(46, 204, 113)); loginBtn.setForeground(Color.WHITE);
        JButton backBtn = new JButton("Back");
        backBtn.setBackground(new Color(192, 57, 43)); backBtn.setForeground(Color.WHITE);

        showPass.addActionListener(e -> passIn.setEchoChar(showPass.isSelected() ? (char)0 : '•'));

        loginBtn.addActionListener(e -> {
            String email = emailIn.getText();
            String pass = new String(passIn.getPassword());
            if (email.contains("@") && pass.length() >= 8 && !pass.equals(pass.toLowerCase())) {
                currentUser = UserFactory.createUser(email);
                cards.add(createDashboard(), "DASH");
                cl.show(cards, "DASH");
            } else {
                JOptionPane.showMessageDialog(this, "Check Email (@) or Password (8+ chars & 1 Capital)");
            }
        });

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        right.add(new JLabel("Admin Login Panel", 0), gbc);
        gbc.gridwidth = 1; gbc.gridy = 1; right.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; right.add(emailIn, gbc);
        gbc.gridx = 0; gbc.gridy = 2; right.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; right.add(passIn, gbc);
        gbc.gridy = 3; right.add(showPass, gbc);
        gbc.gridx = 0; gbc.gridy = 4; right.add(loginBtn, gbc);
        gbc.gridx = 1; right.add(backBtn, gbc);
        
        main.add(left); main.add(right);
        return main;
    }

    // --- DASHBOARD (From your image) ---
    private JPanel createDashboard() {
        JPanel p = new JPanel(new BorderLayout());
        
        // Sidebar
        JPanel sidebar = new JPanel(new GridLayout(8, 1, 5, 5));
        sidebar.setBackground(new Color(52, 73, 94));
        sidebar.setPreferredSize(new Dimension(200, 0));
        
        String[] btns = {"Add Question", "Edit Quiz", "Delete Quiz", "View Quiz", "View Users", "View Results", "Save & Exit"};
        Color[] colors = {Color.GREEN, Color.CYAN, Color.RED, Color.MAGENTA, Color.ORANGE, Color.YELLOW, Color.GRAY};
        
        for (int i = 0; i < btns.length; i++) {
            JButton b = new JButton(btns[i]);
            b.setBackground(colors[i % colors.length]);
            if(btns[i].equals("View Quiz")) {
                b.addActionListener(e -> {
                    QuizManager.getInstance().start();
                    ScoreManager.getInstance().reset();
                    cards.add(createQuizView(), "QUIZ");
                    loadNext();
                    cl.show(cards, "QUIZ");
                });
            }
            sidebar.add(b);
        }

        // Main Content
        JPanel main = new JPanel(new BorderLayout());
        JLabel welcome = new JLabel("Welcome, " + currentUser.getRole() + "!", 0);
        welcome.setFont(new Font("SansSerif", Font.BOLD, 24));
        JTextArea desc = new JTextArea(currentUser.getWelcomeMsg());
        desc.setEditable(false);
        main.add(welcome, BorderLayout.NORTH);
        main.add(desc, BorderLayout.CENTER);

        p.add(sidebar, BorderLayout.WEST);
        p.add(main, BorderLayout.CENTER);
        return p;
    }

    private JPanel createQuizView() {
        JPanel p = new JPanel(new BorderLayout(20, 20));
        p.setBorder(BorderFactory.createEmptyBorder(30,30,30,30));
        qProg = new JLabel("", 4);
        qText = new JLabel("");
        JPanel oPanel = new JPanel(new GridLayout(4,1));
        for(int i=0; i<4; i++) { opts[i] = new JRadioButton(); group.add(opts[i]); oPanel.add(opts[i]); }
        JButton sub = new JButton("Submit");
        sub.addActionListener(e -> {
            for(int i=0; i<4; i++) if(opts[i].isSelected() && i == currentQ.correct) ScoreManager.getInstance().addPoint();
            loadNext();
        });
        p.add(qProg, BorderLayout.NORTH); p.add(qText, BorderLayout.CENTER);
        JPanel btm = new JPanel(new BorderLayout()); btm.add(oPanel, 0); btm.add(sub, "South");
        p.add(btm, BorderLayout.SOUTH);
        return p;
    }

    private void loadNext() {
        currentQ = QuizManager.getInstance().next();
        if(currentQ != null) {
            qText.setText(currentQ.text);
            qProg.setText("Question " + QuizManager.getInstance().getPos() + "/10");
            for(int i=0; i<4; i++) {
                if(i < currentQ.opts.length) { opts[i].setText(currentQ.opts[i]); opts[i].setVisible(true); }
                else opts[i].setVisible(false);
            }
            group.clearSelection();
        } else {
            JOptionPane.showMessageDialog(this, "Finished! Score: " + ScoreManager.getInstance().getScore());
            cl.show(cards, "DASH");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new QuizSystem().setVisible(true));
    }
}