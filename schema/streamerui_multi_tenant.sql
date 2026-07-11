-- =====================================================================
-- Streamer.UI - multi-tenant schema (multiple streamer accounts, OAuth
-- login, and streamer-to-streamer partnerships).
--
-- This REPLACES the single-tenant schema the app currently auto-creates
-- (global user_profiles / badge_library / banner_library / overlay_config
-- with no owner). Run this by hand against your Aiven MySQL `defaultdb`
-- (or a fresh schema) - the app's Hibernate ddl-auto=update will NOT
-- migrate old single-tenant tables to this shape on its own.
--
-- Design summary
-- --------------
-- * `streamers`            - one row per streamer account (the tenant).
-- * `oauth_identities`     - login via Google/Facebook/YouTube, linked to
--                            a streamer. A streamer can link more than one
--                            provider; each provider account maps to
--                            exactly one streamer.
-- * `partnerships`         - request/accept relationship between two
--                            streamers. Only one active (pending/accepted)
--                            row can exist per pair at a time; rejected/
--                            revoked history is kept.
-- * `badge_library` /
--   `banner_library`       - owned by a single streamer (owner_streamer_id).
--                            "Can see badges/banners the partner uploaded"
--                            is a QUERY, not a schema change - see the
--                            example query near the bottom.
-- * `user_profiles`        - a streamer's own per-chatter appearance
--                            settings (what you have today, now scoped by
--                            owner_streamer_id instead of being global).
-- * `partnership_user_profiles` - the "separate partnership profile"
--                            option: one shared appearance record per
--                            (partnership, chatter), used only when both
--                            partnered streamers want a chatter's badges/
--                            name color/banner to look the same on both
--                            channels instead of configuring it twice.
-- * `overlay_config`       - now one row per streamer instead of a single
--                            global row.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Streamer accounts
-- ---------------------------------------------------------------------
CREATE TABLE streamers (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    display_name  VARCHAR(256)      NOT NULL,
    primary_email VARCHAR(320)      NULL,
    avatar_url    VARCHAR(1024)     NULL,
    -- Plain username/password login (in addition to/instead of OAuth - see
    -- LocalAuthController). Null for accounts created purely via OAuth.
    username      VARCHAR(64)       NULL,
    password_hash VARCHAR(255)      NULL,
    status        ENUM('active','disabled') NOT NULL DEFAULT 'active',
    created_at    DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_streamers_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- OAuth logins (Google / Facebook / YouTube). One row per linked
-- provider account; a streamer can link several providers, but each
-- provider slot (google/facebook/youtube) can only be used once per
-- streamer, and each external account can only ever map to one streamer.
--
-- access_token/refresh_token are here in case you later call the official
-- YouTube Data API (e.g. to verify channel ownership) - encrypt these at
-- the application layer before writing, or drop the columns if you end up
-- not needing them (today's chat reader doesn't use the official API).
-- ---------------------------------------------------------------------
CREATE TABLE oauth_identities (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    streamer_id         BIGINT        NOT NULL,
    provider            ENUM('google','facebook','youtube') NOT NULL,
    provider_user_id    VARCHAR(191)  NOT NULL,
    email               VARCHAR(320)  NULL,
    display_name        VARCHAR(256)  NULL,
    avatar_url          VARCHAR(1024) NULL,
    -- Only meaningful when provider = 'youtube'; this is what the app
    -- passes to the chat poller. Kept nullable/generic for the other
    -- two providers.
    youtube_channel_id  VARCHAR(64)   NULL,
    access_token        TEXT          NULL,
    refresh_token       TEXT          NULL,
    token_expires_at    DATETIME      NULL,
    linked_at           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at       DATETIME      NULL,
    CONSTRAINT fk_oauth_streamer FOREIGN KEY (streamer_id) REFERENCES streamers(id) ON DELETE CASCADE,
    UNIQUE KEY uq_oauth_provider_account (provider, provider_user_id),
    UNIQUE KEY uq_oauth_streamer_provider (streamer_id, provider)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Partnerships. Request/accept flow between two streamers.
-- Enforces at most one active (pending or accepted) row per unordered
-- pair via a generated column that collapses to NULL once a request is
-- rejected/revoked (MySQL unique indexes allow unlimited NULLs, so old
-- rejected/revoked rows don't block a fresh request later).
-- ---------------------------------------------------------------------
CREATE TABLE partnerships (
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    requester_streamer_id  BIGINT NOT NULL,
    addressee_streamer_id  BIGINT NOT NULL,
    status                 ENUM('pending','accepted','rejected','revoked') NOT NULL DEFAULT 'pending',
    requested_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at           DATETIME NULL,
    active_pair_key VARCHAR(64) AS (
        CASE WHEN status IN ('pending','accepted')
             THEN CONCAT(LEAST(requester_streamer_id, addressee_streamer_id),
                         '-',
                         GREATEST(requester_streamer_id, addressee_streamer_id))
             ELSE NULL
        END
    ) STORED,
    -- No ON DELETE CASCADE here (deliberately): requester_streamer_id and
    -- addressee_streamer_id feed the active_pair_key generated column above,
    -- and MySQL/InnoDB rejects CASCADE/SET NULL on a foreign key for any
    -- column a generated column depends on ("Cannot add foreign key
    -- constraint", error 1215). Deleting a streamer with active
    -- partnerships will fail instead - clear their partnerships first.
    CONSTRAINT fk_partnership_requester FOREIGN KEY (requester_streamer_id) REFERENCES streamers(id),
    CONSTRAINT fk_partnership_addressee FOREIGN KEY (addressee_streamer_id) REFERENCES streamers(id),
    CONSTRAINT chk_partnership_not_self CHECK (requester_streamer_id <> addressee_streamer_id),
    UNIQUE KEY uq_partnership_active_pair (active_pair_key),
    KEY idx_partnership_requester (requester_streamer_id),
    KEY idx_partnership_addressee (addressee_streamer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Per-streamer overlay config (one row per streamer now, not a single
-- global singleton).
-- ---------------------------------------------------------------------
CREATE TABLE overlay_config (
    streamer_id             BIGINT PRIMARY KEY,
    header_enabled          BOOLEAN       NOT NULL DEFAULT TRUE,
    header_text             VARCHAR(512)  NOT NULL DEFAULT 'Live Chat',
    header_logo_url         VARCHAR(1024) NULL,
    header_banner_url       VARCHAR(1024) NULL,
    background_type         VARCHAR(32)   NOT NULL DEFAULT 'color',
    background_value        VARCHAR(512)  NOT NULL DEFAULT '#0f0f13cc',
    badge_slot_count        INT           NOT NULL DEFAULT 4,
    default_glow_enabled    BOOLEAN       NOT NULL DEFAULT FALSE,
    default_name_color      VARCHAR(16)   NOT NULL DEFAULT '#FFFFFF',
    font_family              VARCHAR(512) NOT NULL DEFAULT '''Inter'', ''Noto Sans Thai'', sans-serif',
    max_messages            INT           NOT NULL DEFAULT 60,
    banner_display_seconds  INT           NOT NULL DEFAULT 6,
    user_banners_enabled    BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_overlay_config_streamer FOREIGN KEY (streamer_id) REFERENCES streamers(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Badge / banner libraries - owned by one streamer each. Visibility for
-- partners is a query (see example near the bottom), not a schema flag.
-- ---------------------------------------------------------------------
CREATE TABLE badge_library (
    id                VARCHAR(128) PRIMARY KEY,
    owner_streamer_id BIGINT        NOT NULL,
    label             VARCHAR(256)  NULL,
    image_url         VARCHAR(1024) NULL,
    sort_order        BIGINT        NOT NULL DEFAULT 0,
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_badge_library_streamer FOREIGN KEY (owner_streamer_id) REFERENCES streamers(id) ON DELETE CASCADE,
    KEY idx_badge_library_owner_sort (owner_streamer_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE banner_library (
    id                VARCHAR(128) PRIMARY KEY,
    owner_streamer_id BIGINT        NOT NULL,
    label             VARCHAR(256)  NULL,
    image_url         VARCHAR(1024) NULL,
    sort_order        BIGINT        NOT NULL DEFAULT 0,
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_banner_library_streamer FOREIGN KEY (owner_streamer_id) REFERENCES streamers(id) ON DELETE CASCADE,
    KEY idx_banner_library_owner_sort (owner_streamer_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Personal chat profiles: one streamer's own appearance settings for a
-- given chatter (channel_id). Same shape as before, now scoped.
-- ---------------------------------------------------------------------
CREATE TABLE user_profiles (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_streamer_id BIGINT        NOT NULL,
    channel_id        VARCHAR(128)  NOT NULL,
    display_name      VARCHAR(256)  NULL,
    name_color        VARCHAR(16)   NOT NULL DEFAULT '#FFFFFF',
    glow_enabled      BOOLEAN       NOT NULL DEFAULT FALSE,
    glow_color        VARCHAR(16)   NOT NULL DEFAULT '#7B61FF',
    glow_size         INT           NOT NULL DEFAULT 10,
    banner_image_url  VARCHAR(1024) NULL,
    banner_enabled    BOOLEAN       NOT NULL DEFAULT FALSE,
    banner_message    VARCHAR(2048) NULL,
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_profiles_streamer FOREIGN KEY (owner_streamer_id) REFERENCES streamers(id) ON DELETE CASCADE,
    UNIQUE KEY uq_user_profiles_owner_channel (owner_streamer_id, channel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_profile_badges (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_profile_id BIGINT        NOT NULL,
    position        INT           NOT NULL,
    badge_id        VARCHAR(128)  NOT NULL,
    label           VARCHAR(256)  NULL,
    image_url       VARCHAR(1024) NULL,
    CONSTRAINT fk_upb_profile FOREIGN KEY (user_profile_id) REFERENCES user_profiles(id) ON DELETE CASCADE,
    UNIQUE KEY uq_upb_profile_position (user_profile_id, position)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Shared "partnership profile" (the simpler alternative you mentioned):
-- one appearance record per (partnership, chatter), used only when both
-- partnered streamers want the same look for that chatter instead of
-- each configuring their own. Independent of user_profiles above - a
-- chatter can have a personal profile on each side AND a shared one; it's
-- up to the app (not the schema) which one wins when both exist.
-- ---------------------------------------------------------------------
CREATE TABLE partnership_user_profiles (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    partnership_id   BIGINT        NOT NULL,
    channel_id       VARCHAR(128)  NOT NULL,
    display_name     VARCHAR(256)  NULL,
    name_color       VARCHAR(16)   NOT NULL DEFAULT '#FFFFFF',
    glow_enabled     BOOLEAN       NOT NULL DEFAULT FALSE,
    glow_color       VARCHAR(16)   NOT NULL DEFAULT '#7B61FF',
    glow_size        INT           NOT NULL DEFAULT 10,
    banner_image_url VARCHAR(1024) NULL,
    banner_enabled   BOOLEAN       NOT NULL DEFAULT FALSE,
    banner_message   VARCHAR(2048) NULL,
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_partner_profiles_partnership FOREIGN KEY (partnership_id) REFERENCES partnerships(id) ON DELETE CASCADE,
    UNIQUE KEY uq_partner_profiles_partnership_channel (partnership_id, channel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE partnership_user_profile_badges (
    id                           BIGINT AUTO_INCREMENT PRIMARY KEY,
    partnership_user_profile_id BIGINT        NOT NULL,
    position                     INT           NOT NULL,
    badge_id                     VARCHAR(128)  NOT NULL,
    label                        VARCHAR(256)  NULL,
    image_url                    VARCHAR(1024) NULL,
    CONSTRAINT fk_ppub_profile FOREIGN KEY (partnership_user_profile_id) REFERENCES partnership_user_profiles(id) ON DELETE CASCADE,
    UNIQUE KEY uq_ppub_profile_position (partnership_user_profile_id, position)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- Example queries the app-layer would use (not run automatically)
-- =====================================================================

-- All badges a streamer (:me) can pick from - their own + accepted
-- partners':
-- SELECT bl.*
-- FROM badge_library bl
-- WHERE bl.owner_streamer_id = :me
--    OR bl.owner_streamer_id IN (
--         SELECT CASE WHEN requester_streamer_id = :me THEN addressee_streamer_id
--                     ELSE requester_streamer_id END
--         FROM partnerships
--         WHERE status = 'accepted'
--           AND (requester_streamer_id = :me OR addressee_streamer_id = :me)
--       );
-- (banner_library follows the identical pattern.)

-- Effective chat profile for a chatter on streamer :me's overlay - prefer
-- a personal profile, otherwise fall back to a shared partnership one:
-- SELECT * FROM user_profiles WHERE owner_streamer_id = :me AND channel_id = :channelId
-- UNION ALL
-- SELECT pup.* FROM partnership_user_profiles pup
-- JOIN partnerships p ON p.id = pup.partnership_id AND p.status = 'accepted'
-- WHERE (p.requester_streamer_id = :me OR p.addressee_streamer_id = :me)
--   AND pup.channel_id = :channelId
-- LIMIT 1;
