# 0005. Commands as arguments, output as a parameter

- **Status:** Accepted
- **Date:** 2026-10-06

## Context

A console tool can read commands from an interactive menu or take them as arguments. And it can
print to `System.out` or write to something it is given.

## Decision

Commands are arguments (`inv receive TOO-0001 10`) and the runner writes to an `Appendable` passed
into its constructor. Time comes in through a `Clock` for the same reason.

## Alternatives considered

- **An interactive menu:** nicer to demonstrate live, much harder to test, and it cannot be put in a
  shell script or a scheduled job.
- **Printing to `System.out` directly:** forces tests to capture a global stream, which makes them
  order-dependent and awkward to run in parallel.

## Consequences

Every command is testable end to end by passing arguments and reading a `StringBuilder`, and exit
codes let a script react to failures. The cost is that a live demonstration is less flashy than a
menu, which the README offsets with a worked example.
