package jp.ac.ir.repository.chat;

import jp.ac.ir.model.dto.chat.ChatRequest;
import jp.ac.ir.model.dto.chat.ChatResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * Python API（FastAPI）のチャットエンドポイントを宣言的に定義するインターフェース【サンプル】
 */
@HttpExchange
public interface ChatApiClient {

    @PostExchange("/chat")
    ChatResponse send(@RequestBody ChatRequest request);
}
