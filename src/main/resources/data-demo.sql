-- ═══════════════════════════════════════════════════════════
-- PRIMEVIDEO CLONE — Données de démonstration
-- À exécuter dans phpMyAdmin après le premier démarrage
-- (les tables sont créées automatiquement par Spring Boot)
-- ═══════════════════════════════════════════════════════════

USE primevideo_db;
SET FOREIGN_KEY_CHECKS = 0;

-- Nettoyage complet dans l'ordre inverse des dépendances
DELETE FROM user_backup_codes;
DELETE FROM ratings;
DELETE FROM viewing_history;
DELETE FROM watchlist_items;
DELETE FROM profiles;
DELETE FROM subscriptions;
DELETE FROM payments;
DELETE FROM devices;
DELETE FROM anime_metadata;
DELETE FROM kdrama_metadata;
DELETE FROM webtoon_metadata;
DELETE FROM episodes;
DELETE FROM seasons;
DELETE FROM contents;
DELETE FROM users;

-- Réinitialisation des compteurs d'ID
ALTER TABLE users AUTO_INCREMENT = 1;
ALTER TABLE contents AUTO_INCREMENT = 1;
ALTER TABLE profiles AUTO_INCREMENT = 1;
ALTER TABLE subscriptions AUTO_INCREMENT = 1;
ALTER TABLE ratings AUTO_INCREMENT = 1;
ALTER TABLE seasons AUTO_INCREMENT = 1;
ALTER TABLE episodes AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

-- ── Utilisateurs de test (ID fixés pour les relations) ──────────────────
INSERT INTO users (id, full_name, email, password, role, is_active, email_verified, created_at, updated_at)
VALUES
(1, 'Administrateur', 'admin@primevideo.com',
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
 'ADMIN', true, true, NOW(), NOW()),
(2, 'Jean Dupont', 'user@primevideo.com',
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
 'USER', true, true, NOW(), NOW()),
(3, 'Marie Curie', 'marie@test.com',
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
 'USER', true, false, NOW(), NOW());

-- ── Films ─────────────────────────────────────────────────────────────────
INSERT INTO contents (title, description, type, genre, year, duration_seconds, video_url,
    poster_url, rating_average, rating_count, age_rating, is_premium, is_available,
    director, cast_list, language, created_at, updated_at)
VALUES
('Inception', 'Un voleur qui s''infiltre dans les rêves des gens se voit offrir la chance de retrouver sa vie normale.',
 'FILM', 'SCIENCE_FICTION', 2010, 8880, 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
 'https://image.tmdb.org/t/p/w500/edv5CZvWj09upOsy2Y6IwQwZRV.jpg',
 8.8, 2400000, 12, false, true,
 'Christopher Nolan', 'Leonardo DiCaprio, Joseph Gordon-Levitt, Elliot Page', 'en', NOW(), NOW()),

('The Dark Knight', 'Batman affronte le Joker, un criminel au génie anarchique qui sème le chaos à Gotham City.',
 'FILM', 'ACTION', 2008, 9120,
 'https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg',
 9.0, 2800000, 13, false, true,
 'Christopher Nolan', 'Christian Bale, Heath Ledger, Aaron Eckhart', 'en', NOW(), NOW()),

('Parasite', 'Toute la famille Ki-taek est au chômage et s''intéresse particulièrement au train de vie de la richissime famille Park.',
 'FILM', 'THRILLER', 2019, 8220,
 'https://image.tmdb.org/t/p/w500/7IiTTgloJzvGI1TAYymCfbfl3vT.jpg',
 8.5, 850000, 16, false, true,
 'Bong Joon-ho', 'Song Kang-ho, Lee Sun-kyun, Cho Yeo-jeong', 'ko', NOW(), NOW()),

('Interstellar', 'Dans un futur proche, la Terre est ravagée par une famine mondiale. Un groupe d''astronautes part explorer l''univers.',
 'FILM', 'SCIENCE_FICTION', 2014, 10140,
 'https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MvrIdZ1O.jpg',
 8.6, 1900000, 10, true, true,
 'Christopher Nolan', 'Matthew McConaughey, Anne Hathaway, Jessica Chastain', 'en', NOW(), NOW()),

