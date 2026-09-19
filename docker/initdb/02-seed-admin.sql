-- Demo superadmin for the Docker setup.
-- Username: admin    Password: DemoAdmin1!
-- The password below is a bcrypt ($2a$, cost 12) hash, the same format your app checks.
INSERT INTO admins (username, password, name, role, must_change_password)
VALUES ('admin', '$2a$12$14W6/lLpmO81.dPHGoGxQOTFiM7r4rWj0iOUY6ZCgc2zbO8pIl4Me', 'Demo Superadmin', 'superadmin', FALSE);