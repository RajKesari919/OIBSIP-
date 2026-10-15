import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

/**
 * Number Guessing Game (Swing GUI version)
 *
 * Pick a difficulty, type a guess in the text field and press Enter or the
 * Guess button. Shows Too High / Too Low / Correct, a live attempt counter,
 * a "You Lost!" message when attempts run out, a Play Again prompt, and a
 * running score history across rounds.
 */
public class NumberGuessingGameGUI extends JFrame {

    // ---------- Game logic (no Swing, so it can be tested on its own) ----------

    static final String[] LEVEL_NAMES = {"Easy", "Medium", "Hard"};
    static final int[] LEVEL_MAX = {50, 100, 200};
    static final int[] LEVEL_ATTEMPTS = {10, 7, 5};

    enum Result { TOO_HIGH, TOO_LOW, CORRECT, LOST }

    static class GameState {
        private final Random random = new Random();
        int level;
        int max;
        int maxAttempts;
        int secret;
        int attempts;
        boolean over;

        void start(int level) {
            this.level = level;
            this.max = LEVEL_MAX[level];
            this.maxAttempts = LEVEL_ATTEMPTS[level];
            this.secret = random.nextInt(max) + 1; // 1..max inclusive
            this.attempts = 0;
            this.over = false;
        }

        /** Process a valid in-range guess. */
        Result guess(int g) {
            attempts++;
            if (g == secret) {
                over = true;
                return Result.CORRECT;
            }
            if (attempts >= maxAttempts) {
                over = true;
                return Result.LOST;
            }
            return g > secret ? Result.TOO_HIGH : Result.TOO_LOW;
        }
    }

    // ---------- UI ----------

    private final GameState game = new GameState();
    private int round = 0;
    private int wins = 0;
    private int roundsPlayed = 0;

    private final JComboBox<String> levelBox = new JComboBox<>(new String[]{
            "Easy (1-50, 10 attempts)",
            "Medium (1-100, 7 attempts)",
            "Hard (1-200, 5 attempts)"});
    private final JLabel promptLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel attemptLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel feedbackLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JTextField guessField = new JTextField(8);
    private final JButton guessButton = new JButton("Guess");
    private final JButton newRoundButton = new JButton("New Round");
    private final JTextArea historyArea = new JTextArea(8, 30);
    private final JLabel winsLabel = new JLabel("Wins: 0 / 0 rounds");

