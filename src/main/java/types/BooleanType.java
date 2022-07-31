package types;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class BooleanType extends ValueType {
    public static final BooleanType TRUE = new BooleanType(true);
    public static final BooleanType FALSE = new BooleanType(false);

    private final boolean data;

    private BooleanType(boolean data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof BooleanType other && this.data == other.data;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.data);
    }

    @Override
    public int compareTo(@NotNull ValueType other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return Boolean.toString(this.data);
    }

    @Override
    public boolean toBoolean() {
        return this.data;
    }

    @Override
    public double toNumber() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanType not() {
        return this.data ? BooleanType.FALSE : BooleanType.TRUE;
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
    public ValueType getField(ValueType key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setField(ValueType key, ValueType value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NumberType length() {
        return null;
    }

    @Override
    public ValueType[] call(ValueType[] arguments) {
        throw new UnsupportedOperationException();
    }
}
