package nl.openweb.hippo.test.domain;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoDocument;

/**
 * @author Ebrahim Aharpour
 * @since 9/9/2017
 */
@Node(jcrType = "ns:NewsPage")
public class NewsPage extends HippoDocument {

    public String getTitle() {
        return getProperty("ns:title");
    }

    public String getIntroduction() {
        return getProperty("ns:introduction");
    }

    public String getSubjecttags() {
        return getProperty("ns:subjecttags");
    }

    public String getBrowserTitle() {
        return getProperty("ns:browserTitle");
    }

}
