# Publishing Pipeline Design

**Date:** 2026-03-18
**Scope:** Maven Central publishing workflow, versioning strategy, changelog process

---

## Overview

TwitchKt publishes 8 modules to Maven Central via Sonatype Central Portal. Releases are triggered by pushing a git tag. The workflow validates the tag against `VERSION_NAME` in `gradle.properties`, publishes all modules, extracts the relevant `CHANGELOG.md` block, and creates a GitHub Release automatically.

---

## Versioning

`VERSION_NAME` in `gradle.properties` is the single source of truth.

Format: SemVer with optional pre-release labels:

```
0.1.0-alpha01
0.1.0-beta01
0.1.0-rc01
0.1.0
0.2.0
1.0.0
```

**Release process:**
1. Bump `VERSION_NAME` in `gradle.properties`
2. Move `[Unreleased]` content in `CHANGELOG.md` to a new `## [X.Y.Z] - DATE` section
3. Update comparison links at the bottom of `CHANGELOG.md`
4. Commit both files
5. Tag: `git tag vX.Y.Z && git push --tags`

The publish workflow asserts that the tag (minus the leading `v`) equals `VERSION_NAME`. If they differ it fails before touching Maven Central.

**Failure recovery:** If the workflow fails after the tag has been pushed (e.g. Maven Central rejects the upload), delete the tag locally and remotely, fix the issue, and re-tag:

```bash
git tag -d vX.Y.Z
git push origin :refs/tags/vX.Y.Z
# fix, then re-tag
git tag vX.Y.Z && git push --tags
```

---

## Changelog

`CHANGELOG.md` at the repo root, [Keep a Changelog](https://keepachangelog.com) format.

```markdown
# Changelog

## [Unreleased]

### Added
- ...

## [0.1.0-alpha01] - 2026-03-18

### Added
- Initial release (#1)

[Unreleased]: https://github.com/CaptnBlubber/twitchkt/compare/v0.1.0-alpha01...HEAD
[0.1.0-alpha01]: https://github.com/CaptnBlubber/twitchkt/releases/tag/v0.1.0-alpha01
```

**Maintenance:** add bullets under `[Unreleased]` as PRs merge. At release time, move to a versioned section and update the links.

**Changelog extraction in the workflow:** skip the `[Unreleased]` heading; find the first `## [X.Y.Z]` heading (a versioned heading — any `## [` line that is not `## [Unreleased]`); assert it matches `## [VERSION_NAME]` and fail with a clear error if not; extract everything from that heading up to (but not including) the next `## [` heading or end of file. Write the extracted block to `$RUNNER_TEMP/release-notes.md`. If the file is empty, fail the workflow rather than creating a GitHub Release with empty notes.

---

## Publish Workflow (`.github/workflows/publish.yml`)

**Trigger:** `push` on tags matching `v*`

**Permissions:** `contents: write` — set at **job level** (not workflow level) to avoid granting elevated permissions to any future jobs added to the workflow

**Steps:**

1. `actions/checkout@v4`
2. `actions/setup-java@v4` — temurin, Java 21
3. `gradle/actions/setup-gradle@v4` — Gradle caching
4. **Validate version** — extract `VERSION_NAME` from `gradle.properties`, strip `v` from tag, assert they match; fail with a clear message if not
5. **Publish** — `./gradlew publishAllPublicationsToMavenCentralRepository`
6. **Extract changelog** — parse `CHANGELOG.md` using the extraction rule above to get the block matching the released version
7. **Create GitHub Release** — `gh release create vX.Y.Z --title "vX.Y.Z" --notes-file <extracted-block-file>`. If `VERSION_NAME` contains `alpha`, `beta`, or `rc`, also pass `--prerelease`.

**Environment variables (vanniktech plugin properties):**

| Env variable | GitHub Secret |
|---|---|
| `ORG_GRADLE_PROJECT_mavenCentralUsername` | `MAVEN_CENTRAL_USERNAME` |
| `ORG_GRADLE_PROJECT_mavenCentralPassword` | `MAVEN_CENTRAL_PASSWORD` |
| `ORG_GRADLE_PROJECT_signingInMemoryKey` | `SIGNING_KEY` |
| `ORG_GRADLE_PROJECT_signingInMemoryKeyId` | `SIGNING_KEY_ID` (value: `8F3CEBC1`) |
| `ORG_GRADLE_PROJECT_signingInMemoryKeyPassword` | `SIGNING_PASSWORD` |

---

## Secrets Setup

| Secret | Status | Value |
|---|---|---|
| `MAVEN_CENTRAL_USERNAME` | Added | Sonatype Central Portal user token username |
| `MAVEN_CENTRAL_PASSWORD` | Added | Sonatype Central Portal user token password |
| `SIGNING_KEY` | Added | GPG key `F87A0AFF8F3CEBC1` exported as armored base64 |
| `SIGNING_KEY_ID` | **Needs adding** | `8F3CEBC1` (last 8 chars of fingerprint) |
| `SIGNING_PASSWORD` | Added | GPG key passphrase |

GPG public key uploaded to `keyserver.ubuntu.com` and `keys.openpgp.org`.

---

## Files Changed / Created

| File | Change |
|---|---|
| `.github/workflows/publish.yml` | New — tag-triggered publish workflow |
| `CHANGELOG.md` | New — Keep a Changelog format with initial `[Unreleased]` section |
