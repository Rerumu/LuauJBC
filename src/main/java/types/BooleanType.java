package types;

import org.jetbrains.annotations.NotNull;

public class BooleanType extends ValueType {
    private static final BooleanType TRUE = new BooleanType(true);
    private static final BooleanType FALSE = new BooleanType(false);

    private final boolean data;

    private BooleanType(boolean data) {
        this.data = data;
    }

    public static BooleanType from(boolean data) {
        return data ? TRUE : FALSE;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof BooleanType other && this.data == other.data;
    }

    @Override
    public int compareTo(@NotNull ValueType other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean toBoolean() {
        return data;
    }

    @Override
    public double toNumber() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BooleanType not() {
        return BooleanType.from(!this.data);
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
    public ValueType get_field(ValueType key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set_field(ValueType key, ValueType value) {
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
