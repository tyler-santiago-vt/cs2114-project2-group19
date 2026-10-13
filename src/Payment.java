/**
 * One transaction: the amount in cents, the payer's ticket number, the
 * recipient (the restaurant, or another customer's ticket for auctions), and
 * the transaction type ("skip" or "auction"). Payment does not override equals,
 * so two identical $4 payments are still two separate records in the Ledger.
 *
 * @author Nana Ababio
 * @version October 2026
 */
public class Payment
{
    /** Recipient name used when the restaurant is paid. */
    public static final String RESTAURANT = "Restaurant";

    /** Transaction type for paying to skip the line. */
    public static final String TYPE_SKIP = "skip";

    /** Transaction type for an auction (stretch goal). */
    public static final String TYPE_AUCTION = "auction";

    private final int amountCents;
    private final int payerTicketNumber;
    private final String recipient;
    private final String transactionType;

    /**
     * Creates a payment record.
     *
     * @param amountCents
     *            amount paid in cents, greater than 0
     * @param payerTicketNumber
     *            ticket number of the payer, at least 1
     * @param recipient
     *            who receives the money, not blank
     * @param transactionType
     *            TYPE_SKIP or TYPE_AUCTION
     * @throws IllegalArgumentException
     *             if any value is invalid
     */
    public Payment(
        int amountCents,
        int payerTicketNumber,
        String recipient,
        String transactionType)
    {
        if (amountCents <= 0)
        {
            throw new IllegalArgumentException(
                "Payment amount must be positive");
        }
        if (payerTicketNumber < 1)
        {
            throw new IllegalArgumentException(
                "Payer ticket number must be at least 1");
        }
        if (recipient == null || recipient.trim().isEmpty())
        {
            throw new IllegalArgumentException("Recipient must not be blank");
        }
        if (!TYPE_SKIP.equals(transactionType)
            && !TYPE_AUCTION.equals(transactionType))
        {
            throw new IllegalArgumentException(
                "Transaction type must be \"" + TYPE_SKIP + "\" or \""
                    + TYPE_AUCTION + "\"");
        }
        this.amountCents = amountCents;
        this.payerTicketNumber = payerTicketNumber;
        this.recipient = recipient.trim();
        this.transactionType = transactionType;
    }


    /**
     * Returns the transaction amount.
     *
     * @return the amount in cents
     */
    public int getAmountCents()
    {
        return amountCents;
    }


    /**
     * Returns the payer's ticket number.
     *
     * @return the payer's ticket number
     */
    public int getPayerTicketNumber()
    {
        return payerTicketNumber;
    }


    /**
     * Returns the recipient of the payment.
     *
     * @return the recipient
     */
    public String getRecipient()
    {
        return recipient;
    }


    /**
     * Returns the transaction type.
     *
     * @return "skip" or "auction"
     */
    public String getTransactionType()
    {
        return transactionType;
    }
}
