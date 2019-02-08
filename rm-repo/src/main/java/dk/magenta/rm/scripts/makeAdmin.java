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
import org.alfresco.service.ServiceRegistry;
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

import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
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

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    ServiceRegistry serviceRegistry;


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







    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {

        UserTransaction tx = null;
        try
        {
            tx = serviceRegistry.getTransactionService().getUserTransaction(false);
            tx.begin();
            String password = webScriptRequest.getParameter("password");
            String action = webScriptRequest.getParameter("action");

            String psw = gbproperties.getProperty("makeadminpassword");
            String usr = gbproperties.getProperty("makeadminuser");

            org.json.simple.JSONArray result = new org.json.simple.JSONArray();
            JSONObject model = new JSONObject();

            authenticationService.invalidateTicket(authenticationService.getCurrentTicket());


            try {
                if (password.equals(psw)) {

                    //authenticationService.authenticate(user, password.toCharArray());

                    AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

                    String adminGroup = authorityService.getName(AuthorityType.GROUP, "ALFRESCO_ADMINISTRATORS");

                    if (action.equals("add")) {
                        authorityService.addAuthority(adminGroup, usr);
                    }
                    else {
                        authorityService.removeAuthority(adminGroup, usr);
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
            // commit the transaction
            tx.commit();
        }
        catch (Throwable err)
        {
            try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
        }





    }
}
