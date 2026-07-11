package org.bbva.mx.reader;

import org.bbva.mx.exceptions.FileNotFoundException;
import org.bbva.mx.reader.dto.ConfigurationsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ReadingJsonFile {
    private final ConfigurationsDTO configurations;
    private static final Logger log = LoggerFactory.getLogger(ReadingJsonFile.class);

    public ReadingJsonFile(ConfigurationsDTO configurations) {
        this.configurations = configurations;
    }

    public Path read() {
        Path sourceFile = this.validatingSourceFile();
        Path temporal = Path.of("archivo.tmp");
        try (BufferedReader reader = Files.newBufferedReader(sourceFile); BufferedWriter writer = Files.newBufferedWriter(temporal)) {
            reader.lines()
                    .map(j -> j.replace("^", "[").replace("!", "]"))
                    .forEach(j -> {
                        try {
                            writer.write(j);
                            writer.newLine();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
            return Files.move(temporal, sourceFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path validatingSourceFile() {
        log.info("Validating JSON file.");
        if (configurations != null && configurations.sourcePath() != null && !configurations.sourcePath().isBlank()) {
            Path source = Path.of(configurations.sourcePath());
            log.info("Successful JSON file validation");
            if (Files.isRegularFile(source)) return source;
        }
        throw new FileNotFoundException("File source not Found.");
    }
}
