package org.bbva.mx.reader;

import org.bbva.mx.exceptions.FileNotFoundException;
import org.bbva.mx.reader.dto.ConfigurationsDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ReadingJsonFileTest {


    @ParameterizedTest
    @MethodSource("configurationInvalid")
    void shouldThrowFileNotFoundExceptionWhenConfigurationFileIsInvalid(ConfigurationsDTO configurationsDTO) {
        ReadingJsonFile readingJsonFile = new ReadingJsonFile(configurationsDTO);
        assertThrows(FileNotFoundException.class, readingJsonFile::read);
    }

    @Test
    void shouldThrowIOExceptionWhenAttributeSourceInConfigurationsFilesIsInvalid() {
        String sourcePath = "configurations-invalid.json";
        String targetPath = "target-test-invalid";
        ConfigurationsDTO configurations = new ConfigurationsDTO(sourcePath, targetPath);
        ReadingJsonFile readingJsonFile = new ReadingJsonFile(configurations);
        assertThrows(IllegalStateException.class, readingJsonFile::read);
    }

    @ParameterizedTest
    @MethodSource(value = "sourceJsonFileValid")
    void shouldCompleteSuccessfullyWhenTheJSONSourceIsValid(String sourceJsonFile) {
        String targetPath = "rc/main/resources/target-test";
        ConfigurationsDTO configurations = new ConfigurationsDTO(sourceJsonFile, targetPath);
        ReadingJsonFile readingJsonFile = new ReadingJsonFile(configurations);
        Path path = readingJsonFile.read();
        assertNotNull(path);
    }


    static Stream<Arguments> configurationInvalid() {
        ConfigurationsDTO configurationNull = new ConfigurationsDTO(null, null);
        ConfigurationsDTO configurationEmpty = new ConfigurationsDTO("", "");
        ConfigurationsDTO configurationBlank = new ConfigurationsDTO(" ", " ");
        return Stream.of(
                Arguments.of((Object) null),
                Arguments.of(configurationNull),
                Arguments.of(configurationEmpty),
                Arguments.of(configurationBlank)
        );
    }


    static Stream<Arguments> sourceJsonFileValid() {
        return Stream.of(
                Arguments.of("src/main/resources/invalid-bhs.json"),
                Arguments.of("src/main/resources/valid-bhs.json")
        );
    }


}