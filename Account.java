import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents a single bank account.
 * Demonstrates encapsulation: fields are private, accessed only via getters
 * and controlled mutator methods (deposit/withdraw), never set directly.
 * Implements Serializable so the account (balance + full transaction
 * history) can be written to a checkpoint file and restored later.
 */
public class Account implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String accountId;
    private final String pin;
    private String holderName;
    private double balance;
    private final ArrayList<Transaction> transactionHistory;

    public Account(String accountId, String pin, String holderName, double initialBalance) {
        this.accountId = accountId;
        this.pin = pin;
        this.holderName = holderName;
        this.balance = initialBalance;
        this.transactionHistory = new ArrayList<>();
    }

    // ---------- Getters ----------

    public String getAccountId() {
        return accountId;
    }

    public String getHolderName() {
        return holderName;
    }

    public double getBalance() {
        return balance;
    }

    public ArrayList<Transaction> getTransactionHistory() {
        return transactionHistory;
    }

    // ---------- Setters ----------

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    // ---------- Behaviour ----------

    /**
     * Validates a PIN against this account's stored PIN.
     */
    public boolean checkPin(String enteredPin) {
        return pin.equals(enteredPin);
    }

    /**
     * Checks whether the account can cover a withdrawal/transfer of the given amount.
     */
    public boolean hasSufficientFunds(double amount) {
        return amount <= balance;
    }

    /**
     * Adds funds to the account and logs the transaction.
     */
    public void deposit(double amount, String details) {
        balance += amount;
        transactionHistory.add(new Transaction("DEPOSIT", amount, balance, details));
    }

    /**
     * Removes funds from the account and logs the transaction.
     * Caller is expected to have already verified hasSufficientFunds().
     */
    public void withdraw(double amount, String details) {
        balance -= amount;
        transactionHistory.add(new Transaction("WITHDRAW", amount, balance, details));
    }

    /**
     * Logs an outgoing transfer without a generic "withdraw" label,
     * so transaction history clearly distinguishes transfers from plain withdrawals.
     */
    public void recordTransferOut(double amount, String recipientId) {
        balance -= amount;
        transactionHistory.add(new Transaction("TRANSFER-OUT", amount, balance,
                "To account " + recipientId));
    }

    /**
     * Logs an incoming transfer.
     */
    public void recordTransferIn(double amount, String senderId) {
        balance += amount;
        transactionHistory.add(new Transaction("TRANSFER-IN", amount, balance,
                "From account " + senderId));
    }
}
