package types;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class TableType extends ValueType {
    private final HashMap<ValueType, ValueType> map;

    public TableType(int size) {
        map = new HashMap<>(size);
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
        return BooleanType.from(false);
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
        return new NumberType(this.map.size());
    }

    @Override
    public ValueType get_field(ValueType key) {
        return this.map.get(key);
    }

    @Override
    public void set_field(ValueType key, ValueType value) {
        this.map.put(key, value);
    }

    @Override
    public ValueType[] call(ValueType[] arguments) {
        throw new UnsupportedOperationException();
    }
}
