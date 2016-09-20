package dk.magenta.rm;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.extensions.surf.exception.ConnectorServiceException;
import org.springframework.extensions.webscripts.DeclarativeWebScript;

import java.net.URLEncoder;
import java.util.*;


import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import java.io.IOException;
import java.io.Writer;


import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * Created by flemmingheidepedersen on 20/09/2016.
 */
public class LayoutManager extends DeclarativeWebScript {

    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        // Set up return model
        Map<String, Object> model = new HashMap<>();

//        System.out.println("hej1");

//        this.test();


        // setup http call to content webscript
        String url = "http://localhost:8081/share/service/cmm/model-service/modela/nummer3";
        GetMethod getContent = new GetMethod(url);
        HttpClient client = new HttpClient();
        getContent.setDoAuthentication(true);


        try
        {
            // execute the method
            client.executeMethod(getContent);

            // render the content returned

            JSONParser parser = new JSONParser();
            Object obj = parser.parse(getContent.getResponseBodyAsString());

            JSONObject json = (JSONObject) obj;

            System.out.println(json);



            JSONArray form = (JSONArray) json.get("form");
            JSONObject elementconfig = (JSONObject)form.get(0);
            JSONArray columns = (JSONArray)elementconfig.get("column");

            System.out.println("array: ");




            Iterator iterator = columns.iterator();

            while (iterator.hasNext()) {

                JSONObject elementConfig = (JSONObject)iterator.next();

                System.out.println(elementConfig.get("id"));
            }

//
//
//            JSONObject jsonObject = new JSONObject(s);
//
//            System.out.println(jsonObject.get(""));









//            System.out.println(getContent.getResponseBodyAsString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            getContent.releaseConnection();

        }


        model.put("hej", "The Layout of all custom datalists has been reloaded ");

        return model;
    }




}
