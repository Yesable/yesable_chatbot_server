package com.example.yesable_chatbot.controller;


import com.example.yesable_chatbot.dto.MessageDTO;
import com.example.yesable_chatbot.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatbotService chatbotService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    //private final RealService realService;

    @GetMapping("/chat")
    public String chatPage(Model model) {
        int ancestorId = 123; // 임의 값
        model.addAttribute("ancestorId", ancestorId);
        return "chat";
    }


    @MessageMapping("/chat")
    public void sendMessage(@Payload MessageDTO messageDto) {
        try {// 사용자가 보낸 메시지를 처리하고 대화 응답을 생성
            chatbotService.generateChatbotResponse(messageDto);
        } catch (Exception e){
            simpMessagingTemplate.convertAndSend("/topic/messages", new MessageDTO("Error occured while " +
                    "fetching GPT Response. " + "Please try again"));
        }
    }

    @MessageMapping("/init")
    public void initChatbotService(){
        try{
            chatbotService.initChatbot();
        } catch(Exception e){
            simpMessagingTemplate.convertAndSend("/topic/messages", new MessageDTO("Error occured while " +
                    "initializing Chatbot " + "Please try again"));
        }
    }
}
