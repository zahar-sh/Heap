package com.example.heap.core;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface HeapQueue<E> {

    Comparator<? super E> comparator();

    int size();

    boolean isEmpty();

    boolean contains(E e);

    boolean offer(E e);

    E peek();

    E poll();

    int indexOf(E e);

    E get(int index);

    E remove(int index);

    void clear();

    boolean removeIf(Predicate<? super E> filter);

    void forEach(Consumer<? super E> action);

    static int parent(int index) {
        return (index - 1) >>> 1;
    }

    static int left(int index) {
        return (index << 1) + 1;
    }

    static int right(int index) {
        return (index << 1) + 2;
    }
}
