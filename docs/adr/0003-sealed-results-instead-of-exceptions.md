# 0003. Expected failures are values, not exceptions

- **Status:** Accepted
- **Date:** 2026-10-06

## Context

Two kinds of things go wrong here: impossible values (a negative price, a malformed SKU) and
ordinary business answers (there is not enough stock, that product does not exist).

## Decision

Impossible values throw from the constructor of the value object, because no caller can do anything
sensible with them. Business answers are returned as sealed results — `MovementOutcome` and
`ParseResult` — so the caller has to handle every case, and the compiler checks that it did.

## Alternatives considered

- **Exceptions for everything:** running out of stock is not exceptional, it happens daily, and an
  exception lets the caller ignore it with an empty catch.
- **Returning `null` or `Optional`:** says that something failed but not why, so the message shown to
  the user has to be invented at the call site.

## Consequences

Switches over these results are exhaustive without a `default`, so adding a new reason breaks the
build until it is handled. Note that the banking project in my profile does the opposite — domain
exceptions — because there a violated invariant really is exceptional. Both are defensible; what
matters is choosing deliberately.
