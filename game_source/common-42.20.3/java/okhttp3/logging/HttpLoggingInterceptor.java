/*
 * Decompiled with CFR 0.152.
 */
package okhttp3.logging;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import kotlin.Deprecated;
import kotlin.DeprecationLevel;
import kotlin.Metadata;
import kotlin.ReplaceWith;
import kotlin.collections.CollectionsKt;
import kotlin.collections.SetsKt;
import kotlin.io.CloseableKt;
import kotlin.jvm.JvmField;
import kotlin.jvm.JvmName;
import kotlin.jvm.JvmOverloads;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.StringCompanionObject;
import kotlin.text.StringsKt;
import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.platform.Platform;
import okhttp3.logging.Utf8Kt;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;
import org.jetbrains.annotations.NotNull;

/*
 * Illegal identifiers - consider using --renameillegalidents true
 */
@Metadata(mv={1, 4, 0}, bv={1, 0, 3}, k=1, d1={"\u0000L\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\"\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0006\u0018\u00002\u00020\u0001:\u0002\u001e\u001fB\u0011\b\u0007\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011H\u0002J\r\u0010\u000b\u001a\u00020\tH\u0007\u00a2\u0006\u0002\b\u0012J\u0010\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0016H\u0016J\u0018\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0019\u001a\u00020\u001aH\u0002J\u000e\u0010\u001b\u001a\u00020\u00182\u0006\u0010\u001c\u001a\u00020\u0007J\u000e\u0010\u001d\u001a\u00020\u00002\u0006\u0010\n\u001a\u00020\tR\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R$\u0010\n\u001a\u00020\t2\u0006\u0010\b\u001a\u00020\t@GX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\n\u0010\rR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006 "}, d2={"Lokhttp3/logging/HttpLoggingInterceptor;", "Lokhttp3/Interceptor;", "logger", "Lokhttp3/logging/HttpLoggingInterceptor$Logger;", "(Lokhttp3/logging/HttpLoggingInterceptor$Logger;)V", "headersToRedact", "", "", "<set-?>", "Lokhttp3/logging/HttpLoggingInterceptor$Level;", "level", "getLevel", "()Lokhttp3/logging/HttpLoggingInterceptor$Level;", "(Lokhttp3/logging/HttpLoggingInterceptor$Level;)V", "bodyHasUnknownEncoding", "", "headers", "Lokhttp3/Headers;", "-deprecated_level", "intercept", "Lokhttp3/Response;", "chain", "Lokhttp3/Interceptor$Chain;", "logHeader", "", "i", "", "redactHeader", "name", "setLevel", "Level", "Logger", "okhttp-logging-interceptor"})
public final class HttpLoggingInterceptor
implements Interceptor {
    private volatile Set<String> headersToRedact;
    @NotNull
    private volatile Level level;
    private final Logger logger;

    @NotNull
    public final Level getLevel() {
        return this.level;
    }

    @JvmName(name="level")
    public final void level(@NotNull Level level) {
        Intrinsics.checkNotNullParameter((Object)level, "<set-?>");
        this.level = level;
    }

    public final void redactHeader(@NotNull String name) {
        Intrinsics.checkNotNullParameter(name, "name");
        TreeSet<String> newHeadersToRedact = new TreeSet<String>(StringsKt.getCASE_INSENSITIVE_ORDER(StringCompanionObject.INSTANCE));
        Collection collection = newHeadersToRedact;
        Iterable iterable = this.headersToRedact;
        boolean bl = false;
        CollectionsKt.addAll(collection, iterable);
        collection = newHeadersToRedact;
        boolean bl2 = false;
        collection.add(name);
        this.headersToRedact = newHeadersToRedact;
    }

    @NotNull
    public final HttpLoggingInterceptor setLevel(@NotNull Level level) {
        Intrinsics.checkNotNullParameter((Object)level, "level");
        HttpLoggingInterceptor httpLoggingInterceptor = this;
        boolean bl = false;
        boolean bl2 = false;
        HttpLoggingInterceptor $this$apply = httpLoggingInterceptor;
        boolean bl3 = false;
        $this$apply.level = level;
        return httpLoggingInterceptor;
    }

    @Deprecated(message="moved to var", replaceWith=@ReplaceWith(imports={}, expression="level"), level=DeprecationLevel.ERROR)
    @JvmName(name="-deprecated_level")
    @NotNull
    public final Level -deprecated_level() {
        return this.level;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * WARNING - void declaration
     */
    @Override
    @NotNull
    public Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
        int n;
        String string;
        Intrinsics.checkNotNullParameter(chain, "chain");
        Level level = this.level;
        Request request = chain.request();
        if (level == Level.NONE) {
            return chain.proceed(request);
        }
        boolean logBody = level == Level.BODY;
        boolean logHeaders = logBody || level == Level.HEADERS;
        RequestBody requestBody = request.body();
        Connection connection = chain.connection();
        String requestStartMessage = "--> " + request.method() + ' ' + request.url() + (connection != null ? " " + (Object)((Object)connection.protocol()) : "");
        if (!logHeaders && requestBody != null) {
            requestStartMessage = requestStartMessage + " (" + requestBody.contentLength() + "-byte body)";
        }
        this.logger.log(requestStartMessage);
        if (logHeaders) {
            int n2;
            Headers headers = request.headers();
            if (requestBody != null) {
                MediaType mediaType = requestBody.contentType();
                if (mediaType != null) {
                    MediaType mediaType2 = mediaType;
                    n2 = 0;
                    boolean bl = false;
                    MediaType it = mediaType2;
                    boolean bl2 = false;
                    if (headers.get("Content-Type") == null) {
                        this.logger.log("Content-Type: " + it);
                    }
                }
                if (requestBody.contentLength() != -1L && headers.get("Content-Length") == null) {
                    this.logger.log("Content-Length: " + requestBody.contentLength());
                }
            }
            int n3 = 0;
            n2 = headers.size();
            while (n3 < n2) {
                void i;
                this.logHeader(headers, (int)i);
                ++i;
            }
            if (!logBody || requestBody == null) {
                this.logger.log("--> END " + request.method());
            } else if (this.bodyHasUnknownEncoding(request.headers())) {
                this.logger.log("--> END " + request.method() + " (encoded body omitted)");
            } else if (requestBody.isDuplex()) {
                this.logger.log("--> END " + request.method() + " (duplex request body omitted)");
            } else if (requestBody.isOneShot()) {
                this.logger.log("--> END " + request.method() + " (one-shot body omitted)");
            } else {
                Buffer buffer = new Buffer();
                requestBody.writeTo(buffer);
                MediaType contentType = requestBody.contentType();
                Object object = contentType;
                if (object == null || (object = ((MediaType)object).charset(StandardCharsets.UTF_8)) == null) {
                    Charset charset = StandardCharsets.UTF_8;
                    object = charset;
                    Intrinsics.checkNotNullExpressionValue(charset, "UTF_8");
                }
                Object charset = object;
                this.logger.log("");
                if (Utf8Kt.isProbablyUtf8(buffer)) {
                    this.logger.log(buffer.readString((Charset)charset));
                    this.logger.log("--> END " + request.method() + " (" + requestBody.contentLength() + "-byte body)");
                } else {
                    this.logger.log("--> END " + request.method() + " (binary " + requestBody.contentLength() + "-byte body omitted)");
                }
            }
        }
        long startNs = System.nanoTime();
        Response response = null;
        try {
            response = chain.proceed(request);
        }
        catch (Exception e) {
            this.logger.log("<-- HTTP FAILED: " + e);
            throw (Throwable)e;
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
        ResponseBody responseBody = response.body();
        Intrinsics.checkNotNull(responseBody);
        ResponseBody responseBody2 = responseBody;
        long contentLength = responseBody2.contentLength();
        String bodySize = contentLength != -1L ? contentLength + "-byte" : "unknown-length";
        StringBuilder stringBuilder = new StringBuilder().append("<-- ").append(response.code());
        CharSequence charSequence = response.message();
        int n4 = 0;
        if (charSequence.length() == 0) {
            string = "";
        } else {
            char c = ' ';
            String string2 = response.message();
            n = 0;
            string = String.valueOf(c) + string2;
        }
        this.logger.log(stringBuilder.append(string).append(' ').append(response.request().url()).append(" (").append(tookMs).append("ms").append(!logHeaders ? ", " + bodySize + " body" : "").append(')').toString());
        if (logHeaders) {
            Headers headers = response.headers();
            n4 = 0;
            n = headers.size();
            while (n4 < n) {
                void i;
                this.logHeader(headers, (int)i);
                ++i;
            }
            if (!logBody || !HttpHeaders.promisesBody(response)) {
                this.logger.log("<-- END HTTP");
            } else if (this.bodyHasUnknownEncoding(response.headers())) {
                this.logger.log("<-- END HTTP (encoded body omitted)");
            } else {
                MediaType contentType;
                Object object;
                BufferedSource source2 = responseBody2.source();
                source2.request(Long.MAX_VALUE);
                Buffer buffer = source2.getBuffer();
                Long gzippedLength = null;
                if (StringsKt.equals("gzip", headers.get("Content-Encoding"), true)) {
                    gzippedLength = buffer.size();
                    Closeable closeable = new GzipSource(buffer.clone());
                    boolean bl = false;
                    boolean bl3 = false;
                    Throwable throwable = null;
                    try {
                        GzipSource gzippedResponseBody = (GzipSource)closeable;
                        boolean bl4 = false;
                        buffer = new Buffer();
                        long l = buffer.writeAll(gzippedResponseBody);
                    }
                    catch (Throwable throwable2) {
                        throwable = throwable2;
                        throw throwable2;
                    }
                    finally {
                        CloseableKt.closeFinally(closeable, throwable);
                    }
                }
                if ((object = (contentType = responseBody2.contentType())) == null || (object = ((MediaType)object).charset(StandardCharsets.UTF_8)) == null) {
                    Charset charset = StandardCharsets.UTF_8;
                    object = charset;
                    Intrinsics.checkNotNullExpressionValue(charset, "UTF_8");
                }
                Object charset = object;
                if (!Utf8Kt.isProbablyUtf8(buffer)) {
                    this.logger.log("");
                    this.logger.log("<-- END HTTP (binary " + buffer.size() + "-byte body omitted)");
                    return response;
                }
                if (contentLength != 0L) {
                    this.logger.log("");
                    this.logger.log(buffer.clone().readString((Charset)charset));
                }
                if (gzippedLength != null) {
                    this.logger.log("<-- END HTTP (" + buffer.size() + "-byte, " + gzippedLength + "-gzipped-byte body)");
                } else {
                    this.logger.log("<-- END HTTP (" + buffer.size() + "-byte body)");
                }
            }
        }
        return response;
    }

    private final void logHeader(Headers headers, int i) {
        String value = this.headersToRedact.contains(headers.name(i)) ? "\u2588\u2588" : headers.value(i);
        this.logger.log(headers.name(i) + ": " + value);
    }

    private final boolean bodyHasUnknownEncoding(Headers headers) {
        String string = headers.get("Content-Encoding");
        if (string == null) {
            return false;
        }
        String contentEncoding = string;
        return !StringsKt.equals(contentEncoding, "identity", true) && !StringsKt.equals(contentEncoding, "gzip", true);
    }

    @JvmOverloads
    public HttpLoggingInterceptor(@NotNull Logger logger) {
        Intrinsics.checkNotNullParameter(logger, "logger");
        this.logger = logger;
        this.headersToRedact = SetsKt.emptySet();
        this.level = Level.NONE;
    }

    public /* synthetic */ HttpLoggingInterceptor(Logger logger, int n, DefaultConstructorMarker defaultConstructorMarker) {
        if ((n & 1) != 0) {
            logger = Logger.DEFAULT;
        }
        this(logger);
    }

    @JvmOverloads
    public HttpLoggingInterceptor() {
        this(null, 1, null);
    }

    @Metadata(mv={1, 4, 0}, bv={1, 0, 3}, k=1, d1={"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0006\b\u0086\u0001\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006\u00a8\u0006\u0007"}, d2={"Lokhttp3/logging/HttpLoggingInterceptor$Level;", "", "(Ljava/lang/String;I)V", "NONE", "BASIC", "HEADERS", "BODY", "okhttp-logging-interceptor"})
    public static final class Level
    extends Enum<Level> {
        public static final /* enum */ Level NONE;
        public static final /* enum */ Level BASIC;
        public static final /* enum */ Level HEADERS;
        public static final /* enum */ Level BODY;
        private static final /* synthetic */ Level[] $VALUES;

        static {
            Level[] levelArray = new Level[4];
            Level[] levelArray2 = levelArray;
            levelArray[0] = NONE = new Level();
            levelArray[1] = BASIC = new Level();
            levelArray[2] = HEADERS = new Level();
            levelArray[3] = BODY = new Level();
            $VALUES = levelArray;
        }

        public static Level[] values() {
            return (Level[])$VALUES.clone();
        }

        public static Level valueOf(String string) {
            return Enum.valueOf(Level.class, string);
        }
    }

    @Metadata(mv={1, 4, 0}, bv={1, 0, 3}, k=1, d1={"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u00e6\u0080\u0001\u0018\u0000 \u00062\u00020\u0001:\u0001\u0006J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H&\u00a8\u0006\u0007"}, d2={"Lokhttp3/logging/HttpLoggingInterceptor$Logger;", "", "log", "", "message", "", "Companion", "okhttp-logging-interceptor"})
    public static interface Logger {
        @JvmField
        @NotNull
        public static final Logger DEFAULT;
        public static final Companion Companion;

        public void log(@NotNull String var1);

        static {
            Companion = new Companion(null);
            DEFAULT = new Companion.DefaultLogger();
        }

        @Metadata(mv={1, 4, 0}, bv={1, 0, 3}, k=1, d1={"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001:\u0001\u0005B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0016\u0010\u0003\u001a\u00020\u00048\u0006X\u0087\u0004\u00f8\u0001\u0000\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0001\u0082\u0002\u0007\n\u0005\b\u0091F0\u0001\u00a8\u0006\u0006"}, d2={"Lokhttp3/logging/HttpLoggingInterceptor$Logger$Companion;", "", "()V", "DEFAULT", "Lokhttp3/logging/HttpLoggingInterceptor$Logger;", "DefaultLogger", "okhttp-logging-interceptor"})
        public static final class Companion {
            static final /* synthetic */ Companion $$INSTANCE;

            private Companion() {
            }

            public /* synthetic */ Companion(DefaultConstructorMarker $constructor_marker) {
                this();
            }

            @Metadata(mv={1, 4, 0}, bv={1, 0, 3}, k=1, d1={"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0002\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0016\u00a8\u0006\u0007"}, d2={"Lokhttp3/logging/HttpLoggingInterceptor$Logger$Companion$DefaultLogger;", "Lokhttp3/logging/HttpLoggingInterceptor$Logger;", "()V", "log", "", "message", "", "okhttp-logging-interceptor"})
            private static final class DefaultLogger
            implements Logger {
                @Override
                public void log(@NotNull String message) {
                    Intrinsics.checkNotNullParameter(message, "message");
                    Platform.log$default(Platform.Companion.get(), message, 0, null, 6, null);
                }
            }
        }
    }
}

