import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for Auction. The seller is ticket 1.
 *
 * @author Jonathan Davis
 * @version October 2026
 */
public class AuctionTest
{
    private Auction auction;

    /**
     * Opens an auction for ticket 1 before each test.
     */
    @Before
    public void setUp()
    {
        auction = new Auction(1);
    }

    // getSellerTicketNumber


    /** Normal: returns the seller's ticket. */
    @Test
    public void testGetSellerTicketNumber()
    {
        assertEquals(1, auction.getSellerTicketNumber());
    }


    /** Bad input: a seller ticket below 1 is rejected. */
    @Test
    public void testConstructorBadInput()
    {
        assertThrows(IllegalArgumentException.class, () -> new Auction(0));
    }

    // placeBid


    /** Normal: another party's bid is accepted. */
    @Test
    public void testPlaceBid()
    {
        Bid bid = new Bid(2, 300);
        assertTrue(auction.placeBid(bid));
        assertSame(bid, auction.getHighestBid());
    }


    /** Bad input: a null bid and the seller's own bid are refused. */
    @Test
    public void testPlaceBidBadInput()
    {
        assertFalse(auction.placeBid(null));
        assertFalse(auction.placeBid(new Bid(1, 300)));
        assertNull(auction.getHighestBid());
    }

    // getHighestBid


    /** Normal: the largest bid wins, wherever it was placed. */
    @Test
    public void testGetHighestBid()
    {
        auction.placeBid(new Bid(2, 300));
        Bid best = new Bid(3, 900);
        auction.placeBid(best);
        auction.placeBid(new Bid(4, 500));
        assertSame(best, auction.getHighestBid());
    }


    /** Bad input: an auction with no bids returns null (empty chain). */
    @Test
    public void testGetHighestBidEmpty()
    {
        assertNull(auction.getHighestBid());
    }


    /** Edge: on a tie, the earlier bid wins. */
    @Test
    public void testGetHighestBidTie()
    {
        Bid first = new Bid(2, 500);
        auction.placeBid(first);
        auction.placeBid(new Bid(3, 500));
        assertSame(first, auction.getHighestBid());
    }
}
