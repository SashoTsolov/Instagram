CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  email VARCHAR(50) NOT NULL UNIQUE,
  phone_number VARCHAR(30),
  name VARCHAR(50),
  bio VARCHAR(255),
  date_of_birth DATE NOT NULL,
  gender VARCHAR(1),
  profile_picture_url VARCHAR(255),
  is_verified TINYINT NOT NULL,
  verification_code VARCHAR(100) NOT NULL UNIQUE,
  date_time_created TIMESTAMP NOT NULL,
  is_deactivated TINYINT NOT NULL
);

CREATE TABLE followers (
  following_user_id BIGINT,
  followed_user_id BIGINT,
  date_time_of_follow TIMESTAMP NOT NULL,
  PRIMARY KEY (following_user_id, followed_user_id),
  FOREIGN KEY (following_user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (followed_user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE locations (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL
);

CREATE TABLE post_types (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE posts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  owner_id BIGINT NOT NULL,
  location_id BIGINT,
  post_type_id BIGINT NOT NULL,
  caption TEXT,
  date_time_created TIMESTAMP NOT NULL,
  FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE CASCADE,
  FOREIGN KEY (post_type_id) REFERENCES post_types(id) ON DELETE CASCADE
);

CREATE TABLE media (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  media_url VARCHAR(255) NOT NULL,
  post_id BIGINT NOT NULL,
  FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

CREATE TABLE users_like_posts (
  user_id BIGINT,
  post_id BIGINT,
  PRIMARY KEY (user_id, post_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

CREATE TABLE hashtags (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL
);

CREATE TABLE posts_have_hashtags (
  hashtag_id BIGINT,
  post_id BIGINT,
  PRIMARY KEY (hashtag_id, post_id),
  FOREIGN KEY (hashtag_id) REFERENCES hashtags(id) ON DELETE CASCADE,
  FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

CREATE TABLE users_have_tagged_posts (
  user_id BIGINT,
  post_id BIGINT,
  PRIMARY KEY (user_id, post_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

CREATE TABLE comments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  owner_id BIGINT NOT NULL,
  parent_id BIGINT,
  content VARCHAR(2000) NOT NULL,
  date_time_created TIMESTAMP NOT NULL,
  FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
  FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE
);

CREATE TABLE comments_have_likes (
  comment_id BIGINT,
  user_id BIGINT,
  PRIMARY KEY (comment_id, user_id),
  FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE users_save_posts (
  user_id BIGINT,
  post_id BIGINT,
  PRIMARY KEY (user_id, post_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

CREATE TABLE users_block_users (
  blocking_user_id BIGINT,
  blocked_user_id BIGINT,
  PRIMARY KEY (blocking_user_id, blocked_user_id),
  FOREIGN KEY (blocking_user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (blocked_user_id) REFERENCES users(id) ON DELETE CASCADE
);