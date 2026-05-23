package com.primevideo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Long id;
    private String profileName;
    private String profileAvatar;
    private Integer score;
    private String comment;
    private LocalDateTime createdAt;
}
