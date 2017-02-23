package se.kth.id2203.paxos;

import se.kth.id2203.kvstore.Operation;

import java.util.List;

/**
 * Created by victoraxelsson on 2017-02-23.
 */
public class ReadlistItem {

    public int ts;
    public List<Operation> vsuf;

    public ReadlistItem(int ts, List<Operation> vsuf) {
        this.ts = ts;
        this.vsuf = vsuf;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReadlistItem that = (ReadlistItem) o;

        if (ts != that.ts) return false;
        return vsuf != null ? vsuf.equals(that.vsuf) : that.vsuf == null;
    }

    @Override
    public int hashCode() {
        int result = ts;
        result = 31 * result + (vsuf != null ? vsuf.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ReadlistItem{" +
                "ts=" + ts +
                ", vsuf=" + vsuf +
                '}';
    }
}
