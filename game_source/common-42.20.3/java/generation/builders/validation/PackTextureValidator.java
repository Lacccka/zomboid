/*
 * Decompiled with CFR 0.152.
 */
package generation.builders.validation;

import com.google.common.base.Predicates;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

public class PackTextureValidator {
    private static final String DEFAULT = "default";
    public static final Set<String> PACK_TEXTURES = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    private static final int VERS = 1936876918;
    private static final int ION = 544108393;
    private static final int HTTP = 1886680168;
    private static final int PZPK = 1263557200;
    private static final Set<Path> UNUSED = Set.of(Paths.get("media/texturepacks/ApCom_old.pack", new String[0]), Paths.get("media/texturepacks/blair_temp.pack", new String[0]), Paths.get("media/texturepacks/Characters.pack", new String[0]), Paths.get("media/texturepacks/DepthMaps2x.pack", new String[0]), Paths.get("media/texturepacks/ApCom.pack", new String[0]), Paths.get("media/texturepacks/Erosion.pack", new String[0]));
    private final String name;

    public static PackTextureValidator of(String name, String prefix) {
        return new PackTextureValidator(name, prefix);
    }

    public static PackTextureValidator of(String name) {
        return new PackTextureValidator(name, null);
    }

    public static PackTextureValidator icon(String name) {
        return new PackTextureValidator(name, "Item_");
    }

    private PackTextureValidator(String name, String prefix) {
        this.name = name;
        if (prefix != null) {
            if (!(DEFAULT.equals(name) || PackTextureValidator.inPackOrOnDisk(prefix + name) || PackTextureValidator.inPackOrOnDisk(name))) {
                System.err.println("Missing %stexture: %s".formatted(prefix.isEmpty() ? "" : " %s".formatted(prefix), name));
            }
        } else if (!PACK_TEXTURES.contains(name)) {
            System.err.println("Missing texture: %s".formatted(name));
        }
    }

    private static boolean inPackOrOnDisk(String name) {
        return PACK_TEXTURES.contains(name) || Files.exists(Paths.get("media/textures/%s.png".formatted(name), new String[0]), new LinkOption[0]);
    }

    public String toString() {
        return this.name;
    }

    public static Set<String> getPackTiles(Path path) {
        ToIntFunction<InputStream> readInt = in -> {
            try {
                return in.read() | in.read() << 8 | in.read() << 16 | in.read() << 24;
            }
            catch (IOException e) {
                return -1;
            }
        };
        Function<InputStream, String> readString = in -> {
            try {
                StringBuilder builder = new StringBuilder();
                int size = readInt.applyAsInt((InputStream)in);
                for (int i = 0; i < size; ++i) {
                    builder.append((char)in.read());
                }
                return builder.toString();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        HashSet<String> output = new HashSet<String>();
        try (BufferedInputStream in2 = new BufferedInputStream(Files.newInputStream(path, new OpenOption[0]));){
            int pageCount;
            int version;
            int data = readInt.applyAsInt(in2);
            if (data == 1263557200) {
                version = readInt.applyAsInt(in2);
                pageCount = readInt.applyAsInt(in2);
            } else {
                if (data == 1936876918) {
                    ((InputStream)in2).mark(8);
                    if (readInt.applyAsInt(in2) == 544108393 && readInt.applyAsInt(in2) == 1886680168) {
                        throw new RuntimeException("git lfs might not be installed, looking at pointer files for file: %s, use git lfs install;git lfs pull".formatted(path));
                    }
                    ((InputStream)in2).reset();
                }
                version = 0;
                pageCount = data;
            }
            block7: for (int i = 0; i < pageCount; ++i) {
                readString.apply(in2);
                int numEntries = readInt.applyAsInt(in2);
                readInt.applyAsInt(in2);
                for (int j = 0; j < numEntries; ++j) {
                    output.add(readString.apply(in2));
                    long skip = 32L;
                    while ((skip -= ((InputStream)in2).skip(skip)) > 0L) {
                    }
                }
                if (version > 0) {
                    long len = readInt.applyAsInt(in2);
                    while ((len -= ((InputStream)in2).skip(len)) > 0L) {
                    }
                    continue;
                }
                while (true) {
                    if (((InputStream)in2).read() != 239) {
                        continue;
                    }
                    ((InputStream)in2).mark(3);
                    if (((InputStream)in2).read() == 190 && ((InputStream)in2).read() == 173 && ((InputStream)in2).read() == 222) continue block7;
                    ((InputStream)in2).reset();
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return output;
    }

    static {
        try (Stream<Path> walk = Files.walk(Paths.get("media/texturepacks", new String[0]), new FileVisitOption[0]);){
            walk.filter(p -> p.toString().endsWith(".pack")).filter(Predicates.not(UNUSED::contains)).forEach(path -> PACK_TEXTURES.addAll(PackTextureValidator.getPackTiles(path)));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

