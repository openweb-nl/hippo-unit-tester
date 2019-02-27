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
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.lang.StringUtils;
import org.hippoecm.hst.component.support.spring.util.MetadataReaderClasspathResourceScanner;
import org.hippoecm.hst.content.beans.ObjectBeanManagerException;
import org.hippoecm.hst.content.beans.manager.ObjectBeanManager;
import org.hippoecm.hst.content.beans.manager.ObjectBeanManagerImpl;
import org.hippoecm.hst.content.beans.manager.ObjectConverter;
import org.hippoecm.hst.content.beans.query.HstQueryManager;
import org.hippoecm.hst.content.beans.query.HstQueryManagerImpl;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.jcr.RuntimeRepositoryException;
import org.hippoecm.hst.core.search.HstQueryManagerFactoryImpl;
import org.hippoecm.hst.site.content.ObjectConverterFactoryBean;
import org.hippoecm.hst.util.PathUtils;
import org.hippoecm.repository.util.DateTools;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import nl.openweb.hippo.exception.SetupTeardownException;
import nl.openweb.jcr.importer.JcrImporter;
import nl.openweb.jcr.importer.JsonImporter;
import nl.openweb.jcr.utils.NodeTypeUtils;


import static org.hippoecm.repository.api.HippoNodeType.HIPPO_PATHS;

/**
 * @author Ebrahim Aharpour
 * @since 11/2/2017
 */
public abstract class AbstractRepoTest extends SimpleHippoTest {

    private static final String SLASH = "/";
    protected Node rootNode;
    protected ObjectConverter objectConverter;
    protected ObjectBeanManager objectBeanManager;
    protected HstQueryManager hstQueryManager;

    @Override
    public void setup() {
        super.setup();
        try {
            importNodeStructure();
            setObjectConverter();
            setQueryManager();
        } catch (Exception e) {
            throw new SetupTeardownException(e);
        }
    }

    protected HippoBean getHippoBean(String path) {
        try {
            return (HippoBean) requestContext.getObjectBeanManager().getObject(path);
        } catch (ObjectBeanManagerException e) {
            throw new HstComponentException(e);
        }
    }

    protected void setContentBean(String path) {
        requestContext.setContentBean(getHippoBean(path));
    }

    /**
     * Set the SiteContentBasePath and the siteContentBaseBean on HstRequestContext
     * @param path absolute path. It must not be null or empty and it must start with a /
     */
    protected void setSiteContentBase(String path) {
        try {
            if (StringUtils.isBlank(path) || !path.startsWith("/")) {
                throw new IllegalArgumentException("Parameter path must be a String that starts with /");
            }
            HippoBean hippoBean = (HippoBean) requestContext.getObjectBeanManager().getObject(path);
            // here it must be relative to root
            requestContext.setSiteContentBasePath(path.substring(1));
            requestContext.setSiteContentBaseBean(hippoBean);
        } catch (ObjectBeanManagerException e) {
            throw new HstComponentException(e);
        }
    }

    protected String getFileFormatByPath(String path) {
        if (path != null && path.contains(".")) {
            String fileExtension = path.substring(path.lastIndexOf(".") + 1).toLowerCase();
            return fileExtension;
        }
        return null;
    }

    abstract JcrImporter getImporter(String fileFormat);

    protected abstract String getAnnotatedClassesResourcePath();

    private void importNodeStructure() throws RepositoryException {
        String pathToResource = getPathToTestResource();
        if (pathToResource != null) {
            String fileFormat = getFileFormatByPath(pathToResource);
            JcrImporter importer = getImporter(fileFormat);
            if (importer != null) {
                this.rootNode = importer.createNodes(getResourceAsStream(pathToResource));
            } else {
                throw new IllegalStateException("Failed to get an importer for fileFormat " + fileFormat);
            }
        } else {
            JcrImporter importer = getImporter(JsonImporter.FORMAT);
            this.rootNode = importer.createNodes("{}");
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

    protected void recalculateHippoPaths() {
        recalculateHippoPaths(true);
    }

    protected void recalculateHippoPaths(boolean save) {
        recalculateHippoPaths("/content", save);
    }

    protected void recalculateHippoPaths(String absolutePath, boolean save) {
        try {
            validateAbsolutePath(absolutePath);
            Node node = rootNode.getNode(absolutePath.substring(1));
            calculateHippoPaths(node, getPathsForNode(node));
            if (save) {
                rootNode.getSession().save();
            }
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    protected void printNodeStructure() {
        printNodeStructure("/");
    }

    protected void printNodeStructure(String absolutePath) {
        printNodeStructure(absolutePath, System.out);
    }

    protected void printNodeStructure(String absolutePath, PrintStream printStream) {
        try {
            validateAbsolutePath(absolutePath);
            Node node;
            if (SLASH.equals(absolutePath)) {
                node = rootNode;
            } else {
                node = rootNode.getNode(absolutePath.substring(1));
            }
            printStream.println(node.getName());
            printSubNodes(printStream, node, "");
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    private void printSubNodes(PrintStream ps, Node node, String prefix) throws RepositoryException {
        for (NodeIterator iterator = node.getNodes(); iterator.hasNext(); ) {
            Node subnode = iterator.nextNode();
            ps.println(prefix + "   |_" + subnode.getName());
            if (iterator.hasNext()) {
                printSubNodes(ps, subnode, prefix + "   |");
            } else {
                printSubNodes(ps, subnode, prefix + "    ");
            }
        }
    }

    private void validateAbsolutePath(String absolutePath) {
        if (!absolutePath.startsWith(SLASH)) {
            throw new IllegalArgumentException("The path is not absolute.");
        }
    }

    private LinkedList<String> getPathsForNode(Node node) throws RepositoryException {
        LinkedList<String> paths = new LinkedList<>();
        Node parentNode = node;
        do {
            parentNode = parentNode.getParent();
            paths.add(parentNode.getIdentifier());
        } while (!parentNode.isSame(rootNode));
        return paths;
    }

    @SuppressWarnings("unchecked")
    private void calculateHippoPaths(Node node, LinkedList<String> paths) throws RepositoryException {
        paths.add(0, node.getIdentifier());
        setHippoPath(node, paths);
        for (NodeIterator nodes = node.getNodes(); nodes.hasNext(); ) {
            Node subnode = nodes.nextNode();
            if (!subnode.isNodeType("hippo:handle")) {
                if (!subnode.isNodeType("hippotranslation:translations")) {
                    calculateHippoPaths(subnode, (LinkedList<String>) paths.clone());
                }
            } else {
                setHandleHippoPaths(subnode, (LinkedList<String>) paths.clone());
            }
        }


    }

    private void setHippoPath(Node node, LinkedList<String> paths) throws RepositoryException {
        node.setProperty(HIPPO_PATHS, paths.toArray(new String[paths.size()]));
    }

    private void setHandleHippoPaths(Node handle, LinkedList<String> paths) throws RepositoryException {
        paths.add(0, handle.getIdentifier());
        for (NodeIterator nodes = handle.getNodes(handle.getName()); nodes.hasNext(); ) {
            Node subnode = nodes.nextNode();
            paths.add(0, subnode.getIdentifier());
            setHippoPath(subnode, paths);
            paths.remove(0);
        }
    }
}
