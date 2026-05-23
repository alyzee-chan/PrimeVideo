package com.primevideo.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RatingRequest {
    @NotNull(message = "La note est obligatoire")
    @Min(1) @Max(10)
    private Integer score;

    @NotBlank(message = "Le commentaire ne peut pas être vide")
    private String comment;

    @NotNull(message = "Le profil est obligatoire")
    private Long profileId;
}
