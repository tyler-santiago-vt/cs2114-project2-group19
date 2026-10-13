import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for WaitlistEntry. Bad input for the getters is tested through the
 * constructor, which refuses to build an entry with invalid data.
 *
 * @author Tyler Santiago
 * @version October 2026
 */
public class WaitlistEntryTest
{
    private WaitlistEntry entry;

    /**
     * Creates a normal entry before each test.
     */
    @Before
    public void setUp()
    {
        entry = new WaitlistEntry(1, "Name", 4, 1, 1000);
    }

    // getTicketNumber


    /** Normal: returns the ticket number given. */
    @Test
    public void testGetTicketNumber()
    {
        assertEquals(1, entry.getTicketNumber());
    }


    /** Bad input: ticket 0 or negative is rejected. */
    @Test
    public void testGetTicketNumberBadInput()
    {
        assertThrows(
            IllegalArgumentException.class,
            () -> new WaitlistEntry(0, "Name", 4, 1, 1000));
        assertThrows(
            IllegalArgumentException.class,
            () -> new WaitlistEntry(-3, "Name", 4, 1, 1000));
    }

    // getName


    /** Normal: returns the name, trimmed. */
    @Test
    public void testGetName()
    {
        assertEquals("Name", entry.getName());
        assertEquals(
            "Smith",
            new WaitlistEntry(2, "  Smith  ", 2, 2, 0).getName());
    }


    /** Bad input: null, blank, and over-30-character names. */
    @Test
    public void testGetNameBadInput()
    {
        assertThrows(
            IllegalArgumentException.class,
            () -> new WaitlistEntry(1, null, 4, 1, 1000));
        assertThrows(
            IllegalArgumentException.class,
            () -> new WaitlistEntry(1, "   ", 4, 1, 1000));
        assertThrows(
            IllegalArgumentException.class,
            () -> new WaitlistEntry(1, repeat('A', 31), 4, 1, 1000));
    }

    // getPartySize


    /** Normal: returns the party size, including both limits. */
    @Test
    public void testGetPartySize()
    {
        assertEquals(4, entry.getPartySize());
        assertEquals(1, new WaitlistEntry(2, "A", 1, 2, 0).getPartySize());
        assertEquals(12, new WaitlistEntry(3, "B", 12, 3, 0).getPartySize());
    }


    /** Bad input: party sizes 0 and 13 are rejected. */
    @Test
    public void testGetPartySizeBadInput()
    {
        assertThrows(
            IllegalArgumentException.class,
            () -> new WaitlistEntry(1, "Name", 0, 1, 1000));
        assertThrows(
            IllegalArgumentException.class,
            () -> new WaitlistEntry(1, "Name", 13, 1, 1000));
    }

    // getPriorityNumber / setPriorityNumber


    /** Normal: returns the priority given at creation. */
    @Test
    public void testGetPriorityNumber()
    {
        assertEquals(1, entry.getPriorityNumber());
    }


    /** Edge: after a skip, priority can be zero or negative. */
    @Test
    public void testGetPriorityNumberAfterNegativeSet()
    {
        entry.setPriorityNumber(-2);
        assertEquals(-2, entry.getPriorityNumber());
    }


    /** Normal: setting a new priority changes it. */
    @Test
    public void testSetPriorityNumber()
    {
        entry.setPriorityNumber(7);
        assertEquals(7, entry.getPriorityNumber());
    }


    /** Edge: setPriorityNumber accepts 0 by design (no exception). */
    @Test
    public void testSetPriorityNumberZero()
    {
        entry.setPriorityNumber(0);
        assertEquals(0, entry.getPriorityNumber());
    }

    // getWalletBalanceCents / setWalletBalanceCents


    /** Normal: returns the starting balance. */
    @Test
    public void testGetWalletBalanceCents()
    {
        assertEquals(1000, entry.getWalletBalanceCents());
    }


    /** Bad input: a negative starting balance is rejected. */
    @Test
    public void testGetWalletBalanceCentsBadInput()
    {
        assertThrows(
            IllegalArgumentException.class,
            () -> new WaitlistEntry(1, "Name", 4, 1, -1));
    }


    /** Normal: setting a new balance (including 0) changes it. */
    @Test
    public void testSetWalletBalanceCents()
    {
        entry.setWalletBalanceCents(400);
        assertEquals(400, entry.getWalletBalanceCents());
        entry.setWalletBalanceCents(0);
        assertEquals(0, entry.getWalletBalanceCents());
    }


    /** Bad input: a negative balance is rejected and nothing changes. */
    @Test
    public void testSetWalletBalanceCentsBadInput()
    {
        assertThrows(
            IllegalArgumentException.class,
            () -> entry.setWalletBalanceCents(-50));
        assertEquals(1000, entry.getWalletBalanceCents());
    }

    // equals / hashCode / toString


    /** Normal: same ticket means equal, even with a different name. */
    @Test
    public void testEquals()
    {
        WaitlistEntry sameTicket = new WaitlistEntry(1, "Other", 2, 9, 0);
        assertTrue(entry.equals(sameTicket));
        assertTrue(entry.equals(entry));
    }


    /** Bad input: different ticket, null, or another type. */
    @Test
    public void testEqualsBadInput()
    {
        assertFalse(entry.equals(new WaitlistEntry(2, "Name", 4, 1, 1000)));
        assertFalse(entry.equals(null));
        assertFalse(entry.equals("Name"));
    }


    /** Normal: equal entries have equal hash codes. */
    @Test
    public void testHashCode()
    {
        assertEquals(
            entry.hashCode(),
            new WaitlistEntry(1, "Other", 2, 9, 0).hashCode());
    }


    /** Bad input: different tickets give different hash codes here. */
    @Test
    public void testHashCodeDifferentTickets()
    {
        assertFalse(
            entry.hashCode() == new WaitlistEntry(2, "Name", 4, 1, 1000)
                .hashCode());
    }


    /** Normal: toString shows ticket, name, and size. */
    @Test
    public void testToString()
    {
        assertEquals("#1 Name (party of 4)", entry.toString());
    }


    /** Edge: toString uses the trimmed name. */
    @Test
    public void testToStringTrimmedName()
    {
        assertEquals(
            "#5 Lee (party of 1)",
            new WaitlistEntry(5, "  Lee ", 1, 5, 0).toString());
    }


    /**
     * Builds a string of the given length (Java 8 has no String.repeat).
     *
     * @param c
     *            the character to repeat
     * @param count
     *            how many times
     * @return the repeated string
     */
    private static String repeat(char c, int count)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++)
        {
            sb.append(c);
        }
        return sb.toString();
    }
}
