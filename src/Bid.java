/**
 * One offer in a spot auction: the bidder's ticket number and the amount
 * offered in cents. Bid is the third element type stored in the generic
 * LinkedChain (inside Auction). It does not override equals, so two identical
 * bids are still two separate offers.
 *
 * @author Tyler Santiago
 * @version October 2026
 */
public class Bid
{
    private final int bidderTicketNumber;
    private final int amountCents;

    /**
     * Creates a bid.
     *
     * @param bidderTicketNumber
     *            ticket number of the bidder, at least 1
     * @param amountCents
     *            amount offered in cents, greater than 0
     * @throws IllegalArgumentException
     *             if either value is invalid
     */
    public Bid(int bidderTicketNumber, int amountCents)
    {
        if (bidderTicketNumber < 1)
        {
            throw new IllegalArgumentException(
                "Bidder ticket number must be at least 1");
        }
        if (amountCents <= 0)
        {
            throw new IllegalArgumentException("Bid must be positive");
        }
        this.bidderTicketNumber = bidderTicketNumber;
        this.amountCents = amountCents;
    }


    /**
     * Returns the bidder's ticket number. O(1).
     *
     * @return the bidder's ticket number
     */
    public int getBidderTicketNumber()
    {
        return bidderTicketNumber;
    }


    /**
     * Returns the amount offered. O(1).
     *
     * @return the amount in cents
     */
    public int getAmountCents()
    {
        return amountCents;
    }
}
