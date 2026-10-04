package com.eduframepackage.eduframe.quiz.dto;

import com.eduframepackage.eduframe.quiz.entity.QuestionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** Form-backing object for creating/editing a Question (teacher side). */
@Data
public class QuestionForm {
    private Long id;
    private Long quizId;

    @NotBlank(message = "Question text is required")
    private String questionText;

    private QuestionType questionType = QuestionType.MCQ;

    @Min(value = 1, message = "Marks must be at least 1")
    private Integer marks = 1;

    @NotBlank(message = "Correct answer is required")
    private String correctAnswer;

    /** Raw option lines from the form, one per line, MCQ only. */
    private List<String> options = new ArrayList<>();
}
