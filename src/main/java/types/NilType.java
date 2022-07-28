package types;

import org.jetbrains.annotations.NotNull;

public class NilType extends ValueType {
    public static final NilType SINGLETON = new NilType();

    private NilType() {
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof NilType;
    }

    @Override
    public int compareTo(@NotNull ValueType other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean toBoolean() {
        return false;
    }

    @Override
    public double toNumber() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanType not() {
        return BooleanType.from(true);
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
        return null;
    }

    @Override
    public ValueType get_field(ValueType key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set_field(ValueType key, ValueType value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ValueType[] call(ValueType[] arguments) {
        throw new UnsupportedOperationException();
    }
}
