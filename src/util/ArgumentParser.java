// Xiaoou, Dec,2013
// xli65@usfca.edu

package util;

import java.util.HashMap;
import java.util.Map;

public class ArgumentParser {
    private final Map<String, String> args = new HashMap<String, String>();

    public ArgumentParser(String[] args) {
        parseArgs(args);
    }

    private void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (isFlag(args[i])) {
                if (i < args.length - 1) {
                    if (!isFlag(args[i + 1])) {
                        this.args.put(args[i], args[i + 1]);
                    } else {
                        this.args.put(args[i], null);
                    }
                } else {
                    this.args.put(args[i], null);
                }
            } else {
                if (isFlag(args[i - 1])) {
                    continue;
                } else {
                    System.out.println("Two values!");
                    System.exit(0);
                }
            }
        }
    }

    public static boolean isFlag(String arg) {
        if (arg != null) {
            return arg.startsWith("-");
        } else {
            return false;
        }
    }

    public static boolean isValue(String arg) {
        if (arg != null) {
            return (!arg.startsWith("-"));
        } else {
            return false;
        }
    }

    public boolean hasFlag(String flag) {
        return args.containsKey(flag);
    }

    public boolean hasValue(String flag) {
        return hasFlag(flag) && args.get(flag) != null;
    }

    public String getValue(String flag) {
        return args.get(flag);
    }

    public int numFlags() {
        return args.size();
    }

    public int numArguments() {
        return args.size();
    }
}