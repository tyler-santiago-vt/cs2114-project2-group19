/**
 * Manages the line and enforces its rules. Parties are stored in a
 * LinkedChain<WaitlistEntry>, which keeps no meaningful order; serving order
 * comes entirely from priority numbers (lower is served sooner). This is the
 * only class that assigns or changes priority numbers. Priority rule (our
 * answer to the "unknown" in the scope): - joining gives nextPriorityNumber,
 * which only ever increases, so a new party is always behind everyone; -
 * skipping gives (current lowest priority - 1), so the skipper is strictly
 * ahead of everyone; - an auction swaps two existing numbers, so no new number
 * is made. No step ever creates a number that is already in use, so two parties
 * can never tie. Numbers may go to zero or negative after skips; that is fine
 * because only comparisons matter.
 *
 * @author Tyler Santiago
 * @version October 2026
 */
public class Waitlist
{
    /** Most parties allowed in line. */
    public static final int MAX_PARTIES = 50;

    /** Price to skip, per party passed, in cents ($2.00). */
    public static final int SKIP_PRICE_PER_PARTY_CENTS = 200;

    /** Returned by getPosition and getSkipPrice for an unknown ticket. */
    public static final int NOT_FOUND = -1;

    private LinkedChain<WaitlistEntry> entries;
    private int nextTicketNumber;
    private int nextPriorityNumber;

    /**
     * Creates an empty waitlist.
     */
    public Waitlist()
    {
        entries = new LinkedChain<WaitlistEntry>();
        nextTicketNumber = 1;
        nextPriorityNumber = 1;
    }


    /**
     * Creates a new party, assigns its ticket and priority, and adds it to the
     * line. Ticket numbers are never reused. O(1).
     *
     * @param name
     *            the party name
     * @param partySize
     *            the party size, 1 to 12
     * @param walletBalanceCents
     *            the starting wallet balance, at least 0
     * @return the new entry, or null if the line already has MAX_PARTIES
     * @throws IllegalArgumentException
     *             if name, size, or balance is invalid (InputValidator should
     *             have caught these first)
     */
    public
        WaitlistEntry
        join(String name, int partySize, int walletBalanceCents)
    {
        if (entries.size() >= MAX_PARTIES)
        {
            return null;
        }
        // Build first: if the data is bad this throws before any counter
        // changes, so no ticket number is wasted.
        WaitlistEntry entry = new WaitlistEntry(
            nextTicketNumber,
            name,
            partySize,
            nextPriorityNumber,
            walletBalanceCents);
        entries.add(entry);
        nextTicketNumber++;
        nextPriorityNumber++;
        return entry;
    }


    /**
     * Finds the party with the given ticket number. O(n).
     *
     * @param ticketNumber
     *            the ticket to look for
     * @return the matching entry, or null if none exists
     */
    public WaitlistEntry findByTicket(int ticketNumber)
    {
        for (WaitlistEntry entry : snapshot())
        {
            if (entry.getTicketNumber() == ticketNumber)
            {
                return entry;
            }
        }
        return null;
    }


    /**
     * Finds a party only if the ticket number AND name match, so a customer
     * cannot act on someone else's ticket. The name comparison ignores case and
     * surrounding spaces. O(n).
     *
     * @param ticketNumber
     *            the ticket given by the customer
     * @param name
     *            the name given by the customer
     * @return the matching entry, or null if there is no match
     */
    public WaitlistEntry findParty(int ticketNumber, String name)
    {
        if (name == null)
        {
            return null;
        }
        WaitlistEntry entry = findByTicket(ticketNumber);
        if (entry == null || !entry.getName().equalsIgnoreCase(name.trim()))
        {
            return null;
        }
        return entry;
    }


    /**
     * Returns the party's 1-based position in line: 1 plus the number of
     * parties with a lower priority number. O(n).
     *
     * @param ticketNumber
     *            the ticket to look up
     * @return the position, or NOT_FOUND if the ticket is not in line
     */
    public int getPosition(int ticketNumber)
    {
        WaitlistEntry target = findByTicket(ticketNumber);
        if (target == null)
        {
            return NOT_FOUND;
        }
        int ahead = 0;
        for (WaitlistEntry entry : snapshot())
        {
            if (entry.getPriorityNumber() < target.getPriorityNumber())
            {
                ahead++;
            }
        }
        return ahead + 1;
    }


