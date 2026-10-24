package com.assistantsmart.shopping.service;

import com.assistantsmart.shopping.model.RetrievalDocument;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class MilvusRagRetriever implements RagRetriever {

    private static final List<RetrievalDocument> CATALOG = List.of(
            new RetrievalDocument("Phone Buying Guide", "Choose phones by camera, battery and budget.", "https://shop.example/guides/phone"),
            new RetrievalDocument("Laptop Buying Guide", "Compare CPU, RAM and portability for laptops.", "https://shop.example/guides/laptop"),
            new RetrievalDocument("Headphone Buying Guide", "Use ANC and latency to evaluate headphones.", "https://shop.example/guides/headphones")
    );

    @Override
    public List<RetrievalDocument> retrieve(String query) {
        String normalized = query.toLowerCase(Locale.ROOT);
        List<RetrievalDocument> matched = new ArrayList<>();
        for (RetrievalDocument doc : CATALOG) {
            if (normalized.contains("phone") && doc.title().contains("Phone")) {
                matched.add(doc);
            }
            if (normalized.contains("laptop") && doc.title().contains("Laptop")) {
                matched.add(doc);
            }
            if (normalized.contains("headphone") && doc.title().contains("Headphone")) {
                matched.add(doc);
            }
        }
        return matched.isEmpty() ? List.of(CATALOG.get(0)) : matched;
    }
}
