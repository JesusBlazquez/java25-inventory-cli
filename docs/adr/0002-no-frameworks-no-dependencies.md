# 0002. No frameworks and no runtime dependencies

- **Status:** Accepted
- **Date:** 2026-10-06

## Context

This tool could have been built with Spring Shell or picocli for the command line and Jackson for
the data file, and it would have been shorter.

## Decision

The runtime has no dependencies at all: argument parsing, CSV reading and writing, and report
formatting use only the JDK. JUnit and AssertJ are the only libraries, and only for tests.

## Alternatives considered

- **picocli:** excellent annotation-driven CLI parsing. It would also hide the part this project
  exists to show — that I can model commands and parse input myself.
- **Jackson:** the obvious choice for JSON in a real project, and overkill for six columns.

## Consequences

The jar is tiny, starts instantly and will still build in five years. The cost is code that a
library would have provided: a CSV splitter that honours quotes, and a hand-written parser. Both are
small, tested, and deliberately not reinvented beyond what this tool needs. In a product that had to
read arbitrary CSV, using a library would be the right call.
