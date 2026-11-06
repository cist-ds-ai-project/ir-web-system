package jp.ac.ir.presentation.component.notification;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

public class ErrorNotification extends Notification {
    public ErrorNotification(String text) {
        addThemeVariants(NotificationVariant.LUMO_ERROR);
        setPosition(Position.TOP_CENTER);
        setDuration(5000);
        setText(text);
        open();
    }
}
