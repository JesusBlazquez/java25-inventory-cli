# java25-inventory-cli

[![CI](https://github.com/JesusBlazquez/java25-inventory-cli/actions/workflows/ci.yml/badge.svg)](https://github.com/JesusBlazquez/java25-inventory-cli/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-25-orange)
![Dependencies](https://img.shields.io/badge/runtime%20dependencies-0-success)
![License](https://img.shields.io/badge/license-MIT-blue)

A warehouse inventory tool for the command line, written in modern Java with **no frameworks and no
runtime dependencies**.

## The problem it solves

A small warehouse needs to know what it has, what is leaving, and what to reorder — without a server,
a database or a web interface. One jar, one CSV file, and commands that fit in a shell script or a
scheduled job.

It is also a deliberate exercise: every feature here could have been handed to a library. Doing it
with the JDK alone is what shows whether the language itself is understood.

## Getting started

**Prerequisites:** JDK 25 — to build *and* to run.

```bash
./mvnw package
java -jar target/inv.jar help
```

> Maven compiles with the JDK 25 declared in your toolchains, so the build works even if `JAVA_HOME`
> points at another version. Running the jar does not: Java refuses to load classes built for a newer
> release, so `java -jar` must be a JDK 25 one. If your default is older:
>
> ```bash
> "$JAVA_25_HOME/bin/java" -jar target/inv.jar help     # Windows: & "C:\Program Files\Eclipse Adoptium\jdk-25...\bin\java.exe"
> ```

A worked example:

```bash
java -jar target/inv.jar add TOO-0001 Hammer tools 12.50 5
java -jar target/inv.jar receive TOO-0001 20
java -jar target/inv.jar issue TOO-0001 17
java -jar target/inv.jar low-stock
# 1 product(s) need reordering:
# TOO-0001   Hammer                              3 below a minimum of 5
java -jar target/inv.jar value
```

The inventory lives in `inventory.csv` in the current directory, or wherever `INVENTORY_FILE` points.

## Commands

| Command | What it does |
|---|---|
| `add <sku> <name> <category> <unit-price> <minimum-stock>` | Registers a product |
| `receive <sku> <units>` | Goods arriving from a supplier |
| `issue <sku> <units>` | Goods leaving the warehouse |
| `adjust <sku> <counted> <reason>` | Corrects the stock after a count |
| `list` | Every product and its stock |
| `low-stock` | What needs reordering |
| `value` | Stock value, by category |

Commands return `0` on success and `1` on failure, so a script can act on the result.

## What this project is meant to show

| Feature of the language | Where it is used | Why it earns its place |
|---|---|---|
| Records with compact constructors | `Sku`, `Product`, every command | A value that is only ever valid, with no setters and no builder |
| Sealed interfaces | `StockMovement`, `Command`, `MovementOutcome`, `ParseResult` | The compiler knows every case, so switches need no `default` |
| Pattern matching with record patterns | `Inventory.apply`, `CommandRunner.run` | Match the type and destructure it in one expression |
| Guarded patterns (`when`) | Rejecting an issue larger than the stock | Two rules over the same shape, stated as two cases |
| Unnamed patterns (`_`) | Movement handling | Says "this field exists and I do not use it" instead of inventing a name |
| Text blocks | Help text and reports | Multi-line output that reads in the source as it reads on screen |
| Streams and collectors | Reports and valuation | Grouping by category and adding up is one expression |
| Sequenced collections | `Inventory.all()` | An ordered view, stated in the type |
| `java.nio.file` and atomic moves | `CsvInventoryStore` | An interrupted write cannot corrupt the file |

## Design

```
cli ──────────► domain ◄────────── storage
(parse, dispatch)  (rules)      (CSV, atomic writes)
         └──────► report ◄──────────┘
```

The domain knows nothing about files or arguments. Everything that varies — where the data lives,
where the output goes, what time it is — enters through a parameter, which is what makes the whole
tool testable without touching the real console or clock.

### Running the tests

```bash
./mvnw verify
```

Unit tests cover the rules and the parser; `MainTest` runs the real entry point end to end against a
temporary file, which is what would catch a command wired to the wrong place.

## Technical decisions

| Decision | Why | ADR |
|---|---|---|
| No frameworks, no runtime dependencies | The point is the language, not the library | [0002](docs/adr/0002-no-frameworks-no-dependencies.md) |
| Expected failures as sealed results, not exceptions | Running out of stock is an answer, not an accident | [0003](docs/adr/0003-sealed-results-instead-of-exceptions.md) |
| CSV written atomically | Readable by a person, and safe against an interrupted write | [0004](docs/adr/0004-csv-with-atomic-writes.md) |
| Arguments instead of an interactive menu | Scriptable, and testable without simulating a keyboard | [0005](docs/adr/0005-arguments-over-an-interactive-menu.md) |

## What I would do differently / next steps

- **No concurrency control.** Two processes writing at once would have one overwrite the other. A
  lock file would be the next step, and a real database the one after that.
- **The whole file is read and written on every command.** Fine for a warehouse with hundreds of
  products, wrong for hundreds of thousands.
- **No movement history.** Only the current stock is stored; the movements that produced it are not.
  Keeping them would make the file an audit trail, as the banking project in my profile does.
- **Java 25 was chosen on purpose**, while the rest of my portfolio runs on Java 21 (LTS and what
  most companies use). Here there is no framework or deployment target to constrain it, so it is the
  right place to show where the language is going.

## License

[MIT](LICENSE) © Jesús Blázquez Durán
