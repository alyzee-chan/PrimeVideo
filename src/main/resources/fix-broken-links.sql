-- ═══════════════════════════════════════════════════════════
-- RÉPARATION DES LIENS VIDÉO CASSÉS
-- Remplace les liens vides ou invalides par un lien d'exemple fonctionnel
-- ═══════════════════════════════════════════════════════════

USE primevideo_db;

-- 1. Réparer spécifiquement Hôtel Transylvanie 4 (si le titre correspond)
UPDATE contents 
SET video_url = 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4'
WHERE title LIKE '%Hotel Transylvania 4%';

-- 2. Réparer tous les autres contenus qui n'ont pas d'URL
UPDATE contents 
SET video_url = 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4'
WHERE video_url IS NULL OR video_url = '' OR video_url = 'null';

SELECT 'Liens vidéo réparés avec succès !' as message;
