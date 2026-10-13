import java.io.PrintStream;
import java.util.Scanner;

/**
 * The console interface for GrubQueue. This is the only class that reads input
 * or prints output. It shows the menus, passes raw text to InputValidator,
 * re-prompts on invalid input, and calls Waitlist, Ledger, and Auction to carry
 * out each action. The Scanner and PrintStream are passed in so JUnit tests can
 * feed in scripted input and check the printed output.
 *
 * @author Tyler Santiago
 * @version October 2026
 */
public class GrubQueueApp
{
    private static final String[] MAIN_MENU =
        { "Customer menu", "Restaurant menu", "Quit" };

    private static final String[] CUSTOMER_MENU = { "Join the line",
        "Check my position", "Skip to the front", "Auction my spot",
        "Bid on the auctioned spot", "Close my auction", "Back to main menu" };

    private static final String[] RESTAURANT_MENU =
        { "Serve next party", "Remove a party", "View the line", "View revenue",
            "View transaction history", "Back to main menu" };

    private final Scanner in;
    private final PrintStream out;
    private final Waitlist waitlist;
    private final Ledger ledger;
    private Auction auction; // the one open auction, or null

    /**
     * Creates the app with an empty waitlist and ledger and no auction.
     *
     * @param in
     *            where user input is read from
     * @param out
     *            where output is printed
     * @throws IllegalArgumentException
     *             if in or out is null
     */
    public GrubQueueApp(Scanner in, PrintStream out)
    {
        if (in == null || out == null)
        {
            throw new IllegalArgumentException(
                "Scanner and output stream must not be null");
        }
        this.in = in;
        this.out = out;
        waitlist = new Waitlist();
        ledger = new Ledger();
        auction = null;
    }


    /**
     * Starts GrubQueue on the console.
     *
     * @param args
     *            not used
     */
    public static void main(String[] args)
    {
        new GrubQueueApp(new Scanner(System.in), System.out).run();
    }


    /**
     * Runs the main menu until the user quits or input runs out. Never throws
     * on bad input.
     */
    public void run()
    {
        out.println("Welcome to GrubQueue!");
        try
        {
            boolean running = true;
            while (running)
            {
                int choice = chooseFromMenu("Main Menu", MAIN_MENU);
                if (choice == 1)
                {
                    customerMenu();
                }
                else if (choice == 2)
                {
                    restaurantMenu();
                }
                else
                {
                    running = false;
                }
            }
        }
        catch (EndOfInputException e)
        {
            out.println();
            out.println("No more input.");
        }
        out.println("Goodbye!");
    }

    // ---------------------------------------------------------------
    // Menus
    // ---------------------------------------------------------------


    /**
     * Runs the customer menu until the user goes back.
     */
    private void customerMenu()
    {
        boolean inMenu = true;
        while (inMenu)
        {
            int choice = chooseFromMenu("Customer Menu", CUSTOMER_MENU);
            switch (choice)
            {
                case 1:
                    joinLine();
                    break;
                case 2:
                    checkPosition();
                    break;
                case 3:
                    skipToFront();
                    break;
                case 4:
                    auctionSpot();
                    break;
                case 5:
                    placeBid();
                    break;
                case 6:
                    closeAuction();
                    break;
                default:
                    inMenu = false;
                    break;
            }
        }
    }


    /**
     * Runs the restaurant menu until the user goes back.
     */
    private void restaurantMenu()
    {
        boolean inMenu = true;
        while (inMenu)
        {
            int choice = chooseFromMenu("Restaurant Menu", RESTAURANT_MENU);
            switch (choice)
            {
                case 1:
                    serveNext();
                    break;
                case 2:
                    removeParty();
                    break;
                case 3:
                    viewLine();
                    break;
                case 4:
                    out.println(
                        "Total skip revenue: "
                            + formatCents(ledger.getRestaurantRevenue()) + ".");
                    break;
                case 5:
                    viewHistory();
                    break;
                default:
                    inMenu = false;
                    break;
            }
        }
    }

    // ---------------------------------------------------------------
    // Customer actions: MVP
    // ---------------------------------------------------------------


    /**
     * Joins a new party to the line. Waitlist.join() refuses the party if the
     * line is full.
     */
    private void joinLine()
    {
        String name = promptName();
        int partySize = promptPartySize();
        int wallet = promptBalance();
        WaitlistEntry entry = waitlist.join(name, partySize, wallet);
        if (entry == null)
        {
            out.println(
                "Sorry, the line is full (" + Waitlist.MAX_PARTIES
                    + " parties). Please try again later.");
            return;
        }
        int position = waitlist.getPosition(entry.getTicketNumber());
        out.println(
            "Welcome, " + entry.getName() + "! Your ticket number is #"
                + entry.getTicketNumber() + ".");
        out.println(
            "You are number " + position + " in line (" + parties(position - 1)
                + " ahead of you).");
        out.println(
            "Wallet balance: " + formatCents(entry.getWalletBalanceCents())
                + ".");
    }


