package nl.openweb.hippo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.onehippo.cm.engine.JcrContentProcessor;
import org.onehippo.cm.model.definition.ActionType;
import org.onehippo.cm.model.impl.GroupImpl;
import org.onehippo.cm.model.impl.ModuleImpl;
import org.onehippo.cm.model.impl.ProjectImpl;
import org.onehippo.cm.model.impl.definition.ContentDefinitionImpl;
import org.onehippo.cm.model.parser.ContentSourceParser;
import org.onehippo.cm.model.source.ResourceInputProvider;
import org.onehippo.cm.model.source.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.openweb.jcr.utils.PathUtils;


/**
 * Use: YamlImporter.importPlainYaml(getResourceAsStream("testcontent.yaml"), rootNode, "/content/documents/mysite", "hippostd:folder");
 * Instead of overriding getPathToTestResource()
 */
public class YamlImporter {

    private static final Logger LOG = LoggerFactory.getLogger(YamlImporter.class);

    private static final JcrContentProcessor PROCESSOR = new JcrContentProcessor();

    public static void importPlainYaml(final InputStream inputStream, final Node parentNode, String path, String intermediateNodeType) throws RepositoryException {

        try {
            final ResourceInputProvider resourceInputProvider = new ResourceInputProvider() {
                @Override
                public boolean hasResource(final Source source, final String resourcePath) {
                    return false;
                }

                @Override
                public InputStream getResourceInputStream(final Source source, final String resourcePath) throws IOException {
                    throw new IOException("Plain YAML import does not support links to resources");
                }

            };

            Node node = createNode(parentNode, path, intermediateNodeType);
            final ModuleImpl module = new ModuleImpl("import-module", new ProjectImpl("import-project", new GroupImpl("import-group")));
            final ContentSourceParser sourceParser = new ContentSourceParser(resourceInputProvider);
            sourceParser.parse(inputStream, "/import", "console.yaml", module);
            final ContentDefinitionImpl contentDefinition = module.getContentSources().iterator().next().getContentDefinition();
            PROCESSOR.importNode(contentDefinition.getNode(), node, ActionType.RELOAD);
        } catch (Exception e) {
            throw new RuntimeException("Import failed", e);
        }
    }

    private static Node  createNode(final Node rootNode, final String path, final String intermediateNodeType) {
        Node result = rootNode;
        if (path != null && !path.isEmpty()) {
            String[] nodes = PathUtils.normalizePath(path).split("/");
            List<String> pathSegments = Arrays.asList(nodes);
            for(String segment: pathSegments){
                try {
                    if (result.hasNode(segment)) {
                        result = result.getNode(segment);
                    } else {
                        result = result.addNode(segment, intermediateNodeType);
                    }
                } catch (RepositoryException e) {
                    LOG.error("error while trying to create the node structure", e);
                }
            }
        }
        return result;
    }


}
