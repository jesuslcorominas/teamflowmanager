/**
 * Account deletion for Team Flow Manager.
 *
 * Google Play requires an in-app route to delete the account and every piece of
 * data attached to it. The cascade runs here, with the Admin SDK, rather than in
 * the client: see ../docs/ACCOUNT_DELETION.md for why, and for the ownership
 * policy this file implements.
 *
 * POST /api/deleteAccount
 * Header: Authorization: Bearer <firebase id token>
 * Response: { deleted: true, summary: {...} }
 */

const { onRequest } = require('firebase-functions/v2/https');
const admin = require('firebase-admin');

// Firestore caps a batch at 500 writes. Stay under it with room to spare so a
// chunk that grows by a document or two cannot push a batch over the limit.
const BATCH_LIMIT = 400;

const PRESIDENT_ROLE = 'Presidente';

/**
 * Delete the caller's account and all data that belongs to it.
 *
 * The whole cascade runs in a single invocation: a client that is killed
 * mid-way would otherwise leave orphaned documents behind with nothing to
 * resume them. Nothing is reported as deleted unless the cascade finished,
 * so the app never tells the user their data is gone while it is not.
 */
exports.deleteAccount = onRequest({ timeoutSeconds: 540, memory: '512MiB' }, async (req, res) => {
  res.set('Access-Control-Allow-Origin', '*');
  res.set('Access-Control-Allow-Methods', 'POST, OPTIONS');
  res.set('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    return res.status(204).send('');
  }

  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method Not Allowed' });
  }

  let uid;
  try {
    uid = await authenticate(req);
  } catch (error) {
    console.warn('[deleteAccount] Rejected unauthenticated request:', error.message);
    return res.status(401).json({ error: 'Unauthorized' });
  }

  try {
    const summary = await deleteAccountCascade(uid);
    console.log('[deleteAccount] Deleted account', uid, summary);
    return res.status(200).json({ deleted: true, summary });
  } catch (error) {
    console.error('[deleteAccount] Cascade failed for', uid, error);
    return res.status(500).json({ error: 'Failed to delete account' });
  }
});

/**
 * Resolve the caller from the bearer token.
 *
 * The uid comes from the verified token and never from the request body: taking
 * it from the body would let any caller delete anyone's account.
 */
async function authenticate(req) {
  const header = req.get('Authorization') || '';
  const match = header.match(/^Bearer (.+)$/);
  if (!match) {
    throw new Error('Missing bearer token');
  }
  const decoded = await admin.auth().verifyIdToken(match[1]);
  return decoded.uid;
}

/**
 * The cascade itself, exported for testing and for the scheduled cleanup of
 * deletion requests received through the web form.
 */
