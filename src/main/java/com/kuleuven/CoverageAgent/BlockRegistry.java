package com.kuleuven.CoverageAgent;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;

public final class BlockRegistry {
    private record MethodScope(
            String className,
            String methodName,
            String methodDescriptor
    ) {}

    private static Map<Integer, BlockInfo> blockInfoMap;

    private static final Map<String, Integer> lineLookup = new HashMap<>();

    private static final Set<MethodScope> SUTScope = new HashSet<>();

    public static void init(@NotNull String blockMapPath) {
        load(blockMapPath);
        buildLineLookup();
        buildSUTScope();
    }

    private static void load(@NotNull String blockMapPath) {
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<Integer, BlockInfo>>() {
            }.getType();

            try (InputStreamReader reader =
                         new InputStreamReader(
                                 java.nio.file.Files.newInputStream(
                                         java.nio.file.Path.of(blockMapPath)))) {

                blockInfoMap = gson.fromJson(reader, type);
            }
        } catch (Exception e) {
            System.err.printf("Failed to load JSON from path %s", blockMapPath);
            throw new RuntimeException(e);
        }
    }

    private static void buildLineLookup() {
        for (Map.Entry<Integer, BlockInfo> e : blockInfoMap.entrySet()) {
            BlockInfo info = e.getValue();
            String key = createKey(
                    info.className(),
                    info.methodName(),
                    info.methodDescriptor(),
                    info.lineNumber());

            lineLookup.put(key, e.getKey());
        }
    }

    private static void buildSUTScope() {
        for (BlockInfo info : blockInfoMap.values()) {
            MethodScope scope = new MethodScope(
                    info.className(),
                    info.methodName(),
                    info.methodDescriptor()
            );
            SUTScope.add(scope);
        }
    }

    public static boolean isInSUTScope(
            String className,
            String methodName,
            String methodDescriptor
    ) {
        MethodScope scope = new MethodScope(
                className,
                methodName,
                methodDescriptor
        );
        return SUTScope.contains(scope);
    }

    public static String createKey(
            String className,
            String methodName,
            String methodDescriptor,
            int lineNumber
    ) {
        return className + "|" +
                methodName + "|" +
                methodDescriptor + "|" +
                lineNumber;
    }

    public static Integer lookupByLine(
            String className,
            String methodName,
            String methodDescriptor,
            int lineNumber
    ) {
        String key = createKey(className, methodName, methodDescriptor, lineNumber);
        return lineLookup.get(key);
    }

    public static Map<Integer, BlockInfo> getBlocks() {
        return blockInfoMap;
    }

    private BlockRegistry() {}
}
