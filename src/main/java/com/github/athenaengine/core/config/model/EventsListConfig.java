package com.github.athenaengine.core.config.model;

import java.util.List;

public class EventsListConfig {

    private List<Event> events;

    public List<Event> getEvents() {
        return events;
    }

    public final class Event {

        public String jarName;
        public String classPath;

        public String getJarName() {
            return jarName;
        }

        public String getClassPath() {
            return classPath;
        }
    }
}
