package builtin;

import org.jetbrains.annotations.NotNull;
import types.NumberType;
import types.StringType;
import types.ValueType;

public class Auxiliary {
    public static @NotNull ValueType concatenate(@NotNull ValueType[] list) {
        var builder = new StringBuilder();

        for (var value : list) {
            if (value instanceof StringType || value instanceof NumberType) {
                builder.append(value);
            } else {
                throw new IllegalArgumentException("Failed to coerce for concatenation");
            }
        }

        return StringType.from(builder.toString());
    }

    public static boolean shouldLoopRun(@NotNull ValueType limit, @NotNull ValueType step, @NotNull ValueType index) {
        var stepNumber = step.toNumber();

        if (stepNumber == 0.0) {
            throw new IllegalArgumentException("Step cannot be 0");
        }

        if (step.toNumber() > 0.0) {
            return index.toNumber() < limit.toNumber();
        } else {
            return index.toNumber() > limit.toNumber();
        }
    }
}
