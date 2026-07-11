package org.bbva.mx.writing;

import org.bbva.mx.reader.dto.ConfigurationsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.*;
import java.util.stream.Stream;

public class WritingJSONFiles {

    private static final Logger log = LoggerFactory.getLogger(WritingJSONFiles.class);
    private final Path sourceFile;
    private final ConfigurationsDTO configurations;
    private final ObjectMapper mapper = new ObjectMapper();

    private final ThreadPoolExecutor executor =
            new ThreadPoolExecutor(
                    Runtime.getRuntime().availableProcessors(),
                    Runtime.getRuntime().availableProcessors(),
                    0L,
                    TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(500),
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );

    public WritingJSONFiles(Path sourceFile, ConfigurationsDTO configurations) {
        this.sourceFile = sourceFile;
        this.configurations = configurations;
    }

    public void write() {

        Path target = validateTargetPath();

        try (JsonParser parser = mapper.createParser(sourceFile)) {
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new IllegalStateException("Expected JSON array");
            }
            int counter = 0;

            while (parser.nextToken() != JsonToken.END_ARRAY) {
                JsonNode node = parser.readValueAsTree();
                Path file = target.resolve("file_" + (++counter) + ".json");
                executor.execute(() -> mapper.writeValue(file.toFile(), node));
            }

        }

        executor.shutdown();

        try {
            if (!executor.awaitTermination(2, TimeUnit.HOURS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            log.info("Process completed");
        }
    }

    private Path validateTargetPath() {

        if (configurations == null
                || configurations.targetPath() == null
                || configurations.targetPath().isBlank()) {

            throw new IllegalArgumentException("Target directory not found");
        }

        Path target = Path.of(configurations.targetPath());

        if (!Files.isDirectory(target)) {
            throw new IllegalArgumentException("Target directory not found");
        }

        deleteFilesInTheDestinationDirectory(target);

        return target;
    }

    private void deleteFilesInTheDestinationDirectory(Path target) {

        try (Stream<Path> files = Files.walk(target)) {

            files.filter(path -> path.toString().endsWith(".json"))
                    .forEach(path ->  executor.submit(()-> Files.deleteIfExists(path)));

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}