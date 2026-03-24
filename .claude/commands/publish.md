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
5. Inform: "PR creada hacia main. El CI ejecutará tests y subirá la build a Play Beta. Cuando el CI sea verde, mergea la PR para promover a producción."
6. Provide the Actions URL: `https://github.com/jesuslcorominas/teamflowmanager/actions`

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
3. Push: `git push origin HEAD`
   - Hook bumps versionCode, pushes, and the CI re-runs on the open PR
4. Provide the Actions URL: `https://github.com/jesuslcorominas/teamflowmanager/actions`

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

## Notes:
- The pre-push hook at `scripts/hooks/pre-push` handles the versionCode bump automatically
- If the hook is not installed, remind the user to run `bash scripts/install-hooks.sh`
- After CI is green on the PR, merge it — post-release handles everything else automatically

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