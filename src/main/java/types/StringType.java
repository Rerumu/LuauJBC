package types;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class StringType extends ValueType {
    private final byte[] data;

    public StringType(byte[] data) {
        this.data = data;
    }

    public StringType(String data) {
        this(data.getBytes(StandardCharsets.ISO_8859_1));
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof StringType other && Arrays.equals(this.data, other.data);
    }

    @Override
    public int compareTo(@NotNull ValueType object) {
        if (object instanceof StringType other) {
            return Arrays.compare(this.data, other.data);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public String toString() {
        return new String(this.data, StandardCharsets.ISO_8859_1);
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
        return new NumberType(this.data.length);
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
