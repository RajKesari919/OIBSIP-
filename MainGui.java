import javax.swing.SwingUtilities;

/**
 * Entry point for the graphical (Swing) version of the ATM.
 * Uses the exact same Bank / Account / Transaction model as the console
 * version — only ATMGui differs in how it presents the interaction.
 *
 * On startup, tries to load a previously saved checkpoint (atm_checkpoint.dat),
 * shared with the console version. If none exists, it creates a fresh Bank
 * with sample accounts and saves an initial checkpoint. From then on, ATMGui
 * auto-saves a checkpoint after every successful transaction.
 *
 * Sample login credentials (for testing, used only on a fresh checkpoint):
 *   User ID: A001   PIN: 1111   (John Doe, Rs. 5000.00)
 *   User ID: A002   PIN: 2222   (Priya Sharma, Rs. 10000.00)
 *   User ID: A003   PIN: 3333   (Raj Mehta, Rs. 2500.00)
 */
public class MainGui {

    public static void main(String[] args) {
        Bank bank = DataStore.loadBank();

        if (bank == null) {
            bank = new Bank();
            bank.addAccount(new Account("A001", "1111", "John Doe", 5000.00));
            bank.addAccount(new Account("A002", "2222", "Priya Sharma", 10000.00));
            bank.addAccount(new Account("A003", "3333", "Raj Mehta", 2500.00));
            DataStore.saveBank(bank);
        }

        final Bank finalBank = bank;
        SwingUtilities.invokeLater(() -> {
            ATMGui gui = new ATMGui(finalBank);
            gui.setVisible(true);
        });
    }
}
