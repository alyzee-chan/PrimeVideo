-- ═══════════════════════════════════════════════════════════
-- RESTORE MASTER v17 — LOCAL IMAGES & EXTENDED CATALOG
-- ═══════════════════════════════════════════════════════════

USE primevideo_db;
SET FOREIGN_KEY_CHECKS = 0;

-- 0. Nettoyage Utilisateurs
DELETE FROM user_backup_codes;
DELETE FROM profiles;
DELETE FROM subscriptions;
DELETE FROM payments;
DELETE FROM devices;
DELETE FROM users;

-- 1. Nettoyage Contenu
DELETE FROM episodes;
DELETE FROM seasons;
DELETE FROM anime_metadata;
DELETE FROM kdrama_metadata;
DELETE FROM webtoon_metadata;
DELETE FROM contents;

SET FOREIGN_KEY_CHECKS = 1;

-- ── Utilisateurs de test ──
INSERT INTO users (id, full_name, email, password, role, is_active, email_verified, created_at, updated_at)
VALUES
(1, 'Administrateur', 'admin@primevideo.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'ADMIN', true, true, NOW(), NOW()),
(2, 'Jean Dupont', 'user@primevideo.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'USER', true, true, NOW(), NOW()),
(3, 'Marie Curie', 'marie@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'USER', true, false, NOW(), NOW());

-- Fix pour les nouveaux genres (évite Data Truncated si ENUM)
ALTER TABLE contents MODIFY COLUMN genre VARCHAR(100);

-- 2. CONTENUS (FULL CATALOG WITH LOCAL IMAGES)
INSERT INTO contents (id, title, description, type, genre, year, duration_seconds, video_url, poster_url, banner_url, rating_average, rating_count, age_rating, is_premium, is_available, director, cast_list, language, created_at, updated_at)
VALUES
(1, 'The Boys', 'Des justiciers qui s''attaquent à des super-héros abusant de leurs pouvoirs.', 'SERIE', 'ACTION', 2019, 0, '/media/the_boys_s1e1.mp4', '/images/catalog/theBoys.jpg', '/images/catalog/theBoys.jpg', 8.7, 15000, 18, true, true, 'Eric Kripke', 'Karl Urban, Jack Quaid', 'en', NOW(), NOW()),
(2, 'Stranger Things', 'Une bande d''amis découvre des mystères surnaturels dans leur petite ville.', 'SERIE', 'SCIENCE_FICTION', 2016, 0, '/media/stranger_things_s1e1.mp4', '/images/posters/stranger_things.png', '/images/posters/stranger_things.png', 8.7, 20000, 15, true, true, 'The Duffer Brothers', 'Millie Bobby Brown, Finn Wolfhard', 'en', NOW(), NOW()),
(3, 'Demon Slayer', 'Tanjiro devient un chasseur de démons pour sauver sa sœur.', 'ANIME', 'SHONEN', 2019, 0, '', '/images/catalog/demon_slayer.jpg', '/images/catalog/demon_slayer.jpg', 8.8, 12000, 13, false, true, 'Haruo Sotozaki', 'Natsuki Hanae', 'ja', NOW(), NOW()),
(4, 'The Tomorrow War', 'Des soldats du futur recrutent des civils du présent pour une guerre alien.', 'FILM', 'SCIENCE_FICTION', 2021, 8400, '/media/the_tomorrow_war.avi', '/images/catalog/TomorrowWar.jpg', '/images/catalog/TomorrowWar.jpg', 6.7, 5000, 12, true, true, 'Chris McKay', 'Chris Pratt', 'en', NOW(), NOW()),
(5, 'Inception', 'Un voleur s''infiltre dans les rêves pour implanter une idée.', 'FILM', 'SCIENCE_FICTION', 2010, 8880, 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', '/images/catalog/Inception.jpg', '/images/catalog/Inception.jpg', 8.8, 2400, 12, false, true, 'Christopher Nolan', 'Leonardo DiCaprio', 'en', NOW(), NOW()),
(6, 'The Purge', 'Une nuit par an, tous les crimes sont légaux aux États-Unis.', 'SERIE', 'HORREUR', 2018, 0, '/media/the_purge_s1e1.mkv', '/images/catalog/ThePurge.jpg', '/images/catalog/ThePurge.jpg', 6.4, 6000, 18, true, true, 'James DeMonaco', 'Gabriel Chavarria', 'en', NOW(), NOW()),
(7, 'Gen V', 'La nouvelle génération de super-héros à l''université Godolkin.', 'SERIE', 'ACTION', 2023, 0, '/media/gen_v_s1e1.mp4', '/images/catalog/GenV.jpg', '/images/catalog/GenV.jpg', 7.8, 4000, 18, true, true, 'Michele Fazekas', 'Jaz Sinclair', 'en', NOW(), NOW()),
(8, 'The Banker', 'Deux entrepreneurs noirs luttent contre le racisme dans l''immobilier.', 'FILM', 'DRAME', 2020, 7200, '/media/the_banker.mp4', '/images/posters/amelie.png', '/images/posters/amelie.png', 7.3, 4000, 12, true, true, 'George Nolfi', 'Anthony Mackie', 'en', NOW(), NOW()),
(9, 'Your Name', 'Deux lycéens que tout oppose échangent leurs corps dans leurs rêves.', 'FILM', 'ANIMATION', 2016, 6480, '', '/images/catalog/your_name.jpg', '/images/catalog/your_name.jpg', 8.6, 8000, 0, false, true, 'Makoto Shinkai', 'Radwimps', 'ja', NOW(), NOW()),
(10, 'Squid Game', 'Des participants endettés jouent à des jeux mortels pour de l''argent.', 'SERIE', 'THRILLER', 2021, 0, '', '/images/catalog/squid_game.jpg', '/images/catalog/squid_game.jpg', 8.3, 15000, 16, true, true, 'Hwang Dong-hyuk', 'Lee Jung-jae', 'ko', NOW(), NOW()),
(11, 'Goblin', 'Un gardien immortel cherche sa fiancée pour mettre fin à sa vie.', 'KDRAMA', 'FANTAISIE', 2016, 0, '', '/images/catalog/goblin.jpg', '/images/catalog/goblin.jpg', 8.7, 7000, 12, false, true, 'Lee Eung-bok', 'Gong Yoo', 'ko', NOW(), NOW()),
(12, 'Crash Landing on You', 'Une héritière sud-coréenne atterrit par accident en Corée du Nord.', 'KDRAMA', 'ROMANCE', 2019, 0, '', '/images/catalog/crash_landing_on_you.jpg', '/images/catalog/crash_landing_on_you.jpg', 8.7, 9000, 12, true, true, 'Lee Jeong-hyo', 'Hyun Bin', 'ko', NOW(), NOW()),
(13, 'Black Bullet', 'Dans un futur ravagé par un virus, des enfants maudits luttent pour l''humanité.', 'ANIME', 'SEINEN', 2014, 0, '', '/images/catalog/black_bullet.jpg', '/images/catalog/black_bullet.jpg', 7.5, 3000, 16, false, true, 'Shinji Kojima', 'Yuki Kaji', 'ja', NOW(), NOW()),
(14, 'Darling in the Franxx', 'Des pilotes d''élite luttent contre des monstres dans un futur dystopique.', 'ANIME', 'SHONEN', 2018, 0, '', '/images/catalog/darling_in_the_franxx.jpg', '/images/catalog/darling_in_the_franxx.jpg', 7.9, 5000, 15, true, true, 'Atsushi Nishigori', 'Yuto Uemura', 'ja', NOW(), NOW()),
(15, 'Heartbeat in a Corner', 'Une romance douce et touchante entre deux lycéens timides.', 'ANIME', 'SCHOOL_LIFE', 2022, 0, '', '/images/catalog/Heartbeat_in_a_Corner.jpg', '/images/catalog/Heartbeat_in_a_Corner.jpg', 8.2, 2000, 10, false, true, 'Anime Studio', 'Voice Actor', 'ja', NOW(), NOW()),
(16, 'Hell''s Paradise', 'Un ninja condamné à mort cherche l''élixir d''immortalité sur une île maudite.', 'ANIME', 'SHONEN', 2023, 0, '', '/images/catalog/hell''s_paradise.png', '/images/catalog/hell''s_paradise.png', 8.4, 4500, 16, true, true, 'Kaori Makita', 'Chiaki Kobayashi', 'ja', NOW(), NOW()),
(17, 'Kimi ni Todoke', 'Sawako, une lycéenne incomprise, s''ouvre aux autres grâce à son amitié avec Kazehaya.', 'ANIME', 'SHOJO', 2009, 0, '', '/images/catalog/kimi_ni_todoke.jpg', '/images/catalog/kimi_ni_todoke.jpg', 8.5, 6000, 0, false, true, 'Hiro Kaburaki', 'Mamiko Noto', 'ja', NOW(), NOW()),
(18, 'Lovely Complex', 'L''histoire d''amour hilarante entre une fille très grande et un garçon très petit.', 'ANIME', 'SHOJO', 2007, 0, '', '/images/catalog/LovelyComplex.jpg', '/images/catalog/LovelyComplex.jpg', 8.3, 3500, 0, false, true, 'Konosuke Uda', 'Akemi Okamura', 'ja', NOW(), NOW()),
(19, 'Nisekoi', 'Le fils d''un chef yakuza doit feindre une relation avec la fille d''un gang rival.', 'ANIME', 'SHONEN', 2014, 0, '', '/images/catalog/nisekoi.png', '/images/catalog/nisekoi.png', 7.8, 4000, 12, false, true, 'Akiyuki Shinbo', 'Koki Uchiyama', 'ja', NOW(), NOW()),
(20, 'Skip to Loafer', 'Une fille de la campagne emménage à Tokyo et apporte un vent de fraîcheur à son lycée.', 'ANIME', 'SEINEN', 2023, 0, '', '/images/catalog/SkipToLoafer.jpg', '/images/catalog/SkipToLoafer.jpg', 8.6, 2500, 0, false, true, 'Kotomi Deai', 'Tomoyo Kurosawa', 'ja', NOW(), NOW()),
(21, 'Soul Eater', 'Des étudiants d''une académie spéciale apprennent à chasser les âmes maléfiques.', 'ANIME', 'SHONEN', 2008, 0, '', '/images/catalog/soul_eater.jpg', '/images/catalog/soul_eater.jpg', 8.1, 7500, 12, false, true, 'Takuya Igarashi', 'Chiaki Omigawa', 'ja', NOW(), NOW()),
(22, 'Trois Yakuzas pour une Otaku', 'Une otaku se retrouve au cœur d''une rivalité entre trois membres de la pègre.', 'ANIME', 'SHOJO', 2021, 0, '', '/images/catalog/Trois_yakuzas_pour_une_otaku.jpg', '/images/catalog/Trois_yakuzas_pour_une_otaku.jpg', 7.4, 1500, 15, false, true, 'Studio X', 'Seiyuu', 'ja', NOW(), NOW()),
(23, 'Yamada and the Seven Witches', 'Un délinquant et une élève modèle échangent leurs corps après un accident.', 'ANIME', 'SHONEN', 2015, 0, '', '/images/catalog/yamada_a_seven_witches.png', '/images/catalog/yamada_a_seven_witches.png', 7.7, 3200, 15, false, true, 'Tomoki Takuno', 'Ryota Osaka', 'ja', NOW(), NOW()),
(24, 'Your Lie in April', 'Un pianiste prodige retrouve le goût de la musique grâce à une violoniste excentrique.', 'ANIME', 'SCHOOL_LIFE', 2014, 0, '', '/images/catalog/you_lie_in_april.jpg', '/images/catalog/you_lie_in_april.jpg', 8.9, 10000, 12, false, true, 'Kyohei Ishiguro', 'Natsuki Hanae', 'ja', NOW(), NOW()),
(25, 'Solo Leveling', 'Le plus faible des chasseurs devient le plus fort grâce à un mystérieux système.', 'ANIME', 'ISEKAI', 2024, 0, '', '/images/catalog/Solo_Leveling.jpg', '/images/catalog/Solo_Leveling.jpg', 8.9, 5000, 13, true, true, 'Shunsuke Nakashige', 'Taito Ban', 'ja', NOW(), NOW());

-- 3. SAISONS (EXTENDED)
INSERT INTO seasons (id, content_id, season_number, title, description, poster_url)
VALUES 
(1, 1, 1, 'Saison 1', 'Le début de la vengeance contre les Sept.', '/images/catalog/theBoys.jpg'),
(2, 2, 1, 'Saison 1', 'Le mystère du Monde à l''Envers.', '/images/posters/stranger_things.png'),
(3, 3, 1, 'Saison 1', 'L''entraînement de Tanjiro.', '/images/catalog/demon_slayer.jpg'),
(4, 6, 1, 'Saison 1', 'La nuit de la purge.', '/images/catalog/ThePurge.jpg'),
(5, 7, 1, 'Saison 1', 'Godolkin University.', '/images/catalog/GenV.jpg'),
(6, 10, 1, 'Saison 1', 'Le jeu commence.', '/images/catalog/squid_game.jpg'),
(7, 15, 1, 'Saison 1', 'Premier battement de cœur.', '/images/catalog/Heartbeat_in_a_Corner.jpg'),
(8, 15, 2, 'Saison 2', 'L''amour s''intensifie.', '/images/catalog/Heartbeat_in_a_Corner_saison_2.jpg'),
(9, 19, 1, 'Saison 1', 'Faux amour, vrais problèmes.', '/images/catalog/nisekoi.png'),
(10, 19, 2, 'Saison 2', 'Nouveaux rivaux.', '/images/catalog/nisekoi_saison_2.png');

-- 4. EPISODES
INSERT INTO episodes (id, season_id, episode_number, title, description, duration_seconds, video_url)
VALUES 
(1, 1, 1, 'La règle du jeu', 'Le début de la lutte contre Vought.', 3600, '/media/the_boys_s1e1.mp4'),
(2, 2, 1, 'La disparition de Will Byers', 'Will disparaît.', 3000, '/media/stranger_things_s1e1.mp4'),
(3, 3, 1, 'Cruauté', 'Tanjiro rentre chez lui.', 1500, ''),
(4, 6, 1, 'What is America?', 'Le début de la purge annuelle.', 3200, '/media/the_purge_s1e1.mkv'),
(5, 7, 1, 'On se reverra', 'Bienvenue à l''université.', 3400, '/media/gen_v_s1e1.mp4'),
(6, 8, 1, 'Episode 1', 'Le début de la saison 2.', 1440, ''),
(7, 10, 1, 'Episode 1', 'Le retour du faux couple.', 1440, '');

-- 5. METADONNÉES ANIME
INSERT INTO anime_metadata (content_id, japanese_title, romaji_title, studio, episode_count, air_status)
VALUES 
(3, '鬼滅の刃', 'Kimetsu no Yaiba', 'Ufotable', 26, 'COMPLETED'),
(13, 'ブラック・ブレット', 'Black Bullet', 'Kinema Citrus', 13, 'COMPLETED'),
(14, 'ダーリン・イン・ザ・フランキス', 'Darling in the Franxx', 'Trigger', 24, 'COMPLETED'),
(16, '地獄楽', 'Jigokuraku', 'MAPPA', 13, 'COMPLETED'),
(17, '君に届け', 'Kimi ni Todoke', 'Production I.G', 25, 'COMPLETED'),
(18, 'ラブ★コン', 'Lovely Complex', 'Toei Animation', 24, 'COMPLETED'),
(19, 'ニセコイ', 'Nisekoi', 'Shaft', 20, 'COMPLETED'),
(20, 'スキップとローファー', 'Skip to Loafer', 'P.A. Works', 12, 'COMPLETED'),
(21, 'ソウルイーター', 'Soul Eater', 'Bones', 51, 'COMPLETED'),
(23, '山田くんと7人の魔女', 'Yamada-kun to 7-nin no Majo', 'Liden Films', 12, 'COMPLETED'),
(24, '四月は君の嘘', 'Shigatsu wa Kimi no Uso', 'A-1 Pictures', 22, 'COMPLETED');

SELECT 'Restauration v17 terminée — Catalogue étendu avec IMAGES LOCALES !' as message;
