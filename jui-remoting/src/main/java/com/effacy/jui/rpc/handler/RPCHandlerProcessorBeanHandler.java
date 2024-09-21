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
package com.effacy.jui.rpc.handler;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.effacy.jui.rpc.handler.command.ICommandProcessor;
import com.effacy.jui.rpc.handler.query.IQueryProcessor;

/**
 * See {@link RPCHandlerProcessor}.
 * <p>
 * This is a {@link BeanPostProcessor} that will check for the
 * {@link RPCHandlerProcessor} annotation and if found register the bean with
 * the configured {@link Executor}. This provides a convenient mechanism to
 * auto-register processors managed through Spring.
 * <p>
 * In general this should be created as a bean along with the creation of the
 * associated {@link Executor}.
 *
 * @author Jeremy Buckley
 */
public class RPCHandlerProcessorBeanHandler implements BeanPostProcessor {

    /**
     * The executor to add processors to.
     */
    private Executor<?, ?> executor;

    /**
     * Construct with an executor.
     * 
     * @param executor
     *            the executor to add to.
     */
    @Autowired
    @SuppressWarnings("rawtypes")
    public RPCHandlerProcessorBeanHandler(Executor executor) {
        this.executor = executor;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object,
     *      java.lang.String)
     */
    @Override
    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        RPCHandlerProcessor annotation = bean.getClass ().getAnnotation (RPCHandlerProcessor.class);
        if (annotation != null) {
            if (ICommandProcessor.class.isAssignableFrom (bean.getClass ()))
                executor.add ((ICommandProcessor) bean);
            else if (IQueryProcessor.class.isAssignableFrom (bean.getClass ()))
                executor.add ((IQueryProcessor) bean);
        }
        return bean;
    }
}
