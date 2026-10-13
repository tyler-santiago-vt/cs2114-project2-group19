import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Tests for InputValidator. Rejected numbers come back as
 * InputValidator.INVALID; rejected names come back as null.
 *
 * @author Nana Ababio
 * @version October 2026
 */
public class InputValidatorTest
{
    private static final int INVALID = InputValidator.INVALID;

    // validateMenuChoice

    /** Normal: 2 with choices 1-4 is accepted (spaces are ignored). */
    @Test
    public void testValidateMenuChoice()
    {
        assertEquals(2, InputValidator.validateMenuChoice("2", 1, 4));
        assertEquals(4, InputValidator.validateMenuChoice(" 4 ", 1, 4));
    }


    /** Bad input: out of range, non-numeric, blank, null. */
    @Test
    public void testValidateMenuChoiceBadInput()
    {
        assertEquals(INVALID, InputValidator.validateMenuChoice("7", 1, 4));
        assertEquals(INVALID, InputValidator.validateMenuChoice("0", 1, 4));
        assertEquals(INVALID, InputValidator.validateMenuChoice("abc", 1, 4));
        assertEquals(INVALID, InputValidator.validateMenuChoice("", 1, 4));
        assertEquals(INVALID, InputValidator.validateMenuChoice("-1", 1, 4));
        assertEquals(INVALID, InputValidator.validateMenuChoice("2.5", 1, 4));
        assertEquals(INVALID, InputValidator.validateMenuChoice(null, 1, 4));
        assertEquals(
            INVALID,
            InputValidator.validateMenuChoice("99999999999", 1, 4));
    }

    // validateName


    /** Normal: a name is accepted and trimmed; 30 chars is allowed. */
    @Test
    public void testValidateName()
    {
        assertEquals("Name", InputValidator.validateName("Name"));
        assertEquals("Mary Ann", InputValidator.validateName("  Mary Ann "));
        String thirty = repeat('A', 30);
        assertEquals(thirty, InputValidator.validateName(thirty));
    }


    /** Bad input: blank, only spaces, 31 chars, null. */
    @Test
    public void testValidateNameBadInput()
    {
        assertNull(InputValidator.validateName(""));
        assertNull(InputValidator.validateName("   "));
        assertNull(InputValidator.validateName(repeat('A', 31)));
        assertNull(InputValidator.validateName(null));
    }

    // validatePartySize


    /** Normal: 4 is accepted, as are the limits 1 and 12. */
    @Test
    public void testValidatePartySize()
    {
        assertEquals(4, InputValidator.validatePartySize("4"));
        assertEquals(1, InputValidator.validatePartySize("1"));
        assertEquals(12, InputValidator.validatePartySize("12"));
    }


    /** Bad input: 15, 0, negative, non-numeric, null. */
    @Test
    public void testValidatePartySizeBadInput()
    {
        assertEquals(INVALID, InputValidator.validatePartySize("15"));
        assertEquals(INVALID, InputValidator.validatePartySize("0"));
        assertEquals(INVALID, InputValidator.validatePartySize("-3"));
        assertEquals(INVALID, InputValidator.validatePartySize("four"));
        assertEquals(INVALID, InputValidator.validatePartySize(null));
    }

    // validateTicketNumber


    /** Normal: 12 is accepted (spaces are ignored). */
    @Test
    public void testValidateTicketNumber()
    {
        assertEquals(12, InputValidator.validateTicketNumber("12"));
        assertEquals(12, InputValidator.validateTicketNumber(" 12 "));
    }


    /** Bad input: abc, 0, negative, "#12", blank, null. */
    @Test
    public void testValidateTicketNumberBadInput()
    {
        assertEquals(INVALID, InputValidator.validateTicketNumber("abc"));
        assertEquals(INVALID, InputValidator.validateTicketNumber("0"));
        assertEquals(INVALID, InputValidator.validateTicketNumber("-12"));
        assertEquals(INVALID, InputValidator.validateTicketNumber(""));
        assertEquals(INVALID, InputValidator.validateTicketNumber("#12"));
        assertEquals(INVALID, InputValidator.validateTicketNumber(null));
    }

    // validateMoney


    /** Normal: 6.5 is converted to 650 cents; other formats work. */
    @Test
    public void testValidateMoney()
    {
        assertEquals(650, InputValidator.validateMoney("6.5"));
        assertEquals(650, InputValidator.validateMoney("6.50"));
        assertEquals(600, InputValidator.validateMoney("6"));
        assertEquals(75, InputValidator.validateMoney(".75"));
        assertEquals(1, InputValidator.validateMoney("0.01"));
        assertEquals(
            InputValidator.MAX_MONEY_CENTS,
            InputValidator.validateMoney("10000.00"));
    }


    /** Bad input: abc, zero, negative, too many decimals, too large. */
    @Test
    public void testValidateMoneyBadInput()
    {
        assertEquals(INVALID, InputValidator.validateMoney("abc"));
        assertEquals(INVALID, InputValidator.validateMoney("0"));
        assertEquals(INVALID, InputValidator.validateMoney("0.00"));
        assertEquals(INVALID, InputValidator.validateMoney("-5"));
        assertEquals(INVALID, InputValidator.validateMoney("6.505"));
        assertEquals(INVALID, InputValidator.validateMoney("10000.01"));
        assertEquals(INVALID, InputValidator.validateMoney("1.2.3"));
        assertEquals(INVALID, InputValidator.validateMoney("$6"));
        assertEquals(INVALID, InputValidator.validateMoney(""));
        assertEquals(INVALID, InputValidator.validateMoney(null));
    }

    // validateBalance


    /** Normal: like validateMoney, but zero is allowed. */
    @Test
    public void testValidateBalance()
    {
        assertEquals(0, InputValidator.validateBalance("0"));
        assertEquals(2000, InputValidator.validateBalance("20"));
    }


    /** Bad input: negative, non-numeric, null. */
    @Test
    public void testValidateBalanceBadInput()
    {
        assertEquals(INVALID, InputValidator.validateBalance("-1"));
        assertEquals(INVALID, InputValidator.validateBalance("twenty"));
        assertEquals(INVALID, InputValidator.validateBalance(null));
    }


    /**
     * Builds a string of the given length (Java 8 has no String.repeat).
     *
     * @param c
     *            the character to repeat
     * @param count
     *            how many times
     * @return the repeated string
     */
    private static String repeat(char c, int count)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++)
        {
            sb.append(c);
        }
        return sb.toString();
    }
}
