package io.github.jesusblazquez.inventory.cli;

import io.github.jesusblazquez.inventory.domain.Category;
import io.github.jesusblazquez.inventory.domain.Sku;
import java.math.BigDecimal;

/**
 * Everything this tool can be asked to do.
 *
 * <p>Sealed again, for the same reason as the movements: the dispatcher switches over it without a
 * default branch, so adding a command without wiring it breaks the build.
 */
public sealed interface Command {

    record Add(Sku sku, String name, Category category, BigDecimal unitPrice, int minimumStock) implements Command {}

    record Receive(Sku sku, int units) implements Command {}

    record Issue(Sku sku, int units) implements Command {}

    record Adjust(Sku sku, int countedQuantity, String reason) implements Command {}

    record List() implements Command {}

    record LowStock() implements Command {}

    record Value() implements Command {}

    record Help() implements Command {}
}
