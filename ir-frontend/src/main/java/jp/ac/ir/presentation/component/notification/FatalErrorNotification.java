package jp.ac.ir.presentation.component.notification;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class FatalErrorNotification extends Notification {
    public FatalErrorNotification(String text) {
        addThemeVariants(NotificationVariant.LUMO_ERROR);
        Button closeButton = new Button(new Icon("lumo", "cross"));
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        closeButton.addClickListener(event -> {
            close();
        });
        add(new HorizontalLayout(new Text(text), closeButton));
        setPosition(Position.TOP_CENTER);
        open();
    }
}
