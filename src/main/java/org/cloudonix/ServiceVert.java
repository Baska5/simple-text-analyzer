package org.cloudonix;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.TreeSet;

public class ServiceVert extends AbstractVerticle {
    private Router router;
    private TreeSet<String> words;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        words = new TreeSet<>();

        router = Router.router(vertx);
        router.route(HttpMethod.POST, "/analyze")
                .handler(BodyHandler.create())
                .handler(context -> {
                    String inputText = context.body().asJsonObject().getString("text");
                    String closestValueWord = null;
                    String closestLexicalWord = null;

                    if (inputText != null && !inputText.isBlank() && inputText.matches("[a-zA-Z]+")) {
                        inputText = inputText.toLowerCase();
                        closestValueWord = findClosestValueWord(inputText);
                        closestLexicalWord = findClosestLexicalWord(inputText);
                        words.add(inputText);
                    }

                    JsonObject responseJson = new JsonObject()
                            .put("value", closestValueWord)
                            .put("lexical", closestLexicalWord);

                    HttpServerResponse response = context.response();
                    response.putHeader("Content-Type", "application/json");
                    response.end(responseJson.encode());

                    saveWordsToFile();
                });

        vertx.fileSystem().exists("words.txt", existsResult -> {
            if (existsResult.succeeded() && existsResult.result()) {
                vertx.fileSystem().readFile("words.txt", readResult -> {
                    if (readResult.succeeded()) {
                        loadWords(readResult.result().toString());
                        startServer(startPromise);
                    } else {
                        startPromise.fail(readResult.cause());
                    }
                });
            } else {
                startServer(startPromise);
            }
        });
    }

    private void startServer(Promise<Void> startPromise) {
        vertx.createHttpServer().requestHandler(router)
                .listen(config().getInteger("http.port", 8080))
                .onSuccess(server -> {
                    startPromise.complete();
                })
                .onFailure(startPromise::fail);
    }

    private String findClosestValueWord(String inputText) {
        if (words.isEmpty()) {
            return null;
        }
        if (words.contains(inputText)) {
            return inputText;
        }
        int inputValue = calculateValue(inputText);
        int minDifference = Integer.MAX_VALUE;
        String closestWord = null;

        for (String word : words) {
            int wordValue = calculateValue(word);
            int difference = Math.abs(inputValue - wordValue);

            if (difference < minDifference) {
                minDifference = difference;
                closestWord = word;
            }
        }
        return closestWord;
    }

    private String findClosestLexicalWord(String inputText) {
        if (words.isEmpty()) {
            return null;
        }
        if (words.contains(inputText)) {
            return inputText;
        }
        String closestWord = null;
        String greaterOrEqual = words.ceiling(inputText);
        String lessOrEqual = words.floor(inputText);
        if (greaterOrEqual != null && lessOrEqual != null) {
            int diff1 = Math.abs(greaterOrEqual.length() - inputText.length());
            int diff2 = Math.abs(lessOrEqual.length() - inputText.length());
            closestWord = (diff1 <= diff2) ? greaterOrEqual : lessOrEqual;
        } else if (greaterOrEqual != null) {
            closestWord = greaterOrEqual;
        } else if (lessOrEqual != null) {
            closestWord = lessOrEqual;
        }
        return closestWord;
    }

    private int calculateValue(String word) {
        int value = 0;
        for (char c : word.toCharArray()) {
            if (Character.isLetter(c)) {
                value += (int) c - 96;
            }
        }
        return value;
    }

    private void loadWords(String fileWords) {
        String[] lines = fileWords.split(System.lineSeparator());

        for (String line : lines) {
            if (!line.isEmpty()) {
                words.add(line);
            }
        }
    }

    private void saveWordsToFile() {
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            sb.append(word).append(System.lineSeparator());
        }
        Buffer buffer = Buffer.buffer(sb.toString());
        vertx.fileSystem().writeFile("words.txt", buffer, ar -> {
            if (ar.failed()) {
                ar.cause().printStackTrace();
            }
        });
    }
}