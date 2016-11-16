/*
 * Copyright 2015 Decebal Suiu
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
package ro.fortsoft.pf4j.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import ro.fortsoft.pf4j.ExtensionFactory;
import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginManager;
import ro.fortsoft.pf4j.PluginWrapper;

/**
 * Basic implementation of a extension factory that uses Java reflection to
 * instantiate an object.
 * Create a new extension instance every time a request is done.

 * @author Decebal Suiu
 */
public class SpringExtensionFactory implements ExtensionFactory {

    private static final Logger log = LoggerFactory.getLogger(SpringExtensionFactory.class);

    private PluginManager pluginManager;
    private boolean autowire;

    public SpringExtensionFactory(PluginManager pluginManager) {
        this(pluginManager, true);
    }

    public SpringExtensionFactory(PluginManager pluginManager, boolean autowire) {
        this.pluginManager = pluginManager;
        this.autowire = autowire;
    }

    @Override
    public Object create(Class<?> extensionClass) {
        Object extension = createWithoutSpring(extensionClass);
        if (autowire && extension != null) {
            // test for SpringBean
            PluginWrapper pluginWrapper = pluginManager.whichPlugin(extensionClass);
            if (pluginWrapper != null) {
                Plugin plugin = pluginWrapper.getPlugin();
                if (plugin instanceof SpringPlugin) {
                    // autowire
                    ApplicationContext pluginContext = ((SpringPlugin) plugin).getApplicationContext();
                    pluginContext.getAutowireCapableBeanFactory().autowireBean(extension);
                }
            }
        }

        return extension;
    }

    protected Object createWithoutSpring(Class<?> extensionClass) {
        try {
            return extensionClass.newInstance();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

}
