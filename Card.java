package banking;

public class Card
{
    private final String cardNumber;
    private final String PIN;

    public Card(String cardNumber, String PIN)
    {
        this.cardNumber = cardNumber;
        this.PIN = PIN;
    }

    public String getCardNumber()
    {
        return cardNumber;
    }

    public String getPIN()
    {
        return PIN;
    }

    public String toString()
    {
        return getCardNumber() + " - " + getPIN();
    }
}