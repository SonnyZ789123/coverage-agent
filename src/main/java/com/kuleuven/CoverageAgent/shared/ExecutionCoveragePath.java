package com.kuleuven.CoverageAgent.shared;

import java.util.List;

public class ExecutionCoveragePath {
    public final String entryMethodFullName;
    public final List<CoveragePath> coveragePaths;

    public ExecutionCoveragePath(String entryMethodFullName, List<CoveragePath> coveragePaths) {
        this.entryMethodFullName = entryMethodFullName;
        this.coveragePaths = coveragePaths;
    }
}
