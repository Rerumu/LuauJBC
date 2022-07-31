package types;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public final class TableType extends ValueType {
    private final HashMap<ValueType, ValueType> map;
    private final ArrayList<ValueType> list;

    private TableType(int mapSize, int arraySize) {
        this.map = new HashMap<>(mapSize);
        this.list = new ArrayList<>(arraySize);
    }

    private TableType(TableType other) {
        this.map = new HashMap<>(other.map);
        this.list = new ArrayList<>(other.list);
    }

    public static TableType from(int mapSize, int arraySize) {
        return new TableType(mapSize, arraySize);
    }

    public static TableType copy(TableType other) {
        return new TableType(other);
    }

    private static int tryToIndex(ValueType key) {
        if (key instanceof NumberType temp && temp.toNumber() % 1.0 == 0.0) {
            return (int) temp.toNumber() - 1;
        } else {
            return -1;
        }
    }

    public HashMap<ValueType, ValueType> getMap() {
        return this.map;
    }

    @Override
    public int compareTo(@NotNull ValueType object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean toBoolean() {
        return true;
    }

    @Override
    public double toNumber() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanType not() {
        return BooleanType.FALSE;
    }

    @Override
    public ValueType add(ValueType other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ValueType sub(ValueType other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ValueType mul(ValueType other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ValueType div(ValueType other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ValueType mod(ValueType other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ValueType pow(ValueType other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ValueType neg() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NumberType length() {
        return NumberType.from(this.map.size() + this.list.size());
    }

    @Override
    public ValueType getField(ValueType key) {
        var index = TableType.tryToIndex(key);

        if (index >= 0 && index < this.list.size()) {
            return this.list.get(index);
        } else {
            return this.map.getOrDefault(key, NilType.SINGLETON);
        }
    }

    @Override
    public void setField(ValueType key, ValueType value) {
        var index = TableType.tryToIndex(key);

        if (index >= 0 && index < this.list.size()) {
            this.list.set(index, value);
        } else if (index == this.list.size()) {
            this.list.add(value);
        } else {
            this.map.put(key, value);
        }
    }

    @Override
    public ValueType[] call(ValueType[] arguments) {
        throw new UnsupportedOperationException();
    }
}
