INSERT INTO users (username, password)
VALUES ('testuser', '$2a$10$vfD5MjzkGnkIt6gyvb0c/.wD5JcArFYMQjyGsv7zmg0zGZiu7jsm6')
ON CONFLICT (username) DO NOTHING;
