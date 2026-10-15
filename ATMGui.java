import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * Swing-based graphical interface for the ATM.
 * Reuses the same Bank / Account / Transaction model classes as the
 * console version (ATM.java / Main.java) — only the presentation layer
 * differs, which keeps the banking logic identical and in one place.
 */
public class ATMGui extends JFrame {

    private static final int MAX_PIN_ATTEMPTS = 3;
    private static final Color PRIMARY = new Color(25, 55, 109);
    private static final Color ACCENT = new Color(0, 120, 215);
    private static final Font FONT_LABEL = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 20);
    private static final Font FONT_BALANCE = new Font("SansSerif", Font.BOLD, 18);

    private final Bank bank;
    private int attemptsRemaining = MAX_PIN_ATTEMPTS;
    private Account currentAccount;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);

    // Login screen fields
    private JTextField userIdField;
    private JPasswordField pinField;
    private JLabel loginStatusLabel;

    // Menu screen fields
    private JLabel welcomeLabel;
    private JLabel balanceLabel;

    // History screen
    private JTextArea historyArea;

    public ATMGui(Bank bank) {
        super("Java Console ATM \u2014 GUI Edition");
        this.bank = bank;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 560);
        setMinimumSize(new Dimension(420, 480));
        setLocationRelativeTo(null);

        cardPanel.add(buildLoginPanel(), "login");
        cardPanel.add(buildMenuPanel(), "menu");
        cardPanel.add(buildHistoryPanel(), "history");
        cardPanel.add(buildWithdrawPanel(), "withdraw");
        cardPanel.add(buildDepositPanel(), "deposit");
        cardPanel.add(buildTransferPanel(), "transfer");

        add(cardPanel);
        cardLayout.show(cardPanel, "login");
    }

    // ---------------------------------------------------------------
    // Login panel
    // ---------------------------------------------------------------

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Welcome to the ATM", SwingConstants.CENTER);
        title.setFont(FONT_TITLE);
        title.setForeground(PRIMARY);
        gbc.gridy = 0;
        panel.add(title, gbc);

        JLabel userIdLabel = new JLabel("User ID");
        userIdLabel.setFont(FONT_LABEL);
        gbc.gridy = 1;
        panel.add(userIdLabel, gbc);

        userIdField = new JTextField();
        userIdField.setFont(FONT_LABEL);
        gbc.gridy = 2;
        panel.add(userIdField, gbc);

        JLabel pinLabel = new JLabel("PIN");
        pinLabel.setFont(FONT_LABEL);
        gbc.gridy = 3;
        panel.add(pinLabel, gbc);

        pinField = new JPasswordField();
        pinField.setFont(FONT_LABEL);
        gbc.gridy = 4;
        panel.add(pinField, gbc);
        pinField.addActionListener(this::onLoginAttempt);

        JButton loginButton = new JButton("Login");
        styleButton(loginButton);
        loginButton.addActionListener(this::onLoginAttempt);
        gbc.gridy = 5;
        gbc.insets = new Insets(20, 0, 8, 0);
        panel.add(loginButton, gbc);

        loginStatusLabel = new JLabel(" ", SwingConstants.CENTER);
        loginStatusLabel.setForeground(new Color(180, 40, 40));
        loginStatusLabel.setFont(FONT_LABEL);
        gbc.gridy = 6;
        gbc.insets = new Insets(8, 0, 8, 0);
        panel.add(loginStatusLabel, gbc);

        JLabel hint = new JLabel("<html><center>Sample logins:<br>A001/1111, A002/2222, A003/3333</center></html>",
                SwingConstants.CENTER);
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(Color.GRAY);
        gbc.gridy = 7;
        panel.add(hint, gbc);

        return panel;
    }

    private void onLoginAttempt(ActionEvent e) {
        String userId = userIdField.getText().trim();
        String pin = new String(pinField.getPassword()).trim();

        Account account = bank.authenticate(userId, pin);
        if (account != null) {
            currentAccount = account;
            attemptsRemaining = MAX_PIN_ATTEMPTS;
            pinField.setText("");
            loginStatusLabel.setText(" ");
            showMenu();
            return;
        }

        attemptsRemaining--;
        pinField.setText("");

        if (attemptsRemaining <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Too many incorrect attempts. Access denied.\nPlease contact your bank branch.",
                    "Access Denied", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        } else {
            loginStatusLabel.setText("Incorrect User ID or PIN. Attempts remaining: " + attemptsRemaining);
        }
    }

    // ---------------------------------------------------------------
    // Main menu panel
    // ---------------------------------------------------------------

    private JPanel buildMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel header = new JPanel();
        header.setBackground(PRIMARY);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(20, 20, 20, 20));

        welcomeLabel = new JLabel("Welcome");
        welcomeLabel.setFont(FONT_TITLE);
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        balanceLabel = new JLabel("Balance: Rs. 0.00");
        balanceLabel.setFont(FONT_BALANCE);
        balanceLabel.setForeground(Color.WHITE);
        balanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(welcomeLabel);
        header.add(Box.createVerticalStrut(6));
        header.add(balanceLabel);
        panel.add(header, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(5, 1, 12, 12));
        buttons.setBorder(new EmptyBorder(30, 60, 30, 60));
        buttons.setBackground(Color.WHITE);

        JButton historyBtn = new JButton("1. Transaction History");
        JButton withdrawBtn = new JButton("2. Withdraw");
        JButton depositBtn = new JButton("3. Deposit");
        JButton transferBtn = new JButton("4. Transfer");
        JButton quitBtn = new JButton("5. Quit");

        for (JButton b : new JButton[]{historyBtn, withdrawBtn, depositBtn, transferBtn, quitBtn}) {
            styleButton(b);
        }
        quitBtn.setBackground(new Color(180, 40, 40));

        historyBtn.addActionListener(e -> showHistory());
        withdrawBtn.addActionListener(e -> {
            clearWithdrawFields();
            cardLayout.show(cardPanel, "withdraw");
        });
        depositBtn.addActionListener(e -> {
            clearDepositFields();
            cardLayout.show(cardPanel, "deposit");
        });
        transferBtn.addActionListener(e -> {
            clearTransferFields();
            cardLayout.show(cardPanel, "transfer");
        });
        quitBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Thank you for using the Java Console ATM.\nGoodbye, " + currentAccount.getHolderName()
                            + "!\n\nYour account data has been saved to checkpoint.",
                    "Goodbye", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        });

        buttons.add(historyBtn);
        buttons.add(withdrawBtn);
        buttons.add(depositBtn);
        buttons.add(transferBtn);
        buttons.add(quitBtn);

        panel.add(buttons, BorderLayout.CENTER);
        return panel;
    }

    private void showMenu() {
        welcomeLabel.setText("Welcome, " + currentAccount.getHolderName());
        refreshBalanceLabel();
        cardLayout.show(cardPanel, "menu");
    }

    private void refreshBalanceLabel() {
        balanceLabel.setText(String.format("Account %s   \u2014   Balance: Rs. %.2f",
                currentAccount.getAccountId(), currentAccount.getBalance()));
    }

    // ---------------------------------------------------------------
    // Transaction history panel
    // ---------------------------------------------------------------

    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Transaction History");
        title.setFont(FONT_TITLE);
        title.setForeground(PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        panel.add(new JScrollPane(historyArea), BorderLayout.CENTER);

        panel.add(buildBackButton(), BorderLayout.SOUTH);
        return panel;
    }

    private void showHistory() {
        ArrayList<Transaction> history = currentAccount.getTransactionHistory();
        if (history.isEmpty()) {
            historyArea.setText("No transactions yet in this session.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (Transaction t : history) {
                sb.append(t).append("\n");
            }
            historyArea.setText(sb.toString());
        }
        historyArea.setCaretPosition(0);
        cardLayout.show(cardPanel, "history");
    }

    // ---------------------------------------------------------------
    // Withdraw panel
    // ---------------------------------------------------------------

    private JTextField withdrawAmountField;
    private JLabel withdrawStatusLabel;

    private JPanel buildWithdrawPanel() {
        AmountForm form = buildAmountFormPanel("Withdraw Cash", "Amount to withdraw (Rs.)");
        withdrawAmountField = form.amountField;
        withdrawStatusLabel = form.statusLabel;

        form.confirmButton.addActionListener(e -> {
            Double amount = parseAmount(withdrawAmountField.getText(), withdrawStatusLabel);
            if (amount == null) return;

            if (!currentAccount.hasSufficientFunds(amount)) {
                withdrawStatusLabel.setText(String.format(
                        "Insufficient Funds. Current balance: Rs. %.2f", currentAccount.getBalance()));
                return;
            }

            currentAccount.withdraw(amount, "Cash withdrawal");
            DataStore.saveBank(bank);
            JOptionPane.showMessageDialog(this,
                    String.format("Withdrawal successful.%nNew balance: Rs. %.2f", currentAccount.getBalance()),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshBalanceLabel();
            showMenu();
        });

        return form.panel;
    }

    private void clearWithdrawFields() {
        withdrawAmountField.setText("");
        withdrawStatusLabel.setText(" ");
    }

    // ---------------------------------------------------------------
    // Deposit panel
    // ---------------------------------------------------------------

    private JTextField depositAmountField;
    private JLabel depositStatusLabel;

    private JPanel buildDepositPanel() {
        AmountForm form = buildAmountFormPanel("Deposit Cash", "Amount to deposit (Rs.)");
        depositAmountField = form.amountField;
        depositStatusLabel = form.statusLabel;

        form.confirmButton.addActionListener(e -> {
            Double amount = parseAmount(depositAmountField.getText(), depositStatusLabel);
            if (amount == null) return;

            currentAccount.deposit(amount, "Cash deposit");
            DataStore.saveBank(bank);
            JOptionPane.showMessageDialog(this,
                    String.format("Deposit successful.%nNew balance: Rs. %.2f", currentAccount.getBalance()),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshBalanceLabel();
            showMenu();
        });

        return form.panel;
    }

    private void clearDepositFields() {
        depositAmountField.setText("");
        depositStatusLabel.setText(" ");
    }

    // ---------------------------------------------------------------
    // Transfer panel
    // ---------------------------------------------------------------

    private JTextField transferRecipientField;
    private JTextField transferAmountField;
    private JLabel transferStatusLabel;

    private JPanel buildTransferPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Transfer Funds");
        title.setFont(FONT_TITLE);
        title.setForeground(PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel recipientLabel = new JLabel("Recipient Account ID");
        recipientLabel.setFont(FONT_LABEL);
        gbc.gridy = 0;
        form.add(recipientLabel, gbc);

        transferRecipientField = new JTextField();
        transferRecipientField.setFont(FONT_LABEL);
        gbc.gridy = 1;
        form.add(transferRecipientField, gbc);

        JLabel amountLabel = new JLabel("Amount to transfer (Rs.)");
        amountLabel.setFont(FONT_LABEL);
        gbc.gridy = 2;
        form.add(amountLabel, gbc);

        transferAmountField = new JTextField();
        transferAmountField.setFont(FONT_LABEL);
        gbc.gridy = 3;
        form.add(transferAmountField, gbc);

        JButton confirmBtn = new JButton("Confirm Transfer");
        styleButton(confirmBtn);
        gbc.gridy = 4;
        gbc.insets = new Insets(16, 0, 8, 0);
        form.add(confirmBtn, gbc);

        transferStatusLabel = new JLabel(" ", SwingConstants.CENTER);
        transferStatusLabel.setForeground(new Color(180, 40, 40));
        transferStatusLabel.setFont(FONT_LABEL);
        gbc.gridy = 5;
        gbc.insets = new Insets(8, 0, 8, 0);
        form.add(transferStatusLabel, gbc);

        panel.add(form, BorderLayout.CENTER);
        panel.add(buildBackButton(), BorderLayout.SOUTH);

        confirmBtn.addActionListener(e -> {
            String recipientId = transferRecipientField.getText().trim();

            if (recipientId.isEmpty()) {
                transferStatusLabel.setText("Please enter a recipient Account ID.");
                return;
            }
            if (recipientId.equals(currentAccount.getAccountId())) {
                transferStatusLabel.setText("You cannot transfer funds to your own account.");
                return;
            }

            Account recipient = bank.getAccount(recipientId);
            if (recipient == null) {
                transferStatusLabel.setText("No account found with ID: " + recipientId);
                return;
            }

            Double amount = parseAmount(transferAmountField.getText(), transferStatusLabel);
            if (amount == null) return;

            if (!currentAccount.hasSufficientFunds(amount)) {
                transferStatusLabel.setText(String.format(
                        "Insufficient Funds. Current balance: Rs. %.2f", currentAccount.getBalance()));
                return;
            }

            currentAccount.recordTransferOut(amount, recipient.getAccountId());
            recipient.recordTransferIn(amount, currentAccount.getAccountId());
            DataStore.saveBank(bank);

            JOptionPane.showMessageDialog(this,
                    String.format("Transfer successful.%nRs. %.2f sent to %s.%nNew balance: Rs. %.2f",
                            amount, recipient.getAccountId(), currentAccount.getBalance()),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshBalanceLabel();
            showMenu();
        });

        return panel;
    }

    private void clearTransferFields() {
        transferRecipientField.setText("");
        transferAmountField.setText("");
        transferStatusLabel.setText(" ");
    }

    // ---------------------------------------------------------------
    // Shared helpers
    // ---------------------------------------------------------------

    /**
     * Small holder returned by buildAmountFormPanel so callers can wire up
     * listeners against the exact fields they need, without reaching into
     * the component tree by index.
     */
    private static final class AmountForm {
        final JPanel panel;
        final JTextField amountField;
        final JButton confirmButton;
        final JLabel statusLabel;

        AmountForm(JPanel panel, JTextField amountField, JButton confirmButton, JLabel statusLabel) {
            this.panel = panel;
            this.amountField = amountField;
            this.confirmButton = confirmButton;
            this.statusLabel = statusLabel;
        }
    }

    /**
     * Builds a simple "title + label + text field + confirm button + status label"
     * form panel, shared by Withdraw and Deposit screens.
     */
    private AmountForm buildAmountFormPanel(String titleText, String fieldLabel) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel(titleText);
        title.setFont(FONT_TITLE);
        title.setForeground(PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel label = new JLabel(fieldLabel);
        label.setFont(FONT_LABEL);
        gbc.gridy = 0;
        form.add(label, gbc);

        JTextField amountField = new JTextField();
        amountField.setFont(FONT_LABEL);
        gbc.gridy = 1;
        form.add(amountField, gbc);

        JButton confirmBtn = new JButton("Confirm");
        styleButton(confirmBtn);
        gbc.gridy = 2;
        gbc.insets = new Insets(16, 0, 8, 0);
        form.add(confirmBtn, gbc);

        JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setForeground(new Color(180, 40, 40));
        statusLabel.setFont(FONT_LABEL);
        gbc.gridy = 3;
        gbc.insets = new Insets(8, 0, 8, 0);
        form.add(statusLabel, gbc);

        panel.add(form, BorderLayout.CENTER);
        panel.add(buildBackButton(), BorderLayout.SOUTH);
        return new AmountForm(panel, amountField, confirmBtn, statusLabel);
    }

    private JPanel buildBackButton() {
        JButton backBtn = new JButton("\u2190 Back to Menu");
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> showMenu());
        JPanel wrapper = new JPanel();
        wrapper.setBackground(Color.WHITE);
        wrapper.add(backBtn);
        return wrapper;
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(ACCENT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
    }

    /**
     * Parses and validates a positive monetary amount from a text field.
     * On invalid input, sets the given status label and returns null.
     */
    private Double parseAmount(String text, JLabel statusLabel) {
        try {
            double amount = Double.parseDouble(text.trim());
            if (amount <= 0) {
                statusLabel.setText("Amount must be greater than zero.");
                return null;
            }
            return amount;
        } catch (NumberFormatException e) {
            statusLabel.setText("Please enter a valid numeric amount.");
            return null;
        }
    }
}
