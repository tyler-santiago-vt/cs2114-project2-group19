import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for Payment. Bad input for the getters is tested through the
 * constructor, which refuses to build a payment with invalid data.
 *
 * @author Tyler Santiago
 * @version October 2026
 */
public class PaymentTest
{
    private Payment payment;

    /**
     * Creates a normal $6.50 skip payment before each test.
     */
    @Before
    public void setUp()
    {
        payment = new Payment(650, 4, Payment.RESTAURANT, Payment.TYPE_SKIP);
    }


    /** Normal: returns the amount in cents. */
    @Test
    public void testGetAmountCents()
    {
        assertEquals(650, payment.getAmountCents());
    }


    /** Bad input: zero and negative amounts are rejected. */
    @Test
    public void testGetAmountCentsBadInput()
    {
        assertThrows(
            IllegalArgumentException.class,
            () -> new Payment(0, 4, Payment.RESTAURANT, Payment.TYPE_SKIP));
        assertThrows(
            IllegalArgumentException.class,
            () -> new Payment(-650, 4, Payment.RESTAURANT, Payment.TYPE_SKIP));
    }


    /** Normal: returns the payer's ticket number. */
    @Test
    public void testGetPayerTicketNumber()
    {
        assertEquals(4, payment.getPayerTicketNumber());
    }


    /** Bad input: ticket 0 is rejected. */
    @Test
    public void testGetPayerTicketNumberBadInput()
    {
        assertThrows(
            IllegalArgumentException.class,
            () -> new Payment(650, 0, Payment.RESTAURANT, Payment.TYPE_SKIP));
    }


    /** Normal: returns the recipient. */
    @Test
    public void testGetRecipient()
    {
        assertEquals("Restaurant", payment.getRecipient());
        assertEquals(
            "Ticket #2",
            new Payment(100, 4, "Ticket #2", Payment.TYPE_AUCTION)
                .getRecipient());
    }


    /** Bad input: null or blank recipient is rejected. */
    @Test
    public void testGetRecipientBadInput()
    {
        assertThrows(
            IllegalArgumentException.class,
            () -> new Payment(650, 4, null, Payment.TYPE_SKIP));
        assertThrows(
            IllegalArgumentException.class,
            () -> new Payment(650, 4, "  ", Payment.TYPE_SKIP));
    }


    /** Normal: returns "skip" or "auction". */
    @Test
    public void testGetTransactionType()
    {
        assertEquals("skip", payment.getTransactionType());
        assertEquals(
            "auction",
            new Payment(100, 4, "Ticket #2", Payment.TYPE_AUCTION)
                .getTransactionType());
    }


    /** Bad input: null or unknown type is rejected. */
    @Test
    public void testGetTransactionTypeBadInput()
    {
        assertThrows(
            IllegalArgumentException.class,
            () -> new Payment(650, 4, Payment.RESTAURANT, null));
        assertThrows(
            IllegalArgumentException.class,
            () -> new Payment(650, 4, Payment.RESTAURANT, "refund"));
    }


    /** Edge: identical payments are separate records, not equal. */
    @Test
    public void testIdenticalPaymentsAreDistinct()
    {
        Payment twin =
            new Payment(650, 4, Payment.RESTAURANT, Payment.TYPE_SKIP);
        assertFalse(payment.equals(twin));
        assertEquals(payment.getAmountCents(), twin.getAmountCents());
    }
}
