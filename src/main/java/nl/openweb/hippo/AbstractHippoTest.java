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
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.hst.core.search.HstQueryManagerFactoryImpl;
import org.hippoecm.hst.mock.core.component.MockHstRequest;
import org.hippoecm.hst.mock.core.component.MockHstResponse;
import org.hippoecm.hst.mock.core.request.MockHstRequestContext;
import org.hippoecm.hst.mock.core.request.MockResolvedSiteMapItem;
import org.hippoecm.hst.site.content.ObjectConverterFactoryBean;
import org.hippoecm.hst.util.PathUtils;
import org.hippoecm.repository.util.DateTools;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import nl.openweb.hippo.exception.SetupTeardownException;
import nl.openweb.hippo.mock.MockMount;
import nl.openweb.hippo.mock.MockResolvedMount;
import nl.openweb.jcr.Importer;

/**
 * @author Ebrahim Aharpour
 * @since 8/20/2017
 */
public abstract class AbstractHippoTest {
    protected MockHstResponse response = new MockHstResponse();
    protected Node rootNode;
    protected MockHstRequest request = new MockHstRequest();
    protected MockHstRequestContext requestContext = new MockHstRequestContext();
    protected ObjectConverter objectConverter;
    protected ObjectBeanManager objectBeanManager;
    protected HstQueryManagerImpl hstQueryManager;
    protected MockResolvedSiteMapItem resolvedSiteMapItem;
    protected MockResolvedMount resolvedMount;
    protected MockMount mockMount;

    public void setup() {
        try {
            importNodeStructure(getImporter());
            setRequestContextProvider();
            setObjectConverter();
            setQueryManager();
            setResolvedSiteMapItem();
            setMount();
            request.setRequestContext(requestContext);
        } catch (Exception e) {
            throw new SetupTeardownException(e);
        }
    }

    protected abstract Importer getImporter();

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


    protected abstract String getPathToTestResource();

    protected abstract String getAnnotatedClassesResourcePath();

    protected void setContentBean(String path) throws ObjectBeanManagerException {
        HippoBean hippoBean = (HippoBean) requestContext.getObjectBeanManager().getObject(path);
        requestContext.setContentBean(hippoBean);
    }

    protected void setChannelInfo(ChannelInfo channelInfo) {
        this.mockMount.setChannelInfo(channelInfo);
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

    protected InputStream getResourceAsStream(String pathToResource) {
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

    public void teardown() {
        try {
            clearRequestContextProvider();
        } catch (Exception e) {
            throw new SetupTeardownException(e);
        }
    }


}
