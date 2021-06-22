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

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author Decebal Suiu
 */
public abstract class SpringPlugin extends Plugin {

    private ApplicationContext selfApplicationContext;

    private boolean useParentApplicationContext = false;

    public SpringPlugin(PluginWrapper wrapper, boolean useParentApplicationContext) {
        super(wrapper);
        this.useParentApplicationContext = useParentApplicationContext;
    }

    public SpringPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    public boolean isUseParentApplicationContext() {
        return useParentApplicationContext;
    }

    /** Desc: <br>
     * 〈support Parent-ApplicationContext>
     *
     * @param parentApplicationContext  Parent-ApplicationContext

     * @return: org.springframework.context.ApplicationContext
     * @since : 1.0.0
     * @author : Ted
     * @date : 2021/6/22 13:04
     */
    public final ApplicationContext getApplicationContext(ApplicationContext parentApplicationContext) {
        if (selfApplicationContext == null) {
            selfApplicationContext = createApplicationContext(parentApplicationContext);
        }

        return selfApplicationContext;
    }

    public final ApplicationContext getApplicationContext() {
        if (selfApplicationContext == null) {
            selfApplicationContext = createApplicationContext();
        }

        return selfApplicationContext;
    }

    @Override
    public void stop() {
        // close applicationContext
        if ((selfApplicationContext != null) && (selfApplicationContext instanceof ConfigurableApplicationContext)) {
            ((ConfigurableApplicationContext) selfApplicationContext).close();
        }
    }

    /** Desc: <br>
     * 〈support Parent-ApplicationContext〉
     *  You can ignore the Parent-ApplicationContext
     * @param parentApplicationContext Parent-ApplicationContext

     * @return: org.springframework.context.ApplicationContext
     * @since : 1.0.0
     * @author : Ted
     * @date : 2021/6/22 13:05
     */
    protected abstract ApplicationContext createApplicationContext(ApplicationContext parentApplicationContext);

    protected abstract ApplicationContext createApplicationContext();

}