    public NumberGuessingGameGUI() {
        super("Number Guessing Game");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        levelBox.setSelectedIndex(1); // Medium by default (set before listeners exist)
        buildUi();
        startRound();
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        // Title + difficulty
        JLabel title = new JLabel("Number Guessing Game", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        JPanel levelRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        levelRow.add(new JLabel("Difficulty:"));
        levelRow.add(levelBox);

        JPanel top = new JPanel(new GridLayout(2, 1, 0, 8));
        top.add(title);
        top.add(levelRow);
        root.add(top, BorderLayout.NORTH);

        // Centre: prompt, counter, input, feedback
        promptLabel.setFont(promptLabel.getFont().deriveFont(14f));
        attemptLabel.setFont(attemptLabel.getFont().deriveFont(Font.BOLD, 15f));
        feedbackLabel.setFont(feedbackLabel.getFont().deriveFont(Font.BOLD, 22f));
        guessField.setFont(guessField.getFont().deriveFont(18f));
        guessField.setHorizontalAlignment(JTextField.CENTER);

        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        inputRow.add(guessField);
        inputRow.add(guessButton);
        inputRow.add(newRoundButton);

        JPanel centre = new JPanel(new GridLayout(4, 1, 0, 8));
        centre.add(promptLabel);
        centre.add(attemptLabel);
        centre.add(inputRow);
        centre.add(feedbackLabel);
        root.add(centre, BorderLayout.CENTER);

        // Bottom: score history
        historyArea.setEditable(false);
        historyArea.setLineWrap(true);
        historyArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(historyArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Score history"));

        JPanel bottom = new JPanel(new BorderLayout(0, 4));
        bottom.add(scroll, BorderLayout.CENTER);
        bottom.add(winsLabel, BorderLayout.SOUTH);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);

        // Actions
        guessButton.addActionListener(e -> submitGuess());
        guessField.addActionListener(e -> submitGuess()); // Enter key
        newRoundButton.addActionListener(e -> startRound());
        levelBox.addActionListener(e -> {
            // Changing difficulty starts a fresh round, but only if the
            // current round has not been started with a guess yet or is over.
            if (game.attempts == 0 || game.over) {
                startRound();
            } else {
                int choice = JOptionPane.showConfirmDialog(this,
                        "Change difficulty and abandon this round?",
                        "Change difficulty", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    startRound();
                } else {
                    levelBox.setSelectedIndex(game.level); // revert
                }
            }
        });
        // Clear red error colour as soon as the user types again
        guessField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (!game.over) {
                    feedbackLabel.setForeground(UIManager.getColor("Label.foreground"));
                }
            }
        });
    }

    private void startRound() {
        round++;
        game.start(levelBox.getSelectedIndex());
        promptLabel.setText("Round " + round + ": guess a number between 1 and " + game.max);
        updateAttemptLabel();
        setFeedback(" ", null);
        guessField.setText("");
        guessField.setEnabled(true);
        guessButton.setEnabled(true);
        guessField.requestFocusInWindow();
    }

    private void updateAttemptLabel() {
        attemptLabel.setText("Attempts: " + game.attempts + " / " + game.maxAttempts
                + "   (remaining: " + (game.maxAttempts - game.attempts) + ")");
    }

    private void setFeedback(String text, Color color) {
        feedbackLabel.setText(text);
        feedbackLabel.setForeground(color != null ? color : UIManager.getColor("Label.foreground"));
    }

    private void submitGuess() {
        if (game.over) {
            return;
        }
        String text = guessField.getText().trim();
        int g;
        try {
            g = Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            setFeedback("Enter a whole number", new Color(0xC0392B));
            guessField.selectAll();
            guessField.requestFocusInWindow();
            return;
        }
        if (g < 1 || g > game.max) {
            setFeedback("Must be between 1 and " + game.max, new Color(0xC0392B));
            guessField.selectAll();
            guessField.requestFocusInWindow();
            return;
        }

        Result r = game.guess(g);
        updateAttemptLabel();

        switch (r) {
            case TOO_HIGH:
                setFeedback("Too High!", new Color(0xD35400));
                break;
            case TOO_LOW:
                setFeedback("Too Low!", new Color(0x2471A3));
                break;
            case CORRECT:
                setFeedback("Correct!", new Color(0x1E8449));
                endRound(true);
                return;
            case LOST:
                setFeedback("You Lost!", new Color(0xC0392B));
                endRound(false);
                return;
        }
        guessField.setText("");
        guessField.requestFocusInWindow();
    }

    private void endRound(boolean won) {
        guessField.setEnabled(false);
        guessButton.setEnabled(false);
        roundsPlayed++;

        String levelName = LEVEL_NAMES[game.level];
        String line;
        if (won) {
            wins++;
            line = "Round " + round + " — guessed in " + game.attempts + " attempt"
                    + (game.attempts == 1 ? "" : "s") + " (" + levelName + ")";
        } else {
            line = "Round " + round + " — lost, number was " + game.secret
                    + " (" + levelName + ")";
        }
        historyArea.append(line + "\n");
        historyArea.setCaretPosition(historyArea.getDocument().getLength());
        winsLabel.setText("Wins: " + wins + " / " + roundsPlayed + " rounds");

        String message = won
                ? "Correct! You guessed " + game.secret + " in " + game.attempts
                + " attempt" + (game.attempts == 1 ? "" : "s") + ".\n\nPlay again?"
                : "You Lost! The number was " + game.secret + ".\n\nPlay again?";
        int choice = JOptionPane.showConfirmDialog(this, message,
                won ? "You won!" : "Game over", JOptionPane.YES_NO_OPTION,
                won ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            startRound();
        } else {
            promptLabel.setText("Thanks for playing! Press New Round to play again.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new NumberGuessingGameGUI().setVisible(true));
    }
}
