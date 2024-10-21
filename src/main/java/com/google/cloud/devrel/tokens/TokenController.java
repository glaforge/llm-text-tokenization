package com.google.cloud.devrel.tokens;

import ai.djl.sentencepiece.SpTokenizer;
import com.google.auth.oauth2.GoogleCredentials;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.client.netty.DefaultHttpClient;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.inject.Inject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static io.micronaut.http.MediaType.APPLICATION_JSON;

@Controller(value = "/tokens")
public class TokenController {
    @Inject
    private DefaultHttpClient client;

    @Inject
    private GeminiTokenizerService geminiTokenizer;

    @Value("${gcp.region}")
    private String region;

    @Value("${gcp.project}")
    private String project;

    @Post("/")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @ExecuteOn(TaskExecutors.BLOCKING)
    public Map tokenize(@NonNull String prompt, @NonNull String model) throws IOException {
        var credentials = GoogleCredentials.getApplicationDefault();
        credentials.refreshIfExpired();
        var token = credentials.getAccessToken().getTokenValue();

        var uri = UriBuilder
            .of("/v1beta1/projects/" + project + "/locations/" + region + "/publishers/google/models/" + model + ":computeTokens")
            .scheme("https")
            .host(region + "-aiplatform.googleapis.com")
            .build();

        var request = HttpRequest
            .POST(uri, Map.of("instances", List.of(Map.of("content", prompt))))
            .bearerAuth(token)
            .accept(APPLICATION_JSON)
            .contentType(APPLICATION_JSON);

        return client.toBlocking().exchange(request, Map.class).body();
    }

    @Post("/gemini")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @ExecuteOn(TaskExecutors.BLOCKING)
    public Map tokenizeGemini(@NonNull String prompt) throws IOException {
        return geminiTokenizer.tokenize(prompt);
    }
}
