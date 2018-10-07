/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.github.javaovo.cache.config;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import io.github.javaovo.cache.factory.RedisFactoryBean;

/**
 * @Author: javaovo@163.com
 * @Date: 2018-10-07 21:38:16
 * @Since: 1.0
 */
public class RedisBeanDefinitionParser extends AbstractBeanDefinitionParser {

	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(RedisFactoryBean.class);
		setRedisHome(element, builder);
		return getSourcedBeanDefinition(builder, element, parserContext);
	}

	private void setRedisHome(Element element, BeanDefinitionBuilder builder) {
		builder.addPropertyValue("host", element.getAttribute("host"));
		builder.addPropertyValue("ns", element.getAttribute("ns"));
		builder.addPropertyValue("password", element.getAttribute("password"));
		builder.addPropertyValue("compress", element.getAttribute("compress"));
		builder.addPropertyValue("maxTotal", element.getAttribute("maxTotal"));
	}

	private AbstractBeanDefinition getSourcedBeanDefinition(BeanDefinitionBuilder builder, Element source, ParserContext context) {
		AbstractBeanDefinition definition = builder.getBeanDefinition();
		definition.setSource(context.extractSource(source));
		return definition;
	}
}
