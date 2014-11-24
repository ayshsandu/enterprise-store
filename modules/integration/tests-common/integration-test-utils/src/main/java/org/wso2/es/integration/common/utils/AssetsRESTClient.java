/*
 * Copyright (c) 2014, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.es.integration.common.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class AssetsRESTClient extends ESIntegrationTest {
    private URLConnection urlConn;
    private URL endpointUrl;

//    private DataOutputStream printout;
//    private DataInputStream input;

    private StringBuilder response = null;
    private String endpoint;
    private String baseURL = "https://localhost:9443";
    private String username = "admin";
    private String password = "admin";

    private JsonParser parser;
    private JsonElement elem;

    private static int defaultPageSize = 12;
    private static final Logger log = Logger.getLogger(AssetsRESTClient.class);

    /**
     * This methods make a call to ES-Publisher REST API and obtain a sessionID
     * @return SessionId for the authenticated user
     */
    private String getSessionID() {
        String sessionID = null;
        try {
            endpoint = baseURL + "/publisher/apis/authenticate?"; //authenticate endpoint
            endpointUrl = new URL(endpoint);
            urlConn = endpointUrl.openConnection();
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);
            // Specify the content type.
            urlConn.setRequestProperty
                    ("Content-Type", "application/x-www-form-urlencoded");
            // Send POST output.
            DataOutputStream printout = new DataOutputStream(urlConn.getOutputStream());
            String content =
                    "username=" + URLEncoder.encode(username) +
                            "&password=" + URLEncoder.encode(password);
            printout.writeBytes(content);
            printout.flush();
            printout.close();
            // Get response data.
            DataInputStream input = new DataInputStream(urlConn.getInputStream());
            String str;
            response = new StringBuilder();
            while (null != ((str = input.readLine()))) {
                response.append(str);
            }
            parser = new JsonParser();
            elem = parser.parse(response.toString());
            sessionID = elem.getAsJsonObject().getAsJsonObject("data").get("sessionId").toString();
            input.close();
        } catch (MalformedURLException e) {
            log.error("MalformedURLException Error while login ", e);
        } catch (IOException e) {
            log.error("IOException Error while login ", e);
        } catch (Exception e) {
            log.error("Error while login ", e);
        }
        return sessionID;
    }

    private JsonArray getData(String sessionId) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("###### Get Assets via REST endpoint ######");
            }
            endpoint = baseURL + "/publisher/apis/assets?type=gadget";//endpoint list assets
            endpointUrl = new URL(endpoint);
            urlConn = endpointUrl.openConnection();
            urlConn.setRequestProperty("Cookie", "JSESSIONID=" + sessionId + ";");//send
            // SessionId Cookie
            urlConn.connect();
            //GET response data
            DataInputStream input = new DataInputStream(urlConn.getInputStream());
            response = new StringBuilder();
            String str;
            while (null != ((str = input.readLine()))) {
                response.append(str);
            }
            input.close();
            parser = new JsonParser();
            elem = parser.parse(response.toString());
            // parse response to a JasonArray
            return elem.getAsJsonObject().getAsJsonArray("data");

        } catch (MalformedURLException e) {
            log.error("MalformedURLException Error while retrieving assets ", e);
        } catch (IOException e) {
            log.error("IOException Error while retrieving assets ", e);
        } catch (Exception e) {
            log.error("Error while retrieving assets ", e);
        }
        return null;
    }

    private void logOut(String sessionId) {
        try {
            endpoint = baseURL + "/publisher/apis/logout"; //authenticate endpoint
            endpointUrl = new URL(endpoint);
            urlConn = endpointUrl.openConnection();
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);
            // Specify the content type.
            urlConn.setRequestProperty("Cookie", "JSESSIONID=" + sessionId + ";");
            //send SessionId Cookie
            // Send POST output.
            DataOutputStream printout = new DataOutputStream(urlConn.getOutputStream());

            printout.flush();
            printout.close();
            // Get response data.
//            input = new DataInputStream(urlConn.getInputStream());
//            String str;
//            input.close();
        } catch (MalformedURLException e) {
            log.error("MalformedURLException Error while logout ", e);
        } catch (IOException e) {
            log.error("IOException Error while logout ", e);
        } catch (Exception e) {
            log.error("Error while logout ", e);
        }
    }

    public boolean isIndexCompleted() {
        String sessionId = getSessionID();
        JsonArray assets = getData(sessionId);
        logOut(sessionId);
        return assets.size() == defaultPageSize;
    }
}
