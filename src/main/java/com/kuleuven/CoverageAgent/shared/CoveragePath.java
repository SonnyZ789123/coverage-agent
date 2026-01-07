package com.kuleuven.CoverageAgent.shared;

public final class CoveragePath implements java.io.Serializable {
    public final String methodFullName;
    public final int[] instructionPath;
    public final int[] blockPath;

    public CoveragePath(String methodFullName, int[] instructionPath, int[] blockPath) {
        this.methodFullName = methodFullName;
        this.instructionPath = instructionPath;
        this.blockPath = blockPath;
    }
}
