package banking;

public class Main
{
    public static void main(String[] args)
    {
        if (args[0].equals("-fileName") && args[1] != null)
        {
            Bank bank = new Bank("jdbc:sqlite:" + args[1]);
            bank.setupDatabase();

            Menu menu = new Menu(bank);
            menu.menuHandler();
        } else
        {
            System.out.println("Path to database not found, the program will now terminate.");
        }
    }
}
