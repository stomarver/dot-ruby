package SwordsGame.server.gameplay;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class ResPack {
    private final EnumMap<Res, Integer> values = new EnumMap<>(Res.class);

    public ResPack() {
        for (Res type : Res.values()) {
            values.put(type, 0);
        }
    }

    public static ResPack of(Res type, int amount) {
        ResPack bundle = new ResPack();
        bundle.set(type, amount);
        return bundle;
    }

    public static ResPack of(Res a, int amountA, Res b, int amountB) {
        ResPack bundle = new ResPack();
        bundle.set(a, amountA);
        bundle.set(b, amountB);
        return bundle;
    }

    public void set(Res type, int amount) {
        values.put(type, Math.max(0, amount));
    }

    public int get(Res type) {
        return values.getOrDefault(type, 0);
    }

    public ResPack copy() {
        ResPack copy = new ResPack();
        for (Map.Entry<Res, Integer> entry : values.entrySet()) {
            copy.set(entry.getKey(), entry.getValue());
        }
        return copy;
    }

    public ResPack add(ResPack other) {
        ResPack result = copy();
        for (Res type : Res.values()) {
            result.set(type, result.get(type) + other.get(type));
        }
        return result;
    }

    public Map<Res, Integer> asMap() {
        return Collections.unmodifiableMap(values);
    }
}
