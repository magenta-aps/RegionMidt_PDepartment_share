package dk.magenta.rm.scripts;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.dom4j.DocumentFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.extensions.surf.exception.PlatformRuntimeException;
import org.springframework.extensions.webscripts.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

import static dk.magenta.rm.scripts.PresetGlobal.*;
import static org.alfresco.model.ContentModel.ASSOC_CONTAINS;
import static org.alfresco.model.ContentModel.PROP_NAME;
import static org.alfresco.model.ContentModel.TYPE_FOLDER;

public class GetPresets extends DeclarativeWebScript {
    private ContentService contentService;
    private SearchService searchService;

    // Search for our custom preset XML files
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        // Create new presets document with root element
        DocumentFactory documentFactory = DocumentFactory.getInstance();
        SAXReader xmlReader = new SAXReader();
        xmlReader.setDocumentFactory(documentFactory);
        Document resultDoc = documentFactory.createDocument();
        Element presetsRootElement = documentFactory.createElement("presets");

        //Search extension preset folder for documents
        String presetDirectoryQuery = DATA_DICTIONARY_QUERY_PATH + "/" + EXTENSION_FOLDER_ID +"/*\"";
        StoreRef store = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        ResultSet presetDirectoryResults = searchService.query(store, SearchService.LANGUAGE_FTS_ALFRESCO, presetDirectoryQuery);
        List<NodeRef> presetDirectoryNodes = presetDirectoryResults.getNodeRefs();

        // For each store in search path find all files
        for (NodeRef presetNode : presetDirectoryNodes) {
            try {
                // Parse each file and then add the documents to root element of xml document
                ContentReader contentReader = contentService.getReader(presetNode, ContentModel.PROP_CONTENT);
                InputStream componentsInputStream = contentReader.getContentInputStream();
                Document doc = xmlReader.read(componentsInputStream);
                List<Element> presets = doc.getRootElement().elements();
                for (Element preset : presets)
                    presetsRootElement.add(preset.detach());
            } catch (DocumentException e) {
                throw new PlatformRuntimeException("Error processing presets XML file: " + presetNode, e);
            }
        }
        // Add root element to result preset file
        resultDoc.add(presetsRootElement);

        // Respond with result preset file
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("resultDoc", resultDoc.asXML());
        return model;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }
}