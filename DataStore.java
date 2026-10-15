import java.io.*;

/**
 * Handles persistence of the Bank's state to a checkpoint file on disk,
 * using Java's built-in object serialization.
 *
 * Every successful transaction triggers a save, so account balances and
 * transaction history survive a restart of the program. If no checkpoint
 * file exists yet (first run), loadBank() returns null and the caller is
 * expected to create a fresh Bank with starting data.
 */
public class DataStore {

    private static final String CHECKPOINT_FILE = "atm_checkpoint.dat";

    private DataStore() {
        // Utility class; not meant to be instantiated.
    }

    /**
     * Writes the given Bank (and everything reachable from it — all
     * Accounts and their Transaction history) to the checkpoint file.
     * Failures are reported but never crash the ATM: a failed save just
     * means the next session starts from the last successful checkpoint.
     */
    public static void saveBank(Bank bank) {
        try (ObjectOutputStream out =
                     new ObjectOutputStream(new FileOutputStream(CHECKPOINT_FILE))) {
            out.writeObject(bank);
        } catch (IOException e) {
            System.err.println("Warning: could not save checkpoint (" + e.getMessage() + ")");
        }
    }

    /**
     * Reads a previously saved Bank back from the checkpoint file.
     * Returns null if no checkpoint exists yet, or if the file is
     * unreadable/corrupted — in either case the caller should fall back
     * to creating a fresh Bank.
     */
    public static Bank loadBank() {
        File file = new File(CHECKPOINT_FILE);
        if (!file.exists()) {
            return null;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (Bank) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Warning: could not read checkpoint (" + e.getMessage()
                    + "). Starting fresh.");
            return null;
        }
    }

    /**
     * True if a checkpoint file is already present on disk.
     */
    public static boolean checkpointExists() {
        return new File(CHECKPOINT_FILE).exists();
    }
}
