package com.chy.summer.framework.boot;

import com.chy.summer.framework.boot.ansi.AnsiColor;
import com.chy.summer.framework.boot.ansi.AnsiOutput;
import com.chy.summer.framework.boot.ansi.AnsiStyle;
import com.chy.summer.framework.core.evn.Environment;

import java.io.PrintStream;

public class SummerBootBanner implements Banner {

    private static final String[] BANNER = {
            "                                                                       ",
            "                                                                       ",
            "     _______. __    __  .___  ___. .___  ___.  _______ .______         ",
            "    /       ||  |  |  | |   \\/   | |   \\/   | |   ____||   _  \\",
            "   |   (----`|  |  |  | |  \\  /  | |  \\  /  | |  |__   |  |_)  |",
            "    \\   \\    |  |  |  | |  |\\/|  | |  |\\/|  | |   __|  |      /",
            ".----)   |   |  `--'  | |  |  |  | |  |  |  | |  |____ |  |\\  \\----.",
            "|_______/     \\______/  |__|  |__| |__|  |__| |_______|| _| `._____|",
            " ==================================================================  ",

    };





    private static final String SUMMER_BOOT = " :: Summer Boot :: ";

    private static final int STRAP_LINE_SIZE = 42;

    static {
        AnsiOutput.setConsoleAvailable(true);
    }

    @Override
    public void printBanner(Environment environment, Class<?> sourceClass,
                            PrintStream printStream) {
        for (String line : BANNER) {
            printStream.println(line);
        }
        String version = "1.0.0.RELEASE";
        version = (version != null) ? " (v" + version + ")" : "";
        StringBuilder padding = new StringBuilder();
        while (padding.length() < STRAP_LINE_SIZE
                - (version.length() + SUMMER_BOOT.length())) {
            padding.append(" ");
        }

        printStream.println(AnsiOutput.toString(AnsiColor.GREEN, SUMMER_BOOT,
                AnsiColor.DEFAULT, padding.toString(), AnsiStyle.FAINT, version));
        printStream.println();
    }

}
