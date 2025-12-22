package com.kuleuven.CoverageAgent.shared;

// Should match the BlockInfo of the pathcov
public record BlockInfo(
        int blockId,
        String className,
        String methodName,
        String methodDescriptor,
        String stmtId,
        int lineNumber
) {}
