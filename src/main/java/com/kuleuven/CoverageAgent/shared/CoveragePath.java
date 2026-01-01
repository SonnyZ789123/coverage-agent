package com.kuleuven.CoverageAgent.shared;

public final class CoveragePath implements java.io.Serializable {
    public final int[] instructionPath;
    public final int[] blockPath;

    public CoveragePath(int[] instructionPath, int[] blockPath) {
        this.instructionPath = instructionPath;
        this.blockPath = blockPath;
    }
}
