/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.collection.Collections;
import io.vavr.collection.GwtIncompatible;
import io.vavr.collection.Seq;
import io.vavr.collection.Traversable;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

class JavaConverters {
    private JavaConverters() {
    }

    @GwtIncompatible
    static <T, C extends Seq<T>> ListView<T, C> asJava(C seq, ChangePolicy changePolicy) {
        return new ListView(seq, changePolicy.isMutable());
    }

    @GwtIncompatible(value="reflection is not supported")
    static class ListView<T, C extends Seq<T>>
    extends HasDelegate<C>
    implements List<T> {
        private static final long serialVersionUID = 1L;

        ListView(C delegate, boolean mutable) {
            super(delegate, mutable);
        }

        @Override
        public boolean add(T element) {
            this.setDelegate(() -> ((Seq)this.getDelegate()).append(element));
            return true;
        }

        @Override
        public void add(int index, T element) {
            this.setDelegate(() -> ((Seq)this.getDelegate()).insert(index, element));
        }

        @Override
        public boolean addAll(Collection<? extends T> collection) {
            Objects.requireNonNull(collection, "collection is null");
            return this.setDelegateAndCheckChanged(() -> ((Seq)this.getDelegate()).appendAll(collection));
        }

        @Override
        public boolean addAll(int index, Collection<? extends T> collection) {
            Objects.requireNonNull(collection, "collection is null");
            return this.setDelegateAndCheckChanged(() -> ((Seq)this.getDelegate()).insertAll(index, collection));
        }

        @Override
        public void clear() {
            if (this.isEmpty()) {
                return;
            }
            this.setDelegate(() -> ((Seq)this.getDelegate()).take(0));
        }

        @Override
        public boolean contains(Object obj) {
            Object that = obj;
            return ((Seq)this.getDelegate()).contains(that);
        }

        @Override
        public boolean containsAll(Collection<?> collection) {
            Objects.requireNonNull(collection, "collection is null");
            Collection<?> that = collection;
            return ((Seq)this.getDelegate()).containsAll(that);
        }

        @Override
        public T get(int index) {
            return ((Seq)this.getDelegate()).get(index);
        }

        @Override
        public int indexOf(Object obj) {
            Object that = obj;
            return ((Seq)this.getDelegate()).indexOf(that);
        }

        @Override
        public boolean isEmpty() {
            return ((Seq)this.getDelegate()).isEmpty();
        }

        @Override
        public java.util.Iterator<T> iterator() {
            return new Iterator(this);
        }

        @Override
        public int lastIndexOf(Object obj) {
            Object that = obj;
            return ((Seq)this.getDelegate()).lastIndexOf(that);
        }

        @Override
        public java.util.ListIterator<T> listIterator() {
            return new ListIterator(this, 0);
        }

        @Override
        public java.util.ListIterator<T> listIterator(int index) {
            return new ListIterator(this, index);
        }

        @Override
        public T remove(int index) {
            return this.setDelegateAndGetPreviousElement(index, () -> ((Seq)this.getDelegate()).removeAt(index));
        }

        @Override
        public boolean remove(Object obj) {
            Object that = obj;
            return this.setDelegateAndCheckChanged(() -> ((Seq)this.getDelegate()).remove(that));
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            Objects.requireNonNull(collection, "collection is null");
            Collection<?> that = collection;
            return this.setDelegateAndCheckChanged(() -> ((Seq)this.getDelegate()).removeAll(that));
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            Objects.requireNonNull(collection, "collection is null");
            Collection<?> that = collection;
            return this.setDelegateAndCheckChanged(() -> ((Seq)this.getDelegate()).retainAll(that));
        }

        @Override
        public T set(int index, T element) {
            return this.setDelegateAndGetPreviousElement(index, () -> ((Seq)this.getDelegate()).update(index, element));
        }

        @Override
        public int size() {
            return ((Seq)this.getDelegate()).size();
        }

        @Override
        public void sort(Comparator<? super T> comparator) {
            Objects.requireNonNull(comparator, "comparator is null");
            if (this.isEmpty()) {
                return;
            }
            this.setDelegate(() -> ((Seq)this.getDelegate()).sorted(comparator));
        }

        @Override
        public List<T> subList(int fromIndex, int toIndex) {
            return new ListView(((Seq)this.getDelegate()).subSequence(fromIndex, toIndex), this.isMutable());
        }

        @Override
        public Object[] toArray() {
            return ((Seq)this.getDelegate()).toJavaArray();
        }

        @Override
        public <U> U[] toArray(U[] array) {
            Object[] target;
            Objects.requireNonNull(array, "array is null");
            Seq delegate = (Seq)this.getDelegate();
            int length = delegate.length();
            if (array.length < length) {
                Class<?> newType = array.getClass();
                target = newType == Object[].class ? new Object[length] : (Object[])Array.newInstance(newType.getComponentType(), length);
            } else {
                if (array.length > length) {
                    array[length] = null;
                }
                target = array;
            }
            java.util.Iterator iter = delegate.iterator();
            for (int i = 0; i < length; ++i) {
                target[i] = iter.next();
            }
            return target;
        }

        @Override
        public boolean equals(Object o) {
            return o == this || o instanceof List && Collections.areEqual(this.getDelegate(), (List)o);
        }

        @Override
        public int hashCode() {
            return Collections.hashOrdered(this.getDelegate());
        }

        public String toString() {
            return ((Seq)this.getDelegate()).mkString("[", ", ", "]");
        }

        private T setDelegateAndGetPreviousElement(int index, Supplier<C> delegate) {
            this.ensureMutable();
            Object previousElement = ((Seq)this.getDelegate()).get(index);
            this.setDelegate(delegate);
            return previousElement;
        }

        private static class ListIterator<T, C extends Seq<T>>
        extends Iterator<T, C>
        implements java.util.ListIterator<T> {
            ListIterator(ListView<T, C> list, int index) {
                super(list);
                if (index < 0 || index > list.size()) {
                    throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + list.size());
                }
                this.nextIndex = index;
            }

            @Override
            public boolean hasPrevious() {
                return this.nextIndex != 0;
            }

            @Override
            public int nextIndex() {
                return this.nextIndex;
            }

            @Override
            public int previousIndex() {
                return this.nextIndex - 1;
            }

            @Override
            public T previous() {
                this.checkForComodification();
                int index = this.nextIndex - 1;
                if (index < 0) {
                    throw new NoSuchElementException();
                }
                if (index >= this.list.size()) {
                    throw new ConcurrentModificationException();
                }
                try {
                    Object element = this.list.get(index);
                    this.lastIndex = this.nextIndex = index;
                    return element;
                }
                catch (IndexOutOfBoundsException x) {
                    throw new ConcurrentModificationException();
                }
            }

            @Override
            public void set(T element) {
                this.list.ensureMutable();
                if (this.lastIndex < 0) {
                    throw new IllegalStateException();
                }
                this.checkForComodification();
                try {
                    this.list.set(this.lastIndex, element);
                }
                catch (IndexOutOfBoundsException x) {
                    throw new ConcurrentModificationException();
                }
            }

            @Override
            public void add(T element) {
                this.list.ensureMutable();
                this.checkForComodification();
                try {
                    int index = this.nextIndex;
                    this.list.add(index, element);
                    this.nextIndex = index + 1;
                    this.lastIndex = -1;
                    this.expectedSize = this.list.size();
                }
                catch (IndexOutOfBoundsException ex) {
                    throw new ConcurrentModificationException();
                }
            }
        }