    /**
     * Shows a customer's position in line.
     */
    private void checkPosition()
    {
        WaitlistEntry party = identifyCustomer();
        if (party == null)
        {
            return;
        }
        int position = waitlist.getPosition(party.getTicketNumber());
        out.println(
            "Ticket #" + party.getTicketNumber() + ": you are number "
                + position + " in line (" + parties(position - 1)
                + " ahead of you).");
    }


    /**
     * Lets a customer pay to move to the front of the line.
     */
    private void skipToFront()
    {
        WaitlistEntry party = identifyCustomer();
        if (party == null)
        {
            return;
        }
        int ticket = party.getTicketNumber();
        int price = waitlist.getSkipPrice(ticket);
        if (price == 0)
        {
            out.println("You're already next! You were not charged.");
            return;
        }
        int wallet = party.getWalletBalanceCents();
        out.println(
            "Skipping " + parties(price / Waitlist.SKIP_PRICE_PER_PARTY_CENTS)
                + " costs " + formatCents(price) + ". Your wallet: "
                + formatCents(wallet) + ".");
        if (wallet < price)
        {
            out.println("Not enough money in your wallet. Skip cancelled.");
            return;
        }
        int payment = promptMoney("Enter your payment: ");
        if (payment < price)
        {
            out.println(
                "That doesn't cover the price of " + formatCents(price)
                    + ". Skip cancelled.");
            return;
        }

        party.setWalletBalanceCents(wallet - price);
        waitlist.moveToFront(ticket);
        ledger.recordPayment(
            new Payment(price, ticket, Payment.RESTAURANT, Payment.TYPE_SKIP));

        out.println("Charged " + formatCents(price) + ".");
        if (payment > price)
        {
            out.println(
                "You overpaid, so you were only charged the price. "
                    + "Your change: " + formatCents(payment - price) + ".");
        }
        out.println(
            "You're now number 1 in line! Wallet balance: "
                + formatCents(party.getWalletBalanceCents()) + ".");
    }

    // ---------------------------------------------------------------
    // Customer actions: spot auction (stretch goal)
    // ---------------------------------------------------------------


    /**
     * Lists a customer's spot for auction. Only one auction runs at a time.
     */
    private void auctionSpot()
    {
        WaitlistEntry seller = identifyCustomer();
        if (seller == null)
        {
            return;
        }
        if (auctionIsOpen())
        {
            out.println(
                "A spot is already up for auction. Try again after "
                    + "it closes.");
            return;
        }
        auction = new Auction(seller.getTicketNumber());
        out.println(
            "Your spot (number "
                + waitlist.getPosition(seller.getTicketNumber())
                + " in line) is up for auction. Parties behind you can bid.");
    }


    /**
     * Places a bid on the open auction.
     */
    private void placeBid()
    {
        if (!auctionIsOpen())
        {
            out.println("No spot is up for auction right now.");
            return;
        }
        WaitlistEntry bidder = identifyCustomer();
        if (bidder == null)
        {
            return;
        }
        int seller = auction.getSellerTicketNumber();
        int ticket = bidder.getTicketNumber();
        if (ticket == seller)
        {
            out.println("You can't bid on your own spot.");
            return;
        }
        if (waitlist.getPosition(ticket) < waitlist.getPosition(seller))
        {
            out.println("You're already ahead of that spot.");
            return;
        }
        out.println(
            "Spot for sale: number " + waitlist.getPosition(seller)
                + " in line. Your wallet: "
                + formatCents(bidder.getWalletBalanceCents()) + ".");
        int amount = promptMoney("Enter your bid: ");
        if (amount > bidder.getWalletBalanceCents())
        {
            out.println(
                "You only have " + formatCents(bidder.getWalletBalanceCents())
                    + " in your wallet. Bid not placed.");
            return;
        }
        auction.placeBid(new Bid(ticket, amount));
        out.println("Bid of " + formatCents(amount) + " placed.");
    }


