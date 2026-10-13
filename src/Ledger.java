/**
 * Tracks every transaction in a LinkedChain<Payment>. Duplicates are expected
 * (two $4 skips are two real payments), which is why a bag-style chain fits
 * here.
 *
 * @author Jonathan Davis
 * @version October 2026
 */
public class Ledger
{
    private LinkedChain<Payment> payments;

    /**
     * Creates an empty ledger.
     */
    public Ledger()
    {
        payments = new LinkedChain<Payment>();
    }


    /**
     * Adds a transaction to the ledger. O(1).
     *
     * @param payment
     *            the payment to record
     * @return true if recorded, false if payment is null
     */
    public boolean recordPayment(Payment payment)
    {
        if (payment == null)
        {
            return false;
        }
        payments.add(payment);
        return true;
    }


    /**
     * Returns the total paid to the restaurant. Payments between customers
     * (auctions) are not restaurant revenue. O(n).
     *
     * @return total restaurant revenue in cents; 0 if there are none
     */
    public int getRestaurantRevenue()
    {
        int total = 0;
        for (Payment payment : payments.toArray(new Payment[payments.size()]))
        {
            if (Payment.RESTAURANT.equals(payment.getRecipient()))
            {
                total += payment.getAmountCents();
            }
        }
        return total;
    }


    /**
     * Returns all recorded payments, oldest first. LinkedChain.add() puts new
     * entries at the front, so toArray() gives newest first; this method
     * reverses that. O(n).
     *
     * @return a new array of payments, oldest first; empty if none
     */
    public Payment[] getHistory()
    {
        Payment[] newestFirst = payments.toArray(new Payment[payments.size()]);
        Payment[] oldestFirst = new Payment[newestFirst.length];
        for (int i = 0; i < newestFirst.length; i++)
        {
            oldestFirst[i] = newestFirst[newestFirst.length - 1 - i];
        }
        return oldestFirst;
    }
}
