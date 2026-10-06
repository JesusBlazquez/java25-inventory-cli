package io.github.jesusblazquez.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SkuTest {

    @Test
    void normalisesCaseAndWhitespace() {
        assertThat(Sku.of("  too-0042 ").value()).isEqualTo("TOO-0042");
    }

    @ParameterizedTest
    @ValueSource(strings = {"TOO42", "TOOL-0042", "TO-0042", "TOO-42", "", "   "})
    void rejectsMalformedCodes(String value) {
        assertThatThrownBy(() -> Sku.of(value)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equalValuesAreTheSameSku() {
        assertThat(Sku.of("TOO-0042")).isEqualTo(Sku.of("too-0042"));
    }
}
