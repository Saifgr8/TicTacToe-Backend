# Tic Tac Twist

Tic Tac Twist is a Java backend for a multiplayer, expanded take on tic-tac-toe. The game concept combines a growing board with points and special moves. This repository currently contains the backend; it does not include a frontend.

## What is implemented

- Player registration, password login, Google sign-in integration, and JWT-based HTTP authentication.
- Match creation, move submission, match history, and match completion endpoints.
- Real-time gameplay and lobby messaging over STOMP/WebSocket.
- A live game session with turn handling, win and draw checks, pattern scoring, and special move types.
- PostgreSQL persistence for players, matches, and move snapshots.

### Gameplay today

Players place symbols in turns. A win is detected when a player fills a complete row, column, or main/anti-diagonal. A full board without a win grows by one row and column, then the session is reset for another round.

Three-cell patterns can award points. The available special move types are `OVERRIDE`, `EMPTY_CELL`, `PATTERN_BOMB`, `DOUBLE_OVERRIDE`, and `TWIST`. Their point costs and effects are implemented in the game engine. A twist temporarily swaps the players' effective symbols.

The game concept may evolve, but a separate spell inventory or active bot gameplay is not currently implemented. The bot source files are commented out, and `SpecialMoveHandler` is empty.

## Architecture

The application is a single Spring Boot service with REST and STOMP/WebSocket interfaces:

```text
HTTP / WebSocket clients
          |
 Controllers
    |           \
 Services     GameSessionManager
    |              |
Repositories    GameSession -> GameEngine
    |              |
 PostgreSQL     MatchData snapshots
```

- **Controllers** receive HTTP and WebSocket requests and return or publish responses.
- **Services** implement authentication, match workflows, and match-history operations.
- **GameSession** holds the state of a live match; **GameEngine** validates moves, updates the board, scores patterns, and checks wins/draws.
- **GameSessionManager** creates and stores active sessions in memory.
- **JPA models and repositories** persist players, matches, and move snapshots in PostgreSQL.
- **Security components** use BCrypt password encoding and JWTs for HTTP authentication. Google sign-in retrieves user information through Google's user-info endpoint.

### Main code areas

| Package | Main responsibilities |
|---|---|
| `Contollers` | REST authentication, match, and match-data endpoints; WebSocket gameplay and invitation handlers. |
| `Services` | Player authentication, Google user lookup, match operations, and match-data operations. |
| `GAME` | `GameSession` state and turn flow, `GameEngine` rule processing, game enums, and diagonal helpers. |
| `WebSocketConfiguration` | STOMP setup, handshake principals, active-user tracking, and in-memory game sessions. |
| `Models` | JPA entities for `Player`, `Match`, and `MatchData`, plus persistence enums. |
| `Repositories` | Spring Data JPA interfaces for players, matches, and move records. |
| `JWT` and `SpringSecurity` | JWT creation/validation/filtering, user lookup, and HTTP security configuration. |
| `DTOs` | Request and response types for authentication, game sessions, moves, invitations, and RPS messages. |
| `GlobalException` | Application exceptions and HTTP exception response handling. |
| `bot` | Bot-related types; current bot implementation is commented out. |

## How it works

`TicTacToeApplication` starts Spring Boot and loads the application's configured components. For HTTP authentication, clients register or log in through `PlayerAuthController`; protected requests provide the returned bearer JWT. WebSocket connections use the `/gameplay` STOMP endpoint.

When a match is created, `GameSessionManager` loads or creates the player records, saves a match, constructs a `GameSession`, and adds it to an in-memory session map. A move is passed to the session, which asks `GameEngine` to validate and apply it. Successful moves are saved as `MatchData` board snapshots. The session then awards points for newly detected patterns and checks for a winner or a full-board draw. Controllers return the updated state over HTTP or publish it to WebSocket subscribers.

Match and player records are stored in PostgreSQL. Active game sessions and some lobby/RPS coordination state are held in application memory.

## Technologies

- Java 17
- Spring Boot 3.5.3
- Maven
- Spring Web, Validation, Data JPA, Security, WebSocket, and OAuth2 Client
- PostgreSQL
- JJWT 0.12.6
- JUnit 5, Spring Boot Test, Mockito, and Spring Security Test

## Run locally

You will need Java 17 or later and a PostgreSQL database. Configure the datasource and authentication settings for your environment before starting the application. Do not commit credentials or tokens to source control.

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS or Linux:

```bash
./mvnw spring-boot:run
```

Run the test suite with:

```powershell
.\mvnw.cmd test
```

The current test sources have a known compilation issue: `src/test/java/com/example/TicTacToe/Services/AuthService.java` expects `PlayerAuthService.loginPlayerService` to return a `Map`, while the service returns `DTOLoginResponse`. Tests need that mismatch resolved before the suite can run.

## Current status and limitations

- REST and WebSocket gameplay paths both operate on in-memory sessions; sessions are not persisted for recovery across application restarts.
- Match completion and player-stat updates are handled in multiple flows.
- `gameMode` is present in the game-creation request but is not used by session creation.
- The standalone match-data save endpoint is separate from the live session move flow.
- Bot gameplay is inactive, and the special-move handler is a placeholder.
- Automated gameplay-rule tests are not currently present in the test suite.

## Potential improvements

These are areas to consider for future work, not features currently implemented:

- Centralize move handling so HTTP and WebSocket gameplay share the same application workflow.
- Decide how active sessions should be cleaned up, recovered, or shared across multiple application instances.
- Separate game-rule processing from persistence and match-stat updates.
- Consolidate match completion and player-stat updates into one consistent flow.
- Use response DTOs at API boundaries instead of returning persistence entities.
- Add focused tests for move validation, scoring, special moves, wins, and board expansion.

Ask before making changes that alter game rules, session lifecycle, persistence design, or the overall architecture.
