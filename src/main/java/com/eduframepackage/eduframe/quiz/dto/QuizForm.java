package com.eduframepackage.eduframe.quiz.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class QuizForm {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private Long courseId;

    @Min(value = 1, message = "Timer must be at least 1 minute")
    private Integer timerMinutes = 30;

    @Min(value = 0, message = "Pass mark must be 0-100")
    private Integer passMarkPercentage = 50;
}
