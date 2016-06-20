package org.gradroid.depexplorer;

/**
 * 20 июн. 2016 г.
 *
 * @author denis.mirochnik
 */
public class Para<T1, T2> {
    private final T1 t1;
    private final T2 t2;

    private Para(T1 t1, T2 t2) {
        this.t1 = t1;
        this.t2 = t2;
    }

    public T1 getLeft() {
        return t1;
    }

    public T2 getRight() {
        return t2;
    }

    public static <T1, T2> Para<T1, T2> of(T1 left, T2 right) {
        return new Para<>(left, right);
    }
}
