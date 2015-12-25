/*
 * Copyright (c) 2015 Christian Chevalley
 * This file is part of Project Ethercis
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

package com.ethercis.vehr.parser;

import com.ethercis.logonservice.session.I_SessionManager;
import com.ethercis.servicemanager.cluster.RunTimeSingleton;
import com.ethercis.servicemanager.exceptions.ServiceManagerException;
import com.ethercis.servicemanager.runlevel.I_ServiceRunMode;
import junit.framework.TestCase;
import org.apache.commons.collections4.iterators.IteratorEnumeration;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by christian on 12/8/2015.
 */
public class EhrScapeURIParserTest extends TestCase {
    protected HttpClient client;
    protected RunTimeSingleton global;
    String hostname = "ab.cd.com";
    I_URIParser uriParser;

    @Before
    public void setUp() throws Exception {
        global = RunTimeSingleton.instance();
        global.getProperty().set(I_ServiceRunMode.SERVER_DIALECT_PARAMETER, I_ServiceRunMode.DialectSpace.EHRSCAPE.toString());
        global.getProperty().set(I_ServiceRunMode.SERVER_DIALECT_ROOT, "/rest/v1");
        uriParser = new EhrScapeURIParser(global);
    }

    @Test
    public void testEhrQueryParser() throws ServiceManagerException {
//        Request request = client.newRequest("http://" + hostname + ":8080/rest/v1/ehr?subjectId=1234&subjectNamespace=ABCDEF");
//        request.method(HttpMethod.POST);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/rest/v1/ehr");
        Map<String, String[]> parameters = new HashMap<String, String[]>();
        parameters.put("subjectId", new String[]{"1234"});
        parameters.put("subjectNamespace", new String[]{"ABCDEF"});
        when(request.getParameterMap()).thenReturn(parameters);
        Map<String, String[]> headers = new HashMap<String, String[]>();
        headers.put("Content-Type", new String[]{"application/json"});
        when(request.getHeaderNames()).thenReturn(new IteratorEnumeration<String>(headers.keySet().iterator()));
        when(request.getHeader("Content-Type")).thenReturn("application/json");
        when(request.getMethod()).thenReturn("POST");
        uriParser.parse(request);
        assertEquals("POST", uriParser.identifyMethod().toUpperCase());
        assertEquals("1234", uriParser.identifyParametersAsProperties().getClientProperty("subjectId").toString());
        assertEquals("ABCDEF", uriParser.identifyParametersAsProperties().getClientProperty("subjectNamespace").toString());

        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/rest/v1/ehr/8fd2bea0-9e0e-11e5-8994-feff819cdc9f");
        parameters = new HashMap<>();
        when(request.getParameterMap()).thenReturn(parameters);
        headers = new HashMap<>();
        headers.put("Content-Type", new String[]{"application/json"});
        when(request.getHeaderNames()).thenReturn(new IteratorEnumeration<String>(headers.keySet().iterator()));
        when(request.getHeader("Content-Type")).thenReturn("application/json");
        when(request.getMethod()).thenReturn("GET");
        uriParser.parse(request);
        assertEquals("GET", uriParser.identifyMethod().toUpperCase());
        assertEquals("8fd2bea0-9e0e-11e5-8994-feff819cdc9f", uriParser.identifyParametersAsProperties().getClientProperty("ehrId").toString());

        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/rest/v1/ehr");
        parameters = new HashMap<>();
        parameters.put("subjectId", new String[]{"1234"});
        parameters.put("subjectNamespace", new String[]{"ABCDEF"});
        when(request.getParameterMap()).thenReturn(parameters);
        headers = new HashMap<>();
        headers.put("Content-Type", new String[]{"application/json"});
        when(request.getHeaderNames()).thenReturn(new IteratorEnumeration<String>(headers.keySet().iterator()));
        when(request.getHeader("Content-Type")).thenReturn("application/json");
        when(request.getMethod()).thenReturn("GET");
        uriParser.parse(request);
        assertEquals("GET", uriParser.identifyMethod().toUpperCase());
        assertEquals("rest/v1/ehr/status", uriParser.identifyPath());
        assertEquals("1234", uriParser.identifyParametersAsProperties().getClientProperty("subjectId").toString());
        assertEquals("ABCDEF", uriParser.identifyParametersAsProperties().getClientProperty("subjectNamespace").toString());

        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/rest/v1/ehr/status/8fd2bea0-9e0e-11e5-8994-feff819cdc9f");
        parameters = new HashMap<>();
        when(request.getParameterMap()).thenReturn(parameters);
        headers = new HashMap<>();
        headers.put("Content-Type", new String[]{"application/json"});
        when(request.getHeaderNames()).thenReturn(new IteratorEnumeration<String>(headers.keySet().iterator()));
        when(request.getHeader("Content-Type")).thenReturn("application/json");
        when(request.getMethod()).thenReturn("PUT");
        uriParser.parse(request);
        assertEquals("PUT", uriParser.identifyMethod().toUpperCase());
        assertEquals("rest/v1/ehr/status", uriParser.identifyPath());
        assertEquals("8fd2bea0-9e0e-11e5-8994-feff819cdc9f", uriParser.identifyParametersAsProperties().getClientProperty("ehrId").toString());
    }

