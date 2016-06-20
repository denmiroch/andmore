package org.eclipse.andmore.android.gradle;

/**
 * 20 июн. 2016 г.
 *
 * @author denis.mirochnik
 */
public class Triple<T1, T2, T3> {
    private final T1 t1;
    private final T2 t2;
    private final T3 t3;

    private Triple(T1 t1, T2 t2, T3 t3) {
        this.t1 = t1;
        this.t2 = t2;
        this.t3 = t3;
    }

    public T1 getFirst() {
        return t1;
    }

    public T2 getSecond() {
        return t2;
    }

    public T3 getThird() {
        return t3;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((t1 == null) ? 0 : t1.hashCode());
        result = prime * result + ((t2 == null) ? 0 : t2.hashCode());
        result = prime * result + ((t3 == null) ? 0 : t3.hashCode());
        return result;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Triple)) {
            return false;
        }

        Triple other = (Triple) obj;
        if (t1 == null) {
            if (other.t1 != null) {
                return false;
            }
        } else if (!t1.equals(other.t1)) {
            return false;
        }
        if (t2 == null) {
            if (other.t2 != null) {
                return false;
            }
        } else if (!t2.equals(other.t2)) {
            return false;
        }
        if (t3 == null) {
            if (other.t3 != null) {
                return false;
            }
        } else if (!t3.equals(other.t3)) {
            return false;
        }
        return true;
    }

    public static <T1, T2, T3> Triple<T1, T2, T3> of(T1 t1, T2 t2, T3 t3) {
        return new Triple<T1, T2, T3>(t1, t2, t3);
    }
}
