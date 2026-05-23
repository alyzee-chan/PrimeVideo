package com.primevideo.repository;

import com.primevideo.entity.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Accès à la table {@code contents} en base de données.
 *
 * <p>Étend {@link JpaRepository} qui fournit gratuitement les opérations CRUD standards :
 * {@code findAll()}, {@code findById()}, {@code save()}, {@code deleteById()}, etc.</p>
 *
 * <p><strong>Principe des méthodes Spring Data :</strong> Spring lit le nom de la méthode
 * et génère automatiquement le SQL correspondant. Par exemple :<br>
 * {@code findByType(FILM)} → {@code SELECT * FROM contents WHERE type = 'FILM'}<br>
 * {@code findByTypeAndIsAvailableTrue(SERIE)} → {@code SELECT * FROM contents WHERE type = 'SERIE' AND is_available = 1}</p>
 *
 * <p>Pour les requêtes plus complexes, on utilise {@code @Query} avec du JPQL
 * (langage de requête Java, similaire au SQL mais sur les objets Java et non les tables).</p>
 */
@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {

    List<Content> findByTitleIgnoreCase(String title);

    /**
     * Retourne tous les contenus d'un type donné, paginés.
     * Utilisé pour les pages Films, Séries, Animés, K-Dramas.
     *
     * @param type     le type souhaité (ex : ContentType.FILM)
     * @param pageable objet de pagination (numéro de page, taille, tri)
     * @return une page de contenus
     */
    Page<Content> findByType(Content.ContentType type, Pageable pageable);

    /**
     * Retourne tous les contenus d'un genre donné, paginés.
     *
     * @param genre le genre souhaité (ex : Genre.ACTION)
     */
    Page<Content> findByGenre(Content.Genre genre, Pageable pageable);

    /**
     * Retourne les contenus disponibles ({@code is_available = true}) d'un type donné.
     */
    Page<Content> findByTypeAndIsAvailableTrue(Content.ContentType type, Pageable pageable);

    /**
     * Retourne les contenus filtrés par type et limite d'âge.
     */
    Page<Content> findByTypeAndIsAvailableTrueAndAgeRatingLessThanEqual(Content.ContentType type, Integer ageRating, Pageable pageable);

    /**
     * Retourne tous les contenus disponibles respectant une limite d'âge.
     */
    Page<Content> findAllByAgeRatingLessThanEqualAndIsAvailableTrue(Integer ageRating, Pageable pageable);

    /**
     * Recherche plein-texte dans le titre, la description, le réalisateur et le casting.
     * Utilise JPQL pour une meilleure compatibilité entre bases de données (H2, MySQL, etc.).
     *
     * @param query le mot-clé recherché
     * @param maxAge limite d'âge du profil
     */
    @Query("SELECT c FROM Content c WHERE " +
           "(LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.director) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.castList) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND c.isAvailable = true AND c.ageRating <= :maxAge")
    Page<Content> searchWithAgeLimit(@Param("query") String query, @Param("maxAge") Integer maxAge, Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.isPremium = false AND c.isAvailable = true AND c.ageRating <= :maxAge ORDER BY c.ratingAverage DESC")
    List<Content> findFreeTopRatedWithAgeLimit(@Param("maxAge") Integer maxAge, Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.isAvailable = true AND c.ageRating <= :maxAge ORDER BY c.createdAt DESC")
    List<Content> findLatestAvailableWithAgeLimit(@Param("maxAge") Integer maxAge, Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.isAvailable = true AND c.ageRating <= :maxAge ORDER BY c.ratingCount DESC")
    List<Content> findMostWatchedWithAgeLimit(@Param("maxAge") Integer maxAge, Pageable pageable);

    /**
     * Retourne les contenus d'un type ET d'un genre donnés, paginés avec limite d'âge.
     */
    Page<Content> findByTypeAndGenreAndIsAvailableTrueAndAgeRatingLessThanEqual(Content.ContentType type, Content.Genre genre, Integer ageRating, Pageable pageable);

    /**
     * Retourne les contenus d'un type ET d'un genre donnés.
     * Utilisé pour les recommandations "Du même genre".
     */
    List<Content> findByTypeAndGenre(Content.ContentType type, Content.Genre genre);
}
