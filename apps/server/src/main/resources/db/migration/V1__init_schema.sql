CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    openid VARCHAR(128) NOT NULL,
    unionid VARCHAR(128),
    nickname VARCHAR(64),
    avatar_url VARCHAR(512),
    status VARCHAR(32) NOT NULL,
    last_login_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT uk_users_openid UNIQUE (openid)
);

CREATE TABLE task_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    color VARCHAR(32),
    sort_no INT NOT NULL DEFAULT 0,
    is_default BIT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,
    CONSTRAINT fk_task_categories_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    category_id BIGINT,
    title VARCHAR(128) NOT NULL,
    description VARCHAR(1024),
    priority VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    due_at DATETIME,
    remind_at DATETIME,
    repeat_type VARCHAR(32) NOT NULL,
    completed_at DATETIME,
    deleted_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_tasks_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_tasks_category FOREIGN KEY (category_id) REFERENCES task_categories(id)
);

CREATE TABLE task_reminder_jobs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    planned_at DATETIME NOT NULL,
    status VARCHAR(32) NOT NULL,
    sent_at DATETIME,
    fail_reason VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_reminder_jobs_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_reminder_jobs_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE user_subscriptions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    scene VARCHAR(64) NOT NULL,
    template_id VARCHAR(128),
    accept_status VARCHAR(32) NOT NULL,
    expired_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_user_subscriptions_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_task_categories_user_deleted ON task_categories(user_id, deleted_at);
CREATE INDEX idx_tasks_user_deleted_status ON tasks(user_id, deleted_at, status);
CREATE INDEX idx_tasks_due_at ON tasks(due_at);
CREATE INDEX idx_tasks_deleted_at ON tasks(deleted_at);
CREATE INDEX idx_reminder_jobs_status_planned_at ON task_reminder_jobs(status, planned_at);
CREATE INDEX idx_user_subscriptions_user_scene ON user_subscriptions(user_id, scene);
