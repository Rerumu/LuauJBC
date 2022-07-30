package types;

public abstract class ValueType implements Comparable<ValueType> {
    public abstract boolean toBoolean();

    public abstract double toNumber();

    public final ValueType and(ValueType other) {
        return this.toBoolean() ? other : this;
    }

    public final ValueType or(ValueType other) {
        return this.toBoolean() ? this : other;
    }

    public abstract BooleanType not();

    // For numbers
    public abstract ValueType add(ValueType other);

    public abstract ValueType sub(ValueType other);

    public abstract ValueType mul(ValueType other);

    public abstract ValueType div(ValueType other);

    public abstract ValueType mod(ValueType other);

    public abstract ValueType pow(ValueType other);

    public abstract ValueType neg();

    // For tables
    public abstract NumberType length();

    public abstract ValueType getField(ValueType key);

    public abstract void setField(ValueType key, ValueType value);

    // For closures
    public abstract ValueType[] call(ValueType[] arguments);
}
