# Firebase Cloud Functions + Hosting
## Team Flow Manager Short Links

This directory contains Firebase Cloud Functions and Hosting configuration for generating and managing short links for team invitations.

## What's This?

This replaces the deprecated **Firebase Dynamic Links** with a custom solution using:
- **Firebase Cloud Functions** (Node.js 18)
- **Firebase Hosting**  
- **Firestore** for link storage

## Files

- **`index.js`** - Cloud Functions code (createShortLink, redirectShortLink)
- **`package.json`** - Node.js dependencies
- **`firebase.json`** - Firebase configuration (rewrites, hosting)
- **`public/index.html`** - Landing page

## Quick Start

### 1. Install Dependencies

```bash
npm install
```

### 2. Deploy

```bash
firebase deploy --only functions,hosting
```

### 3. Test

Create a short link:
```bash
curl -X POST https://teamflowmanager.web.app/api/createShortLink \
  -H "Content-Type: application/json" \
  -d '{"teamId":"test123","teamName":"Test Team"}'
```

## How It Works

1. **Android app** calls `/api/createShortLink` with team info
2. **Cloud Function** generates unique 6-char ID
3. **Stores** in Firestore: `shortLinks/{id}`
4. **Returns** short link: `https://teamflowmanager.web.app/l/{id}`
5. **User clicks** link → Hosting rewrites to `redirectShortLink` function
6. **Function** serves HTML page that:
   - Attempts to open app with deep link
   - Falls back to Play Store after 3 seconds

## Local Development

```bash
firebase emulators:start --only functions,hosting
```

Then test locally:
- http://localhost:5000 (Hosting)
- http://localhost:5001/teamflowmanager/us-central1/createShortLink (Functions)

## Documentation

See **[FIREBASE_HOSTING_SETUP.md](../FIREBASE_HOSTING_SETUP.md)** for complete setup guide.

## Costs

Using Firebase **free tier**:
- ✅ 2M function invocations/month
- ✅ 10 GB hosting transfer/month
- ✅ 50K Firestore reads/day

Expected: **$0/month** for typical usage.
