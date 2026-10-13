import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for Waitlist. "full" is a line of three parties (tickets 1, 2, 3);
 * "empty" has no one in it.
 *
 * @author Tyler Santiago
 * @version October 2026
 */
public class WaitlistTest
{
    private Waitlist full;
    private Waitlist empty;

    /**
     * Builds a three-party line and an empty line before each test.
     */
    @Before
    public void setUp()
    {
        full = new Waitlist();
        full.join("Smith", 2, 1000);   // ticket 1
        full.join("Jones", 4, 1000);   // ticket 2
        full.join("Lee", 3, 1000);     // ticket 3
        empty = new Waitlist();
    }

    // join


    /** Normal: join assigns increasing tickets and adds the party. */
    @Test
    public void testJoin()
    {
        WaitlistEntry entry = empty.join("Name", 4, 1000);
        assertNotNull(entry);
        assertEquals(1, entry.getTicketNumber());
        assertEquals(1, empty.size());
        assertEquals(2, empty.join("Name", 4, 1000).getTicketNumber());
    }


    /** Bad input: a 50-party line refuses the 51st party. */
    @Test
    public void testJoinWhenFull()
    {
        for (int i = 0; i < Waitlist.MAX_PARTIES; i++)
        {
            assertNotNull(empty.join("Party" + i, 2, 0));
        }
        assertNull(empty.join("Late", 2, 0));
        assertEquals(50, empty.size());
    }


    /** Bad input: bad data throws and does not use up a ticket. */
    @Test
    public void testJoinBadDataKeepsTicketCounter()
    {
        assertThrows(
            IllegalArgumentException.class,
            () -> empty.join("  ", 4, 1000));
        assertThrows(
            IllegalArgumentException.class,
            () -> empty.join("Name", 15, 1000));
        assertEquals(0, empty.size());
        assertEquals(1, empty.join("Name", 4, 1000).getTicketNumber());
    }


    /** Edge: ticket numbers are never reused after removal. */
    @Test
    public void testJoinNeverReusesTickets()
    {
        full.removeByTicket(3);
        assertEquals(4, full.join("New", 1, 0).getTicketNumber());
    }

    // findByTicket


    /** Normal: an existing ticket returns its entry. */
    @Test
    public void testFindByTicket()
    {
        assertEquals("Jones", full.findByTicket(2).getName());
    }


    /** Bad input: unknown ticket, or an empty line, returns null. */
    @Test
    public void testFindByTicketMissing()
    {
        assertNull(full.findByTicket(99));
        assertNull(empty.findByTicket(1));
    }

    // findParty


    /** Normal: matching ticket and name (any case) finds the party. */
    @Test
    public void testFindParty()
    {
        assertEquals(2, full.findParty(2, "Jones").getTicketNumber());
        assertEquals(2, full.findParty(2, "  jones ").getTicketNumber());
    }


    /** Bad input: someone else's ticket, null name, unknown ticket. */
    @Test
    public void testFindPartyBadInput()
    {
        assertNull(full.findParty(2, "Smith"));
        assertNull(full.findParty(2, null));
        assertNull(full.findParty(99, "Jones"));
    }

    // getPosition


    /** Normal: ticket 3 has 2 parties ahead, so it is position 3. */
    @Test
    public void testGetPosition()
    {
        assertEquals(1, full.getPosition(1));
        assertEquals(3, full.getPosition(3));
    }


    /** Bad input: an unknown ticket returns NOT_FOUND. */
    @Test
    public void testGetPositionMissing()
    {
        assertEquals(Waitlist.NOT_FOUND, full.getPosition(99));
        assertEquals(Waitlist.NOT_FOUND, empty.getPosition(1));
    }

    // getSkipPrice


    /** Normal: 2 parties ahead costs $4.00; the front party pays 0. */
    @Test
    public void testGetSkipPrice()
    {
        assertEquals(400, full.getSkipPrice(3));
        assertEquals(0, full.getSkipPrice(1));
    }


    /** Bad input: an unknown ticket returns NOT_FOUND. */
    @Test
    public void testGetSkipPriceMissing()
    {
        assertEquals(Waitlist.NOT_FOUND, full.getSkipPrice(99));
    }

    // moveToFront


    /** Normal: ticket 3 moves to first; others shift back. */
    @Test
    public void testMoveToFront()
    {
        assertTrue(full.moveToFront(3));
        assertEquals(1, full.getPosition(3));
        assertEquals(2, full.getPosition(1));
        assertEquals(3, full.getPosition(2));
    }


    /** Bad input: unknown ticket returns false; line is unchanged. */
    @Test
    public void testMoveToFrontMissing()
    {
        assertFalse(full.moveToFront(99));
        assertEquals(1, full.getPosition(1));
        assertEquals(3, full.size());
        assertFalse(empty.moveToFront(1));
    }


    /** Bad input: a party already at the front is not moved. */
    @Test
    public void testMoveToFrontAlreadyNext()
    {
        int before = full.findByTicket(1).getPriorityNumber();
        assertFalse(full.moveToFront(1));
        assertEquals(before, full.findByTicket(1).getPriorityNumber());
    }


