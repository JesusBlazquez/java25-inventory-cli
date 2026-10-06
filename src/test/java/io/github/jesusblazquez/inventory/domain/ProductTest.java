package io.github.jesusblazquez.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProductTest {

    private static Product hammer(int quantity, int minimum) {
        return new Product(Sku.of("TOO-0001"), "Hammer", Category.TOOLS, new BigDecimal("12.5"), quantity, minimum);
    }

    @Test
    void keepsPricesWithTwoDecimals() {
        assertThat(hammer(0, 0).unitPrice()).isEqualByComparingTo("12.50");
    }

    @Test
    void changingTheStockReturnsANewProduct() {
        Product original = hammer(5, 2);

        Product updated = original.withQuantity(9);

        assertThat(updated.quantity()).isEqualTo(9);
        assertThat(original.quantity()).isEqualTo(5);
    }

    @Test
    void knowsWhenItIsBelowItsMinimum() {
        assertThat(hammer(1, 3).isBelowMinimum()).isTrue();
        assertThat(hammer(3, 3).isBelowMinimum()).isFalse();
    }

    @Test
    void valuesItsOwnStock() {
        assertThat(hammer(4, 0).stockValue()).isEqualByComparingTo("50.00");
    }

    @Test
    void refusesImpossibleValues() {
        assertThatThrownBy(() -> hammer(-1, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Product(
                        Sku.of("TOO-0001"), "  ", Category.TOOLS, BigDecimal.ONE, 0, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Product(
                        Sku.of("TOO-0001"), "Hammer", Category.TOOLS, new BigDecimal("-1"), 0, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
