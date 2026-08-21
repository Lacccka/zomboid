/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.control;

import io.vavr.API;
import io.vavr.CheckedConsumer;
import io.vavr.CheckedFunction0;
import io.vavr.CheckedFunction1;
import io.vavr.CheckedFunction2;
import io.vavr.CheckedFunction3;
import io.vavr.CheckedFunction4;
import io.vavr.CheckedFunction5;
import io.vavr.CheckedFunction6;
import io.vavr.CheckedFunction7;
import io.vavr.CheckedFunction8;
import io.vavr.CheckedPredicate;
import io.vavr.CheckedRunnable;
import io.vavr.PartialFunction;
import io.vavr.Value;
import io.vavr.collection.IndexedSeq;
import io.vavr.collection.Iterator;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Either;
import io.vavr.control.GwtIncompatible;
import io.vavr.control.Option;
import io.vavr.control.TryModule;
import io.vavr.control.Validation;
import java.io.Serializable;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Try<T>
extends Value<T>,
Serializable {
    public static final long serialVersionUID = 1L;

    public static <T> Try<T> of(CheckedFunction0<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        try {
            return new Success(supplier.apply());
        }
        catch (Throwable t) {
            return new Failure(t);
        }
    }

    public static <T> Try<T> ofSupplier(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return Try.of(supplier::get);
    }

    public static <T> Try<T> ofCallable(Callable<? extends T> callable) {
        Objects.requireNonNull(callable, "callable is null");
        return Try.of(callable::call);
    }

    public static Try<Void> run(CheckedRunnable runnable2) {
        Objects.requireNonNull(runnable2, "runnable is null");
        try {
            runnable2.run();
            return new Success<Void>(null);
        }
        catch (Throwable t) {
            return new Failure<Void>(t);
        }
    }

    public static Try<Void> runRunnable(Runnable runnable2) {
        Objects.requireNonNull(runnable2, "runnable is null");
        return Try.run(runnable2::run);
    }

    public static <T> Try<Seq<T>> sequence(Iterable<? extends Try<? extends T>> values2) {
        Objects.requireNonNull(values2, "values is null");
        IndexedSeq vector = Vector.empty();
        for (Try<T> value : values2) {
            if (value.isFailure()) {
                return Try.failure(value.getCause());
            }
            vector = vector.append((Object)value.get());
        }
        return Try.success(vector);
    }

    public static <T, U> Try<Seq<U>> traverse(Iterable<? extends T> values2, Function<? super T, ? extends Try<? extends U>> mapper) {
        Objects.requireNonNull(values2, "values is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return Try.sequence(Iterator.ofAll(values2).map(mapper));
    }

    public static <T> Try<T> success(T value) {
        return new Success(value);
    }

    public static <T> Try<T> failure(Throwable exception) {
        return new Failure(exception);
    }

    public static <T> Try<T> narrow(Try<? extends T> t) {
        return t;
    }

    default public Try<T> andThen(Consumer<? super T> consumer) {
        Objects.requireNonNull(consumer, "consumer is null");
        return this.andThenTry(consumer::accept);
    }

    default public Try<T> andThenTry(CheckedConsumer<? super T> consumer) {
        Objects.requireNonNull(consumer, "consumer is null");
        if (this.isFailure()) {
            return this;
        }
        try {
            consumer.accept(this.get());
            return this;
        }
        catch (Throwable t) {
            return new Failure(t);
        }
    }

    default public Try<T> andThen(Runnable runnable2) {
        Objects.requireNonNull(runnable2, "runnable is null");
        return this.andThenTry(runnable2::run);
    }

    default public Try<T> andThenTry(CheckedRunnable runnable2) {
        Objects.requireNonNull(runnable2, "runnable is null");
        if (this.isFailure()) {
            return this;
        }
        try {
            runnable2.run();
            return this;
        }
        catch (Throwable t) {
            return new Failure(t);
        }
    }

    default public <R> Try<R> collect(PartialFunction<? super T, ? extends R> partialFunction) {
        Objects.requireNonNull(partialFunction, "partialFunction is null");
        return this.filter(partialFunction::isDefinedAt).map(partialFunction::apply);
    }

    default public Try<Throwable> failed() {
        if (this.isFailure()) {
            return new Success<Throwable>(this.getCause());
        }
        return new Failure<Throwable>(new NoSuchElementException("Success.failed()"));
    }

    default public Try<T> filter(Predicate<? super T> predicate, Supplier<? extends Throwable> throwableSupplier) {
        Objects.requireNonNull(predicate, "predicate is null");
        Objects.requireNonNull(throwableSupplier, "throwableSupplier is null");
        return this.filterTry(predicate::test, throwableSupplier);
    }

    default public Try<T> filter(Predicate<? super T> predicate, Function<? super T, ? extends Throwable> errorProvider) {
        Objects.requireNonNull(predicate, "predicate is null");
        Objects.requireNonNull(errorProvider, "errorProvider is null");
        return this.filterTry(predicate::test, errorProvider::apply);
    }

    default public Try<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.filterTry(predicate::test);
    }

    default public Try<T> filterTry(CheckedPredicate<? super T> predicate, Supplier<? extends Throwable> throwableSupplier) {
        Objects.requireNonNull(predicate, "predicate is null");
        Objects.requireNonNull(throwableSupplier, "throwableSupplier is null");
        if (this.isFailure()) {
            return this;
        }
        try {
            if (predicate.test(this.get())) {
                return this;
            }
            return new Failure(throwableSupplier.get());
        }
        catch (Throwable t) {
            return new Failure(t);
        }
    }

    default public Try<T> filterTry(CheckedPredicate<? super T> predicate, CheckedFunction1<? super T, ? extends Throwable> errorProvider) {
        Objects.requireNonNull(predicate, "predicate is null");
        Objects.requireNonNull(errorProvider, "errorProvider is null");
        return this.flatMapTry(t -> predicate.test(t) ? this : Try.failure((Throwable)errorProvider.apply((T)t)));
    }

    default public Try<T> filterTry(CheckedPredicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.filterTry(predicate, () -> new NoSuchElementException("Predicate does not hold for " + this.get()));
    }

    default public <U> Try<U> flatMap(Function<? super T, ? extends Try<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return this.flatMapTry(mapper::apply);
    }

    default public <U> Try<U> flatMapTry(CheckedFunction1<? super T, ? extends Try<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (this.isFailure()) {
            return (Failure)this;
        }
        try {
            return mapper.apply(this.get());
        }
        catch (Throwable t) {
            return new Failure(t);
        }
    }

    @Override
    public T get();

    public Throwable getCause();

    @Override
    default public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isEmpty();

    public boolean isFailure();

    @Override
    default public boolean isLazy() {
        return false;
    }

    @Override
    default public boolean isSingleValued() {
        return true;
    }

    public boolean isSuccess();

    @Override
    default public Iterator<T> iterator() {
        return this.isSuccess() ? Iterator.of(this.get()) : Iterator.empty();
    }

    @Override
    default public <U> Try<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return this.mapTry(mapper::apply);
    }

    @GwtIncompatible
    default public Try<T> mapFailure(API.Match.Case<? extends Throwable, ? extends Throwable> ... cases) {
        if (this.isSuccess()) {
            return this;
        }
        Option<? extends Throwable> x = API.Match(this.getCause()).option(cases);
        return x.isEmpty() ? this : Try.failure(x.get());
    }

    default public <U> Try<U> mapTry(CheckedFunction1<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (this.isFailure()) {
            return (Failure)this;
        }
        try {
            return new Success(mapper.apply(this.get()));
        }
        catch (Throwable t) {
            return new Failure(t);
        }
    }

    default public Try<T> onFailure(Consumer<? super Throwable> action) {
        Objects.requireNonNull(action, "action is null");
        if (this.isFailure()) {
            action.accept(this.getCause());
        }
        return this;
    }

    @GwtIncompatible
    default public <X extends Throwable> Try<T> onFailure(Class<X> exceptionType, Consumer<? super X> action) {
        Objects.requireNonNull(exceptionType, "exceptionType is null");
        Objects.requireNonNull(action, "action is null");
        if (this.isFailure() && exceptionType.isAssignableFrom(this.getCause().getClass())) {
            action.accept(this.getCause());
        }
        return this;
    }

    default public Try<T> onSuccess(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        if (this.isSuccess()) {
            action.accept(this.get());
        }
        return this;
    }

    default public Try<T> orElse(Try<? extends T> other) {
        Objects.requireNonNull(other, "other is null");
        return this.isSuccess() ? this : other;
    }

    default public Try<T> orElse(Supplier<? extends Try<? extends T>> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return this.isSuccess() ? this : supplier.get();
    }

    default public T getOrElseGet(Function<? super Throwable, ? extends T> other) {
        Objects.requireNonNull(other, "other is null");
        if (this.isFailure()) {
            return other.apply(this.getCause());
        }
        return this.get();
    }

    default public void orElseRun(Consumer<? super Throwable> action) {
        Objects.requireNonNull(action, "action is null");
        if (this.isFailure()) {
            action.accept(this.getCause());
        }
    }

    default public <X extends Throwable> T getOrElseThrow(Function<? super Throwable, X> exceptionProvider) throws X {
        Objects.requireNonNull(exceptionProvider, "exceptionProvider is null");
        if (this.isFailure()) {
            throw (Throwable)exceptionProvider.apply(this.getCause());
        }
        return this.get();
    }

    default public <X> X fold(Function<? super Throwable, ? extends X> ifFail, Function<? super T, ? extends X> f) {
        if (this.isFailure()) {
            return ifFail.apply(this.getCause());
        }
        return f.apply(this.get());
    }

    @Override
    default public Try<T> peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        if (this.isSuccess()) {
            action.accept(this.get());
        }
        return this;
    }

    @GwtIncompatible
    default public <X extends Throwable> Try<T> recover(Class<X> exceptionType, Function<? super X, ? extends T> f) {
        Throwable cause;
        Objects.requireNonNull(exceptionType, "exceptionType is null");
        Objects.requireNonNull(f, "f is null");
        if (this.isFailure() && exceptionType.isAssignableFrom((cause = this.getCause()).getClass())) {
            return Try.of(() -> f.apply((Object)cause));
        }
        return this;
    }

    @GwtIncompatible
    default public <X extends Throwable> Try<T> recoverWith(Class<X> exceptionType, Function<? super X, Try<? extends T>> f) {
        Throwable cause;
        Objects.requireNonNull(exceptionType, "exceptionType is null");
        Objects.requireNonNull(f, "f is null");
        if (this.isFailure() && exceptionType.isAssignableFrom((cause = this.getCause()).getClass())) {
            try {
                return Try.narrow(f.apply(cause));
            }
            catch (Throwable t) {
                return new Failure(t);
            }
        }
        return this;
    }

    @GwtIncompatible
    default public <X extends Throwable> Try<T> recoverWith(Class<X> exceptionType, Try<? extends T> recovered) {
        Objects.requireNonNull(exceptionType, "exeptionType is null");
        Objects.requireNonNull(recovered, "recovered is null");
        return this.isFailure() && exceptionType.isAssignableFrom(this.getCause().getClass()) ? Try.narrow(recovered) : this;
    }

    @GwtIncompatible
    default public <X extends Throwable> Try<T> recover(Class<X> exceptionType, T value) {
        Objects.requireNonNull(exceptionType, "exceptionType is null");
        return this.isFailure() && exceptionType.isAssignableFrom(this.getCause().getClass()) ? Try.success(value) : this;
    }

    default public Try<T> recover(Function<? super Throwable, ? extends T> f) {
        Objects.requireNonNull(f, "f is null");
        if (this.isFailure()) {
            return Try.of(() -> f.apply(this.getCause()));
        }
        return this;
    }

    default public Try<T> recoverWith(Function<? super Throwable, ? extends Try<? extends T>> f) {
        Objects.requireNonNull(f, "f is null");
        if (this.isFailure()) {
            try {
                return f.apply(this.getCause());
            }
            catch (Throwable t) {
                return new Failure(t);
            }
        }
        return this;
    }

    default public Either<Throwable, T> toEither() {
        if (this.isFailure()) {
            return Either.left(this.getCause());
        }
        return Either.right(this.get());
    }

    default public Validation<Throwable, T> toValidation() {
        return this.toValidation(Function.identity());
    }

    @Override
    default public <U> Validation<U, T> toValidation(Function<? super Throwable, ? extends U> throwableMapper) {
        Objects.requireNonNull(throwableMapper, "throwableMapper is null");
        if (this.isFailure()) {
            return Validation.invalid(throwableMapper.apply(this.getCause()));
        }
        return Validation.valid(this.get());
    }

    default public <U> U transform(Function<? super Try<T>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    default public Try<T> andFinally(Runnable runnable2) {
        Objects.requireNonNull(runnable2, "runnable is null");
        return this.andFinallyTry(runnable2::run);
    }

    default public Try<T> andFinallyTry(CheckedRunnable runnable2) {
        Objects.requireNonNull(runnable2, "runnable is null");
        try {
            runnable2.run();
            return this;
        }
        catch (Throwable t) {
            return new Failure(t);
        }
    }

    @Override
    public boolean equals(Object var1);

    @Override
    public int hashCode();

    @Override
    public String toString();

    public static <T1 extends AutoCloseable> WithResources1<T1> withResources(CheckedFunction0<? extends T1> t1Supplier) {
        return new WithResources1(t1Supplier);
    }

    public static <T1 extends AutoCloseable, T2 extends AutoCloseable> WithResources2<T1, T2> withResources(CheckedFunction0<? extends T1> t1Supplier, CheckedFunction0<? extends T2> t2Supplier) {
        return new WithResources2(t1Supplier, t2Supplier);
    }

    public static <T1 extends AutoCloseable, T2 extends AutoCloseable, T3 extends AutoCloseable> WithResources3<T1, T2, T3> withResources(CheckedFunction0<? extends T1> t1Supplier, CheckedFunction0<? extends T2> t2Supplier, CheckedFunction0<? extends T3> t3Supplier) {
        return new WithResources3(t1Supplier, t2Supplier, t3Supplier);
    }

    public static <T1 extends AutoCloseable, T2 extends AutoCloseable, T3 extends AutoCloseable, T4 extends AutoCloseable> WithResources4<T1, T2, T3, T4> withResources(CheckedFunction0<? extends T1> t1Supplier, CheckedFunction0<? extends T2> t2Supplier, CheckedFunction0<? extends T3> t3Supplier, CheckedFunction0<? extends T4> t4Supplier) {
        return new WithResources4(t1Supplier, t2Supplier, t3Supplier, t4Supplier);
    }

    public static <T1 extends AutoCloseable, T2 extends AutoCloseable, T3 extends AutoCloseable, T4 extends AutoCloseable, T5 extends AutoCloseable> WithResources5<T1, T2, T3, T4, T5> withResources(CheckedFunction0<? extends T1> t1Supplier, CheckedFunction0<? extends T2> t2Supplier, CheckedFunction0<? extends T3> t3Supplier, CheckedFunction0<? extends T4> t4Supplier, CheckedFunction0<? extends T5> t5Supplier) {
        return new WithResources5(t1Supplier, t2Supplier, t3Supplier, t4Supplier, t5Supplier);
    }

    public static <T1 extends AutoCloseable, T2 extends AutoCloseable, T3 extends AutoCloseable, T4 extends AutoCloseable, T5 extends AutoCloseable, T6 extends AutoCloseable> WithResources6<T1, T2, T3, T4, T5, T6> withResources(CheckedFunction0<? extends T1> t1Supplier, CheckedFunction0<? extends T2> t2Supplier, CheckedFunction0<? extends T3> t3Supplier, CheckedFunction0<? extends T4> t4Supplier, CheckedFunction0<? extends T5> t5Supplier, CheckedFunction0<? extends T6> t6Supplier) {
        return new WithResources6(t1Supplier, t2Supplier, t3Supplier, t4Supplier, t5Supplier, t6Supplier);
    }

    public static <T1 extends AutoCloseable, T2 extends AutoCloseable, T3 extends AutoCloseable, T4 extends AutoCloseable, T5 extends AutoCloseable, T6 extends AutoCloseable, T7 extends AutoCloseable> WithResources7<T1, T2, T3, T4, T5, T6, T7> withResources(CheckedFunction0<? extends T1> t1Supplier, CheckedFunction0<? extends T2> t2Supplier, CheckedFunction0<? extends T3> t3Supplier, CheckedFunction0<? extends T4> t4Supplier, CheckedFunction0<? extends T5> t5Supplier, CheckedFunction0<? extends T6> t6Supplier, CheckedFunction0<? extends T7> t7Supplier) {
        return new WithResources7(t1Supplier, t2Supplier, t3Supplier, t4Supplier, t5Supplier, t6Supplier, t7Supplier);
    }

    public static <T1 extends AutoCloseable, T2 extends AutoCloseable, T3 extends AutoCloseable, T4 extends AutoCloseable, T5 extends AutoCloseable, T6 extends AutoCloseable, T7 extends AutoCloseable, T8 extends AutoCloseable> WithResources8<T1, T2, T3, T4, T5, T6, T7, T8> withResources(CheckedFunction0<? extends T1> t1Supplier, CheckedFunction0<? extends T2> t2Supplier, CheckedFunction0<? extends T3> t3Supplier, CheckedFunction0<? extends T4> t4Supplier, CheckedFunction0<? extends T5> t5Supplier, CheckedFunction0<? extends T6> t6Supplier, CheckedFunction0<? extends T7> t7Supplier, CheckedFunction0<? extends T8> t8Supplier) {
        return new WithResources8(t1Supplier, t2Supplier, t3Supplier, t4Supplier, t5Supplier, t6Supplier, t7Supplier, t8Supplier);
    }

    public static final class WithResources8<T1 extends AutoCloseable, T2 extends AutoCloseable, T3 extends AutoCloseable, T4 extends AutoCloseable, T5 extends AutoCloseable, T6 extends AutoCloseable, T7 extends AutoCloseable, T8 extends AutoCloseable> {
        private final CheckedFunction0<? extends T1> t1Supplier;
        private final CheckedFunction0<? extends T2> t2Supplier;
        private final CheckedFunction0<? extends T3> t3Supplier;
        private final CheckedFunction0<? extends T4> t4Supplier;
        private final CheckedFunction0<? extends T5> t5Supplier;
        private final CheckedFunction0<? extends T6> t6Supplier;
        private final CheckedFunction0<? extends T7> t7Supplier;
        private final CheckedFunction0<? extends T8> t8Supplier;

        private WithResources8(CheckedFunction0<? extends T1> t1Supplier, CheckedFunction0<? extends T2> t2Supplier, CheckedFunction0<? extends T3> t3Supplier, CheckedFunction0<? extends T4> t4Supplier, CheckedFunction0<? extends T5> t5Supplier, CheckedFunction0<? extends T6> t6Supplier, CheckedFunction0<? extends T7> t7Supplier, CheckedFunction0<? extends T8> t8Supplier) {
            this.t1Supplier = t1Supplier;
            this.t2Supplier = t2Supplier;
            this.t3Supplier = t3Supplier;
            this.t4Supplier = t4Supplier;
            this.t5Supplier = t5Supplier;
            this.t6Supplier = t6Supplier;
            this.t7Supplier = t7Supplier;
            this.t8Supplier = t8Supplier;
        }

        public <R> Try<R> of(CheckedFunction8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? extends R> f) {
            return Try.of(() -> {
                /*
                 * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
                 * 
                 * org.benf.cfr.reader.util.ConfusedCFRException: Started 14 blocks at once
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
                 *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
                 *     at org.benf.cfr.reader.entities.Method.getAnalysis(Method.java:520)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteDynamicExpression(LambdaRewriter.java:351)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteDynamicExpression(LambdaRewriter.java:167)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteExpression(LambdaRewriter.java:105)
                 *     at org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredReturn.rewriteExpressions(StructuredReturn.java:99)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewrite(LambdaRewriter.java:88)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.rewriteLambdas(Op04StructuredStatement.java:1137)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:912)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
                 *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
                 *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1050)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseInnerClassesPass1(ClassFile.java:923)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1035)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
                 *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
                 *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
                 *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
                 *     at org.benf.cfr.reader.Main.main(Main.java:54)
                 */
                throw new IllegalStateException("Decompilation failed");
            });
        }
    }

    public static final class WithResources7<T1 extends AutoCloseable, T2 extends AutoCloseable, T3 extends AutoCloseable, T4 extends AutoCloseable, T5 extends AutoCloseable, T6 extends AutoCloseable, T7 extends AutoCloseable> {
        private final CheckedFunction0<? extends T1> t1Supplier;
        private final CheckedFunction0<? extends T2> t2Supplier;
        private final CheckedFunction0<? extends T3> t3Supplier;
        private final CheckedFunction0<? extends T4> t4Supplier;
        private final CheckedFunction0<? extends T5> t5Supplier;
        private final CheckedFunction0<? extends T6> t6Supplier;
        private final CheckedFunction0<? extends T7> t7Supplier;

        private WithResources7(CheckedFunction0<? extends T1> t1Supplier, CheckedFunction0<? extends T2> t2Supplier, CheckedFunction0<? extends T3> t3Supplier, CheckedFunction0<? extends T4> t4Supplier, CheckedFunction0<? extends T5> t5Supplier, CheckedFunction0<? extends T6> t6Supplier, CheckedFunction0<? extends T7> t7Supplier) {
            this.t1Supplier = t1Supplier;
            this.t2Supplier = t2Supplier;
            this.t3Supplier = t3Supplier;
            this.t4Supplier = t4Supplier;
            this.t5Supplier = t5Supplier;
            this.t6Supplier = t6Supplier;
            this.t7Supplier = t7Supplier;
        }

        public <R> Try<R> of(CheckedFunction7<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? extends R> f) {
            return Try.of(() -> {
                /*
                 * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
                 * 
                 * org.benf.cfr.reader.util.ConfusedCFRException: Started 12 blocks at once
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
                 *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
                 *     at org.benf.cfr.reader.entities.Method.getAnalysis(Method.java:520)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteDynamicExpression(LambdaRewriter.java:351)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteDynamicExpression(LambdaRewriter.java:167)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteExpression(LambdaRewriter.java:105)
                 *     at org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredReturn.rewriteExpressions(StructuredReturn.java:99)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewrite(LambdaRewriter.java:88)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.rewriteLambdas(Op04StructuredStatement.java:1137)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:912)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
                 *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
                 *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1050)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseInnerClassesPass1(ClassFile.java:923)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1035)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
                 *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
                 *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
                 *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
                 *     at org.benf.cfr.reader.Main.main(Main.java:54)
                 */
                throw new IllegalStateException("Decompilation failed");
            });
        }
    }

    public static final class WithResources6<T1 extends AutoCloseable, T2 extends AutoCloseable, T3 extends AutoCloseable, T4 extends AutoCloseable, T5 extends AutoCloseable, T6 extends AutoCloseable> {
        private final CheckedFunction0<? extends T1> t1Supplier;
        private final CheckedFunction0<? extends T2> t2Supplier;
        private final CheckedFunction0<? extends T3> t3Supplier;
        private final CheckedFunction0<? extends T4> t4Supplier;
        private final CheckedFunction0<? extends T5> t5Supplier;
        private final CheckedFunction0<? extends T6> t6Supplier;

        private WithResources6(CheckedFunction0<? extends T1> t1Supplier, CheckedFunction0<? extends T2> t2Supplier, CheckedFunction0<? extends T3> t3Supplier, CheckedFunction0<? extends T4> t4Supplier, CheckedFunction0<? extends T5> t5Supplier, CheckedFunction0<? extends T6> t6Supplier) {
            this.t1Supplier = t1Supplier;
            this.t2Supplier = t2Supplier;
            this.t3Supplier = t3Supplier;
            this.t4Supplier = t4Supplier;
            this.t5Supplier = t5Supplier;
            this.t6Supplier = t6Supplier;
        }

        public <R> Try<R> of(CheckedFunction6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> f) {
            return Try.of(() -> {
                /*
                 * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
                 * 
                 * org.benf.cfr.reader.util.ConfusedCFRException: Started 10 blocks at once
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
                 *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
                 *     at org.benf.cfr.reader.entities.Method.getAnalysis(Method.java:520)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteDynamicExpression(LambdaRewriter.java:351)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteDynamicExpression(LambdaRewriter.java:167)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteExpression(LambdaRewriter.java:105)
                 *     at org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredReturn.rewriteExpressions(StructuredReturn.java:99)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewrite(LambdaRewriter.java:88)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.rewriteLambdas(Op04StructuredStatement.java:1137)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:912)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
                 *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
                 *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1050)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseInnerClassesPass1(ClassFile.java:923)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1035)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
                 *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
                 *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
                 *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
                 *     at org.benf.cfr.reader.Main.main(Main.java:54)
                 */
                throw new IllegalStateException("Decompilation failed");
            });
        }
    }

    public static final class WithResources5<T1 extends AutoCloseable, T2 extends AutoCloseable, T3 extends AutoCloseable, T4 extends AutoCloseable, T5 extends AutoCloseable> {
        private final CheckedFunction0<? extends T1> t1Supplier;
        private final CheckedFunction0<? extends T2> t2Supplier;
        private final CheckedFunction0<? extends T3> t3Supplier;
        private final CheckedFunction0<? extends T4> t4Supplier;
        private final CheckedFunction0<? extends T5> t5Supplier;

        private WithResources5(CheckedFunction0<? extends T1> t1Supplier, CheckedFunction0<? extends T2> t2Supplier, CheckedFunction0<? extends T3> t3Supplier, CheckedFunction0<? extends T4> t4Supplier, CheckedFunction0<? extends T5> t5Supplier) {
            this.t1Supplier = t1Supplier;
            this.t2Supplier = t2Supplier;
            this.t3Supplier = t3Supplier;
            this.t4Supplier = t4Supplier;
            this.t5Supplier = t5Supplier;
        }

        public <R> Try<R> of(CheckedFunction5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> f) {
            return Try.of(() -> {
                /*
                 * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
                 * 
                 * org.benf.cfr.reader.util.ConfusedCFRException: Started 8 blocks at once
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
                 *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
                 *     at org.benf.cfr.reader.entities.Method.getAnalysis(Method.java:520)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteDynamicExpression(LambdaRewriter.java:351)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteDynamicExpression(LambdaRewriter.java:167)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteExpression(LambdaRewriter.java:105)
                 *     at org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredReturn.rewriteExpressions(StructuredReturn.java:99)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewrite(LambdaRewriter.java:88)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.rewriteLambdas(Op04StructuredStatement.java:1137)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:912)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
                 *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
                 *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1050)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseInnerClassesPass1(ClassFile.java:923)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1035)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
                 *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
                 *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
                 *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
                 *     at org.benf.cfr.reader.Main.main(Main.java:54)
                 */
                throw new IllegalStateException("Decompilation failed");
            });
        }
    }

    public static final class WithResources4<T1 extends AutoCloseable, T2 extends AutoCloseable, T3 extends AutoCloseable, T4 extends AutoCloseable> {
        private final CheckedFunction0<? extends T1> t1Supplier;
        private final CheckedFunction0<? extends T2> t2Supplier;
        private final CheckedFunction0<? extends T3> t3Supplier;
        private final CheckedFunction0<? extends T4> t4Supplier;

        private WithResources4(CheckedFunction0<? extends T1> t1Supplier, CheckedFunction0<? extends T2> t2Supplier, CheckedFunction0<? extends T3> t3Supplier, CheckedFunction0<? extends T4> t4Supplier) {
            this.t1Supplier = t1Supplier;
            this.t2Supplier = t2Supplier;
            this.t3Supplier = t3Supplier;
            this.t4Supplier = t4Supplier;
        }

        public <R> Try<R> of(CheckedFunction4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> f) {
            return Try.of(() -> {
                /*
                 * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
                 * 
                 * org.benf.cfr.reader.util.ConfusedCFRException: Started 6 blocks at once
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
                 *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
                 *     at org.benf.cfr.reader.entities.Method.getAnalysis(Method.java:520)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteDynamicExpression(LambdaRewriter.java:351)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteDynamicExpression(LambdaRewriter.java:167)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteExpression(LambdaRewriter.java:105)
                 *     at org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredReturn.rewriteExpressions(StructuredReturn.java:99)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewrite(LambdaRewriter.java:88)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.rewriteLambdas(Op04StructuredStatement.java:1137)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:912)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
                 *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
                 *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1050)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseInnerClassesPass1(ClassFile.java:923)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1035)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
                 *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
                 *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
                 *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
                 *     at org.benf.cfr.reader.Main.main(Main.java:54)
                 */
                throw new IllegalStateException("Decompilation failed");
            });
        }
    }

    public static final class WithResources3<T1 extends AutoCloseable, T2 extends AutoCloseable, T3 extends AutoCloseable> {
        private final CheckedFunction0<? extends T1> t1Supplier;
        private final CheckedFunction0<? extends T2> t2Supplier;
        private final CheckedFunction0<? extends T3> t3Supplier;

        private WithResources3(CheckedFunction0<? extends T1> t1Supplier, CheckedFunction0<? extends T2> t2Supplier, CheckedFunction0<? extends T3> t3Supplier) {
            this.t1Supplier = t1Supplier;
            this.t2Supplier = t2Supplier;
            this.t3Supplier = t3Supplier;
        }

        public <R> Try<R> of(CheckedFunction3<? super T1, ? super T2, ? super T3, ? extends R> f) {
            return Try.of(() -> {
                /*
                 * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
                 * 
                 * org.benf.cfr.reader.util.ConfusedCFRException: Started 4 blocks at once
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
                 *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
                 *     at org.benf.cfr.reader.entities.Method.getAnalysis(Method.java:520)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteDynamicExpression(LambdaRewriter.java:351)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteDynamicExpression(LambdaRewriter.java:167)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteExpression(LambdaRewriter.java:105)
                 *     at org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredReturn.rewriteExpressions(StructuredReturn.java:99)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewrite(LambdaRewriter.java:88)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.rewriteLambdas(Op04StructuredStatement.java:1137)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:912)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
                 *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
                 *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1050)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseInnerClassesPass1(ClassFile.java:923)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1035)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
                 *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
                 *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
                 *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
                 *     at org.benf.cfr.reader.Main.main(Main.java:54)
                 */
                throw new IllegalStateException("Decompilation failed");
            });
        }
    }

    public static final class WithResources2<T1 extends AutoCloseable, T2 extends AutoCloseable> {
        private final CheckedFunction0<? extends T1> t1Supplier;
        private final CheckedFunction0<? extends T2> t2Supplier;

        private WithResources2(CheckedFunction0<? extends T1> t1Supplier, CheckedFunction0<? extends T2> t2Supplier) {
            this.t1Supplier = t1Supplier;
            this.t2Supplier = t2Supplier;
        }

        public <R> Try<R> of(CheckedFunction2<? super T1, ? super T2, ? extends R> f) {
            return Try.of(() -> {
                /*
                 * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
                 * 
                 * org.benf.cfr.reader.util.ConfusedCFRException: Started 2 blocks at once
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
                 *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
                 *     at org.benf.cfr.reader.entities.Method.getAnalysis(Method.java:520)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteDynamicExpression(LambdaRewriter.java:351)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteDynamicExpression(LambdaRewriter.java:167)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteExpression(LambdaRewriter.java:105)
                 *     at org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredReturn.rewriteExpressions(StructuredReturn.java:99)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewrite(LambdaRewriter.java:88)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.rewriteLambdas(Op04StructuredStatement.java:1137)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:912)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
                 *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
                 *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1050)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseInnerClassesPass1(ClassFile.java:923)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1035)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
                 *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
                 *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
                 *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
                 *     at org.benf.cfr.reader.Main.main(Main.java:54)
                 */
                throw new IllegalStateException("Decompilation failed");
            });
        }
    }

    public static final class WithResources1<T1 extends AutoCloseable> {
        private final CheckedFunction0<? extends T1> t1Supplier;

        private WithResources1(CheckedFunction0<? extends T1> t1Supplier) {
            this.t1Supplier = t1Supplier;
        }

        public <R> Try<R> of(CheckedFunction1<? super T1, ? extends R> f) {
            return Try.of(() -> {
                try (AutoCloseable t1 = (AutoCloseable)this.t1Supplier.apply();){
                    Object r = f.apply(t1);
                    return r;
                }
            });
        }
    }

    public static final class Failure<T>
    implements Try<T>,
    Serializable {
        private static final long serialVersionUID = 1L;
        private final Throwable cause;

        private Failure(Throwable cause) {
            Objects.requireNonNull(cause, "cause is null");
            if (TryModule.isFatal(cause)) {
                TryModule.sneakyThrow(cause);
            }
            this.cause = cause;
        }

        @Override
        public T get() {
            return (T)TryModule.sneakyThrow(this.cause);
        }

        @Override
        public Throwable getCause() {
            return this.cause;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean isFailure() {
            return true;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj instanceof Failure && Arrays.deepEquals(this.cause.getStackTrace(), ((Failure)obj).cause.getStackTrace());
        }

        @Override
        public String stringPrefix() {
            return "Failure";
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(this.cause.getStackTrace());
        }

        @Override
        public String toString() {
            return this.stringPrefix() + "(" + this.cause + ")";
        }
    }

    public static final class Success<T>
    implements Try<T>,
    Serializable {
        private static final long serialVersionUID = 1L;
        private final T value;

        private Success(T value) {
            this.value = value;
        }

        @Override
        public T get() {
            return this.value;
        }

        @Override
        public Throwable getCause() {
            throw new UnsupportedOperationException("getCause on Success");
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean isFailure() {
            return false;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj instanceof Success && Objects.equals(this.value, ((Success)obj).value);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.value);
        }

        @Override
        public String stringPrefix() {
            return "Success";
        }

        @Override
        public String toString() {
            return this.stringPrefix() + "(" + this.value + ")";
        }
    }
}

