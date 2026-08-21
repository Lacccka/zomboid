/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.control;

import io.vavr.Value;
import io.vavr.collection.IndexedSeq;
import io.vavr.collection.Iterator;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Either<L, R>
extends Value<R>,
Serializable {
    public static final long serialVersionUID = 1L;

    public static <L, R> Either<L, R> right(R right) {
        return new Right(right);
    }

    public static <L, R> Either<L, R> left(L left) {
        return new Left(left);
    }

    public static <L, R> Either<L, R> narrow(Either<? extends L, ? extends R> either) {
        return either;
    }

    public L getLeft();

    public boolean isLeft();

    public boolean isRight();

    @Deprecated
    default public LeftProjection<L, R> left() {
        return new LeftProjection(this);
    }

    @Deprecated
    default public RightProjection<L, R> right() {
        return new RightProjection(this);
    }

    default public <X, Y> Either<X, Y> bimap(Function<? super L, ? extends X> leftMapper, Function<? super R, ? extends Y> rightMapper) {
        Objects.requireNonNull(leftMapper, "leftMapper is null");
        Objects.requireNonNull(rightMapper, "rightMapper is null");
        if (this.isRight()) {
            return new Right(rightMapper.apply(this.get()));
        }
        return new Left(leftMapper.apply(this.getLeft()));
    }

    default public <U> U fold(Function<? super L, ? extends U> leftMapper, Function<? super R, ? extends U> rightMapper) {
        Objects.requireNonNull(leftMapper, "leftMapper is null");
        Objects.requireNonNull(rightMapper, "rightMapper is null");
        if (this.isRight()) {
            return rightMapper.apply(this.get());
        }
        return leftMapper.apply(this.getLeft());
    }

    public static <L, R> Either<Seq<L>, Seq<R>> sequence(Iterable<? extends Either<? extends L, ? extends R>> eithers) {
        Objects.requireNonNull(eithers, "eithers is null");
        return Iterator.ofAll(eithers).partition(Either::isLeft).apply((leftPartition, rightPartition) -> leftPartition.hasNext() ? Either.left(leftPartition.map(Either::getLeft).toVector()) : Either.right(rightPartition.map(Either::get).toVector()));
    }

    public static <L, R, T> Either<Seq<L>, Seq<R>> traverse(Iterable<? extends T> values2, Function<? super T, ? extends Either<? extends L, ? extends R>> mapper) {
        Objects.requireNonNull(values2, "values is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return Either.sequence(Iterator.ofAll(values2).map(mapper));
    }

    public static <L, R> Either<L, Seq<R>> sequenceRight(Iterable<? extends Either<? extends L, ? extends R>> eithers) {
        Objects.requireNonNull(eithers, "eithers is null");
        IndexedSeq rightValues = Vector.empty();
        for (Either<L, R> either : eithers) {
            if (either.isRight()) {
                rightValues = rightValues.append(either.get());
                continue;
            }
            return Either.left(either.getLeft());
        }
        return Either.right(rightValues);
    }

    public static <L, R, T> Either<L, Seq<R>> traverseRight(Iterable<? extends T> values2, Function<? super T, ? extends Either<? extends L, ? extends R>> mapper) {
        Objects.requireNonNull(values2, "values is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return Either.sequenceRight(Iterator.ofAll(values2).map(mapper));
    }

    default public R getOrElseGet(Function<? super L, ? extends R> other) {
        Objects.requireNonNull(other, "other is null");
        if (this.isRight()) {
            return this.get();
        }
        return other.apply(this.getLeft());
    }

    default public void orElseRun(Consumer<? super L> action) {
        Objects.requireNonNull(action, "action is null");
        if (this.isLeft()) {
            action.accept(this.getLeft());
        }
    }

    default public <X extends Throwable> R getOrElseThrow(Function<? super L, X> exceptionFunction) throws X {
        Objects.requireNonNull(exceptionFunction, "exceptionFunction is null");
        if (this.isRight()) {
            return this.get();
        }
        throw (Throwable)exceptionFunction.apply(this.getLeft());
    }

    default public Either<R, L> swap() {
        if (this.isRight()) {
            return new Left(this.get());
        }
        return new Right(this.getLeft());
    }

    default public <U> Either<L, U> flatMap(Function<? super R, ? extends Either<L, ? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (this.isRight()) {
            return mapper.apply(this.get());
        }
        return this;
    }

    default public <U> Either<L, U> map(Function<? super R, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (this.isRight()) {
            return Either.right(mapper.apply(this.get()));
        }
        return this;
    }

    default public <U> Either<U, R> mapLeft(Function<? super L, ? extends U> leftMapper) {
        Objects.requireNonNull(leftMapper, "leftMapper is null");
        if (this.isLeft()) {
            return Either.left(leftMapper.apply(this.getLeft()));
        }
        return this;
    }

    default public Option<Either<L, R>> filter(Predicate<? super R> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.isLeft() || predicate.test(this.get()) ? Option.some(this) : Option.none();
    }

    default public Either<L, R> filterOrElse(Predicate<? super R> predicate, Function<? super R, ? extends L> zero) {
        Objects.requireNonNull(predicate, "predicate is null");
        Objects.requireNonNull(zero, "zero is null");
        if (this.isLeft() || predicate.test(this.get())) {
            return this;
        }
        return Either.left(zero.apply(this.get()));
    }

    @Override
    public R get();

    @Override
    default public boolean isEmpty() {
        return this.isLeft();
    }

    default public Either<L, R> orElse(Either<? extends L, ? extends R> other) {
        Objects.requireNonNull(other, "other is null");
        return this.isRight() ? this : other;
    }

    default public Either<L, R> orElse(Supplier<? extends Either<? extends L, ? extends R>> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return this.isRight() ? this : supplier.get();
    }

    @Override
    default public boolean isAsync() {
        return false;
    }

    @Override
    default public boolean isLazy() {
        return false;
    }

    @Override
    default public boolean isSingleValued() {
        return true;
    }

    @Override
    default public Iterator<R> iterator() {
        if (this.isRight()) {
            return Iterator.of(this.get());
        }
        return Iterator.empty();
    }

    default public Either<L, R> peek(Consumer<? super R> action) {
        Objects.requireNonNull(action, "action is null");
        if (this.isRight()) {
            action.accept(this.get());
        }
        return this;
    }

    default public Either<L, R> peekLeft(Consumer<? super L> action) {
        Objects.requireNonNull(action, "action is null");
        if (this.isLeft()) {
            action.accept(this.getLeft());
        }
        return this;
    }

    default public Validation<L, R> toValidation() {
        return this.isRight() ? Validation.valid(this.get()) : Validation.invalid(this.getLeft());
    }

    @Override
    public boolean equals(Object var1);

    @Override
    public int hashCode();

    @Override
    public String toString();

    public static final class Right<L, R>
    implements Either<L, R>,
    Serializable {
        private static final long serialVersionUID = 1L;
        private final R value;

        private Right(R value) {
            this.value = value;
        }

        @Override
        public R get() {
            return this.value;
        }

        @Override
        public L getLeft() {
            throw new NoSuchElementException("getLeft() on Right");
        }

        @Override
        public boolean isLeft() {
            return false;
        }

        @Override
        public boolean isRight() {
            return true;
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj instanceof Right && Objects.equals(this.value, ((Right)obj).value);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.value);
        }

        @Override
        public String stringPrefix() {
            return "Right";
        }

        @Override
        public String toString() {
            return this.stringPrefix() + "(" + this.value + ")";
        }
    }

    public static final class Left<L, R>
    implements Either<L, R>,
    Serializable {
        private static final long serialVersionUID = 1L;
        private final L value;

        private Left(L value) {
            this.value = value;
        }

        @Override
        public R get() {
            throw new NoSuchElementException("get() on Left");
        }

        @Override
        public L getLeft() {
            return this.value;
        }

        @Override
        public boolean isLeft() {
            return true;
        }

        @Override
        public boolean isRight() {
            return false;
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj instanceof Left && Objects.equals(this.value, ((Left)obj).value);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.value);
        }

        @Override
        public String stringPrefix() {
            return "Left";
        }

        @Override
        public String toString() {
            return this.stringPrefix() + "(" + this.value + ")";
        }
    }

    @Deprecated
    public static final class RightProjection<L, R>
    implements Value<R> {
        private final Either<L, R> either;

        private RightProjection(Either<L, R> either) {
            this.either = either;
        }

        public <L2, R2> RightProjection<L2, R2> bimap(Function<? super L, ? extends L2> leftMapper, Function<? super R, ? extends R2> rightMapper) {
            return this.either.bimap(leftMapper, rightMapper).right();
        }

        @Override
        public boolean isAsync() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return this.either.isLeft();
        }

        @Override
        public boolean isLazy() {
            return false;
        }

        @Override
        public boolean isSingleValued() {
            return true;
        }

        @Override
        public R get() {
            if (this.either.isRight()) {
                return this.either.get();
            }
            throw new NoSuchElementException("RightProjection.get() on Left");
        }

        public RightProjection<L, R> orElse(RightProjection<? extends L, ? extends R> other) {
            Objects.requireNonNull(other, "other is null");
            return this.either.isRight() ? this : other;
        }

        public RightProjection<L, R> orElse(Supplier<? extends RightProjection<? extends L, ? extends R>> supplier) {
            Objects.requireNonNull(supplier, "supplier is null");
            return this.either.isRight() ? this : supplier.get();
        }

        @Override
        public R getOrElse(R other) {
            return this.either.getOrElse(other);
        }

        public R getOrElseGet(Function<? super L, ? extends R> other) {
            Objects.requireNonNull(other, "other is null");
            return this.either.getOrElseGet(other);
        }

        public void orElseRun(Consumer<? super L> action) {
            Objects.requireNonNull(action, "action is null");
            this.either.orElseRun(action);
        }

        public <X extends Throwable> R getOrElseThrow(Function<? super L, X> exceptionFunction) throws X {
            Objects.requireNonNull(exceptionFunction, "exceptionFunction is null");
            return this.either.getOrElseThrow(exceptionFunction);
        }

        public Either<L, R> toEither() {
            return this.either;
        }

        public Option<RightProjection<L, R>> filter(Predicate<? super R> predicate) {
            Objects.requireNonNull(predicate, "predicate is null");
            return this.either.isLeft() || predicate.test(this.either.get()) ? Option.some(this) : Option.none();
        }

        public <U> RightProjection<L, U> flatMap(Function<? super R, ? extends RightProjection<L, ? extends U>> mapper) {
            Objects.requireNonNull(mapper, "mapper is null");
            if (this.either.isRight()) {
                return mapper.apply(this.either.get());
            }
            return this;
        }

        public <U> RightProjection<L, U> map(Function<? super R, ? extends U> mapper) {
            Objects.requireNonNull(mapper, "mapper is null");
            if (this.either.isRight()) {
                return this.either.map(mapper).right();
            }
            return this;
        }

        public RightProjection<L, R> peek(Consumer<? super R> action) {
            Objects.requireNonNull(action, "action is null");
            if (this.either.isRight()) {
                action.accept(this.either.get());
            }
            return this;
        }

        public <U> U transform(Function<? super RightProjection<L, R>, ? extends U> f) {
            Objects.requireNonNull(f, "f is null");
            return f.apply(this);
        }

        @Override
        public Iterator<R> iterator() {
            return this.either.iterator();
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj instanceof RightProjection && Objects.equals(this.either, ((RightProjection)obj).either);
        }

        @Override
        public int hashCode() {
            return this.either.hashCode();
        }

        @Override
        public String stringPrefix() {
            return "RightProjection";
        }

        @Override
        public String toString() {
            return this.stringPrefix() + "(" + this.either + ")";
        }
    }

    @Deprecated
    public static final class LeftProjection<L, R>
    implements Value<L> {
        private final Either<L, R> either;

        private LeftProjection(Either<L, R> either) {
            this.either = either;
        }

        public <L2, R2> LeftProjection<L2, R2> bimap(Function<? super L, ? extends L2> leftMapper, Function<? super R, ? extends R2> rightMapper) {
            return this.either.bimap(leftMapper, rightMapper).left();
        }

        @Override
        public boolean isAsync() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return this.either.isRight();
        }

        @Override
        public boolean isLazy() {
            return false;
        }

        @Override
        public boolean isSingleValued() {
            return true;
        }

        @Override
        public L get() {
            if (this.either.isLeft()) {
                return this.either.getLeft();
            }
            throw new NoSuchElementException("LeftProjection.get() on Right");
        }

        public LeftProjection<L, R> orElse(LeftProjection<? extends L, ? extends R> other) {
            Objects.requireNonNull(other, "other is null");
            return this.either.isLeft() ? this : other;
        }

        public LeftProjection<L, R> orElse(Supplier<? extends LeftProjection<? extends L, ? extends R>> supplier) {
            Objects.requireNonNull(supplier, "supplier is null");
            return this.either.isLeft() ? this : supplier.get();
        }

        @Override
        public L getOrElse(L other) {
            return this.either.isLeft() ? this.either.getLeft() : other;
        }

        public L getOrElseGet(Function<? super R, ? extends L> other) {
            Objects.requireNonNull(other, "other is null");
            if (this.either.isLeft()) {
                return this.either.getLeft();
            }
            return other.apply(this.either.get());
        }

        public void orElseRun(Consumer<? super R> action) {
            Objects.requireNonNull(action, "action is null");
            if (this.either.isRight()) {
                action.accept(this.either.get());
            }
        }

        public <X extends Throwable> L getOrElseThrow(Function<? super R, X> exceptionFunction) throws X {
            Objects.requireNonNull(exceptionFunction, "exceptionFunction is null");
            if (this.either.isLeft()) {
                return this.either.getLeft();
            }
            throw (Throwable)exceptionFunction.apply(this.either.get());
        }

        public Either<L, R> toEither() {
            return this.either;
        }

        public Option<LeftProjection<L, R>> filter(Predicate<? super L> predicate) {
            Objects.requireNonNull(predicate, "predicate is null");
            return this.either.isRight() || predicate.test(this.either.getLeft()) ? Option.some(this) : Option.none();
        }

        public <U> LeftProjection<U, R> flatMap(Function<? super L, ? extends LeftProjection<? extends U, R>> mapper) {
            Objects.requireNonNull(mapper, "mapper is null");
            if (this.either.isLeft()) {
                return mapper.apply(this.either.getLeft());
            }
            return this;
        }

        public <U> LeftProjection<U, R> map(Function<? super L, ? extends U> mapper) {
            Objects.requireNonNull(mapper, "mapper is null");
            if (this.either.isLeft()) {
                return this.either.mapLeft(mapper).left();
            }
            return this;
        }

        public LeftProjection<L, R> peek(Consumer<? super L> action) {
            Objects.requireNonNull(action, "action is null");
            if (this.either.isLeft()) {
                action.accept(this.either.getLeft());
            }
            return this;
        }

        public <U> U transform(Function<? super LeftProjection<L, R>, ? extends U> f) {
            Objects.requireNonNull(f, "f is null");
            return f.apply(this);
        }

        @Override
        public Iterator<L> iterator() {
            if (this.either.isLeft()) {
                return Iterator.of(this.either.getLeft());
            }
            return Iterator.empty();
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj instanceof LeftProjection && Objects.equals(this.either, ((LeftProjection)obj).either);
        }

        @Override
        public int hashCode() {
            return this.either.hashCode();
        }

        @Override
        public String stringPrefix() {
            return "LeftProjection";
        }

        @Override
        public String toString() {
            return this.stringPrefix() + "(" + this.either + ")";
        }
    }
}

