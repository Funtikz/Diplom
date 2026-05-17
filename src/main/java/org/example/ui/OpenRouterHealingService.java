package org.example.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;

import java.time.Duration;

import static org.example.ui.Constants.Capability.OPEN_ROUTER_KEY;

public class OpenRouterHealingService
        implements AIHealingService {

    private static final String API_URL =
            "https://openrouter.ai/api/v1/chat/completions";

    private static final String MODEL =
            "nvidia/nemotron-3-super-120b-a12b:free";

    private final OkHttpClient client =
            new OkHttpClient.Builder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .readTimeout(Duration.ofSeconds(60))
                    .build();

    private final ObjectMapper mapper =
            new ObjectMapper();

    @Override
    public String healLocator(
            String failedLocator,
            String dom
    ) {

        try {

            String apiKey =
                    OPEN_ROUTER_KEY;

            if (apiKey == null || apiKey.isBlank()) {

                throw new RuntimeException(
                        "OPENROUTER_API_KEY is null"
                );
            }

            // Ограничиваем DOM
            if (dom.length() > 10000) {

                dom = dom.substring(0, 10000);
            }

            String prompt = buildPrompt(
                    failedLocator,
                    dom
            );

            ObjectNode root =
                    mapper.createObjectNode();

            root.put("model", MODEL);

            ArrayNode messages =
                    root.putArray("messages");

            ObjectNode user =
                    mapper.createObjectNode();

            user.put("role", "user");
            user.put("content", prompt);

            messages.add(user);

            String requestBody =
                    mapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(root);

            System.out.println("========== OPENROUTER REQUEST ==========");
            System.out.println(requestBody);

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader(
                            "Authorization",
                            "Bearer " + apiKey
                    )
                    .addHeader(
                            "Content-Type",
                            "application/json"
                    )
                    .post(RequestBody.create(
                            requestBody,
                            MediaType.parse("application/json")
                    ))
                    .build();

            Response response =
                    client.newCall(request).execute();

            String responseBody =
                    response.body().string();

            System.out.println("========== OPENROUTER RESPONSE ==========");
            System.out.println(responseBody);

            JsonNode json =
                    mapper.readTree(responseBody);

            // Проверка ошибок OpenRouter
            if (json.get("error") != null) {

                throw new RuntimeException(
                        "OpenRouter API error: "
                                + json.get("error").toPrettyString()
                );
            }

            // Проверка структуры ответа
            if (json.get("choices") == null
                    || json.get("choices").isEmpty()) {

                throw new RuntimeException(
                        "No choices returned from OpenRouter"
                );
            }

            String healedXpath = json
                    .get("choices")
                    .get(0)
                    .get("message")
                    .get("content")
                    .asText()
                    .trim();

            System.out.println(
                    "========== HEALED XPATH =========="
            );

            System.out.println(healedXpath);

            return healedXpath;

        } catch (Exception e) {

            System.out.println("AI healing failed");

            e.printStackTrace();

            throw new RuntimeException(
                    "AI healing failed",
                    e
            );
        }
    }

    private String buildPrompt(
            String failedLocator,
            String dom
    ) {

        return """
                You are an AI self-healing locator system.

                Failed XPath:
                %s

                Your task:
                Find a replacement XPath locator.

                IMPORTANT RULES:
                - Return ONLY xpath
                - Do NOT explain
                - Do NOT add markdown
                - Prefer:
                  - data-testid
                  - id
                  - aria-label
                - Avoid dynamic classes

                DOM:
                %s
                """.formatted(
                failedLocator,
                dom
        );
    }
}