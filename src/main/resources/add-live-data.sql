-- ═══════════════════════════════════════════════════════════
-- AJOUT DE CONTENUS "LIVE" POUR TESTER LE LECTEUR
-- ═══════════════════════════════════════════════════════════

USE primevideo_db;

INSERT INTO contents (title, description, type, genre, year, duration_seconds, 
    poster_url, rating_average, rating_count, age_rating, is_premium, is_available, 
    director, cast_list, language, created_at, updated_at, video_url)
VALUES
('Canal+ Sport LIVE', 'Suivez le meilleur du sport en direct : Football, Tennis, Formule 1 et bien plus encore.', 
 'LIVE', 'SPORT', 2024, 0, 
 'https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&q=80&w=500', 
 4.8, 1500, 0, true, true, 
 'Direction des Sports', 'Équipe Canal+', 'fr', NOW(), NOW(), 
 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4'),

('CNN International', 'Actualités mondiales en direct 24h/24. Reportages exclusifs et analyses des événements globaux.', 
 'LIVE', 'DOCUMENTAIRE', 2024, 0, 
 'https://images.unsplash.com/photo-1495020689067-958852a7765e?auto=format&fit=crop&q=80&w=500', 
 4.5, 8000, 0, false, true, 
 'CNN Global', 'Journalistes CNN', 'en', NOW(), NOW(), 
 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4'),

('Festival de Musique Coachella', 'Le plus grand festival de musique au monde en direct de Californie. Concerts exclusifs.', 
 'LIVE', 'MUSICAL', 2024, 0, 
 'https://images.unsplash.com/photo-1459749411177-042180ce673c?auto=format&fit=crop&q=80&w=500', 
 4.9, 12000, 12, true, true, 
 'Live Nation', 'Artistes variés', 'en', NOW(), NOW(), 
 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4'),

('Gaming Championship', 'Finales mondiales du tournoi E-Sport. Les meilleurs joueurs s''affrontent en direct.', 
 'LIVE', 'SPORT', 2024, 0, 
 'https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&q=80&w=500', 
 4.7, 5000, 12, false, true, 
 'E-Sport Org', 'Pro Players', 'fr', NOW(), NOW(), 
 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4');

SELECT '4 contenus LIVE ajoutés avec succès !' as message;
