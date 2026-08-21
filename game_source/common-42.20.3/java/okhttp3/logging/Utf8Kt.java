/*
 * Decompiled with CFR 0.152.
 */
package okhttp3.logging;

import java.io.EOFException;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt;
import okio.Buffer;
import org.jetbrains.annotations.NotNull;

@Metadata(mv={1, 4, 0}, bv={1, 0, 3}, k=2, d1={"\u0000\f\n\u0000\n\u0002\u0010\u000b\n\u0002\u0018\u0002\n\u0000\u001a\f\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u0000\u00a8\u0006\u0003"}, d2={"isProbablyUtf8", "", "Lokio/Buffer;", "okhttp-logging-interceptor"})
public final class Utf8Kt {
    /*
     * WARNING - void declaration
     */
    public static final boolean isProbablyUtf8(@NotNull Buffer $this$isProbablyUtf8) {
        Intrinsics.checkNotNullParameter($this$isProbablyUtf8, "$this$isProbablyUtf8");
        try {
            Buffer prefix = new Buffer();
            long byteCount = RangesKt.coerceAtMost($this$isProbablyUtf8.size(), 64L);
            $this$isProbablyUtf8.copyTo(prefix, 0L, byteCount);
            int n = 0;
            int n2 = 16;
            while (n < n2 && !prefix.exhausted()) {
                void i;
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
                ++i;
            }
            return true;
        }
        catch (EOFException _) {
            return false;
        }
    }
}

