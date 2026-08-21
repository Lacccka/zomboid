/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.time.DurationUtils;

public class ThreadUtils {
    @Deprecated
    public static final AlwaysTruePredicate ALWAYS_TRUE_PREDICATE = new AlwaysTruePredicate();
    private static final Predicate<?> ALWAYS_TRUE = t -> true;

    private static <T> Predicate<T> alwaysTruePredicate() {
        return ALWAYS_TRUE;
    }

    public static Thread findThreadById(long threadId) {
        if (threadId <= 0L) {
            throw new IllegalArgumentException("The thread id must be greater than zero");
        }
        Collection<Thread> result = ThreadUtils.findThreads((Thread t) -> t != null && t.getId() == threadId);
        return result.isEmpty() ? null : result.iterator().next();
    }

    public static Thread findThreadById(long threadId, String threadGroupName) {
        Objects.requireNonNull(threadGroupName, "threadGroupName");
        Thread thread2 = ThreadUtils.findThreadById(threadId);
        if (thread2 != null && thread2.getThreadGroup() != null && thread2.getThreadGroup().getName().equals(threadGroupName)) {
            return thread2;
        }
        return null;
    }

    public static Thread findThreadById(long threadId, ThreadGroup threadGroup) {
        Objects.requireNonNull(threadGroup, "threadGroup");
        Thread thread2 = ThreadUtils.findThreadById(threadId);
        if (thread2 != null && threadGroup.equals(thread2.getThreadGroup())) {
            return thread2;
        }
        return null;
    }

    public static Collection<ThreadGroup> findThreadGroups(Predicate<ThreadGroup> predicate) {
        return ThreadUtils.findThreadGroups(ThreadUtils.getSystemThreadGroup(), true, predicate);
    }

    public static Collection<ThreadGroup> findThreadGroups(ThreadGroup threadGroup, boolean recurse, Predicate<ThreadGroup> predicate) {
        ThreadGroup[] threadGroups;
        Objects.requireNonNull(threadGroup, "threadGroup");
        Objects.requireNonNull(predicate, "predicate");
        int count = threadGroup.activeGroupCount();
        while ((count = threadGroup.enumerate(threadGroups = new ThreadGroup[count + count / 2 + 1], recurse)) >= threadGroups.length) {
        }
        return Collections.unmodifiableCollection(Stream.of(threadGroups).limit(count).filter(predicate).collect(Collectors.toList()));
    }

    @Deprecated
    public static Collection<ThreadGroup> findThreadGroups(ThreadGroup threadGroup, boolean recurse, ThreadGroupPredicate predicate) {
        return ThreadUtils.findThreadGroups(threadGroup, recurse, predicate::test);
    }

    @Deprecated
    public static Collection<ThreadGroup> findThreadGroups(ThreadGroupPredicate predicate) {
        return ThreadUtils.findThreadGroups(ThreadUtils.getSystemThreadGroup(), true, predicate);
    }

    public static Collection<ThreadGroup> findThreadGroupsByName(String threadGroupName) {
        return ThreadUtils.findThreadGroups(ThreadUtils.predicateThreadGroup(threadGroupName));
    }

    public static Collection<Thread> findThreads(Predicate<Thread> predicate) {
        return ThreadUtils.findThreads(ThreadUtils.getSystemThreadGroup(), true, predicate);
    }

    public static Collection<Thread> findThreads(ThreadGroup threadGroup, boolean recurse, Predicate<Thread> predicate) {
        Thread[] threads;
        Objects.requireNonNull(threadGroup, "The group must not be null");
        Objects.requireNonNull(predicate, "The predicate must not be null");
        int count = threadGroup.activeCount();
        while ((count = threadGroup.enumerate(threads = new Thread[count + count / 2 + 1], recurse)) >= threads.length) {
        }
        return Collections.unmodifiableCollection(Stream.of(threads).limit(count).filter(predicate).collect(Collectors.toList()));
    }

    @Deprecated
    public static Collection<Thread> findThreads(ThreadGroup threadGroup, boolean recurse, ThreadPredicate predicate) {
        return ThreadUtils.findThreads(threadGroup, recurse, predicate::test);
    }

