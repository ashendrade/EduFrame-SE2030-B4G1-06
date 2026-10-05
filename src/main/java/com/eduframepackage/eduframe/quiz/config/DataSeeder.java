package com.eduframepackage.eduframe.quiz.config;

import com.eduframepackage.eduframe.quiz.entity.*;
import com.eduframepackage.eduframe.quiz.repository.QuizRepository;
import com.eduframepackage.eduframe.quiz.repository.QuizUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final QuizUserRepository userRepository;
    private final QuizRepository quizRepository;

    @Override
    public void run(String... args) {
        User teacher = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.TEACHER)
                .findFirst()
                .orElse(null);

        if (quizRepository.count() == 0 && teacher != null) {
            Quiz quiz = Quiz.builder()
                .title("Software Engineering Fundamentals - Quiz 1")
                .description("Covers SDLC, Agile/Scrum, and basic UML concepts. Pass mark 50%.")
                .timerMinutes(15)
                .passMarkPercentage(50)
                .status(QuizStatus.PUBLISHED)
                .createdBy(teacher)
                .build();

        Question q1 = Question.builder()
                .questionText("Which SDLC model delivers working software in short, repeated cycles called sprints?")
                .questionType(QuestionType.MCQ)
                .marks(2)
                .correctAnswer("Agile/Scrum")
                .sequence(1)
                .build();
        q1.addOption(QuestionOption.builder().optionText("Waterfall").sequence(1).build());
        q1.addOption(QuestionOption.builder().optionText("Agile/Scrum").sequence(2).build());
        q1.addOption(QuestionOption.builder().optionText("Spiral").sequence(3).build());
        q1.addOption(QuestionOption.builder().optionText("V-Model").sequence(4).build());

        Question q2 = Question.builder()
                .questionText("A Use Case Diagram shows the internal source code structure of a system.")
                .questionType(QuestionType.TRUE_FALSE)
                .marks(1)
                .correctAnswer("FALSE")
                .sequence(2)
                .build();

        Question q3 = Question.builder()
                .questionText("Name the design pattern that lets you swap interchangeable algorithms " +
                        "(e.g. grading MCQ vs Short Answer questions) behind a common interface.")
                .questionType(QuestionType.SHORT_ANSWER)
                .marks(2)
                .correctAnswer("Strategy pattern")
                .sequence(3)
                .build();

        quiz.addQuestion(q1);
        quiz.addQuestion(q2);
        quiz.addQuestion(q3);

        quizRepository.save(quiz);
        }

        System.out.println("EduFrame Quiz & Assessment Subsystem initialized.");
    }

}
