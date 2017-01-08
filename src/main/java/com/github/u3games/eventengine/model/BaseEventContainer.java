package com.github.u3games.eventengine.model;

import com.github.u3games.eventengine.config.interfaces.EventConfig;
import com.github.u3games.eventengine.events.handler.AbstractEvent;
import com.github.u3games.eventengine.interfaces.EventContainer;
import com.github.u3games.eventengine.util.GsonUtils;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseEventContainer implements EventContainer {

    private static final Logger LOGGER = Logger.getLogger(BaseEventContainer.class.getName());
    private static final String EVENTS_PATH = "./eventengine/events/";

    private EventConfig _config;

    public BaseEventContainer()
    {

    }

    public String getSimpleEventName()
    {
        return getEventName().toLowerCase().replace(" ", "");
    }

    protected abstract Class<? extends EventConfig> getConfigClass();

    protected EventConfig getConfig()
    {
        if (_config == null) _config = (EventConfig) GsonUtils.loadConfig(EVENTS_PATH + getSimpleEventName() + "/config.conf", getConfigClass());
        return _config;
    }

    @Override
    public boolean checkStructure() {
        File folder = new File("./eventengine/events/" + getSimpleEventName());

        if (!folder.exists() && !folder.mkdir()) {
            return false;
        } else {
            ClassLoader classLoader = getClass().getClassLoader();

            System.out.println("Blabla2 " + classLoader.getResource("config.conf"));
            InputStream is = classLoader.getResourceAsStream("config.conf");

            //System.out.println("Blabla3 " + getStringFromInputStream(is));
            try {
                writeFile(is);
            } catch (Exception e) {
                e.printStackTrace();
            }

            /*File file = new File(classLoader.getResource("/config.conf").getFile());

            StringBuilder result = new StringBuilder("");

            try (Scanner scanner = new Scanner(file)) {

                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    result.append(line).append("\n");
                }

                scanner.close();

            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }

        return true;
    }

    private String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

    private void writeFile(InputStream is) throws IOException {
        OutputStream os = new FileOutputStream("./eventengine/events/" + getSimpleEventName() + "/config.conf");

        byte[] buffer = new byte[is.available()];

        int bytesRead;
        //read from is to buffer
        while((bytesRead = is.read(buffer)) !=-1){
            os.write(buffer, 0, bytesRead);
        }
        is.close();
        //flush OutputStream to write any buffered data to file
        os.flush();
        os.close();
    }

    public AbstractEvent newEventInstance()
    {
        EventBuilder builder = new EventBuilder();
        builder.setEventClass(getEventClass());
        builder.setConfig(getConfig());
        return builder.build();
    }

    private class EventBuilder {

        private final Logger LOGGER = Logger.getLogger(EventBuilder.class.getName());

        private Class<? extends AbstractEvent> _eventClass;
        private EventConfig _config;

        private EventBuilder setEventClass(Class<? extends AbstractEvent> eventClass)
        {
            _eventClass = eventClass;
            return this;
        }

        public EventBuilder setConfig(EventConfig config)
        {
            _config = config;
            return this;
        }

        private AbstractEvent build()
        {
            AbstractEvent event;

            try
            {
                event = _eventClass.newInstance();
                event.setConfig(_config);
                event.initialize();
                return event;
            }
            catch (Exception e)
            {
                LOGGER.log(Level.WARNING, e.getMessage());
            }

            return null;
        }
    }
}
