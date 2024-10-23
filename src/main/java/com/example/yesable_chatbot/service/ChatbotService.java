package com.example.yesable_chatbot.service;


import com.example.yesable_chatbot.api.OpenApi;
import com.example.yesable_chatbot.dto.MessageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatbotService {
    private final OpenApi openApi;
    public void generateChatbotResponse(MessageDTO userMessage){
        try {
            openApi.getReply(userMessage);
        } catch (Exception e) {
            e.printStackTrace();;
            throw e;
        }
    }

    public void initChatbot(){
        try{
            openApi.clearChatHistories();
        }catch (Exception e){
            System.out.println("failed initializing Chatbot!\n");
            throw e;
        }
    }
}
