import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;

/**
 * Online Examination System (Java Swing)
 *
 * Screens (CardLayout): Login -> Profile -> Exam -> Result -> (Logout) Login
 * Demo accounts: student1 / pass123   and   student2 / pass456
 *
 * Compile:  javac OnlineExamSystem.java
 * Run:      java OnlineExamSystem
 */
public class OnlineExamSystem extends JFrame {

    // ---------- Config ----------
    static final int EXAM_MINUTES = 30;          // change to e.g. 1 to test auto-submit quickly
    static final Color PRIMARY = new Color(37, 99, 235);
    static final Color BG = new Color(244, 247, 252);
    static final Color OK = new Color(22, 163, 74);
    static final Color BAD = new Color(220, 38, 38);

    // ---------- Data models ----------
    static class User {
        String username, password, displayName;
        User(String u, String p, String d) { username = u; password = p; displayName = d; }
    }

    static class Question {
        String text; String[] options; int correct;
        Question(String t, String[] o, int c) { text = t; options = o; correct = c; }
    }

    // ---------- State ----------
    private final Map<String, User> users = new HashMap<>();
    private final List<Question> questions = new ArrayList<>();
    private User currentUser;
    private int[] answers;               // -1 = unanswered
    private int current = 0;
    private int secondsLeft;
    private long startNanos;
    private boolean examRunning = false;
    private javax.swing.Timer countdown;

    // ---------- UI ----------
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    // login
    private JTextField loginUser;
    private JPasswordField loginPass;
    private JLabel loginMsg;

    // profile
    private JLabel profileHeader;
    private JTextField nameField;
    private JPasswordField newPass, confirmPass;
    private JLabel profileMsg;

    // exam
    private JLabel examWho, timerLabel, qCounter, qText;
    private JRadioButton[] optionBtns = new JRadioButton[4];
    private ButtonGroup group;
    private JButton prevBtn, nextBtn, submitBtn;
    private JProgressBar progress;

    // result
    private JLabel resultTitle, scoreLabel, timeLabel, breakdownLabel;
    private DefaultTableModel tableModel;

