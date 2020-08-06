package banking;

import java.util.Scanner;

public class Menu
{
    private final Bank bank;
    private boolean isLoggedIn = false;
    private final Scanner scanner = new Scanner(System.in);

    public Menu(Bank bank)
    {
        this.bank = bank;
    }

    public void menuHandler()
    {
        while (true)
        {
            if (isLoggedIn)
            {
                loggedInMenu();
            } else
            {
                mainMenu();
            }
        }
    }

    public void mainMenu()
    {
        while (true)
        {
            System.out.println("1. Create an account \n" + "2. Log into account \n" + "0. Exit");
            int userInput = Integer.parseInt(scanner.nextLine());

            switch (userInput)
            {
                case 0:
                    System.out.println("Bye!");
                    System.exit(0);
                case 1:
                    bank.createAccount();
                    break;
                case 2:
                    boolean successfulLogin = bank.accessAccount();
                    if (successfulLogin)
                    {
                        isLoggedIn = true;
                        loggedInMenu();
                    } else
                    {
                        System.out.println("Could not access account");
                    }
                    break;
                default:
                    System.out.println("Invalid input, try again.");
            }
        }
    }

    public void loggedInMenu()
    {
        while (true)
        {
            System.out.println("1. Balance \n" +
                    "2. Add income \n" +
                    "3. Do Transfer \n" +
                    "4. Close account \n" +
                    "5. Log out \n" +
                    "0. Exit");
            int userInput = Integer.parseInt(scanner.nextLine());

            switch (userInput)
            {
                case 0:
                    System.out.println("Bye!");
                    System.exit(0);
                case 1:
                    System.out.println("Balance: " + bank.getBalance());
                    break;
                case 2:
                    bank.addIncome();
                    break;
                case 3:
                    bank.transferMoney();
                    break;
                case 4:
                    bank.closeAccount();
                    return;
                case 5:
                    System.out.println("\nYou have successfully logged out!\n");
                    isLoggedIn = false;
                    return;
                default:
                    System.out.println("Invalid input, try again.");
            }
        }
    }
}
