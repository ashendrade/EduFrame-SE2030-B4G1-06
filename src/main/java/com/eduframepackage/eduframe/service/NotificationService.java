package com.eduframepackage.eduframe.service;

import com.eduframepackage.eduframe.model.Announcement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service interface and stub implementation for dispatching notification alerts to enrolled students.
 */
public interface NotificationService {
    void dispatchPostNotification(Announcement post);
    List<String> getDispatchedLog();
}

@Service
class MockNotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(MockNotificationServiceImpl.class);
    private final List<String> dispatchedLog = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void dispatchPostNotification(Announcement post) {
        String msg = String.format("[NOTIFICATION SERVICE] Dispatched alert for %s '%s' (Course: %s, Author: %s)",
                post.getType(), post.getTitle(), post.getCourseId(), post.getAuthorId());
        dispatchedLog.add(msg);
        log.info(msg);
    }

    @Override
    public List<String> getDispatchedLog() {
        return new ArrayList<>(dispatchedLog);
    }
}