async function deleteAccountCascade(uid) {
  const db = admin.firestore();

  const userEmail = await resolveUserEmail(uid);

  // ── Decide what happens to each club the user belongs to ──────────────────
  const memberships = await db.collection('clubMembers').where('userId', '==', uid).get();
  const ownedClubs = await db.collection('clubs').where('ownerId', '==', uid).get();

  // Owned clubs are queried separately rather than derived from the memberships:
  // a club whose membership row went missing would otherwise be skipped here and
  // left orphaned with an owner that no longer exists.
  const clubIds = [
    ...new Set([
      ...memberships.docs.map((doc) => doc.data().clubId),
      ...ownedClubs.docs.map((doc) => doc.id),
    ].filter(Boolean)),
  ];

  const clubsToDelete = [];
  const clubsTransferred = [];

  for (const clubId of clubIds) {
    const clubSnapshot = await db.collection('clubs').doc(clubId).get();
    if (!clubSnapshot.exists) continue;

    if (clubSnapshot.data().ownerId !== uid) {
      // Somebody else owns the club: it is their data, and losing one member
      // must not destroy it.
      continue;
    }

    const successor = await findSuccessor(db, clubId, uid);
    if (successor) {
      await transferClubOwnership(db, clubId, successor);
      clubsTransferred.push({ clubId, newOwnerId: successor.data().userId });
    } else {
      // The owner is the only member left, so nothing survives them.
      clubsToDelete.push(clubId);
    }
  }

  const deletedClubIds = new Set(clubsToDelete);

  // ── Decide what happens to each team the user coaches ─────────────────────
  const coachedTeams = await db.collection('teams').where('assignedCoachId', '==', uid).get();

  const teamsToDelete = [];
  const teamsReleased = [];

  for (const teamDoc of coachedTeams.docs) {
    const clubId = teamDoc.data().clubId;
    if (clubId && !deletedClubIds.has(clubId)) {
      // The club outlives the user and remains the controller of the team's
      // data. Hand the team back unassigned so a president can reassign it.
      teamsReleased.push(teamDoc.id);
    } else {
      // A team with no club, or one whose club is going away with the user, has
      // nobody left to own it.
      teamsToDelete.push(teamDoc.id);
    }
  }

  // Teams belonging to a club that is being deleted, but coached by somebody
  // else, go with the club.
  for (const clubId of clubsToDelete) {
    const clubTeams = await db.collection('teams').where('clubId', '==', clubId).get();
    for (const teamDoc of clubTeams.docs) {
      if (!teamsToDelete.includes(teamDoc.id)) {
        teamsToDelete.push(teamDoc.id);
      }
    }
  }

  // ── Execute ───────────────────────────────────────────────────────────────
  for (const teamId of teamsReleased) {
    await releaseTeam(db, teamId, uid, userEmail);
  }

  let deletedPlayers = 0;
  let deletedMatches = 0;
  for (const teamId of teamsToDelete) {
    const counts = await deleteTeamCascade(db, teamId);
    deletedPlayers += counts.players;
    deletedMatches += counts.matches;
  }

  for (const clubId of clubsToDelete) {
    await deleteClubRemains(db, clubId);
  }

  // The user's own footprint goes unconditionally, whatever happened above.
  await deleteDocs(db, memberships.docs);
  for (const clubId of clubIds) {
    if (deletedClubIds.has(clubId)) continue;
    await db.collection('clubs').doc(clubId).collection('notificationPreferences').doc(uid)
      .delete()
      .catch(() => {});
  }

  const tokens = await db.collection('fcmTokens').where('userId', '==', uid).get();
  await deleteDocs(db, tokens.docs);

  await db.recursiveDelete(db.collection('users').doc(uid));

  // Anything the app wrote under the user's own Storage prefix. Player photos
  // are removed with their players, in deleteTeamCascade.
  await deleteStoragePrefix(`users/${uid}/`);

  // Last, because everything above authenticates as this user: once the auth
  // record is gone there is no way back to retry a failed step.
  await admin.auth().deleteUser(uid);

  return {
    clubsDeleted: clubsToDelete.length,
    clubsTransferred,
    teamsDeleted: teamsToDelete.length,
    teamsReleased: teamsReleased.length,
    playersDeleted: deletedPlayers,
    matchesDeleted: deletedMatches,
    fcmTokensDeleted: tokens.size,
  };
}

async function resolveUserEmail(uid) {
  try {
    const record = await admin.auth().getUser(uid);
    return record.email || null;
  } catch (error) {
    return null;
  }
}

/**
 * Pick who inherits a club: a sitting president first, otherwise any remaining
 * member, who is promoted. An orphaned club with no owner can never be
 * administered again, so somebody has to take it.
 */
async function findSuccessor(db, clubId, uid) {
  const members = await db.collection('clubMembers').where('clubId', '==', clubId).get();
  const others = members.docs.filter((doc) => doc.data().userId !== uid);
  if (others.length === 0) return null;

  return (
    others.find((doc) => (doc.data().roles || []).includes(PRESIDENT_ROLE)) || others[0]
  );
}

async function transferClubOwnership(db, clubId, successor) {
  const roles = successor.data().roles || [];
  const batch = db.batch();

  batch.update(db.collection('clubs').doc(clubId), {
    ownerId: successor.data().userId,
  });

  if (!roles.includes(PRESIDENT_ROLE)) {
    batch.update(successor.ref, { roles: [...roles, PRESIDENT_ROLE] });
  }

  await batch.commit();
}

/**
 * Detach the leaving coach from a team that survives, without touching the
 * team's players, matches or history: those belong to the club.
 */
