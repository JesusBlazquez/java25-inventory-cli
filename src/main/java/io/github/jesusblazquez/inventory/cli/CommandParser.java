package io.github.jesusblazquez.inventory.cli;

import io.github.jesusblazquez.inventory.domain.Category;
import io.github.jesusblazquez.inventory.domain.Sku;
import java.math.BigDecimal;
import java.util.Arrays;

/** Turns command line arguments into a {@link Command}, or explains why it cannot. */
public final class CommandParser {

    private CommandParser() {}

    public static ParseResult parse(String[] args) {
        if (args.length == 0) {
            return ParseResult.parsed(new Command.Help());
        }

        String verb = args[0].toLowerCase();
        String[] rest = Arrays.copyOfRange(args, 1, args.length);

        try {
            return switch (verb) {
                case "add" -> parseAdd(rest);
                case "receive" -> hasAtLeast(rest, 2)
                        ? ParseResult.parsed(new Command.Receive(Sku.of(rest[0]), positive(rest[1], "units")))
                        : usage("receive <sku> <units>");
                case "issue" -> hasAtLeast(rest, 2)
                        ? ParseResult.parsed(new Command.Issue(Sku.of(rest[0]), positive(rest[1], "units")))
                        : usage("issue <sku> <units>");
                case "adjust" -> parseAdjust(rest);
                case "list" -> ParseResult.parsed(new Command.List());
                case "low-stock" -> ParseResult.parsed(new Command.LowStock());
                case "value" -> ParseResult.parsed(new Command.Value());
                case "help", "-h", "--help" -> ParseResult.parsed(new Command.Help());
                default -> ParseResult.invalid("Unknown command '%s'. Run 'inv help' to see them all.".formatted(verb));
            };
        } catch (IllegalArgumentException e) {
            // Value objects validate themselves; here we only translate the complaint.
            return ParseResult.invalid(e.getMessage());
        }
    }

    private static ParseResult parseAdd(String[] args) {
        if (args.length < 5) {
            return usage("add <sku> <name> <category> <unit-price> <minimum-stock>");
        }
        return ParseResult.parsed(new Command.Add(
                Sku.of(args[0]),
                args[1],
                Category.parse(args[2]),
                new BigDecimal(args[3]),
                notNegative(args[4], "minimum stock")));
    }

    private static ParseResult parseAdjust(String[] args) {
        if (args.length < 3) {
            return usage("adjust <sku> <counted-quantity> <reason>");
        }
        String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        return ParseResult.parsed(
                new Command.Adjust(Sku.of(args[0]), notNegative(args[1], "counted quantity"), reason));
    }

    private static boolean hasAtLeast(String[] args, int expected) {
        return args.length >= expected;
    }

    private static ParseResult usage(String usage) {
        return ParseResult.invalid("Usage: inv " + usage);
    }

    private static int positive(String raw, String field) {
        int value = number(raw, field);
        if (value <= 0) {
            throw new IllegalArgumentException("The %s must be greater than zero".formatted(field));
        }
        return value;
    }

    private static int notNegative(String raw, String field) {
        int value = number(raw, field);
        if (value < 0) {
            throw new IllegalArgumentException("The %s cannot be negative".formatted(field));
        }
        return value;
    }

    private static int number(String raw, String field) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'%s' is not a valid %s".formatted(raw, field));
        }
    }
}
