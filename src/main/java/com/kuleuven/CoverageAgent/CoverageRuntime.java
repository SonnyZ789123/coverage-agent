package com.kuleuven.CoverageAgent;

import org.jetbrains.annotations.NotNull;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.BitSet;

public final class CoverageRuntime {

    private static final BitSet blocks = new BitSet();
    private static Path outputFile;

    public static void init(@NotNull String outputPath) {
        outputFile = Path.of(outputPath);

        Runtime.getRuntime().addShutdownHook(
                new Thread(CoverageRuntime::dump)
        );
    }

    public static void hit(int blockId) {
        blocks.set(blockId);
    }

    public static BitSet snapshot() {
        return (BitSet) blocks.clone();
    }

    public static void reset() {
        blocks.clear();
    }

    private static void dump() {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(outputFile.toFile()))) {

            oos.writeObject(blocks);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


