package se.kth.id2203.kvstore;

/**
 * Created by Nick on 2/24/2017.
 */
public class CasOperation extends Operation {
    public final String reference;
    public final String newValue;

    public CasOperation(String key, String reference, String newValue) {
        super(key);
        this.reference = reference;
        this.newValue = newValue;
    }

    @Override
    public String toString() {
        return "CasOperation{" +
                "reference='" + reference + '\'' +
                ", newValue='" + newValue + '\'' +
                '}';
    }
}
