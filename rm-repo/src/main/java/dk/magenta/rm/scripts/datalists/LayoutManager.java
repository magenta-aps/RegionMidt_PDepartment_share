package dk.magenta.rm.scripts.datalists;


/*
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.springframework.extensions.webscripts.*;

import org.alfresco.service.cmr.search.SearchService;

import org.alfresco.service.cmr.dictionary.DictionaryService;

import java.io.*;
import java.util.*;





import org.jdom2.*;

public class LayoutManager extends DeclarativeWebScript {


    private NodeService nodeService;
    private DictionaryService dictionaryService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }


    public void test() {



//   Læs layoutstruktur fra datalisten
//   Lav xml struktur for layout




//   indsæt xml struktur i share-config-custom
//  indsæt mellem <alfresco-config>
        // gem fil
        // bed brugeren om at genstarte share - det træder i kraft efter genstart af share

        Document d = new Document();


//        try {
//            Scanner in = new Scanner(new FileReader("/Users/flemmingheidepedersen/src/OpenDESK-REPO/share/target/test-classes/alfresco/web-extension/share-config-custom.xml"));
//
//            while (in.hasNext()) {
//                System.out.println(in.next());
//            }
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }



        try{

            Document document = null;
            Element root = null;
            File xmlFile = new File("/Users/flemmingheidepedersen/src/OpenDESK-REPO/share/target/share-war/WEB-INF/classes/alfresco/web-extension/share-config-custom.xml");

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


            root = this.removeLayouts(root, "ffsd");

//            Element compte = new Element("config");
//            compte.setAttribute(new Attribute("idCompte", "ffsd"));


//            compte.addContent(new Element("numCompte").setText(this.idCompte));
//            compte.addContent(new Element("nom").setText(this.nom));
//            compte.addContent(new Element("solde").setText(this.solde));
//
//            root.addContent(compte);
            document.setContent(root);

            FileWriter writer = new FileWriter("/Users/flemmingheidepedersen/src/OpenDESK-REPO/share/target/share-war/WEB-INF/classes/alfresco/web-extension/share-config-custom.xml");
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

     private void getCustomLayouts() {


         String OD_URI = "http://www.test.com/model/regionmidt/1.0";
         String OD_MDL = "modela.concarde.dk";
         String OD_PREFIX = "rm";

         QName TYPE_DATALISTS = QName.createQName(OD_URI, "datalistmodel");
         QName TYPE_MODELLA = QName.createQName(OD_MDL, "nummer3");

         Collection<QName> c = dictionaryService.getSubTypes(TYPE_DATALISTS, true);
         TypeDefinition t = dictionaryService.getType(TYPE_MODELLA);

         System.out.println(t.getModel().getName().getLocalName());


//         System.out.println(t);

         Iterator i = c.iterator();

         // du får namespace ud for modellen ( du skal kun bruge model navnet)  dette giver dig navnet:   System.out.println(t.getModel().getName().getLocalName());
         // og du får navnet på typen ud (den er god nok)

         while (i.hasNext()) {
//             System.out.println(i.next());
         }



//        // hent alle layouts som nedarver fra vores custom - som er blevet lavet i modellen

    // hent alle unikke objekter af typen rm:datalistmodel

    // for hver af dem, hent deres layout og transform det til xml - til brug i share-config-custom


    }
//
//    private void transformLayoutToXML(String layout) {
//
//    }
//
//
//    private void addCustomLayouts(String allCustomLayouts) {
//
//    }


    private Element removeLayouts(Element root, String layoutID) {

        List<Element> children = root.getChildren();

        Iterator i = children.iterator();

        ArrayList childrenToRemove = new ArrayList();

        while (i.hasNext()) {
            Element e = (Element)i.next();

            if ((e.getAttributeValue("id") != null ) && e.getAttributeValue("id").equals("MagentaDataList")) {
                System.out.println(e.getAttributeValue("id"));
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



    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        // Set up return model
        Map<String, Object> model = new HashMap<>();

//        System.out.println("hej1");

//        this.test();

        this.getCustomLayouts();

        model.put("hej", "The Layout of all custom datalists has been reloaded ");

        return model;


    }
}