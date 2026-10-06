package io.github.jesusblazquez.inventory;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * End to end through the real entry point: arguments in, text and exit code out, with a real file
 * on disk in between. These are the tests that would catch a command wired to the wrong use case.
 */
class MainTest {

    @TempDir
    Path directory;

    private record Result(int exitCode, String output) {}

    private Result run(String... args) {
        StringBuilder out = new StringBuilder();
        int exitCode = Main.run(args, directory.resolve("inventory.csv"), out);
        return new Result(exitCode, out.toString());
    }

    @Test
    void addsReceivesAndIssuesStockAcrossRuns() {
        assertThat(run("add", "TOO-0001", "Hammer", "tools", "12.50", "3").exitCode()).isZero();
        assertThat(run("receive", "TOO-0001", "10").output()).contains("Stock is now 10");
        assertThat(run("issue", "TOO-0001", "4").output()).contains("Stock is now 6");

        Result listing = run("list");
        assertThat(listing.exitCode()).isZero();
        assertThat(listing.output()).contains("TOO-0001").contains("Hammer").contains("TOOLS");
    }

    @Test
    void refusesToIssueMoreThanThereIs() {
        run("add", "TOO-0001", "Hammer", "tools", "12.50", "3");
        run("receive", "TOO-0001", "2");

        Result result = run("issue", "TOO-0001", "5");

        assertThat(result.exitCode()).isEqualTo(1);
        assertThat(result.output()).contains("Not enough stock");
    }

    @Test
    void reportsWhatNeedsReordering() {
        run("add", "TOO-0001", "Hammer", "tools", "12.50", "5");
        run("receive", "TOO-0001", "2");

        assertThat(run("low-stock").output()).contains("TOO-0001").contains("below a minimum of 5");
    }

    @Test
    void valuesTheStockByCategory() {
        run("add", "TOO-0001", "Hammer", "tools", "10.00", "0");
        run("receive", "TOO-0001", "3");
        run("add", "ELE-0002", "Cable", "electronics", "2.50", "0");
        run("receive", "ELE-0002", "4");

        String output = run("value").output();

        assertThat(output).contains("TOOLS").contains("30.00").contains("ELECTRONICS").contains("10.00");
        assertThat(output).contains("TOTAL").contains("40.00");
    }

    @Test
    void correctsTheStockAfterACount() {
        run("add", "TOO-0001", "Hammer", "tools", "12.50", "3");
        run("receive", "TOO-0001", "10");

        Result result = run("adjust", "TOO-0001", "8", "annual", "count");

        assertThat(result.exitCode()).isZero();
        assertThat(result.output()).contains("set to 8").contains("annual count");
    }

    @Test
    void failsWithAMessageAndANonZeroCodeOnBadInput() {
        Result result = run("receive", "TOO-9999", "1");

        assertThat(result.exitCode()).isEqualTo(1);
        assertThat(result.output()).contains("no product with SKU");
    }

    @Test
    void printsTheHelpWithNoArguments() {
        Result result = run();

        assertThat(result.exitCode()).isZero();
        assertThat(result.output()).contains("inv add").contains("inv low-stock");
    }
}
