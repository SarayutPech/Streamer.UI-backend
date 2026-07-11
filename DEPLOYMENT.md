# Deploy backend to Render + Aiven MySQL

Both repos already have GitHub remotes (`SarayutPech/Streamer.UI-backend` and
`SarayutPech/Streamer.UI-frontend`), so Render can deploy straight from
GitHub. Commit and push the changes from this session first (both repos have
new/modified files).

The backend is now multi-tenant: each streamer has their own account (login
with Google/Facebook/YouTube), their own badge/banner library and chat
profiles, and can partner with other streamers to share badges/banners. None
of this exists in local/single-tenant dev mode (no login, one implicit
"streamer") - it only turns on with the `mysql` Spring profile, i.e. this
deployed setup.

## 1. Create the free MySQL database (Aiven)

1. Sign up at https://aiven.io (no card required for the free plan).
2. Create a new service -> **MySQL** -> Free plan -> pick any region.
3. Once it's running, open the service's **Overview** tab and note:
   `Host`, `Port`, `Database name` (usually `defaultdb`), `User` (usually
   `avnadmin`), `Password`.
4. **Run `schema/streamerui_multi_tenant.sql` against it before deploying**
   (DBeaver, `mysql` CLI, whatever you've got - see the SSL notes from
   earlier in this conversation if you use DBeaver). The app now runs with
   `ddl-auto: validate` (it checks the schema matches, it doesn't create or
   alter tables anymore), so the tables have to already exist.

## 2. Register OAuth apps (Google, Facebook)

**Google** (covers both "Continue with Google" and "Continue with YouTube" -
same OAuth client, YouTube login just additionally asks for
`youtube.readonly` scope):
1. https://console.cloud.google.com -> a project -> APIs & Services ->
   Credentials -> Create Credentials -> OAuth client ID -> Web application.
2. Authorized redirect URIs - add **both**:
   - `https://<your-render-backend>.onrender.com/login/oauth2/code/google`
   - `https://<your-render-backend>.onrender.com/login/oauth2/code/youtube`
3. Note the Client ID and Client Secret.

**Facebook**:
1. https://developers.facebook.com -> Create App -> add the "Facebook Login"
   product.
2. Valid OAuth Redirect URI:
   `https://<your-render-backend>.onrender.com/login/oauth2/code/facebook`
3. Note the App ID and App Secret.

(You can skip Facebook and just not set its env vars below if you only want
Google/YouTube login - the "Continue with Facebook" button will just fail to
redirect anywhere useful, but nothing else breaks.)

## 3. Deploy the backend (Render)

1. Sign up at https://render.com (no card required for free tier).
2. **New +** -> **Web Service** -> connect the `Streamer.UI-backend` GitHub
   repo.
3. Environment: **Docker** (it'll auto-detect the `Dockerfile` in this repo).
   Plan: **Free**.
4. Add these environment variables (Render dashboard -> Environment):

   | Key | Value |
   | --- | --- |
   | `SPRING_PROFILES_ACTIVE` | `prod,mysql` |
   | `DB_HOST` | from Aiven |
   | `DB_PORT` | from Aiven |
   | `DB_NAME` | from Aiven |
   | `DB_USER` | from Aiven |
   | `DB_PASSWORD` | from Aiven |
   | `CORS_ALLOWED_ORIGIN` | your Cloudflare Pages URL, e.g. `https://streamer-ui.pages.dev` |
   | `FRONTEND_URL` | same as above - where OAuth login redirects back to after signing in |
   | `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | from step 2 |
   | `FACEBOOK_CLIENT_ID` / `FACEBOOK_CLIENT_SECRET` | from step 2 (optional) |
   | `JWT_SECRET` | a long random string, e.g. output of `openssl rand -base64 48` |

   `YOUTUBE_CLIENT_ID`/`YOUTUBE_CLIENT_SECRET` are optional - they default to
   the Google ones above, which is almost certainly what you want.

5. Deploy. Render gives you a URL like
   `https://streamer-ui-backend.onrender.com` - that's your new backend
   address, and matches the redirect URIs you registered in step 2.

Notes:
- Free tier spins the service down after 15 min with no traffic; the first
  request after that takes ~30-60s to wake it back up. Fine for a hobby
  overlay.
- Free tier disk is ephemeral - uploaded badge/banner images (`/uploads`)
  are lost on redeploy/restart. Everything else (accounts, chat profiles,
  badge/banner metadata, partnerships) is safe in MySQL. If lost uploads
  becomes a problem later, move them to something like Cloudflare R2 (also
  has a free tier) - not done here.

## 4. Point the frontend at the new backend

1. In `Streamer.UI-frontend/.env.production`, set:
   ```
   VITE_API_BASE_URL=https://streamer-ui-backend.onrender.com
   ```
   (use your actual Render URL from step 3.5)
2. Rebuild: `npm run build`
3. Re-upload the new `dist/` to Cloudflare Pages (same way you did before).

## 5. Verify

- Open the Cloudflare Pages URL - you should land on `/login` (production
  mode requires login now). Try "Continue with Google".
- After signing in you should land on `/admin` with your name shown top
  right.
- The "Open overlay" link now includes your streamer id
  (`/overlay/<id>`) - that's the URL to paste into OBS.
- Try the Partnerships tab: search won't find anyone until a second account
  exists to test with.

## Why this fixes the original CORS error

The backend's CORS policy was already wide open (`allowed-origins: "*"`).
The real problem: the frontend build was still pointed at
`http://localhost:8080` (or wherever it was), which doesn't exist from your
visitors' browsers - that shows up in the console as a blocked
cross-origin request. Deploying the backend somewhere public (Render) and
building the frontend with `VITE_API_BASE_URL` pointed at it fixes that.
