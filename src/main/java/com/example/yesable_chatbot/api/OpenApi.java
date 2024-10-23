package com.example.yesable_chatbot.api;




import com.azure.ai.openai.OpenAIClient;

import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.*;


import com.azure.core.credential.KeyCredential;
import com.azure.core.util.IterableStream;
import com.example.yesable_chatbot.dto.ChatHistoriesDTO;
import com.example.yesable_chatbot.dto.MessageDTO;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OpenApi {

    @Value("${openai.openaikey}")
    String openaiKey;


    @Value("${openai.deploymentmodel}")
    String deploymentOrModelId;

    OpenAIClient client;


    private final ChatHistoriesDTO chatHistories = new ChatHistoriesDTO(3);
   // private final RealService realService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @PostConstruct
    public void init() {

        this.client = new OpenAIClientBuilder()
                .credential(new KeyCredential(openaiKey))
                .buildClient();
    }



    // ---------------------- Chatbot 관련 코드 -----------------------//
    public void clearChatHistories(){
        chatHistories.clearHistory();
    }

    public void getReply(MessageDTO messageDto) {
        AtomicReference<Long> messageId = new AtomicReference<>(1L);
        List<String> gptResponses = new ArrayList<>();
        List<ChatRequestMessage> chatMessages = new ArrayList<>();

        // 사용자 메시지를 ChatRequestUserMessage로 추가
        chatMessages.add(new ChatRequestUserMessage(messageDto.getMessage()));



        // temperature 0으로 설정하고 메시지를 ChatCompletionsOptions에 추가
        ChatCompletionsOptions options = new ChatCompletionsOptions(chatMessages);
        options.setTemperature(0.0);

        // OpenAI 클라이언트를 사용하여 스트리밍 방식으로 응답 받기
        IterableStream<ChatCompletions> chatCompletionsStream = client.getChatCompletionsStream(
                deploymentOrModelId, options);

        // 스트림 처리
        chatCompletionsStream.stream()
                .forEach(chatCompletions -> {
                    if (chatCompletions.getChoices().isEmpty()) {
                        return;
                    }
                    ChatResponseMessage delta = chatCompletions.getChoices().get(0).getDelta();
                    if (delta.getRole() != null) {
                        System.out.println("Role = " + delta.getRole());
                    }
                    if (delta.getContent() != null) {
                        String responseContent = delta.getContent();
                        System.out.print(responseContent);
                        gptResponses.add(responseContent);
                        simpMessagingTemplate.convertAndSend("/topic/messages", new MessageDTO(responseContent,
                                messageId.getAndSet(messageId.get() + 1)));
                    }
                });

        // 응답(스트림)이 끝난 것을 프론트에 알려 새로운 응답 박스가 생기게 하는 역할
        simpMessagingTemplate.convertAndSend("/topic/messageEnd", new MessageDTO("response ended"));

        // chatHistory에 유저의 질문과 그에 대한 답변을 저장
        chatHistories.add(messageDto.getMessage(), String.join("", gptResponses));
    }



}
