# Streamer.UI Backend

Spring Boot service that reads a YouTube Live broadcast's chat and streams it
to the overlay frontend over WebSocket, merging in each chatter's saved
appearance settings (badges, name color/glow, banner).

## Requirements

- Java 17+
- Maven 3.9+ (or use an IDE that bundles one)
- No YouTube API key needed. See "How chat is read" below.

## Run

```bash
mvn spring-boot:run
```

The server starts on `http://localhost:8080`.

## How chat is read (no API key)

This does **not** use the official YouTube Data API. Instead
`com.streamerui.service.YoutubeApiClient` reads the same internal endpoint
that powers YouTube's own live chat "popout" page
(`https://www.youtube.com/live_chat?is_popout=1&v=VIDEO_ID`) - load that page
once to get a continuation token, then repeatedly POST it to YouTube's
internal `youtubei/v1/live_chat/get_live_chat` endpoint for the next batch of
messages. This is the same unofficial technique used by tools like pytchat,
youtube-chat, and chat-downloader.

Trade-offs versus the official API:

- No API key, no setup, no daily quota.
- **Unofficial and unsupported by Google.** It depends on the internal shape
  of YouTube's page/endpoints, which can change without notice. If connecting
  starts failing, check the error message and backend logs first - it'll
  usually say which JSON field it expected but didn't find
  (`YoutubeApiClient` has the field paths in one place, near the top).
- The video must actually be live (an upcoming/scheduled stream that hasn't
  started yet has no live chat to read).

## Data storage

Everything lives under `./data` as plain JSON files, so you can open/edit
them by hand if you want:

- `data/users.json` - per-chatter appearance settings, keyed by YouTube
  channelId (badges, name color, glow, banner).
- `data/config.json` - global overlay settings (header, background, badge
  slot count, etc).

Uploaded badge/banner images are saved under `./uploads` and served at
`/uploads/<filename>`.

### Swapping the JSON files for a real database later

Both repositories are defined as plain interfaces:

- `com.streamerui.repository.UserProfileRepository`
- `com.streamerui.repository.OverlayConfigRepository`

The only implementations right now are the JSON-file ones in
`com.streamerui.repository.json`. To move to Postgres/MySQL/etc., add a JPA
entity + repository implementing the same interface, mark it `@Primary` (or
remove the JSON `@Repository` beans), and nothing else in the app needs to
change - controllers and services only depend on the interfaces.

## REST API

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/users` | list all saved chatter profiles |
| GET | `/api/users/{channelId}` | get one profile |
| POST | `/api/users` | create a profile |
| PUT | `/api/users/{channelId}` | update a profile |
| DELETE | `/api/users/{channelId}` | delete a profile |
| GET | `/api/config` | get overlay config |
| PUT | `/api/config` | update overlay config (also pushed live to overlay via WS) |
| POST | `/api/uploads` | multipart image upload, returns `{ "url": "/uploads/..." }` |
| POST | `/api/youtube/connect` | body `{ videoUrlOrId }` - starts reading chat |
| POST | `/api/youtube/disconnect` | stops reading |
| GET | `/api/youtube/status` | connection status |

WebSocket (STOMP over SockJS) at `/ws`:
- `/topic/chat` - one message per new chat item
- `/topic/config` - pushed whenever config is saved

## Notes

- CORS is wide open (`*`) since this is meant to run locally for one
  streamer. Tighten `streamerui.cors.allowed-origins` in
  `application.yml` if you ever expose it beyond your own machine.
- The poller respects the poll delay YouTube's own endpoint returns (with a
  floor set by `streamerui.youtube.min-poll-interval-ms`), so it won't hammer
  the endpoint.
