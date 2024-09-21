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
package com.effacy.jui.rpc.extdirect.annotation;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effacy.jui.rpc.extdirect.ExtendedRemoteCallRequest;
import com.effacy.jui.rpc.extdirect.IActionErrorHandler;
import com.effacy.jui.rpc.extdirect.IActionHandler;
import com.effacy.jui.rpc.extdirect.InvalidCallRequestException;
import com.effacy.jui.rpc.extdirect.RemoteCallRequest;
import com.effacy.jui.rpc.extdirect.RemoteCallResponse;
import com.effacy.jui.rpc.extdirect.Router;
import com.effacy.jui.rpc.extdirect.metadata.ActionMetadata;
import com.effacy.jui.rpc.extdirect.metadata.IMethodMetadata;
import com.effacy.jui.rpc.extdirect.metadata.IParameterMetadata;
import com.effacy.jui.rpc.extdirect.metadata.MethodMetadata;
import com.effacy.jui.rpc.extdirect.metadata.MethodMetadata.TransactionMode;
import com.effacy.jui.rpc.extdirect.metadata.ParameterMetadata;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Action handler that encompasses a bean that is used to process actions. It is
 * assumed that the bean is thread safe.
 * 
 * @author Jeremy Buckley
 */
public class AnnotatedActionHandler extends ActionMetadata implements IActionHandler {

    /**
     * The bean.
     */
    private Object bean;

    /**
     * Map of method name to handler for that method.
     */
    private Map<String, MethodHandler> nameToMethodHandler = new HashMap<String, MethodHandler> ();

    /**
     * Commons logging.
     */
    private static final Logger LOG = LoggerFactory.getLogger (AnnotatedActionHandler.class);

    /**
     * Commons logging (general).
     */
    private static final Logger LOG_REMOTE = LoggerFactory.getLogger (Router.LOGGER_CHANNEL_ERROR);

    /**
     * Construct with a bean.
     * 
     * @param bean
     *            the bean.
     */
    public AnnotatedActionHandler(Object bean) {
        this.bean = bean;

        Class<?> klass = bean.getClass ();
        resolveActionAnnotations (klass);
    }


