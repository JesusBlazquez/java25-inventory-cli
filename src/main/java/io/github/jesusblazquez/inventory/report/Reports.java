package io.github.jesusblazquez.inventory.report;

import io.github.jesusblazquez.inventory.domain.Category;
import io.github.jesusblazquez.inventory.domain.Inventory;
import io.github.jesusblazquez.inventory.domain.Product;
import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Turns the inventory into the text a warehouse manager actually reads. */
public final class Reports {

    private static final String ROW = "%-10s %-28s %-14s %10s %8d %8d";

    private Reports() {}

    public static String productTable(Inventory inventory) {
        if (inventory.size() == 0) {
            return "The warehouse is empty. Add a product with: inv add <sku> <name> <category> <price> <minimum>";
        }

        String header =
                """
                SKU        NAME                         CATEGORY            PRICE    STOCK  MINIMUM
                ---------- ---------------------------- -------------- ---------- -------- --------""";

        String rows = inventory.all().stream().map(Reports::row).collect(Collectors.joining(System.lineSeparator()));

        return header + System.lineSeparator() + rows;
    }

    public static String lowStock(Inventory inventory) {
        var low = inventory.belowMinimum();
        if (low.isEmpty()) {
            return "Every product is at or above its minimum stock.";
        }

        String rows = low.stream()
                .map(product -> "%-10s %-28s %8d below a minimum of %d"
                        .formatted(product.sku(), product.name(), product.quantity(), product.minimumStock()))
                .collect(Collectors.joining(System.lineSeparator()));

        return "%d product(s) need reordering:%s%s".formatted(low.size(), System.lineSeparator(), rows);
    }

    /** Total value, broken down by category: a grouping collector is the whole implementation. */
    public static String valuation(Inventory inventory) {
        Map<Category, BigDecimal> byCategory = inventory.all().stream()
                .collect(Collectors.groupingBy(
                        Product::category,
                        TreeMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Product::stockValue, BigDecimal::add)));

        String lines = byCategory.entrySet().stream()
                .map(entry -> "%-16s %12s".formatted(entry.getKey(), entry.getValue().toPlainString()))
                .collect(Collectors.joining(System.lineSeparator()));

        return """
                Stock value by category
                -----------------------
                %s
                -----------------------
                %-16s %12s"""
                .formatted(lines, "TOTAL", inventory.totalValue().toPlainString());
    }

    private static String row(Product product) {
        return ROW.formatted(
                product.sku(),
                truncate(product.name(), 28),
                product.category(),
                product.unitPrice().toPlainString(),
                product.quantity(),
                product.minimumStock());
    }

    private static String truncate(String value, int length) {
        return value.length() <= length ? value : value.substring(0, length - 1) + "…";
    }
}
