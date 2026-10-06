package io.github.jesusblazquez.inventory.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * A product and the stock held of it.
 *
 * <p>Immutable: changing the stock returns a new product rather than mutating this one, so a
 * movement that turns out to be invalid cannot leave a half-applied state behind.
 */
public record Product(Sku sku, String name, Category category, BigDecimal unitPrice, int quantity, int minimumStock) {

    public Product {
        Objects.requireNonNull(sku, "sku is required");
        Objects.requireNonNull(category, "category is required");
        Objects.requireNonNull(unitPrice, "unit price is required");
        name = requireText(name, "name");
        unitPrice = unitPrice.setScale(2, RoundingMode.HALF_EVEN);

        if (unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        if (minimumStock < 0) {
            throw new IllegalArgumentException("Minimum stock cannot be negative");
        }
    }

    public static Product of(String sku, String name, Category category, String unitPrice, int minimumStock) {
        return new Product(Sku.of(sku), name, category, new BigDecimal(unitPrice), 0, minimumStock);
    }

    public Product withQuantity(int newQuantity) {
        return new Product(sku, name, category, unitPrice, newQuantity, minimumStock);
    }

    public boolean isBelowMinimum() {
        return quantity < minimumStock;
    }

    /** What the stock of this product is worth. */
    public BigDecimal stockValue() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field + " is required");
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be blank");
        }
        return value.trim();
    }
}
