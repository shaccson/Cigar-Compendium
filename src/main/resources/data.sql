-- Users
INSERT INTO users (username, email, password, role)
SELECT 'jack','jack@example.com',
       '$2a$10$QfE20A6p7m8k6a5Y2k9K9u9c6yZq0hQeFQhQvT.R5iRZ2n1oNQZz2',
       'ROLE_USER'
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE username='jack');

INSERT INTO users (username, email, password, role)
SELECT 'john','john@example.com',
       '$2a$10$QfE20A6p7m8k6a5Y2k9K9u9c6yZq0hQeFQhQvT.R5iRZ2n1oNQZz2',
       'ROLE_USER'
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE username='john');

-- Cigars
INSERT INTO cigar (name, brand, origin, strength, flavor)
SELECT 'Cohiba Robusto','Cohiba','CUBA','FULL','Earthy, Leather'
    WHERE NOT EXISTS (SELECT 1 FROM cigar WHERE name='Cohiba Robusto');

INSERT INTO cigar (name, brand, origin, strength, flavor)
SELECT 'Montecristo No. 2','Montecristo','CUBA','MEDIUM_FULL','Spicy, Cedar'
    WHERE NOT EXISTS (SELECT 1 FROM cigar WHERE name='Montecristo No. 2');

INSERT INTO cigar (name, brand, origin, strength, flavor)
SELECT 'Oliva Serie V','Oliva','NICARAGUA','MEDIUM_FULL','Coffee, Chocolate'
    WHERE NOT EXISTS (SELECT 1 FROM cigar WHERE name='Oliva Serie V');

-- Reviews
INSERT INTO review (rating, comment, user_id, cigar_id)
SELECT 5,'Perfect draw and rich flavor.',
       (SELECT id FROM users WHERE username='john'),
       (SELECT id FROM cigar WHERE name='Oliva Serie V')
    WHERE NOT EXISTS (
  SELECT 1 FROM review r
  WHERE r.user_id = (SELECT id FROM users WHERE username='john')
    AND r.cigar_id = (SELECT id FROM cigar WHERE name='Oliva Serie V')
);

INSERT INTO review (rating, comment, user_id, cigar_id)
SELECT 4,'Strong and complex.',
       (SELECT id FROM users WHERE username='jack'),
       (SELECT id FROM cigar WHERE name='Cohiba Robusto')
    WHERE NOT EXISTS (
  SELECT 1 FROM review r
  WHERE r.user_id = (SELECT id FROM users WHERE username='jack')
    AND r.cigar_id = (SELECT id FROM cigar WHERE name='Cohiba Robusto')
);
