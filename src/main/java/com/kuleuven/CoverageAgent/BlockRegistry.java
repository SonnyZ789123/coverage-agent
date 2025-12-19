package com.kuleuven.CoverageAgent;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public final class BlockRegistry {
    private static final String CFG_BLOCK_MAPPING_FILE_PATH = "./out/cfg_block_mapping.json";

    private static Map<Integer, BlockInfo> BLOCKS;

    private static final Map<String, Integer> LINE_LOOKUP = new HashMap<>();

    static {
        load();
        buildLineLookup();
    }

    private static void load() {
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<Integer, BlockInfo>>() {
            }.getType();

            try (InputStreamReader reader =
                         new InputStreamReader(
                                 java.nio.file.Files.newInputStream(
                                         java.nio.file.Path.of(CFG_BLOCK_MAPPING_FILE_PATH)))) {

                BLOCKS = gson.fromJson(reader, type);
            }
        } catch (Exception e) {
            System.err.printf("Failed to load JSON from path %s", CFG_BLOCK_MAPPING_FILE_PATH);
            throw new RuntimeException(e);
        }
    }

    private static void buildLineLookup() {
        for (Map.Entry<Integer, BlockInfo> e : BLOCKS.entrySet()) {
            BlockInfo info = e.getValue();
            String key = createKey(
                    info.className(),
                    info.methodName(),
                    info.methodDescriptor(),
                    info.lineNumber());

            LINE_LOOKUP.put(key, e.getKey());
        }
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
        return LINE_LOOKUP.get(key);
    }

    public static Map<Integer, BlockInfo> getBlocks() {
        return BLOCKS;
    }

    private BlockRegistry() {}
}
