package dk.magenta.rm.scripts;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.*;

import static org.alfresco.model.ContentModel.*;
import static dk.magenta.rm.scripts.PresetGlobal.*;

public class PostPreset extends DeclarativeWebScript {

    private ContentService contentService;
    private NodeService nodeService;
    private SearchService searchService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

        // Get the parameter "site"
        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        String siteName = templateArgs.get("site");
        String presetName = templateArgs.get("presetName");

        // Find current site
        StoreRef store = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        String siteQuery = "TYPE:\"cm:folder\" AND PATH:\"/app:company_home/st:sites/cm:" + siteName + "\"";
        ResultSet siteResults = searchService.query(store, SearchService.LANGUAGE_FTS_ALFRESCO, siteQuery);
        List<NodeRef> siteNodes = siteResults.getNodeRefs();
        NodeRef siteNode = siteNodes.get(0);

        //Find surf-config folder in current site
        String surfConfigQuery = "+TYPE:\"cm:folder\" AND +PARENT:\"" + siteNode + "\" AND + ASPECT:\"sys:hidden\"";
        ResultSet surfConfigResults = searchService.query(store, SearchService.LANGUAGE_FTS_ALFRESCO, surfConfigQuery);
        List<NodeRef> surfConfigNodes = surfConfigResults.getNodeRefs();
        NodeRef surfConfigNode = surfConfigNodes.get(0);

