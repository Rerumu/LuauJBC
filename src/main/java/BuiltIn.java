import types.ClosureType;
import types.StringType;
import types.TableType;
import types.ValueType;

class Print extends ClosureType {
    @Override
    public ValueType[] call(ValueType[] argList) {
        for (var i = 0; i < argList.length; i += 1) {
            var term = i == argList.length - 1 ? '\n' : '\t';

            System.out.print(argList[i]);
            System.out.print(term);
        }

        return new ValueType[0];
    }
}


public class BuiltIn {
    public static TableType INSTANCE = TableType.from(0, 0);

    static {
        var env = BuiltIn.INSTANCE;

        env.setField(StringType.from("print"), new Print());
    }
}