    /**
     * Calculates the price to skip to the front: $2 for each party ahead. A
     * party already at the front gets a price of 0. O(n).
     *
     * @param ticketNumber
     *            the ticket to price
     * @return the price in cents, or NOT_FOUND if the ticket is not in line
     */
    public int getSkipPrice(int ticketNumber)
    {
        int position = getPosition(ticketNumber);
        if (position == NOT_FOUND)
        {
            return NOT_FOUND;
        }
        return (position - 1) * SKIP_PRICE_PER_PARTY_CENTS;
    }


    /**
     * Gives the selected party a priority number lower than everyone else's.
     * This does not handle money; the caller charges first. O(n).
     *
     * @param ticketNumber
     *            the ticket to move
     * @return true if the party moved; false if the ticket is not in line or
     *             the party is already next
     */
    public boolean moveToFront(int ticketNumber)
    {
        WaitlistEntry target = findByTicket(ticketNumber);
        if (target == null)
        {
            return false;
        }
        WaitlistEntry first = peekNext();
        if (first.equals(target))
        {
            return false;
        }
        target.setPriorityNumber(first.getPriorityNumber() - 1);
        return true;
    }


    /**
     * Swaps two parties' priority numbers, so they trade places in line. Used
     * when an auction closes. O(n).
     *
     * @param firstTicket
     *            one party's ticket
     * @param secondTicket
     *            the other party's ticket
     * @return true if swapped; false if the tickets are the same or either is
     *             not in line (the line is then unchanged)
     */
    public boolean swapPriorities(int firstTicket, int secondTicket)
    {
        if (firstTicket == secondTicket)
        {
            return false;
        }
        WaitlistEntry first = findByTicket(firstTicket);
        WaitlistEntry second = findByTicket(secondTicket);
        if (first == null || second == null)
        {
            return false;
        }
        int temp = first.getPriorityNumber();
        first.setPriorityNumber(second.getPriorityNumber());
        second.setPriorityNumber(temp);
        return true;
    }


    /**
     * Finds, removes, and returns the party with the lowest priority number.
     * O(n).
     *
     * @return the served party, or null if the line is empty
     */
    public WaitlistEntry serveNext()
    {
        WaitlistEntry next = peekNext();
        if (next != null)
        {
            entries.remove(next);
        }
        return next;
    }


    /**
     * Removes the party with the given ticket number. O(n).
     *
     * @param ticketNumber
     *            the ticket to remove
     * @return true if a party was removed, false if the ticket is not in line
     */
    public boolean removeByTicket(int ticketNumber)
    {
        WaitlistEntry target = findByTicket(ticketNumber);
        return target != null && entries.remove(target);
    }


    /**
     * Returns the current parties sorted by priority (serving order). Uses
     * selection sort on a copy, so the chain itself is unchanged. O(n^2).
     *
     * @return a new array, first-to-be-served first; empty if no one is waiting
     */
    public WaitlistEntry[] getServingOrder()
    {
        WaitlistEntry[] order = snapshot();
        for (int i = 0; i < order.length - 1; i++)
        {
            int minIndex = i;
            for (int j = i + 1; j < order.length; j++)
            {
                if (order[j].getPriorityNumber() < order[minIndex]
                    .getPriorityNumber())
                {
                    minIndex = j;
                }
            }
            WaitlistEntry temp = order[i];
            order[i] = order[minIndex];
            order[minIndex] = temp;
        }
        return order;
    }


    /**
     * Returns the number of parties currently waiting. O(1).
     *
     * @return the number of parties
     */
    public int size()
    {
        return entries.size();
    }


    /**
     * Returns the party that would be served next, without removing it. O(n).
     *
     * @return the entry with the lowest priority number, or null if the line is
     *             empty
     */
    private WaitlistEntry peekNext()
    {
        WaitlistEntry best = null;
        for (WaitlistEntry entry : snapshot())
        {
            if (best == null
                || entry.getPriorityNumber() < best.getPriorityNumber())
            {
                best = entry;
            }
        }
        return best;
    }


    /**
     * Copies the chain into a correctly sized array. O(n).
     *
     * @return every entry currently waiting
     */
    private WaitlistEntry[] snapshot()
    {
        return entries.toArray(new WaitlistEntry[entries.size()]);
    }
}
