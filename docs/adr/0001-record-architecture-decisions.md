# 0001. Record architecture decisions

- **Status:** Accepted
- **Date:** 2026-10-06

## Context

Design decisions should be traceable: anyone reading the code should be able to find out *why* it
was built this way, not only *what* was built.

## Decision

We will keep Architecture Decision Records in `docs/adr/`, one Markdown file per significant
decision, numbered sequentially and never deleted.

## Alternatives considered

- **Comments in the code:** scattered, and they do not capture the options that were rejected.
- **A wiki:** lives apart from the code and drifts out of date.

## Consequences

A few minutes of writing per decision, and in return the README can summarise and link here.
