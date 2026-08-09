# ADR-0002: Build and Workspace Tooling

- Status: Accepted
- Date: 2026-08-09

## Context

Zakup Gotov is a polyglot monorepo with a Java backend and TypeScript web/client packages. The build must be reproducible, easy to run on developer machines and CI, and conservative enough for a long-lived platform while still allowing fast frontend iteration.

## Decision

Use:

- Java 25 as the Java runtime/toolchain baseline;
- Apache Maven 3.9.16 for the Java build, pinned through Maven Wrapper once the API project is created;
- Node.js 24.18.1 LTS for TypeScript tooling;
- pnpm 11.4.0 with a root workspace for web/shared TypeScript packages;
- repository-level version pins in `.java-version`, `.nvmrc`, and `package.json#packageManager`;
- native Maven and pnpm commands rather than introducing a polyglot orchestration layer in M0A.

This ADR intentionally refines the original M0A plan's Node.js `24.18.0` execution pin to `24.18.1`: execution-time verification found `24.18.1` to be the latest Node 24 LTS patch available before Task 1 was merged. The architectural major-line decision remains unchanged.

## Rationale

### Maven 3.9.16

Maven 3.9.16 is the current maintained GA line at the time of this decision. Maven 4 remains release-candidate software rather than GA, so adopting it would add migration and plugin-compatibility risk without solving a current product requirement.

### Node.js 24 LTS

Node.js 24 is an active LTS line. We prefer an LTS runtime over the newer Current line for repeatable CI and lower upgrade churn, and pin the latest verified patch at the time of foundation execution.

### pnpm 11

pnpm provides workspace support and strict, efficient dependency management. Version 11.4.0 is pinned so local and CI lockfile behavior remains deterministic.

### No Turborepo/Nx/Gradle composite build yet

M0A has only one Java application, one web application, and a small number of TypeScript packages. Native build tools are simpler and easier to diagnose. A cross-language task orchestrator can be introduced later only if measured build/developer-experience needs justify it.

## Consequences

Positive:

- explicit and reproducible toolchain versions;
- small setup surface;
- Java and TypeScript ecosystems use their native tools;
- straightforward GitHub Actions caching and troubleshooting;
- no early dependency on a monorepo framework.

Costs:

- local verification initially coordinates Maven and pnpm with a shell script;
- developers need both Java and Node toolchains installed;
- version upgrades must update pins, CI, documentation, and lockfiles together.

## Revisit triggers

Revisit when:

- Maven 4 is GA and provides a concrete benefit worth migration;
- build graph size makes native sequential commands materially slow;
- many shared TypeScript packages require advanced affected-project execution;
- mobile work introduces a measured need for stronger cross-workspace orchestration.