    @Deprecated
    public static Collection<Thread> findThreads(ThreadPredicate predicate) {
        return ThreadUtils.findThreads(ThreadUtils.getSystemThreadGroup(), true, predicate);
    }

    public static Collection<Thread> findThreadsByName(String threadName) {
        return ThreadUtils.findThreads(ThreadUtils.predicateThread(threadName));
    }

    public static Collection<Thread> findThreadsByName(String threadName, String threadGroupName) {
        Objects.requireNonNull(threadName, "threadName");
        Objects.requireNonNull(threadGroupName, "threadGroupName");
        return Collections.unmodifiableCollection(ThreadUtils.findThreadGroups(ThreadUtils.predicateThreadGroup(threadGroupName)).stream().flatMap(group -> ThreadUtils.findThreads(group, false, ThreadUtils.predicateThread(threadName)).stream()).collect(Collectors.toList()));
    }

    public static Collection<Thread> findThreadsByName(String threadName, ThreadGroup threadGroup) {
        return ThreadUtils.findThreads(threadGroup, false, ThreadUtils.predicateThread(threadName));
    }

    public static Collection<ThreadGroup> getAllThreadGroups() {
        return ThreadUtils.findThreadGroups(ThreadUtils.alwaysTruePredicate());
    }

    public static Collection<Thread> getAllThreads() {
        return ThreadUtils.findThreads(ThreadUtils.alwaysTruePredicate());
    }

    public static ThreadGroup getSystemThreadGroup() {
        ThreadGroup threadGroup;
        for (threadGroup = Thread.currentThread().getThreadGroup(); threadGroup != null && threadGroup.getParent() != null; threadGroup = threadGroup.getParent()) {
        }
        return threadGroup;
    }

    public static void join(Thread thread2, Duration duration) throws InterruptedException {
        DurationUtils.accept(thread2::join, duration);
    }

    private static <T> Predicate<T> namePredicate(String name, Function<T, String> nameGetter) {
        return t -> t != null && Objects.equals(nameGetter.apply(t), Objects.requireNonNull(name));
    }

    private static Predicate<Thread> predicateThread(String threadName) {
        return ThreadUtils.namePredicate(threadName, Thread::getName);
    }

    private static Predicate<ThreadGroup> predicateThreadGroup(String threadGroupName) {
        return ThreadUtils.namePredicate(threadGroupName, ThreadGroup::getName);
    }

    public static void sleep(Duration duration) throws InterruptedException {
        DurationUtils.accept(Thread::sleep, duration);
    }

    public static void sleepQuietly(Duration duration) {
        try {
            ThreadUtils.sleep(duration);
        }
        catch (InterruptedException interruptedException) {
            // empty catch block
        }
    }

    @Deprecated
    public ThreadUtils() {
    }

    @Deprecated
    @FunctionalInterface
    public static interface ThreadGroupPredicate {
        public boolean test(ThreadGroup var1);
    }

    @Deprecated
    @FunctionalInterface
    public static interface ThreadPredicate {
        public boolean test(Thread var1);
    }

    @Deprecated
    private static final class AlwaysTruePredicate
    implements ThreadPredicate,
    ThreadGroupPredicate {
        private AlwaysTruePredicate() {
        }

        @Override
        public boolean test(Thread thread2) {
            return true;
        }

        @Override
        public boolean test(ThreadGroup threadGroup) {
            return true;
        }
    }

    @Deprecated
    public static class ThreadIdPredicate
    implements ThreadPredicate {
        private final long threadId;

        public ThreadIdPredicate(long threadId) {
            if (threadId <= 0L) {
                throw new IllegalArgumentException("The thread id must be greater than zero");
            }
            this.threadId = threadId;
        }

        @Override
        public boolean test(Thread thread2) {
            return thread2 != null && thread2.getId() == this.threadId;
        }
    }

    @Deprecated
    public static class NamePredicate
    implements ThreadPredicate,
    ThreadGroupPredicate {
        private final String name;

        public NamePredicate(String name) {
            Objects.requireNonNull(name, "name");
            this.name = name;
        }

        @Override
        public boolean test(Thread thread2) {
            return thread2 != null && thread2.getName().equals(this.name);
        }

        @Override
        public boolean test(ThreadGroup threadGroup) {
            return threadGroup != null && threadGroup.getName().equals(this.name);
        }
    }
}

