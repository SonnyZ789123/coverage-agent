package com.kuleuven.CoverageAgent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kuleuven.CoverageAgent.shared.CoverageDump;
import com.kuleuven.CoverageAgent.shared.CoveragePath;
import com.kuleuven.CoverageAgent.shared.ExecutionCoveragePath;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
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

    // Multiple test threads may finish execution concurrently
    private static final List<ExecutionCoveragePath> executionPaths =
            Collections.synchronizedList(new ArrayList<>());

    // INVARIANT:
    // For each thread: one call stack → one subpath list → one execution path
    private static final ThreadLocal<List<CoveragePath>> paths =
            ThreadLocal.withInitial(ArrayList::new);

    private static final ThreadLocal<Deque<Frame>> stack =
            ThreadLocal.withInitial(ArrayDeque::new);

    private static final Gson GSON = new GsonBuilder().create();

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
                    List.copyOf(executionPaths)
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

    public static void hitBlocks(int[] blockIds) {
        Frame f = stack.get().peek();
        if (f != null) {
            f.blocks.addAll(IntList.of(blockIds));
        }
    }

    public static void startPath(String methodFullName) {
        stack.get().push(new Frame(methodFullName));
    }

    public static void endPath() {
        try {
            Frame f = stack.get().pop();

            paths.get().add(new CoveragePath(
                    f.methodId,
                    f.insns.toIntArray(),
                    f.blocks.toIntArray()
            ));

            // If the stack is empty, we completed a full execution path of an entry method.
            if (stack.get().isEmpty()) {
                executionPaths.add(new ExecutionCoveragePath(
                        f.methodId,
                        List.copyOf(paths.get())
                ));
                paths.get().clear();
            }
        } catch (NoSuchElementException e) {
            throw new IllegalStateException("Mismatched startPath/endPath calls", e);
        }
    }

}


