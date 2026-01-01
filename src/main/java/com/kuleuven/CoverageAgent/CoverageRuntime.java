package com.kuleuven.CoverageAgent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kuleuven.CoverageAgent.shared.CoverageDump;
import com.kuleuven.CoverageAgent.shared.CoveragePath;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CoverageRuntime {
    private static final List<CoveragePath> paths =
            Collections.synchronizedList(new ArrayList<>());

    /** Current execution path of instruction indexes (per invocation) */
    private static final ThreadLocal<IntArrayList> currentInstructionPath =
            ThreadLocal.withInitial(IntArrayList::new);

    /** Current execution path of instruction indexes (per invocation) */
    private static final ThreadLocal<IntArrayList> currentBlockPath =
            ThreadLocal.withInitial(IntArrayList::new);

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()   // remove if you want compact output
            .create();

    private static Path outputFile;

    public static void init(@NotNull String outputPath) {
        outputFile = Path.of(outputPath);

        Runtime.getRuntime().addShutdownHook(
                new Thread(CoverageRuntime::dump)
        );
    }

    private static void dump() {
        try {
            Files.createDirectories(outputFile.getParent());

            CoverageDump dump = new CoverageDump(
                    1,
                    List.copyOf(paths)
            );

            try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
                GSON.toJson(dump, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hitInstruction(int insnIdx) {
        currentInstructionPath.get().add(insnIdx);
    }

    public static void hitBlock(int blockId) {
        currentBlockPath.get().add(blockId);
    }

    public static void startPath() {
        currentInstructionPath.get().clear();

        currentBlockPath.get().clear();
    }

    public static void endPath() {
        int[] instructionPath = currentInstructionPath.get().toIntArray();
        int[] blockPath = currentBlockPath.get().toIntArray();

        paths.add(new CoveragePath(instructionPath, blockPath));

        currentInstructionPath.get().clear();
        currentBlockPath.get().clear();
    }

}


