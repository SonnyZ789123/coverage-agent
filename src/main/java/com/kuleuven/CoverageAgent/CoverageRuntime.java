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
import java.util.*;

public final class CoverageRuntime {
    private static final class Frame {
        final String methodId;
        final IntArrayList insns = new IntArrayList();
        final IntArrayList blocks = new IntArrayList();

        Frame(String methodId) {
            this.methodId = methodId;
        }
    }

    private static final List<CoveragePath> paths =
            Collections.synchronizedList(new ArrayList<>());

    private static final ThreadLocal<Deque<Frame>> stack =
            ThreadLocal.withInitial(ArrayDeque::new);

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
        Frame f = stack.get().peek();
        if (f != null) {
            f.insns.add(insnIdx);
        }
    }

    public static void hitBlock(int blockId) {
        Frame f = stack.get().peek();
        if (f != null) {
            f.blocks.add(blockId);
        }
    }

    public static void startPath(String methodFullName) {
        stack.get().push(new Frame(methodFullName));
    }

    public static void endPath() {
        Frame f = stack.get().poll();
        if (f == null) return;

        paths.add(new CoveragePath(
                f.methodId,
                f.insns.toIntArray(),
                f.blocks.toIntArray()
        ));
    }

}


