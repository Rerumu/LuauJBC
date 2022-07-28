package types;

public abstract class ValueType implements Comparable<ValueType> {
    public abstract boolean toBoolean();

    public abstract double toNumber();

    public abstract BooleanType not();

    // For numbers
    public abstract ValueType add(ValueType other);

    public abstract ValueType sub(ValueType other);

    public abstract ValueType mul(ValueType other);

    public abstract ValueType div(ValueType other);

    public abstract ValueType mod(ValueType other);

    public abstract ValueType pow(ValueType other);

    public abstract ValueType neg();

    public final BooleanType equal(ValueType other) {
        return BooleanType.from(this.equals(other));
    }

    public final BooleanType notEqual(ValueType other) {
        return BooleanType.from(!this.equals(other));
    }

    public final BooleanType lessThan(ValueType other) {
        return BooleanType.from(this.compareTo(other) < 0);
    }

    public final BooleanType lessThanOrEqual(ValueType other) {
        return BooleanType.from(this.compareTo(other) <= 0);
    }

    public final BooleanType greaterThan(ValueType other) {
        return BooleanType.from(this.compareTo(other) > 0);
    }

    public final BooleanType greaterThanOrEqual(ValueType other) {
        return BooleanType.from(this.compareTo(other) >= 0);
    }

    // For tables
    public abstract NumberType length();

    public abstract ValueType get_field(ValueType key);

    public abstract void set_field(ValueType key, ValueType value);

    // For closures
    public abstract ValueType[] call(ValueType[] arguments);
}
