/*
 * Copyright 2017 Open Web IT B.V. (https://www.openweb.nl/)
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
package nl.openweb.hippo.mock;

import org.hippoecm.hst.core.container.ComponentManager;
import org.hippoecm.hst.core.container.ComponentsException;
import org.hippoecm.hst.mock.core.container.MockContainerConfiguration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Mock for the ComponentManager. Use {@code addComponent(name, component)} to add Components to the manager.
 * @author Ivor Boers
 */
@SuppressWarnings("unchecked")
public class MockComponentManager implements ComponentManager {
    private Map<String, Object> components = new HashMap<>();
    private ServletConfig servletConfig;
    private ServletContext servletContext;
    protected MockContainerConfiguration containerConfiguration = new MockContainerConfiguration();

    public MockComponentManager() {
    }

    public MockContainerConfiguration getContainerConfiguration() {
        return containerConfiguration;
    }

    public void setConfigurationResources(String[] configurationResources) {
    }

    public String[] getConfigurationResources() {
        return new String[0];
    }

    /** @deprecated */
    @Deprecated
    public void setServletConfig(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
        if (servletConfig != null) {
            this.servletContext = servletConfig.getServletContext();
        }

    }

    /** @deprecated */
    @Deprecated
    public ServletConfig getServletConfig() {
        return this.servletConfig;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public ServletContext getServletContext() {
        return this.servletContext;
    }

    public void initialize() {
    }

    public void start() {
    }

    public <T> T getComponent(String name) {
        return (T) this.components.get(name);
    }

    public <T> T getComponent(Class<T> requiredType) throws ComponentsException {
        Map<String, T> componentsOfType = getComponentsOfType(requiredType);
        if (componentsOfType.isEmpty()) {
            return null;
        } else if (componentsOfType.size() != 1) {
            throw new ComponentsException("Multiple components found by the specified type, " + requiredType);
        } else {
            // return the only item in the map
            return (T) componentsOfType.get(componentsOfType.keySet().iterator().next());
        }
    }

    public <T> Map<String, T> getComponentsOfType(Class<T> requiredType) {
        return components.entrySet().stream()
                .filter(it -> it.getValue() != null)
                .filter(it -> requiredType.isAssignableFrom(it.getValue().getClass()))
                .collect(Collectors.toMap(Entry::getKey, e -> (T)e.getValue()));
    }


    public <T> T getComponent(String name, String... addonModuleNames) {
        return this.getComponent(name);
    }

    public <T> T getComponent(Class<T> requiredType, String... addonModuleNames) {
        return this.getComponent(requiredType);
    }

    public <T> Map<String, T> getComponentsOfType(Class<T> requiredType, String... addonModuleNames) {
        return getComponentsOfType(requiredType);
    }

    public void publishEvent(EventObject event) {
    }

    public void registerEventSubscriber(Object subscriber) {
    }

    public void unregisterEventSubscriber(Object subscriber) {
    }

    public void stop() {
    }

    public void close() {
    }

    /**
     * Add a component to the components Map
     * @param name the name
     * @param component the component
     * @param <T> can be any type
     */
    public <T> void addComponent(String name, T component) {
        this.components.put(name, component);
    }
}
