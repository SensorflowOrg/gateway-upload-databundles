package com.sensorflow.util;


import java.io.Serializable;
import java.util.Map;

/**
 * Is this simply the AbstractMap.SimpleEntry pulled out into it's own class so it's easier to use? Yes
 *
 * @param <K>
 * @param <V>
 */
public class Pair<K, V> implements Map.Entry<K, V>, Serializable {
    private static final long serialVersionUID = -8499721149061103585L;
    private final K key;
    private V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public Pair(Map.Entry<? extends K, ? extends V> entry) {
        this.key = entry.getKey();
        this.value = entry.getValue();
    }

    public K getKey() {
        return this.key;
    }

    public V getValue() {
        return this.value;
    }

    public V setValue(V value) {
        V oldValue = this.value;
        this.value = value;
        return oldValue;
    }

    public String toString() {
        return this.key + "=" + this.value;
    }
}
