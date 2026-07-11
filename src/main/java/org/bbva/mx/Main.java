package org.bbva.mx;

import org.bbva.mx.orchestrator.JsonSplitterOrchestrator;

public class Main {
    static void main(String[] args) {
        JsonSplitterOrchestrator orchestrator = new JsonSplitterOrchestrator();
        orchestrator.execute(args);
    }
}
