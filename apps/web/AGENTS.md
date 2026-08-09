# Web application guidance

This package uses Next.js 16.2 / React 19.2 and may differ from older Next.js conventions.

Before changing framework-specific behavior:

1. read the relevant installed Next.js documentation under `node_modules/next/dist/docs/`;
2. follow the repository-wide rules in `../../docs/ENGINEERING.md` and `../../docs/DEVELOPMENT.md`;
3. preserve the generated OpenAPI client boundary instead of duplicating backend contracts in web code;
4. use TDD for executable behavior and run the focused test before the relevant regression suite;
5. keep responsive and keyboard-accessible behavior covered by component and/or Playwright tests when affected;
6. do not introduce retailer secrets, provider credentials, or direct retailer integrations into client code without an approved provider-specific design.

The root repository documentation is authoritative for architecture, state, security, and roadmap decisions.
