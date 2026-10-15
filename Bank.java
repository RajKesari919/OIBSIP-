import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the bank as a whole: a directory of accounts.
 * Responsible for looking accounts up and authenticating User ID + PIN
 * combinations. Keeping this logic out of ATM keeps responsibilities
 * cleanly separated (Bank = data/authentication, ATM = user interaction).
 * Implements Serializable so the entire bank (all accounts, all balances,
 * all transaction history) can be checkpointed to disk as one unit.
 */
public class Bank implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<String, Account> accounts;

    public Bank() {
        this.accounts = new HashMap<>();
    }

    /**
     * Registers a new account with the bank.
     */
    public void addAccount(Account account) {
        accounts.put(account.getAccountId(), account);
    }

    /**
     * Looks up an account by its ID. Returns null if not found.
     */
    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    /**
     * Returns true if an account with this ID exists in the bank.
     */
    public boolean accountExists(String accountId) {
        return accounts.containsKey(accountId);
    }

    /**
     * Authenticates a User ID + PIN pair.
     * Returns the matching Account on success, or null on failure
     * (unknown ID or wrong PIN) — callers should not be able to tell
     * which one failed, for basic security hygiene.
     */
    public Account authenticate(String accountId, String pin) {
        Account account = accounts.get(accountId);
        if (account != null && account.checkPin(pin)) {
            return account;
        }
        return null;
    }
}
