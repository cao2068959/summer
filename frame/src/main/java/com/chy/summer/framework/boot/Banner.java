package com.chy.summer.framework.boot;

import com.chy.summer.framework.core.evn.Environment;

import java.io.PrintStream;

public interface Banner {

    void printBanner(Environment environment, Class<?> sourceClass, PrintStream out);

    enum Mode {

        OFF,

        CONSOLE,

        LOG

    }

}
