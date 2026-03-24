Guide the user through the release publish flow for TeamFlowManager.

Start by running `git branch --show-current` to determine the current branch, and `git branch | grep "release/"` to find existing release branches.

## If on `develop`:

**Case A — No release branches exist:**
1. Ask: "¿Qué versión quieres publicar? (ej: 0.3.1)"
2. Once confirmed, run:
   - `git checkout -b release/{version}`
3. Tell the user: "Rama release/{version} creada. Cuando hagas push, el hook bumpeará el versionCode automáticamente y se disparará el workflow de CI."
4. Ask if they want to push now. If yes: `git push -u origin release/{version}`

**Case B — One or more release branches exist:**
1. Show the list of existing release branches numbered
2. Ask: "¿Quieres continuar en una de estas ramas o crear una nueva?"
3. If switching to existing: `git checkout release/{chosen}`
4. If creating new: ask for version name, then `git checkout -b release/{version}` from develop
5. After switching/creating, ask if they want to push now

## If on a `release/*` branch:

1. Run `git status --short` to check for uncommitted changes
2. If there are uncommitted changes:
   - Run `git diff --stat` to show what changed
   - Ask for a commit message (suggest "chore: release prep" if no clear message)
   - Run `git add -A && git commit -m "{message}"`
3. Push: `git push origin HEAD`
4. Inform: "Push realizado. El hook ha bumpeado el versionCode y el workflow de CI se está ejecutando."
5. Provide the GitHub Actions URL: `https://github.com/jesuslcorominas/teamflowmanager/actions`

## If on `main` or any other branch:

Warn: "Para publicar debes estar en `develop` o en una rama `release/*`. ¿Quieres cambiar a develop?"
If yes: `git checkout develop`
Then restart the flow from the beginning.

## Notes:
- The pre-push hook at `scripts/hooks/pre-push` handles the versionCode bump automatically
- If the hook is not installed, remind the user to run `bash scripts/install-hooks.sh`
- The CI workflow at `.github/workflows/release.yml` handles tests, build, Play Store upload and IPA generation
- After the CI runs successfully, the user should test on beta and then manually promote to production in Play Console
- Once promoted to production: merge `release/{version}` into `main` and `develop`

## iOS App Store — Current status & future work

**Current state**: iOS CI builds an ad-hoc IPA and attaches it as a GitHub Actions artifact (30 days retention). There is NO automatic upload to App Store Connect yet.

**Check if Apple secrets are configured** by running:
```
gh secret list --repo jesuslcorominas/teamflowmanager | grep APPLE
```

- If `APPLE_CERTIFICATE_BASE64`, `APPLE_CERTIFICATE_PASSWORD`, `APPLE_PROVISIONING_PROFILE_BASE64` are present → the IPA is already being built. Remind the user that App Store Connect upload is still manual.
- If those secrets are missing → inform the user that iOS CI is not yet fully configured and remind them to add those secrets when ready.

**When iOS App Store upload is implemented**, the following restrictions should mirror what is done for Android (Play Store):
1. **Version validation**: Check that the build number has not already been uploaded to App Store Connect (use the App Store Connect API `/apps/{id}/builds` or `altool`).
2. **Old TestFlight cleanup**: Expire previous TestFlight builds if needed (App Store Connect API).
3. **Upload**: Use `xcrun altool --upload-app` or `fastlane deliver` / `fastlane pilot` targeting TestFlight.
4. **Skill update**: Add a check here for the iOS track status similar to the Play Store beta check.

Secrets that will be needed (not yet configured):
- `APPLE_CERTIFICATE_BASE64` — distribution certificate (.p12, base64)
- `APPLE_CERTIFICATE_PASSWORD` — password for the .p12
- `APPLE_PROVISIONING_PROFILE_BASE64` — distribution provisioning profile (.mobileprovision, base64)
- `APP_STORE_CONNECT_API_KEY_ID` — App Store Connect API key ID
- `APP_STORE_CONNECT_API_ISSUER_ID` — App Store Connect API issuer ID
- `APP_STORE_CONNECT_API_KEY_BASE64` — App Store Connect API private key (.p8, base64)