    /**
     * Closes the seller's auction: the highest bidder pays the seller and the
     * two parties swap places.
     */
    private void closeAuction()
    {
        WaitlistEntry seller = identifyCustomer();
        if (seller == null)
        {
            return;
        }
        if (!auctionIsOpen()
            || auction.getSellerTicketNumber() != seller.getTicketNumber())
        {
            out.println("You don't have a spot up for auction.");
            return;
        }
        Bid best = auction.getHighestBid();
        auction = null;
        if (best == null)
        {
            out.println(
                "No bids were placed. Auction closed; nothing " + "changed.");
            return;
        }
        int sellerTicket = seller.getTicketNumber();
        int amount = best.getAmountCents();
        WaitlistEntry winner =
            waitlist.findByTicket(best.getBidderTicketNumber());
        if (winner == null || winner.getWalletBalanceCents() < amount
            || waitlist.getPosition(winner.getTicketNumber()) < waitlist
                .getPosition(sellerTicket))
        {
            out.println(
                "The winning bidder can no longer complete the "
                    + "trade. Auction closed; nothing changed.");
            return;
        }

        winner.setWalletBalanceCents(winner.getWalletBalanceCents() - amount);
        seller.setWalletBalanceCents(seller.getWalletBalanceCents() + amount);
        waitlist.swapPriorities(sellerTicket, winner.getTicketNumber());
        ledger.recordPayment(
            new Payment(
                amount,
                winner.getTicketNumber(),
                "Ticket #" + sellerTicket,
                Payment.TYPE_AUCTION));

        out.println(
            "Sold to ticket #" + winner.getTicketNumber() + " for "
                + formatCents(amount) + "! You're now number "
                + waitlist.getPosition(sellerTicket) + " in line. Wallet "
                + "balance: " + formatCents(seller.getWalletBalanceCents())
                + ".");
    }


    /**
     * Tells whether an auction is open. An auction whose seller has been served
     * or removed is cancelled here.
     *
     * @return true if an auction is open and its seller is still in line
     */
    private boolean auctionIsOpen()
    {
        if (auction != null
            && waitlist.findByTicket(auction.getSellerTicketNumber()) == null)
        {
            auction = null;
        }
        return auction != null;
    }


    /**
     * Asks for a ticket number and name, and finds the party only if both
     * match.
     *
     * @return the matching party, or null (after printing a message)
     */
    private WaitlistEntry identifyCustomer()
    {
        int ticket = promptTicket();
        String name = readLine("Enter the name on the ticket: ");
        WaitlistEntry party = waitlist.findParty(ticket, name);
        if (party == null)
        {
            out.println(
                "No party with ticket #" + ticket + " under that name.");
        }
        return party;
    }

    // ---------------------------------------------------------------
    // Restaurant actions
    // ---------------------------------------------------------------


    /**
     * Serves and removes the next party.
     */
    private void serveNext()
    {
        WaitlistEntry next = waitlist.serveNext();
        if (next == null)
        {
            out.println("No one is waiting.");
        }
        else
        {
            out.println("Now serving: " + next);
        }
    }


    /**
     * Removes a party by ticket number.
     */
    private void removeParty()
    {
        int ticket = promptTicket();
        if (waitlist.removeByTicket(ticket))
        {
            out.println("Removed party #" + ticket + ".");
        }
        else
        {
            out.println("No party with ticket #" + ticket + ".");
        }
    }


    /**
     * Prints the line in serving order.
     */
    private void viewLine()
    {
        WaitlistEntry[] order = waitlist.getServingOrder();
        if (order.length == 0)
        {
            out.println("No one is waiting.");
            return;
        }
        out.println("Current line:");
        for (int i = 0; i < order.length; i++)
        {
            out.println("  " + (i + 1) + ". " + order[i]);
        }
    }


    /**
     * Prints every transaction, oldest first.
     */
    private void viewHistory()
    {
        Payment[] history = ledger.getHistory();
        if (history.length == 0)
        {
            out.println("No transactions yet.");
            return;
        }
        out.println("Transaction history:");
        for (int i = 0; i < history.length; i++)
        {
            Payment p = history[i];
            out.println(
                "  " + (i + 1) + ". Ticket #" + p.getPayerTicketNumber()
                    + " paid " + formatCents(p.getAmountCents()) + " to "
                    + p.getRecipient() + " (" + p.getTransactionType() + ")");
        }
    }

    // ---------------------------------------------------------------
    // Prompts with re-prompt loops
    // ---------------------------------------------------------------


    /**
     * Prints a menu and reads a choice, reprinting the menu after any invalid
     * choice.
     *
     * @param title
     *            the menu heading
     * @param options
     *            the option labels, numbered from 1
     * @return the chosen option number
     */
    private int chooseFromMenu(String title, String[] options)
    {
        while (true)
        {
            out.println();
            out.println("=== " + title + " ===");
            for (int i = 0; i < options.length; i++)
            {
                out.println((i + 1) + ". " + options[i]);
            }
            int choice = InputValidator.validateMenuChoice(
                readLine("Choose an option: "),
                1,
                options.length);
            if (choice != InputValidator.INVALID)
            {
                return choice;
            }
            out.println(
                "Invalid choice. Enter a number from 1 to " + options.length
                    + ".");
        }
    }


    /**
     * Reads a valid name.
     *
     * @return the trimmed name
     */
    private String promptName()
    {
        while (true)
        {
            String name =
                InputValidator.validateName(readLine("Enter your name: "));
            if (name != null)
            {
                return name;
