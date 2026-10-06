package io.github.jesusblazquez.inventory.storage;

import io.github.jesusblazquez.inventory.domain.Category;
import io.github.jesusblazquez.inventory.domain.Inventory;
import io.github.jesusblazquez.inventory.domain.Product;
import io.github.jesusblazquez.inventory.domain.Sku;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Stores the inventory as a CSV file, using only the JDK.
 *
 * <p>Writing is atomic: the new content goes to a temporary file in the same directory and is then
 * moved over the old one. A crash halfway through leaves the previous file untouched instead of a
 * truncated one — the kind of detail that only hurts the day it happens.
 */
public final class CsvInventoryStore implements InventoryStore {

    private static final String HEADER = "sku,name,category,unit_price,quantity,minimum_stock";
    private static final char SEPARATOR = ',';

    private final Path file;

    public CsvInventoryStore(Path file) {
        this.file = file;
    }

    @Override
    public Inventory load() {
        if (Files.notExists(file)) {
            return Inventory.empty();
        }
        try (Stream<String> lines = Files.lines(file, StandardCharsets.UTF_8)) {
            List<Product> products = lines.skip(1)
                    .filter(line -> !line.isBlank())
                    .map(CsvInventoryStore::toProduct)
                    .toList();
            return Inventory.of(products);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + file, e);
        }
    }

    @Override
    public void save(Inventory inventory) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        inventory.all().forEach(product -> lines.add(toCsv(product)));

        try {
            Path directory = file.toAbsolutePath().getParent();
            Files.createDirectories(directory);
            Path temporary = Files.createTempFile(directory, "inventory", ".csv");
            Files.write(temporary, lines, StandardCharsets.UTF_8);
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write " + file, e);
        }
    }

    private static Product toProduct(String line) {
        List<String> fields = split(line);
        if (fields.size() != 6) {
            throw new IllegalArgumentException("Malformed line, expected 6 fields: " + line);
        }
        return new Product(
                Sku.of(fields.get(0)),
                fields.get(1),
                Category.parse(fields.get(2)),
                new BigDecimal(fields.get(3)),
                Integer.parseInt(fields.get(4)),
                Integer.parseInt(fields.get(5)));
    }

    private static String toCsv(Product product) {
        return String.join(
                String.valueOf(SEPARATOR),
                product.sku().value(),
                quoteIfNeeded(product.name()),
                product.category().name(),
                product.unitPrice().toPlainString(),
                String.valueOf(product.quantity()),
                String.valueOf(product.minimumStock()));
    }

    /** A name may contain a comma or a quote, so it is quoted the way CSV expects. */
    private static String quoteIfNeeded(String value) {
        if (value.indexOf(SEPARATOR) < 0 && value.indexOf('"') < 0) {
            return value;
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }

    /** Splits a CSV line honouring quoted fields. Small on purpose: no dependency for six columns. */
    private static List<String> split(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (inQuotes && character == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                current.append('"');
                i++;
            } else if (character == '"') {
                inQuotes = !inQuotes;
            } else if (character == SEPARATOR && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }
        fields.add(current.toString());
        return fields;
    }
}
