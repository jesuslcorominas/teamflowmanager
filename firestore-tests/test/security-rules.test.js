const { assertFails, assertSucceeds, initializeTestEnvironment } = require('@firebase/rules-unit-testing');
const { doc, setDoc, getDoc, updateDoc, deleteDoc, collection, addDoc } = require('firebase/firestore');

const PROJECT_ID = 'teamflowmanager-test';
const FIRESTORE_RULES_PATH = '../firestore.rules';

describe('Firestore Security Rules - C1-S3', () => {
  let testEnv;

  before(async () => {
    testEnv = await initializeTestEnvironment({
      projectId: PROJECT_ID,
      firestore: {
        rules: require('fs').readFileSync(FIRESTORE_RULES_PATH, 'utf8'),
        host: '127.0.0.1',
        port: 8080
      }
    });
  });

  after(async () => {
    await testEnv.cleanup();
  });

  beforeEach(async () => {
    await testEnv.clearFirestore();
  });

  describe('Clubs - Write Permissions', () => {
    it('should allow USER_X to modify club where USER_X is ownerId', async () => {
      const userId = 'user_x';
      const clubId = 'club_a';
      
      const adminDb = testEnv.authenticatedContext(userId, { admin: true }).firestore();
      await setDoc(doc(adminDb, 'clubs', clubId), {
        ownerId: userId,
        name: 'Club A',
        invitationCode: 'INVITE123'
      });

      const userDb = testEnv.authenticatedContext(userId).firestore();
      await assertSucceeds(
        updateDoc(doc(userDb, 'clubs', clubId), {
          name: 'Club A Updated'
        })
      );
    });

    it('should deny USER_Y from modifying club owned by USER_X', async () => {
      const userX = 'user_x';
      const userY = 'user_y';
      const clubId = 'club_a';
      
      const adminDb = testEnv.authenticatedContext(userX, { admin: true }).firestore();
      await setDoc(doc(adminDb, 'clubs', clubId), {
        ownerId: userX,
        name: 'Club A',
        invitationCode: 'INVITE123'
      });

      const userYDb = testEnv.authenticatedContext(userY).firestore();
      await assertFails(
        updateDoc(doc(userYDb, 'clubs', clubId), {
          name: 'Club A Hacked'
        })
      );
    });

    it('should allow USER_X to create a club with themselves as owner', async () => {
      const userId = 'user_x';
      const clubId = 'club_new';
      
      const userDb = testEnv.authenticatedContext(userId).firestore();
      await assertSucceeds(
        setDoc(doc(userDb, 'clubs', clubId), {
          ownerId: userId,
          name: 'New Club',
          invitationCode: 'NEW123'
        })
      );
    });

    it('should deny USER_X from creating a club with USER_Y as owner', async () => {
      const userX = 'user_x';
      const userY = 'user_y';
      const clubId = 'club_new';
      
      const userDb = testEnv.authenticatedContext(userX).firestore();
      await assertFails(
        setDoc(doc(userDb, 'clubs', clubId), {
          ownerId: userY,
          name: 'Fake Club',
          invitationCode: 'FAKE123'
        })
      );
    });

    it('should allow USER_X to delete their own club', async () => {
      const userId = 'user_x';
      const clubId = 'club_a';
      
      const adminDb = testEnv.authenticatedContext(userId, { admin: true }).firestore();
      await setDoc(doc(adminDb, 'clubs', clubId), {
        ownerId: userId,
        name: 'Club A',
        invitationCode: 'INVITE123'
      });

      const userDb = testEnv.authenticatedContext(userId).firestore();
      await assertSucceeds(deleteDoc(doc(userDb, 'clubs', clubId)));
    });

    it('should deny unauthenticated access to clubs', async () => {
      const unauthDb = testEnv.unauthenticatedContext().firestore();
      await assertFails(
        setDoc(doc(unauthDb, 'clubs', 'club_test'), {
          ownerId: 'someone',
          name: 'Test Club',
          invitationCode: 'TEST123'
        })
      );
    });
  });

  describe('Teams - Write Permissions', () => {
    it('should allow coach (USER_X) to modify their team', async () => {
      const coachId = 'user_x';
      const teamId = 'team_b';
      
      const adminDb = testEnv.authenticatedContext(coachId, { admin: true }).firestore();
      await setDoc(doc(adminDb, 'teams', teamId), {
        ownerId: coachId,
        name: 'Team B',
        coachName: 'Coach X',
        delegateName: 'Delegate X',
        teamType: 11
      });

      const userDb = testEnv.authenticatedContext(coachId).firestore();
      await assertSucceeds(
        updateDoc(doc(userDb, 'teams', teamId), {
          name: 'Team B Updated'
        })
      );
    });

    it('should deny USER_Y from modifying team coached by USER_X', async () => {
      const coachX = 'user_x';
      const userY = 'user_y';
      const teamId = 'team_b';
      
      const adminDb = testEnv.authenticatedContext(coachX, { admin: true }).firestore();
      await setDoc(doc(adminDb, 'teams', teamId), {
        ownerId: coachX,
        name: 'Team B',
        coachName: 'Coach X',
        delegateName: 'Delegate X',
        teamType: 11
      });

      const userYDb = testEnv.authenticatedContext(userY).firestore();
      await assertFails(
        updateDoc(doc(userYDb, 'teams', teamId), {
          name: 'Team B Hacked'
        })
      );
    });

    it('should allow coach to create a team', async () => {
      const coachId = 'user_x';
      const teamId = 'team_new';
      
      const userDb = testEnv.authenticatedContext(coachId).firestore();
      await assertSucceeds(
        setDoc(doc(userDb, 'teams', teamId), {
          ownerId: coachId,
          name: 'New Team',
          coachName: 'Coach X',
          delegateName: 'Delegate X',
          teamType: 11
        })
      );
    });

    it('should allow coach to delete their team', async () => {
      const coachId = 'user_x';
      const teamId = 'team_b';
      
      const adminDb = testEnv.authenticatedContext(coachId, { admin: true }).firestore();
      await setDoc(doc(adminDb, 'teams', teamId), {
        ownerId: coachId,
        name: 'Team B',
        coachName: 'Coach X',
        delegateName: 'Delegate X',
        teamType: 11
      });

      const userDb = testEnv.authenticatedContext(coachId).firestore();
      await assertSucceeds(deleteDoc(doc(userDb, 'teams', teamId)));
    });
  });

  describe('Players - Write Permissions', () => {
    beforeEach(async () => {
      // Setup: Create a team
      const coachId = 'user_x';
      const teamId = 'team_b';
      const adminDb = testEnv.authenticatedContext(coachId, { admin: true }).firestore();
      await setDoc(doc(adminDb, 'teams', teamId), {
        ownerId: coachId,
        name: 'Team B',
        coachName: 'Coach X',
        delegateName: 'Delegate X',
        teamType: 11
      });
    });

    it('should allow coach to modify players in their team', async () => {
      const coachId = 'user_x';
      const teamId = 'team_b';
      const playerId = 'player_1';
      
      const adminDb = testEnv.authenticatedContext(coachId, { admin: true }).firestore();
      await setDoc(doc(adminDb, 'players', playerId), {
        teamId: teamId,
        firstName: 'John',
        lastName: 'Doe',
        number: 10,
        positions: 'FW',
        captain: false,
        deleted: false
      });

      const userDb = testEnv.authenticatedContext(coachId).firestore();
      await assertSucceeds(
        updateDoc(doc(userDb, 'players', playerId), {
          number: 11
        })
      );
    });

    it('should deny non-coach from modifying players', async () => {
      const coachId = 'user_x';
      const otherUser = 'user_y';
      const teamId = 'team_b';
      const playerId = 'player_1';
      
      const adminDb = testEnv.authenticatedContext(coachId, { admin: true }).firestore();
      await setDoc(doc(adminDb, 'players', playerId), {
        teamId: teamId,
        firstName: 'John',
        lastName: 'Doe',
        number: 10,
        positions: 'FW',
        captain: false,
        deleted: false
      });

      const userYDb = testEnv.authenticatedContext(otherUser).firestore();
      await assertFails(
        updateDoc(doc(userYDb, 'players', playerId), {
          number: 99
        })
      );
    });

    it('should allow coach to create a player in their team', async () => {
      const coachId = 'user_x';
      const teamId = 'team_b';
      const playerId = 'player_new';
      
      const userDb = testEnv.authenticatedContext(coachId).firestore();
      await assertSucceeds(
        setDoc(doc(userDb, 'players', playerId), {
          teamId: teamId,
          firstName: 'Jane',
          lastName: 'Smith',
          number: 7,
          positions: 'MF',
          captain: false,
          deleted: false
        })
      );
    });

    it('should allow coach to delete a player from their team', async () => {
      const coachId = 'user_x';
      const teamId = 'team_b';
      const playerId = 'player_1';
      
      const adminDb = testEnv.authenticatedContext(coachId, { admin: true }).firestore();
      await setDoc(doc(adminDb, 'players', playerId), {
        teamId: teamId,
        firstName: 'John',
        lastName: 'Doe',
        number: 10,
        positions: 'FW',
        captain: false,
        deleted: false
      });

      const userDb = testEnv.authenticatedContext(coachId).firestore();
      await assertSucceeds(deleteDoc(doc(userDb, 'players', playerId)));
    });
  });

  describe('Matches - Write Permissions', () => {
    beforeEach(async () => {
      // Setup: Create a team
      const coachId = 'user_x';
      const teamId = 'team_b';
      const adminDb = testEnv.authenticatedContext(coachId, { admin: true }).firestore();
      await setDoc(doc(adminDb, 'teams', teamId), {
        ownerId: coachId,
        name: 'Team B',
        coachName: 'Coach X',
        delegateName: 'Delegate X',
        teamType: 11
      });
    });

    it('should allow coach to modify matches in their team', async () => {
      const coachId = 'user_x';
      const teamId = 'team_b';
      const matchId = 'match_1';
      
      const adminDb = testEnv.authenticatedContext(coachId, { admin: true }).firestore();
      await setDoc(doc(adminDb, 'matches', matchId), {
        teamId: teamId,
        teamName: 'Team B',
        opponent: 'Team C',
        location: 'Field 1',
        dateTime: 1234567890,
        numberOfPeriods: 2,
        status: 'SCHEDULED',
        archived: false
      });

      const userDb = testEnv.authenticatedContext(coachId).firestore();
      await assertSucceeds(
        updateDoc(doc(userDb, 'matches', matchId), {
          opponent: 'Team D'
        })
      );
    });

    it('should deny non-coach from modifying matches', async () => {
      const coachId = 'user_x';
      const otherUser = 'user_y';
      const teamId = 'team_b';
      const matchId = 'match_1';
      
      const adminDb = testEnv.authenticatedContext(coachId, { admin: true }).firestore();
      await setDoc(doc(adminDb, 'matches', matchId), {
        teamId: teamId,
        teamName: 'Team B',
        opponent: 'Team C',
        location: 'Field 1',
        dateTime: 1234567890,
        numberOfPeriods: 2,
        status: 'SCHEDULED',
        archived: false
      });

      const userYDb = testEnv.authenticatedContext(otherUser).firestore();
      await assertFails(
        updateDoc(doc(userYDb, 'matches', matchId), {
          opponent: 'Team Hacked'
        })
      );
    });

    it('should allow coach to create a match for their team', async () => {
      const coachId = 'user_x';
      const teamId = 'team_b';
      const matchId = 'match_new';
      
      const userDb = testEnv.authenticatedContext(coachId).firestore();
      await assertSucceeds(
        setDoc(doc(userDb, 'matches', matchId), {
          teamId: teamId,
          teamName: 'Team B',
          opponent: 'Team E',
          location: 'Field 2',
          dateTime: 1234567890,
          numberOfPeriods: 2,
          status: 'SCHEDULED',
          archived: false
        })
      );
    });

    it('should allow coach to delete a match from their team', async () => {
      const coachId = 'user_x';
      const teamId = 'team_b';
      const matchId = 'match_1';
      
      const adminDb = testEnv.authenticatedContext(coachId, { admin: true }).firestore();
      await setDoc(doc(adminDb, 'matches', matchId), {
        teamId: teamId,
        teamName: 'Team B',
        opponent: 'Team C',
        location: 'Field 1',
        dateTime: 1234567890,
        numberOfPeriods: 2,
        status: 'SCHEDULED',
        archived: false
      });

      const userDb = testEnv.authenticatedContext(coachId).firestore();
      await assertSucceeds(deleteDoc(doc(userDb, 'matches', matchId)));
    });
  });

  describe('Teams - Read Permissions', () => {
    it('should allow coach to read their own team', async () => {
      const coachId = 'user_x';
      const teamId = 'team_b';
      
      const adminDb = testEnv.authenticatedContext(coachId, { admin: true }).firestore();
      await setDoc(doc(adminDb, 'teams', teamId), {
        ownerId: coachId,
        name: 'Team B',
        coachName: 'Coach X',
        delegateName: 'Delegate X',
        teamType: 11,
        clubId: null
      });

      const userDb = testEnv.authenticatedContext(coachId).firestore();
      await assertSucceeds(getDoc(doc(userDb, 'teams', teamId)));
    });

    it('should allow Presidente to read teams in their club', async () => {
      const clubOwnerId = 'user_club_owner';
      const presidenteId = 'user_presidente';
      const coachId = 'user_coach';
      const clubId = 'club_a';
      const teamId = 'team_b';
      
      // Setup: Create club, club member (Presidente), and team
      const adminDb = testEnv.authenticatedContext(clubOwnerId, { admin: true }).firestore();
      await setDoc(doc(adminDb, 'clubs', clubId), {
        ownerId: clubOwnerId,
        name: 'Club A',
        invitationCode: 'INVITE123'
      });
      
      await setDoc(doc(adminDb, 'clubMembers', `${presidenteId}_${clubId}`), {
        userId: presidenteId,
        name: 'Presidente User',
        email: 'presidente@club.com',
        clubId: clubId,
        role: 'Presidente'
      });
      
      await setDoc(doc(adminDb, 'teams', teamId), {
        ownerId: coachId,
        name: 'Team B',
        coachName: 'Coach X',
        delegateName: 'Delegate X',
        teamType: 11,
        clubId: clubId
      });

      const presidenteDb = testEnv.authenticatedContext(presidenteId).firestore();
      await assertSucceeds(getDoc(doc(presidenteDb, 'teams', teamId)));
    });

    it('should deny unauthorized user from reading team', async () => {
      const coachId = 'user_x';
      const otherUser = 'user_y';
      const teamId = 'team_b';
      
      const adminDb = testEnv.authenticatedContext(coachId, { admin: true }).firestore();
      await setDoc(doc(adminDb, 'teams', teamId), {
        ownerId: coachId,
        name: 'Team B',
        coachName: 'Coach X',
        delegateName: 'Delegate X',
        teamType: 11,
        clubId: null
      });

      const userYDb = testEnv.authenticatedContext(otherUser).firestore();
      await assertFails(getDoc(doc(userYDb, 'teams', teamId)));
    });

    it('should deny non-Presidente club member from reading team', async () => {
      const clubOwnerId = 'user_club_owner';
      const regularMemberId = 'user_regular';
      const coachId = 'user_coach';
      const clubId = 'club_a';
      const teamId = 'team_b';
      
      // Setup: Create club, club member (non-Presidente), and team
      const adminDb = testEnv.authenticatedContext(clubOwnerId, { admin: true }).firestore();
      await setDoc(doc(adminDb, 'clubs', clubId), {
        ownerId: clubOwnerId,
        name: 'Club A',
        invitationCode: 'INVITE123'
      });
      
      await setDoc(doc(adminDb, 'clubMembers', `${regularMemberId}_${clubId}`), {
        userId: regularMemberId,
        name: 'Regular Member',
        email: 'member@club.com',
        clubId: clubId,
        role: 'Socio'
      });
      
      await setDoc(doc(adminDb, 'teams', teamId), {
        ownerId: coachId,
        name: 'Team B',
        coachName: 'Coach X',
        delegateName: 'Delegate X',
        teamType: 11,
        clubId: clubId
      });

      const memberDb = testEnv.authenticatedContext(regularMemberId).firestore();
      await assertFails(getDoc(doc(memberDb, 'teams', teamId)));
    });
  });

  describe('Players - Read Permissions', () => {
    beforeEach(async () => {
      // Setup: Create a team
      const coachId = 'user_x';
      const teamId = 'team_b';
      const adminDb = testEnv.authenticatedContext(coachId, { admin: true }).firestore();
      await setDoc(doc(adminDb, 'teams', teamId), {
        ownerId: coachId,
        name: 'Team B',
        coachName: 'Coach X',
        delegateName: 'Delegate X',
        teamType: 11,
        clubId: null
      });
    });

    it('should allow coach to read players in their team', async () => {
      const coachId = 'user_x';
      const teamId = 'team_b';
      const playerId = 'player_1';
      
      const adminDb = testEnv.authenticatedContext(coachId, { admin: true }).firestore();
      await setDoc(doc(adminDb, 'players', playerId), {
        teamId: teamId,
        firstName: 'John',
        lastName: 'Doe',
        number: 10,
        positions: 'FW',
        captain: false,
        deleted: false
      });

      const userDb = testEnv.authenticatedContext(coachId).firestore();
      await assertSucceeds(getDoc(doc(userDb, 'players', playerId)));
    });

    it('should allow Presidente to read players in club teams', async () => {
      const clubOwnerId = 'user_club_owner';
      const presidenteId = 'user_presidente';
      const coachId = 'user_coach';
      const clubId = 'club_a';
      const teamId = 'team_b';
      const playerId = 'player_1';
      
      // Setup: Create club, club member (Presidente), team, and player
      const adminDb = testEnv.authenticatedContext(clubOwnerId, { admin: true }).firestore();
      await setDoc(doc(adminDb, 'clubs', clubId), {
        ownerId: clubOwnerId,
        name: 'Club A',
        invitationCode: 'INVITE123'
      });
      
      await setDoc(doc(adminDb, 'clubMembers', `${presidenteId}_${clubId}`), {
        userId: presidenteId,
        name: 'Presidente User',
        email: 'presidente@club.com',
        clubId: clubId,
        role: 'Presidente'
      });
      
      await setDoc(doc(adminDb, 'teams', teamId), {
        ownerId: coachId,
        name: 'Team B',
        coachName: 'Coach X',
        delegateName: 'Delegate X',
        teamType: 11,
        clubId: clubId
      });
      
      await setDoc(doc(adminDb, 'players', playerId), {
        teamId: teamId,
        firstName: 'John',
        lastName: 'Doe',
        number: 10,
        positions: 'FW',
        captain: false,
        deleted: false
      });

      const presidenteDb = testEnv.authenticatedContext(presidenteId).firestore();
      await assertSucceeds(getDoc(doc(presidenteDb, 'players', playerId)));
    });

    it('should deny unauthorized user from reading players', async () => {
      const coachId = 'user_x';
      const otherUser = 'user_y';
      const teamId = 'team_b';
      const playerId = 'player_1';
      
      const adminDb = testEnv.authenticatedContext(coachId, { admin: true }).firestore();
      await setDoc(doc(adminDb, 'players', playerId), {
        teamId: teamId,
        firstName: 'John',
        lastName: 'Doe',
        number: 10,
        positions: 'FW',
        captain: false,
        deleted: false
      });

      const userYDb = testEnv.authenticatedContext(otherUser).firestore();
      await assertFails(getDoc(doc(userYDb, 'players', playerId)));
    });
  });

  describe('Matches - Read Permissions', () => {
    beforeEach(async () => {
      // Setup: Create a team
      const coachId = 'user_x';
      const teamId = 'team_b';
      const adminDb = testEnv.authenticatedContext(coachId, { admin: true }).firestore();
      await setDoc(doc(adminDb, 'teams', teamId), {
        ownerId: coachId,
        name: 'Team B',
        coachName: 'Coach X',
        delegateName: 'Delegate X',
        teamType: 11,
        clubId: null
      });
    });

    it('should allow coach to read matches in their team', async () => {
      const coachId = 'user_x';
      const teamId = 'team_b';
      const matchId = 'match_1';
      
      const adminDb = testEnv.authenticatedContext(coachId, { admin: true }).firestore();
      await setDoc(doc(adminDb, 'matches', matchId), {
        teamId: teamId,
        teamName: 'Team B',
        opponent: 'Team C',
        location: 'Field 1',
        dateTime: 1234567890,
        numberOfPeriods: 2,
        status: 'SCHEDULED',
        archived: false
      });

      const userDb = testEnv.authenticatedContext(coachId).firestore();
      await assertSucceeds(getDoc(doc(userDb, 'matches', matchId)));
    });

    it('should allow Presidente to read matches in club teams', async () => {
      const clubOwnerId = 'user_club_owner';
      const presidenteId = 'user_presidente';
      const coachId = 'user_coach';
      const clubId = 'club_a';
      const teamId = 'team_b';
      const matchId = 'match_1';
      
      // Setup: Create club, club member (Presidente), team, and match
      const adminDb = testEnv.authenticatedContext(clubOwnerId, { admin: true }).firestore();
      await setDoc(doc(adminDb, 'clubs', clubId), {
        ownerId: clubOwnerId,
        name: 'Club A',
        invitationCode: 'INVITE123'
      });
      
      await setDoc(doc(adminDb, 'clubMembers', `${presidenteId}_${clubId}`), {
        userId: presidenteId,
        name: 'Presidente User',
        email: 'presidente@club.com',
        clubId: clubId,
        role: 'Presidente'
      });
      
      await setDoc(doc(adminDb, 'teams', teamId), {
        ownerId: coachId,
        name: 'Team B',
        coachName: 'Coach X',
        delegateName: 'Delegate X',
        teamType: 11,
        clubId: clubId
      });
      
      await setDoc(doc(adminDb, 'matches', matchId), {
        teamId: teamId,
        teamName: 'Team B',
        opponent: 'Team C',
        location: 'Field 1',
        dateTime: 1234567890,
        numberOfPeriods: 2,
        status: 'SCHEDULED',
        archived: false
      });

      const presidenteDb = testEnv.authenticatedContext(presidenteId).firestore();
      await assertSucceeds(getDoc(doc(presidenteDb, 'matches', matchId)));
    });

    it('should deny unauthorized user from reading matches', async () => {
      const coachId = 'user_x';
      const otherUser = 'user_y';
      const teamId = 'team_b';
      const matchId = 'match_1';
      
      const adminDb = testEnv.authenticatedContext(coachId, { admin: true }).firestore();
      await setDoc(doc(adminDb, 'matches', matchId), {
        teamId: teamId,
        teamName: 'Team B',
        opponent: 'Team C',
        location: 'Field 1',
        dateTime: 1234567890,
        numberOfPeriods: 2,
        status: 'SCHEDULED',
        archived: false
      });

      const userYDb = testEnv.authenticatedContext(otherUser).firestore();
      await assertFails(getDoc(doc(userYDb, 'matches', matchId)));
    });
  });
});
