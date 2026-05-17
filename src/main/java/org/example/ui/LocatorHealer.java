package org.example.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;

public class LocatorHealer {

    private static final String API_URL =
            "https://openrouter.ai/api/v1/chat/completions";

    private static final String API_KEY =
            System.getenv("OPENROUTER_API_KEY");

    public String healLocator(
            String failedLocator,
            String dom
    ) throws Exception {

        OkHttpClient client = new OkHttpClient();

        String prompt = """
            You are a self-healing locator system.

            Failed locator:
            %s

            Task:
            Find the most likely replacement XPath.

            Rules:
            - Return ONLY xpath
            - Prefer:
              - data-testid
              - id
              - aria-label
            - Avoid dynamic classes

            DOM:
            %s
            """.formatted(failedLocator, dom);

        ObjectMapper mapper = new ObjectMapper();

        ObjectNode requestJson = mapper.createObjectNode();

        requestJson.put("model",
                "qwen/qwen3-14b:free");

        ArrayNode messages =
                requestJson.putArray("messages");

        ObjectNode userMessage =
                mapper.createObjectNode();

        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        messages.add(userMessage);

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader(
                        "Authorization",
                        "Bearer " + API_KEY
                )
                .addHeader(
                        "Content-Type",
                        "application/json"
                )
                .post(RequestBody.create(
                        mapper.writeValueAsString(requestJson),
                        MediaType.parse("application/json")
                ))
                .build();

        Response response =
                client.newCall(request).execute();

        String body =
                response.body().string();

        JsonNode json =
                mapper.readTree(body);

        return json
                .get("choices")
                .get(0)
                .get("message")
                .get("content")
                .asText()
                .trim();
    }
}