/*******************************************************************************
 * Copyright 2024 Jeremy Buckley
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * <a href= "http://www.apache.org/licenses/LICENSE-2.0">Apache License v2</a>
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.effacy.jui.rpc.extdirect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.effacy.jui.rpc.client.ErrorMessage;
import com.effacy.jui.rpc.client.ErrorRemoteResponse;
import com.effacy.jui.rpc.extdirect.csrf.ICRFHandler;
import com.effacy.jui.rpc.extdirect.csrf.StandardCSRFHandler;
import com.effacy.jui.rpc.extdirect.json.IJsonParser;
import com.effacy.jui.rpc.extdirect.json.JsonParser;
import com.effacy.jui.rpc.extdirect.json.JsonParserException;
import com.effacy.jui.rpc.extdirect.metadata.IActionMetadata;
import com.effacy.jui.rpc.extdirect.metadata.IMethodMetadata;
import com.effacy.jui.rpc.extdirect.metadata.IParameterMetadata;
import com.effacy.jui.rpc.extdirect.metadata.IRecordMetadata;
import com.effacy.jui.rpc.extdirect.metadata.IRouterMetadata;
import com.effacy.jui.rpc.extdirect.metadata.MethodMetadata.TransactionMode;
import com.effacy.jui.rpc.extdirect.metadata.RouterMetadataUtils;
import com.fasterxml.jackson.databind.module.SimpleModule;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Ext Direct router that is decoupled from the mechanism used to invoke it
 * (though not from HTTP).
 * <p>
 * Note that the mechanism supports error handling for remote method calls.
 * Generally errors within the router (generally related to router bugs or
 * router configuration) are logged to the standard class named channel.
 * Exceptions thrown from remote method calls that are eventually handled are
 * logged on this channel as a warning (they don't relate to router problems).
 * <p>
 * A special channel (see {@link #LOGGER_CHANNEL_ERROR}) is used to log only
 * remote method call errors. This channel should be used by all action error
 * handlers to log errors (and is used by default for
 * {@link ActionErrorHandler}). These errors are logged at the error level.
 * However, all remote method errors are logged at the warn level by the router.
 * If one thinks that errors are not being logged by the action handlers the
 * dropping to warn will expose these missing errors.
 * 
 * @author Jeremy Buckley
 */
public class Router implements IRouterMetadata {

    /**
     * Formatting template to create a remote request.
     */
    public static final String JSON_REQUEST_TEMPLATE = "{\"action\":\"%s\",\"method\":\"%s\",\"data\":[%s],\"type\":\"%s\",\"tid\":%s}";

    /**
     * Prefix for the remoting meta-data additional configuration values that must
     * be replaced by the context path.
     */
    public static final String META_DATA_URL_PREFIX = "url://";

    /**
     * The default remoting API reference ({@code "RemotingApi"}).
     */
    public static final String DEFAULT_REMOTING_API = "RemotingApi";

    /**
     * The default provider add method ({@code "Ext.Direct.addProvider"}).
     */
    public static final String DEFAULT_PROVIDER_METHOD = "Ext.Direct.addProvider";

    /**
     * The default remoting provider type ({@code "remoting"}).
     */
    public static final String DEFAULT_REMOTING_PROVIDER_TYPE = "remoting";

    /**
     * Search string in the content type for detecting a JSON encoded request.
     */
    public static final String CONTENT_TYPE_JSON = "application/json";

    /**
     * Form field for the action.
     */
    public static final String FIELD_ACTION = "extAction";

    /**
     * Form field for the method.
     */
    public static final String FIELD_METHOD = "extMethod";

    /**
     * Form field for the type.
     */
    public static final String FIELD_TYPE = "extType";

    /**
     * Form field for the TID.
     */
    public static final String FIELD_TID = "extTID";

    /**
     * Form field for the CSRF.
     */
    public static final String FIELD_CSRF = "extCsrf";

    /**
     * Field name for JSON data encapsulated in a multipart form request.
     */
    public static final String FIELD_JSON_DATA = "_jsonData";

    /**
     * Special channel for logging just error messages from remote calls (@{code
     * "system.rpc"}).
     */
    public static final String LOGGER_CHANNEL_ERROR = "system.rpc";

    /**
     * Commons logging (general).
     */
    private static final Logger LOG = LoggerFactory.getLogger (Router.class);

    /**
     * Indicates if is in debug mode.
     */
    private boolean debugMode = false;

    /**
     * The remoting API reference (see {@link #DEFAULT_REMOTING_API} for the
     * default).
     */
    private String remotingApi = DEFAULT_REMOTING_API;

    /**
     * The remoting provider type that is set in the provider meta-data.
     */
    private String remotingProviderType = DEFAULT_REMOTING_PROVIDER_TYPE;

    /**
     * The method call to add the provider.
     */
    private String providerMethod = DEFAULT_PROVIDER_METHOD;

    /**
     * Any additional configuration data that should be passed through to the
     * remoting API meta-data.
     */
    private Map<String, String> remotingConfig = null;

    /**
     * Controls wherether or not the {@link #getRemotingApi(String)} call includes a
     * JavaScript statement to include the API variable as a provider.
     */
    private boolean includeProviderStatement = true;

    /**
     * Controls whether or not the {@link #getRemotingApi(String)} call includes a
     * JavaScript declaring records.
     */
    private boolean includeRecordDeclarations = true;

