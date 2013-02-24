package ru.selena.util.collections;

/**
 * Date: 12/16/12
 * Time: 12:03 PM
 *
 * @author Artem Titov
 */
public class Pair<X, Y> {
    private final X first;
    private final Y second;
    private final int calculatedHashCode;

    public Pair(final X first, final Y second) {
        this.first = first;
        this.second = second;
        this.calculatedHashCode = calculateHashCode();
    }

    public X getFirst() {
        return first;
    }

    public Y getSecond() {
        return second;
    }

    public static <X, Y> Pair<X, Y> of(final X x, final Y y) {
        return new Pair<X, Y>(x, y);
    }

    @Override
    public int hashCode() {
        return calculatedHashCode;
    }

    private int calculateHashCode() {
        int hashCode = 17;
        hashCode += 31 * hashCode + (first == null ? 0 : first.hashCode());
        hashCode += 31 * hashCode + (second == null ? 0 : second.hashCode());
        return hashCode;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Pair)) {
            return false;
        }
        final Pair p = (Pair) obj;
        return ((first == null && p.first == null) || (first != null && first.equals(p.first)))
                && ((second == null && p.second == null) || (second != null && second.equals(p.second)));
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)",
                first == null ? "null" : first.toString(),
                second == null ? "null" : second.toString());
    }
}
