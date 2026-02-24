# Club Structure Migration Guide

## Overview

This guide provides step-by-step instructions for introducing the `clubs` and `clubMembers` collections to Firestore in the TeamFlow Manager application.

## Prerequisites

- Firebase project set up with Firestore enabled
- Firebase Admin SDK access (for scripts) or Firebase Console access
- Understanding of Firestore data structure
- Review of `CLUB_STRUCTURE_DATA_MODEL.md` document

## Migration Steps

### Step 1: Enable Firestore (if not already enabled)

1. Go to Firebase Console: https://console.firebase.google.com/
2. Select your TeamFlow Manager project
3. Navigate to "Firestore Database" in the left menu
4. If not enabled, click "Create database"
5. Choose production mode or test mode (test mode for development)
6. Select a location for your database (preferably close to your users)

### Step 2: Create Collections Manually (Firebase Console Method)

#### Creating the clubs Collection

1. In Firestore Console, click "Start collection"
2. Enter collection ID: `clubs`
3. Add the first document:
   - Document ID: (Auto-ID or custom like `club_example_001`)
   - Fields:
     ```
     ownerId: "user_firebase_uid_example" (string)
     name: "Club Ejemplo" (string)
     invitationCode: "INVITE123" (string)
     ```
4. Click "Save"

#### Creating the clubMembers Collection

1. In Firestore Console, click "Start collection"
2. Enter collection ID: `clubMembers`
3. Add the first document:
   - Document ID: (Auto-ID or custom like `member_example_001`)
   - Fields:
     ```
     userId: "user_firebase_uid_example" (string)
     name: "John Doe" (string)
     email: "john.doe@club.com" (string)
     clubId: "club_example_001" (string)
     role: "Presidente" (string)
     ```
4. Click "Save"

### Step 3: Set Up Security Rules

1. In Firebase Console, go to "Firestore Database" → "Rules"
2. Update your security rules to include club structure rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper function to check if user is club owner
    function isClubOwner(clubId) {
      return get(/databases/$(database)/documents/clubs/$(clubId)).data.ownerId == request.auth.uid;
    }
    
    // Helper function to check if user is club member
    function isClubMember(clubId) {
      return exists(/databases/$(database)/documents/clubMembers/$(request.auth.uid + '_' + clubId));
    }
    
    // Clubs collection rules
    match /clubs/{clubId} {
      // Allow read if user is the owner or a member of the club
      allow read: if request.auth != null && (
        resource.data.ownerId == request.auth.uid ||
        isClubMember(clubId)
      );
      
      // Allow create if user is authenticated and sets themselves as owner
      allow create: if request.auth != null && 
        request.resource.data.ownerId == request.auth.uid &&
        request.resource.data.keys().hasAll(['ownerId', 'name', 'invitationCode']);
      
      // Allow update only by the owner
      allow update: if request.auth != null && 
        resource.data.ownerId == request.auth.uid;
      
      // Allow delete only by the owner
      allow delete: if request.auth != null && 
        resource.data.ownerId == request.auth.uid;
    }
    
    // ClubMembers collection rules
    match /clubMembers/{memberId} {
      // Allow read if user is the member or the club owner
      allow read: if request.auth != null && (
        resource.data.userId == request.auth.uid ||
        isClubOwner(resource.data.clubId)
      );
      
      // Allow create only by club owner
      allow create: if request.auth != null && 
        isClubOwner(request.resource.data.clubId) &&
        request.resource.data.keys().hasAll(['userId', 'name', 'email', 'clubId', 'role']);
      
      // Allow update only by club owner
      allow update: if request.auth != null && 
        isClubOwner(resource.data.clubId);
      
      // Allow delete by club owner or the member themselves
      allow delete: if request.auth != null && (
        isClubOwner(resource.data.clubId) ||
        resource.data.userId == request.auth.uid
      );
    }
    
    // Existing rules for teams, players, matches, etc.
    // ... (keep your existing rules here)
  }
}
```

3. Click "Publish" to deploy the security rules

### Step 4: Create Indexes

Some queries may require composite indexes. Create them via Firebase Console:

1. Go to "Firestore Database" → "Indexes"
2. Click "Create Index"

#### Index 1: clubMembers by clubId and role
- Collection ID: `clubMembers`
- Fields to index:
  - `clubId` - Ascending
  - `role` - Ascending
- Query scope: Collection

#### Index 2: clubMembers by userId and clubId
- Collection ID: `clubMembers`
- Fields to index:
  - `userId` - Ascending
  - `clubId` - Ascending
- Query scope: Collection

Note: Firestore will automatically suggest indexes when you run queries that need them. You can also create indexes on-demand when Firebase logs index requirement errors.

### Step 5: Programmatic Data Migration (Optional)

If you need to migrate existing data or create test data programmatically, use this approach:

#### Using Firebase Admin SDK (Node.js)

1. Install Firebase Admin SDK:
```bash
npm install firebase-admin
```

2. Create a migration script `migrate-clubs.js`:

```javascript
const admin = require('firebase-admin');
const serviceAccount = require('./path-to-serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function migrateClubStructure() {
  try {
    // Create sample club
    const clubRef = await db.collection('clubs').add({
      ownerId: 'user_firebase_uid_001',
      name: 'Club Ejemplo',
      invitationCode: 'INVITE123'
    });
    
    console.log('Club created with ID:', clubRef.id);
    
    // Create sample club member
    const memberRef = await db.collection('clubMembers').add({
      userId: 'user_firebase_uid_001',
      name: 'John Doe',
      email: 'john.doe@club.com',
      clubId: clubRef.id,
      role: 'Presidente'
    });
    
    console.log('Club member created with ID:', memberRef.id);
    
    console.log('Migration completed successfully!');
  } catch (error) {
    console.error('Migration error:', error);
  }
}

