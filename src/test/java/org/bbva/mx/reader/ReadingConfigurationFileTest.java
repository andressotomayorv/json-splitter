package org.bbva.mx.reader;

import org.bbva.mx.exceptions.FileNotFoundException;
import org.bbva.mx.reader.dto.ConfigurationsDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ReadingConfigurationFileTest {
    @ParameterizedTest
    @MethodSource(value = "nullAndLengthZeroArguments")
    void shouldThrowIllegalArgumentExceptionWhenArgumentsAreInvalid(String[] args) {
        ReadingConfigurationFile readingConfigurations = new ReadingConfigurationFile(args);
        String message = assertThrows(IllegalArgumentException.class, readingConfigurations::reading).getMessage();
        assertEquals("No input arguments were found.", message);

    }

    @ParameterizedTest
    @MethodSource(value = "nullAndBlankSourceConfigurationFile")
    void shouldThrowIllegalArgumentExceptionWhenSourceAreInvalid(String[] source) {
        ReadingConfigurationFile readingConfigurations = new ReadingConfigurationFile(source);
        String message = assertThrows(IllegalArgumentException.class, readingConfigurations::reading).getMessage();
        assertEquals("Invalid configuration file path.", message);

    }

    @Test
    void shouldThrowFileNotFoundExceptionWhenConfigurationFileSourceIsInvalid() {
        ReadingConfigurationFile readingConfigurations = new ReadingConfigurationFile(new String[]{"not-found.json"});
        String message = assertThrows(FileNotFoundException.class, readingConfigurations::reading).getMessage();
        assertEquals("Invalid configuration file", message);
    }

    @Test
    void shouldReturnAConfigurationsDTOWhenSourceIsValid() {
        ReadingConfigurationFile readingConfigurations = new ReadingConfigurationFile(new String[]{"src/main/resources/configurations-valid.json"});
        ConfigurationsDTO configurationFile = readingConfigurations.reading();
        assertNotNull(configurationFile);
        assertNotNull(configurationFile.sourcePath());
        assertTrue(Files.exists(Path.of(configurationFile.sourcePath())));
    }

    static Stream<Arguments> nullAndLengthZeroArguments() {
        return Stream.of(
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) null)
        );
    }

    static Stream<Arguments> nullAndBlankSourceConfigurationFile() {
        return Stream.of(
                Arguments.of((Object) new String[]{""}),
                Arguments.of((Object) new String[]{null}),
                Arguments.of((Object) new String[]{" "})
        );
    }

}