package org.bbva.mx.writing;

import org.bbva.mx.reader.dto.ConfigurationsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class WritingJSONFiles {

    private static final Logger log = LoggerFactory.getLogger(WritingJSONFiles.class);

    private static final int THREADS = Runtime.getRuntime().availableProcessors();
    private static final int QUEUE_SIZE = 500;
    private static final long TIMEOUT = 2L;

    private final Path sourceFile;
    private final ConfigurationsDTO configurations;
    private final ObjectMapper mapper = new ObjectMapper();

    private final ThreadPoolExecutor executor =
            new ThreadPoolExecutor(
                    THREADS,
                    THREADS,
                    0L,
                    TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(QUEUE_SIZE),
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );

    public WritingJSONFiles(Path sourceFile, ConfigurationsDTO configurations) {
        this.sourceFile = sourceFile;
        this.configurations = configurations;
    }

    public void write() {

        log.info("Starting JSON split process.");

        Path target = validateTargetPath();

        try (JsonParser parser = mapper.createParser(sourceFile)) {
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new IllegalStateException("Expected a JSON array.");
            }
            int counter = 0;

            while (parser.nextToken() != JsonToken.END_ARRAY) {
                JsonNode node = parser.readValueAsTree();
                Path outputFile = target.resolve("file_" + (++counter) + ".json");
                executor.execute(() -> writeJsonFile(outputFile, node));
            }

        } catch (JacksonException e) {
            log.error(e.getMessage());
        } finally {
            shutdownExecutor();
        }
        log.info("JSON split completed successfully.");
    }

    private void writeJsonFile(Path file, JsonNode node) {

        mapper.writeValue(file.toFile(), node);

    }

    private void shutdownExecutor() {

        executor.shutdown();

        try {

            if (!executor.awaitTermination(TIMEOUT, TimeUnit.HOURS)) {

                log.warn("Timeout reached. Forcing executor shutdown.");
                executor.shutdownNow();
            }

        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            log.error("Executor interrupted while shutting down.", e);
        }
    }

    private Path validateTargetPath() {

        log.info("Validating target directory.");

        if (configurations == null
                || configurations.targetPath() == null
                || configurations.targetPath().isBlank()) {

            throw new IllegalArgumentException("Target directory not found.");
        }

        Path target = Path.of(configurations.targetPath());

        if (!Files.isDirectory(target)) {
            throw new IllegalArgumentException("Target directory not found.");
        }

        deleteExistingJsonFiles(target);

        log.info("Target directory validated successfully.");

        return target;
    }

    private void deleteExistingJsonFiles(Path target) {

        log.info("Deleting existing JSON files.");

        try (Stream<Path> files = Files.walk(target)) {
            files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        } catch (IOException | UncheckedIOException e) {
            throw new IllegalStateException("Error deleting existing JSON files.", e);
        }
    }
}