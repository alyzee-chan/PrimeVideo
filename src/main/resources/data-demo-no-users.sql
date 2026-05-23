USE primevideo_db;

-- ── Films ─────────────────────────────────────────────────────────────────
INSERT INTO contents (title, description, type, genre, year, duration_seconds,
    poster_url, rating_average, rating_count, age_rating, is_premium, is_available,
    director, cast_list, language, created_at, updated_at)
VALUES
('Inception', 'Un voleur qui s''infiltre dans les rêves des gens se voit offrir la chance de retrouver sa vie normale.',
 'FILM', 'SCIENCE_FICTION', 2010, 8880,
 'https://upload.wikimedia.org/wikipedia/en/2/2e/Inception_%282010%29_theatrical_poster.jpg',
 8.8, 2400000, 12, false, true,
 'Christopher Nolan', 'Leonardo DiCaprio, Joseph Gordon-Levitt, Elliot Page', 'en', NOW(), NOW()),

('The Dark Knight', 'Batman affronte le Joker, un criminel au génie anarchique qui sème le chaos à Gotham City.',
 'FILM', 'ACTION', 2008, 9120,
 'https://upload.wikimedia.org/wikipedia/en/1/1c/The_Dark_Knight_%282008_film%29.jpg',
 9.0, 2800000, 13, false, true,
 'Christopher Nolan', 'Christian Bale, Heath Ledger, Aaron Eckhart', 'en', NOW(), NOW()),

('Parasite', 'Toute la famille Ki-taek est au chômage et s''intéresse particulièrement au train de vie de la richissime famille Park.',
 'FILM', 'THRILLER', 2019, 8220,
 'https://upload.wikimedia.org/wikipedia/en/5/53/Parasite_%282019_film%29.png',
 8.5, 850000, 16, false, true,
 'Bong Joon-ho', 'Song Kang-ho, Lee Sun-kyun, Cho Yeo-jeong', 'ko', NOW(), NOW()),

('Interstellar', 'Dans un futur proche, la Terre est ravagée par une famine mondiale. Un groupe d''astronautes part explorer l''univers.',
 'FILM', 'SCIENCE_FICTION', 2014, 10140,
 'https://upload.wikimedia.org/wikipedia/en/b/bc/Interstellar_film_poster.jpg',
 8.6, 1900000, 10, true, true,
 'Christopher Nolan', 'Matthew McConaughey, Anne Hathaway, Jessica Chastain', 'en', NOW(), NOW()),

('Spirited Away', 'La petite Chihiro et ses parents s''égarent dans un parc à thème abandonné. Les parents se transforment en cochons.',
 'FILM', 'ANIMATION', 2001, 7560,
 'https://upload.wikimedia.org/wikipedia/en/d/db/Spirited_Away_Japanese_poster.png',
 8.6, 740000, 0, false, true,
 'Hayao Miyazaki', 'Daveigh Chase, Suzanne Pleshette', 'ja', NOW(), NOW()),

('Get Out', 'Un jeune Afro-Américain rend visite à la famille blanche de sa petite amie et découvre une vérité cauchemardesque.',
 'FILM', 'HORREUR', 2017, 6420,
 NULL, 7.7, 390000, 16, false, true,
 'Jordan Peele', 'Daniel Kaluuya, Allison Williams', 'en', NOW(), NOW()),

('Le Fabuleux Destin d''Amélie Poulain', 'Amélie Poulain est une jeune serveuse parisienne qui décide d''améliorer la vie des autres.',
 'FILM', 'COMEDIE', 2001, 7380,
 NULL, 8.3, 720000, 12, false, true,
 'Jean-Pierre Jeunet', 'Audrey Tautou, Mathieu Kassovitz', 'fr', NOW(), NOW()),

('Black Panther', 'T''Challa retourne dans sa nation de Wakanda pour prendre la place de son père comme roi.',
 'FILM', 'ACTION', 2018, 8040,
 NULL, 7.3, 710000, 12, true, true,
 'Ryan Coogler', 'Chadwick Boseman, Michael B. Jordan', 'en', NOW(), NOW());

-- ── Séries ────────────────────────────────────────────────────────────────
INSERT INTO contents (title, description, type, genre, year, duration_seconds,
    poster_url, rating_average, rating_count, age_rating, is_premium, is_available,
    director, cast_list, language, created_at, updated_at)
