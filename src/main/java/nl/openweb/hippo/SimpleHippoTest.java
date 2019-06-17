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
package nl.openweb.hippo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.hippoecm.hst.configuration.channel.ChannelInfo;
import org.hippoecm.hst.container.RequestContextProvider;
import org.hippoecm.hst.core.container.ComponentManager;
import org.hippoecm.hst.core.container.ContainerConfiguration;
import org.hippoecm.hst.core.linking.HstLinkCreator;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.hst.mock.core.component.MockHstRequest;
import org.hippoecm.hst.mock.core.component.MockHstResponse;
import org.hippoecm.hst.mock.core.container.MockComponentManager;
import org.hippoecm.hst.mock.core.container.MockContainerConfiguration;
import org.hippoecm.hst.mock.core.request.MockComponentConfiguration;
import org.hippoecm.hst.mock.core.request.MockHstRequestContext;
import org.hippoecm.hst.mock.core.request.MockResolvedSiteMapItem;
import org.hippoecm.hst.site.HstServices;

import nl.openweb.hippo.exception.SetupTeardownException;
import nl.openweb.hippo.mock.DelegatingComponentManager;
import nl.openweb.hippo.mock.MockHstLinkCreator;
import nl.openweb.hippo.mock.MockMount;
import nl.openweb.hippo.mock.MockResolvedMount;


import static org.hippoecm.hst.utils.ParameterUtils.PARAMETERS_INFO_ATTRIBUTE;

/**
 * @author Ebrahim Aharpour
 * @since 8/20/2017
 */
public class SimpleHippoTest {

    public static final String COMPONENT_REFERENCE_NAMESPACE = "r1_r2";

    private static DelegatingComponentManager delegatingComponentManager = new DelegatingComponentManager();

    protected MockHstResponse response = new MockHstResponse();
    protected MockHstRequest request = new MockHstRequest();
    protected MockHstRequestContext requestContext = new MockHstRequestContext();
    protected MockContainerConfiguration containerConfiguration = new MockContainerConfiguration();
    protected MockResolvedSiteMapItem resolvedSiteMapItem;
    protected MockResolvedMount resolvedMount;
    protected MockMount mount;
    protected MockHstLinkCreator hstLinkCreator = new MockHstLinkCreator();
    protected MockComponentConfiguration componentConfiguration = new MockComponentConfiguration();
    protected MockComponentManager componentManager = new MockComponentManager() {
        @Override
        public ContainerConfiguration getContainerConfiguration() {
            return containerConfiguration;
        }
    };

    public SimpleHippoTest() {
        HstServices.setComponentManager(delegatingComponentManager);
    }


    public void teardown() {
        try {
            clearRequestContextProvider();
            delegatingComponentManager.remove();
        } catch (Exception e) {
            throw new SetupTeardownException(e);
        }
    }

    public void setup() {
        try {
            setupParameterAndAttributeMaps();

            initializedRequest();
            setRequestContextProvider();
            setResolvedSiteMapItem();
            setMount();
            request.setRequestContext(requestContext);
            setComponentManager(componentManager);
            setHstLinkCreator(hstLinkCreator);
        } catch (Exception e) {
            throw new SetupTeardownException(e);
        }
    }

    public static void setComponentManager(ComponentManager componentManager) {
        delegatingComponentManager.setComponentManager(componentManager);
    }

    protected String getPathToTestResource() {
        return "/skeleton.xml";
    }

    protected String getComponentReferenceNamespace() {
        return COMPONENT_REFERENCE_NAMESPACE;
    }

    @SuppressWarnings("unchecked")
    protected <T> T getRequestAttribute(String name) {
        return (T) request.getAttribute(name);
    }

    protected void addPublicRequestParameter(String name, String value) {
        Map<String, String[]> namespaceLessParameters = request.getParameterMap("");
        namespaceLessParameters.put(name, new String[]{value});
    }

    protected void addPublicRequestParameter(String name, String[] value) {
        Map<String, String[]> namespaceLessParameters = request.getParameterMap("");
        namespaceLessParameters.put(name, value);
    }

    protected void setChannelInfo(ChannelInfo channelInfo) {
        this.mount.setChannelInfo(channelInfo);
    }

    protected void setComponentParameterInfo(Object parameterInfo) {
        this.request.setAttribute(PARAMETERS_INFO_ATTRIBUTE, parameterInfo);
    }

    private void setupParameterAndAttributeMaps() {
        request.setAttributeMap("", new HashMap<>());
        request.setAttributeMap(COMPONENT_REFERENCE_NAMESPACE, new HashMap<>());
        request.setParameterMap("", new HashMap<>());
        request.setParameterMap(COMPONENT_REFERENCE_NAMESPACE, new HashMap<>());
    }

    private void initializedRequest() {
        request.setReferencePath(getComponentReferenceNamespace());
    }

    private void setMount() {
        this.mount = new MockMount();
        this.resolvedMount = new MockResolvedMount();
        this.resolvedMount.setMount(this.mount);
        this.requestContext.setResolvedMount(this.resolvedMount);
    }

    private void setResolvedSiteMapItem() {
        resolvedSiteMapItem = new MockResolvedSiteMapItem();
        requestContext.setResolvedSiteMapItem(resolvedSiteMapItem);
    }

    private void setRequestContextProvider() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method set = RequestContextProvider.class.getDeclaredMethod("set", HstRequestContext.class);
        set.setAccessible(true);
        set.invoke(null, requestContext);
        set.setAccessible(false);
    }

    private void clearRequestContextProvider() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method set = RequestContextProvider.class.getDeclaredMethod("clear");
        set.setAccessible(true);
        set.invoke(null);
        set.setAccessible(false);
    }

    private void setHstLinkCreator(HstLinkCreator hstLinkCreator) {
        requestContext.setHstLinkCreator(hstLinkCreator);
    }

}