migrateClubStructure();
```

3. Run the script:
```bash
node migrate-clubs.js
```

#### Using Firestore REST API

You can also use the REST API to create documents:

```bash
# Create a club
curl -X POST \
  https://firestore.googleapis.com/v1/projects/YOUR_PROJECT_ID/databases/(default)/documents/clubs \
  -H 'Authorization: Bearer YOUR_ACCESS_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{
    "fields": {
      "ownerId": {"stringValue": "user_firebase_uid_001"},
      "name": {"stringValue": "Club Ejemplo"},
      "invitationCode": {"stringValue": "INVITE123"}
    }
  }'
```

### Step 6: Verify Collections in App

Once collections are created, you can verify them from your Android app:

#### Test Code Example

```kotlin
// In your data source or repository
fun testClubStructure() {
    val firestore = FirebaseFirestore.getInstance()
    
    // Test reading clubs
    firestore.collection("clubs")
        .whereEqualTo("ownerId", FirebaseAuth.getInstance().currentUser?.uid)
        .get()
        .addOnSuccessListener { documents ->
            Log.d("ClubTest", "Found ${documents.size()} clubs")
            for (document in documents) {
                Log.d("ClubTest", "${document.id} => ${document.data}")
            }
        }
        .addOnFailureListener { exception ->
            Log.w("ClubTest", "Error getting clubs: ", exception)
        }
    
    // Test reading club members
    firestore.collection("clubMembers")
        .whereEqualTo("userId", FirebaseAuth.getInstance().currentUser?.uid)
        .get()
        .addOnSuccessListener { documents ->
            Log.d("ClubMemberTest", "Found ${documents.size()} memberships")
            for (document in documents) {
                Log.d("ClubMemberTest", "${document.id} => ${document.data}")
            }
        }
        .addOnFailureListener { exception ->
            Log.w("ClubMemberTest", "Error getting memberships: ", exception)
        }
}
```

### Step 7: Create Test Data

For testing purposes, create some sample data:

#### Sample Clubs

```json
// Club 1
{
  "ownerId": "user_firebase_uid_001",
  "name": "Club Deportivo Ejemplo",
  "invitationCode": "CLUB001"
}

// Club 2
{
  "ownerId": "user_firebase_uid_002",
  "name": "Academia Juvenil",
  "invitationCode": "ACAD2024"
}

