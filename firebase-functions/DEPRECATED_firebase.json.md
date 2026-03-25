# Firebase Functions Configuration (DEPRECATED)

**NOTE**: This file has been replaced by the root-level `firebase.json` in the project root.

The root `firebase.json` now contains the complete Firebase project configuration including:
- Firestore rules (`firestore.rules`)
- Cloud Functions (source: `firebase-functions/`)
- Hosting (public: `firebase-functions/public/`)

This file is kept for reference only but is no longer used by Firebase CLI.

## Original Configuration (for reference)

```json
{
  "functions": {
    "source": ".",
    "runtime": "nodejs18"
  },
  "hosting": {
    "public": "public",
    "ignore": [
      "firebase.json",
      "**/.*",
      "**/node_modules/**"
    ],
    "rewrites": [
      {
        "source": "/l/**",
        "function": "redirectShortLink"
      },
      {
        "source": "/api/createShortLink",
        "function": "createShortLink"
      }
    ]
  }
}
```

## Migration Notes

The hosting and functions configuration has been moved to the root `../firebase.json` with:
- Functions source path updated to `firebase-functions`
- Hosting public path updated to `firebase-functions/public`
- All rewrites preserved
- Firestore rules configuration added

All Firebase CLI commands should now be run from the project root directory.
