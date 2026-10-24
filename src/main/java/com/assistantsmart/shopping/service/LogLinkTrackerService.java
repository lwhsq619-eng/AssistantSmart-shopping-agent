package com.assistantsmart.shopping.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LogLinkTrackerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogLinkTrackerService.class);

    private final Map<String, List<String>> traceLinks = new ConcurrentHashMap<>();

    public String track(List<String> links) {
        String traceId = UUID.randomUUID().toString();
        traceLinks.put(traceId, links);
        LOGGER.info("traceId={} sourceLinks={}", traceId, links);
        return traceId;
    }

    public List<String> linksOf(String traceId) {
        return traceLinks.getOrDefault(traceId, List.of());
    }
}