async function releaseTeam(db, teamId, uid, userEmail) {
  const update = { assignedCoachId: null };

  const teamDoc = await db.collection('teams').doc(teamId).get();
  if (teamDoc.exists && userEmail && teamDoc.data().pendingCoachEmail === userEmail) {
    update.pendingCoachEmail = null;
  }

  await db.collection('teams').doc(teamId).update(update);

  const pending = await db.collection('pendingCoachAssignments').doc(teamId).get();
  if (pending.exists) {
    const data = pending.data();
    if (data.userId === uid || (userEmail && data.email === userEmail)) {
      await pending.ref.delete();
    }
  }
}

/**
 * Delete a team and everything hanging off it.
 *
 * The match-scoped collections are keyed by teamId rather than nested under the
 * match, so each needs its own query; missing one is what leaves the orphans
 * Play's reviewers look for.
 */
async function deleteTeamCascade(db, teamId) {
  const players = await db.collection('players').where('teamId', '==', teamId).get();
  for (const playerDoc of players.docs) {
    const imageUri = playerDoc.data().imageUri;
    if (imageUri) {
      await deleteStorageObject(imageUri);
    }
    // recursiveDelete takes the playerStats subcollection with the document.
    await db.recursiveDelete(playerDoc.ref);
  }

  const matches = await db.collection('matches').where('teamId', '==', teamId).get();
  for (const matchDoc of matches.docs) {
    await db.recursiveDelete(matchDoc.ref);
  }

  for (const collection of [
    'goals',
    'substitutions',
    'playerTimes',
    'playerTimeHistory',
    'matchOperations',
    'shortLinks',
  ]) {
    const docs = await db.collection(collection).where('teamId', '==', teamId).get();
    await deleteDocs(db, docs.docs);
  }

  await db.collection('pendingCoachAssignments').doc(teamId).delete().catch(() => {});

  // Takes teamStats with it.
  await db.recursiveDelete(db.collection('teams').doc(teamId));

  return { players: players.size, matches: matches.size };
}

/**
 * Everything left of a club once its teams are gone: the remaining membership
 * rows, the president's notification feed, per-member preferences and the club
 * document itself (recursiveDelete covers the subcollections).
 */
async function deleteClubRemains(db, clubId) {
  const members = await db.collection('clubMembers').where('clubId', '==', clubId).get();
  await deleteDocs(db, members.docs);

  await db.recursiveDelete(db.collection('presidentNotifications').doc(clubId));
  await db.recursiveDelete(db.collection('clubs').doc(clubId));
}

async function deleteDocs(db, docs) {
  for (let i = 0; i < docs.length; i += BATCH_LIMIT) {
    const batch = db.batch();
    for (const doc of docs.slice(i, i + BATCH_LIMIT)) {
      batch.delete(doc.ref);
    }
    await batch.commit();
  }
}

/**
 * Delete a Storage object from the download URL stored on the document.
 *
 * Best effort: a photo that was never uploaded, or already removed, must not
 * abort the cascade and leave the account half-deleted.
 */
async function deleteStorageObject(downloadUrl) {
  const path = storagePathFromUrl(downloadUrl);
  if (!path) return;

  try {
    await admin.storage().bucket().file(path).delete();
  } catch (error) {
    if (error.code !== 404) {
      console.warn('[deleteAccount] Could not delete storage object', path, error.message);
    }
  }
}

/**
 * Extract the object path from a Firebase Storage download URL
 * (`.../o/players%2Fabc.jpg?alt=media&token=...`). Returns null for anything
 * else, including the local `content://` and `file://` URIs that player photos
 * carry before they are uploaded.
 */
function storagePathFromUrl(downloadUrl) {
  const match = /\/o\/([^?]+)/.exec(downloadUrl);
  if (!match) return null;
  try {
    return decodeURIComponent(match[1]);
  } catch (error) {
    return null;
  }
}

async function deleteStoragePrefix(prefix) {
  try {
    await admin.storage().bucket().deleteFiles({ prefix });
  } catch (error) {
    console.warn('[deleteAccount] Could not delete storage prefix', prefix, error.message);
  }
}

exports.deleteAccountCascade = deleteAccountCascade;
exports.storagePathFromUrl = storagePathFromUrl;
