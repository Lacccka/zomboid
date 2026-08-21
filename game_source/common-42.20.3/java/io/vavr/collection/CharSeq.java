/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.PartialFunction;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.Value;
import io.vavr.collection.Array;
import io.vavr.collection.CharSeqModule;
import io.vavr.collection.Collections;
import io.vavr.collection.GwtIncompatible;
import io.vavr.collection.IndexedSeq;
import io.vavr.collection.Iterator;
import io.vavr.collection.JavaConverters;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Option;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

public final class CharSeq
implements CharSequence,
IndexedSeq<Character>,
Serializable,
Comparable<CharSeq> {
    private static final long serialVersionUID = 1L;
    private static final CharSeq EMPTY = new CharSeq("");
    private final String back;

    private CharSeq(String javaString) {
        this.back = javaString;
    }

    public static CharSeq empty() {
        return EMPTY;
    }

    public static Collector<Character, ArrayList<Character>, CharSeq> collector() {
        Supplier<ArrayList> supplier = ArrayList::new;
        BiConsumer<ArrayList, Character> accumulator = ArrayList::add;
        BinaryOperator combiner = (left, right) -> {
            left.addAll(right);
            return left;
        };
        Function<ArrayList, CharSeq> finisher = CharSeq::ofAll;
        return Collector.of(supplier, accumulator, combiner, finisher, new Collector.Characteristics[0]);
    }

    public static CharSeq of(CharSequence sequence) {
        Objects.requireNonNull(sequence, "sequence is null");
        if (sequence instanceof CharSeq) {
            return (CharSeq)sequence;
        }
        return sequence.length() == 0 ? CharSeq.empty() : new CharSeq(sequence.toString());
    }

    public static CharSeq of(char character) {
        return new CharSeq(new String(new char[]{character}));
    }

    public static CharSeq of(char ... characters) {
        Objects.requireNonNull(characters, "characters is null");
        if (characters.length == 0) {
            return CharSeq.empty();
        }
        char[] chrs = new char[characters.length];
        System.arraycopy(characters, 0, chrs, 0, characters.length);
        return new CharSeq(new String(chrs));
    }

    public static CharSeq ofAll(Iterable<? extends Character> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (Collections.isEmpty(elements)) {
            return EMPTY;
        }
        if (elements instanceof CharSeq) {
            return (CharSeq)elements;
        }
        if (elements instanceof JavaConverters.ListView && ((JavaConverters.ListView)elements).getDelegate() instanceof CharSeq) {
            return (CharSeq)((JavaConverters.ListView)elements).getDelegate();
        }
        StringBuilder sb = new StringBuilder();
        java.util.Iterator<? extends Character> iterator2 = elements.iterator();
        while (iterator2.hasNext()) {
            char character = iterator2.next().charValue();
            sb.append(character);
        }
        return CharSeq.of(sb);
    }

    public static CharSeq tabulate(int n, Function<? super Integer, ? extends Character> f) {
        Objects.requireNonNull(f, "f is null");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; ++i) {
            sb.append(f.apply((Integer)i).charValue());
        }
        return CharSeq.of(sb);
    }

    public static CharSeq fill(int n, Supplier<? extends Character> s) {
        return CharSeq.tabulate(n, anything -> (Character)s.get());
    }

    public static CharSeq range(char from, char toExclusive) {
        return new CharSeq(Iterator.range(from, toExclusive).mkString());
    }

    public static CharSeq rangeBy(char from, char toExclusive, int step) {
        return new CharSeq(Iterator.rangeBy(from, toExclusive, step).mkString());
    }

    public static CharSeq rangeClosed(char from, char toInclusive) {
        return new CharSeq(Iterator.rangeClosed(from, toInclusive).mkString());
    }

    public static CharSeq rangeClosedBy(char from, char toInclusive, int step) {
        return new CharSeq(Iterator.rangeClosedBy(from, toInclusive, step).mkString());
    }

    public static <T> CharSeq unfoldRight(T seed, Function<? super T, Option<Tuple2<? extends Character, ? extends T>>> f) {
        return CharSeq.ofAll(Iterator.unfoldRight(seed, f));
    }

    public static <T> CharSeq unfoldLeft(T seed, Function<? super T, Option<Tuple2<? extends T, ? extends Character>>> f) {
        return CharSeq.ofAll(Iterator.unfoldLeft(seed, f));
    }

    public static CharSeq unfold(Character seed, Function<? super Character, Option<Tuple2<? extends Character, ? extends Character>>> f) {
        return CharSeq.ofAll(Iterator.unfold(seed, f));
    }

    private Tuple2<CharSeq, CharSeq> splitByBuilder(StringBuilder sb) {
        if (sb.length() == 0) {
            return Tuple.of(EMPTY, this);
        }
        if (sb.length() == this.length()) {
            return Tuple.of(this, EMPTY);
        }
        return Tuple.of(CharSeq.of(sb), CharSeq.of(this.back.substring(sb.length())));
    }

    public static CharSeq repeat(char character, int times) {
        int length = Math.max(times, 0);
        char[] characters = new char[length];
        Arrays.fill(characters, character);
        return new CharSeq(String.valueOf(characters));
    }

    public CharSeq repeat(int times) {
        int i;
        if (times <= 0 || this.isEmpty()) {
            return CharSeq.empty();
        }
        if (times == 1) {
            return this;
        }
        int finalLength = this.length() * times;
        char[] result = new char[finalLength];
        this.back.getChars(0, this.length(), result, 0);
        for (i = this.length(); i <= finalLength >>> 1; i <<= 1) {
            System.arraycopy(result, 0, result, i, i);
        }
        System.arraycopy(result, 0, result, i, finalLength - i);
        return CharSeq.of(new String(result));
    }

    public CharSeq append(Character element) {
        char c = element.charValue();
        return CharSeq.of(this.back + c);
    }

    public CharSeq appendAll(Iterable<? extends Character> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (Collections.isEmpty(elements)) {
            return this;
        }
        StringBuilder sb = new StringBuilder(this.back);
        java.util.Iterator<? extends Character> iterator2 = elements.iterator();
        while (iterator2.hasNext()) {
            char element = iterator2.next().charValue();
            sb.append(element);
        }
        return CharSeq.of(sb);
    }

    @Override
    @GwtIncompatible
    public List<Character> asJava() {
        return JavaConverters.asJava(this, JavaConverters.ChangePolicy.IMMUTABLE);
    }

    @GwtIncompatible
    public CharSeq asJava(Consumer<? super List<Character>> action) {
        return Collections.asJava(this, action, JavaConverters.ChangePolicy.IMMUTABLE);
    }

    @Override
    @GwtIncompatible
    public List<Character> asJavaMutable() {
        return JavaConverters.asJava(this, JavaConverters.ChangePolicy.MUTABLE);
    }

    @GwtIncompatible
    public CharSeq asJavaMutable(Consumer<? super List<Character>> action) {
        return Collections.asJava(this, action, JavaConverters.ChangePolicy.MUTABLE);
    }

    @Override
    public <R> IndexedSeq<R> collect(PartialFunction<? super Character, ? extends R> partialFunction) {
        return Vector.ofAll(this.iterator().collect(partialFunction));
    }

    @Override
    public IndexedSeq<CharSeq> combinations() {
        return ((Vector)Vector.rangeClosed(0, this.length()).map((T n) -> this.combinations((int)n))).flatMap(Function.identity());
    }

    @Override
    public IndexedSeq<CharSeq> combinations(int k) {
        return CharSeqModule.Combinations.apply(this, Math.max(k, 0));
    }

    @Override
    public Iterator<CharSeq> crossProduct(int power) {
        return Collections.crossProduct(CharSeq.empty(), this, power);
    }

    @Override
    public CharSeq distinct() {
        return this.distinctBy(Function.identity());
    }

    public CharSeq distinctBy(Comparator<? super Character> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        TreeSet<? super Character> seen = new TreeSet<Character>(comparator);
        return this.filter(seen::add);
    }

    public <U> CharSeq distinctBy(Function<? super Character, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor is null");
        HashSet seen = new HashSet();
        return this.filter((T t) -> seen.add(keyExtractor.apply((Character)t)));
    }

    @Override
    public CharSeq drop(int n) {
        if (n <= 0) {
            return this;
        }
        if (n >= this.length()) {
            return EMPTY;
        }
        return CharSeq.of(this.back.substring(n));
    }

    public CharSeq dropUntil(Predicate<? super Character> predicate) {
        return Collections.dropUntil(this, predicate);
    }

    public CharSeq dropWhile(Predicate<? super Character> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.dropUntil((Predicate)predicate.negate());
    }

    @Override
    public CharSeq dropRight(int n) {
        if (n <= 0) {
            return this;
        }
        if (n >= this.length()) {
            return EMPTY;
        }
        return CharSeq.of(this.back.substring(0, this.length() - n));
    }

    public CharSeq dropRightWhile(Predicate<? super Character> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.dropRightUntil((Predicate)predicate.negate());
    }

    public CharSeq dropRightUntil(Predicate<? super Character> predicate) {
        return Collections.dropRightUntil(this, predicate);
    }

    public CharSeq filter(Predicate<? super Character> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.back.length(); ++i) {
            char ch = this.get(i).charValue();
            if (!predicate.test(Character.valueOf(ch))) continue;
            sb.append(ch);
        }
        if (sb.length() == 0) {
            return EMPTY;
        }
        if (sb.length() == this.length()) {
            return this;
        }
        return CharSeq.of(sb);
    }

    public CharSeq reject(Predicate<? super Character> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Collections.reject(this, predicate);
    }

    @Override
    public <U> IndexedSeq<U> flatMap(Function<? super Character, ? extends Iterable<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (this.isEmpty()) {
            return Vector.empty();
        }
        Seq result = Vector.empty();
        for (int i = 0; i < this.length(); ++i) {
            for (U u : mapper.apply(this.get(i))) {
                result = result.append(u);
            }
        }
        return result;
    }

    public CharSeq flatMapChars(CharFunction<? extends CharSequence> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (this.isEmpty()) {
            return this;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.back.length(); ++i) {
            builder.append(mapper.apply(this.back.charAt(i)));
        }
        return CharSeq.of(builder);
    }

    @Override
    public <C> Map<C, CharSeq> groupBy(Function<? super Character, ? extends C> classifier) {
        return Collections.groupBy(this, classifier, CharSeq::ofAll);
    }

    @Override
    public Iterator<CharSeq> grouped(int size) {
        return this.sliding(size, size);
    }

    @Override
    public boolean hasDefiniteSize() {
        return true;
    }

    @Override
    public CharSeq init() {
        if (this.isEmpty()) {
            throw new UnsupportedOperationException("init of empty string");
        }
        return CharSeq.of(this.back.substring(0, this.length() - 1));
    }

    @Override
    public Option<CharSeq> initOption() {
        return this.isEmpty() ? Option.none() : Option.some(this.init());
    }

    public CharSeq insert(int index, Character element) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("insert(" + index + ", e)");
        }
        if (index > this.length()) {
            throw new IndexOutOfBoundsException("insert(" + index + ", e) on String of length " + this.length());
        }
        char c = element.charValue();
        return CharSeq.of(new StringBuilder(this.back).insert(index, c).toString());
    }

    public CharSeq insertAll(int index, Iterable<? extends Character> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (index < 0) {
            throw new IndexOutOfBoundsException("insertAll(" + index + ", elements)");
        }
        if (index > this.length()) {
            throw new IndexOutOfBoundsException("insertAll(" + index + ", elements) on String of length " + this.length());
        }
        StringBuilder sb = new StringBuilder(this.back.substring(0, index));
        java.util.Iterator<? extends Character> iterator2 = elements.iterator();
        while (iterator2.hasNext()) {
            char element = iterator2.next().charValue();
            sb.append(element);
        }
        sb.append(this.back.substring(index));
        return CharSeq.of(sb);
    }

    @Override
    public Iterator<Character> iterator() {
        return Iterator.ofAll(this.toCharArray());
    }

    public CharSeq intersperse(Character element) {
        char c = element.charValue();
        if (this.isEmpty()) {
            return EMPTY;
        }
        StringBuilder sb = new StringBuilder().append(this.head());
        for (int i = 1; i < this.length(); ++i) {
            sb.append(c).append(this.get(i));
        }
        return CharSeq.of(sb);
    }

    @Override
    public <U> IndexedSeq<U> map(Function<? super Character, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        Seq result = Vector.empty();
        for (int i = 0; i < this.length(); ++i) {
            result = result.append(mapper.apply(this.get(i)));
        }
        return result;
    }

    @Override
    public String mkString() {
        return this.back;
    }

    public CharSeq padTo(int length, Character element) {
        int actualLength = this.back.length();
        if (length <= actualLength) {
            return this;
        }
        return new CharSeq(this.back + CharSeq.padding(element.charValue(), length - actualLength));
    }

    public CharSeq leftPadTo(int length, Character element) {
        int actualLength = this.back.length();
        if (length <= actualLength) {
            return this;
        }
        return CharSeq.of(CharSeq.padding(element.charValue(), length - actualLength).append(this.back));
    }

    public CharSeq orElse(Iterable<? extends Character> other) {
        return this.isEmpty() ? CharSeq.ofAll(other) : this;
    }

    public CharSeq orElse(Supplier<? extends Iterable<? extends Character>> supplier) {
        return this.isEmpty() ? CharSeq.ofAll(supplier.get()) : this;
    }

    private static StringBuilder padding(char element, int limit) {
        StringBuilder padding = new StringBuilder();
        for (int i = 0; i < limit; ++i) {
            padding.append(element);
        }
        return padding;
    }

    public CharSeq patch(int from, Iterable<? extends Character> that, int replaced) {
        from = from < 0 ? 0 : (from > this.length() ? this.length() : from);
        replaced = replaced < 0 ? 0 : replaced;
        StringBuilder sb = new StringBuilder(this.back.substring(0, from));
        java.util.Iterator<? extends Character> iterator2 = that.iterator();
        while (iterator2.hasNext()) {
            char character = iterator2.next().charValue();
            sb.append(character);
        }
        if ((from += replaced) < this.length()) {
            sb.append(this.back.substring(from));
        }
        return sb.length() == 0 ? EMPTY : CharSeq.of(sb);
    }

    public CharSeq mapChars(CharUnaryOperator mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (this.isEmpty()) {
            return this;
        }
        char[] chars = this.back.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            chars[i] = mapper.apply(chars[i]);
        }
        return CharSeq.of(chars);
    }

    @Override
    public Tuple2<CharSeq, CharSeq> partition(Predicate<? super Character> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (this.isEmpty()) {
            return Tuple.of(EMPTY, EMPTY);
        }
        StringBuilder left = new StringBuilder();
        StringBuilder right = new StringBuilder();
        for (int i = 0; i < this.length(); ++i) {
            Character t = this.get(i);
            (predicate.test(t) ? left : right).append(t);
        }
        if (left.length() == 0) {
            return Tuple.of(EMPTY, CharSeq.of(right.toString()));
        }
        if (right.length() == 0) {
            return Tuple.of(CharSeq.of(left.toString()), EMPTY);
        }
        return Tuple.of(CharSeq.of(left.toString()), CharSeq.of(right.toString()));
    }

    public CharSeq peek(Consumer<? super Character> action) {
        Objects.requireNonNull(action, "action is null");
        if (!this.isEmpty()) {
            action.accept(this.get(0));
        }
        return this;
    }

    @Override
    public IndexedSeq<CharSeq> permutations() {
        if (this.isEmpty()) {
            return Vector.empty();
        }
        if (this.length() == 1) {
            return Vector.of(this);
        }
        Seq<CharSeq> result = Vector.empty();
        for (Character t : this.distinct()) {
            for (CharSeq ts : this.remove(t).permutations()) {
                result = result.append(CharSeq.of(t.charValue()).appendAll((Iterable)ts));
            }
        }
        return result;
    }

    public CharSeq prepend(Character element) {
        char c = element.charValue();
        return CharSeq.of(c + this.back);
    }

    public CharSeq prependAll(Iterable<? extends Character> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (Collections.isEmpty(elements)) {
            return this;
        }
        if (this.isEmpty()) {
            return CharSeq.ofAll(elements);
        }
        StringBuilder sb = new StringBuilder();
        java.util.Iterator<? extends Character> iterator2 = elements.iterator();
        while (iterator2.hasNext()) {
            char element = iterator2.next().charValue();
            sb.append(element);
        }
        sb.append(this.back);
        return CharSeq.of(sb);
    }

    public CharSeq remove(Character element) {
        if (element == null) {
            return this;
        }
        StringBuilder sb = new StringBuilder();
        boolean found = false;
        for (int i = 0; i < this.length(); ++i) {
            char c = this.get(i).charValue();
            if (!found && c == element.charValue()) {
                found = true;
                continue;
            }
            sb.append(c);
        }
        return sb.length() == 0 ? EMPTY : (sb.length() == this.length() ? this : CharSeq.of(sb));
    }

    public CharSeq removeFirst(Predicate<Character> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        StringBuilder sb = new StringBuilder();
        boolean found = false;
        for (int i = 0; i < this.back.length(); ++i) {
            char ch = this.get(i).charValue();
            if (predicate.test(Character.valueOf(ch))) {
                if (found) {
                    sb.append(ch);
                }
                found = true;
                continue;
            }
            sb.append(ch);
        }
        return found ? (sb.length() == 0 ? EMPTY : CharSeq.of(sb.toString())) : this;
    }

    public CharSeq removeLast(Predicate<Character> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        for (int i = this.length() - 1; i >= 0; --i) {
            if (!predicate.test(this.get(i))) continue;
            return this.removeAt(i);
        }
        return this;
    }

    @Override
    public CharSeq removeAt(int index) {
        String removed = this.back.substring(0, index) + this.back.substring(index + 1);
        return removed.isEmpty() ? EMPTY : CharSeq.of(removed);
    }

    public CharSeq removeAll(Character element) {
        if (element == null) {
            return this;
        }
        return Collections.removeAll(this, element);
    }

    public CharSeq removeAll(Iterable<? extends Character> elements) {
        return Collections.removeAll(this, elements);
    }

    @Deprecated
    public CharSeq removeAll(Predicate<? super Character> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.reject((Predicate)predicate);
    }

    public CharSeq replace(Character currentElement, Character newElement) {
        if (currentElement == null) {
            return this;
        }
        char currentChar = currentElement.charValue();
        char newChar = newElement.charValue();
        StringBuilder sb = new StringBuilder();
        boolean found = false;
        for (int i = 0; i < this.length(); ++i) {
            char c = this.get(i).charValue();
            if (!found && c == currentChar) {
                sb.append(newChar);
                found = true;
                continue;
            }
            sb.append(c);
        }
        return found ? CharSeq.of(sb) : this;
    }

    public CharSeq replaceAll(Character currentElement, Character newElement) {
        if (currentElement == null) {
            return this;
        }
        char currentChar = currentElement.charValue();
        char newChar = newElement.charValue();
        StringBuilder sb = new StringBuilder();
        boolean found = false;
        for (int i = 0; i < this.length(); ++i) {
            char c = this.get(i).charValue();
            if (c == currentChar) {
                sb.append(newChar);
                found = true;
                continue;
            }
            sb.append(c);
        }
        return found ? CharSeq.of(sb) : this;
    }

    public CharSeq retainAll(Iterable<? extends Character> elements) {
        return Collections.retainAll(this, elements);
    }

    @Override
    public CharSeq reverse() {
        return CharSeq.of(new StringBuilder(this.back).reverse().toString());
    }

    @Override
    public CharSeq rotateLeft(int n) {
        return Collections.rotateLeft(this, n);
    }

    @Override
    public CharSeq rotateRight(int n) {
        return Collections.rotateRight(this, n);
    }

    public CharSeq scan(Character zero, BiFunction<? super Character, ? super Character, ? extends Character> operation) {
        return Collections.scanLeft(this, zero, operation, Value::toCharSeq);
    }

    @Override
    public <U> IndexedSeq<U> scanLeft(U zero, BiFunction<? super U, ? super Character, ? extends U> operation) {
        return Collections.scanLeft(this, zero, operation, Value::toVector);
    }

    @Override
    public <U> IndexedSeq<U> scanRight(U zero, BiFunction<? super Character, ? super U, ? extends U> operation) {
        return Collections.scanRight(this, zero, operation, Value::toVector);
    }

    @Override
    public CharSeq shuffle() {
        return Collections.shuffle(this, CharSeq::ofAll);
    }

    @Override
    public CharSeq slice(int beginIndex, int endIndex) {
        int to;
        int from = beginIndex < 0 ? 0 : beginIndex;
        int n = to = endIndex > this.length() ? this.length() : endIndex;
        if (from >= to) {
            return EMPTY;
        }
        if (from <= 0 && to >= this.length()) {
            return this;
        }
        return CharSeq.of(this.back.substring(from, to));
    }

    @Override
    public Iterator<CharSeq> slideBy(Function<? super Character, ?> classifier) {
        return this.iterator().slideBy(classifier).map(CharSeq::ofAll);
    }

    @Override
    public Iterator<CharSeq> sliding(int size) {
        return this.sliding(size, 1);
    }

    @Override
    public Iterator<CharSeq> sliding(int size, int step) {
        return this.iterator().sliding(size, step).map(CharSeq::ofAll);
    }

    @Override
    public CharSeq sorted() {
        return this.isEmpty() ? this : this.toJavaStream().sorted().collect(CharSeq.collector());
    }

    public CharSeq sorted(Comparator<? super Character> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        return this.isEmpty() ? this : this.toJavaStream().sorted(comparator).collect(CharSeq.collector());
    }

    public <U extends Comparable<? super U>> CharSeq sortBy(Function<? super Character, ? extends U> mapper) {
        return this.sortBy(Comparable::compareTo, (Function)mapper);
    }

    public <U> CharSeq sortBy(Comparator<? super U> comparator, Function<? super Character, ? extends U> mapper) {
        return Collections.sortBy(this, comparator, mapper, CharSeq.collector());
    }

    @Override
    public Tuple2<CharSeq, CharSeq> span(Predicate<? super Character> predicate) {
        char c;
        Objects.requireNonNull(predicate, "predicate is null");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.length() && predicate.test(Character.valueOf(c = this.get(i).charValue())); ++i) {
            sb.append(c);
        }
        return this.splitByBuilder(sb);
    }

    @Override
    public CharSeq subSequence(int beginIndex) {
        if (beginIndex < 0 || beginIndex > this.length()) {
            throw new IndexOutOfBoundsException("begin index " + beginIndex + " < 0");
        }
        if (beginIndex == 0) {
            return this;
        }
        if (beginIndex == this.length()) {
            return EMPTY;
        }
        return CharSeq.of(this.back.substring(beginIndex));
    }

    @Override
    public CharSeq tail() {
        if (this.isEmpty()) {
            throw new UnsupportedOperationException("tail of empty string");
        }
        return CharSeq.of(this.back.substring(1));
    }

    @Override
    public Option<CharSeq> tailOption() {
        return this.isEmpty() ? Option.none() : Option.some(this.tail());
    }

    @Override
    public CharSeq take(int n) {
        if (n <= 0) {
            return EMPTY;
        }
        if (n >= this.length()) {
            return this;
        }
        return CharSeq.of(this.back.substring(0, n));
    }

    public CharSeq takeUntil(Predicate<? super Character> predicate) {
        return Collections.takeUntil(this, predicate);
    }

    public CharSeq takeWhile(Predicate<? super Character> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.takeUntil((Predicate)predicate.negate());
    }

    @Override
    public CharSeq takeRight(int n) {
        if (n <= 0) {
            return EMPTY;
        }
        if (n >= this.length()) {
            return this;
        }
        return CharSeq.of(this.back.substring(this.length() - n));
    }

    public CharSeq takeRightUntil(Predicate<? super Character> predicate) {
        return Collections.takeRightUntil(this, predicate);
    }

    public CharSeq takeRightWhile(Predicate<? super Character> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.takeRightUntil((Predicate)predicate.negate());
    }

    public <U> U transform(Function<? super CharSeq, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    @Override
    public <T1, T2> Tuple2<IndexedSeq<T1>, IndexedSeq<T2>> unzip(Function<? super Character, Tuple2<? extends T1, ? extends T2>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        Seq xs = Vector.empty();
        Seq ys = Vector.empty();
        for (int i = 0; i < this.length(); ++i) {
            Tuple2<? extends T1, ? extends T2> t = unzipper.apply(this.get(i));
            xs = xs.append(t._1);
            ys = ys.append(t._2);
        }
        return Tuple.of(xs, ys);
    }

    @Override
    public <T1, T2, T3> Tuple3<IndexedSeq<T1>, IndexedSeq<T2>, IndexedSeq<T3>> unzip3(Function<? super Character, Tuple3<? extends T1, ? extends T2, ? extends T3>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        Seq xs = Vector.empty();
        Seq ys = Vector.empty();
        Seq zs = Vector.empty();
        for (int i = 0; i < this.length(); ++i) {
            Tuple3<? extends T1, ? extends T2, ? extends T3> t = unzipper.apply(this.get(i));
            xs = xs.append(t._1);
            ys = ys.append(t._2);
            zs = zs.append(t._3);
        }
        return Tuple.of(xs, ys, zs);
    }

    public CharSeq update(int index, Character element) {
        if (index < 0 || index >= this.length()) {
            throw new IndexOutOfBoundsException("update(" + index + ")");
        }
        char c = element.charValue();
        return CharSeq.of(this.back.substring(0, index) + c + this.back.substring(index + 1));
    }

    public CharSeq update(int index, Function<? super Character, ? extends Character> updater) {
        Objects.requireNonNull(updater, "updater is null");
        char c = updater.apply(this.get(index)).charValue();
        return this.update(index, Character.valueOf(c));
    }

    @Override
    public <U> IndexedSeq<Tuple2<Character, U>> zip(Iterable<? extends U> that) {
        return this.zipWith((Iterable)that, Tuple::of);
    }

    @Override
    public <U, R> IndexedSeq<R> zipWith(Iterable<? extends U> that, BiFunction<? super Character, ? super U, ? extends R> mapper) {
        Objects.requireNonNull(that, "that is null");
        Objects.requireNonNull(mapper, "mapper is null");
        Seq result = Vector.empty();
        java.util.Iterator list1 = this.iterator();
        java.util.Iterator<U> list2 = that.iterator();
        while (list1.hasNext() && list2.hasNext()) {
            result = result.append(mapper.apply((Character)list1.next(), list2.next()));
        }
        return result;
    }

    @Override
    public <U> IndexedSeq<Tuple2<Character, U>> zipAll(Iterable<? extends U> that, Character thisElem, U thatElem) {
        Objects.requireNonNull(that, "that is null");
        Seq<Tuple2<Character, Object>> result = Vector.empty();
        java.util.Iterator list1 = this.iterator();
        java.util.Iterator<U> list2 = that.iterator();
        while (list1.hasNext() || list2.hasNext()) {
            Character elem1 = list1.hasNext() ? (Character)list1.next() : thisElem;
            U elem2 = list2.hasNext() ? list2.next() : thatElem;
            result = result.append(Tuple.of(elem1, elem2));
        }
        return result;
    }

    @Override
    public IndexedSeq<Tuple2<Character, Integer>> zipWithIndex() {
        return this.zipWithIndex(Tuple::of);
    }

    @Override
    public <U> IndexedSeq<U> zipWithIndex(BiFunction<? super Character, ? super Integer, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        Seq result = Vector.empty();
        for (int i = 0; i < this.length(); ++i) {
            result = result.append(mapper.apply(this.get(i), i));
        }
        return result;
    }

    @Override
    public Character get(int index) {
        return Character.valueOf(this.back.charAt(index));
    }

    @Override
    public int indexOf(Character element, int from) {
        return this.back.indexOf(element.charValue(), from);
    }

    @Override
    public int lastIndexOf(Character element, int end) {
        return this.back.lastIndexOf(element.charValue(), end);
    }

    @Override
    public Tuple2<CharSeq, CharSeq> splitAt(int n) {
        if (n <= 0) {
            return Tuple.of(EMPTY, this);
        }
        if (n >= this.length()) {
            return Tuple.of(this, EMPTY);
        }
        return Tuple.of(CharSeq.of(this.back.substring(0, n)), CharSeq.of(this.back.substring(n)));
    }

    @Override
    public Tuple2<CharSeq, CharSeq> splitAt(Predicate<? super Character> predicate) {
        Character t;
        Objects.requireNonNull(predicate, "predicate is null");
        if (this.isEmpty()) {
            return Tuple.of(EMPTY, EMPTY);
        }
        StringBuilder left = new StringBuilder();
        for (int i = 0; i < this.length() && !predicate.test(t = this.get(i)); ++i) {
            left.append(t);
        }
        return this.splitByBuilder(left);
    }

    @Override
    public Tuple2<CharSeq, CharSeq> splitAtInclusive(Predicate<? super Character> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (this.isEmpty()) {
            return Tuple.of(EMPTY, EMPTY);
        }
        StringBuilder left = new StringBuilder();
        for (int i = 0; i < this.length(); ++i) {
            Character t = this.get(i);
            left.append(t);
            if (predicate.test(t)) break;
        }
        return this.splitByBuilder(left);
    }

    @Override
    public boolean startsWith(Iterable<? extends Character> that, int offset) {
        return this.startsWith(CharSeq.ofAll(that), offset);
    }

    @Override
    public Character head() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("head of empty string");
        }
        return this.get(0);
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.back.isEmpty();
    }

    @Override
    public boolean isLazy() {
        return false;
    }

    @Override
    public boolean isTraversableAgain() {
        return true;
    }

    private Object readResolve() {
        return this.isEmpty() ? EMPTY : this;
    }

    @Override
    public boolean equals(Object o) {
        return Collections.equals(this, o);
    }

    @Override
    public int hashCode() {
        return Collections.hashOrdered(this);
    }

    @Override
    public char charAt(int index) {
        return this.get(index).charValue();
    }

    @Override
    public int length() {
        return this.back.length();
    }

    public int codePointAt(int index) {
        return this.back.codePointAt(index);
    }

    public int codePointBefore(int index) {
        return this.back.codePointBefore(index);
    }

    public int codePointCount(int beginIndex, int endIndex) {
        return this.back.codePointCount(beginIndex, endIndex);
    }

    public int offsetByCodePoints(int index, int codePointOffset) {
        return this.back.offsetByCodePoints(index, codePointOffset);
    }

    @Override
    public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        this.back.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    public byte[] getBytes(String charsetName) throws UnsupportedEncodingException {
        return this.back.getBytes(charsetName);
    }

    public byte[] getBytes(Charset charset) {
        return this.back.getBytes(charset);
    }

    public byte[] getBytes() {
        return this.back.getBytes();
    }

    public boolean contentEquals(StringBuffer sb) {
        return this.back.contentEquals(sb);
    }

    public boolean contentEquals(CharSequence cs) {
        return this.back.contentEquals(cs);
    }

    public boolean equalsIgnoreCase(CharSeq anotherString) {
        return this.back.equalsIgnoreCase(anotherString.back);
    }

    @Override
    public int compareTo(CharSeq anotherString) {
        return this.back.compareTo(anotherString.back);
    }

    public int compareToIgnoreCase(CharSeq str) {
        return this.back.compareToIgnoreCase(str.back);
    }

    public boolean regionMatches(int toffset, CharSeq other, int ooffset, int len) {
        return this.back.regionMatches(toffset, other.back, ooffset, len);
    }

    public boolean regionMatches(boolean ignoreCase, int toffset, CharSeq other, int ooffset, int len) {
        return this.back.regionMatches(ignoreCase, toffset, other.back, ooffset, len);
    }

    @Override
    public CharSeq subSequence(int beginIndex, int endIndex) {
        if (beginIndex < 0) {
            throw new IndexOutOfBoundsException("begin index " + beginIndex + " < 0");
        }
        if (endIndex > this.length()) {
            throw new IndexOutOfBoundsException("endIndex " + endIndex + " > length " + this.length());
        }
        int subLen = endIndex - beginIndex;
        if (subLen < 0) {
            throw new IllegalArgumentException("beginIndex " + beginIndex + " > endIndex " + endIndex);
        }
        if (beginIndex == 0 && endIndex == this.length()) {
            return this;
        }
        return CharSeq.of(this.back.subSequence(beginIndex, endIndex));
    }

    public boolean startsWith(CharSeq prefix, int toffset) {
        return this.back.startsWith(prefix.back, toffset);
    }

    public boolean startsWith(CharSeq prefix) {
        return this.back.startsWith(prefix.back);
    }

    public boolean endsWith(CharSeq suffix) {
        return this.back.endsWith(suffix.back);
    }

    @Override
    public int indexOf(int ch) {
        return this.back.indexOf(ch);
    }

    @Override
    Option<Integer> indexOfOption(int ch) {
        return Collections.indexOption(this.indexOf(ch));
    }

    @Override
    public int indexOf(int ch, int fromIndex) {
        return this.back.indexOf(ch, fromIndex);
    }

    @Override
    Option<Integer> indexOfOption(int ch, int fromIndex) {
        return Collections.indexOption(this.indexOf(ch, fromIndex));
    }

    @Override
    public int lastIndexOf(int ch) {
        return this.back.lastIndexOf(ch);
    }

    @Override
    Option<Integer> lastIndexOfOption(int ch) {
        return Collections.indexOption(this.lastIndexOf(ch));
    }

    @Override
    public int lastIndexOf(int ch, int fromIndex) {
        return this.back.lastIndexOf(ch, fromIndex);
    }

    @Override
    public Option<Integer> lastIndexOfOption(int ch, int fromIndex) {
        return Collections.indexOption(this.lastIndexOf(ch, fromIndex));
    }

    @Override
    public int indexOf(CharSeq str) {
        return this.back.indexOf(str.back);
    }

    @Override
    public Option<Integer> indexOfOption(CharSeq str) {
        return Collections.indexOption(this.indexOf(str));
    }

    @Override
    public int indexOf(CharSeq str, int fromIndex) {
        return this.back.indexOf(str.back, fromIndex);
    }

    @Override
    public Option<Integer> indexOfOption(CharSeq str, int fromIndex) {
        return Collections.indexOption(this.indexOf(str, fromIndex));
    }

    @Override
    public int lastIndexOf(CharSeq str) {
        return this.back.lastIndexOf(str.back);
    }

    @Override
    public Option<Integer> lastIndexOfOption(CharSeq str) {
        return Collections.indexOption(this.lastIndexOf(str));
    }

    @Override
    public int lastIndexOf(CharSeq str, int fromIndex) {
        return this.back.lastIndexOf(str.back, fromIndex);
    }

    @Override
    public Option<Integer> lastIndexOfOption(CharSeq str, int fromIndex) {
        return Collections.indexOption(this.lastIndexOf(str, fromIndex));
    }

    public CharSeq substring(int beginIndex) {
        return CharSeq.of(this.back.substring(beginIndex));
    }

    public CharSeq substring(int beginIndex, int endIndex) {
        return CharSeq.of(this.back.substring(beginIndex, endIndex));
    }

    @Override
    public String stringPrefix() {
        return "CharSeq";
    }

    @Override
    public String toString() {
        return this.back;
    }

    public CharSeq concat(CharSeq str) {
        return CharSeq.of(this.back.concat(str.back));
    }

    public boolean matches(String regex) {
        return this.back.matches(regex);
    }

    @Override
    public boolean contains(CharSequence s) {
        return this.back.contains(s);
    }

    public CharSeq replaceFirst(String regex, String replacement) {
        return CharSeq.of(this.back.replaceFirst(regex, replacement));
    }

    public CharSeq replaceAll(String regex, String replacement) {
        return CharSeq.of(this.back.replaceAll(regex, replacement));
    }

    public CharSeq replace(CharSequence target, CharSequence replacement) {
        return CharSeq.of(this.back.replace(target, replacement));
    }

    public Seq<CharSeq> split(String regex) {
        return this.split(regex, 0);
    }

    public Seq<CharSeq> split(String regex, int limit) {
        Array split = Array.wrap(this.back.split(regex, limit));
        return split.map(CharSeq::of);
    }

    public CharSeq toLowerCase(Locale locale) {
        return CharSeq.of(this.back.toLowerCase(locale));
    }

    public CharSeq toLowerCase() {
        return CharSeq.of(this.back.toLowerCase(Locale.getDefault()));
    }

    public CharSeq toUpperCase(Locale locale) {
        return CharSeq.of(this.back.toUpperCase(locale));
    }

    public CharSeq toUpperCase() {
        return CharSeq.of(this.back.toUpperCase(Locale.getDefault()));
    }

    public CharSeq capitalize(Locale locale) {
        if (this.back.isEmpty()) {
            return this;
        }
        return CharSeq.of(this.back.substring(0, 1).toUpperCase(locale) + this.back.substring(1));
    }

    public CharSeq capitalize() {
        return this.capitalize(Locale.getDefault());
    }

    public CharSeq trim() {
        return CharSeq.of(this.back.trim());
    }

    public char[] toCharArray() {
        return this.back.toCharArray();
    }

    public Byte decodeByte() {
        return Byte.decode(this.back);
    }

    public Integer decodeInteger() {
        return Integer.decode(this.back);
    }

    public Long decodeLong() {
        return Long.decode(this.back);
    }

    public Short decodeShort() {
        return Short.decode(this.back);
    }

    public boolean parseBoolean() {
        return Boolean.parseBoolean(this.back);
    }

    public byte parseByte() {
        return Byte.parseByte(this.back);
    }

    public byte parseByte(int radix) {
        return Byte.parseByte(this.back, radix);
    }

    public double parseDouble() {
        return Double.parseDouble(this.back);
    }

    public float parseFloat() {
        return Float.parseFloat(this.back);
    }

    public int parseInt() {
        return Integer.parseInt(this.back);
    }

    public int parseInt(int radix) {
        return Integer.parseInt(this.back, radix);
    }

    @GwtIncompatible
    public int parseUnsignedInt() {
        return Integer.parseUnsignedInt(this.back);
    }

    @GwtIncompatible
    public int parseUnsignedInt(int radix) {
        return Integer.parseUnsignedInt(this.back, radix);
    }

    public long parseLong() {
        return Long.parseLong(this.back);
    }

    public long parseLong(int radix) {
        return Long.parseLong(this.back, radix);
    }

    @GwtIncompatible
    public long parseUnsignedLong() {
        return Long.parseUnsignedLong(this.back);
    }

    @GwtIncompatible
    public long parseUnsignedLong(int radix) {
        return Long.parseUnsignedLong(this.back, radix);
    }

    public short parseShort() {
        return Short.parseShort(this.back);
    }

    public short parseShort(int radix) {
        return Short.parseShort(this.back, radix);
    }

    public Boolean toBoolean() {
        return Boolean.valueOf(this.back);
    }

    public Byte toByte() {
        return Byte.valueOf(this.back);
    }

    public Byte toByte(int radix) {
        return Byte.valueOf(this.back, radix);
    }

    public Double toDouble() {
        return Double.valueOf(this.back);
    }

    public Float toFloat() {
        return Float.valueOf(this.back);
    }

    public Integer toInteger() {
        return Integer.valueOf(this.back);
    }

    public Integer toInteger(int radix) {
        return Integer.valueOf(this.back, radix);
    }

    public Long toLong() {
        return Long.valueOf(this.back);
    }

    public Long toLong(int radix) {
        return Long.valueOf(this.back, radix);
    }

    public Short toShort() {
        return Short.valueOf(this.back);
    }

    public Short toShort(int radix) {
        return Short.valueOf(this.back, radix);
    }

    public Character[] toJavaArray() {
        return this.toJavaList().toArray(new Character[0]);
    }

    @FunctionalInterface
    public static interface CharFunction<R> {
        public R apply(char var1);
    }

    @FunctionalInterface
    public static interface CharUnaryOperator {
        public char apply(char var1);
    }
}

