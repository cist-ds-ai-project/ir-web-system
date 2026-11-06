package jp.ac.ir.presentation.component.notification;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

public class SuccessNotification extends Notification {
    public SuccessNotification(String text) {
        addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        setPosition(Position.TOP_CENTER);
        setDuration(5000);
        setText(text);
        open();
    }
}
