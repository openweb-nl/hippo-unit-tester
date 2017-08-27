package nl.openweb.hippo;

import javax.jcr.Session;

import org.apache.sling.testing.mock.jcr.MockJcr;

import nl.openweb.jcr.Importer;

/**
 * @author Ebrahim Aharpour
 * @since 8/27/2017
 */
public abstract class SlingBaseHippoTest extends AbstractHippoTest {

    protected Importer getImporter() {
        try {
            return new Importer.Builder(() -> {
                Session session = MockJcr.newSession();
                return session.getRootNode();
            }).addMixins(false).build();
        } catch (Exception e) {
            throw new SetupTeardownException(e);
        }

    }
}