    public OnlineExamSystem() {
        super("Online Examination System");
        seedData();

        root.add(buildLogin(), "LOGIN");
        root.add(buildProfile(), "PROFILE");
        root.add(buildExam(), "EXAM");
        root.add(buildResult(), "RESULT");
        add(root);

        // Session management: intercept the window close button
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { handleClose(); }
        });

        setSize(820, 600);
        setMinimumSize(new Dimension(760, 560));
        setLocationRelativeTo(null);
        cards.show(root, "LOGIN");
    }

    // =====================================================================
    // Data
    // =====================================================================
    private void seedData() {
        users.put("student1", new User("student1", "pass123", "Student One"));
        users.put("student2", new User("student2", "pass456", "Student Two"));

        q("Which keyword is used to inherit a class in Java?",
                new String[]{"implements", "extends", "inherits", "super"}, 1);
        q("Which of these is NOT a primitive data type in Java?",
                new String[]{"int", "boolean", "String", "char"}, 2);
        q("What is the default value of an int instance variable?",
                new String[]{"null", "0", "1", "undefined"}, 1);
        q("Which collection does NOT allow duplicate elements?",
                new String[]{"ArrayList", "LinkedList", "HashSet", "Vector"}, 2);
        q("Which method is the entry point of a Java application?",
                new String[]{"start()", "run()", "main()", "init()"}, 2);
        q("Which Swing class is the top-level window container?",
                new String[]{"JPanel", "JFrame", "JLabel", "JButton"}, 1);
        q("Which keyword prevents a method from being overridden?",
                new String[]{"static", "abstract", "final", "private"}, 2);
        q("What does JVM stand for?",
                new String[]{"Java Variable Machine", "Java Virtual Machine", "Joint Visual Module", "Java Verified Memory"}, 1);
        q("Which exception is thrown when dividing an int by zero?",
                new String[]{"NullPointerException", "ArithmeticException", "NumberFormatException", "IOException"}, 1);
        q("Which Swing layout manager switches between screens like stacked cards?",
                new String[]{"BorderLayout", "GridLayout", "CardLayout", "FlowLayout"}, 2);
        q("Which interface must be implemented to run code in a new Thread?",
                new String[]{"Runnable", "Serializable", "Comparable", "Cloneable"}, 0);
        q("What is the size of the Java 'int' type?",
                new String[]{"8 bits", "16 bits", "32 bits", "64 bits"}, 2);
    }

    private void q(String t, String[] o, int c) { questions.add(new Question(t, o, c)); }

    // =====================================================================
    // Helpers
    // =====================================================================
    private JButton styledButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JPanel card(int w) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 237)),
                BorderFactory.createEmptyBorder(28, 34, 28, 34)));
        return p;
    }

    private GridBagConstraints gbc(int row) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = row;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.insets = new Insets(5, 0, 5, 0);
        return g;
    }

    private JPanel centered(JPanel inner) {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(BG);
        wrap.add(inner);
        return wrap;
    }

    // =====================================================================
    // 1. Login screen
    // =====================================================================
    private JPanel buildLogin() {
        JPanel c = card(400);
        int r = 0;

        JLabel title = new JLabel("<html><div style='text-align:center'>"
                + "<span style='font-size:22px;color:#2563EB'><b>Online Examination</b></span><br>"
                + "<span style='color:#64748B'>Sign in to continue</span></div></html>", SwingConstants.CENTER);
        c.add(title, gbc(r++));

        c.add(new JLabel("Username"), gbc(r++));
        loginUser = new JTextField();
        loginUser.setPreferredSize(new Dimension(300, 34));
        c.add(loginUser, gbc(r++));

        c.add(new JLabel("Password"), gbc(r++));
        loginPass = new JPasswordField();
        loginPass.setPreferredSize(new Dimension(300, 34));
        c.add(loginPass, gbc(r++));

        loginMsg = new JLabel(" ", SwingConstants.CENTER);
        loginMsg.setForeground(BAD);
        c.add(loginMsg, gbc(r++));

        JButton login = styledButton("Login", PRIMARY);
        login.addActionListener(e -> doLogin());
        c.add(login, gbc(r++));

        JLabel hint = new JLabel("<html><div style='text-align:center;color:#94A3B8;font-size:10px'>"
                + "Demo: student1 / pass123</div></html>", SwingConstants.CENTER);
        c.add(hint, gbc(r++));

        getRootPane().setDefaultButton(login);
        return centered(c);
    }

    private void doLogin() {
        String u = loginUser.getText().trim();
        String p = new String(loginPass.getPassword());
        User user = users.get(u);
        if (user != null && user.password.equals(p)) {
            currentUser = user;
            loginMsg.setText(" ");
            loginPass.setText("");
            showProfile();
        } else {
            loginMsg.setText("Invalid username or password");
        }
    }

    // =====================================================================
    // 2. Profile update screen
    // =====================================================================
    private JPanel buildProfile() {
        JPanel c = card(440);
        int r = 0;

        profileHeader = new JLabel("", SwingConstants.CENTER);
        c.add(profileHeader, gbc(r++));

        c.add(new JLabel("Display name"), gbc(r++));
        nameField = new JTextField();
        nameField.setPreferredSize(new Dimension(320, 34));
        c.add(nameField, gbc(r++));

        c.add(new JLabel("New password (leave blank to keep current)"), gbc(r++));
        newPass = new JPasswordField();
        newPass.setPreferredSize(new Dimension(320, 34));
        c.add(newPass, gbc(r++));

        c.add(new JLabel("Confirm new password"), gbc(r++));
        confirmPass = new JPasswordField();
        confirmPass.setPreferredSize(new Dimension(320, 34));
        c.add(confirmPass, gbc(r++));

        profileMsg = new JLabel(" ", SwingConstants.CENTER);
        c.add(profileMsg, gbc(r++));

        JButton save = styledButton("Save Changes", new Color(71, 85, 105));
        save.addActionListener(e -> saveProfile());
        c.add(save, gbc(r++));

        JButton start = styledButton("Start Exam", OK);
        start.addActionListener(e -> startExam());
        c.add(start, gbc(r++));

        JLabel info = new JLabel("<html><div style='text-align:center;color:#64748B;font-size:10px'>"
                + questions.size() + " questions &bull; " + EXAM_MINUTES
                + " minutes &bull; auto-submits when time is up</div></html>", SwingConstants.CENTER);
        c.add(info, gbc(r++));

        JButton logout = new JButton("Cancel & Logout");
        logout.setBorderPainted(false);
        logout.setContentAreaFilled(false);
        logout.setForeground(PRIMARY);
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> logout());
        c.add(logout, gbc(r++));

        return centered(c);
    }

    private void showProfile() {
        profileHeader.setText("<html><div style='text-align:center'><span style='font-size:20px;color:#2563EB'>"
                + "<b>Your Profile</b></span><br><span style='color:#64748B'>Update details before you begin</span></div></html>");
        nameField.setText(currentUser.displayName);
        newPass.setText("");
        confirmPass.setText("");
        profileMsg.setText(" ");
        cards.show(root, "PROFILE");
    }

    /** @return true if saved (or nothing invalid to save) */
    private boolean saveProfile() {
        String name = nameField.getText().trim();
        String p1 = new String(newPass.getPassword());
        String p2 = new String(confirmPass.getPassword());

        if (name.isEmpty()) {
            profileMsg.setForeground(BAD);
            profileMsg.setText("Display name cannot be empty");
            return false;
        }
        if (!p1.isEmpty()) {
            if (p1.length() < 4) {
                profileMsg.setForeground(BAD);
                profileMsg.setText("Password must be at least 4 characters");
                return false;
            }
            if (!p1.equals(p2)) {
                profileMsg.setForeground(BAD);
                profileMsg.setText("Passwords do not match");
                return false;
            }
            currentUser.password = p1;
        }
        currentUser.displayName = name;
        newPass.setText("");
        confirmPass.setText("");
        profileMsg.setForeground(OK);
        profileMsg.setText("Profile updated successfully");
        return true;
    }

    // =====================================================================
    // 3. Exam screen
    // =====================================================================
    private JPanel buildExam() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(BG);
        p.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        // --- top bar: user + timer ---
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 237)),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        examWho = new JLabel();
        timerLabel = new JLabel("30:00", SwingConstants.RIGHT);
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 26));
        timerLabel.setForeground(PRIMARY);
        top.add(examWho, BorderLayout.WEST);
        top.add(timerLabel, BorderLayout.EAST);

        // --- question card ---
        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 237)),
                BorderFactory.createEmptyBorder(22, 26, 22, 26)));

        JPanel qHead = new JPanel(new BorderLayout(0, 8));
        qHead.setOpaque(false);
        qCounter = new JLabel();
        progress = new JProgressBar(0, 100);
        progress.setStringPainted(false);
        progress.setForeground(PRIMARY);
        qText = new JLabel();
        qHead.add(qCounter, BorderLayout.NORTH);
        qHead.add(progress, BorderLayout.CENTER);
        qHead.add(qText, BorderLayout.SOUTH);
        body.add(qHead, BorderLayout.NORTH);

        JPanel opts = new JPanel(new GridLayout(4, 1, 0, 10));
        opts.setOpaque(false);
        group = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            JRadioButton rb = new JRadioButton();
            rb.setFont(new Font("SansSerif", Font.PLAIN, 15));
            rb.setOpaque(true);
            rb.setBackground(new Color(248, 250, 252));
            rb.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 232, 240)),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            rb.addActionListener(e -> answers[current] = idx);
            group.add(rb);
            opts.add(rb);
            optionBtns[i] = rb;
        }
        body.add(opts, BorderLayout.CENTER);

        // --- bottom nav ---
        JPanel nav = new JPanel(new BorderLayout());
        nav.setOpaque(false);
        prevBtn = styledButton("\u2190 Previous", new Color(71, 85, 105));
        nextBtn = styledButton("Next \u2192", PRIMARY);
        submitBtn = styledButton("Submit Exam", BAD);
        prevBtn.addActionListener(e -> goTo(current - 1));
        nextBtn.addActionListener(e -> goTo(current + 1));
        submitBtn.addActionListener(e -> manualSubmit());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(prevBtn);
        left.add(nextBtn);
        nav.add(left, BorderLayout.WEST);
        nav.add(submitBtn, BorderLayout.EAST);

        p.add(top, BorderLayout.NORTH);
        p.add(body, BorderLayout.CENTER);
        p.add(nav, BorderLayout.SOUTH);
        return p;
    }

    private void startExam() {
        // Save any pending edits first; stop if they are invalid
        boolean wantsPassword = newPass.getPassword().length > 0 || confirmPass.getPassword().length > 0;
        if (!nameField.getText().trim().equals(currentUser.displayName) || wantsPassword) {
            if (!saveProfile()) return;
        }

        answers = new int[questions.size()];
        Arrays.fill(answers, -1);
        current = 0;
        secondsLeft = EXAM_MINUTES * 60;
        startNanos = System.nanoTime();
        examRunning = true;

        examWho.setText("<html><span style='color:#64748B'>Candidate</span><br><b>"
                + escape(currentUser.displayName) + "</b></html>");
        updateTimerLabel();
        goTo(0);
        cards.show(root, "EXAM");

        if (countdown != null) countdown.stop();
        countdown = new javax.swing.Timer(1000, e -> tick());
        countdown.start();
    }

    private void tick() {
        secondsLeft--;
        updateTimerLabel();
        if (secondsLeft <= 0) {
            countdown.stop();
            JOptionPane.showMessageDialog(this, "Time is up! Your exam is being submitted automatically.",
                    "Time Over", JOptionPane.INFORMATION_MESSAGE);
            finishExam();
        }
    }

    private void updateTimerLabel() {
        int m = Math.max(secondsLeft, 0) / 60, s = Math.max(secondsLeft, 0) % 60;
        timerLabel.setText(String.format("\u23F1 %02d:%02d", m, s));
        timerLabel.setForeground(secondsLeft <= 60 ? BAD : PRIMARY);
    }

    private void goTo(int index) {
        if (index < 0 || index >= questions.size()) return;
        current = index;
        Question q = questions.get(index);

        qCounter.setText("<html><span style='color:#64748B'>Question <b>" + (index + 1)
                + "</b> of " + questions.size() + "</span></html>");
        progress.setValue((int) (100.0 * (index + 1) / questions.size()));
        qText.setText("<html><body style='width:560px'><span style='font-size:16px'><b>"
                + escape(q.text) + "</b></span></body></html>");

        group.clearSelection();
        for (int i = 0; i < 4; i++) {
            optionBtns[i].setText("<html><body style='width:520px'><b>"
                    + (char) ('A' + i) + ".</b>&nbsp; " + escape(q.options[i]) + "</body></html>");
        }
        if (answers[index] >= 0) optionBtns[answers[index]].setSelected(true);

        prevBtn.setEnabled(index > 0);
        nextBtn.setEnabled(index < questions.size() - 1);
    }

    private void manualSubmit() {
        long unanswered = Arrays.stream(answers).filter(a -> a == -1).count();
        String msg = unanswered > 0
                ? "You have " + unanswered + " unanswered question(s).\nSubmit the exam anyway?"
                : "Are you sure you want to submit your exam?";
        int choice = JOptionPane.showConfirmDialog(this, msg, "Confirm Submission",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) finishExam();
    }

    // =====================================================================
    // 4. Result screen
    // =====================================================================
    private JPanel buildResult() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(BG);
        p.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JPanel summary = new JPanel(new GridLayout(4, 1, 0, 4));
        summary.setBackground(Color.WHITE);
        summary.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 237)),
                BorderFactory.createEmptyBorder(16, 22, 16, 22)));
        resultTitle = new JLabel();
        scoreLabel = new JLabel();
        timeLabel = new JLabel();
        breakdownLabel = new JLabel();
        summary.add(resultTitle);
        summary.add(scoreLabel);
        summary.add(timeLabel);
        summary.add(breakdownLabel);

        tableModel = new DefaultTableModel(new String[]{"#", "Question", "Your Answer", "Correct Answer", "Result"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(26);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);
        table.getColumnModel().getColumn(4).setMaxWidth(90);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                                     boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (col == 4 && !sel) {
                    String s = String.valueOf(v);
                    c.setForeground(s.equals("Correct") ? OK : s.equals("Wrong") ? BAD : new Color(217, 119, 6));
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if (!sel) {
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });
        p.add(summary, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton logout = styledButton("Logout", PRIMARY);
        logout.addActionListener(e -> logout());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setOpaque(false);
        south.add(logout);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private void finishExam() {
        if (countdown != null) countdown.stop();
        examRunning = false;

        long elapsedSec = Math.min((System.nanoTime() - startNanos) / 1_000_000_000L, EXAM_MINUTES * 60L);
        int correct = 0, wrong = 0, skipped = 0;
        tableModel.setRowCount(0);

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            String yours, status;
            if (answers[i] == -1) { yours = "\u2014"; status = "Skipped"; skipped++; }
            else {
                yours = (char) ('A' + answers[i]) + ". " + q.options[answers[i]];
                if (answers[i] == q.correct) { status = "Correct"; correct++; }
                else { status = "Wrong"; wrong++; }
            }
            String right = (char) ('A' + q.correct) + ". " + q.options[q.correct];
            tableModel.addRow(new Object[]{i + 1, q.text, yours, right, status});
        }

        int total = questions.size();
        int pct = Math.round(100f * correct / total);
        resultTitle.setText("<html><span style='font-size:18px;color:#2563EB'><b>Exam Result \u2013 "
                + escape(currentUser.displayName) + "</b></span></html>");
        scoreLabel.setText("<html><span style='font-size:15px'>Score: <b>" + correct + " out of " + total
                + "</b> (" + pct + "%)</span></html>");
        timeLabel.setText("<html><span style='font-size:13px'>Time taken: <b>"
                + String.format("%02d:%02d", elapsedSec / 60, elapsedSec % 60)
                + "</b> of " + EXAM_MINUTES + ":00</span></html>");
        breakdownLabel.setText("<html><span style='font-size:13px'>"
                + "<span style='color:#16A34A'><b>" + correct + " correct</b></span> &nbsp;|&nbsp; "
                + "<span style='color:#DC2626'><b>" + wrong + " incorrect</b></span> &nbsp;|&nbsp; "
                + "<span style='color:#D97706'><b>" + skipped + " unanswered</b></span></span></html>");

        cards.show(root, "RESULT");
    }

    // =====================================================================
    // Session management
    // =====================================================================
    private void handleClose() {
        if (examRunning) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to quit?\nYour exam will be submitted with the answers so far.",
                    "Quit Exam", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                finishExam();   // submit what the student has, then close
                dispose();
                System.exit(0);
            }
        } else {
            dispose();
            System.exit(0);
        }
    }

    private void logout() {
        if (countdown != null) countdown.stop();
        examRunning = false;
        currentUser = null;
        loginUser.setText("");
        loginPass.setText("");
        loginMsg.setText(" ");
        cards.show(root, "LOGIN");
        loginUser.requestFocusInWindow();
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // =====================================================================
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new OnlineExamSystem().setVisible(true));
    }
}