    /**
     * Controls whether or not the JavaScript create blank method should be added to
     * the record.
     */
    private boolean includeCreateBlank = true;

    /**
     * Map of action names to action implementations.
     */
    private Map<String, IActionHandler> nameToActionMap = new HashMap<String, IActionHandler> ();

    /**
     * Meta-data for the actions held within the router.
     */
    private List<IActionMetadata> actionMetadata = new ArrayList<IActionMetadata> ();

    /**
     * The JSON parser to use.
     */
    protected IJsonParser jsonParser;

    /**
     * The transaction manager to use to create a transaction around remote methods
     * that need one.
     */
    protected PlatformTransactionManager transactionManager;

    /**
     * The transaction template.
     */
    private TransactionTemplate transactionTemplate;

    /**
     * The transaction template (for read only).
     */
    private TransactionTemplate transactionTemplateReadOnly;

    /**
     * Indicates that all remote method calls should be enclosed in a transaction
     * (rather than letting the action or method declare the need for a
     * transaction).
     */
    private boolean enforceTransactions = false;

    /**
     * The number of times an invocation should be retried (where a non-application
     * exception is encoutered).
     */
    private int retryCount = 0;

    /**
     * The delay in milliseconds between retries.
     */
    private int retryDelay = 100;

    /**
     * Determines if CSRF tokens should be used.
     */
    private boolean useCsrfTokens = true;

    /**
     * When using CSRF tokens also perform a session check.
     */
    private boolean useCsrfSession = true;

    /**
     * Sets the CSRF token cookie to be HTTP only.
     */
    private boolean useCsrfHttpOnly = false;

    /**
     * Encoder for CSRF encoding.
     */
    private ICRFHandler csrfEncoder = new StandardCSRFHandler ();

    /**
     * Router logger.
     */
    private IRouterLogger logger;

    /**
     * The local transaction for the router.
     */
    private static final ThreadLocal<IRouterTransaction> ROUTER_TX = new ThreadLocal<IRouterTransaction> ();

    /**
     * Default constructor.
     */
    public Router() {
        // Nothing.
    }

    /**
     * Construct with a remoting API variable name.
     * 
     * @param remotingApi
     *                    the remoting API variable name.
     * @param packages
     *                    packages to scan.
     * @see #setRemotingApi(String)
     */
    public Router(String remotingApi, String... packages) {
        setRemotingApi (remotingApi);
        scanPackages (packages);
    }

    /**
     * Sets debug mode.
     * 
     * @param debugMode
     *                  set to {@code true} if debug mode should be turned on.
     */
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    /**
     * Sets the remoting API variable name reference. This is returned when the
     * router is accessed with a GET request and is used to define the remoting API
     * that is used by Ext.Direct.
     * <p>
     * The default is defined by {@link #DEFAULT_REMOTING_API}.
     * 
     * @param remotingApi
     *                    the remoting API reference.
     */
    public void setRemotingApi(String remotingApi) {
        this.remotingApi = remotingApi;
    }

    /**
     * Sets the provider type for the remoting API (which corresponds to the value
     * of the {@code "type"} attribute of the API meta-data). Normally this is
     * {@code "remoting"} but can be set to something else (i.e. to reference a
     * customer extension of {@code Ext.direct.RemotingProvider}).
     * 
     * @param remotingProviderType
     */
    public void setRemotingProviderType(String remotingProviderType) {
        this.remotingProviderType = remotingProviderType;
    }

    /**
     * Sets whether or not the {@link #getRemotingApi(String)} call includes a
     * JavaScript statement to include the API variable as a provider.
     * 
     * @param includeProviderStatement
     *                                 {@code true} if should be included.
     */
    public void setIncludeProviderStatement(boolean includeProviderStatement) {
        this.includeProviderStatement = includeProviderStatement;
    }

    /**
     * Sets whether or not the {@link #getRemotingApi(String)} call includes
     * JavaScript declaring the records declared by actions.
     * 
     * @param includeRecordDeclarations
     *                                  {@code true} if should be included.
     */
    public void setIncludeRecordDeclarations(boolean includeRecordDeclarations) {
        this.includeRecordDeclarations = includeRecordDeclarations;
    }

    /**
     * Sets whether or not the JavaScript create blank method should be added to the
     * record (default is {@code true}).
     * 
     * @param includeCreateBlank
     *                           {@code true} if it should be included.
     */
    public void setIncludeCreateBlank(boolean includeCreateBlank) {
        this.includeCreateBlank = includeCreateBlank;
    }

    /**
     * Sets if CSRF tokens should be used. If they are then any request to obtain
     * the remote API will set a special cookie (see {@link #CSRF_COOKIE} ) with a
     * pseudo-random number that will be passed back in remote calls. Validation
     * will ensure that the cookie value and the passed value in the remote call
     * match.
     * <p>
     * By default CSRF tokens are enabled.
     * 
     * @param useCsrfTokens
     *                      if CSRF should be employed.
     */
    public void setUseCsrfTokens(boolean useCsrfTokens) {
        this.useCsrfTokens = useCsrfTokens;
    }

    /**
     * When using CSRF tokens (see {@link #setUseCsrfTokens(boolean)}) also perform
     * a session check for the token. This means that the session cookie can be HTTP
     * only as the CSRF cookie cannot (it needs to be accessed by javascript to
     * inject into the AJAX query).
     * 
     * @param useCsrfSession
     *                       {@code true} to also use the session (default is
     *                       {@code true} ).
     */
    public void setUseCsrfSession(boolean useCsrfSession) {
        this.useCsrfSession = useCsrfSession;
    }

