import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import org.junit.Test;

/**
 * Tests for GrubQueueApp. Each test feeds a script of typed lines into the app
 * and checks the printed output. Menu numbers used in the scripts: Main: 1
 * customer, 2 restaurant, 3 quit Customer: 1 join, 2 position, 3 skip, 4
 * auction my spot, 5 bid, 6 close my auction, 7 back Restaurant: 1 serve, 2
 * remove, 3 view line, 4 revenue, 5 history, 6 back
 *
 * @author Tyler Santiago
 * @version October 2026
 */
public class GrubQueueAppTest
{
    /** Customer menu, join Smith (party of 2, $10.00), back to main. */
    private static final String JOIN_SMITH = "1\n1\nSmith\n2\n10\n7\n";

    /** Customer menu, join Jones (party of 4, $5.00), back to main. */
    private static final String JOIN_JONES = "1\n1\nJones\n4\n5\n7\n";

    /** Customer menu, Smith (ticket 1) lists his spot, back to main. */
    private static final String SMITH_LISTS = "1\n4\n1\nSmith\n7\n";

    /**
     * Runs the app on the given input and returns everything printed.
     *
     * @param input
     *            the lines the user "types"
     * @return the app's output
     */
    private String runWith(String input)
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes);
        new GrubQueueApp(new Scanner(input), out).run();
        out.flush();
        return bytes.toString();
    }

    // constructor


    /** Bad input: null Scanner or output is rejected. */
    @Test
    public void testConstructorBadInput()
    {
        assertThrows(
            IllegalArgumentException.class,
            () -> new GrubQueueApp(null, System.out));
        assertThrows(
            IllegalArgumentException.class,
            () -> new GrubQueueApp(new Scanner(""), null));
    }

    // run: normal MVP path


    /** Normal: join, check position, view line, serve, quit. */
    @Test
    public void testRunMvpPath()
    {
        String output = runWith(
            JOIN_SMITH + JOIN_JONES + "1\n2\n2\nJones\n7\n"     // Jones checks
                                                                // position
                + "2\n3\n1\n3\n6\n"         // view line, serve, view line
                + "3\n");
        assertTrue(output.contains("Your ticket number is #1."));
        assertTrue(output.contains("Your ticket number is #2."));
        assertTrue(output.contains("you are number 2 in line (1 party"));
        assertTrue(output.contains("1. #1 Smith (party of 2)"));
        assertTrue(output.contains("Now serving: #1 Smith (party of 2)"));
        assertTrue(output.contains("1. #2 Jones (party of 4)"));
        assertTrue(output.contains("Goodbye!"));
    }


    /** Normal: a paid skip moves the party and is recorded. */
    @Test
    public void testRunSkipWithOverpayment()
    {
        String output = runWith(
            JOIN_SMITH + JOIN_JONES + "1\n3\n2\nJones\n3\n7\n"   // Jones skips,
                                                                 // pays $3
                + "2\n3\n4\n5\n6\n3\n");     // line, revenue, history
        assertTrue(output.contains("Skipping 1 party costs $2.00"));
        assertTrue(output.contains("Your change: $1.00."));
        assertTrue(
            output.contains(
                "You're now number 1 in line! " + "Wallet balance: $3.00."));
        assertTrue(output.contains("1. #2 Jones (party of 4)"));
        assertTrue(output.contains("Total skip revenue: $2.00."));
        assertTrue(
            output.contains("Ticket #2 paid $2.00 to Restaurant " + "(skip)"));
    }

    // run: spot auction (stretch goal)


    /** Normal: list, bid, close; the parties swap and money moves. */
    @Test
    public void testRunAuction()
    {
        String output = runWith(
            JOIN_SMITH + JOIN_JONES + SMITH_LISTS + "1\n5\n2\nJones\n3\n7\n"   // Jones
                                                                               // bids
                                                                               // $3
                + "1\n6\n1\nSmith\n7\n"      // Smith closes
                + "2\n3\n4\n5\n6\n3\n");     // line, revenue, history
        assertTrue(output.contains("is up for auction"));
        assertTrue(output.contains("Bid of $3.00 placed."));
        assertTrue(
            output.contains(
                "Sold to ticket #2 for $3.00! You're "
                    + "now number 2 in line. Wallet balance: $13.00."));
        assertTrue(output.contains("1. #2 Jones (party of 4)"));
        assertTrue(output.contains("Total skip revenue: $0.00."));
        assertTrue(
            output
                .contains("Ticket #2 paid $3.00 to Ticket #1 " + "(auction)"));
    }


    /** Bad input: the auction rules each refuse with a message. */
    @Test
    public void testRunAuctionRules()
    {
        String output = runWith(
            JOIN_SMITH + JOIN_JONES + "1\n" + "5\n"                // bid with
                                                                   // no auction
                                                                   // open
                + "4\n2\nJones\n"      // Jones lists his spot
                + "4\n1\nSmith\n"      // a second listing is refused
                + "5\n2\nJones\n"      // Jones bids on his own spot
                + "5\n1\nSmith\n"      // Smith is already ahead
                + "6\n1\nSmith\n"      // Smith has no auction to close
                + "6\n2\nJones\n"      // Jones closes with no bids
                + "7\n3\n");
        assertTrue(output.contains("No spot is up for auction right now."));
        assertTrue(output.contains("A spot is already up for auction."));
        assertTrue(output.contains("You can't bid on your own spot."));
        assertTrue(output.contains("You're already ahead of that spot."));
        assertTrue(output.contains("You don't have a spot up for auction."));
        assertTrue(output.contains("No bids were placed."));
    }


    /** Bad input: zero bid re-prompts; a bid over the wallet is refused. */
    @Test
    public void testRunAuctionBadBids()
    {
        String output = runWith(
            JOIN_SMITH + JOIN_JONES + SMITH_LISTS
                + "1\n5\n2\nJones\n0\n9\n7\n3\n");
        assertTrue(output.contains("Amounts must be positive"));
        assertTrue(
            output.contains(
                "You only have $5.00 in your wallet. " + "Bid not placed."));
    }


    /** Bad input: serving the seller cancels the auction. */
    @Test
    public void testRunAuctionSellerServed()
    {
        String output = runWith(
            JOIN_SMITH + JOIN_JONES + SMITH_LISTS + "2\n1\n6\n"           // restaurant
                                                                          // serves
                                                                          // Smith
                + "1\n5\n7\n3\n");      // Jones tries to bid
        assertTrue(output.contains("No spot is up for auction right now."));
    }


    /** Bad input: a winner who can no longer pay gets nothing. */
    @Test
    public void testRunAuctionWinnerCannotPay()
    {
        String output = runWith(
            JOIN_SMITH + JOIN_JONES + SMITH_LISTS + "1\n5\n2\nJones\n5\n"     // Jones
                                                                              // bids
                                                                              // all
                                                                              // $5
                + "3\n2\nJones\n2\n"        // then spends $2 to skip
                + "6\n1\nSmith\n7\n"        // Smith closes
                + "2\n5\n6\n3\n");          // history
        assertTrue(
            output.contains(
                "The winning bidder can no longer " + "complete the trade."));
        assertFalse(output.contains("(auction)"));
    }

    // run: bad input survives


    /** Bad input: invalid menu choices print a message and the menu. */
    @Test
    public void testRunInvalidMenuChoice()
    {
        String output = runWith("abc\n7\n\n3\n");
        assertTrue(
            output
                .contains("Invalid choice. Enter a number from 1 " + "to 3."));
        assertTrue(output.contains("Goodbye!"));
    }


    /** Bad input: bad name, size, and wallet are re-prompted. */
    @Test
    public void testRunJoinReprompts()
    {
        String output = runWith(
            "1\n1\n   \n" + repeat('A', 31)
                + "\nSmith\nfour\n15\n2\n-5\nabc\n10\n7\n3\n");
        assertTrue(output.contains("Name must be 1 to 30 characters"));
        assertTrue(
            output.contains(
                "Party size must be a whole number " + "from 1 to 12."));
        assertTrue(output.contains("Enter an amount from $0.00"));
        assertTrue(output.contains("Your ticket number is #1."));
    }


    /** Bad input: the 51st party is told the line is full. */
    @Test
    public void testRunLineFull()
    {
        StringBuilder script = new StringBuilder("1\n");
        for (int i = 0; i <= Waitlist.MAX_PARTIES; i++)
        {
            script.append("1\nParty\n2\n0\n");
        }
        script.append("7\n3\n");
        String output = runWith(script.toString());
        assertTrue(output.contains("Your ticket number is #50."));
        assertTrue(output.contains("Sorry, the line is full (50 parties)."));
        assertFalse(output.contains("Your ticket number is #51."));
    }


    /** Bad input: unknown ticket, and someone else's ticket. */
    @Test
    public void testRunWrongTicketOrName()
    {
        String output = runWith(
            JOIN_SMITH + "1\n2\nabc\n99\nSmith\n"   // bad format, then unknown
                + "2\n1\nJones\n7\n3\n");    // ticket 1 with the wrong name
        assertTrue(
            output.contains("Ticket numbers are positive whole " + "numbers"));
        assertTrue(
            output.contains("No party with ticket #99 under that " + "name."));
        assertTrue(
            output.contains("No party with ticket #1 under that " + "name."));
    }


    /** Bad input: the front party is not charged to skip. */
    @Test
    public void testRunSkipAlreadyNext()
    {
        String output =
            runWith(JOIN_SMITH + "1\n3\n1\nSmith\n7\n" + "2\n4\n6\n3\n");
        assertTrue(output.contains("You're already next!"));
        assertTrue(output.contains("Total skip revenue: $0.00."));
    }


    /** Bad input: not enough money in the wallet to skip. */
    @Test
    public void testRunSkipNotEnoughFunds()
    {
        String output = runWith(
            "1\n1\nSmith\n2\n0\n1\nJones\n4\n1\n" + "3\n2\nJones\n7\n3\n");
        assertTrue(output.contains("Skipping 1 party costs $2.00."));
        assertTrue(
            output.contains(
                "Not enough money in your wallet. " + "Skip cancelled."));
    }


    /** Bad input: zero/negative payment re-prompts; underpaying cancels. */
    @Test
    public void testRunSkipBadPayments()
    {
        String output = runWith(
            JOIN_SMITH + JOIN_JONES + "1\n3\n2\nJones\n0\n-4\n1\n7\n3\n");
        assertTrue(output.contains("Amounts must be positive"));
        assertTrue(
            output.contains(
                "That doesn't cover the price of $2.00. " + "Skip cancelled."));
        assertFalse(output.contains("You're now number 1"));
    }


    /** Bad input: serve and view on an empty line; remove unknown. */
    @Test
    public void testRunRestaurantEmptyLine()
    {
        String output = runWith("2\n1\n3\n2\n99\n5\n6\n3\n");
        assertTrue(output.contains("No one is waiting."));
        assertTrue(output.contains("No party with ticket #99."));
        assertTrue(output.contains("No transactions yet."));
    }


    /** Normal: the restaurant removes a party by ticket. */
    @Test
    public void testRunRemoveParty()
    {
        String output = runWith(JOIN_SMITH + "2\n2\n1\n3\n6\n3\n");
        assertTrue(output.contains("Removed party #1."));
        assertTrue(output.contains("No one is waiting."));
    }


    /** Bad input: input ending mid-prompt does not crash. */
    @Test
    public void testRunEndOfInput()
    {
        String output = runWith("1\n1\nSmith\n");
        assertTrue(output.contains("No more input."));
        assertTrue(output.contains("Goodbye!"));
    }

    // main


    /** Normal: main runs on System.in and quits on "3". */
    @Test
    public void testMain()
    {
        String output = runMain("3\n");
        assertTrue(output.contains("Welcome to GrubQueue!"));
        assertTrue(output.contains("Goodbye!"));
    }


    /** Bad input: main survives garbage then empty input. */
    @Test
    public void testMainBadInput()
    {
        String output = runMain("garbage\n");
        assertTrue(output.contains("Invalid choice."));
        assertTrue(output.contains("Goodbye!"));
    }


    /**
     * Runs main with System.in and System.out redirected.
     *
     * @param input
     *            the lines the user "types"
     * @return the output
     */
    private String runMain(String input)
    {
        InputStream oldIn = System.in;
        PrintStream oldOut = System.out;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try
        {
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            System.setOut(new PrintStream(bytes));
            GrubQueueApp.main(new String[0]);
        }
        finally
        {
            System.setIn(oldIn);
            System.setOut(oldOut);
        }
        return bytes.toString();
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
