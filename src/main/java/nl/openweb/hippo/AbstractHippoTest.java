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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.hippoecm.hst.component.support.spring.util.MetadataReaderClasspathResourceScanner;
import org.hippoecm.hst.configuration.channel.ChannelInfo;
import org.hippoecm.hst.container.RequestContextProvider;
import org.hippoecm.hst.content.beans.ObjectBeanManagerException;
import org.hippoecm.hst.content.beans.manager.ObjectBeanManager;
import org.hippoecm.hst.content.beans.manager.ObjectBeanManagerImpl;
import org.hippoecm.hst.content.beans.manager.ObjectConverter;
import org.hippoecm.hst.content.beans.query.HstQueryManager;
import org.hippoecm.hst.content.beans.query.HstQueryManagerImpl;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.hst.core.search.HstQueryManagerFactoryImpl;
import org.hippoecm.hst.mock.core.component.MockHstRequest;
import org.hippoecm.hst.mock.core.component.MockHstResponse;
import org.hippoecm.hst.mock.core.container.MockComponentManager;
import org.hippoecm.hst.mock.core.request.MockComponentConfiguration;
import org.hippoecm.hst.mock.core.request.MockHstRequestContext;
import org.hippoecm.hst.mock.core.request.MockResolvedSiteMapItem;
import org.hippoecm.hst.site.HstServices;
import org.hippoecm.hst.site.content.ObjectConverterFactoryBean;
import org.hippoecm.hst.util.PathUtils;
import org.hippoecm.repository.util.DateTools;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import nl.openweb.hippo.exception.SetupTeardownException;
import nl.openweb.hippo.mock.DelegatingComponentManager;
import nl.openweb.hippo.mock.MockMount;
import nl.openweb.hippo.mock.MockResolvedMount;
import nl.openweb.jcr.Importer;


import static org.hippoecm.hst.utils.ParameterUtils.PARAMETERS_INFO_ATTRIBUTE;

/**
 * @author Ebrahim Aharpour
 * @since 8/20/2017
 */
public abstract class AbstractHippoTest {

    private static DelegatingComponentManager delegatingComponentManager = new DelegatingComponentManager();
    protected Importer importer;
    protected Node rootNode;
    protected MockHstResponse response = new MockHstResponse();
    protected MockHstRequest request = new MockHstRequest();
    protected MockHstRequestContext requestContext = new MockHstRequestContext();
    protected MockComponentManager componentManager = new MockComponentManager();
    protected ObjectConverter objectConverter;
    protected ObjectBeanManager objectBeanManager;
    protected HstQueryManagerImpl hstQueryManager;
    protected MockResolvedSiteMapItem resolvedSiteMapItem;
    protected MockResolvedMount resolvedMount;
    protected MockMount mockMount;
    protected MockComponentConfiguration componentConfiguration = new MockComponentConfiguration();

    static {
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
            importer = getImporter();
            importNodeStructure(importer);
            setRequestContextProvider();
            setObjectConverter();
            setQueryManager();
            setResolvedSiteMapItem();
            setMount();
            request.setRequestContext(requestContext);
            delegatingComponentManager.setComponentManager(componentManager);
        } catch (Exception e) {
            throw new SetupTeardownException(e);
        }
    }

    protected abstract Importer getImporter();

    protected abstract String getPathToTestResource();

    protected abstract String getAnnotatedClassesResourcePath();

    @SuppressWarnings("unchecked")
    protected  <T> T getRequestAttribute(String name) {
        return (T) request.getAttribute(name);
    }

    protected void setContentBean(String path) {
        try {
            HippoBean hippoBean = (HippoBean) requestContext.getObjectBeanManager().getObject(path);
            requestContext.setContentBean(hippoBean);
        } catch (ObjectBeanManagerException e) {
            throw new HstComponentException(e);
        }
    }

    protected void setSiteContentBase(String path) {
        try {
            HippoBean hippoBean = (HippoBean) requestContext.getObjectBeanManager().getObject(path);
            requestContext.setSiteContentBasePath(path);
            requestContext.setSiteContentBaseBean(hippoBean);
        } catch (ObjectBeanManagerException e) {
            throw new HstComponentException(e);
        }
    }

    protected void setChannelInfo(ChannelInfo channelInfo) {
        this.mockMount.setChannelInfo(channelInfo);
    }

    protected void setComponentParameterInfo(Object parameterInfo) {
        this.request.setAttribute(PARAMETERS_INFO_ATTRIBUTE, parameterInfo);
    }

    private void importNodeStructure(Importer importer) throws IOException, RepositoryException, JAXBException {
        String pathToResource = getPathToTestResource();
        if (pathToResource != null) {
            if (pathToResource.endsWith(".xml")) {
                this.rootNode = importer.createNodesFromXml(getResourceAsStream(pathToResource));
            } else {
                this.rootNode = importer.createNodesFromJson(getResourceAsStream(pathToResource));
            }
        } else {
            this.rootNode = importer.createNodesFromJson("{}");
        }
        requestContext.setSession(this.rootNode.getSession());
    }

    private void setMount() {
        this.mockMount = new MockMount();
        this.resolvedMount = new MockResolvedMount();
        this.resolvedMount.setMount(this.mockMount);
        this.requestContext.setResolvedMount(this.resolvedMount);
    }

    private void setResolvedSiteMapItem() {
        resolvedSiteMapItem = new MockResolvedSiteMapItem();
        requestContext.setResolvedSiteMapItem(resolvedSiteMapItem);
    }

    private void setQueryManager() throws RepositoryException {
        hstQueryManager = new HstQueryManagerImpl(this.rootNode.getSession(), this.objectConverter, DateTools.Resolution.MILLISECOND);
        requestContext.setDefaultHstQueryManager(hstQueryManager);
        requestContext.setHstQueryManagerFactory(new HstQueryManagerFactoryImpl());
        HashMap<Session, HstQueryManager> map = new HashMap<>();
        map.put(this.rootNode.getSession(), hstQueryManager);
        requestContext.setNonDefaultHstQueryManagers(map);
    }

    private void setObjectConverter() throws Exception {
        MetadataReaderClasspathResourceScanner resourceScanner = new MetadataReaderClasspathResourceScanner();
        resourceScanner.setResourceLoader(new PathMatchingResourcePatternResolver());
        ObjectConverterFactoryBean objectConverterFactory = new ObjectConverterFactoryBean();
        objectConverterFactory.setClasspathResourceScanner(resourceScanner);
        objectConverterFactory.setAnnotatedClassesResourcePath(getAnnotatedClassesResourcePath());
        objectConverterFactory.afterPropertiesSet();
        this.objectConverter = objectConverterFactory.getObject();

        this.objectBeanManager = new ObjectBeanManagerImpl(this.rootNode.getSession(), objectConverter);
        this.requestContext.setDefaultObjectBeanManager(objectBeanManager);
        HashMap<Session, ObjectBeanManager> map = new HashMap<>();
        map.put(this.rootNode.getSession(), objectBeanManager);
        this.requestContext.setNonDefaultObjectBeanManagers(map);

    }

    private InputStream getResourceAsStream(String pathToResource) {
        InputStream result;
        if (pathToResource.startsWith("/")) {
            result = this.getClass().getClassLoader().getResourceAsStream(PathUtils.normalizePath(pathToResource));
        } else {
            result = this.getClass().getResourceAsStream(pathToResource);
        }
        return result;
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

}
