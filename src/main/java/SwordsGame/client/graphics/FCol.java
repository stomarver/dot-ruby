package SwordsGame.client.graphics;

public class FCol {
    private float[] data;
    private int size;

    public FCol(int initialCapacity) {
        this.data = new float[initialCapacity];
    }

    public void add(float value) {
        ensureCapacity(1);
        data[size++] = value;
    }

    public void add(float... values) {
        ensureCapacity(values.length);
        for (float value : values) {
            data[size++] = value;
        }
    }

    public int size() {
        return size;
    }

    public float[] array() {
        return data;
    }

    private void ensureCapacity(int additional) {
        int required = size + additional;
        if (required <= data.length) return;
        int newSize = Math.max(required, data.length * 2);
        float[] next = new float[newSize];
        System.arraycopy(data, 0, next, 0, size);
        data = next;
    }
}
