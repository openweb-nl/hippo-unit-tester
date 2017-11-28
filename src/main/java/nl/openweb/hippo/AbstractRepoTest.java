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
import java.util.HashMap;

import org.hippoecm.hst.component.support.spring.util.MetadataReaderClasspathResourceScanner;
import org.hippoecm.hst.content.beans.manager.ObjectBeanManager;
import org.hippoecm.hst.content.beans.manager.ObjectBeanManagerImpl;
import org.hippoecm.hst.content.beans.query.HstQueryManager;
import org.hippoecm.hst.content.beans.query.HstQueryManagerImpl;
import org.hippoecm.hst.core.search.HstQueryManagerFactoryImpl;
import org.hippoecm.hst.site.content.ObjectConverterFactoryBean;
import org.hippoecm.hst.util.PathUtils;
import org.hippoecm.repository.util.DateTools;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import nl.openweb.hippo.exception.SetupTeardownException;
import nl.openweb.jcr.Importer;
import nl.openweb.jcr.utils.NodeTypeUtils;

/**
 * @author Ebrahim Aharpour
 * @since 11/2/2017
 */
public abstract class AbstractRepoTest extends SimpleHippoTest {

    protected Importer importer;
    protected Node rootNode;

    @Override
    public void setup() {
        super.setup();
        try {
            importer = getImporter();
            importNodeStructure(importer);
            setObjectConverter();
            setQueryManager();
        } catch (Exception e) {
            throw new SetupTeardownException(e);
        }
    }

    protected abstract Importer getImporter();

    protected abstract String getAnnotatedClassesResourcePath();

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

    protected void registerMixinType(String mixinType) throws RepositoryException {
        NodeTypeUtils.createMixin(rootNode.getSession(), mixinType);
    }

    protected void registerMixinType(String mixinType, String superType) throws RepositoryException {
        NodeTypeUtils.createMixin(rootNode.getSession(), mixinType, superType);
    }

    protected void registerNodeType(String nodeType) throws RepositoryException {
        NodeTypeUtils.createNodeType(rootNode.getSession(), nodeType);
    }

    protected void registerNodeType(String nodeType, String superType) throws RepositoryException {
        NodeTypeUtils.createNodeType(rootNode.getSession(), nodeType, superType);
    }

    protected InputStream getResourceAsStream(String pathToResource) {
        InputStream result;
        if (pathToResource.startsWith("/")) {
            result = this.getClass().getClassLoader().getResourceAsStream(PathUtils.normalizePath(pathToResource));
        } else {
            result = this.getClass().getResourceAsStream(pathToResource);
        }
        return result;
    }
}
