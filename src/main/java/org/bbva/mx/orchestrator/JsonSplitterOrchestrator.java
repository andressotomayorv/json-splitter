package org.bbva.mx.orchestrator;

import org.bbva.mx.reader.ReadingConfigurationFile;
import org.bbva.mx.reader.ReadingJsonFile;
import org.bbva.mx.reader.dto.ConfigurationsDTO;
import org.bbva.mx.writing.WritingJSONFiles;

public class JsonSplitterOrchestrator {

    public void execute(String[] args) {
        ConfigurationsDTO configurationFile = new ReadingConfigurationFile(args).reading();
        ReadingJsonFile jsonFile = new ReadingJsonFile(configurationFile);
        WritingJSONFiles write = new WritingJSONFiles(jsonFile.read(), configurationFile);
        write.write();
    }
}
