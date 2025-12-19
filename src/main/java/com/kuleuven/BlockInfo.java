package com.kuleuven;

// Should match the representation that is used in ASM instrumentation
public record BlockInfo(
        String className,
        String methodName,
        String methodDescriptor,
        String stmt,
        int lineNumber
) {}
