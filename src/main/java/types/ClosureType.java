package types;

import org.jetbrains.annotations.NotNull;

public abstract class ClosureType extends ValueType {
    @Override
    public int compareTo(@NotNull ValueType other) {
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
        throw new UnsupportedOperationException();
    }

    @Override
    public ValueType getField(ValueType key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setField(ValueType key, ValueType value) {
        throw new UnsupportedOperationException();
    }
}
