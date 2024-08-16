package com.example.heap.core;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ArrayHeapQueue<E> implements HeapQueue<E>, Cloneable {

    private static final Object[] EMPTY_ARRAY = {};

    private Object[] data;

    private int size;

    private final Comparator<? super E> comparator;

    public ArrayHeapQueue(Comparator<? super E> comparator) {
        this.data = EMPTY_ARRAY;
        this.comparator = comparator;
    }

    public ArrayHeapQueue(Comparator<? super E> comparator, int initialCapacity) {
        if (initialCapacity == 0) {
            this.data = EMPTY_ARRAY;
        } else {
            this.data = new Object[initialCapacity];
        }
        this.comparator = comparator;
    }

    public void ensureCapacity(int minCapacity) {
        if (minCapacity >= data.length) {
            int newCapacity = minCapacity < 64
                    ? (minCapacity << 1)
                    : (minCapacity + (minCapacity >>> 1));
            resize(newCapacity);
        }
    }

    public void trimToSize() {
        int size = this.size;
        if (size == 0) {
            data = EMPTY_ARRAY;
        } else if (size != data.length) {
            resize(size);
        }
    }

    private void resize(int length) {
        Object[] array = new Object[length];
        System.arraycopy(data, 0, array, 0, size);
        data = array;
    }

    private void siftUp(int index, E e) {
        Object[] data = this.data;
        while (index > 0) {
            int parentIndex = HeapQueue.parent(index);
            @SuppressWarnings("unchecked")
            E parent = (E) data[parentIndex];
            int cmp = comparator.compare(e, parent);
            if (cmp >= 0) {
                break;
            }
            data[index] = parent;
            index = parentIndex;
        }
        data[index] = e;
    }

    private void siftDown(int index, E e) {
        Object[] data = this.data;
        int size = this.size;
        int mid = size >>> 1;
        while (index < mid) {
            int leftIndex = HeapQueue.left(index);
            @SuppressWarnings("unchecked")
            E left = (E) data[leftIndex];
            int minIndex = leftIndex;
            E min = left;
            int rightIndex = HeapQueue.right(index);
            if (rightIndex < size) {
                @SuppressWarnings("unchecked")
                E right = (E) data[rightIndex];
                int cmp = comparator.compare(left, right);
                if (cmp > 0) {
                    minIndex = rightIndex;
                    min = right;
                }
            }
            if (comparator.compare(e, min) <= 0) {
                break;
            }
            data[index] = min;
            index = minIndex;
        }
        data[index] = e;
    }

    private void heapify() {
        Object[] data = this.data;
        int i = (size >>> 1) - 1;
        while (i >= 0) {
            @SuppressWarnings("unchecked")
            E element = (E) data[i];
            siftDown(i, element);
            i--;
        }

    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(E e) {
        return indexOf(e) >= 0;
    }

    @Override
    public boolean offer(E e) {
        int lastIndex = this.size;
        int newSize = lastIndex + 1;
        ensureCapacity(newSize);
        siftUp(lastIndex, e);
        this.size = newSize;
        return true;
    }

    @Override
    public E peek() {
        if (isEmpty()) {
            return null;
        }
        return get(0);
    }

    @Override
    public E poll() {
        if (isEmpty()) {
            return null;
        }
        E first = get(0);
        int lastIndex = --size;
        E last = get(lastIndex);
        data[lastIndex] = null;
        if (lastIndex > 0) {
            siftDown(0, last);
        }
        return first;
    }

    @Override
    public int indexOf(E e) {
        Object[] data = this.data;
        int size = this.size;
        if (e == null) {
            for (int i = 0; i < size; i++) {
                if (null == data[i]) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (e.equals(data[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E get(int index) {
        return (E) data[index];
    }

    @Override
    public E remove(int index) {
        Object[] data = this.data;
        int lastIndex = --size;
        @SuppressWarnings("unchecked")
        E removed = (E) data[index];
        if (lastIndex == index) {
            data[index] = null;
        } else {
            @SuppressWarnings("unchecked")
            E last = (E) data[lastIndex];
            data[lastIndex] = null;
            siftDown(index, last);
            if (data[index] == last) {
                siftUp(index, last);
            }
        }
        return removed;
    }

    @Override
    public void clear() {
        Object[] data = this.data;
        int size = this.size;
        this.size = 0;
        for (int i = 0; i < size; i++) {
            data[i] = null;
        }
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        Object[] data = this.data;
        int size = this.size;
        int dest = 0;
        for (int i = 0; i < size; i++) {
            @SuppressWarnings("unchecked")
            E element = (E) data[i];
            if (!filter.test(element)) {
                dest++;
            }
            data[dest] = data[i];
        }
        if (size == dest) {
            return false;
        }
        this.size = dest;
        for (int i = dest; i < size; i++) {
            data[i] = null;
        }
        heapify();
        return true;
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        Object[] data = this.data;
        int size = this.size;
        for (int i = 0; i < size; i++) {
            @SuppressWarnings("unchecked")
            E element = (E) data[i];
            action.accept(element);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public ArrayHeapQueue<E> clone() {
        ArrayHeapQueue<E> clone;
        try {
            clone = (ArrayHeapQueue<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
        Object[] data = this.data;
        Object[] array = new Object[data.length];
        System.arraycopy(data, 0, array, 0, size);
        clone.data = array;
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayHeapQueue<?> that = (ArrayHeapQueue<?>) o;
        int size = this.size;
        if (size != that.size) {
            return false;
        }
        Object[] d1 = this.data;
        Object[] d2 = that.data;
        for (int i = 0; i < size; i++) {
            Object e1 = d1[i];
            Object e2 = d2[i];
            if (!Objects.equals(e1, e2)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        Object[] data = this.data;
        int size = this.size;
        int hash = size == 0 ? 31 : size;
        for (int i = 0; i < size; i++) {
            Object e = data[i];
            hash = 31 * hash + (e != null ? e.hashCode() : 0);
        }
        return hash;
    }

    @Override
    public String toString() {
        int size = this.size;
        if (size == 0) {
            return "[]";
        }
        Object[] data = this.data;
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < size; i++) {
            Object e = data[i];
            sb.append(e).append(',').append(' ');
        }
        sb.setLength(sb.length() - 2);
        sb.append(']');
        return sb.toString();
    }
}
