package io.github.jesusblazquez.inventory.domain;

import java.time.Instant;

/**
 * Something that changes the stock of a product.
 *
 * <p>A sealed interface closes the set of possibilities: the compiler then knows that a switch over
 * a movement covering these three cases is exhaustive, so no {@code default} branch is needed. Add a
 * fourth kind of movement and every switch that handles them stops compiling — which is exactly
 * where you want to find out.
 */
public sealed interface StockMovement permits StockMovement.Receipt, StockMovement.Issue, StockMovement.Adjustment {

    Sku sku();

    Instant occurredAt();

    /** Goods arriving from a supplier. */
    record Receipt(Sku sku, int units, Instant occurredAt) implements StockMovement {
        public Receipt {
            if (units <= 0) {
                throw new IllegalArgumentException("A receipt must add at least one unit");
            }
        }
    }

    /** Goods leaving the warehouse. */
    record Issue(Sku sku, int units, Instant occurredAt) implements StockMovement {
        public Issue {
            if (units <= 0) {
                throw new IllegalArgumentException("An issue must remove at least one unit");
            }
        }
    }

    /** A correction after a stock count, which sets the quantity instead of adding to it. */
    record Adjustment(Sku sku, int countedQuantity, String reason, Instant occurredAt) implements StockMovement {
        public Adjustment {
            if (countedQuantity < 0) {
                throw new IllegalArgumentException("A counted quantity cannot be negative");
            }
            if (reason == null || reason.isBlank()) {
                throw new IllegalArgumentException("An adjustment must state a reason");
            }
        }
    }
}
