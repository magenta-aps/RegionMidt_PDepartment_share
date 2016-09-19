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

import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.springframework.extensions.webscripts.*;

import java.io.*;
import java.util.*;





import org.jdom2.*;

public class LayoutManager extends DeclarativeWebScript {


    private NodeService nodeService;
    private PersonService personService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
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

            List<Element> children = root.getChildren();

            Iterator i = children.iterator();

            while (i.hasNext()) {
                Element e = (Element)i.next();

                if ((e.getAttributeValue("id") != null ) && e.getAttributeValue("id").equals("MagentaDataList")) {
                    System.out.println(e.getAttributeValue("id"));
                }
            }



            Element compte = new Element("config");
            compte.setAttribute(new Attribute("idCompte", "ffsd"));
//            compte.addContent(new Element("numCompte").setText(this.idCompte));
//            compte.addContent(new Element("nom").setText(this.nom));
//            compte.addContent(new Element("solde").setText(this.solde));

            root.addContent(compte);
            document.setContent(root);

//            FileWriter writer = new FileWriter("test.xml");
//            XMLOutputter outputter = new XMLOutputter();
//            outputter.output(document, writer);
//            outputter.output(document, System.out);
//            writer.close(); // close writer

        } catch (IOException io) {
            System.out.println(io.getMessage());
        } catch (JDOMException e) {
            e.printStackTrace();
        }




    }



    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        // Set up return model
        Map<String, Object> model = new HashMap<>();

//        System.out.println("hej1");

        this.test();

        model.put("hej", "hej");

        return model;


    }


}

// create
//http://localhost:8080/alfresco/service/notifications?userName=fhp&message=duerdum&subject=hilsen&method=add&NODE_ID=3570b61b-a861-4a75-8a27-7b16393027cd&STORE_TYPE=workspace&STORE_ID=SpacesStore


// setRead
//http://localhost:8080/alfresco/service/notifications?method=setRead&NODE_ID=76e15607-5519-4ad6-915c-1c07086535f2&STORE_TYPE=workspace&STORE_ID=SpacesStore

//http://178.62.194.129:8080/alfresco/service/notifications?method=setRead&NODE_ID=/f1115ab8-bf2f-408c-b5ee-72acfb14be4c&STORE_TYPE=workspace&STORE_ID=SpacesStore




