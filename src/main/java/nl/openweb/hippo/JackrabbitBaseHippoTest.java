package nl.openweb.hippo;

import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import nl.openweb.hippo.exception.SetupTeardownException;
import nl.openweb.jcr.Importer;
import nl.openweb.jcr.InMemoryJcrRepository;

/**
 * @author Ebrahim Aharpour
 * @since 8/27/2017
 */
public abstract class JackrabbitBaseHippoTest extends AbstractHippoTest {

    private InMemoryJcrRepository repository;

    @Override
    protected Importer getImporter() {
        try {
            repository = new InMemoryJcrRepository();
            return new Importer.Builder(() -> {
                Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
                return session.getRootNode();
            })
                    .addUnknownTypes(true)
                    .saveSession(true)
                    .addUuid(true)
                    .build();
        } catch (Exception e) {
            throw new SetupTeardownException(e);
        }
    }

    public void teardown() {
        super.teardown();
        repository.close();
    }
}
