/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import java.io.IOException;
import java.util.Iterator;
import java.util.function.Supplier;
import org.apache.commons.lang3.exception.UncheckedException;
import org.apache.commons.lang3.function.FailableBiConsumer;

public final class AppendableJoiner<T> {
    private final CharSequence prefix;
    private final CharSequence suffix;
    private final CharSequence delimiter;
    private final FailableBiConsumer<Appendable, T, IOException> appender;

    public static <T> Builder<T> builder() {
        return new Builder();
    }

    @SafeVarargs
    static <A extends Appendable, T> A joinA(A appendable, CharSequence prefix, CharSequence suffix, CharSequence delimiter, FailableBiConsumer<Appendable, T, IOException> appender, T ... elements) throws IOException {
        return AppendableJoiner.joinArray(appendable, prefix, suffix, delimiter, appender, elements);
    }

    private static <A extends Appendable, T> A joinArray(A appendable, CharSequence prefix, CharSequence suffix, CharSequence delimiter, FailableBiConsumer<Appendable, T, IOException> appender, T[] elements) throws IOException {
        appendable.append(prefix);
        if (elements != null) {
            if (elements.length > 0) {
                appender.accept(appendable, (A)elements[0]);
            }
            for (int i = 1; i < elements.length; ++i) {
                appendable.append(delimiter);
                appender.accept(appendable, (A)elements[i]);
            }
        }
        appendable.append(suffix);
        return appendable;
    }

    static <T> StringBuilder joinI(StringBuilder stringBuilder, CharSequence prefix, CharSequence suffix, CharSequence delimiter, FailableBiConsumer<Appendable, T, IOException> appender, Iterable<T> elements) {
        try {
            return AppendableJoiner.joinIterable(stringBuilder, prefix, suffix, delimiter, appender, elements);
        }
        catch (IOException e) {
            throw new UncheckedException(e);
        }
    }

    private static <A extends Appendable, T> A joinIterable(A appendable, CharSequence prefix, CharSequence suffix, CharSequence delimiter, FailableBiConsumer<Appendable, T, IOException> appender, Iterable<T> elements) throws IOException {
        appendable.append(prefix);
        if (elements != null) {
            Iterator<T> iterator2 = elements.iterator();
            if (iterator2.hasNext()) {
                appender.accept(appendable, (A)iterator2.next());
            }
            while (iterator2.hasNext()) {
                appendable.append(delimiter);
                appender.accept(appendable, (A)iterator2.next());
            }
        }
        appendable.append(suffix);
        return appendable;
    }

    @SafeVarargs
    static <T> StringBuilder joinSB(StringBuilder stringBuilder, CharSequence prefix, CharSequence suffix, CharSequence delimiter, FailableBiConsumer<Appendable, T, IOException> appender, T ... elements) {
        try {
            return AppendableJoiner.joinArray(stringBuilder, prefix, suffix, delimiter, appender, elements);
        }
        catch (IOException e) {
            throw new UncheckedException(e);
        }
    }

    private static CharSequence nonNull(CharSequence value) {
        return value != null ? value : "";
    }

    private AppendableJoiner(CharSequence prefix, CharSequence suffix, CharSequence delimiter, FailableBiConsumer<Appendable, T, IOException> appender) {
        this.prefix = AppendableJoiner.nonNull(prefix);
        this.suffix = AppendableJoiner.nonNull(suffix);
        this.delimiter = AppendableJoiner.nonNull(delimiter);
        this.appender = appender != null ? appender : (a, e) -> a.append(String.valueOf(e));
    }

    public StringBuilder join(StringBuilder stringBuilder, Iterable<T> elements) {
        return AppendableJoiner.joinI(stringBuilder, this.prefix, this.suffix, this.delimiter, this.appender, elements);
    }

    public StringBuilder join(StringBuilder stringBuilder, T ... elements) {
        return AppendableJoiner.joinSB(stringBuilder, this.prefix, this.suffix, this.delimiter, this.appender, elements);
    }

    public <A extends Appendable> A joinA(A appendable, Iterable<T> elements) throws IOException {
        return AppendableJoiner.joinIterable(appendable, this.prefix, this.suffix, this.delimiter, this.appender, elements);
    }

    public <A extends Appendable> A joinA(A appendable, T ... elements) throws IOException {
        return AppendableJoiner.joinA(appendable, this.prefix, this.suffix, this.delimiter, this.appender, elements);
    }

    public static final class Builder<T>
    implements Supplier<AppendableJoiner<T>> {
        private CharSequence prefix;
        private CharSequence suffix;
        private CharSequence delimiter;
        private FailableBiConsumer<Appendable, T, IOException> appender;

        Builder() {
        }

        @Override
        public AppendableJoiner<T> get() {
            return new AppendableJoiner(this.prefix, this.suffix, this.delimiter, this.appender);
        }

        public Builder<T> setDelimiter(CharSequence delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public Builder<T> setElementAppender(FailableBiConsumer<Appendable, T, IOException> appender) {
            this.appender = appender;
            return this;
        }

        public Builder<T> setPrefix(CharSequence prefix) {
            this.prefix = prefix;
            return this;
        }

        public Builder<T> setSuffix(CharSequence suffix) {
            this.suffix = suffix;
            return this;
        }
    }
}