('Spirited Away', 'La petite Chihiro et ses parents s''égarent dans un parc à thème abandonné. Les parents se transforment en cochons.',
 'FILM', 'ANIMATION', 2001, 7560,
 'https://image.tmdb.org/t/p/w500/39wmItIWsg5sZMyRUHLkBg8lWOb.jpg',
 8.6, 740000, 0, false, true,
 'Hayao Miyazaki', 'Daveigh Chase, Suzanne Pleshette', 'ja', NOW(), NOW()),

('Get Out', 'Un jeune Afro-Américain rend visite à la famille blanche de sa petite amie et découvre une vérité cauchemardesque.',
 'FILM', 'HORREUR', 2017, 6420,
 'https://image.tmdb.org/t/p/w500/tFXcEccSQAmRoIdcgVKOSNfIKM7.jpg', 7.7, 390000, 16, false, true,
 'Jordan Peele', 'Daniel Kaluuya, Allison Williams, Bradley Whitford', 'en', NOW(), NOW()),

('Le Fabuleux Destin d''Amélie Poulain', 'Amélie Poulain est une jeune serveuse parisienne qui décide d''améliorer la vie des autres.',
 'FILM', 'COMEDIE', 2001, 7380,
 'https://image.tmdb.org/t/p/w500/1XvEMnE6m2QO2Q4xT4tXGj8sTeb.jpg', 8.3, 720000, 12, false, true,
 'Jean-Pierre Jeunet', 'Audrey Tautou, Mathieu Kassovitz', 'fr', NOW(), NOW()),

('Black Panther', 'T''Challa retourne dans sa nation de Wakanda pour prendre la place de son père comme roi.',
 'FILM', 'ACTION', 2018, 8040,
 'https://image.tmdb.org/t/p/w500/uxzzxijgPIY7slzFvMotPv8wjKA.jpg', 7.3, 710000, 12, true, true,
 'Ryan Coogler', 'Chadwick Boseman, Michael B. Jordan, Lupita Nyong''o', 'en', NOW(), NOW());

-- ── Séries ────────────────────────────────────────────────────────────────
INSERT INTO contents (title, description, type, genre, year, duration_seconds,
    poster_url, rating_average, rating_count, age_rating, is_premium, is_available,
    director, cast_list, language, created_at, updated_at)
VALUES
('Breaking Bad', 'Un professeur de chimie atteint d''un cancer se transforme en fabricant de drogue.',
 'SERIE', 'THRILLER', 2008, 0,
 'https://image.tmdb.org/t/p/w500/ggFHVNu6YYI5L9pCfOacjizRGt.jpg',
 9.5, 1800000, 16, false, true,
 'Vince Gilligan', 'Bryan Cranston, Aaron Paul, Anna Gunn', 'en', NOW(), NOW()),

('Squid Game', 'Des centaines de personnes endettées participent à des jeux d''enfants aux enjeux mortels.',
 'SERIE', 'THRILLER', 2021, 0,
 'https://image.tmdb.org/t/p/w500/dDlEmu3EZ0PggZzD0s1qJkYwIYi.jpg',
 8.0, 360000, 16, true, true,
 'Hwang Dong-hyuk', 'Lee Jung-jae, Park Hae-soo, Oh Young-soo', 'ko', NOW(), NOW()),

('Stranger Things', 'Dans une ville de l''Indiana, la disparition d''un enfant révèle des événements surnaturels.',
 'SERIE', 'SCIENCE_FICTION', 2016, 0, '/media/stranger_things_s1e1.mp4',
 'https://image.tmdb.org/t/p/w500/49WJfeN0moxb9IPfGn8IFqDzw0a.jpg', 8.7, 1100000, 12, false, true,
 'Duffer Brothers', 'Millie Bobby Brown, Finn Wolfhard, Winona Ryder', 'en', NOW(), NOW());

-- ── Animés ────────────────────────────────────────────────────────────────
INSERT INTO contents (title, description, type, genre, year, duration_seconds,
    poster_url, rating_average, rating_count, age_rating, is_premium, is_available,
    director, cast_list, language, created_at, updated_at)
