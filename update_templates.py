import os
import re

HOVER_PANEL_HTML = """
                    <!-- NOUVEAU HOVER PANEL (Style Netflix) -->
                    <div class="hover-panel">
                        <div class="hover-actions">
                            <button type="button" class="hover-btn play-btn-small" th:onclick="'window.location.href=\\'/watch/' + ${content.id} + '\\''" title="Lecture"><i class="fas fa-play" style="margin-left:2px;"></i></button>
                            <button type="button" class="hover-btn add-btn-small" title="Ajouter à ma liste" sec:authorize="isAuthenticated()" th:onclick="'toggleWatchlist(' + ${content.id} + ', this); event.preventDefault();'"><i class="fas fa-plus"></i></button>
                            <button type="button" class="hover-btn like-btn-small" title="J'aime" th:onclick="'event.preventDefault();'"><i class="fas fa-thumbs-up"></i></button>
                            <button type="button" class="hover-btn more-btn-small" title="Plus d'infos" style="margin-left: auto;" th:onclick="'window.location.href=\\'/catalog/' + ${content.id} + '\\''"><i class="fas fa-chevron-down"></i></button>
                        </div>
                        <div class="hover-meta">
                            <span class="age-rating" th:text="${content.ageRating} + '+'">16+</span>
                            <span class="duration" th:if="${content.type.name() == 'FILM' or content.type.name() == 'DOCUMENTAIRE' or content.type.name() == 'AFRICAIN'}" th:text="${content.durationFormatted}">2 h 14 min</span>
                            <span class="duration" th:if="${content.type.name() == 'SERIE' or content.type.name() == 'ANIME' or content.type.name() == 'KDRAMA'}">
                                <th:block th:if="${content.animeMetadata != null}" th:text="${content.animeMetadata.episodeCount} + ' Épisodes'"></th:block>
                                <th:block th:if="${content.kdramaMetadata != null}" th:text="${content.kdramaMetadata.episodeCount} + ' Épisodes'"></th:block>
                                <!-- Fallback if no specific metadata -->
                                <th:block th:if="${content.animeMetadata == null and content.kdramaMetadata == null and content.seasons != null}">
                                    <span th:text="${content.seasons.size()} + ' Saisons'"></span>
                                </th:block>
                            </span>
                            <span class="hd-badge">HD</span>
                        </div>
                        <div class="hover-tags">
                            <span th:text="${content.genre}">Genre</span>
                        </div>
                    </div>
"""

def update_templates():
    template_dir = r"d:\KEYCE\B2B\SEMESTRE 2\JAVA SPRING BOOT\TP\primevideo_1\primevideo_1\primevideo\src\main\resources\templates\catalog"
    files_to_update = ["home.html", "member-home.html", "search.html", "list.html"]

    # Pattern to find <div class="card-info">...</div>
    pattern = re.compile(r'<div class="card-info">.*?</div>\s*(?=</a>)', re.DOTALL)

    for filename in files_to_update:
        filepath = os.path.join(template_dir, filename)
        if not os.path.exists(filepath):
            continue
            
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()

        # Replace card-info with hover-panel
        new_content = pattern.sub(HOVER_PANEL_HTML, content)

        if new_content != content:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(new_content)
            print(f"Updated {filename}")
        else:
            print(f"No changes for {filename}")

if __name__ == "__main__":
    update_templates()