    @Test
    public void testCompositionQueryParser() throws ServiceManagerException {
//        Request request = client.newRequest("http://" + hostname + ":8080/rest/v1/ehr?subjectId=1234&subjectNamespace=ABCDEF");
//        request.method(HttpMethod.POST);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/rest/v1/composition");
        Map<String, String[]> parameters = new HashMap<String, String[]>();
        parameters.put("format", new String[]{"RAW"});
        when(request.getParameterMap()).thenReturn(parameters);
        Map<String, String[]> headers = new HashMap<String, String[]>();
        headers.put("Content-Type", new String[]{"application/xml"});
        when(request.getHeaderNames()).thenReturn(new IteratorEnumeration<String>(headers.keySet().iterator()));
        when(request.getHeader("Content-Type")).thenReturn("application/xml");
        when(request.getMethod()).thenReturn("POST");
        uriParser.parse(request);
        assertEquals("POST", uriParser.identifyMethod().toUpperCase());
        assertEquals("XML", uriParser.identifyParametersAsProperties().getClientProperty("format").toString());

        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/rest/v1/composition/8fd2bea0-9e0e-11e5-8994-feff819cdc9f");
        parameters = new HashMap<>();
        parameters.put("format", new String[]{"RAW"});
        when(request.getParameterMap()).thenReturn(parameters);
        headers = new HashMap<>();
        headers.put("Accept", new String[]{"application/xml"});
        when(request.getHeaderNames()).thenReturn(new IteratorEnumeration<String>(headers.keySet().iterator()));
        when(request.getHeader("Accept")).thenReturn("application/xml");
        when(request.getMethod()).thenReturn("GET");
        uriParser.parse(request);
        assertEquals("GET", uriParser.identifyMethod().toUpperCase());
        assertEquals("8fd2bea0-9e0e-11e5-8994-feff819cdc9f", uriParser.identifyParametersAsProperties().getClientProperty("uid").toString());
        assertEquals("XML", uriParser.identifyParametersAsProperties().getClientProperty("format").toString());

        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/rest/v1/composition/8fd2bea0-9e0e-11e5-8994-feff819cdc9f");
        parameters = new HashMap<>();
        when(request.getParameterMap()).thenReturn(parameters);
        headers = new HashMap<>();
        headers.put("Content-Type", new String[]{"application/json"});
        when(request.getHeaderNames()).thenReturn(new IteratorEnumeration<String>(headers.keySet().iterator()));
        when(request.getHeader("Content-Type")).thenReturn("application/xml");
        when(request.getMethod()).thenReturn("DELETE");
        uriParser.parse(request);
        assertEquals("DELETE", uriParser.identifyMethod().toUpperCase());
        assertEquals("rest/v1/composition", uriParser.identifyPath());
        assertEquals("8fd2bea0-9e0e-11e5-8994-feff819cdc9f", uriParser.identifyParametersAsProperties().getClientProperty("uid").toString());


    }

}