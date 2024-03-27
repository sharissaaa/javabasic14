import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class QuizGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private JButton userRegistrationBtn, loginBtn, exitBtn;
    private JPanel panel;
    private Connection con;

    public QuizGUI() {
        setTitle("Quiz");
        setSize(400, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1)); // 3 rows, 1 column
        userRegistrationBtn = new JButton("User Registration");
        loginBtn = new JButton("Login");
        exitBtn = new JButton("Exit");

        panel.add(userRegistrationBtn);
        panel.add(loginBtn);
        panel.add(exitBtn);
        add(panel);

        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3307/ems_db?CharacterEncoding=utf8", "root", "");
        } catch (ClassNotFoundException | SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
            System.exit(1);
        }

        userRegistrationBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                userRegistration();
            }
        });

        loginBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });

        exitBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (con != null) con.close();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
                }
                System.exit(0);
            }
        });
    }

    public String getUserId(String username) {
        try (PreparedStatement pst = con.prepareStatement("SELECT id FROM tbl_reg WHERE uname = ?")) {
            pst.setString(1, username);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("id");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
        return null; // Return null if user ID not found
    }

    public void userRegistration() {
        String username = JOptionPane.showInputDialog("Enter Username:");
        if (username == null) return; // User canceled

        String password = JOptionPane.showInputDialog("Enter Password:");
        if (password == null) return; // User canceled

        try (PreparedStatement pst = con.prepareStatement("INSERT INTO tbl_reg (uname, password) VALUES (?, ?)")) {
            pst.setString(1, username);
            pst.setString(2, password);
            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "User registered successfully");
            } else {
                JOptionPane.showMessageDialog(null, "Failed to register user");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

    public void login() {
        String username = JOptionPane.showInputDialog("Enter Username:");
        if (username == null) return; // User canceled

        String password = JOptionPane.showInputDialog("Enter Password:");
        if (password == null) return; // User canceled

        try (PreparedStatement pst = con.prepareStatement("SELECT * FROM tbl_reg WHERE uname = ? AND password = ?")) {
            pst.setString(1, username);
            pst.setString(2, password);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    JOptionPane.showMessageDialog(null, "Login successful");
                    displayQuiz(username);
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid username or password");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

    public void displayQuiz(String username) {
        String userId = getUserId(username); // Get the user's ID from tbl_reg
    
        if (userId == null) {
            JOptionPane.showMessageDialog(null, "User ID not found");
            return;
        }
    
        String[] questions = {
            "1. What is the capital of France?\n(a) London\n(b) Paris\n(c) Rome\n(d) Berlin",
            "2. Who wrote 'Romeo and Juliet'?\n(a) William Shakespeare\n(b) Charles Dickens\n(c) Jane Austen\n(d) Mark Twain",
            "3. What is the chemical symbol for water?\n(a) Wa\n(b) Wt\n(c) H2O\n(d) W"
        };
    
        String[] answers = {"b", "a", "c"};
        int score = 0;
    
        JOptionPane.showMessageDialog(null, "Welcome, " + username + "! Let's start the quiz.");
    
        for (int i = 0; i < questions.length; i++) {
            String userAnswer = JOptionPane.showInputDialog(null, questions[i], "Question " + (i + 1), JOptionPane.PLAIN_MESSAGE);
            if (userAnswer == null) return; // User canceled
    
            if (userAnswer.equalsIgnoreCase(answers[i])) {
                JOptionPane.showMessageDialog(null, "Correct!");
                score++;
            } else
            {
                JOptionPane.showMessageDialog(null, "Incorrect!");
            }
        }
    
        try (PreparedStatement pst = con.prepareStatement("INSERT INTO tbl_result (id, quiz_mark) VALUES (?, ?)")) {
            pst.setString(1, userId);
            pst.setInt(2, score);
            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "Score inserted into database successfully");
            } else {
                JOptionPane.showMessageDialog(null, "Failed to insert score into database");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    
        JOptionPane.showMessageDialog(null, "Quiz complete!\nYour score: " + score + "/" + questions.length);
    }
    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new QuizGUI().setVisible(true);
            }
        });
    }
}
