package io.github.jesusblazquez.inventory.domain;

import java.util.Arrays;
import java.util.stream.Collectors;

/** What a product is, for grouping and reporting. */
public enum Category {
    TOOLS,
    ELECTRONICS,
    CONSUMABLES,
    PACKAGING,
    SPARE_PARTS;

    public static Category parse(String value) {
        return Arrays.stream(values())
                .filter(category -> category.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown category '%s'. Valid values: %s".formatted(value, names())));
    }

    public static String names() {
        return Arrays.stream(values()).map(Enum::name).collect(Collectors.joining(", "));
    }
}
