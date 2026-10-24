package com.example.mall.agent.memory;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 轻量级多轮对话记忆服务。
 * 当前阶段使用 JVM 内存保存最近 10 轮对话，不依赖 Redis。
 */
@Service
public class ChatMemoryService {
    private static final int MAX_TURNS = 10;
    private final Map<String, Deque<ChatTurn>> memories = new ConcurrentHashMap<>();

    public String normalizeSessionId(String sessionId) {
        return normalizeSessionId(sessionId, "mall-guide-");
    }

    public String normalizeSessionId(String sessionId, String prefix) {
        if (StringUtils.hasText(sessionId)) {
            return sessionId.trim();
        }
        String safePrefix = StringUtils.hasText(prefix) ? prefix : "ai-";
        return safePrefix + UUID.randomUUID();
    }

    public List<ChatTurn> getRecentTurns(String sessionId) {
        Deque<ChatTurn> turns = memories.get(sessionId);
        if (turns == null) {
            return List.of();
        }
        return new ArrayList<>(turns);
    }

    public String buildUserContext(String sessionId, String currentMessage) {
        StringBuilder builder = new StringBuilder();
        for (ChatTurn turn : getRecentTurns(sessionId)) {
            builder.append(turn.userMessage()).append('\n');
        }
        builder.append(currentMessage);
        return builder.toString();
    }

    public void append(String sessionId, String userMessage, String assistantReply) {
        Deque<ChatTurn> turns = memories.computeIfAbsent(sessionId, key -> new ArrayDeque<>());
        turns.addLast(new ChatTurn(userMessage, assistantReply, LocalDateTime.now()));
        while (turns.size() > MAX_TURNS) {
            turns.removeFirst();
        }
    }

    /**
     * 单轮对话记录。
     */
    public record ChatTurn(String userMessage, String assistantReply, LocalDateTime createTime) {
    }
}
