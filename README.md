# GrubQueue (Group 29) Deliverable 3

GrubQueue is a console waitlist for restaurants. Customers join with a name, party size, and wallet balance, get a ticket number, check their position, and can pay $2 per party ahead to skip to the front. A customer can also auction their spot: parties behind them bid, and when the seller closes the auction, the highest bidder pays the seller and the two swap places. The restaurant serves the next party, removes parties by ticket, views the line, and views revenue and transaction history.

## Project layout

```
src/   LinkedChain.java (provided, unchanged), WaitlistEntry.java, Payment.java,
       Bid.java, Waitlist.java, Ledger.java, Auction.java,
       InputValidator.java, GrubQueueApp.java
test/  WaitlistEntryTest.java, PaymentTest.java, BidTest.java, WaitlistTest.java,
       LedgerTest.java, AuctionTest.java, InputValidatorTest.java, GrubQueueAppTest.java
```

## How to compile and run in Eclipse
1. **File → Import → General → Existing Projects into Workspace**, then choose the `GrubQueue` folder. The included `.classpath` already sets up `src`, `test`, and JUnit 4.
2. Run the program: right-click `GrubQueueApp.java` → **Run As → Java Application**.
3. Run the tests: right-click the `test` folder → **Run As → JUnit Test**.


### Menus
- **Main:** 1 Customer menu - 2 Restaurant menu - 3 Quit
- **Customer:** 1 Join - 2 Check my position - 3 Skip to the front - 4 Auction my spot - 5 Bid on the auctioned spot - 6 Close my auction - 7 Back
- **Restaurant:** 1 Serve next - 2 Remove a party - 3 View the line - 4 View revenue - 5 View transaction history - 6 Back

## How the LinkedChain is reused

One generic `LinkedChain<T>` stores three element types:

| Owner class | Field | Element type | LinkedChain methods used |
|---|---|---|---|
| `Waitlist` | `entries` | `WaitlistEntry` | `add`, `remove(T)`, `size`, `toArray(T[])` |
| `Ledger` | `payments` | `Payment` | `add`, `size`, `toArray(T[])` |
| `Auction` | `bids` | `Bid` | `add`, `size`, `toArray(T[])` |

