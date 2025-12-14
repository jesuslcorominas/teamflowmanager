# Security Rules Performance Note

## Document Read Optimization

The Firestore security rules in this implementation require reading parent documents for permission validation in subcollections. This is a known characteristic of Firestore Security Rules.

### Why Multiple Document Reads?

1. **Subcollection Permission Checks**: To validate access to `playerStats` or `matchStats`, we need:
   - Read the player/match document to get the `teamId`
   - Check if user is the coach of that team (read team document)
   - OR check if user is Presidente of the club linked to that team (read team document, club member document)

2. **OR Conditions**: While Firestore rules support short-circuit evaluation (if first condition is true, second isn't evaluated), when BOTH conditions need document reads, at least one read is required per check.

3. **Separate Read/Write Rules**: Read and write permissions are evaluated separately, requiring document reads for each.

### Language Limitations

Firestore Security Rules language does **not** support:
- Variable assignment within rules
- Caching document reads
- Memoization of function results

From the Firestore documentation:
> "Rules are evaluated on a per-request basis. There is no caching or memoization of document reads within a single rule evaluation."

### Real-World Impact

**For a typical operation accessing player statistics:**
- Read player document (1 read)
- Read team document via `isTeamCoach()` (1 read)
- If coach check fails, read team document again via `isPresidenteOfTeamClub()` (1 read)
- If presidente check, read clubMember document (1 read)
- **Total**: 2-4 document reads per permission check

**Cost Analysis:**
- Free tier: 50,000 reads/day
- Each player stat access: 2-4 reads
- Sustainable for apps with moderate usage
- Monitor in production if usage grows

### Mitigation Strategies

1. **Client-Side Caching**: Cache team/club/member data in the application
2. **Denormalization**: Consider adding permission flags to documents if read costs become significant
3. **Batch Operations**: Group related operations to amortize permission check costs
4. **Monitor Usage**: Use Firebase Console to track document reads and identify hotspots

### Why Not Denormalize?

We chose NOT to denormalize permission data (e.g., adding `coachId` to every player/match document) because:
- Increases data consistency complexity
- Requires updating multiple documents on ownership changes
- The read cost is acceptable for most use cases
- Maintains clean separation of concerns

### Alternatives Considered

1. **Custom Claims**: Store roles in Firebase Auth custom claims
   - **Pros**: No document reads for role checks
   - **Cons**: Limited to 1000 bytes, requires admin SDK to update, doesn't work for dynamic team membership

2. **Firestore Functions**: Move permission checks to Cloud Functions
   - **Pros**: More flexible logic
   - **Cons**: Adds latency, requires separate deployment, higher cost

3. **Client-Side Validation**: Rely on client code to enforce permissions
   - **Cons**: Insecure - can be bypassed by malicious clients

### Conclusion

The current implementation prioritizes:
1. **Security**: Proper access control over performance
2. **Correctness**: Accurate permission checks
3. **Maintainability**: Clear, understandable rules

The document read cost is acceptable and documented. If usage patterns show this becomes a bottleneck, we can revisit and optimize specific hotspots.

---

**Last Updated**: 2025-12-14  
**Related**: C1-S3 Implementation
