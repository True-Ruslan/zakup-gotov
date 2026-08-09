# Security Policy

## Supported versions

Zakup Gotov is currently in pre-release development. No released version is considered supported yet.

Once public releases begin, this section will list supported versions and security-fix policy explicitly.

## Reporting a vulnerability

Please do **not** open a public issue for a suspected vulnerability.

Preferred reporting path: GitHub Private Vulnerability Reporting for this repository once it is enabled in repository settings.

Until that feature is enabled, contact the repository owner privately through an appropriate verified channel rather than publishing exploit details.

A useful report should include:

- affected component and version/commit;
- reproduction steps;
- expected and actual behavior;
- realistic impact;
- proof of concept where safe;
- suggested mitigation if known.

## Security principles

The project treats the following as security-sensitive by design:

- retailer/provider credentials and tokens;
- user addresses and precise location data;
- authentication/session material;
- external provider responses that may contain identifiers;
- logs/traces that could accidentally include credentials or location data.

Secrets must never be committed to the repository. Logs, traces, fixtures, and test artifacts must be reviewed for secret and personal-data leakage before publication.

## Dependency and supply-chain policy

The project intends to use GitHub-native dependency review, Dependabot, secret scanning/push protection, and CodeQL as repository quality gates. High-severity security issues block release until triaged and resolved or explicitly documented with a justified exception.