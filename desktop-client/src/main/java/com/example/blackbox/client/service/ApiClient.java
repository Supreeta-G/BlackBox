package com.example.blackbox.client.service;

import com.example.blackbox.client.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ApiClient {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiClient() {
        this("http://localhost:8080");
    }

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public CompletableFuture<List<AgentModel>> getAgents() {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/agents"))
                .GET()
                .build();
        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> parseJson(res.body(), new TypeReference<List<AgentModel>>() {}));
    }

    public CompletableFuture<AgentModel> createAgent(String name, String role, String model) {
        String json = toJson(Map.of("name", name, "role", role, "modelIdentifier", model));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/agents"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> parseJson(res.body(), new TypeReference<AgentModel>() {}));
    }

    public CompletableFuture<List<TaskModel>> getTasks() {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tasks"))
                .GET()
                .build();
        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> parseJson(res.body(), new TypeReference<List<TaskModel>>() {}));
    }

    public CompletableFuture<TaskModel> createTask(String title, String description, String agentId) {
        String json = toJson(Map.of("title", title, "description", description, "agentId", agentId));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> parseJson(res.body(), new TypeReference<TaskModel>() {}));
    }

    public CompletableFuture<ExecutionModel> executeTask(String taskId, String scenario) {
        String json = toJson(Map.of("scenario", scenario));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tasks/" + taskId + "/execute"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> parseJson(res.body(), new TypeReference<ExecutionModel>() {}));
    }

    public CompletableFuture<List<ExecutionModel>> getExecutions() {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/executions"))
                .GET()
                .build();
        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> parseJson(res.body(), new TypeReference<List<ExecutionModel>>() {}));
    }

    public CompletableFuture<ExecutionModel> getExecution(String id) {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/executions/" + id))
                .GET()
                .build();
        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> parseJson(res.body(), new TypeReference<ExecutionModel>() {}));
    }

    public CompletableFuture<List<ExecutionModel>> getChildExecutions(String parentId) {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/executions/" + parentId + "/children"))
                .GET()
                .build();
        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> parseJson(res.body(), new TypeReference<List<ExecutionModel>>() {}));
    }

    public CompletableFuture<List<AgentEventModel>> getExecutionTimeline(String executionId) {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/executions/" + executionId + "/timeline"))
                .GET()
                .build();
        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> parseJson(res.body(), new TypeReference<List<AgentEventModel>>() {}));
    }

    public CompletableFuture<IntegrityResultModel> verifyIntegrity(String executionId) {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/executions/" + executionId + "/verify-integrity"))
                .GET()
                .build();
        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> parseJson(res.body(), new TypeReference<IntegrityResultModel>() {}));
    }

    public CompletableFuture<StatisticsModel> getStatistics() {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/statistics"))
                .GET()
                .build();
        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> parseJson(res.body(), new TypeReference<StatisticsModel>() {}));
    }

    public CompletableFuture<ExecutionComparisonModel> compareExecutions(String exec1, String exec2) {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/executions/compare?exec1=" + exec1 + "&exec2=" + exec2))
                .GET()
                .build();
        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> parseJson(res.body(), new TypeReference<ExecutionComparisonModel>() {}));
    }

    public CompletableFuture<Void> tamperEvent(String executionId, int sequenceNumber, String modifiedDesc) {
        String json = toJson(Map.of("sequenceNumber", sequenceNumber, "modifiedDescription", modifiedDesc));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/dev/executions/" + executionId + "/tamper"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenAccept(res -> {});
    }

    public CompletableFuture<ExecutionModel> runDemoScenario(String scenario) {
        String json = toJson(Map.of("scenario", scenario));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/demo/run-scenario"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> parseJson(res.body(), new TypeReference<ExecutionModel>() {}));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON serialization error", e);
        }
    }

    private <T> T parseJson(String json, TypeReference<T> typeRef) {
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            throw new RuntimeException("JSON deserialization error: " + e.getMessage() + ", raw json: " + json, e);
        }
    }
}
