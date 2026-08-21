/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.beust.jcommander.JCommander
 */
package com.google.zxing.client.j2se;

import com.beust.jcommander.JCommander;
import com.google.zxing.client.j2se.DecodeWorker;
import com.google.zxing.client.j2se.DecoderConfig;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class CommandLineRunner {
    private CommandLineRunner() {
    }

    public static void main(String[] args2) throws Exception {
        DecoderConfig config = new DecoderConfig();
        JCommander jCommander = new JCommander((Object)config, args2);
        jCommander.setProgramName(CommandLineRunner.class.getSimpleName());
        if (config.help) {
            jCommander.usage();
            return;
        }
        List<URI> inputs = config.inputPaths;
        do {
            inputs = CommandLineRunner.retainValid(CommandLineRunner.expand(inputs), config.recursive);
        } while (config.recursive && CommandLineRunner.isExpandable(inputs));
        int numInputs = inputs.size();
        if (numInputs == 0) {
            jCommander.usage();
            return;
        }
        ConcurrentLinkedQueue<URI> syncInputs = new ConcurrentLinkedQueue<URI>(inputs);
        int numThreads = Math.min(numInputs, Runtime.getRuntime().availableProcessors());
        int successful = 0;
        if (numThreads > 1) {
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            ArrayList<Future<Integer>> futures = new ArrayList<Future<Integer>>(numThreads);
            for (int x = 0; x < numThreads; ++x) {
                futures.add(executor.submit(new DecodeWorker(config, syncInputs)));
            }
            executor.shutdown();
            for (Future future : futures) {
                successful += ((Integer)future.get()).intValue();
            }
        } else {
            successful += new DecodeWorker(config, syncInputs).call().intValue();
        }
        if (!config.brief && numInputs > 1) {
            System.out.println("\nDecoded " + successful + " files out of " + numInputs + " successfully (" + successful * 100 / numInputs + "%)\n");
        }
    }

    private static List<URI> expand(List<URI> inputs) throws IOException, URISyntaxException {
        ArrayList<URI> expanded = new ArrayList<URI>();
        for (URI input : inputs) {
            if (CommandLineRunner.isFileOrDir(input)) {
                Path inputPath = Paths.get(input);
                if (Files.isDirectory(inputPath, new LinkOption[0])) {
                    DirectoryStream<Path> childPaths = Files.newDirectoryStream(inputPath);
                    Throwable throwable = null;
                    try {
                        for (Path childPath : childPaths) {
                            expanded.add(childPath.toUri());
                        }
                        continue;
                    }
                    catch (Throwable throwable2) {
                        throwable = throwable2;
                        throw throwable2;
                    }
                    finally {
                        if (childPaths == null) continue;
                        if (throwable != null) {
                            try {
                                childPaths.close();
                            }
                            catch (Throwable throwable3) {
                                throwable.addSuppressed(throwable3);
                            }
                            continue;
                        }
                        childPaths.close();
                        continue;
                    }
                }
                expanded.add(input);
                continue;
            }
            expanded.add(input);
        }
        for (int i = 0; i < expanded.size(); ++i) {
            URI input;
            input = (URI)expanded.get(i);
            if (input.getScheme() != null) continue;
            expanded.set(i, new URI("file", input.getSchemeSpecificPart(), input.getFragment()));
        }
        return expanded;
    }

    private static List<URI> retainValid(List<URI> inputs, boolean recursive) {
        ArrayList<URI> retained = new ArrayList<URI>();
        for (URI input : inputs) {
            Path inputPath;
            boolean retain = CommandLineRunner.isFileOrDir(input) ? !(inputPath = Paths.get(input)).getFileName().toString().startsWith(".") && (recursive || !Files.isDirectory(inputPath, new LinkOption[0])) : true;
            if (!retain) continue;
            retained.add(input);
        }
        return retained;
    }

    private static boolean isExpandable(List<URI> inputs) {
        for (URI input : inputs) {
            if (!CommandLineRunner.isFileOrDir(input) || !Files.isDirectory(Paths.get(input), new LinkOption[0])) continue;
            return true;
        }
        return false;
    }

    private static boolean isFileOrDir(URI uri) {
        return "file".equals(uri.getScheme());
    }
}

