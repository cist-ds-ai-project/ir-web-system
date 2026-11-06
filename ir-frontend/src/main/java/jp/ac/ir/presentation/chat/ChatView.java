package jp.ac.ir.presentation.chat;

import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jp.ac.ir.model.dto.chat.ChatRequest;
import jp.ac.ir.presentation.MainLayout;
import jp.ac.ir.presentation.component.notification.ErrorNotification;
import jp.ac.ir.repository.chat.ChatApiClient;

@Route(value = "chat", layout = MainLayout.class)
@PageTitle("AIチャット")
@PermitAll
public class ChatView extends VerticalLayout {

    private final ChatApiClient chatApiClient;
    private final MessageList messageList = new MessageList();

    public ChatView(ChatApiClient chatApiClient) {
        this.chatApiClient = chatApiClient;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        messageList.setWidthFull();
        messageList.setMaxHeight("70vh");

        MessageInput messageInput = new MessageInput();
        messageInput.setWidthFull();
        messageInput.addSubmitListener(event -> {
            String text = event.getValue();
            if (text == null || text.isBlank()) {
                return;
            }
            messageList.addItem(new MessageListItem(text, null, "あなた"));
            try {
                var response = chatApiClient.send(new ChatRequest(text));
                messageList.addItem(new MessageListItem(response.answer(), null, "AI"));
            } catch (Exception e) {
                new ErrorNotification("チャットの送信に失敗しました: " + e.getMessage());
            }
        });

        add(messageList, messageInput);
        expand(messageList);
    }
}
