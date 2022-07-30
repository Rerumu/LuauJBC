package template;

import types.ClosureType;
import types.StringType;

public class Program {
    private static ClosureType entryPoint;

    public static void main(String[] argList) {
        var translated = new StringType[argList.length];

        for (var i = 0; i < argList.length; i += 1) {
            translated[i] = StringType.from(argList[i]);
        }

        Program.entryPoint.call(translated);
    }
}