    /**
     * Resolve the action metadata from annotations.
     * 
     * @param klass
     *            the class to process.
     */
    protected void resolveActionAnnotations(Class<?> klass) {
        // Extract the action name, using the simple name as a default and
        // checking for an override using the RemoteAction annotation.
        actionName = bean.getClass ().getSimpleName ();
        IActionErrorHandler actionMethodErrorHandler = null;
        TransactionMode actionTransaction = TransactionMode.NONE;

        // Check if the passed class is an error handler.
        if (bean instanceof IActionErrorHandler)
            actionMethodErrorHandler = (IActionErrorHandler) bean;

        // Get the remote action annotation on the bean and process if found.
        RemoteAction remoteActionAnnotation = klass.getAnnotation (RemoteAction.class);
        if (remoteActionAnnotation != null) {
            // Determine if all methods are transactional.
            actionTransaction = remoteActionAnnotation.transaction ();

            // Extract the action name.
            String proposedName = remoteActionAnnotation.name ();
            if (proposedName != null) {
                proposedName = proposedName.trim ();
                if (proposedName.length () > 0)
                    actionName = proposedName;
            }

            // Create the default method error handler if specified.
            if (!remoteActionAnnotation.errorHandler ().isInterface ()) {
                try {
                    actionMethodErrorHandler = remoteActionAnnotation.errorHandler ().getDeclaredConstructor ().newInstance ();
                } catch (Throwable e) {
                    LOG.error ("Failed to create error handler for \"" + klass.getSimpleName () + ", skipping...", e);
                }
            }
        }

        // Extract the methods on the bean and look for the remote method
        // annotation on them.
        LOOP: for (Method method : klass.getMethods ()) {
            RemoteMethod remoteMethodAnnotation = method.getAnnotation (RemoteMethod.class);
            if (remoteMethodAnnotation != null) {
                // Determine the name of the method.
                String methodName = remoteMethodAnnotation.name ();
                if (methodName == null) {
                    methodName = method.getName ();
                } else {
                    methodName = methodName.trim ();
                    if (methodName.length () == 0)
                        methodName = method.getName ();
                }
                if (nameToMethodHandler.containsKey (methodName)) {
                    if (LOG.isWarnEnabled ())
                        LOG.warn ("Duplicate method name \"" + methodName + "\" for \"" + klass.getSimpleName () + ", skipping...");
                    continue LOOP;
                }

                // Determine if is a form handler.
                boolean formHandler = remoteMethodAnnotation.formHandler ();

                // Determine method parameters.
                List<IParameterMetadata> parameterMetadata = new ArrayList<IParameterMetadata> ();
                Class<?> [] parameterTypes = method.getParameterTypes ();
                for (int i = 0, j = 0; i < parameterTypes.length; i++) {
                    Class<?> type = parameterTypes[i];
                    if (HttpServletRequest.class.isAssignableFrom (type))
                        continue;
                    if (HttpSession.class.isAssignableFrom (type))
                        continue;
                    if (RemoteCallRequest.class.isAssignableFrom (type))
                        continue;
                    String name = "arg" + (j++);
                    parameterMetadata.add (new ParameterMetadata (name, type));
                }

                // Obtain validation groups.
                List<Class<?>> validationGroups = new ArrayList<Class<?>> ();
                for (Class<?> validationGroup : remoteMethodAnnotation.validationGroups ())
                    validationGroups.add (validationGroup);

                // Obtain the validation prefix.
                String validationPrefix = remoteActionAnnotation.validationPrefix ();
                if (remoteMethodAnnotation.validationPrefix ().length () > 0)
                    validationPrefix = remoteMethodAnnotation.validationPrefix ();

                // Obtain any error handler.
                IActionErrorHandler errorHandler = actionMethodErrorHandler;
                if (!remoteMethodAnnotation.errorHandler ().isInterface ()) {
                    try {
                        actionMethodErrorHandler = remoteMethodAnnotation.errorHandler ().getDeclaredConstructor().newInstance();
                    } catch (Throwable e) {
                        LOG.error ("Failed to create error handler for method \"" + methodName + "\" on \"" + klass.getSimpleName () + ", skipping...", e);
                    }
                }

                // Check if should be transactional.
                TransactionMode transaction = actionTransaction;
                if (!TransactionMode.NONE.equals (remoteMethodAnnotation.transaction ())) {
                    // Change the transaction to that specified by the method.
                    transaction = remoteMethodAnnotation.transaction ();
                }

                // Construct an associate a handler.
                MethodHandler handler = new MethodHandler (method, methodName, formHandler, parameterMetadata, validationGroups, validationPrefix, transaction, errorHandler);
                getMethodMetadata ().add (handler);
                nameToMethodHandler.put (handler.getMethodName (), handler);
            }
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.com.effacy.jui.rpc.extdirect.IActionHandler#lookupMethodMetadata(com.com.effacy.jui.rpc.extdirect.ExtendedRemoteCallRequest)
     */
    public IMethodMetadata lookupMethodMetadata(ExtendedRemoteCallRequest request) throws InvalidCallRequestException {
        MethodHandler handler = nameToMethodHandler.get (request.getMethod ());
        if (handler == null)
            throw new InvalidCallRequestException (request, "Unknown method \"" + request.getMethod () + "\" for action \"" + request.getAction () + "\".");
        return handler;
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.com.effacy.jui.rpc.extdirect.IActionHandler#process(com.com.effacy.jui.rpc.extdirect.RemoteCallRequest)
     */
    @Override
    public RemoteCallResponse process(ExtendedRemoteCallRequest request) throws Throwable, InvalidCallRequestException {
        MethodHandler handler = nameToMethodHandler.get (request.getMethod ());
        if (handler == null)
            throw new InvalidCallRequestException (request, "Unknown method \"" + request.getMethod () + "\" for action \"" + request.getAction () + "\".");
        return handler.process (request);
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.com.effacy.jui.rpc.extdirect.IActionHandler#processError(com.com.effacy.jui.rpc.extdirect.ExtendedRemoteCallRequest,
     *      java.lang.Throwable)
     */
    @Override
    public RemoteCallResponse processError(ExtendedRemoteCallRequest request, Throwable error) throws InvalidCallRequestException {
        MethodHandler handler = nameToMethodHandler.get (request.getMethod ());
        if (handler == null)
            throw new InvalidCallRequestException (request, "Unknown method \"" + request.getMethod () + "\" for action \"" + request.getAction () + "\".");
        return handler.processError (request, error);
    }


    /**
     * {@inheritDoc}
     *
     * @see com.com.effacy.jui.rpc.extdirect.IActionHandler#isReadOnly(com.com.effacy.jui.rpc.extdirect.ExtendedRemoteCallRequest)
     */
    @Override
    public boolean isReadOnly(ExtendedRemoteCallRequest request) {
        MethodHandler handler = nameToMethodHandler.get (request.getMethod ());
        return (handler != null) && handler.isTransactional () && handler.isTransactionReadOnly ();
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.com.effacy.jui.rpc.extdirect.IActionHandler#retryError(com.com.effacy.jui.rpc.extdirect.ExtendedRemoteCallRequest,
     *      java.lang.Throwable)
     */
    @Override
    public boolean retryError(ExtendedRemoteCallRequest request, Throwable error) {
        MethodHandler handler = nameToMethodHandler.get (request.getMethod ());
        return (handler == null) ? true : handler.retryError (error);
    }

    /**
     * Method handler for processing a method call.
     */
    private class MethodHandler extends MethodMetadata {

        /**
         * The execution method.
         */
        private Method method;

        /**
         * Optional error handler for processing errors from the method.
         */
        private IActionErrorHandler errorHandler;

        /**
         * Construct with a method, method name for mapping and number of
         * declared arguments.
         * 
         * @param method
         *            the method to use.
         * @param methodName
         *            the method name.
         * @param formHandler
         *            if is a form handler.
         * @param parameters
         *            the parameters.
         * @param validationGroups
         *            the validation groups for the action.
         * @param validationPrefix
         *            the validation prefix for the action.
         * @param transactional
         *            if the method requires a transaction.
         * @param errorHandler
         *            optional error handler for the method.
         */
        public MethodHandler(Method method, String methodName, boolean formHandler, List<IParameterMetadata> parameters, List<Class<?>> validationGroups, String validationPrefix, TransactionMode transaction, IActionErrorHandler errorHandler) {
            super (methodName, formHandler, parameters, validationGroups, validationPrefix, transaction);
            this.method = method;
            this.errorHandler = errorHandler;
        }


        /**
         * Process a request against a method.
         * 
         * @param request
         *            the request.
         * @return The response.
         * @throws Throwable
         *             On error.
         * @throws InvalidCallRequestEception
         *             If there was some problem mapping arguments.
         */
        public RemoteCallResponse process(ExtendedRemoteCallRequest request) throws Throwable, InvalidCallRequestException {
            List<Object> argumentList = new ArrayList<Object> ();
            List<Object> passedArguments = new ArrayList<Object> (request.getData ());
            for (Class<?> type : method.getParameterTypes ()) {
                if (HttpServletRequest.class.isAssignableFrom (type))
                    argumentList.add (request.getRequest ());
                else if (HttpSession.class.isAssignableFrom (type))
                    argumentList.add (request.getRequest ().getSession ());
                else if (RemoteCallRequest.class.isAssignableFrom (type))
                    argumentList.add (request);
                else if (passedArguments.isEmpty ())
                    throw new InvalidCallRequestException (request, "Insufficient arguments passed.");
                else
                    argumentList.add (castArgument (passedArguments.remove (0), type));
            }
            if (!passedArguments.isEmpty ())
                throw new InvalidCallRequestException (request, "Too many arguments passed.");
            try {
                try {
                    Object response = method.invoke (bean, argumentList.toArray ());
                    if (response instanceof RemoteCallResponse)
                        return (RemoteCallResponse) response;
                    return new RemoteCallResponse (request, response);
                } catch (InvocationTargetException e) {
                    throw e.getTargetException ();
                } catch (Throwable e) {
                    LOG.error ("Uncaught exception", e);
                    throw new InvalidCallRequestException (request, "Uncaught exception.");
                }
            } catch (ClassCastException e) {
                // Often this occurs as a result of not being able to deserialise one of the
                // arguments. We can be more explicit if one of the arguments in a Map.
                for (Object arg : argumentList) {
                    if (arg instanceof Map) {
                        // Quite possibly a deserialisaion issue and the default deserialisation of an
                        // object is to a map.
                        LOG.error ("Possibly failed to deserialise a class (check that all remote classes have been properly annotated or registered and the server has been updated will all relevant changes).", e);
                        throw new InvalidCallRequestException (request, "Possible deserialisation failed due to unmapped class.");
                    }
                }
                throw new InvalidCallRequestException (request, "Mismatching arguments.");
            } catch (IllegalArgumentException e) {
                LOG.error ("Invalid arguments passed", e);
                throw new InvalidCallRequestException (request, "Invalid arguments passed.");
            } catch (IllegalAccessException e) {
                LOG.error ("Method not accessible", e);
                throw new InvalidCallRequestException (request, "Method not accessible.");
            }
        }


        /**
         * Handles the given error on the request.
         * 
         * @param request
         *            the request to handle the error for.
         * @param error
         *            the error being handled.
         * @return The response to the error.
         * @throws InvalidCallRequestException
         *             If the error could not be handled.
         */
        public RemoteCallResponse processError(ExtendedRemoteCallRequest request, Throwable error) throws InvalidCallRequestException {
            // If there is a target exception thrown from the method
            // implementation, then see if there is an error handler we
            // can use.
            if (errorHandler != null) {
                Object response = errorHandler.handleError (request, error);
                if (response != null) {
                    if (response instanceof RemoteCallResponse)
                        return (RemoteCallResponse) response;
                    return new RemoteCallResponse (request, response);
                }
            }
            if (LOG_REMOTE.isErrorEnabled ())
                LOG_REMOTE.error ("Unhandled expection calling: " + request.toString (), error);
            throw new InvalidCallRequestException (request, "Unable to handle error.");
        }


        /**
         * Determines if the passed error should be subject to a retry (the
         * default is {@code true}).
         * 
         * @param error
         *            the error to test retry for.
         * @return {@code true} if the error should be retried (if there is not
         *         error handler the default is {@code true}).
         */
        public boolean retryError(Throwable error) {
            return (errorHandler == null) ? true : errorHandler.retryError (error);
        }


        /**
         * Attempts to cast an argument to the required type.
         * 
         * @param passedArgument
         *            the argument to cast.
         * @param type
         *            the type of argument.
         * @throws InvalidCallRequestException
         *             If there was a problem.
         */
        private Object castArgument(Object passedArgument, Class<?> type) throws InvalidCallRequestException {
            if (passedArgument == null)
                return passedArgument;

            // Quite often we find that the passed argument is an array list
            // while the type is an array.
            if (type.isArray () && (passedArgument instanceof List<?>)) {
                int len = ((List<?>) passedArgument).size ();
                Object array = Array.newInstance (type.getComponentType (), len);
                Object [] passedArray = ((List<?>) passedArgument).toArray ();
                for (int i = 0; i < len; i++)
                    Array.set (array, i, passedArray[i]);
                return array;
            }

            // Else return the argument directly.
            return passedArgument;
        }
    }

}
