Guide the user through the release publish flow for TeamFlowManager.

Start by running `git branch --show-current` to determine the current branch, and `git branch | grep "release/"` to find existing release branches.

## If on `develop`:

1. Ask: "¿Qué versión quieres publicar? (ej: 0.3.1)"
2. Ask: "¿Tienes release notes para esta versión? (opcional — si no, se usará un mensaje genérico)"
   - If provided: write the text to `distribution/whatsnew/whatsnew-es-ES`
   - If not provided: write "Corrección de errores y mejoras de rendimiento." to `distribution/whatsnew/whatsnew-es-ES`
   - Commit: `git add distribution/whatsnew/ && git commit -m "chore: release notes for v{version}"`
3. Create the release branch: `git checkout -b release/{version}`
4. Push: `git push -u origin release/{version}`
   - The pre-push hook will bump versionCode automatically and push
   - GitHub Actions will open a PR to `main` automatically
5. Inform: "PR creada hacia main. Monitorizando la CI..."
6. Start CI monitoring loop (see ## CI Monitoring below)

**Note on screenshots**: Play Store screenshots require subida manual desde Play Console por ahora.
Screenshots support via API es trabajo futuro (ver sección iOS App Store más abajo).

**Case — release branches already exist:**
1. Show the list of existing release branches numbered
2. Ask: "¿Quieres continuar en una de estas ramas o crear una nueva?"
3. If switching to existing: `git checkout release/{chosen}` then go to "If on a release/* branch"
4. If creating new: proceed from step 1 above

## If on a `release/*` branch:

1. Run `git status --short` to check for uncommitted changes
2. If there are uncommitted changes:
   - Show `git diff --stat`
   - Ask for a commit message
   - `git add -A && git commit -m "{message}"`
3. Commit & push: `git push origin HEAD`
   - Hook bumps versionCode, pushes, and the CI re-runs on the open PR
   - To skip Tests & Lint (e.g. when tests already passed locally), include `[skip-tests]` in the commit message: `git commit -m "fix: whatever [skip-tests]"`
4. Start CI monitoring loop (see ## CI Monitoring below)

## If on `main` or any other branch:

Warn: "Para publicar debes estar en `develop` o en una rama `release/*`. ¿Quieres cambiar a develop?"
If yes: `git checkout develop`
Then restart the flow from the beginning.

## Full release flow (reference)

```
push release/* → hook bumpa versionCode
              → CI abre PR a main automáticamente
              → PR dispara: tests + validate versionCode + AAB a Play Beta + IPA
merge PR a main → promueve Play Beta → producción
               → crea GitHub Release con las release notes
               → sincroniza main → develop (merge directo o PR si hay conflictos)
```

## CI Monitoring

After every push to a release branch, enter a monitoring loop:

1. Run `gh run list --branch {current-branch} --limit 1 --json status,conclusion,databaseId,name` to get the latest run
2. If `status == "in_progress"` or `status == "queued"` → wait and check again in 10 minutes
3. If `status == "completed"` and `conclusion == "success"` → inform the user:
   "✅ CI verde. La build está en Play Beta. Mergea la PR cuando quieras para promover a producción."
   Then stop monitoring.
4. If `status == "completed"` and `conclusion == "failure"` → automatically:
   a. Run `gh run view {databaseId} --log-failed 2>&1 | head -100` to read the failure logs
   b. Identify the root cause
   c. Fix the relevant file(s)
   d. Commit and push the fix — this will re-trigger the CI
   e. Inform the user what was fixed
   f. Continue monitoring the new run from step 1
5. If the same error repeats after a fix attempt → inform the user and ask for input before retrying

Do NOT stop monitoring until CI is green or the user explicitly says to stop.

## Notes:
- The pre-push hook at `scripts/hooks/pre-push` handles the versionCode bump automatically
- If the hook is not installed, remind the user to run `bash scripts/install-hooks.sh`
- After CI is green on the PR, merge it — post-release handles everything else automatically

## ⚠️ Pre-push hook behavior — DO NOT retry push manually

The pre-push hook:
1. Bumps versionCode in `app/build.gradle.kts` and `iosApp/iosApp.xcodeproj/project.pbxproj`
2. Commits the bump
3. Pushes the bump commit itself
4. Exits with code 1 — which causes `git push` to report an error

**This exit-1 is intentional and expected.** The remote already has the bump commit after step 3.
After the hook exits, `git status` will show "Your branch is up to date with origin" — the push succeeded via the hook.

**Never retry `git push` or `git pull && git push` after a hook exit-1.** Each retry triggers another full hook cycle: another bump, another commit, another CI run. This wastes CI minutes and creates spurious version increments (e.g. builds 17→18→19 from a single logical push).

## ⚠️ Borrar ramas remotas desde una rama release/* — usar VERSION_BUMP_IN_PROGRESS

El pre-push hook se dispara con **cualquier** `git push` ejecutado mientras estás en una rama `release/*`, incluyendo `git push origin --delete <rama>`. Esto provoca un bump de versionCode inesperado.

**Siempre usar el env var de guardia para borrar ramas remotas:**

```bash
VERSION_BUMP_IN_PROGRESS=1 git push origin --delete <rama>
```

**`VERSION_BUMP_IN_PROGRESS=1` es SOLO para `git push --delete`.** Nunca usarlo en pushes normales de código o de ficheros CI (`.github/workflows/`), aunque el cambio parezca trivial. Cualquier push que dispare Release CI necesita un versionCode único. Si el cambio no debe disparar CI, añádelo al `paths-ignore` del workflow — no lo evites suprimiendo el hook.

## iOS App Store — Current status & future work

**Current state**: iOS CI builds an ad-hoc IPA and attaches it as a GitHub Actions artifact (30 days retention). There is NO automatic upload to App Store Connect yet.

**Check if Apple secrets are configured** by running:
```
gh secret list --repo jesuslcorominas/teamflowmanager | grep APPLE
```

- If `APPLE_CERTIFICATE_BASE64`, `APPLE_CERTIFICATE_PASSWORD`, `APPLE_PROVISIONING_PROFILE_BASE64` are present → the IPA is already being built. App Store Connect upload is still manual.
- If those secrets are missing → iOS CI is not fully configured yet.

**When iOS App Store upload is implemented**, mirror the Android flow:
1. Validate build number against App Store Connect API
2. Upload to TestFlight via `xcrun altool` or `fastlane pilot`
3. On merge to main: promote TestFlight → App Store production

Secrets still needed:
- `APPLE_CERTIFICATE_BASE64`, `APPLE_CERTIFICATE_PASSWORD`, `APPLE_PROVISIONING_PROFILE_BASE64`
- `APP_STORE_CONNECT_API_KEY_ID`, `APP_STORE_CONNECT_API_ISSUER_ID`, `APP_STORE_CONNECT_API_KEY_BASE64`