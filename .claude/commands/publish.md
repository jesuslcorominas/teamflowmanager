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