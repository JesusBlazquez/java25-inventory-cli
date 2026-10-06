package io.github.jesusblazquez.inventory.cli;

/**
 * What came out of reading the command line: either a command to run, or a reason why not.
 *
 * <p>Parsing failures are expected — people mistype — so they travel as a value rather than as an
 * exception, and the caller cannot forget to handle them.
 */
public sealed interface ParseResult {

    record Parsed(Command command) implements ParseResult {}

    record Invalid(String message) implements ParseResult {}

    static ParseResult parsed(Command command) {
        return new Parsed(command);
    }

    static ParseResult invalid(String message) {
        return new Invalid(message);
    }
}
