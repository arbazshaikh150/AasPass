package com.project.arbaz.aaspass.listener;

import com.project.arbaz.aaspass.dto.EventCreatedEvent;
import com.project.arbaz.aaspass.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class EventCreatedListener {
    private final NotificationService notificationService;
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(EventCreatedEvent event) {

        notificationService.notify(
                event.latitude(),
                event.longitude()
        );
    }
}
