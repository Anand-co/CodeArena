package com.codearena.codearena_backend.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.codearena.codearena_backend.entity.SubmissionStatus;
import com.codearena.codearena_backend.entity.TestCase;
import com.codearena.codearena_backend.repository.TestCaseRepository;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class Judge0Service {

    private final TestCaseRepository testCaseRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${judge0.base-url:https://ce.judge0.com}")
    private String baseUrl;

    @Value("${judge0.api-key:}")
    private String apiKey;

    @Value("${judge0.poll-interval-ms:500}")
    private long pollIntervalMs;

    @Value("${judge0.max-poll-attempts:40}")
    private int maxPollAttempts;

    public Judge0Service(TestCaseRepository testCaseRepository) {
        this.testCaseRepository = testCaseRepository;
    }

    public Judge0Evaluation evaluate(
            Long problemId,
            String language,
            String sourceCode) {

        List<TestCase> testCases =
                testCaseRepository.findByProblemIdOrderByIdAsc(problemId);

        if (testCases.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This problem has no test cases configured");
        }

        Judge0Evaluation evaluation = new Judge0Evaluation();
        evaluation.setTotalTestCases(testCases.size());
        evaluation.setPassedTestCases(0);
        evaluation.setExecutionTimeSeconds(0.0);
        evaluation.setMemoryUsageKb(0L);

        boolean wrongAnswer = false;

        for (TestCase testCase : testCases) {
            JsonNode result = executeTestCase(
                    getLanguageId(language),
                    sourceCode,
                    testCase.getInput(),
                    testCase.getExpectedOutput());

            int statusId = result.path("status_id").asInt(-1);

            double time = result.path("time").asDouble(0.0);
            evaluation.setExecutionTimeSeconds(
                    evaluation.getExecutionTimeSeconds() + time);

            long memory = result.path("memory").asLong(0L);
            evaluation.setMemoryUsageKb(
                    Math.max(evaluation.getMemoryUsageKb(), memory));

            if (statusId == 3) {
                evaluation.setPassedTestCases(
                        evaluation.getPassedTestCases() + 1);
                continue;
            }

            if (statusId == 4) {
                wrongAnswer = true;
                if (evaluation.getErrorMessage() == null) {
                    evaluation.setErrorMessage(
                            "Output did not match the expected result.");
                }
                continue;
            }

            if (statusId == 5) {
                evaluation.setStatus(
                        SubmissionStatus.TIME_LIMIT_EXCEEDED);
                evaluation.setErrorMessage("Execution time limit exceeded.");
                return evaluation;
            }

            if (statusId == 6) {
                evaluation.setStatus(
                        SubmissionStatus.COMPILATION_ERROR);
                evaluation.setErrorMessage(
                        readText(result, "compile_output", "Compilation failed."));
                return evaluation;
            }

            if (statusId >= 7 && statusId <= 14) {
                evaluation.setStatus(SubmissionStatus.RUNTIME_ERROR);
                evaluation.setErrorMessage(
                        readText(result, "stderr", "Program execution failed."));
                return evaluation;
            }

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Judge0 returned an unexpected execution status");
        }

        evaluation.setStatus(
                wrongAnswer
                        ? SubmissionStatus.WRONG_ANSWER
                        : SubmissionStatus.ACCEPTED);

        return evaluation;
    }

    private JsonNode executeTestCase(
            int languageId,
            String sourceCode,
            String input,
            String expectedOutput) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            if (apiKey != null && !apiKey.isBlank()) {
                headers.set("X-Auth-Token", apiKey);
            }

            Map<String, Object> body = Map.of(
                    "source_code", sourceCode,
                    "language_id", languageId,
                    "stdin", input == null ? "" : input,
                    "expected_output", expectedOutput == null ? "" : expectedOutput,
                    "cpu_time_limit", 2.0,
                    "wall_time_limit", 5.0,
                    "memory_limit", 128000);

            String createUrl = baseUrl
                    + "/submissions/?base64_encoded=false&wait=false";

            ResponseEntity<JsonNode> created = restTemplate.postForEntity(
                    createUrl,
                    new HttpEntity<>(body, headers),
                    JsonNode.class);

            JsonNode createdBody = created.getBody();

            if (createdBody == null || !createdBody.hasNonNull("token")) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Judge0 did not return an execution token");
            }

            String token = createdBody.get("token").asText();

            String resultUrl = baseUrl
                    + "/submissions/" + token
                    + "?base64_encoded=false"
                    + "&fields=stdout,stderr,status_id,time,memory,compile_output,message";

            HttpEntity<Void> request = new HttpEntity<>(headers);

            for (int attempt = 0; attempt < maxPollAttempts; attempt++) {
                Thread.sleep(pollIntervalMs);

                ResponseEntity<JsonNode> response = restTemplate.exchange(
                        resultUrl, HttpMethod.GET, request, JsonNode.class);

                JsonNode result = response.getBody();

                if (result == null || !result.has("status_id")) {
                    continue;
                }

                int statusId = result.path("status_id").asInt(-1);

                // Status 1 = queued; status 2 = processing.
                if (statusId != 1 && statusId != 2) {
                    return result;
                }
            }

            throw new ResponseStatusException(
                    HttpStatus.GATEWAY_TIMEOUT,
                    "Judge0 took too long to return a result");

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Code execution was interrupted");

        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Could not communicate with Judge0");
        }
    }

    private String readText(
            JsonNode result,
            String field,
            String fallback) {

        String value = result.path(field).asText("");
        return value.isBlank() ? fallback : value;
    }

    private int getLanguageId(String language) {
        if (language == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Language is required");
        }

        return switch (language.trim().toLowerCase()) {
            case "java" -> 62;
            case "python", "python3" -> 71;
            case "cpp", "c++" -> 54;
            case "javascript", "js" -> 63;
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported language");
        };
    }
}