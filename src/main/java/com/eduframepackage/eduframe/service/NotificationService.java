package com.eduframepackage.eduframe.service;

import com.eduframepackage.eduframe.model.Announcement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service interface for dispatching system and announcement notification alerts to enrolled students.
 */
public interface NotificationService {
    
    /**
     * Dispatches a notification alert for a newly published announcement or event post.
     * 
     * @param post Announcement entity for which the alert is generated.
     */
    void dispatchPostNotification(Announcement post);

    /**
     * Retrieves the complete log of all dispatched notification messages.
     * 
     * @return Thread-safe copy of dispatched notification messages log.
     */
    List<String> getDispatchedLog();
}

/**
 * Service implementation providing notification logging and broadcasting capabilities.
 */
@Service
class MockNotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(MockNotificationServiceImpl.class);
    private final List<String> dispatchedLog = Collections.synchronizedList(new ArrayList<>());

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispatchPostNotification(Announcement post) {
        String msg = String.format("[NOTIFICATION SERVICE] Dispatched alert for %s '%s' (Course: %s, Author: %s)",
                post.getType(), post.getTitle(), post.getCourseId(), post.getAuthorId());
        dispatchedLog.add(msg);
        log.info(msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getDispatchedLog() {
        return new ArrayList<>(dispatchedLog);
    }
}
