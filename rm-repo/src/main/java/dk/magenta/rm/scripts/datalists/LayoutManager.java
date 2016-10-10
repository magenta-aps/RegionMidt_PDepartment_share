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

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.dictionary.*;
import org.alfresco.service.cmr.repository.NodeService;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.*;

import org.alfresco.repo.dictionary.CustomModelServiceImpl;

import java.io.*;

import java.util.*;


public class LayoutManager extends AbstractWebScript {


    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private CustomModelServiceImpl customModelService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setCustomModelService(CustomModelServiceImpl customModelService) {
        this.customModelService = customModelService;
    }


     private org.json.simple.JSONArray getCustomTypes() {

         PagingResults<CustomModelDefinition> modelDefinitionPagingResults = customModelService.getCustomModels(new PagingRequest(100));

         Iterator it = modelDefinitionPagingResults.getPage().iterator();

         while (it.hasNext()) {

             CustomModelDefinition cmd = (CustomModelDefinition)it.next();

         }

         PagingResults<TypeDefinition> customModelServiceAllCustomTypes = customModelService.getAllCustomTypes(new PagingRequest(100));

         org.json.simple.JSONArray result = new org.json.simple.JSONArray();


         it = customModelServiceAllCustomTypes.getPage().iterator();

         while (it.hasNext()) {

             JSONObject model = new JSONObject();
             TypeDefinition cmd = (TypeDefinition)it.next();

             try {
                 model.put("model", cmd.getModel().getName() );
                 model.put("type", cmd.getName());

                 result.add(model);

             } catch (JSONException e) {
                 e.printStackTrace();
             }
         }

         return result;
    }


    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {

        Map<String, Object> model = new HashMap<>();

        org.json.simple.JSONArray result = this.getCustomTypes();

        model.put("hej", "The Layout of all custom datalists has been reloaded ");

        try {
            result.writeJSONString(webScriptResponse.getWriter());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
