package io.github.jesusblazquez.inventory.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A stock keeping unit: the code a warehouse uses to name a product, such as {@code TOO-0042}.
 *
 * <p>A record with a compact constructor is the shortest honest way to say "this is a value, and it
 * is only ever valid": there is no setter, no builder and no way to hold a malformed one.
 */
public record Sku(String value) implements Comparable<Sku> {

    private static final Pattern FORMAT = Pattern.compile("[A-Z]{3}-\\d{4}");

    public Sku {
        Objects.requireNonNull(value, "sku is required");
        value = value.trim().toUpperCase();
        if (!FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("SKU '%s' must look like ABC-1234".formatted(value));
        }
    }

    public static Sku of(String value) {
        return new Sku(value);
    }

    @Override
    public int compareTo(Sku other) {
        return value.compareTo(other.value);
    }

    @Override
    public String toString() {
        return value;
    }
}
