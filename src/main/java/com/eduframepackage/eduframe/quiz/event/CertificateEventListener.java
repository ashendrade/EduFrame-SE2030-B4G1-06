package com.eduframepackage.eduframe.quiz.event;

import com.eduframepackage.eduframe.quiz.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Observer that reacts to {@link QuizPassedEvent} by issuing a PDF
 * certificate. Runs after the triggering transaction commits, so the
 * attempt is guaranteed to be saved before the certificate is created.
 */
@Component
@RequiredArgsConstructor
public class CertificateEventListener {

    private final CertificateService certificateService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onQuizPassed(QuizPassedEvent event) {
        try {
            certificateService.issueCertificateForAttempt(event.getAttempt().getId());
        } catch (Exception ex) {
            // Spring silently swallows exceptions thrown from AFTER_COMMIT
            // listeners (the DB transaction has already committed, so it
            // can't roll back) - log loudly here so failures are visible
            // instead of vanishing. StudentQuizController#result also
            // retries this idempotently as a safety net.
            System.err.println("[CertificateEventListener] Failed to issue certificate for attempt "
                    + event.getAttempt().getId() + ": " + ex);
            ex.printStackTrace();
        }
    }
}