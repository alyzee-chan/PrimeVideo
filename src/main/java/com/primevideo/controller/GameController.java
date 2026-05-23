package com.primevideo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class GameController {

    @GetMapping("/games")
    public String games(Model model) {
        // Simulation de données de jeux
        List<Map<String, String>> trendingGames = List.of(
            Map.of("id", "drive-mad", "title", "Drive Mad", "image", "/images/games/driveMad.jpg", "category", "Course"),
            Map.of("id", "snake-game", "title", "Snake Game", "image", "/images/games/snake.jpg", "category", "Retro"),
            Map.of("id", "1", "title", "Asphalt 9: Legends", "image", "/images/games/legends.jpg", "category", "Racing"),
            Map.of("id", "2", "title", "Modern Combat 5", "image", "/images/games/modernCombat.jpg", "category", "Action"),
            Map.of("id", "3", "title", "Dungeon Hunter 5", "image", "/images/games/dungeonHunter.jpg", "category", "RPG")
        );

        List<Map<String, String>> newGames = List.of(
            Map.of("id", "4", "title", "Gangstar Vegas", "image", "https://img.gameloft.com/gangstarvegas/gv_og_image.jpg", "category", "Open World"),
            Map.of("id", "5", "title", "Dragon Mania Legends", "image", "/images/games/dragonManiaLegends.jpg", "category", "Simulation")
        );

        model.addAttribute("trendingGames", trendingGames);
        model.addAttribute("newGames", newGames);
        
        return "catalog/games";
    }

    @GetMapping("/games/drive-mad")
    public String driveMadGame() {
        return "catalog/games/drive-mad";
    }

    @GetMapping("/games/snake-game")
    public String snakeGame() {
        return "catalog/games/Snake Game JS/index";
    }
}
