package io.github.jesusblazquez.inventory.cli;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.jesusblazquez.inventory.domain.Category;
import org.junit.jupiter.api.Test;

class CommandParserTest {

    private static ParseResult parse(String... args) {
        return CommandParser.parse(args);
    }

    @Test
    void noArgumentsShowsTheHelp() {
        assertThat(parse()).isEqualTo(new ParseResult.Parsed(new Command.Help()));
    }

    @Test
    void readsAnAddCommand() {
        ParseResult result = parse("add", "TOO-0001", "Hammer", "tools", "12.50", "3");

        assertThat(result)
                .isInstanceOfSatisfying(ParseResult.Parsed.class, parsed -> assertThat(parsed.command())
                        .isInstanceOfSatisfying(Command.Add.class, add -> {
                            assertThat(add.sku().value()).isEqualTo("TOO-0001");
                            assertThat(add.category()).isEqualTo(Category.TOOLS);
                            assertThat(add.unitPrice()).isEqualByComparingTo("12.50");
                            assertThat(add.minimumStock()).isEqualTo(3);
                        }));
    }

    @Test
    void joinsTheReasonOfAnAdjustment() {
        ParseResult result = parse("adjust", "TOO-0001", "7", "annual", "stock", "count");

        assertThat(result)
                .isInstanceOfSatisfying(ParseResult.Parsed.class, parsed -> assertThat(parsed.command())
                        .isInstanceOfSatisfying(
                                Command.Adjust.class,
                                adjust -> assertThat(adjust.reason()).isEqualTo("annual stock count")));
    }

    @Test
    void complainsAboutAnUnknownCommand() {
        assertThat(parse("teleport"))
                .isInstanceOfSatisfying(
                        ParseResult.Invalid.class,
                        invalid -> assertThat(invalid.message()).contains("Unknown command"));
    }

    @Test
    void complainsAboutMissingArguments() {
        assertThat(parse("receive", "TOO-0001"))
                .isInstanceOfSatisfying(
                        ParseResult.Invalid.class, invalid -> assertThat(invalid.message()).contains("Usage"));
    }

    @Test
    void complainsAboutAMalformedSku() {
        assertThat(parse("receive", "nope", "2")).isInstanceOf(ParseResult.Invalid.class);
    }

    @Test
    void complainsAboutANonNumericQuantity() {
        assertThat(parse("receive", "TOO-0001", "many"))
                .isInstanceOfSatisfying(
                        ParseResult.Invalid.class, invalid -> assertThat(invalid.message()).contains("not a valid"));
    }

    @Test
    void refusesZeroOrNegativeUnits() {
        assertThat(parse("issue", "TOO-0001", "0")).isInstanceOf(ParseResult.Invalid.class);
        assertThat(parse("issue", "TOO-0001", "-3")).isInstanceOf(ParseResult.Invalid.class);
    }

    @Test
    void readsTheReportCommands() {
        assertThat(parse("list")).isEqualTo(new ParseResult.Parsed(new Command.List()));
        assertThat(parse("low-stock")).isEqualTo(new ParseResult.Parsed(new Command.LowStock()));
        assertThat(parse("value")).isEqualTo(new ParseResult.Parsed(new Command.Value()));
    }
}
