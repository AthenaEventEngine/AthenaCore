package com.github.athenaengine.core.interfaces.tasks;

public interface IRepetitiveTask {

    long getInitialDelay();

    long getPeriod();

    void run();
}
