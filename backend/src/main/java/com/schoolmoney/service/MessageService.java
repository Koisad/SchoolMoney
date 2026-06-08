package com.schoolmoney.service;

import com.schoolmoney.dto.ChatMessageRequest;
import com.schoolmoney.model.Message;
import com.schoolmoney.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    public Message saveMessage(String senderId, ChatMessageRequest request) {
        Message message = Message.builder()
                .senderId(senderId)
                .receiverId(request.getReceiverId())
                .classId(request.getClassId())
                .content(request.getContent())
                .build();

        return messageRepository.save(message);
    }

    public List<Message> getClassHistory(String classId) {
        return messageRepository.findByClassIdOrderByTimestampAsc(classId);
    }

    public List<Message> getPrivateHistory(String userId) {
        return messageRepository.findByReceiverIdOrderByTimestampAsc(userId);
    }
}