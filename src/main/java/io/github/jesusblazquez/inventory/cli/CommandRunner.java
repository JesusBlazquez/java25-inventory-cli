package io.github.jesusblazquez.inventory.cli;

import io.github.jesusblazquez.inventory.domain.Inventory;
import io.github.jesusblazquez.inventory.domain.MovementOutcome;
import io.github.jesusblazquez.inventory.domain.Product;
import io.github.jesusblazquez.inventory.domain.StockMovement;
import io.github.jesusblazquez.inventory.report.Reports;
import io.github.jesusblazquez.inventory.storage.InventoryStore;
import java.time.Clock;
import java.time.Instant;

/**
 * Runs a command against the inventory and reports back in text.
 *
 * <p>It writes to whatever {@link Appendable} it is given rather than to {@code System.out}, which
 * is what makes the tests able to read the output instead of capturing a global stream.
 */
public final class CommandRunner {

    public static final int OK = 0;
    public static final int FAILED = 1;

    private final InventoryStore store;
    private final Appendable out;
    private final Clock clock;

    public CommandRunner(InventoryStore store, Appendable out, Clock clock) {
        this.store = store;
        this.out = out;
        this.clock = clock;
    }

    public int run(Command command) {
        Inventory inventory = store.load();

        // Exhaustive over a sealed interface: no default branch, and a new command will not compile
        // until it is handled here.
        return switch (command) {
            case Command.Add(var sku, var name, var category, var price, int minimum) -> {
                if (inventory.find(sku).isPresent()) {
                    yield fail("Product %s already exists".formatted(sku));
                }
                Product product = inventory.register(new Product(sku, name, category, price, 0, minimum));
                store.save(inventory);
                yield print("Added %s (%s), minimum stock %d".formatted(product.sku(), product.name(), minimum));
            }

            case Command.Receive(var sku, int units) ->
                applyMovement(inventory, new StockMovement.Receipt(sku, units, now()), "Received %d unit(s) of %s");

            case Command.Issue(var sku, int units) ->
                applyMovement(inventory, new StockMovement.Issue(sku, units, now()), "Issued %d unit(s) of %s");

            case Command.Adjust(var sku, int counted, String reason) -> {
                MovementOutcome outcome =
                        inventory.apply(new StockMovement.Adjustment(sku, counted, reason, now()));
                yield switch (outcome) {
                    case MovementOutcome.Applied(Product product) -> {
                        store.save(inventory);
                        yield print("%s set to %d unit(s) after a count: %s"
                                .formatted(product.sku(), product.quantity(), reason));
                    }
                    case MovementOutcome.Rejected rejected -> fail(explain(rejected));
                };
            }

            case Command.List() -> print(Reports.productTable(inventory));
            case Command.LowStock() -> print(Reports.lowStock(inventory));
            case Command.Value() -> print(Reports.valuation(inventory));
            case Command.Help() -> print(help());
        };
    }

    private int applyMovement(Inventory inventory, StockMovement movement, String successTemplate) {
        MovementOutcome outcome = inventory.apply(movement);

        return switch (outcome) {
            case MovementOutcome.Applied(Product product) -> {
                store.save(inventory);
                int units = switch (movement) {
                    case StockMovement.Receipt(_, int moved, _) -> moved;
                    case StockMovement.Issue(_, int moved, _) -> moved;
                    case StockMovement.Adjustment(_, int counted, _, _) -> counted;
                };
                String message = successTemplate.formatted(units, product.sku());
                yield print("%s. Stock is now %d.".formatted(message, product.quantity()));
            }
            case MovementOutcome.Rejected rejected -> fail(explain(rejected));
        };
    }

    private String explain(MovementOutcome.Rejected rejected) {
        return switch (rejected.reason()) {
            case UNKNOWN_PRODUCT -> "There is no product with SKU %s".formatted(rejected.sku());
            case NOT_ENOUGH_STOCK -> "Not enough stock of %s for that movement".formatted(rejected.sku());
        };
    }

    private Instant now() {
        return clock.instant();
    }

    private int print(String message) {
        write(message);
        return OK;
    }

    private int fail(String message) {
        write("Error: " + message);
        return FAILED;
    }

    private void write(String message) {
        try {
            out.append(message).append(System.lineSeparator());
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    private static String help() {
        return """
                inv — warehouse inventory

                Usage:
                  inv add <sku> <name> <category> <unit-price> <minimum-stock>
                  inv receive <sku> <units>          Goods arriving from a supplier
                  inv issue <sku> <units>            Goods leaving the warehouse
                  inv adjust <sku> <counted> <reason>  Correct the stock after a count
                  inv list                           Every product and its stock
                  inv low-stock                      What needs reordering
                  inv value                          Stock value, by category
                  inv help                           This text

                SKU format: three letters, a dash and four digits, for example TOO-0042.
                Data file:  inventory.csv in the current directory, or $INVENTORY_FILE.""";
    }
}
