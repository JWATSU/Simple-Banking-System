package banking;

import java.sql.*;
import java.util.Random;
import java.util.Scanner;

public class Bank
{
    private final String URL;
    private final Scanner scanner = new Scanner(System.in);
    private Card card;

    public Bank(String URL)
    {
        this.URL = URL;
    }

    public void setupDatabase()
    {
        String sql = "CREATE TABLE IF NOT EXISTS card (\n"
                + " id INTEGER PRIMARY KEY,\n"
                + " number TEXT NOT NULL,\n"
                + " pin TEXT,\n"
                + " balance INTEGER DEFAULT 0\n"
                + ");";

        // When you connect to an SQLite database that does not exist, it automatically creates a new database. If it already exists a new one won't be created.
        try (Connection conn = DriverManager.getConnection(URL);
             Statement preparedStatement = conn.createStatement())
        {
            preparedStatement.execute(sql);
        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
    }

    private int getChecksumWithLuhnAlgorithm(String cardNumber)
    {
        int[] cardDigits = new int[15];
        int sum = 0;
        int checksum = -1;

        for (int i = 0; i < cardDigits.length; i++)
        {
            cardDigits[i] = Integer.parseInt(String.valueOf(cardNumber.charAt(i)));

            if ((i + 1) % 2 != 0)
            {
                cardDigits[i] *= 2;
            }
            if (cardDigits[i] > 9)
            {
                cardDigits[i] -= 9;
            }
            sum += cardDigits[i];
        }

        for (int i = 0; i < 10; i++)
        {
            if ((i + sum) % 10 == 0)
            {
                checksum = i;
            }
        }
        return checksum;
    }

    public void createAccount()
    {
        Random rnd = new Random();
        StringBuilder customerAccountNumber = new StringBuilder("400000");

        for (int i = 0; i < 9; i++)
        {
            int randomInt = rnd.nextInt(10);
            customerAccountNumber.append(randomInt);
        }
        customerAccountNumber.append(getChecksumWithLuhnAlgorithm(customerAccountNumber.toString()));

        StringBuilder PIN = new StringBuilder();
        for (int i = 0; i < 4; i++)
        {
            int randomInt = rnd.nextInt(10);
            PIN.append(randomInt);
        }

        System.out.println("\nYour card has been created \nYour card number:\n"
                + customerAccountNumber.toString()
                + "\nYour card PIN: \n"
                + PIN.toString() + "\n");

        card = new Card(customerAccountNumber.toString(), PIN.toString());
        insertNewCard();
    }

    public boolean accessAccount()
    {
        System.out.println("Enter your card number:");
        String cardNumber = scanner.nextLine();
        System.out.println("Enter your PIN:");
        String PIN = scanner.nextLine();
        if (checkIfCardExists(cardNumber, PIN))
        {
            System.out.println("\nYou have successfully logged in!\n");
            card = new Card(cardNumber, PIN);
            return true;
        } else
        {
            return false;
        }
    }

    private void insertNewCard()
    {
        String sql = "INSERT INTO card(number, pin) VALUES(?,?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement preparedStatement = conn.prepareStatement(sql))
        {
            preparedStatement.setString(1, card.getCardNumber());
            preparedStatement.setString(2, card.getPIN());
            preparedStatement.executeUpdate();

        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
    }

    private boolean checkIfCardExists(String number, String pin)
    {
        boolean exists = false;
        String sql = "SELECT number, pin "
                + "FROM card WHERE number = ? AND pin = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            pstmt.setString(1, number);
            pstmt.setString(2, pin);
            ResultSet rs = pstmt.executeQuery();
            exists = rs.next();
        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
        return exists;
    }

    public void addIncome()
    {
        System.out.println("Enter income:");
        int income = Integer.parseInt(scanner.nextLine());
        if (income <= 0)
        {
            System.out.println("Invalid input, income needs to be greater than 0.");
            return;
        }

        String sql = "UPDATE card SET balance = balance + ? WHERE number = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            pstmt.setInt(1, income);
            pstmt.setString(2, card.getCardNumber());
            pstmt.executeUpdate();
        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
    }

    public int getBalance()
    {
        int balance = -1;
        String sql = "SELECT balance FROM card WHERE number = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            pstmt.setString(1, card.getCardNumber());
            ResultSet rs = pstmt.executeQuery(sql);
            balance = rs.getInt("balance");
        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
        return balance;
    }

    public void closeAccount()
    {
        String sql = "DELETE FROM card WHERE number = " + card.getCardNumber();

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            pstmt.executeUpdate();
            card = null;
            System.out.println("Account closed.");
        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
    }

    public void transferMoney()
    {
        System.out.println("Enter receiver card number:");
        String toCard = scanner.nextLine();

        if (toCard.equals(card.getCardNumber()))
        {
            System.out.println("You can't transfer money to the same account");
        } else if (!checkLuhnAlgo(toCard))
        {
            System.out.println("You probably made a mistake in the card number. Please try again!");
        } else if (!checkIfCardExists(toCard))
        {
            System.out.println("Such a card does not exist.");
        } else
        {
            System.out.println("Enter how much money you want to transfer:");
            int amount = Integer.parseInt(scanner.nextLine());
            if (amount > getBalance())
            {
                System.out.println("Not enough money!");
            } else
            {
                String sql = "UPDATE card SET balance = balance + ? + WHERE number = ?";

                try (Connection conn = DriverManager.getConnection(URL);
                     PreparedStatement pstmt = conn.prepareStatement(sql))
                {
                    pstmt.setInt(1, amount);
                    pstmt.setString(2, toCard);
                    pstmt.executeUpdate(sql);

                    decreaseBalance(amount);
                }
                catch (SQLException e)
                {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    private void decreaseBalance(int amount)
    {
        String sql = "UPDATE card SET balance = balance - ? WHERE number = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            pstmt.setInt(1, amount);
            pstmt.setString(2, card.getCardNumber());
            pstmt.executeUpdate(sql);
        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
    }

    private boolean checkLuhnAlgo(String accountNumber)
    {
        int[] numArr = new int[accountNumber.length()];
        int sum = 0;

        for (int i = 0; i < numArr.length; i++)
        {
            numArr[i] = Integer.parseInt(String.valueOf(accountNumber.charAt(i)));

            if ((i + 1) % 2 != 0)
            {
                numArr[i] *= 2;
            }
            if (numArr[i] > 9)
            {
                numArr[i] -= 9;
            }

            sum += numArr[i];
        }
        return sum % 10 == 0;
    }

    private boolean checkIfCardExists(String number)
    {
        boolean exists = false;
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement())
        {
            String sql = "SELECT * FROM card WHERE number = '" + number + "'";

            ResultSet rs = stmt.executeQuery(sql);
            exists = rs.next();
        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
        return exists;
    }
}
