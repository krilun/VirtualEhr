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
//Copyright
package com.ethercis.ehrservice;

import com.ethercis.compositionservice.I_CompositionService;
import com.ethercis.dao.access.interfaces.I_EhrAccess;
import com.ethercis.dao.access.interfaces.I_PartyIdentifiedAccess;
import com.ethercis.dao.access.interfaces.I_SystemAccess;
import com.ethercis.logonservice.session.I_SessionManager;
import com.ethercis.persistence.ServiceDataCluster;
import com.ethercis.servicemanager.annotation.*;
import com.ethercis.servicemanager.cluster.I_Info;
import com.ethercis.servicemanager.cluster.RunTimeSingleton;
import com.ethercis.servicemanager.common.I_SessionClientProperties;
import com.ethercis.servicemanager.common.def.Constants;
import com.ethercis.servicemanager.common.def.SysErrorCode;
import com.ethercis.servicemanager.exceptions.ServiceManagerException;
import com.ethercis.servicemanager.runlevel.I_ServiceRunMode;
import com.ethercis.servicemanager.service.ServiceInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.log4j.Logger;
import org.openehr.rm.datatypes.basic.DvIdentifier;

import java.util.*;

/**
 * ETHERCIS Project VirtualEhr
 * Created by Christian Chevalley on 6/30/2015.
 */
@Service(id ="EhrService", version="1.0", system=true)

@RunLevelActions(value = {
        @RunLevelAction(onStartupRunlevel = 9, sequence = 4, action = "LOAD"),
        @RunLevelAction(onShutdownRunlevel = 9, sequence = 4, action = "STOP") })

public class EhrService extends ServiceDataCluster implements I_EhrService, EhrServiceMBean {

    final private String ME = "EhrService";
    final private String Version = "1.0";
    private Logger log = Logger.getLogger(EhrService.class);

    @Override
    public void doInit(RunTimeSingleton global, ServiceInfo serviceInfo)throws ServiceManagerException {
        super.doInit(global, serviceInfo);
        //get a resource service instance
        putObject(I_Info.JMX_PREFIX + ME, this);
        log.info("EhrService service started...");
    }

    @Override
    public UUID create(UUID partyId, UUID systemId) throws Exception {
        //check if an Ehr already exists for this party
        if (I_EhrAccess.checkExist(getDataAccess(), partyId))
            throw new ServiceManagerException(getGlobal(), SysErrorCode.USER_ILLEGALARGUMENT, "Specified party has already an EHR set (partyId="+partyId+")");

        I_EhrAccess ehrAccess = I_EhrAccess.getInstance(getDataAccess(), partyId, systemId, null, null);
        return ehrAccess.commit();

//        //retrieve an existing status
//
//        I_StatusAccess statusAccess = I_StatusAccess.retrieveInstance(getDataAccess(), partyId);
//
//        return I_EhrAccess.retrieveInstanceByStatus(getDataAccess(), statusAccess.getId()).getId();
    }

    @Override
    public UUID retrieve(String subjectId, String nameSpace){
        return I_EhrAccess.retrieveInstanceBySubject(getDataAccess(), subjectId, nameSpace);
    }

    @Override
    public Integer delete(UUID ehrId){
        I_EhrAccess ehrAccess = I_EhrAccess.retrieveInstance(getDataAccess(), ehrId);
        return ehrAccess.delete();
    }

    @QuerySetting(dialect = {
            @QuerySyntax(mode = I_ServiceRunMode.DialectSpace.STANDARD, httpMethod = "GET", method = "get", path = "vehr/ehr", responseType = ResponseType.Json),
            @QuerySyntax(mode = I_ServiceRunMode.DialectSpace.EHRSCAPE, httpMethod = "GET", method = "get", path = "rest/v1/ehr", responseType = ResponseType.Json)
    })
    public Object retrieve(I_SessionClientProperties props) throws ServiceManagerException {
        String subjectId = props.getClientProperty("subjectId", (String) null);
        String nameSpace = props.getClientProperty("subjectNamespace", (String)null);
        String sessionId = props.getClientProperty(I_SessionManager.SECRET_SESSION_ID_INTERNAL, (String)null);

        if (subjectId == null || nameSpace == null)
            throw new ServiceManagerException(getGlobal(), SysErrorCode.USER_ILLEGALARGUMENT, "Invalid user id or namespace in query");

        UUID ehrId = retrieve(subjectId, nameSpace);

        if (ehrId == null)
            throw new ServiceManagerException(getGlobal(), SysErrorCode.USER_ILLEGALARGUMENT, "Could not retrieve an EHR for (subjectId, nameSpace)=("+subjectId+","+nameSpace+")");

        setSessionEhr(sessionId, ehrId);

        Map<String, String> retmap = new HashMap<>();
        retmap.put("ehrId", ehrId.toString());

        return retmap;
    }

