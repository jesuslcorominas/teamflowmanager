# Feature flags

Remotely controlled switches, so a feature can be turned on or off without shipping a new build.

## Architecture

```
domain/config/FeatureFlags.kt            interface — the only thing the rest of the app sees
app/config/RemoteConfigFeatureFlags.kt   Android impl, backed by Firebase Remote Config
di/iosMain/IosModule.kt                  iOS impl (StaticFeatureFlags) — compile-time values
```

`FeatureFlags` is injected like any other dependency. ViewModels expose the value they need as
plain state; composables never read the flag directly:

```kotlin
class PlayerWizardViewModel(
    // …
    private val featureFlags: FeatureFlags,
) : ViewModel() {
    val isImageUploadEnabled: Boolean
        get() = featureFlags.isPlayerImageUploadEnabled
}
```

## Current flags

| Key | Default | Effect when off |
|---|---|---|
| `player_image_upload_enabled` | `false` | The player wizard shows the avatar read-only: no tap target, no camera/gallery dialog, no "tap to add photo" hint. Existing photos are still displayed. |

## Where the flags live

One template per environment, both versioned in the repo:

| File | Project | Published on |
|---|---|---|
| [`remoteconfig/dev.template.json`](../remoteconfig/dev.template.json) | `teamflow-manager-dev` | push to `develop` |
| [`remoteconfig/prod.template.json`](../remoteconfig/prod.template.json) | `teamflow-manager-897a3` | push to `main` |

**Both files exist on both branches** and sync through git like any other file — what differs is
which one each branch publishes. That is the point: a flag can be `true` in dev while it is still
`false` in prod, without the two branches ever diverging.

Keeping a single file per branch instead (letting `main` and `develop` hold different content for
the same path) would fight git: every `main → develop` sync would conflict, forever, and the
"resolution" would always be to discard one side. Two files, one per environment, is the shape that
works with git rather than against it.

`firebase.json` points at the dev template, so a plain `firebase deploy --only remoteconfig` from a
laptop targets dev. `firebase.prod.json` is a minimal config that points at the prod template.

## Changing a flag

Edit the template for the environment you are targeting and open a PR. **Merging publishes it** —
the `Remote Config` workflow (`.github/workflows/remoteconfig.yml`) deploys the template belonging
to the branch that was pushed.

**This is independent of releasing.** A PR to `main` that touches *only* the templates skips the
build and the Play upload — the `scope` job in `release.yml` detects it and short-circuits the
pipeline, while still reporting the required `Tests & Lint` check — so production flags can be
flipped at any time without shipping a version. The point of going through a PR is the history:
who changed which flag, when, and why.

To turn a flag on in **dev**: edit `remoteconfig/dev.template.json`, PR to `develop`, merge.

To turn it on in **prod**: branch off `main`, edit `remoteconfig/prod.template.json`, PR **against
`main`**, merge. `Post-Release` then opens the usual `main → develop` sync PR so both branches keep
both files; merging that sync does **not** redeploy dev, because the workflow only deploys when the
template for *that* environment changed in the push.

A PR touching the templates also runs a parity check: both files must declare the same parameter
keys (values may differ). It catches a flag added to one environment and forgotten in the other,
which would otherwise only show up as a missing flag at runtime.

Read back what a project currently has:

```bash
firebase remoteconfig:get --project teamflow-manager-dev
```

Deploying by hand is still possible as an escape hatch:

```bash
firebase deploy --only remoteconfig --project teamflow-manager-dev
```

```bash
firebase deploy --only remoteconfig --config firebase.prod.json --project teamflow-manager-897a3
```

Flipping a value straight from the Firebase console (Remote Config → parameter → Publish) also
works and needs no release — but the repo template then no longer matches what is published, and
the next deploy, **whether run by CI or by hand, replaces the whole template** and undoes it. Use
the console for a quick test, the template for anything that should stick.

No release is needed either way. The app picks the change up on the next fetch.

### CI credentials

The workflow authenticates with a Google service account per project, following the same pattern as
the Play Store deploy in `release.yml`:

| Secret | Project |
|---|---|
| `FIREBASE_SERVICE_ACCOUNT_DEV` | `teamflow-manager-dev` |
| `FIREBASE_SERVICE_ACCOUNT_PROD` | `teamflow-manager-897a3` |

Each holds the full JSON key of a service account with the **Firebase Remote Config Admin**
(`roles/firebaseremoteconfig.admin`) role on that project — no broader role is needed. Create them
in Google Cloud console → IAM & Admin → Service Accounts → Keys, and paste each JSON into the
matching GitHub repository secret. Without the secret the workflow fails with an explicit message
rather than silently skipping, so a template change is never merged believing it was published.

## Propagation timing

Values are read synchronously from the last activated snapshot, and a fetch is fired once when
`RemoteConfigFeatureFlags` is constructed. In practice a published change lands on the **next app
launch**, not instantly:

- Minimum fetch interval: 1 hour in release builds, 0 in debug builds (so a debug build picks up a
  change on every launch — useful for testing).
- Before the first successful fetch, and whenever the device is offline, the defaults in
  `RemoteConfigFeatureFlags.defaults` apply. A flag that is off by default stays off on a cold
  start with no network.

## Adding a new flag

1. Add the property to the `FeatureFlags` interface, documenting the default and what "off" means.
2. Add the key to `RemoteConfigFeatureFlags.Keys` and its default to `defaults`.
3. Give the iOS `StaticFeatureFlags` a value (iOS has no Remote Config binding yet).
4. Expose it through the ViewModel that owns the screen, not by reading Koin from a composable.
5. Add the parameter to **both** `remoteconfig/dev.template.json` and
   `remoteconfig/prod.template.json` (the parity check enforces this); the values may differ.

## Scope

The switch hides the entry points in the UI. The upload code path
(`ImageStorageDataSource` → `FirebaseStorageDataSourceImpl`, and `uploadPlayerImageIfNeeded` in
`PlayerFirestoreDataSourceImpl`) is untouched and still works: with no picker, no local URI ever
reaches it. If a flag ever needs to be a hard guarantee rather than a UI affordance, gate the data
layer too.
