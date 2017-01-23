package com.github.athenaengine.core.util;

import com.google.gson.Gson;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;

import java.io.File;

public final class GsonUtils {

    public static Object loadConfig(String path, Class c) {
        Gson gson = new Gson();

        File configFile = new File(path);
        Config config = ConfigFactory.parseFile(configFile);

        String configJSON = config.root().render(ConfigRenderOptions.concise());
        return gson.fromJson(configJSON, c);
    }
}
