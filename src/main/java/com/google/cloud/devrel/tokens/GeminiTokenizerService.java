package com.google.cloud.devrel.tokens;

import ai.djl.sentencepiece.SpTokenizer;
import jakarta.inject.Singleton;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class GeminiTokenizerService {

    private final SpTokenizer tokenizer;

    GeminiTokenizerService() throws IOException {
        InputStream tokenizerModelResource = this.getClass().getResourceAsStream("/gemini-tokenizer.model");
        if (tokenizerModelResource == null) {
            throw new FileNotFoundException("Tokenizer model file not found");
        }

        byte[] modelFileBytes = tokenizerModelResource.readAllBytes();
        this.tokenizer = new SpTokenizer(modelFileBytes);
    }

    public Map<String, Object> tokenize(String prompt) {
        List<String> tokens = tokenizer.tokenize(prompt);

        /*
            Follow the same structure as the embedding model endpoints:

            { tokensInfo: [ {
                tokens: [...],  // base64 encoded token strings
                tokenIds: [...] // int32 strings
            } ] }
         */

        LinkedHashMap<String, Object> tokensInfo = new LinkedHashMap<>();
        List<String> base64encodedTokens = tokens.stream()
            .map(s -> Base64.getEncoder().encodeToString(s.replace("\u2581", " ").getBytes()))
            .toList();
        tokensInfo.put("tokens", base64encodedTokens);
        tokensInfo.put("tokenIds", tokens.stream()
            .map(String::hashCode)
            .map(hash -> Math.abs(hash % 256_000)) // Gemini / Gemma vocabulary size
            .toList());

        return Map.of("tokensInfo", List.of(tokensInfo));
    }
}
