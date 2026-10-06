package io.github.jesusblazquez.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jesusblazquez.inventory.domain.MovementOutcome.Rejected;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class InventoryTest {

    private static final Instant NOW = Instant.parse("2026-10-06T09:00:00Z");
    private static final Sku HAMMER = Sku.of("TOO-0001");
    private static final Sku CABLE = Sku.of("ELE-0002");

    private Inventory warehouseWith(int hammers) {
        Inventory inventory = Inventory.empty();
        inventory.register(Product.of("TOO-0001", "Hammer", Category.TOOLS, "12.50", 3).withQuantity(hammers));
        return inventory;
    }

    @Test
    void aReceiptAddsStock() {
        Inventory inventory = warehouseWith(2);

        MovementOutcome outcome = inventory.apply(new StockMovement.Receipt(HAMMER, 5, NOW));

        assertThat(outcome).isInstanceOf(MovementOutcome.Applied.class);
        assertThat(inventory.find(HAMMER).orElseThrow().quantity()).isEqualTo(7);
    }

    @Test
    void anIssueRemovesStock() {
        Inventory inventory = warehouseWith(10);

        inventory.apply(new StockMovement.Issue(HAMMER, 4, NOW));

        assertThat(inventory.find(HAMMER).orElseThrow().quantity()).isEqualTo(6);
    }

    @Test
    void anIssueBeyondTheStockIsRejectedAndChangesNothing() {
        Inventory inventory = warehouseWith(3);

        MovementOutcome outcome = inventory.apply(new StockMovement.Issue(HAMMER, 4, NOW));

        assertThat(outcome)
                .isInstanceOfSatisfying(Rejected.class, rejected ->
                        assertThat(rejected.reason()).isEqualTo(Rejected.Reason.NOT_ENOUGH_STOCK));
        assertThat(inventory.find(HAMMER).orElseThrow().quantity()).isEqualTo(3);
    }

    @Test
    void anIssueOfExactlyTheStockIsAllowed() {
        Inventory inventory = warehouseWith(3);

        inventory.apply(new StockMovement.Issue(HAMMER, 3, NOW));

        assertThat(inventory.find(HAMMER).orElseThrow().quantity()).isZero();
    }

    @Test
    void anAdjustmentSetsTheQuantityInsteadOfAddingToIt() {
        Inventory inventory = warehouseWith(10);

        inventory.apply(new StockMovement.Adjustment(HAMMER, 7, "annual count", NOW));

        assertThat(inventory.find(HAMMER).orElseThrow().quantity()).isEqualTo(7);
    }

    @Test
    void movingAnUnknownProductIsRejected() {
        Inventory inventory = warehouseWith(1);

        MovementOutcome outcome = inventory.apply(new StockMovement.Receipt(CABLE, 1, NOW));

        assertThat(outcome)
                .isInstanceOfSatisfying(Rejected.class, rejected ->
                        assertThat(rejected.reason()).isEqualTo(Rejected.Reason.UNKNOWN_PRODUCT));
    }

    @Test
    void refusesToRegisterTheSameSkuTwice() {
        Inventory inventory = warehouseWith(1);

        assertThatThrownBy(() -> inventory.register(Product.of("TOO-0001", "Another", Category.TOOLS, "1.00", 0)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void listsWhatNeedsReorderingOrderedByHowShortItIs() {
        Inventory inventory = Inventory.empty();
        inventory.register(Product.of("TOO-0001", "Hammer", Category.TOOLS, "12.50", 5).withQuantity(4));
        inventory.register(Product.of("ELE-0002", "Cable", Category.ELECTRONICS, "3.00", 10).withQuantity(1));
        inventory.register(Product.of("PAC-0003", "Boxes", Category.PACKAGING, "0.80", 2).withQuantity(50));

        assertThat(inventory.belowMinimum())
                .extracting(product -> product.sku().value())
                .containsExactly("ELE-0002", "TOO-0001");
    }

    @Test
    void addsUpTheValueOfEverythingItHolds() {
        Inventory inventory = Inventory.empty();
        inventory.register(Product.of("TOO-0001", "Hammer", Category.TOOLS, "12.50", 0).withQuantity(2));
        inventory.register(Product.of("ELE-0002", "Cable", Category.ELECTRONICS, "3.00", 0).withQuantity(10));

        assertThat(inventory.totalValue()).isEqualByComparingTo("55.00");
    }
}