    /** Edge: repeated skips never create ties, even below zero. */
    @Test
    public void testRepeatedSkipsNoTies()
    {
        full.moveToFront(3);
        full.moveToFront(2);
        full.moveToFront(1);
        full.moveToFront(3);
        assertEquals(1, full.getPosition(3));
        assertEquals(2, full.getPosition(1));
        assertEquals(3, full.getPosition(2));
        int p1 = full.findByTicket(1).getPriorityNumber();
        int p2 = full.findByTicket(2).getPriorityNumber();
        int p3 = full.findByTicket(3).getPriorityNumber();
        assertTrue(p1 != p2 && p2 != p3 && p1 != p3);
        assertTrue(p3 < 0);
    }


    /** Edge: a party joining after a skip still goes to the back. */
    @Test
    public void testJoinAfterSkipGoesToBack()
    {
        full.moveToFront(3);
        WaitlistEntry late = full.join("Late", 2, 0);
        assertEquals(4, full.getPosition(late.getTicketNumber()));
    }

    // swapPriorities


    /** Normal: tickets 1 and 3 trade places; ticket 2 stays put. */
    @Test
    public void testSwapPriorities()
    {
        assertTrue(full.swapPriorities(1, 3));
        assertEquals(1, full.getPosition(3));
        assertEquals(2, full.getPosition(2));
        assertEquals(3, full.getPosition(1));
    }


    /** Bad input: unknown ticket or same ticket; line unchanged. */
    @Test
    public void testSwapPrioritiesBadInput()
    {
        assertFalse(full.swapPriorities(1, 99));
        assertFalse(full.swapPriorities(99, 1));
        assertFalse(full.swapPriorities(2, 2));
        assertFalse(empty.swapPriorities(1, 2));
        assertEquals(1, full.getPosition(1));
        assertEquals(2, full.getPosition(2));
        assertEquals(3, full.getPosition(3));
    }

    // serveNext


    /** Normal: serves and removes the first party. */
    @Test
    public void testServeNext()
    {
        WaitlistEntry served = full.serveNext();
        assertEquals("Smith", served.getName());
        assertEquals(2, full.size());
        assertNull(full.findByTicket(1));
        assertEquals(1, full.getPosition(2));
    }


    /** Edge: after a skip, the skipper is served first. */
    @Test
    public void testServeNextAfterSkip()
    {
        full.moveToFront(3);
        assertEquals(3, full.serveNext().getTicketNumber());
        assertEquals(1, full.serveNext().getTicketNumber());
    }


    /** Bad input: serving an empty line returns null. */
    @Test
    public void testServeNextEmpty()
    {
        assertNull(empty.serveNext());
        assertEquals(0, empty.size());
    }


    /** Edge: removes the exact party when two share a name and size. */
    @Test
    public void testServeNextSameNames()
    {
        empty.join("Smith", 2, 0);
        WaitlistEntry second = empty.join("Smith", 2, 0);
        empty.serveNext();
        assertSame(second, empty.findByTicket(2));
        assertEquals(1, empty.size());
    }

    // removeByTicket


    /** Normal: removes the party with that ticket. */
    @Test
    public void testRemoveByTicket()
    {
        assertTrue(full.removeByTicket(2));
        assertEquals(2, full.size());
        assertNull(full.findByTicket(2));
        assertEquals(2, full.getPosition(3));
    }


    /** Bad input: an unknown ticket returns false; line unchanged. */
    @Test
    public void testRemoveByTicketMissing()
    {
        assertFalse(full.removeByTicket(99));
        assertEquals(3, full.size());
    }


    /** Bad input: removing from an empty line returns false. */
    @Test
    public void testRemoveByTicketEmpty()
    {
        assertFalse(empty.removeByTicket(1));
    }


    /** Bad input: a ticket already served cannot be removed again. */
    @Test
    public void testRemoveByTicketAlreadyServed()
    {
        full.serveNext();
        assertFalse(full.removeByTicket(1));
        assertEquals(2, full.size());
    }

    // getServingOrder


    /** Normal: returns parties in serving order, including skips. */
    @Test
    public void testGetServingOrder()
    {
        WaitlistEntry[] order = full.getServingOrder();
        assertEquals(3, order.length);
        assertEquals(1, order[0].getTicketNumber());
        assertEquals(2, order[1].getTicketNumber());
        assertEquals(3, order[2].getTicketNumber());

        full.moveToFront(2);
        order = full.getServingOrder();
        assertEquals(2, order[0].getTicketNumber());
        assertEquals(1, order[1].getTicketNumber());
        assertEquals(3, order[2].getTicketNumber());
    }


    /** Bad input: an empty line returns an empty array. */
    @Test
    public void testGetServingOrderEmpty()
    {
        assertEquals(0, empty.getServingOrder().length);
    }

    // size


    /** Normal: three parties means size 3. */
    @Test
    public void testSize()
    {
        assertEquals(3, full.size());
    }


    /** Bad input: an empty line has size 0. */
    @Test
    public void testSizeEmpty()
    {
        assertEquals(0, empty.size());
    }
}
