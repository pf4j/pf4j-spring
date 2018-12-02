/*
 * Copyright (C) 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pf4j.spring;

import org.pf4j.DefaultPluginStatusProvider;
import org.pf4j.PluginStatusProvider;
import java.nio.file.Path;
import java.nio.file.Paths;


public class CustomConfigDirSpringPluginManager extends SpringPluginManager {

    private static final String CONFIG_DIR_PROPERTY_NAME = "pf4j.pluginsConfigDir";

    @Override
    protected PluginStatusProvider createPluginStatusProvider() {
        Path configPath = getConfigPath();
        return new DefaultPluginStatusProvider(configPath);
    }

    private Path getConfigPath() {
        String configDir = System.getProperty(CONFIG_DIR_PROPERTY_NAME);
        if (configDir == null) {
            return getPluginsRoot();
        }
        return Paths.get(configDir);
    }

}
