package io.github.jesusblazquez.inventory.domain;

/**
 * The result of applying a movement, as a value instead of an exception.
 *
 * <p>Running out of stock is an ordinary business answer, not an exceptional condition, so the
 * caller is made to handle it: a sealed result forces every branch to be considered at compile time.
 */
public sealed interface MovementOutcome permits MovementOutcome.Applied, MovementOutcome.Rejected {

    record Applied(Product product) implements MovementOutcome {}

    record Rejected(Sku sku, Reason reason) implements MovementOutcome {

        public enum Reason {
            UNKNOWN_PRODUCT,
            NOT_ENOUGH_STOCK
        }
    }

    static MovementOutcome applied(Product product) {
        return new Applied(product);
    }

    static MovementOutcome rejected(Sku sku, Rejected.Reason reason) {
        return new Rejected(sku, reason);
    }
}