    @QuerySetting(dialect = {
            @QuerySyntax(mode = I_ServiceRunMode.DialectSpace.STANDARD, httpMethod = "GET", method = "get", path = "vehr/ehr/status", responseType = ResponseType.Json),
            @QuerySyntax(mode = I_ServiceRunMode.DialectSpace.EHRSCAPE, httpMethod = "GET", method = "get", path = "rest/v1/ehr/status", responseType = ResponseType.Json)
    })
    public Object retrieveStatus(I_SessionClientProperties props) throws Exception {
        UUID ehrUuid = UUID.fromString(props.getClientProperty("ehrId", (String)null));
        String sessionId = props.getClientProperty(I_SessionManager.SECRET_SESSION_ID_INTERNAL, (String)null);

        if (ehrUuid == null)
            throw new ServiceManagerException(getGlobal(), SysErrorCode.USER_ILLEGALARGUMENT, "Invalid or null ehrId");

        I_EhrAccess ehrAccess = I_EhrAccess.retrieveInstance(getDataAccess(), ehrUuid);
        Map<String, String> subjectIds = I_EhrAccess.fetchSubjectIdentifiers(getDataAccess(), ehrUuid);
        I_SystemAccess systemAccess = I_SystemAccess.retrieveInstance(getDataAccess(), ehrAccess.getSystemId());

        Map<String, String> statusMap = new HashMap(){{
            put("subjectIds", subjectIds);
            put("queryable", ehrAccess.isQueryable());
            put("modifiable", ehrAccess.isModifiable());
            put("systemSettings", systemAccess.getSettings());
            put("systemDescription", systemAccess.getDescription());
        }};

        Map<String, Object> retmap = new HashMap<>();

        retmap.put("action", "RETRIEVE");
        retmap.put("ehrStatus", statusMap);
        retmap.put("ehrId", ehrUuid.toString());

        setSessionEhr(sessionId, ehrUuid);

        return retmap;

    }

    @QuerySetting(dialect = {
            @QuerySyntax(mode = I_ServiceRunMode.DialectSpace.STANDARD, httpMethod = "GET", method = "create", path = "vehr/ehr", responseType = ResponseType.Json),
            @QuerySyntax(mode = I_ServiceRunMode.DialectSpace.EHRSCAPE, httpMethod = "POST", method = "post", path = "rest/v1/ehr", responseType = ResponseType.Json)
    })
    public Object create(I_SessionClientProperties props) throws Exception {
        String subjectIdCode = props.getClientProperty("subjectId", (String) null);
        String subjectNameSpace = props.getClientProperty("subjectNamespace", (String)null);
        String systemSettings = props.getClientProperty("systemSettings", (String)null);
        String sessionId = props.getClientProperty(I_SessionManager.SECRET_SESSION_ID_INTERNAL, (String)null);

        if (subjectIdCode == null || subjectNameSpace == null)
            throw new ServiceManagerException(getGlobal(), SysErrorCode.USER_ILLEGALARGUMENT, "Invalid user id or namespace in query");

        UUID subjectUuid;
        try {
            subjectUuid = I_PartyIdentifiedAccess.retrievePartyByIdentifier(getDataAccess(), subjectIdCode, subjectNameSpace);
        } catch (Exception e){
            throw new IllegalArgumentException("Ehr cannot be created, there is no existing subject with this identifier:"+subjectIdCode+"::"+subjectNameSpace);
        }

        if (subjectUuid == null)
            throw new ServiceManagerException(getGlobal(), SysErrorCode.USER_ILLEGALARGUMENT, "Subject is not existing for id code="+subjectIdCode+" ,issuer="+subjectNameSpace);

        UUID systemId = null;
        if (systemSettings != null){ //NB: a systemSettings == null is valid, it is defaulted to the local system
            try {
                systemId = I_SystemAccess.retrieveInstanceId(getDataAccess(), systemSettings);
            } catch (Exception e) {
                throw new ServiceManagerException(getGlobal(), SysErrorCode.USER_ILLEGALARGUMENT, "System is not existing for settings="+systemSettings);
            }
        }

        UUID ehrId = create(subjectUuid, systemId);

        Map<String, String> retmap = new HashMap<>();
        retmap.put("ehrId", ehrId.toString());

        setSessionEhr(sessionId, ehrId);

        return retmap;
    }

