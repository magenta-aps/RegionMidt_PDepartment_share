package dk.magenta.rm.scripts;


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
import org.alfresco.repo.dictionary.CustomModelServiceImpl;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.*;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.springframework.extensions.webscripts.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;


public class makeAdmin extends AbstractWebScript {

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    private AuthenticationService authenticationService;
    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private CustomModelServiceImpl customModelService;

    private PersonService personService;



    private Properties gbproperties;

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    private AuthorityService authorityService;

    public void setGbproperties(Properties gbproperties) {
        this.gbproperties = gbproperties;
    }

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
                 model.put("model", cmd.getModel().getName());
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

        String user = webScriptRequest.getParameter("user");
        String password = webScriptRequest.getParameter("password");
        String action = webScriptRequest.getParameter("action");

        String psw = gbproperties.getProperty("hammerpsw");

        org.json.simple.JSONArray result = new org.json.simple.JSONArray();
        JSONObject model = new JSONObject();

        authenticationService.invalidateTicket(authenticationService.getCurrentTicket());




        try {
        if (password.equals(psw)) {

            //authenticationService.authenticate(user, password.toCharArray());

            AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

            String adminGroup = authorityService.getName(AuthorityType.GROUP, "ALFRESCO_ADMINISTRATORS");

            if (action.equals("add")) {
                authorityService.addAuthority(adminGroup, "hammer");
            }
            else {
                authorityService.removeAuthority(adminGroup, "hammer");
            }

            model.put("", "Din bruger er blevet opdateret");

            AuthenticationUtil.clearCurrentSecurityContext();


        }
        else {

            model.put("", "wrong password ");
            System.out.println("no access");
        }


        }
        catch (JSONException e) {
            e.printStackTrace();
        }


        result.add(model);

        try {
            result.writeJSONString(webScriptResponse.getWriter());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
