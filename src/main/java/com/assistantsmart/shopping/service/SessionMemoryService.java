package com.assistantsmart.shopping.service;

import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionMemoryService {
    private static final int MAX_MEMORY = 10;
    private final Map<String, Deque<String>> memory = new ConcurrentHashMap<>();

    public List<String> load(String sessionId) {
        Deque<String> deque = memory.getOrDefault(sessionId, new ArrayDeque<>());
        return new ArrayList<>(deque);
    }

    public void append(String sessionId, String message) {
        Deque<String> deque = memory.computeIfAbsent(sessionId, key -> new ArrayDeque<>());
        synchronized (deque) {
            deque.addLast(message);
            while (deque.size() > MAX_MEMORY) {
                deque.removeFirst();
            }
        }
    }
}
