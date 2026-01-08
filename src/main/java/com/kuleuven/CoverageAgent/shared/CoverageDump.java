package com.kuleuven.CoverageAgent.shared;

import java.util.List;

public final class CoverageDump {
    public final int version;
    public final List<ExecutionCoveragePath> executionPaths;

    public CoverageDump(int version, List<ExecutionCoveragePath> executionPaths) {
        this.version = version;
        this.executionPaths = executionPaths;
    }
}