// Club 3
{
  "ownerId": "user_firebase_uid_001",
  "name": "Club Municipal",
  "invitationCode": "MUNICIPAL"
}
```

#### Sample Club Members

```json
// Member 1 - Owner/President
{
  "userId": "user_firebase_uid_001",
  "name": "Juan Pérez",
  "email": "juan.perez@club.com",
  "clubId": "club_doc_id_001",
  "role": "Presidente"
}

// Member 2 - Coach
{
  "userId": "user_firebase_uid_003",
  "name": "María García",
  "email": "maria.garcia@club.com",
  "clubId": "club_doc_id_001",
  "role": "Entrenador Principal"
}

// Member 3 - Assistant
{
  "userId": "user_firebase_uid_004",
  "name": "Carlos Rodríguez",
  "email": "carlos.rodriguez@club.com",
  "clubId": "club_doc_id_001",
  "role": "Entrenador Asistente"
}
```

## Validation Checklist

After completing the migration, verify:

- [ ] Collections `clubs` and `clubMembers` exist in Firestore
- [ ] Sample documents are created with correct structure
- [ ] Security rules are deployed and active
- [ ] Required indexes are created
- [ ] App can read club data (test with authenticated user)
- [ ] App can read club member data (test with authenticated user)
- [ ] Security rules prevent unauthorized access
- [ ] Document IDs are auto-generated correctly
- [ ] All required fields are present in documents

## Rollback Plan

If you need to rollback the migration:

1. **Remove Security Rules**: Comment out or remove the club-related security rules
2. **Delete Collections**: 
   - In Firestore Console, you can delete individual documents
   - For bulk deletion, use Firebase Admin SDK:
   
   ```javascript
   async function rollbackClubStructure() {
     const batch = db.batch();
     
     // Delete all club members
     const membersSnapshot = await db.collection('clubMembers').get();
     membersSnapshot.docs.forEach(doc => batch.delete(doc.ref));
     
     // Delete all clubs
     const clubsSnapshot = await db.collection('clubs').get();
     clubsSnapshot.docs.forEach(doc => batch.delete(doc.ref));
     
     await batch.commit();
     console.log('Rollback completed');
   }
   ```

3. **Remove Indexes**: Delete the created composite indexes from Firebase Console

## Troubleshooting

### Issue: "Permission Denied" when accessing collections

**Solution**: 
- Verify security rules are deployed correctly
- Ensure user is authenticated with Firebase Auth
- Check that `ownerId` matches the authenticated user's UID

### Issue: "Index required" error

**Solution**: 
- Check Firebase Console for suggested index
- Create the index via Firebase Console or follow the error message link
- Wait 2-5 minutes for index to build

### Issue: Documents not appearing in app

**Solution**: 
- Check Firebase Console to verify documents exist
- Verify collection and field names match exactly (case-sensitive)
- Check for typos in collection names
- Ensure proper error handling in your data source

### Issue: Cannot create documents from app

**Solution**: 
- Verify user is authenticated
- Check security rules allow creation
- Ensure all required fields are included
- Verify `ownerId` is set to authenticated user's UID

## Next Steps

After successful migration:

1. Implement data sources for clubs and club members
2. Create use cases for club management
3. Build UI for club creation and member management
4. Implement invitation code functionality
5. Add member role management features
6. Create club settings and configuration screens

## Additional Resources

- [Firestore Documentation](https://firebase.google.com/docs/firestore)
- [Firestore Security Rules](https://firebase.google.com/docs/firestore/security/get-started)
- [Firebase Admin SDK](https://firebase.google.com/docs/admin/setup)
- [Club Structure Data Model](CLUB_STRUCTURE_DATA_MODEL.md)

## Support

For questions or issues related to this migration:
- Review the data model document: `CLUB_STRUCTURE_DATA_MODEL.md`
- Check Firebase documentation
- Review existing Firestore implementations in the codebase (TeamFirestoreModel, PlayerFirestoreModel)

---

**Document Version**: 1.0  
**Last Updated**: 2025-12-14  
**Related Issues**: C1-S1 - Estructura de Club
