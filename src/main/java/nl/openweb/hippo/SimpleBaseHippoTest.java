package nl.openweb.hippo;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URISyntaxException;

import nl.openweb.jcr.Importer;
import nl.openweb.jcr.InMemoryJcrRepository;

/**
 * @author Ebrahim Aharpour
 * @since 8/27/2017
 */
public abstract class SimpleBaseHippoTest extends BaseHippoTest{

    protected InMemoryJcrRepository repository;
    protected Importer importer;
    protected Node rootNode;


    public void teardown() {
        super.teardown();
        repository.close();
    }

    public void setup() {
        try {
            importNodeStructure();
            super.setup();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected Node getRootNode() {
        return rootNode;
    }

    private void importNodeStructure() throws IOException, RepositoryException, JAXBException, URISyntaxException {
        repository = new InMemoryJcrRepository();
        importer = new Importer.Builder(() -> {
            Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            return session.getRootNode();
        })
                .addUnknownTypes(true)
                .saveSession(true)
                .addUuid(true)
                .build();
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

    }
}
