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

import java.util.Map;

import org.pf4j.ExtensionFactory;
import org.pf4j.Plugin;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

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
    public <T> T create(Class<T> extensionClass) {
        T extension = createWithoutSpring(extensionClass);
        if (autowire && extension != null) {
            ApplicationContext applicationContext = this.getApplicationContext(extensionClass);
            if (applicationContext != null) {
                Map<String, T> extensionBeanMap = applicationContext.getBeansOfType(extensionClass);
                if (!extensionBeanMap.isEmpty()) {
                    if (extensionBeanMap.size() > 1) {
                        log.error("There are more than 1 extension bean '{}' defined!", extensionClass.getName());
                    }
                    extension = extensionBeanMap.values().iterator().next();
                }
                applicationContext.getAutowireCapableBeanFactory().autowireBean(extension);
            }
        }

        return extension;
    }

    protected <T> T createWithoutSpring(Class<T> extensionClass) {
        try {
            return extensionClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    private <T> ApplicationContext getApplicationContext(Class<T> extensionClass) {
        ApplicationContext applicationContext = null;
        PluginWrapper pluginWrapper = this.pluginManager.whichPlugin(extensionClass);
        if (pluginWrapper != null) { // is plugin extension
            Plugin plugin = pluginWrapper.getPlugin();
            if (plugin instanceof SpringPlugin) {
                applicationContext = ((SpringPlugin) plugin).getApplicationContext();
            } 
        } else if (this.pluginManager instanceof SpringPluginManager) { // is system extension and plugin manager is SpringPluginManager
            SpringPluginManager springPluginManager = (SpringPluginManager) this.pluginManager;
            applicationContext = springPluginManager.getApplicationContext();
        }
        return applicationContext;
    }

}