    /**
     * When using CSRF tokens then cookie should be set to HttpOnly. In this case it
     * should not be accessible in script so not able to be sent during remoting
     * requests. In this case the check will only be performed on the cookies
     * returned in the request.
     * 
     * @param useCsrfHttpOnly
     *                        {@code true} if to ensure that requests are HTTP only
     *                        (default is {@code false}).
     */
    public void setUseCsrfHttpOnly(boolean useCsrfHttpOnly) {
        this.useCsrfHttpOnly = useCsrfHttpOnly;
    }

    /**
     * Assigns an encoder (and validator) for CSRF tokens.
     * 
     * @param csrfEncoder
     *                    the encoder to set
     */
    public void setCsrfEncoder(ICRFHandler csrfEncoder) {
        if (csrfEncoder != null)
            this.csrfEncoder = csrfEncoder;
    }

    /**
     * Any additional remoting API configuration meta-data that should be passed
     * through at the top level (the {@code Ext.Direct.RemotingProvider} expects an
     * associative array containing {@code type}, {@code url} and another
     * associative array of {@code actions}, this adds additional name-value pairs
     * at this top level). Generally this is used when a different provider is used
     * (and the provider type will need to be set using
     * {@link #setRemotingProviderType(String)}).
     * <p>
     * Note that the name (key) will always be surrounded in quotes when it appears
     * in the API JavaScript. The value will be inspected to determine how it should
     * be handled. If the value is one of {@code true} or {@code false} (case
     * insensitive) then it will be included without additional quotes. Similarly if
     * it parses as a {@code Double}, if it is surrounded already by double quotes
     * or surrounded by single quotes. All other cases double quotes will be added.
     * For a special case of URLS, if the value starts with
     * {@link #META_DATA_URL_PREFIX} then that prefix will be replaced by the
     * Servlet context root (and so should always be included when specifying local
     * URLS).
     * 
     * @param remotingConfig
     *                       the additional configuration as name-value pairs.
     */
    public void setRemotingConfig(Map<String, String> remotingConfig) {
        this.remotingConfig = remotingConfig;
    }

    /**
     * Adds a name-value pair to the remoting config.
     * 
     * @param name
     *              the property name.
     * @param value
     *              the property value.
     */
    public void addRemotingConfig(String name, String value) {
        if (StringUtils.isBlank (name))
            return;
        if (this.remotingConfig == null)
            this.remotingConfig = new HashMap<> ();
        this.remotingConfig.put (name, value);
    }

    /**
     * Sets the JSON parser to use.
     * 
     * @param jsonParser
     *                   the JSON parser to use.
     */
    public void setJsonParser(IJsonParser jsonParser) {
        this.jsonParser = jsonParser;
    }

    /**
     * Scans the given packages.
     * 
     * @param packages
     *                 the packages to scan.
     */
    public void scanPackages(String...packages) {
        this.jsonParser = new JsonParser (this, packages);
    }

    /**
     * Scans the given packages and allows for additional configuration of the
     * parser.
     * 
     * @param additions
     *                  to further configure the parser.
     * @param packages
     *                  the packages to scan.
     */
    public void scanPackages(Consumer<SimpleModule> additions,String...packages) {
        this.jsonParser = new JsonParser (this, additions, packages);
    }