        if (surfConfigNode != null) try {

            List<String> pagePaths = new ArrayList<>();
            // Get Components and Pages folders from surf-config folder
            List<ChildAssociationRef> surfConfigChildren = nodeService.getChildAssocs(surfConfigNode);
            NodeRef componentsNode = null;
            NodeRef pagesNode = null;
            for (ChildAssociationRef surfConfigChild : surfConfigChildren) {
                NodeRef childRef = surfConfigChild.getChildRef();
                if (nodeService.getProperty(childRef, PROP_NAME).equals("components"))
                    componentsNode = childRef;
                else if (nodeService.getProperty(childRef, PROP_NAME).equals("pages"))
                    pagesNode = childRef;
            }

            // Create DocumentBuilder
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

            // Create xml structure for presets.xml
            Document presetDoc = docBuilder.newDocument();
            Element presetsElement = presetDoc.createElement("presets");
            Element presetElement = presetDoc.createElement("preset");
            String presetId = presetName.replace(' ', '-');
            presetId = presetId.toLowerCase();
            presetElement.setAttribute("id", presetId);
            Element componentsElement = presetDoc.createElement("components");
            presetDoc.appendChild(presetsElement);
            presetsElement.appendChild(presetElement);
            presetElement.appendChild(componentsElement);

            if (componentsNode != null) {
                // Get components xml files
                List<ChildAssociationRef> componentsChildren = nodeService.getChildAssocs(componentsNode);

                for (ChildAssociationRef componentChild : componentsChildren) {
                    // Get input stream from component xml file
                    NodeRef componentNode = componentChild.getChildRef();
                    ContentReader contentReader = contentService.getReader(componentNode, ContentModel.PROP_CONTENT);
                    InputStream componentsInputStream = contentReader.getContentInputStream();

                    // Read components xml file
                    Document componentDoc = docBuilder.parse(componentsInputStream);

                    // Get the component node and remove guid node
                    Node component = componentDoc.getDocumentElement();
                    for(int i=0; i<component.getChildNodes().getLength(); i++)
                        if(component.getChildNodes().item(i).getNodeName().equals("guid")) {
                            component.removeChild(component.getChildNodes().item(i));
                            component.removeChild(component.getChildNodes().item(i++));
                        }
                        else if(component.getChildNodes().item(i).getNodeName().equals("source-id"))
                        {
                            String sourceIdText = component.getChildNodes().item(i).getTextContent();
                            if(!pagePaths.contains(sourceIdText))
                                pagePaths.add(sourceIdText);
                            String changedSourceIdText = sourceIdText.replace(siteName, "${siteid}");
                            component.getChildNodes().item(i).setTextContent(changedSourceIdText);
                        }

                    // Imports the node to presetDoc is needed to append it
                    Node importedNode = presetDoc.importNode(component, true);

                    // Append node from components file to presets.xml
                    componentsElement.appendChild(importedNode);

                    // Close input stream
                    componentsInputStream.close();
                }
            }
            if (pagesNode != null) {

                Element pagesElement = presetDoc.createElement("pages");
                presetElement.appendChild(pagesElement);

                // Get pages xml files IDEA: iterates through source-ids to find relevant files to copy
                HashMap<NodeRef, String> pageNodes = new HashMap<NodeRef, String>();
                for (String path : pagePaths) {
                    NodeRef pageNode = getPages(pagesNode, path);
                    pageNodes.put(pageNode, path);
                }
                Set<NodeRef> nodeRefs = pageNodes.keySet();
                for(NodeRef pageNode : nodeRefs){
                    ContentReader contentReader = contentService.getReader(pageNode, ContentModel.PROP_CONTENT);
                    InputStream pagesInputStream = contentReader.getContentInputStream();

                    // Read pages xml file
                    Document pageDoc = docBuilder.parse(pagesInputStream);

                    // Get the page node
                    Element page = pageDoc.getDocumentElement();

                    // Add id attribute
                    String changedIdText = pageNodes.get(pageNode).replace(siteName, "${siteid}");
                    page.setAttribute("id", changedIdText);

                    // Imports the node to presetDoc: is needed to append it
                    Node importedNode = presetDoc.importNode(page, true);

                    // Append node from components file to presets.xml
                    pagesElement.appendChild(importedNode);

                    // Close input stream
                    pagesInputStream.close();
                }
            }

            // Find Extension Presets folder
            String presetDirectoryQuery = DATA_DICTIONARY_QUERY_PATH + "/" + EXTENSION_FOLDER_ID + "\"";
            ResultSet presetDirectoryResults = searchService.query(store, SearchService.LANGUAGE_FTS_ALFRESCO, presetDirectoryQuery);
            //Create Extension Presets folder if none exists
            NodeRef presetDirectoryNode = null;
            if(presetDirectoryResults.length() == 0) {

                // Get NodeRef for Data Dictionary folder
                String dataDictionaryQuery = DATA_DICTIONARY_QUERY_PATH + "\"";
                ResultSet dataDictionaryResults = searchService.query(store, SearchService.LANGUAGE_FTS_ALFRESCO, dataDictionaryQuery);
                List<NodeRef> dataDictionaryNodes = dataDictionaryResults.getNodeRefs();
                NodeRef dataDictionaryNode = dataDictionaryNodes.get(0);

                // Create Extension Presets Folder
                Map<QName,Serializable> properties = new HashMap<QName,Serializable>();
                properties.put(PROP_NAME, EXTENSION_FOLDER_NAME);
                presetDirectoryNode = nodeService.createNode(dataDictionaryNode, ASSOC_CONTAINS, EXTENSION_FOLDER_ID_QNAME, TYPE_FOLDER, properties).getChildRef();
            }
            else {
                // Get NodeRef for Extension Presets folder
                List<NodeRef> presetDirectoryNodes = presetDirectoryResults.getNodeRefs();
                presetDirectoryNode = presetDirectoryNodes.get(0);
            }

            //Create new preset file
            OutputStream presetInputStream = createNewPresetFile(presetDirectoryNode, presetName);

            // Write to new preset file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            StreamResult result = new StreamResult(presetInputStream);
            DOMSource source = new DOMSource(presetDoc);
            transformer.transform(source, result);
            presetInputStream.close();

        } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
            e.printStackTrace();
        }

        // Respond with success
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("Status", "Success");
        return model;
    }

    private OutputStream createNewPresetFile(NodeRef parent, String presetName)
    {
        String fileName = presetName + ".xml";
        Map<QName,Serializable> properties = new HashMap<QName,Serializable>();
        properties.put(PROP_NAME, fileName);
        NodeRef presetNode = nodeService.createNode(parent, ASSOC_CONTAINS, QName.createQName("cm:" + fileName), TYPE_CONTENT, properties).getChildRef();
        ContentWriter contentWriter = contentService.getWriter(presetNode, ContentModel.PROP_CONTENT, true);
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_XML);
        return contentWriter.getContentOutputStream();
    }

    private NodeRef getPages(NodeRef parentNode, String path) {
        // Iterate through the path until the xml file is reached
        List<ChildAssociationRef> children = nodeService.getChildAssocs(parentNode);
        for (ChildAssociationRef child : children) {
            NodeRef childNode = child.getChildRef();
            String folderName = path.split("/")[0];
            if (nodeService.getProperty(childNode, PROP_NAME).equals(folderName)) {
                if (folderName.endsWith(".xml"))
                    return childNode;
                String newPath = path.substring(folderName.length() + 1);
                if (!newPath.contains("/"))
                    newPath += ".xml";
                return getPages(childNode, newPath);
            }
        }
        return null;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }
}