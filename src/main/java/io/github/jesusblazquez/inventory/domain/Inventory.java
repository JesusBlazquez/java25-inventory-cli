package io.github.jesusblazquez.inventory.domain;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Optional;
import java.util.SequencedCollection;
import java.util.stream.Collectors;

/**
 * The warehouse: the products it holds and the rules for moving stock.
 *
 * <p>Every change goes through {@link #apply(StockMovement)}, so there is a single place where the
 * rules live and a single place to look when they change.
 */
public final class Inventory {

    private final Map<Sku, Product> products = new LinkedHashMap<>();

    public static Inventory empty() {
        return new Inventory();
    }

    public static Inventory of(Collection<Product> products) {
        Inventory inventory = new Inventory();
        products.forEach(inventory::register);
        return inventory;
    }

    /** Adds a product the warehouse did not stock before. */
    public Product register(Product product) {
        if (products.containsKey(product.sku())) {
            throw new IllegalArgumentException("Product %s already exists".formatted(product.sku()));
        }
        products.put(product.sku(), product);
        return product;
    }

    /**
     * Applies a movement.
     *
     * <p>The switch below is exhaustive without a {@code default}: the compiler knows every kind of
     * movement because the interface is sealed. Record patterns pull the fields out in the same
     * expression that matches the type.
     */
    public MovementOutcome apply(StockMovement movement) {
        Product product = products.get(movement.sku());
        if (product == null) {
            return MovementOutcome.rejected(movement.sku(), MovementOutcome.Rejected.Reason.UNKNOWN_PRODUCT);
        }

        return switch (movement) {
            // The underscore is an unnamed pattern: it says "this field exists and I do not use it",
            // which is clearer than inventing a name nobody reads.
            case StockMovement.Receipt(_, int units, _) -> store(product.withQuantity(product.quantity() + units));

            // A guarded pattern: same shape, different rule. Order matters, the guarded one goes first.
            case StockMovement.Issue(Sku sku, int units, _) when units > product.quantity() ->
                MovementOutcome.rejected(sku, MovementOutcome.Rejected.Reason.NOT_ENOUGH_STOCK);

            case StockMovement.Issue(_, int units, _) -> store(product.withQuantity(product.quantity() - units));

            // A stock count replaces the quantity instead of adding to it.
            case StockMovement.Adjustment(_, int counted, _, _) -> store(product.withQuantity(counted));
        };
    }

    public Optional<Product> find(Sku sku) {
        return Optional.ofNullable(products.get(sku));
    }

    /** Every product, ordered by SKU. The returned collection keeps that order. */
    public SequencedCollection<Product> all() {
        return products.values().stream()
                .sorted(Comparator.comparing(Product::sku))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /** Products that have fallen below their minimum stock, the ones a buyer needs to see. */
    public List<Product> belowMinimum() {
        return all().stream()
                .filter(Product::isBelowMinimum)
                .sorted(Comparator.comparingInt(Product::quantity))
                .toList();
    }

    /** What everything in the warehouse is worth. */
    public BigDecimal totalValue() {
        return products.values().stream()
                .map(Product::stockValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int size() {
        return products.size();
    }

    private MovementOutcome store(Product product) {
        products.put(product.sku(), product);
        return MovementOutcome.applied(product);
    }
}
