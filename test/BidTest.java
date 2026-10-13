import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

/**
 * Tests for Bid. Bad input for the getters is tested through the constructor,
 * which refuses to build a bid with invalid data.
 *
 * @author Tyler Santiago
 * @version October 2026
 */
public class BidTest
{
    /** Normal: returns the bidder's ticket number. */
    @Test
    public void testGetBidderTicketNumber()
    {
        assertEquals(3, new Bid(3, 500).getBidderTicketNumber());
    }


    /** Bad input: ticket 0 or negative is rejected. */
    @Test
    public void testGetBidderTicketNumberBadInput()
    {
        assertThrows(IllegalArgumentException.class, () -> new Bid(0, 500));
        assertThrows(IllegalArgumentException.class, () -> new Bid(-1, 500));
    }


    /** Normal: returns the amount, including the smallest (1 cent). */
    @Test
    public void testGetAmountCents()
    {
        assertEquals(500, new Bid(3, 500).getAmountCents());
        assertEquals(1, new Bid(3, 1).getAmountCents());
    }


    /** Bad input: zero and negative bids are rejected. */
    @Test
    public void testGetAmountCentsBadInput()
    {
        assertThrows(IllegalArgumentException.class, () -> new Bid(3, 0));
        assertThrows(IllegalArgumentException.class, () -> new Bid(3, -500));
    }
}
