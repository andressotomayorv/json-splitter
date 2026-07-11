package org.bbva.mx.reader;

import org.bbva.mx.exceptions.FileNotFoundException;
import org.bbva.mx.reader.dto.ConfigurationsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;

public class ReadingConfigurationFile {
    private static final Logger LOG = LoggerFactory.getLogger(ReadingConfigurationFile.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final String[] arguments;

    public ReadingConfigurationFile(String[] arguments) {
        this.arguments = arguments;
    }

    public ConfigurationsDTO reading() {
        LOG.info("Starting to read configuration file");
        if (arguments != null && arguments.length > 0) {
            String sourceConfigurationPath = arguments[0];
            if (sourceConfigurationPath != null && !sourceConfigurationPath.isBlank()) {
                Path configurationFile = Path.of(arguments[0]);
                if (!Files.isRegularFile(configurationFile)) throw new FileNotFoundException("Invalid configuration file");
                mapper.readTree(configurationFile);
                LOG.info("Configuration file successfully validated.");
                return mapper.readValue(configurationFile, ConfigurationsDTO.class);
            } throw new IllegalArgumentException("Invalid configuration file path.");
        }
        throw new IllegalArgumentException("No input arguments were found.");
    }
}
