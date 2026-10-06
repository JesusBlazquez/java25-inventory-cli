# 0004. A CSV file, written atomically

- **Status:** Accepted
- **Date:** 2026-10-06

## Context

The inventory has to survive between runs. The data is a handful of rows that a warehouse clerk may
well want to open in a spreadsheet.

## Decision

The inventory is stored as a CSV file. Writing goes to a temporary file in the same directory and is
then moved over the real one with `ATOMIC_MOVE`.

## Alternatives considered

- **SQLite via JDBC:** a real database with no server, and the natural choice if this grew. It
  would also overlap with what the other projects in my portfolio already demonstrate about SQL.
- **JSON:** better for nested data, worse for something a person may want to edit in Excel.
- **Writing the file in place:** simpler, and a crash halfway through leaves a truncated inventory.

## Consequences

The file is readable, diffable and editable by hand, and an interrupted write cannot corrupt it.
The limits are the obvious ones: the whole file is read and written on every command, and there is
no concurrency control. For thousands of products or several users, this decision would be revisited.
