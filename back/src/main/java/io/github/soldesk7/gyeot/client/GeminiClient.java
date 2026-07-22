package io.github.soldesk7.gyeot.client;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;


@Component
public class GeminiClient {

        private final ChatModel chatModel;

        public GeminiClient(ChatModel chatModel) {
                this.chatModel = chatModel;
        }

        public record Result(String text, boolean blocked) {
        }
}
