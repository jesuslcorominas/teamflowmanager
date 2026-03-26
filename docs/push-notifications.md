# Push Notifications — FCM Infrastructure

This document describes the Firebase Cloud Messaging (FCM) push notification infrastructure implemented for TeamFlowManager.

## Architecture

```
Firebase Cloud Functions (trigger)
    ↓ sends FCM message to topic
FCM topic: club_{clubFirestoreId}
    ↓ delivered to subscribed devices
TeamFlowFirebaseMessagingService (Android)
    ↓ shows local notification
```

## Layers

| Layer | Class | Responsibility |
|---|---|---|
| Domain | `SyncFcmTokenUseCase` | Save device FCM token to Firestore |
| Domain | `DeleteFcmTokenUseCase` | Remove FCM token on sign-out |
| Domain | `SubscribeToClubNotificationsUseCase` | Subscribe to club topic |
| Domain | `UnsubscribeFromClubNotificationsUseCase` | Unsubscribe from club topic |
| Data (Android) | `FcmTokenProviderDataSourceImpl` | Gets token via `FirebaseMessaging.getInstance().token.await()` |
| Data (Android) | `FcmTokenFirestoreDataSourceImpl` | Writes token to `fcmTokens/{userId}_{last16ofToken}` |
| Data (Android) | `FcmNotificationTopicDataSourceImpl` | Calls `FirebaseMessaging.subscribeToTopic` / `unsubscribeFromTopic` |
| Data (iOS) | `IosFcmTokenDataSourceImpl` | No-op stub |
| Data (iOS) | `IosFcmTokenProviderDataSourceImpl` | No-op stub — returns empty string |
| Data (iOS) | `IosNotificationTopicDataSourceImpl` | No-op stub |
| App (Android) | `TeamFlowFirebaseMessagingService` | Extends `FirebaseMessagingService`, shows notifications via `NotificationChannel` |

## Firestore Schema

### Collection: `fcmTokens`

Document ID: `{userId}_{last16charsOfToken}`

```json
{
  "userId": "uid123",
  "token": "fcm_token_string",
  "platform": "android",
  "updatedAt": <Timestamp>
}
```

### FCM Topics

Topic name pattern: `club_{clubFirestoreId}`

Example: `club_abc123` for the club with Firestore ID `abc123`.

## When Tokens Are Synced

Token sync (`SyncFcmTokenUseCase`) should be called after successful login.
Token deletion (`DeleteFcmTokenUseCase`) should be called before sign-out.

Club topic subscription (`SubscribeToClubNotificationsUseCase`) should be called when the user joins or is assigned to a club.

> TODO: Wire use cases into LoginViewModel (after sign-in) and into the sign-out flow.

## Android Notification Channel

Channel ID: `push_notifications_channel`
Channel name: "Notificaciones"
Importance: `IMPORTANCE_DEFAULT`

The channel is created lazily in `TeamFlowFirebaseMessagingService.onMessageReceived`.

## Activation Steps (DEV project)

The following steps were performed on the DEV Firebase project (`teamflow-manager-dev`):

1. **FCM enabled** — Firebase Cloud Messaging is enabled by default on any Firebase project. No manual activation needed.
2. **Firestore rules** — the `fcmTokens` collection must allow writes from authenticated users:
   ```
   match /fcmTokens/{docId} {
     allow write: if request.auth != null && request.resource.data.userId == request.auth.uid;
     allow read: if false; // Cloud Functions read with admin SDK
   }
   ```
3. **Android manifest** — `TeamFlowFirebaseMessagingService` registered with `MESSAGING_EVENT` intent-filter.
4. **google-services.json** — already present for DEV flavor.

## iOS — Current Status

iOS APNs integration is **not yet implemented**. The three iOS datasources (`IosFcmTokenDataSourceImpl`, `IosFcmTokenProviderDataSourceImpl`, `IosNotificationTopicDataSourceImpl`) are no-op stubs.

Requirements to activate on iOS:
- Apple Developer account with APNs key or certificate
- Upload APNs key to Firebase Console → Project Settings → Cloud Messaging → Apple app configuration
- Add `firebase-messaging` pod in `iosApp/Podfile`
- Request notification authorization in app startup
- Register for remote notifications and pass token to `IosFcmTokenProviderDataSourceImpl`
- Replace stub implementations with real ones

## PROD Project

When ready to activate in PROD:
1. Ensure `fcmTokens` Firestore rules are deployed to prod project
2. Deploy Cloud Functions (see below) to `teamflow-manager-prod`
3. Replace DEV `google-services.json` with PROD one in prod flavor assets

## Cloud Functions (Pending)

Server-side notification sending is not yet implemented. Future Cloud Functions will:

- **`notifyClubOnMatchCreated`** — triggered by Firestore write to `matches/`, sends notification to `club_{clubFirestoreId}` topic
- **`notifyClubOnMatchResult`** — triggered when match status changes to FINISHED
- **`notifyPlayerOnSubstitution`** — triggered by write to `playerSubstitutions/`

Cloud Functions deployment is manual and requires:
```bash
cd functions
firebase deploy --only functions --project teamflow-manager-dev
```

> Note: Firebase CLI must be authenticated and the project must have Blaze (pay-as-you-go) plan to deploy Cloud Functions.
