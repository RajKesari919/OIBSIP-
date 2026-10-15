import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single banking transaction (deposit, withdrawal, or transfer).
 * Each Account keeps its own ArrayList<Transaction> as its history log.
 * Implements Serializable so a Transaction can be written to disk as part
 * of a Bank/Account checkpoint and restored on the next run.
 */
public class Transaction implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private final String type;        // "DEPOSIT", "WITHDRAW", "TRANSFER-OUT", "TRANSFER-IN"
    private final double amount;
    private final double balanceAfter;
    private final String details;     // e.g. "Transferred to A002"
    private final LocalDateTime timestamp;

    public Transaction(String type, double amount, double balanceAfter, String details) {
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("[%s] %-14s Rs. %10.2f | Balance After: Rs. %10.2f | %s",
                timestamp.format(FORMATTER), type, amount, balanceAfter, details);
    }
}
