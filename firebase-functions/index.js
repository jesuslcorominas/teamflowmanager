/**
 * Firebase Cloud Functions for Team Flow Manager
 * 
 * Provides short link generation and redirection for team invitations.
 * This replaces deprecated Firebase Dynamic Links with a custom solution
 * using Firebase Hosting + Cloud Functions.
 */

const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

/**
 * Generate a short link for team invitation.
 * 
 * This function stores the team invitation data in Firestore and returns
 * a short link that can be shared via WhatsApp, Email, SMS, etc.
 * 
 * POST /api/createShortLink
 * Body: { teamId: string, teamName: string }
 * Response: { shortLink: string, linkId: string }
 */
exports.createShortLink = functions.https.onRequest(async (req, res) => {
  // Enable CORS for your domain
  res.set('Access-Control-Allow-Origin', '*');
  res.set('Access-Control-Allow-Methods', 'POST, OPTIONS');
  res.set('Access-Control-Allow-Headers', 'Content-Type');

  // Handle preflight
  if (req.method === 'OPTIONS') {
    res.status(204).send('');
    return;
  }

  if (req.method !== 'POST') {
    res.status(405).send('Method Not Allowed');
    return;
  }

  try {
    const { teamId, teamName } = req.body;

    if (!teamId || !teamName) {
      res.status(400).json({ error: 'teamId and teamName are required' });
      return;
    }

    // Generate a unique short ID (6 characters, URL-safe)
    const shortId = generateShortId();

    // Store link data in Firestore
    await admin.firestore().collection('shortLinks').doc(shortId).set({
      teamId,
      teamName,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      clicks: 0,
    });

    // Return the short link
    const shortLink = `https://teamflowmanager.web.app/l/${shortId}`;
    
    res.json({ 
      shortLink,
      linkId: shortId
    });

  } catch (error) {
    console.error('Error creating short link:', error);
    res.status(500).json({ error: 'Failed to create short link' });
  }
});

/**
 * Redirect short link to app or Play Store.
 * 
 * This function handles the redirection logic:
 * - On Android: Opens app with deep link
 * - On other platforms or if app not installed: Redirects to Play Store
 * 
 * GET /l/:linkId
 */
exports.redirectShortLink = functions.https.onRequest(async (req, res) => {
  try {
    // Extract short ID from path (/l/abc123 -> abc123)
    const pathParts = req.path.split('/');
    const linkId = pathParts[pathParts.length - 1];

    if (!linkId) {
      res.status(404).send('Link not found');
      return;
    }

    // Retrieve link data from Firestore
    const linkDoc = await admin.firestore().collection('shortLinks').doc(linkId).get();

    if (!linkDoc.exists) {
      res.status(404).send('Link not found');
      return;
    }

    const linkData = linkDoc.data();
    const { teamId, teamName } = linkData;

    // Increment click counter
    await linkDoc.ref.update({
      clicks: admin.firestore.FieldValue.increment(1),
      lastClickedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    // Build deep link
    const deepLink = `teamflowmanager://team/accept?teamId=${encodeURIComponent(teamId)}&teamName=${encodeURIComponent(teamName)}`;
    const playStoreLink = 'https://play.google.com/store/apps/details?id=com.jesuslcorominas.teamflowmanager';

    // Detect user agent
    const userAgent = req.get('user-agent') || '';
    const isAndroid = /android/i.test(userAgent);

    // Serve an HTML page that attempts app launch and falls back to Play Store
    res.send(`
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Únete a ${escapeHtml(teamName)} - Team Flow Manager</title>
  <meta name="description" content="Has sido invitado a ser el entrenador de ${escapeHtml(teamName)}">
  
  <!-- Open Graph meta tags for rich sharing -->
  <meta property="og:title" content="Únete a ${escapeHtml(teamName)}">
  <meta property="og:description" content="Has sido invitado a ser el entrenador de ${escapeHtml(teamName)} en Team Flow Manager">
  <meta property="og:type" content="website">
  
  <style>
    body {
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      margin: 0;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      text-align: center;
      padding: 20px;
    }
    .container {
      max-width: 400px;
    }
    h1 {
      font-size: 24px;
      margin-bottom: 10px;
    }
    p {
      font-size: 16px;
      opacity: 0.9;
      margin-bottom: 30px;
    }
    .button {
      display: inline-block;
      padding: 12px 30px;
      background: white;
      color: #667eea;
      text-decoration: none;
      border-radius: 25px;
      font-weight: 600;
      transition: transform 0.2s;
    }
    .button:hover {
      transform: scale(1.05);
    }
    .loading {
      font-size: 14px;
      opacity: 0.8;
      margin-top: 20px;
    }
  </style>
</head>
<body>
  <div class="container">
    <h1>🏐 Únete a ${escapeHtml(teamName)}</h1>
    <p>Has sido invitado a ser el entrenador de este equipo en Team Flow Manager</p>
    <a href="${playStoreLink}" class="button" id="fallbackButton">Abrir en Play Store</a>
    <p class="loading" id="loadingText">Intentando abrir la aplicación...</p>
  </div>

  <script>
    // Try to open the app with deep link
    const deepLink = ${JSON.stringify(deepLink)};
    const playStore = ${JSON.stringify(playStoreLink)};
    
    // Attempt app launch
    window.location.href = deepLink;
    
    // If still on page after 2 seconds, app is not installed
    setTimeout(() => {
      document.getElementById('loadingText').textContent = 'La aplicación no está instalada. Haz clic para descargarla.';
    }, 2000);
    
    // Also set a timeout to redirect to Play Store
    setTimeout(() => {
      window.location.href = playStore;
    }, 3000);
  </script>
</body>
</html>
    `);

  } catch (error) {
    console.error('Error redirecting short link:', error);
    res.status(500).send('Error processing link');
  }
});

/**
 * Generate a short, URL-safe ID.
 * Uses base62 encoding (a-z, A-Z, 0-9) for readability.
 */
function generateShortId() {
  const chars = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
  let result = '';
  for (let i = 0; i < 6; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

/**
 * Escape HTML to prevent XSS
 */
function escapeHtml(unsafe) {
  return unsafe
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}
