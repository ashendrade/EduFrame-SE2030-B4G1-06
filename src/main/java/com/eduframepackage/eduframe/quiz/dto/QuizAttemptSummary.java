package com.eduframepackage.eduframe.quiz.dto;

/** Read-only projection used on the teacher's results dashboard. */
public record QuizAttemptSummary(
        Long attemptId,
        String studentName,
        String studentEmail,
        Integer scoreObtained,
        Integer totalMarks,
        Integer scorePercentage,
        Boolean passed,
        String status,
        String submittedAt
) {}
