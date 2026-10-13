/**
 * Static methods that check raw text typed by the user before it reaches the
 * rest of the program. Each method returns the valid, converted value, or
 * reports invalid input by returning INVALID (for numbers) or null (for names).
 * No method throws on bad input, so the console app can simply re-prompt.
 *
 * @author Nana Ababio
 * @version October 2026
 */
public final class InputValidator
{
    /** Returned by the numeric validators when input is rejected. */
    public static final int INVALID = -1;

    /** Longest name allowed, in characters. */
    public static final int MAX_NAME_LENGTH = 30;

    /** Smallest party allowed. */
    public static final int MIN_PARTY_SIZE = 1;

    /** Largest party allowed. */
    public static final int MAX_PARTY_SIZE = 12;

    /** Largest money amount accepted: $10,000.00. */
    public static final int MAX_MONEY_CENTS = 1_000_000;

    /** Longest whole number we parse, to avoid int overflow. */
    private static final int MAX_DIGITS = 9;

    /**
     * Prevents creating InputValidator objects; all methods are static.
     */
    private InputValidator()
    {
        // nothing to do
    }


    /**
     * Converts and checks a menu selection. O(1).
     *
     * @param input
     *            the raw text typed by the user
     * @param min
     *            the smallest valid choice
     * @param max
     *            the largest valid choice
     * @return the choice, or INVALID if it is not a whole number from min to
     *             max
     */
    public static int validateMenuChoice(String input, int min, int max)
    {
        int value = parseWholeNumber(input);
        if (value == INVALID || value < min || value > max)
        {
            return INVALID;
        }
        return value;
    }


    /**
     * Checks that a name is not blank and is at most MAX_NAME_LENGTH characters
     * once surrounding spaces are removed. O(1) because the accepted length is
     * capped.
     *
     * @param input
     *            the raw text typed by the user
     * @return the trimmed name, or null if it is invalid
     */
    public static String validateName(String input)
    {
        if (input == null)
        {
            return null;
        }
        String trimmed = input.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_NAME_LENGTH)
        {
            return null;
        }
        return trimmed;
    }


    /**
     * Converts and checks that a party size is a whole number from
     * MIN_PARTY_SIZE to MAX_PARTY_SIZE. O(1).
     *
     * @param input
     *            the raw text typed by the user
     * @return the party size, or INVALID
     */
    public static int validatePartySize(String input)
    {
        int value = parseWholeNumber(input);
        if (value < MIN_PARTY_SIZE || value > MAX_PARTY_SIZE)
        {
            return INVALID;
        }
        return value;
    }


    /**
     * Converts and checks the format of a ticket number: a positive whole
     * number. This checks format only; Waitlist decides whether the ticket
     * exists. O(1).
     *
     * @param input
     *            the raw text typed by the user
     * @return the ticket number, or INVALID
     */
    public static int validateTicketNumber(String input)
    {
        int value = parseWholeNumber(input);
        if (value < 1)
        {
            return INVALID;
        }
        return value;
    }


    /**
     * Converts a payment or bid amount such as "6.50", "6.5", or "6" into
     * cents. Amounts must be greater than zero, so zero and negative amounts
     * are rejected. O(1).
     *
     * @param input
     *            the raw text typed by the user
     * @return the amount in cents, or INVALID
     */
    public static int validateMoney(String input)
    {
        int cents = parseCents(input);
        if (cents <= 0)
        {
            return INVALID;
        }
        return cents;
    }


    /**
     * Converts a starting wallet balance into cents. Works like validateMoney
     * but allows zero, since a customer may load no money. O(1).
     *
     * @param input
     *            the raw text typed by the user
     * @return the balance in cents, or INVALID
     */
    public static int validateBalance(String input)
    {
        return parseCents(input);
    }


    /**
     * Parses a non-negative whole number made only of the digits 0-9.
     *
     * @param input
     *            the text to parse
     * @return the number, or INVALID
     */
    private static int parseWholeNumber(String input)
    {
        if (input == null)
        {
            return INVALID;
        }
        String trimmed = input.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_DIGITS)
        {
            return INVALID;
        }
        for (int i = 0; i < trimmed.length(); i++)
        {
            char c = trimmed.charAt(i);
            if (c < '0' || c > '9')
            {
                return INVALID;
            }
        }
        return Integer.parseInt(trimmed);
    }


    /**
     * Parses a dollar amount with at most two decimal places into cents.
     *
     * @param input
     *            the text to parse
     * @return the amount in cents (0 to MAX_MONEY_CENTS), or INVALID
     */
    private static int parseCents(String input)
    {
        if (input == null)
        {
            return INVALID;
        }
        String s = input.trim();
        // digits with optional .d or .dd, or just .d / .dd
        if (!s.matches("\\d{1,5}(\\.\\d{0,2})?|\\.\\d{1,2}"))
        {
            return INVALID;
        }
        int dot = s.indexOf('.');
        String dollarPart = (dot == -1) ? s : s.substring(0, dot);
        StringBuilder centPart =
            new StringBuilder((dot == -1) ? "" : s.substring(dot + 1));
        while (centPart.length() < 2)
        {
            centPart.append('0');
        }
        int dollars = dollarPart.isEmpty() ? 0 : Integer.parseInt(dollarPart);
        int cents = dollars * 100 + Integer.parseInt(centPart.toString());
        if (cents > MAX_MONEY_CENTS)
        {
            return INVALID;
        }
        return cents;
    }
}
