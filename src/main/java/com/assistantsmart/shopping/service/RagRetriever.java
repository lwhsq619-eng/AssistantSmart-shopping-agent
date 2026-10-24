package com.assistantsmart.shopping.service;

import com.assistantsmart.shopping.model.RetrievalDocument;

import java.util.List;

public interface RagRetriever {
    List<RetrievalDocument> retrieve(String query);
}
