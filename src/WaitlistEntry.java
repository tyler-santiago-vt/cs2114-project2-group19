/**
 * One party waiting in line: its ticket number, name, party size,
 * priority number, and wallet balance. A lower priority number is
 * served sooner. Two entries are equal when they have the same ticket
 * number, so LinkedChain.remove() deletes the exact party that was found,
 * even if two parties share a name and size.
 *
 * @author Tyler Santiago
 * @version October 2026
 */
public class WaitlistEntry
{
    private final int ticketNumber;
    private final String name;
    private final int partySize;
    private int priorityNumber;
    private int walletBalanceCents;

    /**
     * Creates a party. Waitlist is the class that chooses the ticket and
     * priority numbers.
     *
     * @param ticketNumber       unique ticket number, at least 1
     * @param name               party name, not blank, at most 30
     *                           characters after trimming
     * @param partySize          number of people, 1 to 12
     * @param priorityNumber     serving priority (lower is served sooner)
     * @param walletBalanceCents money available, at least 0
     * @throws IllegalArgumentException if any value is invalid
     */
    public WaitlistEntry(int ticketNumber, String name, int partySize,
        int priorityNumber, int walletBalanceCents)
    {
        if (ticketNumber < 1)
        {
            throw new IllegalArgumentException(
                "Ticket number must be at least 1");
        }
        String checkedName = InputValidator.validateName(name);
        if (checkedName == null)
        {
            throw new IllegalArgumentException(
                "Name must be 1 to " + InputValidator.MAX_NAME_LENGTH
                + " characters and not blank");
        }
        if (partySize < InputValidator.MIN_PARTY_SIZE
            || partySize > InputValidator.MAX_PARTY_SIZE)
        {
            throw new IllegalArgumentException(
                "Party size must be from " + InputValidator.MIN_PARTY_SIZE
                + " to " + InputValidator.MAX_PARTY_SIZE);
        }
        if (walletBalanceCents < 0)
        {
            throw new IllegalArgumentException(
                "Wallet balance cannot be negative");
        }
        this.ticketNumber = ticketNumber;
        this.name = checkedName;
        this.partySize = partySize;
        this.priorityNumber = priorityNumber;
        this.walletBalanceCents = walletBalanceCents;
    }

    /**
     * Returns the party's ticket number.
     *
     * @return the ticket number
     */
    public int getTicketNumber()
    {
        return ticketNumber;
    }

    /**
     * Returns the party's name.
     *
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the number of people in the party.
     *
     * @return the party size
     */
    public int getPartySize()
    {
        return partySize;
    }

    /**
     * Returns the party's current priority number.
     *
     * @return the priority number (lower is served sooner)
     */
    public int getPriorityNumber()
    {
        return priorityNumber;
    }

    /**
     * Changes the party's priority number. Any int is allowed, including
     * zero and negatives, because a skip gives a number lower than the
     * current lowest.
     *
     * @param priorityNumber the new priority number
     */
    public void setPriorityNumber(int priorityNumber)
    {
        this.priorityNumber = priorityNumber;
    }

    /**
     * Returns the party's current wallet balance.
     *
     * @return the balance in cents
     */
    public int getWalletBalanceCents()
    {
        return walletBalanceCents;
    }

    /**
     * Changes the party's wallet balance.
     *
     * @param balance the new balance in cents, at least 0
     * @throws IllegalArgumentException if balance is negative
     */
    public void setWalletBalanceCents(int balance)
    {
        if (balance < 0)
        {
            throw new IllegalArgumentException(
                "Wallet balance cannot be negative");
        }
        walletBalanceCents = balance;
    }

    /**
     * Two entries are equal when they have the same ticket number.
     *
     * @param obj the object to compare with
     * @return true if obj is a WaitlistEntry with the same ticket number
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        WaitlistEntry other = (WaitlistEntry)obj;
        return ticketNumber == other.ticketNumber;
    }

    /**
     * Returns a hash code consistent with equals.
     *
     * @return a hash code based on the ticket number
     */
    @Override
    public int hashCode()
    {
        return Integer.hashCode(ticketNumber);
    }

    /**
     * Returns a short description, e.g. "#3 Smith (party of 2)".
     *
     * @return a readable description of this party
     */
    @Override
    public String toString()
    {
        return "#" + ticketNumber + " " + name + " (party of " + partySize
            + ")";
    }
}
