package com.breakinblocks.painterjs.math;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VariableSet {
    private final Map<String, Double> vars = new ConcurrentHashMap<>();
    private boolean changed = false;

    public void set(String name, double value) {
        vars.put(name, value);
        changed = true;
    }

    public double get(String name) {
        return vars.getOrDefault(name, 0.0);
    }

    public Map<String, Double> getAll() {
        return new HashMap<>(vars);
    }

    public void putAll(Map<String, Double> other) {
        vars.putAll(other);
    }

    public boolean hasChanged() {
        boolean c = changed;
        changed = false;
        return c;
    }

    public void clear() {
        vars.clear();
        changed = true;
    }
}