VALUES
('Breaking Bad', 'Un professeur de chimie atteint d''un cancer se transforme en fabricant de drogue.',
 'SERIE', 'THRILLER', 2008, 0,
 'https://upload.wikimedia.org/wikipedia/en/6/61/Breaking_Bad_title_card.png',
 9.5, 1800000, 16, false, true,
 'Vince Gilligan', 'Bryan Cranston, Aaron Paul, Anna Gunn', 'en', NOW(), NOW()),

('Squid Game', 'Des centaines de personnes endettées participent à des jeux d''enfants aux enjeux mortels.',
 'SERIE', 'THRILLER', 2021, 0,
 'https://upload.wikimedia.org/wikipedia/en/d/d9/Squid_Game_poster.png',
 8.0, 360000, 16, true, true,
 'Hwang Dong-hyuk', 'Lee Jung-jae, Park Hae-soo, Oh Young-soo', 'ko', NOW(), NOW()),

('Stranger Things', 'Dans une ville de l''Indiana, la disparition d''un enfant révèle des événements surnaturels.',
 'SERIE', 'SCIENCE_FICTION', 2016, 0,
 NULL, 8.7, 1100000, 12, false, true,
 'Duffer Brothers', 'Millie Bobby Brown, Finn Wolfhard', 'en', NOW(), NOW());

-- ── Animés ────────────────────────────────────────────────────────────────
INSERT INTO contents (title, description, type, genre, year, duration_seconds,
    poster_url, rating_average, rating_count, age_rating, is_premium, is_available,
    director, cast_list, language, created_at, updated_at)
VALUES
('Attack on Titan', 'Dans un monde où l''humanité vit derrière de grands murs pour se protéger des Titans.',
 'ANIME', 'ACTION', 2013, 0,
 NULL, 9.0, 500000, 16, true, true,
 'Tetsuro Araki', 'Yuki Kaji, Yui Ishikawa', 'ja', NOW(), NOW()),

('Demon Slayer', 'Tanjiro Kamado devient chasseur de démons après que sa famille a été massacrée.',
 'ANIME', 'ACTION', 2019, 0,
 NULL, 8.7, 420000, 13, false, true,
 'Haruo Sotozaki', 'Natsuki Hanae, Akari Kito', 'ja', NOW(), NOW()),

('Your Name', 'Deux lycéens découvrent qu''ils se transforment mystérieusement l''un en l''autre.',
 'FILM', 'ANIMATION', 2016, 6480,
 NULL, 8.4, 310000, 0, false, true,
 'Makoto Shinkai', 'Ryunosuke Kamiki, Mone Kamishiraishi', 'ja', NOW(), NOW());

-- ── K-Dramas ──────────────────────────────────────────────────────────────
INSERT INTO contents (title, description, type, genre, year, duration_seconds,
    poster_url, rating_average, rating_count, age_rating, is_premium, is_available,
    director, cast_list, language, created_at, updated_at)
VALUES
('Goblin', 'Un guerrier coréen immortel cherche sa fiancée, seule capable de mettre fin à sa vie éternelle.',
 'KDRAMA', 'FANTAISIE', 2016, 0,
 NULL, 8.7, 280000, 12, false, true,
 'Lee Eung-bok', 'Gong Yoo, Kim Go-eun', 'ko', NOW(), NOW()),

('Crash Landing on You', 'Une héritière sud-coréenne atterrit accidentellement en Corée du Nord après un accident de parapente.',
 'KDRAMA', 'ROMANCE', 2019, 0,
 NULL, 8.7, 310000, 12, true, true,
 'Lee Jeong-hyo', 'Hyun Bin, Son Ye-jin', 'ko', NOW(), NOW());

-- ── Abonnements pour les utilisateurs de test ─────────────────────────────
-- On suppose que l'utilisateur ID 2 existe (créé par le DataInitializer)
INSERT INTO subscriptions (user_id, plan, status, start_date, end_date, price_amount, price_currency, auto_renew, created_at)
VALUES
(2, 'PREMIUM', 'ACTIVE', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 MONTH), 13.99, 'EUR', true, NOW());

-- ── Profils ───────────────────────────────────────────────────────────────
INSERT INTO profiles (user_id, name, is_kids, max_age_rating, language_preference, created_at)
VALUES
(2, 'Jean', false, 18, 'fr', NOW()),
(2, 'Kids', true, 10, 'fr', NOW());
