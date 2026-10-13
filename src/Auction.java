/**
 * One spot auction (our stretch goal): the seller's ticket number and a
 * LinkedChain<Bid> of offers. Auction only collects bids and picks the winner.
 * When it closes, Waitlist swaps the two parties' priority numbers and Ledger
 * records the transfer; GrubQueueApp coordinates that.
 *
 * @author Tyler Santiago
 * @version October 2026
 */
public class Auction
{
    private final int sellerTicketNumber;
    private LinkedChain<Bid> bids;

    /**
     * Opens an auction with no bids.
     *
     * @param sellerTicketNumber
     *            ticket number of the party selling its spot, at least 1
     * @throws IllegalArgumentException
     *             if the ticket number is less than 1
     */
    public Auction(int sellerTicketNumber)
    {
        if (sellerTicketNumber < 1)
        {
            throw new IllegalArgumentException(
                "Seller ticket number must be at least 1");
        }
        this.sellerTicketNumber = sellerTicketNumber;
        bids = new LinkedChain<Bid>();
    }


    /**
     * Returns the seller's ticket number. O(1).
     *
     * @return the seller's ticket number
     */
    public int getSellerTicketNumber()
    {
        return sellerTicketNumber;
    }


    /**
     * Adds a bid. The seller cannot bid on their own spot. O(1).
     *
     * @param bid
     *            the bid to add
     * @return true if added; false if bid is null or is from the seller
     */
    public boolean placeBid(Bid bid)
    {
        if (bid == null || bid.getBidderTicketNumber() == sellerTicketNumber)
        {
            return false;
        }
        bids.add(bid);
        return true;
    }


    /**
     * Returns the highest bid. If two bids tie, the earlier one wins.
     * LinkedChain.add() puts new bids at the front, so toArray() gives newest
     * first; we scan from the back (oldest) so a later bid must be strictly
     * higher to win. O(b), where b is the number of bids.
     *
     * @return the highest bid, or null if there are no bids
     */
    public Bid getHighestBid()
    {
        Bid[] newestFirst = bids.toArray(new Bid[bids.size()]);
        Bid best = null;
        for (int i = newestFirst.length - 1; i >= 0; i--)
        {
            if (best == null
                || newestFirst[i].getAmountCents() > best.getAmountCents())
            {
                best = newestFirst[i];
            }
        }
        return best;
    }
}
