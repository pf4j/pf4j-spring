/*
 * Copyright 2016 Decebal Suiu
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

import org.pf4j.PluginManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link SpringExtensionFactory} that always returns a specific instance.
 * Optional you can specify the extension classes for which you want singletons.
 *
 * @author Decebal Suiu
 */
public class SingletonSpringExtensionFactory extends SpringExtensionFactory {

    private final List<String> extensionClassNames;

    private Map<String, Object> cache;

    public SingletonSpringExtensionFactory(PluginManager pluginManager) {
        this(pluginManager, true);
    }

    public SingletonSpringExtensionFactory(PluginManager pluginManager, String... extensionClassNames) {
        this(pluginManager, true, extensionClassNames);
    }

    public SingletonSpringExtensionFactory(PluginManager pluginManager, boolean autowire, String... extensionClassNames) {
        super(pluginManager, autowire);

        this.extensionClassNames = Arrays.asList(extensionClassNames);

        cache = new HashMap<>(); // simple cache implementation
    }

    @Override
    public Object create(Class<?> extensionClass) {
        String extensionClassName = extensionClass.getName();
        if (cache.containsKey(extensionClassName)) {
            return cache.get(extensionClassName);
        }

        Object extension = super.create(extensionClass);
        if (extensionClassNames.isEmpty() || extensionClassNames.contains(extensionClassName)) {
            cache.put(extensionClassName, extension);
        }

        return extension;
    }

}