VALUES
('Attack on Titan', 'Dans un monde où l''humanité vit derrière de grands murs pour se protéger des Titans.',
 'ANIME', 'ACTION', 2013, 0,
 'https://image.tmdb.org/t/p/w500/hTP1DtLGFamjfu8WqjnuQdP1n4i.jpg', 9.0, 500000, 16, true, true,
 'Tetsuro Araki', 'Yuki Kaji, Yui Ishikawa, Marina Inoue', 'ja', NOW(), NOW()),

('Demon Slayer', 'Tanjiro Kamado devient chasseur de démons après que sa famille a été massacrée.',
 'ANIME', 'ACTION', 2019, 0,
 'https://image.tmdb.org/t/p/w500/xUfRZu2mi8jH6SnDTRIK1fHL6ZV.jpg', 8.7, 420000, 13, false, true,
 'Haruo Sotozaki', 'Natsuki Hanae, Akari Kito', 'ja', NOW(), NOW()),

('Your Name', 'Deux lycéens découvrent qu''ils se transforment mystérieusement l''un en l''autre.',
 'FILM', 'ANIMATION', 2016, 6480,
 'https://image.tmdb.org/t/p/w500/aB7X1B72xusf4sHlQy33W3XqLwz.jpg', 8.4, 310000, 0, false, true,
 'Makoto Shinkai', 'Ryunosuke Kamiki, Mone Kamishiraishi', 'ja', NOW(), NOW()),

('Your Lie in April', 'Un prodige du piano perd sa capacité à entendre le son de son propre piano après la mort de sa mère.',
 'ANIME', 'ROMANCE', 2014, 0,
 'https://image.tmdb.org/t/p/w500/98Xf68j94e1uK6nNfX2Z05p2dK4gG5H11fB9M0.jpg', 8.9, 150000, 10, true, true,
 'Kyohei Ishiguro', 'Natsuki Hanae, Risa Taneda', 'ja', NOW(), NOW());

-- ── K-Dramas ──────────────────────────────────────────────────────────────
INSERT INTO contents (title, description, type, genre, year, duration_seconds,
    poster_url, rating_average, rating_count, age_rating, is_premium, is_available,
    director, cast_list, language, created_at, updated_at)
VALUES
('Goblin', 'Un guerrier coréen immortel cherche sa fiancée, seule capable de mettre fin à sa vie éternelle.',
 'KDRAMA', 'FANTAISIE', 2016, 0,
 'https://image.tmdb.org/t/p/w500/3o5EAnL27kInWnSt8zI4G5o7rS8.jpg', 8.7, 280000, 12, false, true,
 'Lee Eung-bok', 'Gong Yoo, Kim Go-eun, Lee Dong-wook', 'ko', NOW(), NOW()),

('Crash Landing on You', 'Une héritière sud-coréenne atterrit accidentellement en Corée du Nord après un accident de parapente.',
 'KDRAMA', 'ROMANCE', 2019, 0,
 'https://image.tmdb.org/t/p/w500/uF6nNfX2Z05p2dK4gG5H11fB9M0.jpg', 8.7, 310000, 12, true, true,
 'Lee Jeong-hyo', 'Hyun Bin, Son Ye-jin', 'ko', NOW(), NOW());

-- ── Abonnements pour les utilisateurs de test ─────────────────────────────
INSERT INTO subscriptions (user_id, plan, status, start_date, end_date, price_amount, price_currency, auto_renew, created_at)
VALUES
(2, 'PRIME_MONTHLY', 'ACTIVE', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 MONTH), 13.99, 'EUR', true, NOW());

-- ── Profils (ID fixés pour les relations) ─────────────────────────────────
INSERT INTO profiles (user_id, name, is_kids, age_rating, language, created_at)
VALUES
(2, 'Jean', false, 'R_18', 'fr', NOW()),
(2, 'Kids', true, 'PG', 'fr', NOW()),
(3, 'Marie', false, 'R_18', 'fr', NOW());

SELECT 'Données de démonstration insérées avec succès !' as message;
SELECT COUNT(*) as nb_contenus FROM contents;
SELECT COUNT(*) as nb_utilisateurs FROM users;
