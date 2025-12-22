package com.kuleuven.CoverageAgent;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.jetbrains.annotations.NotNull;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CoverageRuntime {
    /** Current execution path (per invocation) */
    private static final ThreadLocal<IntArrayList> currentPath =
            ThreadLocal.withInitial(IntArrayList::new);

    private static final List<int[]> executionPaths =
            Collections.synchronizedList(new ArrayList<>());

    private static Path outputFile;

    public static void init(@NotNull String outputPath) {
        outputFile = Path.of(outputPath);

        Runtime.getRuntime().addShutdownHook(
                new Thread(CoverageRuntime::dump)
        );
    }

    private static void dump() {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(outputFile.toFile()))) {

            Files.createDirectories(outputFile.getParent());
            oos.writeObject(executionPaths);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hit(int blockId) {
        currentPath.get().add(blockId);
    }

    public static void startPath() {
        currentPath.get().clear();
    }

    public static void endPath() {
        int[] path = currentPath.get().stream().mapToInt(i -> i).toArray();
        executionPaths.add(path);
        currentPath.get().clear();
    }
}


