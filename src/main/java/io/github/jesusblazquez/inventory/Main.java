package io.github.jesusblazquez.inventory;

import io.github.jesusblazquez.inventory.cli.Command;
import io.github.jesusblazquez.inventory.cli.CommandParser;
import io.github.jesusblazquez.inventory.cli.CommandRunner;
import io.github.jesusblazquez.inventory.cli.ParseResult;
import io.github.jesusblazquez.inventory.storage.CsvInventoryStore;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.Clock;

/**
 * The entry point. It does three things and nothing else: read the environment, hand over to the
 * parser and the runner, and return an exit code a shell script can act on.
 */
public final class Main {

    public static void main(String[] args) {
        Path dataFile = Path.of(System.getenv().getOrDefault("INVENTORY_FILE", "inventory.csv"));

        // Not a try-with-resources: closing this writer would close System.out, and exiting before
        // flushing it would swallow the output. Append does not flush on its own, unlike println.
        PrintWriter out = new PrintWriter(System.out);
        int exitCode;
        try {
            exitCode = run(args, dataFile, out);
        } catch (RuntimeException e) {
            out.println("Error: " + e.getMessage());
            exitCode = CommandRunner.FAILED;
        }
        out.flush();
        System.exit(exitCode);
    }

    static int run(String[] args, Path dataFile, Appendable out) {
        CommandRunner runner = new CommandRunner(new CsvInventoryStore(dataFile), out, Clock.systemUTC());

        return switch (CommandParser.parse(args)) {
            case ParseResult.Parsed(Command command) -> runner.run(command);
            case ParseResult.Invalid(String message) -> {
                try {
                    out.append("Error: ").append(message).append(System.lineSeparator());
                } catch (java.io.IOException e) {
                    throw new java.io.UncheckedIOException(e);
                }
                yield CommandRunner.FAILED;
            }
        };
    }
}
