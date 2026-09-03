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

The Remote Config template is versioned in the repo as
[`remoteconfig.template.json`](../remoteconfig.template.json) and wired through `firebase.json`, so
a flag and its default are reviewable in a PR instead of only existing in a console.

The two flavors are **separate Firebase projects**, so every flag has to exist in both:

| Flavor | Project |
|---|---|
| dev | `teamflow-manager-dev` |
| prod | `teamflow-manager-897a3` |

## Changing a flag

Edit `remoteconfig.template.json` and open a PR. **Merging publishes it** — the `Remote Config`
workflow (`.github/workflows/remoteconfig.yml`) deploys on any push that touches the template:

| Branch | Project | When it lands |
|---|---|---|
| `develop` | `teamflow-manager-dev` | as soon as the PR is merged |
| `main` | `teamflow-manager-897a3` | with the release, at the same moment as the app version that uses it |

So a flag can never be "merged but not live", and prod only changes when a release does. The
workflow can also be run by hand from the Actions tab (`workflow_dispatch`) to re-publish the
template as-is.

Read back what a project currently has:

```bash
firebase remoteconfig:get --project teamflow-manager-dev
```

Deploying by hand is still possible as an escape hatch:

```bash
firebase deploy --only remoteconfig --project teamflow-manager-dev
```

Flipping a value straight from the Firebase console (Remote Config → parameter → Publish) also
works and needs no release — but the repo template then no longer matches what is published, and
the next deploy, **whether run by CI or by hand, replaces the whole template** and undoes it. Use
the console for a quick test, the template for anything that should stick.

No release is needed for a dev change. The app picks it up on the next fetch.

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
5. Add the parameter to `remoteconfig.template.json` and deploy it to **both** projects.

## Scope

The switch hides the entry points in the UI. The upload code path
(`ImageStorageDataSource` → `FirebaseStorageDataSourceImpl`, and `uploadPlayerImageIfNeeded` in
`PlayerFirestoreDataSourceImpl`) is untouched and still works: with no picker, no local URI ever
reaches it. If a flag ever needs to be a hard guarantee rather than a UI affordance, gate the data
layer too.
