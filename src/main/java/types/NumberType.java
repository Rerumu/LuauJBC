package types;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class NumberType extends ValueType {
    final double data;

    private NumberType(double data) {
        this.data = data;
    }

    public static NumberType from(double data) {
        return new NumberType(data);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof NumberType other && this.data == other.data;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.data);
    }

    @Override
    public int compareTo(@NotNull ValueType other) {
        return Double.compare(this.data, other.toNumber());
    }

    @Override
    public String toString() {
        return Double.toString(this.data);
    }

    @Override
    public boolean toBoolean() {
        return true;
    }

    @Override
    public double toNumber() {
        return this.data;
    }

    @Override
    public BooleanType not() {
        return BooleanType.FALSE;
    }

    @Override
    public ValueType add(@NotNull ValueType other) {
        return new NumberType(this.data + other.toNumber());
    }

    @Override
    public ValueType sub(@NotNull ValueType other) {
        return new NumberType(this.data - other.toNumber());
    }

    @Override
    public ValueType mul(@NotNull ValueType other) {
        return new NumberType(this.data * other.toNumber());
    }

    @Override
    public ValueType div(@NotNull ValueType other) {
        return new NumberType(this.data / other.toNumber());
    }

    @Override
    public ValueType mod(@NotNull ValueType other) {
        return new NumberType(this.data % other.toNumber());
    }

    @Override
    public ValueType pow(@NotNull ValueType other) {
        var result = Math.pow(this.data, other.toNumber());

        return new NumberType(result);
    }

    @Override
    public ValueType neg() {
        return new NumberType(-this.data);
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
        throw new UnsupportedOperationException();
    }

    @Override
    public ValueType[] call(ValueType[] arguments) {
        throw new UnsupportedOperationException();
    }
}
