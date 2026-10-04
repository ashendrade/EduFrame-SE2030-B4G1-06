package com.eduframepackage.eduframe.quiz.event;

import com.eduframepackage.eduframe.quiz.entity.QuizAttempt;
import org.springframework.context.ApplicationEvent;

/**
 * OBSERVER PATTERN (via Spring's ApplicationEvent/ApplicationListener).
 *
 * Published by QuizAttemptService the moment a graded attempt is found to
 * have passed. CertificateEventListener subscribes to this event and
 * reacts by generating a certificate - the attempt-grading logic stays
 * completely decoupled from certificate generation.
 */
public class QuizPassedEvent extends ApplicationEvent {

    private final QuizAttempt attempt;

    public QuizPassedEvent(Object source, QuizAttempt attempt) {
        super(source);
        this.attempt = attempt;
    }

    public QuizAttempt getAttempt() {
        return attempt;
    }
}
