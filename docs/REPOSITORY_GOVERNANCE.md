# Repository Governance

This document defines the intended GitHub repository controls for `True-Ruslan/zakup-gotov`. Git-tracked configuration and GitHub repository settings are both part of the engineering baseline.

## Principles

1. `main` is reviewed repository truth, not a development branch.
2. Functional work happens in short-lived branches and merges through pull requests.
3. Required checks are based on real successful check names, never guessed strings.
4. Security checks are not disabled merely to make a pull request green.
5. Repository settings should minimize accidental privilege, irreversible history changes, and supply-chain risk.
6. Rules remain practical for a single-maintainer project; controls that require a second human are introduced only when another maintainer exists.

## Merge policy

Target repository settings:

- squash merge: enabled;
- merge commits: disabled;
- rebase merge: disabled;
- automatic head-branch deletion after merge: enabled;
- auto-merge: enabled;
- update-branch button: enabled;
- default branch: `main`.

This gives a linear target history while preserving rich development history in pull requests.

## `main` ruleset

Target rules for `main`:

- changes require a pull request;
- linear history is required;
- force pushes are blocked;
- branch deletion is blocked;
- unresolved review conversations block merge;
- required status checks must pass;
- no mandatory second-human approval while the project has one maintainer;
- administrators should normally follow the same rules rather than silently bypass them.

Required checks are activated only after they have completed successfully at least once in the repository. Current/target check families are:

- `API CI`;
- `Contract CI`;
- `Web CI`;
- `Web E2E`;
- CodeQL Java;
- CodeQL JavaScript/TypeScript;
- `Dependency Review` once GitHub Dependency Graph is operational;
- future container/release checks after their workflows are proven.

## Branch lifecycle

Long-lived branches are avoided. Normally the repository should contain:

- `main`;
- active pull-request branches only.

After a pull request is squash-merged, its source branch should be deleted automatically. Historical context remains in the pull request and target history.

## GitHub Actions policy

Permanent workflows must solve a recurring repository need. One-off generators/scaffold workflows are deleted immediately after the generated artifact is committed and independently verified.

Default workflow permissions should be read-only. A workflow receives write permissions only for the smallest explicit capability needed by that workflow.

Third-party and GitHub-owned actions should ultimately be pinned by full commit SHA in permanent security-sensitive workflows. Dependabot for `github-actions` keeps those pins reviewable and current.

Do not grant workflows permission to approve pull requests.

## Security features

Target security baseline for this public repository:

- Dependency Graph;
- Dependabot alerts;
- Dependabot security updates;
- Dependabot version updates;
- CodeQL code scanning;
- Dependency Review on pull requests;
- secret scanning;
- push protection;
- Private Vulnerability Reporting;
- GitHub Security Advisories for coordinated fixes.

A known security gate that cannot execute because a repository setting is disabled is a repository defect, not a reason to weaken the workflow.

## Issues and community files

The repository tracks:

- structured bug reports;
- structured feature requests;
- public contribution rules;
- security disclosure policy;
- Code of Conduct;
- CODEOWNERS;
- pull request quality template.

Security vulnerabilities must not be filed as public issues.

GitHub Discussions, Wiki, Projects, sponsorship/funding, and CITATION metadata are intentionally not enabled merely to maximize feature count. They should be enabled only when the project has a real workflow that benefits from them.

## License

Repository visibility and software licensing are separate decisions. The repository is public but currently has no open-source license. A license will be added only after the project owner deliberately chooses the intended reuse/distribution model.

## Releases and packages

The approved next release-engineering direction is:

- versioned GitHub Releases;
- prebuilt `api` and `web` OCI images published to GitHub Container Registry;
- multi-platform `linux/amd64` and `linux/arm64` images;
- a release Compose bundle that starts `web + api + PostgreSQL` without local builds;
- release verification against the exact container set that users will run;
- vulnerability scanning, SBOM, provenance/attestation, and immutable image digests;
- prereleases do not update the stable `latest` tag.

This design is specified separately before implementation and is not considered implemented merely because it is documented here.

## Audit cadence

Repository governance should be reviewed when any of the following changes:

- a new required workflow/check is introduced;
- another maintainer joins;
- releases begin;
- secrets/provider credentials are introduced;
- GitHub changes available security controls;
- a security incident or CI bypass reveals a policy gap.
