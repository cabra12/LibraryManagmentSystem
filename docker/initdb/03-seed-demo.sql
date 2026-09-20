-- Demo data for the Docker setup only.
-- Every demo member logs in with the same password: DemoMember1!
-- Loan dates are relative to the day the database is created, so the overdue loans stay overdue.

-- ---------- members ----------
-- demo@example.com must stay first: demo.sh borrows and returns books as this member.
INSERT INTO members (name, email, password, must_change_password) VALUES
  ('Demo Member',   'demo@example.com',  '$2a$12$kYMUdC.GzWdpg0pA3bZ7/uPv9j9iH7hnFy4o8KM2Vb.yCrN/qwroG', FALSE),
  ('Alice Johnson', 'alice@example.com', '$2a$12$kYMUdC.GzWdpg0pA3bZ7/uPv9j9iH7hnFy4o8KM2Vb.yCrN/qwroG', FALSE),
  ('Bob Smith',     'bob@example.com',   '$2a$12$kYMUdC.GzWdpg0pA3bZ7/uPv9j9iH7hnFy4o8KM2Vb.yCrN/qwroG', FALSE),
  ('Carla Nguyen',  'carla@example.com', '$2a$12$kYMUdC.GzWdpg0pA3bZ7/uPv9j9iH7hnFy4o8KM2Vb.yCrN/qwroG', FALSE),
  ('Dev Patel',     'dev@example.com',   '$2a$12$kYMUdC.GzWdpg0pA3bZ7/uPv9j9iH7hnFy4o8KM2Vb.yCrN/qwroG', FALSE);

-- ---------- books ----------
-- Dune must stay first (id 1): demo.sh borrows book 1.
-- available_copies is corrected below, once the loans exist.
INSERT INTO books (title, author, isbn, total_copies, available_copies) VALUES
  ('Dune',                    'Frank Herbert',     '978-0441013593', 3, 3),
  ('Neuromancer',             'William Gibson',    '978-0441569595', 2, 2),
  ('Snow Crash',              'Neal Stephenson',   '978-0553380958', 1, 1),
  ('The Hobbit',              'J.R.R. Tolkien',    '978-0547928227', 2, 2),
  ('1984',                    'George Orwell',     '978-0451524935', 3, 3),
  ('The Great Gatsby',        'F. Scott Fitzgerald', '978-0743273565', 2, 2),
  ('Pride and Prejudice',     'Jane Austen',       '978-0141439518', 2, 2),
  ('To Kill a Mockingbird',   'Harper Lee',        '978-0061120084', 4, 4),
  ('Fahrenheit 451',          'Ray Bradbury',      '978-1451673319', 1, 1),
  ('Brave New World',         'Aldous Huxley',     '978-0060850524', 2, 2);

-- ---------- loans ----------
-- The demo member only has returned loans, so their "my loans" list starts empty.
INSERT INTO borrowed_books (book_id, member_id, borrow_date, due_date, return_date)
SELECT b.id, m.id, l.borrow_date, l.due_date, l.return_date
FROM (VALUES
  -- still checked out, not due yet
  ('alice@example.com', '978-0441569595', CURRENT_DATE - 5,  CURRENT_DATE + 9,  NULL::date),
  ('alice@example.com', '978-0451524935', CURRENT_DATE - 10, CURRENT_DATE + 4,  NULL::date),
  ('carla@example.com', '978-0441013593', CURRENT_DATE - 3,  CURRENT_DATE + 11, NULL::date),
  ('bob@example.com',   '978-0743273565', CURRENT_DATE - 2,  CURRENT_DATE + 12, NULL::date),
  -- still checked out and overdue
  ('bob@example.com',   '978-0553380958', CURRENT_DATE - 20, CURRENT_DATE - 6,  NULL::date),
  ('dev@example.com',   '978-0547928227', CURRENT_DATE - 25, CURRENT_DATE - 11, NULL::date),
  -- already returned (borrowing history)
  ('demo@example.com',  '978-0441013593', CURRENT_DATE - 30, CURRENT_DATE - 16, CURRENT_DATE - 18),
  ('alice@example.com', '978-0553380958', CURRENT_DATE - 40, CURRENT_DATE - 26, CURRENT_DATE - 30),
  ('carla@example.com', '978-0441569595', CURRENT_DATE - 45, CURRENT_DATE - 31, CURRENT_DATE - 25)
) AS l(email, isbn, borrow_date, due_date, return_date)
JOIN members m ON m.email = l.email
JOIN books   b ON b.isbn  = l.isbn;

-- Keep the counts consistent: available copies = total copies minus copies still checked out.
UPDATE books
SET available_copies = total_copies - (
  SELECT COUNT(*) FROM borrowed_books bb
  WHERE bb.book_id = books.id AND bb.return_date IS NULL
);