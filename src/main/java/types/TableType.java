package types;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public final class TableType extends ValueType {
    private final HashMap<ValueType, ValueType> map;

    private TableType(int size) {
        this.map = new HashMap<>(size);
    }

    private TableType(TableType other) {
        this.map = new HashMap<>(other.map);
    }

    public static TableType from(int size) {
        return new TableType(size);
    }

    public static TableType copy(TableType other) {
        return new TableType(other);
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
        return NumberType.from(this.map.size());
    }

    @Override
    public ValueType getField(ValueType key) {
        return this.map.getOrDefault(key, NilType.SINGLETON);
    }

    @Override
    public void setField(ValueType key, ValueType value) {
        this.map.put(key, value);
    }

    @Override
    public ValueType[] call(ValueType[] arguments) {
        throw new UnsupportedOperationException();
    }
}
