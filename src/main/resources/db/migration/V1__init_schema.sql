-- ============================================================
-- V1: Initial Schema - Quiz Application
-- ============================================================

-- ADMINS
CREATE TABLE admins (
    id                   BIGSERIAL PRIMARY KEY,
    email                VARCHAR(255) NOT NULL UNIQUE,
    password             VARCHAR(255) NOT NULL,
    full_name            VARCHAR(255) NOT NULL,
    must_change_password BOOLEAN NOT NULL DEFAULT TRUE,
    active               BOOLEAN NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP NOT NULL DEFAULT NOW()
);

-- PASSWORD RESET TOKENS
CREATE TABLE password_reset_tokens (
    id         BIGSERIAL PRIMARY KEY,
    admin_id   BIGINT       NOT NULL REFERENCES admins(id) ON DELETE CASCADE,
    token      VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP    NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_prt_token ON password_reset_tokens(token);

-- CATEGORIES
CREATE TABLE categories (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    description    TEXT,
    price          NUMERIC(10,2) NOT NULL DEFAULT 0,
    validity_days  INT          NOT NULL DEFAULT 30,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_categories_active ON categories(active, deleted);

-- SUBCATEGORIES
CREATE TABLE subcategories (
    id          BIGSERIAL PRIMARY KEY,
    category_id BIGINT       NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_subcategories_category ON subcategories(category_id, deleted);

-- QUIZZES
CREATE TABLE quizzes (
    id             BIGSERIAL PRIMARY KEY,
    subcategory_id BIGINT       NOT NULL REFERENCES subcategories(id) ON DELETE CASCADE,
    title          VARCHAR(500) NOT NULL,
    description    TEXT,
    is_free        BOOLEAN      NOT NULL DEFAULT FALSE,
    pass_mark      INT          NOT NULL DEFAULT 60,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_quizzes_subcategory ON quizzes(subcategory_id, deleted);
CREATE INDEX idx_quizzes_free ON quizzes(is_free, active);
CREATE INDEX idx_quizzes_title ON quizzes USING gin (to_tsvector('english', title));

-- QUESTIONS
CREATE TABLE questions (
    id              BIGSERIAL PRIMARY KEY,
    quiz_id         BIGINT      NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    question_text   TEXT        NOT NULL,
    question_type   VARCHAR(20) NOT NULL CHECK (question_type IN ('SINGLE','MULTIPLE')),
    order_index     INT         NOT NULL DEFAULT 0,
    deleted         BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP   NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_questions_quiz ON questions(quiz_id, deleted);

-- QUESTION OPTIONS
CREATE TABLE question_options (
    id          BIGSERIAL PRIMARY KEY,
    question_id BIGINT      NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    option_text TEXT        NOT NULL,
    is_correct  BOOLEAN     NOT NULL DEFAULT FALSE,
    order_index INT         NOT NULL DEFAULT 0
);
CREATE INDEX idx_options_question ON question_options(question_id);

-- QUIZ ATTEMPTS
CREATE TABLE quiz_attempts (
    id                BIGSERIAL PRIMARY KEY,
    quiz_id           BIGINT      NOT NULL REFERENCES quizzes(id),
    user_email        VARCHAR(255),
    user_mobile       VARCHAR(20),
    device_id         VARCHAR(255),
    access_code_used  VARCHAR(20),
    status            VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS' CHECK (status IN ('IN_PROGRESS','COMPLETED')),
    current_index     INT         NOT NULL DEFAULT 0,
    total_questions   INT,
    correct_count     INT,
    incorrect_count   INT,
    score_percent     NUMERIC(5,2),
    passed            BOOLEAN,
    started_at        TIMESTAMP   NOT NULL DEFAULT NOW(),
    completed_at      TIMESTAMP,
    created_at        TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP   NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_attempts_quiz ON quiz_attempts(quiz_id);
CREATE INDEX idx_attempts_device ON quiz_attempts(device_id);

-- ATTEMPT ANSWERS
CREATE TABLE attempt_answers (
    id               BIGSERIAL PRIMARY KEY,
    attempt_id       BIGINT  NOT NULL REFERENCES quiz_attempts(id) ON DELETE CASCADE,
    question_id      BIGINT  NOT NULL REFERENCES questions(id),
    selected_options BIGINT[] NOT NULL DEFAULT '{}',
    is_correct       BOOLEAN NOT NULL DEFAULT FALSE,
    answered_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_answers_attempt ON attempt_answers(attempt_id);

-- PAYMENTS
CREATE TABLE payments (
    id              BIGSERIAL PRIMARY KEY,
    category_id     BIGINT       NOT NULL REFERENCES categories(id),
    user_name       VARCHAR(255) NOT NULL,
    user_email      VARCHAR(255) NOT NULL,
    user_mobile     VARCHAR(20)  NOT NULL,
    payment_slip_url VARCHAR(500),
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','APPROVED','REJECTED')),
    reject_reason   TEXT,
    reviewed_at     TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_email ON payments(user_email);

-- ACCESS CODES
CREATE TABLE access_codes (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(20)  NOT NULL UNIQUE,
    payment_id      BIGINT       NOT NULL REFERENCES payments(id),
    category_id     BIGINT       NOT NULL REFERENCES categories(id),
    user_name       VARCHAR(255) NOT NULL,
    user_email      VARCHAR(255) NOT NULL,
    user_mobile     VARCHAR(20)  NOT NULL,
    bound_device_id VARCHAR(255),
    expires_at      TIMESTAMP    NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_access_codes_code ON access_codes(code);
CREATE INDEX idx_access_codes_email ON access_codes(user_email);
CREATE INDEX idx_access_codes_expiry ON access_codes(expires_at, active);

-- ACCESS CODE USAGE LOGS
CREATE TABLE access_code_usages (
    id             BIGSERIAL PRIMARY KEY,
    access_code_id BIGINT      NOT NULL REFERENCES access_codes(id) ON DELETE CASCADE,
    device_id      VARCHAR(255),
    user_email     VARCHAR(255),
    user_mobile    VARCHAR(20),
    action         VARCHAR(50) NOT NULL,
    ip_address     VARCHAR(50),
    used_at        TIMESTAMP   NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_usages_code ON access_code_usages(access_code_id);
