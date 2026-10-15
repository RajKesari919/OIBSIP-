import java.util.Scanner;

/**
 * Drives the user-facing console interaction: login, PIN retry limit,
 * and the main transaction menu (history, withdraw, deposit, transfer, quit).
 */
public class ATM {

    private static final int MAX_PIN_ATTEMPTS = 3;

    private final Bank bank;
    private final Scanner scanner;
    private Account currentAccount;

    public ATM(Bank bank, Scanner scanner) {
        this.bank = bank;
        this.scanner = scanner;
    }

    /**
     * Entry point for the whole session: login, then (if successful) menu loop.
     */
    public void start() {
        System.out.println("=========================================");
        System.out.println("      WELCOME TO THE JAVA CONSOLE ATM");
        System.out.println("=========================================");

        if (login()) {
            runMenu();
        } else {
            System.out.println("\nToo many incorrect attempts. Access denied.");
            System.out.println("Please contact your bank branch. Exiting...");
        }
    }

    /**
     * Prompts for User ID and PIN, allowing up to MAX_PIN_ATTEMPTS tries.
     * Returns true on successful authentication, false if attempts are exhausted.
     */
    private boolean login() {
        System.out.print("\nEnter User ID: ");
        String accountId = scanner.nextLine().trim();

        int attemptsRemaining = MAX_PIN_ATTEMPTS;

        while (attemptsRemaining > 0) {
            System.out.print("Enter PIN: ");
            String pin = scanner.nextLine().trim();

            Account account = bank.authenticate(accountId, pin);
            if (account != null) {
                currentAccount = account;
                System.out.println("\nLogin successful. Welcome, " + account.getHolderName() + "!");
                return true;
            }

            attemptsRemaining--;
            if (attemptsRemaining > 0) {
                System.out.println("Incorrect PIN. Attempts remaining: " + attemptsRemaining);
            }
        }
        return false;
    }

    /**
     * Displays the main menu in a loop until the user chooses to quit.
     */
    private void runMenu() {
        boolean running = true;

        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    showTransactionHistory();
                    break;
                case "2":
                    handleWithdraw();
                    break;
                case "3":
                    handleDeposit();
                    break;
                case "4":
                    handleTransfer();
                    break;
                case "5":
                    System.out.println("\nThank you for using the Java Console ATM. Goodbye, "
                            + currentAccount.getHolderName() + "!");
                    System.out.println("Your account data has been saved to checkpoint.");
                    running = false;
                    break;
                default:
                    System.out.println("\nInvalid choice. Please select an option between 1 and 5.");
            }
        }
    }

    private void printMenu() {
        System.out.println("\n----------------- MAIN MENU -----------------");
        System.out.printf("Account: %s   Balance: Rs. %.2f%n",
                currentAccount.getAccountId(), currentAccount.getBalance());
        System.out.println("1. Transaction History");
        System.out.println("2. Withdraw");
        System.out.println("3. Deposit");
        System.out.println("4. Transfer");
        System.out.println("5. Quit");
        System.out.println("----------------------------------------------");
        System.out.print("Choose an option: ");
    }

    // ---------- Menu option handlers ----------

    private void showTransactionHistory() {
        System.out.println("\n--------------- TRANSACTION HISTORY ---------------");
        if (currentAccount.getTransactionHistory().isEmpty()) {
            System.out.println("No transactions yet in this session.");
        } else {
            for (Transaction t : currentAccount.getTransactionHistory()) {
                System.out.println(t);
            }
        }
        System.out.println("----------------------------------------------------");
    }

    private void handleWithdraw() {
        double amount = promptForAmount("withdraw");
        if (amount <= 0) {
            return;
        }

        if (!currentAccount.hasSufficientFunds(amount)) {
            System.out.println("\nInsufficient Funds. Your current balance is Rs. "
                    + String.format("%.2f", currentAccount.getBalance()));
            return;
        }

        currentAccount.withdraw(amount, "Cash withdrawal");
        DataStore.saveBank(bank);
        System.out.printf("%nWithdrawal successful. New balance: Rs. %.2f%n",
                currentAccount.getBalance());
    }

    private void handleDeposit() {
        double amount = promptForAmount("deposit");
        if (amount <= 0) {
            return;
        }

        currentAccount.deposit(amount, "Cash deposit");
        DataStore.saveBank(bank);
        System.out.printf("%nDeposit successful. New balance: Rs. %.2f%n",
                currentAccount.getBalance());
    }

    private void handleTransfer() {
        System.out.print("\nEnter recipient Account ID: ");
        String recipientId = scanner.nextLine().trim();

        if (recipientId.equals(currentAccount.getAccountId())) {
            System.out.println("You cannot transfer funds to your own account.");
            return;
        }

        Account recipient = bank.getAccount(recipientId);
        if (recipient == null) {
            System.out.println("No account found with ID: " + recipientId);
            return;
        }

        double amount = promptForAmount("transfer");
        if (amount <= 0) {
            return;
        }

        if (!currentAccount.hasSufficientFunds(amount)) {
            System.out.println("\nInsufficient Funds. Your current balance is Rs. "
                    + String.format("%.2f", currentAccount.getBalance()));
            return;
        }

        currentAccount.recordTransferOut(amount, recipient.getAccountId());
        recipient.recordTransferIn(amount, currentAccount.getAccountId());
        DataStore.saveBank(bank);

        System.out.printf("%nTransfer successful. Rs. %.2f sent to %s. New balance: Rs. %.2f%n",
                amount, recipient.getAccountId(), currentAccount.getBalance());
    }

    /**
     * Prompts for a monetary amount and validates it is a positive number.
     * Returns -1 on invalid input so callers can bail out cleanly.
     */
    private double promptForAmount(String actionVerb) {
        System.out.print("\nEnter amount to " + actionVerb + ": Rs. ");
        String input = scanner.nextLine().trim();

        try {
            double amount = Double.parseDouble(input);
            if (amount <= 0) {
                System.out.println("Amount must be greater than zero.");
                return -1;
            }
            return amount;
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount entered. Please enter a numeric value.");
            return -1;
        }
    }
}