    /**
     * Sets the transaction manager to use when transactions are required for remote
     * method calls. This allows implementation to use an effective
     * "transaction-in-view" pattern where the transaction boundary lies not
     * strictly on the remote method but terminates after the construction of the
     * JSON response.
     * 
     * @param transactionManager
     *                           the transaction manager.
     */
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        this.transactionTemplate = new TransactionTemplate (transactionManager);
        this.transactionTemplateReadOnly = new TransactionTemplate (transactionManager);
        this.transactionTemplateReadOnly.setReadOnly (true);
    }

    /**
     * Indicates that all remote method calls should be enclosed in a transaction
     * (rather than letting the action or method declare the need for a
     * transaction). This requires that a transaction manager be set (see
     * {@link #setTransactionManager(PlatformTransactionManager)}). The default for
     * this is {@code false}.
     * 
     * @param enforceTransactions
     *                            if transactions should be enforced.
     */
    public void setEnforceTransactions(boolean enforceTransactions) {
        this.enforceTransactions = enforceTransactions;
    }

    /**
     * Sets the number of times a request should be retried if an un-caught
     * exception is encountered.
     * 
     * @param retryCount
     *                   the number of retry attempts.
     */
    public void setRetryCount(int retryCount) {
        this.retryCount = Math.max (0, retryCount);
    }

    /**
     * Specifies the number of milliseconds between retries (only valid if
     * {@link #setRetryCount(int)} has been set to a value greater than 1).
     * 
     * @param retryDelay
     *                   the delay in milliseconds.
     */
    public void setRetryDelay(int retryDelay) {
        this.retryDelay = Math.max (0, retryDelay);
    }

    /**
     * The method call to add the provider.
     * 
     * @return the providerMethod
     */
    public String getProviderMethod() {
        return providerMethod;
    }

    /**
     * The method call to add the provider.
     * 
     * @param providerMethod
     *                       the providerMethod to set
     */
    public void setProviderMethod(String providerMethod) {
        this.providerMethod = providerMethod;
    }

    /**
     * The logger to use.
     * 
     * @return the logger.
     */
    protected IRouterLogger logger() {
        if (logger == null)
            logger = new RouterLogger ();
        return logger;
    }

    /**
     * Assign a logger.
     * 
     * @param logger
     *               the logger.
     */
    public void setLogger(IRouterLogger logger) {
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.com.effacy.jui.rpc.extdirect.metadata.IRouterMetadata#getActionMetadata()
     */
    @Override
    public List<IActionMetadata> getActionMetadata() {
        return actionMetadata;
    }

    /**
     * Adds an action to the router. Note that this will replace any existing action
     * with the same name (but a warning will be logged).
     * 
     * @param action
     *               the action to add.
     */
    public void addAction(IActionHandler action) {
        if (action.getActionName () == null)
            return;
        if (LOG.isWarnEnabled () && nameToActionMap.containsKey (action.getActionName ())) {
            LOG.warn ("Attempt to add action handler with duplicate action name \"" + action.getActionName () + "\", skipping...");
        } else {
            nameToActionMap.put (action.getActionName (), action);
            actionMetadata.add (action);
        }
    }

    /**
     * Processes a single remote call. This will generate a call response.If the
     * underlying action generates an error, it will be asked to handle that error.
     * If it cannot, then a general error response is returned. This method is
     * expected to adequately handle all exceptions internally.
     * 
     * @param request
     *                the request to process.
     * @return The response from the call.
     */
    public String processToJson(final ExtendedRemoteCallRequest request) {
        try {
            RouterLogger.log ("{" + request.getAction () + "::" + request.getMethod () + "::" + request.getTid () + "}");
            RouterLogger.indent ();

            // Validate the request structure and retrieve the action and the
            // method meta-data.
            request.validate ();
            final IActionHandler action = this.nameToActionMap.get (request.getAction ());
            if (action == null)
                throw new InvalidCallRequestException (request, "Unknown action \"" + request.getAction () + "\"");
            IMethodMetadata methodMetadata = action.lookupMethodMetadata (request);

            // Performance logging.
            long logPerformanceStart = System.currentTimeMillis ();

            int retryCount = this.retryCount;
            Throwable cause = null;
            try {
                LOOP: do {
                    try {
                        return processOnce (action, request, methodMetadata.isTransactional ());
                    } catch (RuntimeException e1) {
                        cause = e1.getCause ();
                        if (!action.retryError (request, cause)) {
                            retryCount = 0;
                        } else {
                            try {
                                if (retryDelay > 0)
                                    Thread.sleep (retryDelay);
                            } catch (InterruptedException e2) {
                                break LOOP;
                            }
                        }
                    } catch (PassThroughException e) {
                        cause = e.exception ();
                        break LOOP;
                    }
                } while (retryCount-- > 0);
            } finally {
                RouterLogger.performance (System.currentTimeMillis () - logPerformanceStart);
                // if (LOG_PERFORMANCE.isInfoEnabled ()) {
                // long executionTime = System.currentTimeMillis () -
                // logPerformanceStart;
                // LOG_PERFORMANCE.info (request.getRequest ().getSession
                // ().getId () + "," + methodMetadata.getMethodName () + "," +
                // executionTime);
                // }
            }

            // This exception will contain the underlying exception
            // thrown from the method handler.
            try {
                // All errors are logged at the warn level. In general the
                // action should perform the logging so dropping down to warn
                // will expose any messages that are being missed.
                // if (LOG_REMOTE.isWarnEnabled ())
                // LOG_REMOTE.warn ("Exception calling: " + request.toString (),
                // cause);
                RouterLogger.exception (cause);
                return jsonParser.remoteCallResponseToJson (action.processError (request, cause));
            } catch (InvalidCallRequestException e2) {
                // The action could not handle the error, so we
                // handle it directly.
                LOG.error ("Error generated when calling [" + request + "]: ", cause);
                return processErrorToJson (request, cause);
            }
        } catch (InvalidCallRequestException e) {
            // If the method could not be mapped or the request is not valid.
            LOG.error ("Error generated when calling [" + request + "]: ", e);
            return processErrorToJson (request, e);
        } catch (Throwable e) {
            // This should not happen.
            LOG.error ("Uncaught exception processing handler: " + e.getMessage (), e);
            return processErrorToJson (request, e);
        } finally {
            try {
                RouterLogger.log ();
            } finally {
                RouterLogger.clear ();
            }
        }
    }

    public static record TransactionResponse(String value, PassThroughException exception) {}

    /**
     * Process an action exactly once. This will handle any non-runtime exceptions.
     * 
     * @param action
     *                      the action to invoke.
     * @param request
     *                      the request.
     * @param transactional
     *                      if the action indicates being transactional.
     * @return The return value.
     * @throws RuntimeException
     *                          On error.
     */
    protected String processOnce(final IActionHandler action, final ExtendedRemoteCallRequest request, boolean transactional) throws PassThroughException {
        TransactionResponse response = processTransactional (new TransactionCallback<TransactionResponse> () {

            public TransactionResponse doInTransaction(TransactionStatus status) {
                try {
                    RouterLogger.indent ("Router.doInTransaction(" + RouterLogger.format (status));
                    return new TransactionResponse(processToJson (action, request), null);
                } catch (PassThroughException e) {
                    return new TransactionResponse (null, e);
                } finally {
                    RouterLogger.outdent ();
                }
            }
        }, transactional, action.isReadOnly (request));
        if (response.exception () != null)
            throw response.exception ();
        return response.value();
    }

    /**
     * Process the callback in a transaction if transactions are available and
     * supported.
     * <p>
     * This will always return a return value even when the transaction is marked as
     * rollback.
     * 
     * @param cb
     *                      the callback (if no transactions are available passed
     *                      status will be {@code null}).
     * @param transactional
     *                      if transactions are to be used.
     * @param readOnly
     *                      if the execution may be considered as read only.
     * @return the result.
     */
    protected <V> V processTransactional(final TransactionCallback<V> cb, boolean transactional, boolean readOnly) {
        final Bucket<V> returnValue = new Bucket<V> ();
        try {
            RouterLogger.indent ("Router.processTransactional(): tx=(" + enforceTransactions + " and " + transactional + "), ro=" + readOnly);
            if (enforceTransactions && transactional) {
                if (transactionManager != null) {
                    final TransactionTemplate template = readOnly ? transactionTemplateReadOnly : transactionTemplate;
                    ROUTER_TX.set (new RouterTransaction (readOnly ? TransactionMode.READ_ONLY : TransactionMode.READ_WRITE));
                    return template.execute (new TransactionCallback<V> () {

                        @Override
                        public V doInTransaction(TransactionStatus status) {
                            try {
                                RouterLogger.indent ("Router.processTransactional->doInTransaction (tx_template=" + RouterLogger.format (template) + ")");
                                V value = cb.doInTransaction (status);
                                returnValue.set (value);
                                return value;
                            } finally {
                                RouterLogger.outdent ();
                            }
                        }
                    });
                }
                LOG.error ("Transactions are being enforced but there is no transaction manager declared");
            }
            ROUTER_TX.set (new RouterTransaction (TransactionMode.NONE));
            return cb.doInTransaction (null);
        } catch (RuntimeException e) {
            if (returnValue.get () != null)
                return returnValue.get ();
            throw e;
        } finally {
            ROUTER_TX.set (null);
            RouterLogger.outdent ();
        }
    }

    /**
     * Processes the given request against the given action handler and returns the
     * result as JSON. If a {@link InvalidCallRequestException} is encountered, then
     * that will be translated to an error response by
     * {@link #processErrorToJson(ExtendedRemoteCallRequest, Throwable)} while any
     * other exception will be wrapped in a {@link RuntimeException} (this allows a
     * transaction to roll-back) - the original exception will be contained as its
     * cause.
     * 
     * @param handler
     *                the handler to process.
     * @param request
     *                the request to process against the handler.
     * @return The return JSON.
     * @throws RuntimeException
     *                                       On error from the handler.
     * @throws RollbackWithResponseException
     *                                       If an exception condition has occurred
     *                                       but we have a specific response to send
     *                                       to the client.
     */
    protected String processToJson(IActionHandler handler, ExtendedRemoteCallRequest request) throws PassThroughException {
        try {
            return jsonParser.remoteCallResponseToJson (handler.process (request));
        } catch (InvalidCallRequestException e) {
            // Could not locate the handler.
            return processErrorToJson (request, e);
        } catch (PassThroughException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException (e);
        }
    }

    /**
     * Generate some JSON when an exception is caught processing a remote method. By
     * default this encodes the error as an error response.
     * 
     * @param request
     *                the request that generated the error.
     * @param e
     *                the error.
     * @return A JSON string to return to the client.
     */
    protected String processErrorToJson(ExtendedRemoteCallRequest request, Throwable e) {
        try {
            if (debugMode)
                return jsonParser.remoteCallResponseToJson (new RemoteCallExceptionResponse (request, e.getMessage (), "The problem occurred at the router."));
            return jsonParser.remoteCallResponseToJson (new RemoteCallExceptionResponse (request, "Sorry, a problem occurred."));
        } catch (JsonParserException e1) {
            // This is not good.
            throw new RuntimeException (e1);
        }
    }

    /**
     * Creates a suitable remote call response from a collection of response
     * messages (errors).
     * 
     * @param request
     *                the request that was processed.
     * @param errors
     *                the errors to encapsulate in a response.
     * @return The response.
     */
    protected String processErrorToJson(ExtendedRemoteCallRequest request, List<ErrorMessage> errors) throws JsonParserException {
        return jsonParser.remoteCallResponseToJson (new RemoteCallResponse (request, new ErrorRemoteResponse (errors)));
    }

    /**
     * Processes a list of requests, generating a list of responses.
     * 
     * @param requests
     *                 the requests to process.
     * @return The JSON (or similar) response.
     */
    public String processToJson(List<ExtendedRemoteCallRequest> requests, HttpServletRequest servletRequest) {
        // Extract any CSRF token (if applicable) to validate against what is passed
        // through.
        Optional<String> csrfToken = useCsrfTokens ? csrfEncoder.token (servletRequest) : Optional.empty ();

        List<String> responses = new ArrayList<String> ();
        for (ExtendedRemoteCallRequest request : requests) {
            if (useCsrfTokens && !useCsrfHttpOnly && csrfToken.isPresent () && !csrfEncoder.validate (request.getCsrfToken (), csrfToken.get ())) {
                try {
                    responses.add (jsonParser.remoteCallResponseToJson (new RemoteCallDeniedResponse (request, "Cross scripting denial.")));
                } catch (JsonParserException e) {
                    // This is not good.
                    throw new RuntimeException (e);
                }
            } else
                responses.add (processToJson (request));
        }

        // Collate each response into a single JSON string.
        StringBuilder jsonResponse = new StringBuilder ();
        jsonResponse.append ('[');
        boolean bFirst = true;
        for (String str : responses) {
            if (bFirst)
                bFirst = false;
            else
                jsonResponse.append (',');
            jsonResponse.append (str);
        }
        jsonResponse.append (']');
        return jsonResponse.toString ();
    }

    /**
     * Processes an HTTP request generating an appropriate HTTP response. If this is
     * a POST then this will handle any exceptions generated by underlying actions.
     * If this is a GET then it will return the API definition string. Result is
     * returned as a string and nothing is written to the response other than the
     * possibility for a cookie.
     * 
     * @param request
     *                the request to process.
     * @return The JSON (or similar) response.
     * @throws IOException
     *                     On error processing stream (including parsing JSON
     *                     requests).
     */
    protected String processRequestToString(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Handle the request for the API.
        if (!"POST".equals (request.getMethod ())) {
            String csrfToken = null;
            if (useCsrfTokens) {
                var csrfTokenVal = establishCsrfCookies (request, response);
                csrfToken = csrfTokenVal.isPresent () ? csrfTokenVal.get () : null;
            }
            return getRemotingApi (request.getRequestURI (), request.getContextPath (), csrfToken);
        }

        // Handle the case where the incoming content type is JSON (this is
        // the case where the formHandler variable of the method meta-data
        // is false).
        String contentType = request.getContentType ();
        if ((contentType != null) && contentType.contains (CONTENT_TYPE_JSON))
            return processJSONRequest (request);
        if (request instanceof MultipartHttpServletRequest)
            return processMultipartFormRequest ((MultipartHttpServletRequest) request);

        // Should be a form request.
        return processFormRequest (request);
    }

    /**
     * Processes the request where the body of the request is assumed to be defined
     * as JSON.
     * 
     * @param request
     *                the request to process.
     * @return The JSON (or similar) response.
     * @throws IOException
     *                     On error processing stream (including parsing JSON
     *                     requests).
     */
    protected String processJSONRequest(HttpServletRequest request) throws IOException {
        String jsonQuery = readerToString (request.getReader ());
        try {
            // Extract request.
            List<RemoteCallRequest> requests = jsonParser.jsonToRemoteCallRequests (jsonQuery);
            List<ExtendedRemoteCallRequest> extendedRequests = new ArrayList<ExtendedRemoteCallRequest> ();

            // Process the individual requests.
            for (RemoteCallRequest call : requests)
                extendedRequests.add (new ExtendedRemoteCallRequest (call, request));

            return processToJson (extendedRequests, request);
        } catch (JsonParserException e) {
            // There was a problem parsing a request. We raise this as a
            // debug rather than an error.
            if (LOG.isDebugEnabled ())
                LOG.debug ("Problem parsing the following JSON: \"" + jsonQuery + "\"", e);
            throw new IOException ("Problem parsing JSON request.", e);
        } catch (Throwable e) {
            // This should not happen.
            if (LOG.isErrorEnabled ())
                LOG.error ("Uncaught exception with parsing the following JSON: \"" + jsonQuery + "\"", e);
            throw new IOException ("Problem parsing JSON request.", e);
        }
    }

    /**
     * Processes the request assuming it to be a normal form request (that is, with
     * form parameters).
     * <p>
     * The request is required to have the {@link #FIELD_ACTION} (a string),
     * {@link #FIELD_METHOD} (a string), {@link #FIELD_TID} (an integer) and the
     * {@link #FIELD_TYPE} (name of one of the {@link RemoteCallType} enumeration
     * values). The action and method are used to lookup the associated method
     * meta-data and then each parameter is created and populated from the Servlet
     * parameter map.
     * <p>
     * If the required parameters are not present then this will be treated as a GET
     * and will return the API.
     * 
     * @param request
     *                the request to process.
     * @return The JSON (or similar) response.
     * @throws IOException
     *                     On error processing stream (including parsing JSON
     *                     requests).
     */
    protected String processFormRequest(HttpServletRequest request) throws IOException {
        // Construct the remote call.
        RemoteCallRequest call = null;
        try {
            List<Object> data = new ArrayList<Object> ();
            String action = request.getParameter (FIELD_ACTION);
            if (action == null)
                return getRemotingApi (request.getRequestURI (), request.getContextPath (), null);
            String method = request.getParameter (FIELD_METHOD);
            if (method == null)
                return getRemotingApi (request.getRequestURI (), request.getContextPath (), null);
            RemoteCallType type = null;
            try {
                type = RemoteCallType.valueOf (request.getParameter (FIELD_TYPE));
            } catch (Throwable e) {
                return getRemotingApi (request.getRequestURI (), request.getContextPath (), null);
            }
            int tid = Integer.parseInt (request.getParameter (FIELD_TID));
            IMethodMetadata metadata = RouterMetadataUtils.resolveMetadata (action, method, this);
            if (metadata != null) {
                for (IParameterMetadata parameter : metadata.getParameterMetadata ()) {
                    Constructor<?> constructor = parameter.getParameterType ().getConstructor ();
                    Object obj = constructor.newInstance ();
                    BeanUtils.populate (obj, request.getParameterMap ());
                    data.add (obj);
                }
            }
            String passedCsrfToken = request.getParameter (FIELD_CSRF);
            call = new RemoteCallRequest (action, method, type, tid, passedCsrfToken, data);
        } catch (Throwable e) {
            throw new IOException ("Problem parsing action data from request.", e);
        }

        // Process the remote call.
        List<ExtendedRemoteCallRequest> extendedRequests = new ArrayList<ExtendedRemoteCallRequest> ();
        extendedRequests.add (new ExtendedRemoteCallRequest (call, request));
        return processToJson (extendedRequests, request);
    }

    /**
     * Processes the request assuming it to be a normal form request (that is, with
     * form parameters).
     * <p>
     * The request is required to have the {@link #FIELD_ACTION} (a string),
     * {@link #FIELD_METHOD} (a string), {@link #FIELD_TID} (an integer) and the
     * {@link #FIELD_TYPE} (name of one of the {@link RemoteCallType} enumeration
     * values). The action and method are used to lookup the associated method
     * meta-data and then each parameter is created and populated from the Servlet
     * parameter map.
     * 
     * @param request
     *                the request to process.
     * @return The JSON (or similar) response.
     * @throws IOException
     *                     On error processing stream (including parsing JSON
     *                     requests).
     */
    protected String processMultipartFormRequest(MultipartHttpServletRequest request) throws IOException {
        try {
            // Construct the remote call.
            String jsonData = request.getParameter (FIELD_JSON_DATA);
            String jsonQuery = String.format (JSON_REQUEST_TEMPLATE, request.getParameter (FIELD_ACTION), request.getParameter (FIELD_METHOD), jsonData, request.getParameter (FIELD_TYPE), request.getParameter (FIELD_TID));

            // Extract request.
            List<RemoteCallRequest> requests = jsonParser.jsonToRemoteCallRequests (jsonQuery);
            List<ExtendedRemoteCallRequest> extendedRequests = new ArrayList<ExtendedRemoteCallRequest> ();
            for (RemoteCallRequest call : requests) {
                // Attempt to bind uploaded files to data beans.
                for (Object data : call.getData ()) {
                    BeanUtils.populate (data, request.getFileMap ());
                }
                extendedRequests.add (new ExtendedRemoteCallRequest (call, request));
            }
            return processToJson (extendedRequests, request);
        } catch (Throwable e) {
            // Only thrown if there is invalid data.
            if (LOG.isDebugEnabled ())
                LOG.debug ("Problem parsing action data from request: ", e);
            throw new IOException ("Problem parsing action data from request.");
        }
    }

    /**
     * Processes an HTTP request generating an appropriate HTTP response. If this is
     * a POST then this will handle any exceptions generated by underlying actions.
     * If this is a GET then it will return the API definition string.
     * 
     * @param request
     *                 the request to process.
     * @param response
     *                 the response to return.
     * @throws IOException
     *                     On error processing stream (including parsing JSON
     *                     requests).
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getWriter ().write (processRequestToString (request, response));
    }

    /**
     * Converts a buffered reader to a string.
     * 
     * @param reader
     *               the reader to convert.
     * @return The string content of the reader.
     * @throws IOException
     *                     On error processing stream.
     */
    protected String readerToString(BufferedReader reader) throws IOException {
        String line = null;
        StringBuilder sb = new StringBuilder ();
        while ((line = reader.readLine ()) != null)
            sb.append (line);
        return sb.toString ();
    }

    /**
     * ExtJS Direct has a mechanism where provider instances can be declared that
     * implement remoting calls to this router. This is done by building a
     * JavaScript associative array that contains meta-data representing the actions
     * and methods exposed by this router, then calling the
     * {@code Ext.Direct.addProvider()} method passing this array. This method then
     * creates an instance of the desired provider type (mapped by name and declared
     * in the {@code type} field of the associative array, typically this is
     * {@code remoting} which maps to the {@code Ext.Direct.RemotingProvider}
     * class). The provider type then builds from the meta-data methods named
     * {@code Service.Method} as declared in the meta-data.
     * <p>
     * This method returns the associative array code in JavaScript and optionally
     * (if {@link #setIncludeProviderStatement(boolean)} has been set to
     * {@code true}, which it is by default) the code that adds the array as a
     * provider. This is normally returned by a GET request to the routers service
     * URL so can be included in a page as a script.
     * <p>
     * Sometimes you may want to extend the {@code Ext.Direct.RemotingProvider}
     * creating your own provider. You then declare that provider in the
     * {@code Ext.Direct.PROVIDERS} array mapped by the name of the type of your
     * provider. To use this provider you must set the name of the type as the
     * remoting provider type (see {@link #setRemotingProviderType(String)}). If
     * your provider needs additional server-side configuration, then you can pass
     * that through as a map of name-value pairs (see
     * {@link #setRemotingConfig(Map)}).
     * <p>
     * This will also return record definitions for records that have been declared
     * in the action meta-data. This can be disabled by setting
     * {@link #setIncludeRecordDeclarations(boolean)} to {@code false}.
     * 
     * @param url
     *                the calling URL to generate the remoting API reference from.
     * @param context
     *                the context path.
     * @return the remoting API.
     */
    protected String getRemotingApi(String url, String context, String csrf) {
        StringWriter sw = new StringWriter ();
        PrintWriter pw = new PrintWriter (sw);

        pw.print (remotingApi);
        pw.print (" = {\"url\":\"");
        pw.print (url);
        pw.print ("\",\"type\":\"");
        pw.print (remotingProviderType);
        if (!StringUtils.isBlank (csrf)) {
            pw.print ("\",\"csrf\":\"");
            pw.print (csrf);
        }
        pw.print ("\",");
        if (remotingConfig != null) {
            for (String key : remotingConfig.keySet ()) {
                String value = remotingConfig.get (key);
                if (value != null) {
                    pw.print ("\"");
                    pw.print (key);
                    pw.print ("\":");
                    pw.print (generateValue (context, value));
                    pw.print (",");
                }
            }
        }
        pw.print ("\"actions\":{");
        boolean bActionFirst = true;
        for (IActionMetadata action : getActionMetadata ()) {
            if (bActionFirst)
                bActionFirst = false;
            else
                pw.print (',');
            pw.print ('\"');
            pw.print (action.getActionName ());
            pw.print ("\":[");
            boolean bMethodFirst = true;
            for (IMethodMetadata method : action.getMethodMetadata ()) {
                if (bMethodFirst)
                    bMethodFirst = false;
                else
                    pw.print (',');
                pw.print ("{\"name\":\"");
                pw.print (method.getMethodName ());
                pw.print ("\",\"len\":");
                pw.print (method.getParameterMetadata ().size ());
                if (method.isFormHandler ())
                    pw.print (",\"formHandler\":true");
                pw.print ('}');
            }
            pw.print (']');
        }
        pw.println ("}};");

        // Add in the call to add (and configure) the provider.
        if (includeProviderStatement) {
            pw.print (providerMethod);
            pw.print ("(");
            pw.print (remotingApi);
            pw.println (");");
        }

        // Add in record declarations.
        if (includeRecordDeclarations) {
            Set<String> recordNames = new HashSet<String> ();
            for (IActionMetadata action : getActionMetadata ()) {
                for (IRecordMetadata record : action.getRecordMetadata ()) {
                    if (!recordNames.contains (record.getName ())) {
                        RouterMetadataUtils.toJavaScript (record, pw, includeCreateBlank);
                        recordNames.add (record.getName ());
                    }
                }
            }
        }

        pw.flush ();
        return sw.toString ();
    }

    /**
     * Generates a (possibly) quoted value for inclusion in the remoting API
     * meta-data.
     * 
     * @param context
     *                the context path.
     * @param value
     *                the value to process.
     * @return The value.
     */
    protected String generateValue(String context, String value) {
        if (value == null)
            return "null";
        if ("true".equalsIgnoreCase (value))
            return "true";
        if ("false".equalsIgnoreCase (value))
            return "false";
        if (value.startsWith ("\"") && value.endsWith ("\""))
            return value;
        if (value.startsWith ("\'") && value.endsWith ("\'"))
            return value;
        try {
            Double.parseDouble (value);
            return value;
        } catch (NumberFormatException e) {
            if (value.startsWith (META_DATA_URL_PREFIX)) {
                if (context.endsWith ("/"))
                    value = context + value.substring (META_DATA_URL_PREFIX.length ());
                else
                    value = context + "/" + value.substring (META_DATA_URL_PREFIX.length ());
            }
            return "\"" + value + "\"";
        }
    }

    /**
     * Gets the router transaction.
     * 
     * @return the transaction.
     */
    public static IRouterTransaction transaction() {
        IRouterTransaction tx = ROUTER_TX.get ();
        return (tx == null) ? new RouterTransaction () : tx;
    }

    /**
     * Forces the current router transaction to be writable.
     */
    public static void forceTransactionWrite() {
        RouterTransaction tx = new RouterTransaction (TransactionMode.READ_WRITE);
        if (transaction ().isInProgress ())
            tx.inProgress = true;
        ROUTER_TX.set (tx);
    }

    /**
     * Establishes cookie data for CSRF.
     * <p>
     * This token is passed back in the response (by default as a Cookie) and is
     * used by the RPC implementation to return the token on each request.
     * 
     * @param request
     *                 the request.
     * @param response
     *                 the response.
     * @param the
     *                 CSRF token that needs to be returned.
     */
    public Optional<String> establishCsrfCookies(HttpServletRequest request, HttpServletResponse response) {
        return csrfEncoder.generate (request, response);
    }

    /**
     * Implementation of {@link IRouterTransaction}.
     */
    private static class RouterTransaction implements IRouterTransaction {

        /**
         * If an execution is in progress.
         */
        private boolean inProgress;

        /**
         * The transaction mode.
         */
        private TransactionMode transactionMode;

        /**
         * Construct with nothing in progress.
         */
        public RouterTransaction() {
            this.inProgress = false;
            this.transactionMode = TransactionMode.NONE;
        }

        /**
         * Construct with a transaction mode.
         * 
         * @param transactionMode
         *                        the mode.
         */
        public RouterTransaction(TransactionMode transactionMode) {
            this.inProgress = true;
            this.transactionMode = (transactionMode == null) ? TransactionMode.NONE : transactionMode;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.com.effacy.jui.rpc.extdirect.IRouterTransaction#isInProgress()
         */
        @Override
        public boolean isInProgress() {
            return inProgress;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.com.effacy.jui.rpc.extdirect.IRouterTransaction#getTransactionMode()
         */
        @Override
        public TransactionMode getTransactionMode() {
            return transactionMode;
        }

    }

    public class Bucket<V> {

        private V value;

        public V get() {
            return value;
        }

        public void set(V value) {
            this.value = value;
        }
    }
}
