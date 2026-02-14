package SwordsGame.rts;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class ResourceBundle {
    private final EnumMap<ResourceType, Integer> values = new EnumMap<>(ResourceType.class);

    public ResourceBundle() {
        for (ResourceType type : ResourceType.values()) {
            values.put(type, 0);
        }
    }

    public static ResourceBundle of(ResourceType type, int amount) {
        ResourceBundle bundle = new ResourceBundle();
        bundle.set(type, amount);
        return bundle;
    }

    public static ResourceBundle of(ResourceType a, int amountA, ResourceType b, int amountB) {
        ResourceBundle bundle = new ResourceBundle();
        bundle.set(a, amountA);
        bundle.set(b, amountB);
        return bundle;
    }

    public void set(ResourceType type, int amount) {
        values.put(type, Math.max(0, amount));
    }

    public int get(ResourceType type) {
        return values.getOrDefault(type, 0);
    }

    public ResourceBundle copy() {
        ResourceBundle copy = new ResourceBundle();
        for (Map.Entry<ResourceType, Integer> entry : values.entrySet()) {
            copy.set(entry.getKey(), entry.getValue());
        }
        return copy;
    }

    public ResourceBundle add(ResourceBundle other) {
        ResourceBundle result = copy();
        for (ResourceType type : ResourceType.values()) {
            result.set(type, result.get(type) + other.get(type));
        }
        return result;
    }

    public Map<ResourceType, Integer> asMap() {
        return Collections.unmodifiableMap(values);
    }
}
