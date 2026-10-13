import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for Ledger.
 *
 * @author Jonathan Davis
 * @version October 2026
 */
public class LedgerTest
{
    private Ledger ledger;
    private Payment first;
    private Payment second;

    /**
     * Creates an empty ledger and two payments before each test.
     */
    @Before
    public void setUp()
    {
        ledger = new Ledger();
        first = new Payment(200, 1, Payment.RESTAURANT, Payment.TYPE_SKIP);
        second = new Payment(500, 2, Payment.RESTAURANT, Payment.TYPE_SKIP);
    }

    // recordPayment


    /** Normal: a $6.50 payment is recorded. */
    @Test
    public void testRecordPayment()
    {
        assertTrue(
            ledger.recordPayment(
                new Payment(650, 1, Payment.RESTAURANT, Payment.TYPE_SKIP)));
        assertEquals(1, ledger.getHistory().length);
    }


    /** Bad input: a null payment is not recorded. */
    @Test
    public void testRecordPaymentNull()
    {
        assertFalse(ledger.recordPayment(null));
        assertEquals(0, ledger.getHistory().length);
    }


    /** Edge: two identical payments are both kept (duplicates). */
    @Test
    public void testRecordPaymentDuplicates()
    {
        ledger.recordPayment(
            new Payment(400, 1, Payment.RESTAURANT, Payment.TYPE_SKIP));
        ledger.recordPayment(
            new Payment(400, 1, Payment.RESTAURANT, Payment.TYPE_SKIP));
        assertEquals(2, ledger.getHistory().length);
        assertEquals(800, ledger.getRestaurantRevenue());
    }

    // getRestaurantRevenue


    /** Normal: 200 + 500 cents is 700 cents. */
    @Test
    public void testGetRestaurantRevenue()
    {
        ledger.recordPayment(first);
        ledger.recordPayment(second);
        assertEquals(700, ledger.getRestaurantRevenue());
    }


    /** Bad input: no payments means 0 revenue. */
    @Test
    public void testGetRestaurantRevenueEmpty()
    {
        assertEquals(0, ledger.getRestaurantRevenue());
    }


    /** Edge: customer-to-customer payments are not restaurant revenue. */
    @Test
    public void testGetRestaurantRevenueSkipsAuctions()
    {
        ledger.recordPayment(first);
        ledger.recordPayment(
            new Payment(900, 3, "Ticket #1", Payment.TYPE_AUCTION));
        assertEquals(200, ledger.getRestaurantRevenue());
    }

    // getHistory


    /** Normal: returns both payments, oldest first. */
    @Test
    public void testGetHistory()
    {
        ledger.recordPayment(first);
        ledger.recordPayment(second);
        Payment[] history = ledger.getHistory();
        assertEquals(2, history.length);
        assertSame(first, history[0]);
        assertSame(second, history[1]);
    }


    /** Bad input: an empty ledger returns an empty array. */
    @Test
    public void testGetHistoryEmpty()
    {
        assertEquals(0, ledger.getHistory().length);
    }
}
