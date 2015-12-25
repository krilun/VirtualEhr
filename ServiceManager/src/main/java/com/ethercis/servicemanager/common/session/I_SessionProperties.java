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
package com.ethercis.servicemanager.common.session;

import com.ethercis.servicemanager.common.I_SessionClientProperties;
import com.ethercis.servicemanager.common.property.PropBoolean;


public interface I_SessionProperties {

	public abstract I_SessionClientProperties getClientProperties();

	/**
	 * Timeout until session expires if no communication happens
	 */
	public abstract long getSessionTimeout();

	/**
	 * Timeout until session expires if no communication happens
	 * @param timeout The login session will be destroyed after given milliseconds.<br />
	 *                Session lasts forever if set to 0L
	 */
	public abstract void setSessionTimeout(long timeout);

	/**
	 * If maxSession == 1, only a single login is possible
	 */
	public abstract int getMaxSessions();

	/**
	 * If maxSession == 1, only a single login is possible
	 * @param max How often the same client may login
	 */
	public abstract void setMaxSessions(int max);

	/**
	 * If clearSessions is true, all old sessions of this user are discarded. 
	 */
	public abstract boolean clearSessions();

	/**
	 * If clearSessions is true, all old sessions of this user are discarded. 
	 * @param clear Defaults to false
	 */
	public abstract void clearSessions(boolean clear);

	/**
	 * @param Set if we allow multiple updates for the same message if we have subscribed multiple times to it. 
	 */
	public abstract void setReconnectSameClientOnly(
			boolean reconnectSameClientOnly);

	/**
	 * @return true if we allow multiple updates for the same message if we have subscribed multiple times to it. 
	 */
	public abstract boolean reconnectSameClientOnly();

	/**
	 */
	public abstract PropBoolean reconnectSameClientOnlyProp();

	/**
	 * Set our session identifier which authenticates us for xmlBlaster. 
	 * <p />
	 * This is used server side only.
	 * @param id The unique and secret sessionId
	 */
	public abstract void setSecretSessionId(String id);

	/**
	 * Get our secret session identifier which authenticates us for xmlBlaster. 
	 * <p />
	 * @return The unique, secret sessionId
	 */
	public abstract String getSecretSessionId();

	/**
	 * The public session ID to support reconnect to an existing session. 
	 * <p>
	 * This is extracted from the sessionName.getPublicSessionId()
	 * </p>
	 * @return 0 if no session but a login name<br />
	 *        <0 if session ID is generated by xmlBlaster<br />
	 *        >0 if session ID is given by user
	 */
	public abstract long getPublicSessionId();

	public abstract boolean hasPublicSessionId();

	/**
	 * Set our unique SessionName. 
	 * @param sessionName
	 */
	public abstract void setSessionName(I_SessionName sessionName);

	/**
	 * Set our unique SessionName. 
	 * @param sessionName
	 * @param markAsModified false if you are setting a default sessionName, true if the user set the sessionName
	 */
	public abstract void setSessionName(I_SessionName sessionName,
			boolean markAsModified);

	public abstract boolean isSessionNameModified();

	/**
	 * Get our unique SessionName. 
	 * <p />
	 * @return The unique SessionName (null if not known)
	 */
	public abstract I_SessionName getSessionName();

	/**
	 * If reconnected==true a client has reconnected to an existing session
	 */
	public abstract void setReconnected(boolean reconnected);

	/**
	 * @return true A client has reconnected to an existing session
	 */
	public abstract boolean isReconnected();

	/**
	 * Unique id of the xmlBlaster server, changes on each restart. 
	 * If 'node/heron' is restarted, the instanceId changes.
	 * @return nodeId + timestamp, '/node/heron/instanceId/33470080380'
	 */
	public abstract String getServerInstanceId();

	/**
	 * Unique id of the xmlBlaster server, changes on each restart. 
	 * If 'node/heron' is restarted, the instanceId changes.
	 * @param instanceId e.g. '/node/heron/instanceId/33470080380'
	 */
	public abstract void setServerInstanceId(String instanceId);

	/**
	 * Returns the connection state directly after the connect() method returns (client side only). 
	 * @return Usually ConnectionStateEnum.ALIVE or ConnectionStateEnum.POLLING
	 */
	public abstract I_ConnectionStateEnum getInitialConnectionState();

	/**
	 * Set the connection state directly after the connect() (client side only). 
	 */
	public abstract void setInitialConnectionState(
			I_ConnectionStateEnum initialConnectionState);

	/**
	 * Tell authenticate to not check the password.
	 * This is an internal attribute and never set from outside
	 * this is no security hole.
	 */
	public abstract void bypassCredentialCheck(boolean bypassCredentialCheck);

	public abstract boolean bypassCredentialCheck();

	/**
	 * @return the login credential or null if not set
	 */
	public abstract I_SecurityProperties getSecurityProperties();

	/**
	 * Force a security configuration. 
	 * <p>
	 * You can use loadClientPlugin() or setUserId() instead which loads the 
	 * given/default security plugin and does a lookup in the environment.
	 */
	public abstract void setSecurityProperties(
			I_SecurityProperties securityprops);

	/**
	 * Converts the data into a valid XML ASCII string.
	 * @return An XML ASCII string
	 */
	public abstract String toString();

	/**
	 * Dump state of this object into a XML ASCII string.
	 */
	public abstract String toXml();

	/**
	 * Dump state of this object into a XML ASCII string.
	 * <br>
	 * @param extraOffset indenting of tags for nice response
	 * @return internal state of the SessionQos logonservice a XML ASCII string
	 */
	public abstract String toXml(String extraOffset);

	/**
	 * Get a usage string for the connection parameters
	 */
	public abstract String usage();

	public abstract String getClientPluginType();

	public abstract String getClientPluginVersion();

	public abstract String getClientIp();

	public abstract boolean isSessionLimitsPubSessionIdSpecific();

	public abstract void setSessionLimitsPubSessionIdSpecific(
			boolean sessionLimitsPubSessionIdSpecific);

}