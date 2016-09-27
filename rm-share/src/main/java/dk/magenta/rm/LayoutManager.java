package dk.magenta.rm;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.extensions.surf.exception.ConnectorServiceException;
import org.springframework.extensions.webscripts.*;

import java.io.*;
import java.net.URLEncoder;
import java.util.*;


import org.springframework.extensions.webscripts.DeclarativeWebScript;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;


import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import org.jdom2.*;
import org.springframework.extensions.webscripts.connector.ConnectorService;
import org.springframework.extensions.webscripts.connector.Response;

/**
 * Created by flemmingheidepedersen on 20/09/2016.
 */
public class LayoutManager extends DeclarativeWebScript {

    private ScriptRemote scriptRemote;
    public void setScriptRemote(ScriptRemote scriptRemote) {
        this.scriptRemote = scriptRemote;
    }

    private ConnectorService connectorService;
    public void setConnectorService(ConnectorService connectorService) {
        this.connectorService = connectorService;
    }



    private ArrayList<String> getColumnsForCustomType(String type, String host) {

        System.out.println("type");
        System.out.println(type);
        System.out.println("host");
        System.out.println(host);

        String url = host  + "/share/service/cmm/model-service/"  + type;
        GetMethod getContent = new GetMethod(url);
        HttpClient client = new HttpClient();
        getContent.setDoAuthentication(true);

        ArrayList<String> result = new ArrayList();


        try
        {
            client.executeMethod(getContent);

            JSONParser parser = new JSONParser();
            Object obj = parser.parse(getContent.getResponseBodyAsString());

            JSONObject json = (JSONObject) obj;

            JSONArray form = (JSONArray) json.get("form");

            if (form.size() > 0) {
                JSONObject elementconfig = (JSONObject)form.get(0);
                JSONArray columns = (JSONArray)elementconfig.get("column");

                Iterator iterator = columns.iterator();

                while (iterator.hasNext()) {
                    JSONObject elementConfig = (JSONObject)iterator.next();
                    result.add((String) elementConfig.get("id"));
                }
            }
            else {
                getContent.releaseConnection();
                System.out.println("no layout for type");
                result = null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            getContent.releaseConnection();
            System.out.println("no layout for type");
            result = null;

        }
        return result;
    }

    public Element createXML(ArrayList<String> properties, String type) {

        String condition = (String)properties.get(0).split(":")[0] + ":" + type;

        Element config = new Element("config");
        config.setAttribute("evaluator", "model-type");
        config.setAttribute("condition", condition);
        config.setAttribute("id", "custom_form_setup");

        Element forms = new Element("forms");

        Element form = new Element("form");

        Element field_visibility = new Element("field-visibility");
        Element create_form  = new Element("create-form");
        Element appearance  = new Element("appearance");

        create_form.setAttribute("template", "../data-lists/forms/dataitem.ftl");

        Iterator iterator = properties.iterator();


        while (iterator.hasNext()) {
            Element property = new Element("show");
            String property_value = (String) iterator.next();
            property.setAttribute("id", property_value);
            field_visibility.addContent(property);
        }

        form.addContent(field_visibility);
        form.addContent(create_form);
        form.addContent(appearance);
        forms.addContent(form);
        config.addContent(forms);

        return config;
    }



    public void addToShareConfigCustom(ArrayList<Element> xmlList) {

        Document d = new Document();

        String workingDir = System.getProperty("user.dir");

        String path = workingDir + "/target/test-classes/alfresco/web-extension/share-config-custom.xml";

        try{

            Document document = null;
            Element root = null;
            File xmlFile = new File(path);

            if(xmlFile.exists()){

                FileInputStream fis = new FileInputStream(xmlFile);
                SAXBuilder sb = new SAXBuilder();
                document = sb.build(fis);
                root = document.getRootElement();
                fis.close();
            }else{
                document = new Document();
                root = new Element("banque");
            }


            root = this.removeLayouts(root, "custom_form_setup");

            Iterator i = xmlList.iterator();
            while (i.hasNext()) {

                Element xml = (Element)i.next();
                root.addContent(xml);
            }

            document.setContent(root);

            FileWriter writer = new FileWriter(path);
            XMLOutputter outputter = new XMLOutputter();
            outputter.output(document, writer);
            outputter.output(document, System.out);
            writer.close(); // close writer

        } catch (IOException io) {
            System.out.println(io.getMessage());
        } catch (JDOMException e) {
            e.printStackTrace();
        }
    }

    private Element removeLayouts(Element root, String layoutID) {

        List<Element> children = root.getChildren();

        Iterator i = children.iterator();

        ArrayList childrenToRemove = new ArrayList();

        while (i.hasNext()) {
            Element e = (Element)i.next();

            if ((e.getAttributeValue("id") != null ) && e.getAttributeValue("id").equals("MagentaDataList")) {
            }

            if ((e.getAttributeValue("id") != null ) && e.getAttributeValue("id").equals(layoutID)) {
                // cant mess with the iterator at this point, so we have to pick it up to be removed after the iterator has finished
                childrenToRemove.add(e.getName());
            }
        }

        i = childrenToRemove.iterator();
        while (i.hasNext()) {
            root.removeChild((String)i.next());
        }

        return root;

    }

    private ArrayList<String> getCustomTypes() {

        String alfrescoEndPoint = null;
        try {
            alfrescoEndPoint = connectorService.getConnector("alfresco").getEndpoint();
        } catch (ConnectorServiceException e) {
            e.printStackTrace();
        }

        String uri = alfrescoEndPoint + "/layoutmanager";
        Response response = scriptRemote.connect().get(uri);


        ArrayList<String> result = new ArrayList();


        try {


            JSONParser parser = new JSONParser();
            Object obj = parser.parse(response.getText());

            System.out.println(obj);

            JSONArray list = (JSONArray) obj;

            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                JSONObject elementConfig = (JSONObject)iterator.next();


                result.add(((String)elementConfig.get("model")).replaceAll("\\{.*\\}", "") + "/" + ((String)elementConfig.get("type")).replaceAll("\\{.*\\}", ""));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();

        }
        return result;
    }



    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {


        System.out.println(("test"));

        Map<String, Object> model = new HashMap<>();

        ArrayList<String> customTypes = this.getCustomTypes();

        ArrayList<Element> xmlList = new ArrayList();

        Iterator i = customTypes.iterator();

        while (i.hasNext()) {

            String type = (String)i.next();

            ArrayList<String> properties = this.getColumnsForCustomType(type, req.getServerPath());


            System.out.println("properties");
            System.out.println(properties);

            if (properties != null) {
                Element xml = this.createXML(properties, type.split("/")[1]);
                xmlList.add(xml);

            }
        }



        model.put("hej", "The Layout of all custom datalists has been reloaded ");

        this.addToShareConfigCustom(xmlList);

        return model;
    }

}
