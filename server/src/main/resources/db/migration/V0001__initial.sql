CREATE TABLE app_user (
    id               BIGINT NOT NULL AUTO_INCREMENT,
    email            VARCHAR(255) NOT NULL,
    email_new        VARCHAR(255) NULL,
    password_hash    VARCHAR(255) NULL,
    authority        VARCHAR(10) NOT NULL,
    enabled          BOOLEAN NOT NULL,
    expired          TIMESTAMP NULL,
    failed_logins    INTEGER,
    locked_out       TIMESTAMP NULL,    
    last_access      TIMESTAMP NULL,
    confirmation_token           CHAR(35),
    confirmation_token_created   TIMESTAMP NULL,
    password_reset_token         CHAR(35),
    password_reset_token_created TIMESTAMP NULL,
    CHECK (authority IN ('USER', 'ADMIN')),
    PRIMARY KEY(id),
    UNIQUE(email)
);

CREATE TABLE app_session (
    id            CHAR(35)  NOT NULL,
    app_user_id   BIGINT    NOT NULL,
    last_access   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip            VARCHAR(39),
    user_agent    VARCHAR(255),
    PRIMARY KEY(id),
    FOREIGN KEY (app_user_id) REFERENCES app_user(id) ON DELETE CASCADE
);

CREATE TABLE todo (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    subject       VARCHAR(255) NOT NULL, 
    description   VARCHAR(255) NULL,
    app_user_id   BIGINT       NOT NULL,
    updated       TIMESTAMP    NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY (app_user_id) REFERENCES app_user(id) ON DELETE CASCADE
);

