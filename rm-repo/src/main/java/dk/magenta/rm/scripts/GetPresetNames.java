package dk.magenta.rm.scripts;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.springframework.extensions.webscripts.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.alfresco.model.ContentModel.*;
import static dk.magenta.rm.scripts.PresetGlobal.*;

public class GetPresetNames extends DeclarativeWebScript {
    private SearchService searchService;
    private NodeService nodeService;

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    // Gets a list of names of all custom presets
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        //Search extension preset folder for documents
        String presetDirectoryQuery = DATA_DICTIONARY_QUERY_PATH + "/" + EXTENSION_FOLDER_ID +"/*\"";
        StoreRef store = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        ResultSet presetDirectoryResults = searchService.query(store, SearchService.LANGUAGE_FTS_ALFRESCO, presetDirectoryQuery);
        List<NodeRef> presetDirectoryNodes = presetDirectoryResults.getNodeRefs();

        // Set up return model
        Map<String, Object> model = new HashMap<String, Object>();

        // For each preset in extension preset folder add name of file to a string separated with line break '\n'
        String presetNames = "";
        for(NodeRef presetNode : presetDirectoryNodes) {
            if(!presetNames.equals(""))
                presetNames += "\n";
            presetNames += nodeService.getProperty(presetNode, PROP_NAME).toString();
        }

        // Respond with the string containing the file names
        model.put("presetNames", presetNames);
        return model;
    }
}