        private static class Iterator<T, C extends Seq<T>>
        implements java.util.Iterator<T> {
            ListView<T, C> list;
            int expectedSize;
            int nextIndex = 0;
            int lastIndex = -1;

            Iterator(ListView<T, C> list) {
                this.list = list;
                this.expectedSize = list.size();
            }

            @Override
            public boolean hasNext() {
                return this.nextIndex != this.list.size();
            }

            @Override
            public T next() {
                this.checkForComodification();
                if (this.nextIndex >= this.list.size()) {
                    throw new NoSuchElementException();
                }
                try {
                    this.lastIndex = this.nextIndex++;
                    return this.list.get(this.lastIndex);
                }
                catch (IndexOutOfBoundsException x) {
                    throw new ConcurrentModificationException();
                }
            }

            @Override
            public void remove() {
                this.list.ensureMutable();
                if (this.lastIndex < 0) {
                    throw new IllegalStateException();
                }
                this.checkForComodification();
                try {
                    this.nextIndex = this.lastIndex;
                    this.list.remove(this.nextIndex);
                    this.lastIndex = -1;
                    this.expectedSize = this.list.size();
                }
                catch (IndexOutOfBoundsException x) {
                    throw new ConcurrentModificationException();
                }
            }

            @Override
            public void forEachRemaining(Consumer<? super T> consumer) {
                Objects.requireNonNull(consumer, "consumer is  null");
                this.checkForComodification();
                if (this.nextIndex >= this.list.size()) {
                    return;
                }
                int index = this.nextIndex;
                while (this.expectedSize == this.list.size() && index < this.expectedSize) {
                    consumer.accept(this.list.get(index++));
                }
                this.nextIndex = index;
                this.lastIndex = index - 1;
                this.checkForComodification();
            }

            final void checkForComodification() {
                if (this.expectedSize != this.list.size()) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    private static abstract class HasDelegate<C extends Traversable<?>>
    implements Serializable {
        private static final long serialVersionUID = 1L;
        private C delegate;
        private final boolean mutable;

        HasDelegate(C delegate, boolean mutable) {
            this.delegate = delegate;
            this.mutable = mutable;
        }

        protected boolean isMutable() {
            return this.mutable;
        }

        C getDelegate() {
            return this.delegate;
        }

        protected boolean setDelegateAndCheckChanged(Supplier<C> delegate) {
            boolean changed;
            this.ensureMutable();
            C previousDelegate = this.delegate;
            Traversable newDelegate = (Traversable)delegate.get();
            boolean bl = changed = newDelegate.size() != previousDelegate.size();
            if (changed) {
                this.delegate = newDelegate;
            }
            return changed;
        }

        protected void setDelegate(Supplier<C> newDelegate) {
            this.ensureMutable();
            this.delegate = (Traversable)newDelegate.get();
        }

        protected void ensureMutable() {
            if (!this.mutable) {
                throw new UnsupportedOperationException();
            }
        }
    }

    static enum ChangePolicy {
        IMMUTABLE,
        MUTABLE;


        boolean isMutable() {
            return this == MUTABLE;
        }
    }
}

