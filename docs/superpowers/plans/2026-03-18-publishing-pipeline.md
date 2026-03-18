# Publishing Pipeline Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a tag-triggered GitHub Actions workflow that validates the version, publishes all 8 TwitchKt modules to Maven Central, extracts the relevant `CHANGELOG.md` block, and creates a GitHub Release automatically.

**Architecture:** `VERSION_NAME` in `gradle.properties` is the version source of truth. Pushing a `v*` tag triggers the publish workflow, which asserts the tag matches `VERSION_NAME`, calls the vanniktech Gradle task to publish to Sonatype Central Portal with in-memory GPG signing, extracts the matching changelog block via Python, and creates a GitHub Release with `--prerelease` for alpha/beta/rc versions.

**Tech Stack:** GitHub Actions, Gradle (`publishAllPublicationsToMavenCentralRepository`), `com.vanniktech.maven.publish` 0.30.0 (`SonatypeHost.CENTRAL_PORTAL`), in-memory GPG signing, `gh` CLI, Python 3 (available on all `ubuntu-latest` runners)

---

## Chunk 1: CHANGELOG.md and publish workflow

### Task 1: Create CHANGELOG.md

**Files:**
- Create: `CHANGELOG.md`

- [ ] **Step 1: Create `CHANGELOG.md` at the repo root**

```markdown
# Changelog

All notable changes to TwitchKt will be documented here.
Format: [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)

## [Unreleased]

## [0.1.0-alpha01] - 2026-03-18

### Added
- Initial release with typed clients for Twitch OAuth2, Helix REST API, EventSub WebSocket, and IRC
- Kotlin Multiplatform support (JVM, JS, WasmJS)
- `Flow`-based cursor pagination for all paginated Helix endpoints
- EventSub WebSocket with automatic reconnect and keepalive management
- Pluggable `TokenProvider` and `TwitchKtLogger` interfaces
- `@RequiresScope` annotations and opt-in `ScopeProvider` for proactive scope validation
- Typed `TwitchApiException` hierarchy with rate-limit retry-after support
- Twitch CLI-compatible URL overrides for local mock server testing
- BOM artifact (`twitchkt-bom`) for version alignment
- Kermit logging bridge (`twitchkt-logging-kermit`)

[Unreleased]: https://github.com/CaptnBlubber/twitchkt/compare/v0.1.0-alpha01...HEAD
[0.1.0-alpha01]: https://github.com/CaptnBlubber/twitchkt/releases/tag/v0.1.0-alpha01
```

- [ ] **Step 2: Verify the file looks correct**

```bash
cat CHANGELOG.md
```

Expected: The file prints cleanly with `[Unreleased]` at the top and `[0.1.0-alpha01]` below it.

- [ ] **Step 3: Commit**

```bash
git add CHANGELOG.md
git commit -m "docs: add CHANGELOG.md with initial release entry"
```

---

### Task 2: Create publish workflow

**Files:**
- Create: `.github/workflows/publish.yml`

- [ ] **Step 1: Create `.github/workflows/publish.yml`**

