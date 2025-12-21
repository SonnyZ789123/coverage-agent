package com.kuleuven.CoverageAgent;

// Should match the BlockInfo of the call-graph-generator
public record BlockInfo(
        int blockId,
        String className,
        String methodName,
        String methodDescriptor,
        String stmtId,
        int lineNumber
) {}
