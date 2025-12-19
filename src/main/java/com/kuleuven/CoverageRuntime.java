package com.kuleuven;

import java.util.BitSet;

public final class CoverageRuntime {

    private static final BitSet blocks = new BitSet();

    public static void hit(int blockId) {
        blocks.set(blockId);
    }

    public static BitSet snapshot() {
        return (BitSet) blocks.clone();
    }

    public static void reset() {
        blocks.clear();
    }

    private static void dump(String path) {
        try {
            saveToFile(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveToFile(String path) throws Exception {
        java.nio.file.Files.createDirectories(
                java.nio.file.Path.of(path).getParent()
        );

        StringBuilder sb = new StringBuilder();

        for (int i = blocks.nextSetBit(0); i >= 0; i = blocks.nextSetBit(i + 1)) {
            sb.append(i).append('\n');
        }

        java.nio.file.Files.writeString(
                java.nio.file.Path.of(path),
                sb.toString()
        );
    }
}