```yaml
name: Publish

on:
  push:
    tags:
      - 'v*'

jobs:
  publish:
    runs-on: ubuntu-latest
    permissions:
      contents: write
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 21

      - uses: gradle/actions/setup-gradle@v4

      - name: Validate version
        run: |
          VERSION=$(grep "^VERSION_NAME=" gradle.properties | cut -d= -f2)
          TAG="${GITHUB_REF#refs/tags/v}"
          if [ "$VERSION" != "$TAG" ]; then
            echo "ERROR: VERSION_NAME in gradle.properties is '$VERSION' but tag is 'v$TAG'. Update gradle.properties before tagging."
            exit 1
          fi
          echo "VERSION=$VERSION" >> "$GITHUB_ENV"

      - name: Publish to Maven Central
        run: ./gradlew publishAllPublicationsToMavenCentralRepository
        env:
          ORG_GRADLE_PROJECT_mavenCentralUsername: ${{ secrets.MAVEN_CENTRAL_USERNAME }}
          ORG_GRADLE_PROJECT_mavenCentralPassword: ${{ secrets.MAVEN_CENTRAL_PASSWORD }}
          ORG_GRADLE_PROJECT_signingInMemoryKey: ${{ secrets.SIGNING_KEY }}
          ORG_GRADLE_PROJECT_signingInMemoryKeyId: ${{ secrets.SIGNING_KEY_ID }}
          ORG_GRADLE_PROJECT_signingInMemoryKeyPassword: ${{ secrets.SIGNING_PASSWORD }}

      - name: Extract changelog
        run: |
          python3 << 'PYEOF'
          import sys, os

          version = os.environ['VERSION']
          runner_temp = os.environ['RUNNER_TEMP']

          with open('CHANGELOG.md') as f:
              lines = f.read().splitlines()

          section_lines = []
          in_section = False

          for line in lines:
              if line.startswith('## [Unreleased]'):
                  continue
              if line.startswith('## ['):
                  if in_section:
                      break
                  expected = f'## [{version}]'
                  if not line.startswith(expected):
                      print(f"ERROR: Expected '{expected}' but first versioned section is '{line}'", file=sys.stderr)
                      sys.exit(1)
                  in_section = True
                  section_lines.append(line)
              elif in_section:
                  section_lines.append(line)

          notes = '\n'.join(section_lines).strip()
          if not notes:
              print(f"ERROR: No changelog content found for version {version}", file=sys.stderr)
              sys.exit(1)

          out = os.path.join(runner_temp, 'release-notes.md')
          with open(out, 'w') as f:
              f.write(notes + '\n')
          print(f"Extracted changelog for {version}")
          PYEOF

      - name: Create GitHub Release
        run: |
          PRERELEASE_FLAG=""
          if echo "$VERSION" | grep -qE 'alpha|beta|rc'; then
            PRERELEASE_FLAG="--prerelease"
          fi
          gh release create "v$VERSION" \
            --title "v$VERSION" \
            --notes-file "$RUNNER_TEMP/release-notes.md" \
            $PRERELEASE_FLAG
        env:
          GH_TOKEN: ${{ github.token }}
```

- [ ] **Step 2: Validate the YAML syntax**

```bash
python3 -c "import yaml, sys; yaml.safe_load(open('.github/workflows/publish.yml'))" && echo "YAML valid"
```

Expected: `YAML valid`

- [ ] **Step 3: Commit**

```bash
git add .github/workflows/publish.yml
git commit -m "ci: add tag-triggered Maven Central publish workflow"
```

---

### Task 3: Add SIGNING_KEY_ID secret and verify secrets

- [ ] **Step 1: Add the missing secret**

Go to `https://github.com/CaptnBlubber/twitchkt/settings/secrets/actions` and add:
- Name: `SIGNING_KEY_ID`
- Value: `8F3CEBC1`

- [ ] **Step 2: Confirm all 5 secrets are present**

```bash
gh secret list -R CaptnBlubber/twitchkt
```

Expected output includes all five:
```
MAVEN_CENTRAL_USERNAME
MAVEN_CENTRAL_PASSWORD
SIGNING_KEY
SIGNING_KEY_ID
SIGNING_PASSWORD
```

- [ ] **Step 3: Push and verify CI is green**

```bash
git push
```

Open `https://github.com/CaptnBlubber/twitchkt/actions` and confirm the CI workflow passes on `main`.

---

### Task 4: Do a test release of `0.1.0-alpha01`

This verifies the entire pipeline end-to-end.

- [ ] **Step 1: Confirm `VERSION_NAME` is `0.1.0-alpha01`**

```bash
grep VERSION_NAME gradle.properties
```

Expected: `VERSION_NAME=0.1.0-alpha01`

- [ ] **Step 2: Confirm `CHANGELOG.md` has a `[0.1.0-alpha01]` section**

```bash
grep "## \[0.1.0-alpha01\]" CHANGELOG.md
```

Expected: `## [0.1.0-alpha01] - 2026-03-18`

- [ ] **Step 3: Tag and push**

```bash
git tag v0.1.0-alpha01
git push --tags
```

- [ ] **Step 4: Watch the workflow**

```bash
gh run watch -R CaptnBlubber/twitchkt
```

Expected: All steps pass. Check:
- "Validate version" passes (tag matches `VERSION_NAME`)
- "Publish to Maven Central" passes (artifacts uploaded)
- "Extract changelog" passes (notes file written)
- "Create GitHub Release" passes (release created with `--prerelease`)

- [ ] **Step 5: Verify the GitHub Release**

```bash
gh release view v0.1.0-alpha01 -R CaptnBlubber/twitchkt
```

Expected: Release exists, marked as pre-release, body contains the `0.1.0-alpha01` changelog block.

- [ ] **Step 6: Verify on Maven Central**

Go to `https://central.sonatype.com/namespace/io.github.captnblubber` and confirm the 8 modules appear under version `0.1.0-alpha01`.

Note: Maven Central can take 10–30 minutes to index and make artifacts searchable after a successful publish.