    @QuerySetting(dialect = {
            @QuerySyntax(mode = I_ServiceRunMode.DialectSpace.STANDARD, httpMethod = "POST", method = "update", path = "vehr/ehr/status", responseType = ResponseType.Json),
            @QuerySyntax(mode = I_ServiceRunMode.DialectSpace.EHRSCAPE, httpMethod = "POST", method = "post", path = "rest/v1/ehr/status", responseType = ResponseType.Json)
    })
    public Object updateStatus(I_SessionClientProperties props) throws Exception {
        String sessionId = props.getClientProperty(I_SessionManager.SECRET_SESSION_ID_INTERNAL, (String)null);
        UUID ehrId = UUID.fromString(props.getClientProperty("ehrId", (String) null));

        if (ehrId == null )
            throw new ServiceManagerException(getGlobal(), SysErrorCode.USER_ILLEGALARGUMENT, "No valid ehr Id parameter found in query");

        //get body stuff
        String content = props.getClientProperty(Constants.REQUEST_CONTENT, (String)null);

        if (content == null)
            throw new ServiceManagerException(getGlobal(), SysErrorCode.USER_ILLEGALARGUMENT, "Content cannot be empty for updating ehr status");

        //get the map structure from the passed content string
        Gson json = new GsonBuilder().create();
        Map<String, Object> atributes = json.fromJson(content, Map.class);

        //retrieve the ehr to update
        I_EhrAccess ehrAccess = I_EhrAccess.retrieveInstance(getDataAccess(), ehrId);

        if (ehrAccess == null)
            throw new ServiceManagerException(getGlobal(), SysErrorCode.USER_ILLEGALARGUMENT, "Passed ehr Id does not match an existing EHR");

        if (atributes.containsKey("modifiable")) {
            ehrAccess.setModifiable((Boolean)atributes.get("modifiable"));
        }

        if (atributes.containsKey("queryable"))
            ehrAccess.setQueryable((Boolean)atributes.get("queryable"));

        if (atributes.containsKey("subjectId") && atributes.containsKey("subjectNamespace")){
            String subjectId = (String)atributes.get("subjectId");
            String subjectNameSpace = (String)atributes.get("subjectNamespace");
            List<DvIdentifier> identifiers = new ArrayList<>();
            identifiers.add(new DvIdentifier(subjectNameSpace, "", subjectId, ""));
            UUID partyId = I_PartyIdentifiedAccess.findIdentifiedParty(getDataAccess(), identifiers);

            if (partyId != null) {
                ehrAccess.setParty(partyId);
            }
        }

        ehrAccess.update();

        Map<String, String> retmap = new HashMap<>();
        retmap.put("ehrId", ehrId.toString());
        retmap.put("action", "UPDATE");

        setSessionEhr(sessionId, ehrId);

        return retmap;

    }

    private void setSessionEhr(String sessionId, UUID ehrId) throws ServiceManagerException {
        I_SessionManager sessionManager = getRegisteredService(getGlobal(), "LogonService", "1.0", null);
        //retrieve the session manager
        sessionManager.getSessionUserMap(sessionId).put(I_CompositionService.EHR_ID, ehrId);
    }

    @QuerySetting(dialect = {
            @QuerySyntax(mode = I_ServiceRunMode.DialectSpace.STANDARD, httpMethod = "GET", method = "delete", path = "vehr/ehr", responseType = ResponseType.String),
            @QuerySyntax(mode = I_ServiceRunMode.DialectSpace.EHRSCAPE, httpMethod = "DELETE", method = "delete", path = "rest/v1/ehr", responseType = ResponseType.String)
    })
    public String delete(I_SessionClientProperties props) throws ServiceManagerException {
        String ehrId = props.getClientProperty("ehrId", (String) null);

        if (ehrId == null || ehrId.length() == 0)
            throw new ServiceManagerException(getGlobal(), SysErrorCode.USER_ILLEGALARGUMENT, "No valid ehr Id parameter found in query");

        UUID ehrUuid = UUID.fromString(ehrId);

        I_EhrAccess ehrAccess = I_EhrAccess.retrieveInstance(getDataAccess(), ehrUuid);

        if (ehrAccess == null)
            throw new ServiceManagerException(getGlobal(), SysErrorCode.USER_ILLEGALARGUMENT, "Passed ehr Id does not match an existing EHR");

        Integer result = ehrAccess.delete();

        if (result > 0)
            return "Done";
        else
            return "Could not delete Ehr (id="+ehrId+")";

    }

}
