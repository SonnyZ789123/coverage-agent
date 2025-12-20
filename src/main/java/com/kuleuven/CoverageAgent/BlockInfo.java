package com.kuleuven.CoverageAgent;

// Should match the BlockInfo of the call-graph-generator
public record BlockInfo(
        String className,
        String methodName,
        String methodDescriptor,
        String stmt,
        int lineNumber
) {}
