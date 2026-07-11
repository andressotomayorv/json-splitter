package org.bbva.mx;

import org.bbva.mx.orchestrator.JsonSplitterOrchestrator;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static void main(String[] args) {
        JsonSplitterOrchestrator orchestrator = new JsonSplitterOrchestrator();
        orchestrator.execute(args);
    }
}