The app classes only call these public methods. All line logic (priorities, positions, prices, serving order, swaps) lives in `Waitlist`. Money logic (revenue, history order) lives in `Ledger`, and bid logic (seller can't bid, highest bid wins) lives in `Auction`.

## Big-O of app methods vs. our spec estimates

*n* = parties in line, *p* = payments, *b* = bids.

| Method | Spec estimate | Actual | Why |
|---|---|---|---|
| `Waitlist.join` | O(1) | O(1) | the cap check uses `size()`; `add` inserts at the front of the chain |
| `Waitlist.findByTicket` | O(n) | O(n) | one `toArray` copy plus one linear scan |
| `Waitlist.findParty` *(new)* | — | O(n) | calls `findByTicket` |
| `Waitlist.getPosition` | O(n) | O(n) | find the ticket (O(n)), then count lower priorities (O(n)) |
| `Waitlist.getSkipPrice` | O(n) | O(n) | one `getPosition` call |
| `Waitlist.moveToFront` | O(n) | O(n) | `findByTicket` + finding the lowest priority, each O(n) |
| `Waitlist.swapPriorities` *(new)* | — | O(n) | two `findByTicket` calls |
| `Waitlist.serveNext` | O(n) | O(n) | find the lowest priority O(n) + `LinkedChain.remove(T)` O(n) |
| `Waitlist.removeByTicket` | O(n) | O(n) | `findByTicket` O(n) + `remove(T)` O(n) |
| `Waitlist.getServingOrder` | O(n²) | O(n²) | selection sort on a copy |
| `Waitlist.size` | O(1) | O(1) | the chain keeps a count |
| `Ledger.recordPayment` | O(1) | O(1) | `add` inserts at the front |
| `Ledger.getRestaurantRevenue` | O(n) | O(p) | one pass over `toArray` |
| `Ledger.getHistory` | O(n) | O(p) | `toArray` + one reversal pass |
| `Auction.placeBid` *(new)* | — | O(1) | `add` inserts at the front |
| `Auction.getHighestBid` *(new)* | — | O(b) | one pass over `toArray` |
| `InputValidator.*` | O(1) | O(1) | input length is capped (9 digits, 30 characters, 5 dollar digits) |
| Getters/setters on `WaitlistEntry`, `Payment`, `Bid` | O(1) | O(1) | return or set one field |

---

## Reflection

### What changed from our specification?

1. **The spot auction (our stretch goal) is built.** We added `Bid`, `Auction` (holding a `LinkedChain<Bid>`), and `Waitlist.swapPriorities`, following the design in our spec's "Stretch goal classes" paragraph. Only one auction runs at a time. The seller lists their spot, parties *behind* them bid, and the seller closes it. On close, the winner's wallet pays the seller's wallet, the two swap priority numbers, and `Ledger` records a `Payment` of type `"auction"` to `"Ticket #N"`. That payment shows up in transaction history but not in restaurant revenue. (Origin: group discussion: building it now let us check that the chain really is generic with a third type.)
2. **`Ledger.recordPayment` returns `boolean` instead of `void`.** Our test plan said an invalid payment is "not recorded", and a `void` method gave tests no way to check that. It now returns `false` for `null`. (Origin: own reflection on the test plan.)
3. **We picked one convention for rejected input.** Numeric validators return `InputValidator.INVALID` (-1) and `validateName` returns `null`. Nothing throws, so the app re-prompts. *(Origin: group discussion.)*
4. **Constructors now validate.** `WaitlistEntry`, `Payment`, `Bid`, and `Auction` throw `IllegalArgumentException` for impossible data, such as party size 13 or a $0 payment. `InputValidator` is still the first line of defense. In our test plan, the getter bad-input cases ("returns the method's invalid value") became `assertThrows` tests on the constructor. `WaitlistEntry`'s constructor takes 5 arguments (including priority) instead of the 4 in our test plan, because `Waitlist` assigns the priority. *(Origin: GenAI suggestion; kept after group review.)*
9. **`GrubQueueApp` takes a `Scanner` and a `PrintStream`, and has a public `run()` method.** This lets JUnit feed in scripted input and check the output, so the console app has real tests. Running out of input ends the program cleanly instead of crashing. *(Origin: GenAI suggestion.)*

### What changed in the LinkedChain?

**Nothing. We used `LinkedChain.java` unchanged.** We did adapt to three ways it differs from what our scope and spec described:
- **`add(null)` throws.** Our scope said `add(null)` returns false, but it throws `IllegalArgumentException`. Instead of changing the class, we make sure no app class ever passes `null`: `recordPayment(null)` and `placeBid(null)` return false before reaching the chain.
- **`toArray` takes an array.** Our spec listed `toArray()`, but the real method is `toArray(T[] values)` because Java can't create a generic array (`new T[n]`). We call it as `entries.toArray(new WaitlistEntry[entries.size()])`.
- **`add` puts new entries at the front.** This doesn't matter for `Waitlist`, because priority numbers decide the order. `Ledger.getHistory` reverses the array so history reads oldest first, and `Auction.getHighestBid` scans from the back so the earlier bid wins a tie. We used these instead of `addToEnd`, which would have made `recordPayment` and `placeBid` O(n) instead of O(1).

### What happened to our unknowns?

- **Priority numbers: solved.** New parties get `nextPriorityNumber++`, which only increases. A skip gets `(current lowest priority) − 1`. An auction swaps two existing numbers. No step ever creates a number already in use, so ties are impossible. Numbers can go to zero or negative, which is harmless because only comparisons matter. Tests: `testRepeatedSkipsNoTies`, `testJoinAfterSkipGoesToBack`, and `testSwapPriorities`.
- **Searching and reordering efficiently: partly open.** Searching is O(n) because a bag has no index, and building the serving order is O(n²). With a 50-party cap this is fast enough. A faster design would need ordered insertion, which a bag doesn't provide.

### What doesn't work yet?

- **The auction doesn't fall back to the next bid.** If the winning bidder can no longer pay or has moved ahead of the seller, the auction closes with no trade instead of offering the spot to the next-highest bidder.
- **Only one auction can run at a time.**
- **The restaurant menu isn't protected.** Anyone at the console can open it.

---

## Next Steps

### What is left before the final (in order)

| # | Item | Owner |
|---|---|---|
| 1 | Every member walks through each class and test file so we can all defend it, including stating the Big-O table above | All |
| 2 | When an auction closes, fall back to the next-highest bid if the winner can't complete the trade | Tyler |
| 3 | Add a restaurant PIN check before the restaurant menu opens | Nana |
| 4 | Fix anything raised in Deliverable 3 grading feedback | Jonathan |
| 5 | Prepare the Deliverable 4 presentation and write-up | All |

### Stretch goals

| Stretch goal (from scope) | Status |
|---|---|
| Spot auctions (`Auction`, `Bid`, `LinkedChain<Bid>`) | **Committed. Working in this prototype.** Item 2 above finishes it. |
| Show wallet balance (scope add-on) | **Committed. Done.** The balance prints after joining, skipping, bidding, and selling. |
| More than one auction at a time | **Dropped.** It would need a `LinkedChain<Auction>` and changes to every auction menu option, which we don't need for the MVP. |

### New tests we will need

- `Auction.getRunnerUpBid` (or similar, for item 2): normal case with three bids; bad input of an auction with zero or one bids.
- `GrubQueueApp` auction fallback: the winner can't pay, so the runner-up gets the spot; nobody can pay, so nothing changes.
- PIN check: correct PIN opens the restaurant menu; wrong or blank PIN is re-prompted or refused.

---

## Attribution

| File | Who worked on it | How |
|---|---|---|
| `LinkedChain.java` | CS 2114 staff | Provided |
| `WaitlistEntry.java` | Tyler | Wrote and checked manually |
| `Payment.java` | Nana | Wrote manually then checked with GenAI |
| `Bid.java` | Tyler | Wrote and checked manually |
| `Waitlist.java` | Tyler | Wrote manually and checked with GenAI. priority rule checked against our scope unknown |
| `Ledger.java` | Jonathan | Wrote manually and checked with GenAI from our spec (Claude) |
| `Auction.java` | Jonathan | Wrote and checked manually |
| `InputValidator.java` | Nana | Wrote with GenAI (Claude) assistance based on our "Where validation lives" section |
| `GrubQueueApp.java` | Tyler | Wrote with GenAI assistance (Claude). Manually ran through the MVP and auction paths |
| `WaitlistEntryTest.java` | Tyler | Wrote manually then checked by GenAI |
| `PaymentTest.java` | Nana | Manually wrote, checked by GenAI (Claude) |
| `BidTest.java` | Tyler | Wrote manually, checked by GenAI (Claude) |
| `WaitlistTest.java` | Tyler | Wrote |
| `LedgerTest.java` | Jonathan | Wrote manually and checked with GenAI (Claude) |
| `AuctionTest.java` | Jonathan | Written and checked |
| `InputValidatorTest.java` | Nana | Written from our test plan |
| `GrubQueueAppTest.java` | Tyler | GenAI-drafted (Claude), finished and reviewed by Tyler |
| `README.md` | Tyler | First draft by GenAI (Claude), edited and finalized by Tyler |

### GenAI Note
GenAI was used for helping us make first drafts, making sure the appropriate test cases were covered, making sure no requirements were missed from the instructions, and assisted in writing the `README.md`