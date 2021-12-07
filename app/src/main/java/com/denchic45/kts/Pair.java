package com.denchic45.kts;

public class Pair<F, S> {

    public final F first;
    public final S second;

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public int hashCode() { return first.hashCode() ^ second.hashCode(); }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) return false;
        Pair<?,?> pair = (Pair<?,?>) o;
        return this.first.equals(pair.first) &&
                this.second.equals(pair.second);
    }

}
