package io.github.jesusblazquez.inventory.storage;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.jesusblazquez.inventory.domain.Category;
import io.github.jesusblazquez.inventory.domain.Inventory;
import io.github.jesusblazquez.inventory.domain.Product;
import io.github.jesusblazquez.inventory.domain.Sku;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvInventoryStoreTest {

    @TempDir
    Path directory;

    @Test
    void readingAFileThatDoesNotExistGivesAnEmptyWarehouse() {
        CsvInventoryStore store = new CsvInventoryStore(directory.resolve("missing.csv"));

        assertThat(store.load().size()).isZero();
    }

    @Test
    void survivesARoundTrip() {
        Path file = directory.resolve("inventory.csv");
        CsvInventoryStore store = new CsvInventoryStore(file);
        Inventory original = Inventory.empty();
        original.register(Product.of("TOO-0001", "Hammer", Category.TOOLS, "12.50", 3).withQuantity(7));
        original.register(Product.of("ELE-0002", "Cable", Category.ELECTRONICS, "3.00", 10).withQuantity(2));

        store.save(original);
        Inventory reloaded = store.load();

        assertThat(reloaded.size()).isEqualTo(2);
        Product hammer = reloaded.find(Sku.of("TOO-0001")).orElseThrow();
        assertThat(hammer.name()).isEqualTo("Hammer");
        assertThat(hammer.quantity()).isEqualTo(7);
        assertThat(hammer.unitPrice()).isEqualByComparingTo("12.50");
        assertThat(hammer.minimumStock()).isEqualTo(3);
        assertThat(reloaded.totalValue()).isEqualByComparingTo(original.totalValue());
    }

    @Test
    void keepsNamesThatContainCommasAndQuotes() {
        Path file = directory.resolve("inventory.csv");
        CsvInventoryStore store = new CsvInventoryStore(file);
        Inventory inventory = Inventory.empty();
        inventory.register(Product.of("TOO-0003", "Wrench, 12\" adjustable", Category.TOOLS, "19.99", 1));

        store.save(inventory);

        assertThat(store.load().find(Sku.of("TOO-0003")).orElseThrow().name())
                .isEqualTo("Wrench, 12\" adjustable");
    }

    @Test
    void writesAHeaderAndOneLinePerProduct() throws IOException {
        Path file = directory.resolve("inventory.csv");
        CsvInventoryStore store = new CsvInventoryStore(file);
        Inventory inventory = Inventory.empty();
        inventory.register(Product.of("TOO-0001", "Hammer", Category.TOOLS, "12.50", 3));

        store.save(inventory);

        assertThat(Files.readAllLines(file))
                .hasSize(2)
                .first()
                .isEqualTo("sku,name,category,unit_price,quantity,minimum_stock");
    }

    @Test
    void leavesNoTemporaryFilesBehind() {
        Path file = directory.resolve("inventory.csv");
        CsvInventoryStore store = new CsvInventoryStore(file);
        Inventory inventory = Inventory.empty();
        inventory.register(Product.of("TOO-0001", "Hammer", Category.TOOLS, "12.50", 3));

        store.save(inventory);
        store.save(inventory);

        assertThat(directory.toFile().list()).containsExactly("inventory.csv");
    }
}
