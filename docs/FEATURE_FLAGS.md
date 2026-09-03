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

Preferred — edit the template and deploy, so the repo stays the source of truth:

```bash
firebase deploy --only remoteconfig --project teamflow-manager-dev
```

```bash
firebase deploy --only remoteconfig --project teamflow-manager-897a3
```

Read back what a project currently has:

```bash
firebase remoteconfig:get --project teamflow-manager-dev
```

Flipping a value straight from the Firebase console (Remote Config → parameter → Publish) also
works and needs no release — but the repo template then no longer matches what is published, and
the next `firebase deploy --only remoteconfig` **replaces the whole template**, undoing it. Use the
console for a quick test, the template for anything that should stick.

No release is needed either way. The app picks the change up on the next fetch.

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
