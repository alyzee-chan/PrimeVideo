package com.primevideo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class DeleteEpisode14 {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/primevideo_db?useSSL=false&serverTimezone=Europe/Paris&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
        String user = "root";
        String password = ""; // WAMP default
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "SELECT e.episode_number, e.duration_seconds, e.video_url FROM episodes e JOIN seasons s ON e.season_id = s.id JOIN contents c ON s.content_id = c.id WHERE c.title = 'Wistoria'";
            try (PreparedStatement pstmt = conn.prepareStatement(sql); java.sql.ResultSet rs = pstmt.executeQuery()) {
                while(rs.next()) {
                    System.out.println("EP: " + rs.getInt("episode_number") + " | DUR: " + rs.getInt("duration_seconds") + " | URL: " + rs.getString("video_url"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
