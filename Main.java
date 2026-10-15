import java.util.Scanner;

/**
 * Entry point for the ATM console simulation.
 *
 * On startup, tries to load a previously saved checkpoint (atm_checkpoint.dat).
 * If none exists, it creates a fresh Bank with sample accounts and saves an
 * initial checkpoint. From then on, the ATM auto-saves a checkpoint after
 * every successful transaction, so balances and history persist across runs.
 *
 * Sample login credentials (for testing, used only on a fresh checkpoint):
 *   User ID: A001   PIN: 1111   (Rahul, Rs. 5000.00)
 *   User ID: A002   PIN: 2222   (Priya Sharma, Rs. 10000.00)
 *   User ID: A003   PIN: 3333   (Raj Mehta, Rs. 2500.00)
 */
public class Main {

    public static void main(String[] args) {
        Bank bank = DataStore.loadBank();

        if (bank == null) {
            System.out.println("No existing checkpoint found. Setting up sample accounts...");
            bank = new Bank();
            bank.addAccount(new Account("A001", "1111", "raj kesari", 5000.00));
            bank.addAccount(new Account("A002", "2222", "Priya Sharma", 10000.00));
            bank.addAccount(new Account("A003", "3333", "Raj Mehta", 2500.00));
            DataStore.saveBank(bank);
        } else {
            System.out.println("Checkpoint loaded. Resuming with saved account balances.");
        }

        try (Scanner scanner = new Scanner(System.in)) {
            ATM atm = new ATM(bank, scanner);
            atm.start();
        }
    }
}
