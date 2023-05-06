/*
 * Copyright 2019-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.elasticsearch.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.index.*;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.lang.Nullable;

/**
 * The operations for the
 * <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices.html">Elasticsearch Index APIs</a>.
 * <br/>
 * IndexOperations are bound to an entity class or an IndexCoordinate by
 * {@link ElasticsearchOperations#indexOps(IndexCoordinates)} or {@link ElasticsearchOperations#indexOps(Class)}
 *
 * @author Peter-Josef Meisch
 * @author Sascha Woo
 * @author George Popides
 * @since 4.0
 */
public interface IndexOperations {

	// region 索引管理
	/**
	 * 创建单个索引
	 *
	 * @return {@literal true} if the index was created
	 */
	boolean create();

	/**
	 * 使用给定配置创建单个索引
	 *
	 * @param settings the index settings
	 * @return {@literal true} if the index was created
	 */
	boolean create(Map<String, Object> settings);

	/**
	 * 使用给定配置和索引 Mapping 来创建单个索引
	 *
	 * @param settings the index settings
	 * @param mapping the index mapping
	 * @return {@literal true} if the index was created
	 * @since 4.2
	 */
	boolean create(Map<String, Object> settings, Document mapping);

	/**
	 * 使用当前对象绑定到实体上的配置和 Mapping 来创建单个索引
	 *
	 * @return {@literal true} if the index was created
	 * @since 4.2
	 */
	boolean createWithMapping();

	/**
	 * 删除当前对象绑定的单个索引
	 *
	 * @return {@literal true} if the index was deleted
	 */
	boolean delete();

	/**
	 * 校验当前对象绑定的索引是否存在
	 *
	 * @return {@literal true} if the index exists
	 */
	boolean exists();

	/**
	 * 刷新当前对象绑定的一个或多个索引
	 */
	void refresh();
	// endregion

	// region 映射
	/**
	 * 使用当前对象绑定到实体上的配置来创建索引 Mapping
	 *
	 * @return mapping object
	 */
	Document createMapping();

	/**
	 * 使用给定类创建索引 Mapping
	 *
	 * @param clazz the clazz to create a mapping for
	 * @return mapping object
	 */
	Document createMapping(Class<?> clazz);

	/**
	 * 使用当前对象绑定的类上的配置，添加索引 Mapping
	 *
	 * @return {@literal true} if the mapping could be stored
	 * @since 4.1
	 */
	default boolean putMapping() {
		return putMapping(createMapping());
	}

	/**
	 * 添加单个 Mapping 到索引上
	 *
	 * @param mapping the Document with the mapping definitions
	 * @return {@literal true} if the mapping could be stored
	 */
	boolean putMapping(Document mapping);

	/**
	 * 使用给定类上的配置创建索引 Mapping，并添加到索引上
	 *
	 * @param clazz the clazz to create a mapping for
	 * @return {@literal true} if the mapping could be stored
	 * @since 4.1
	 */
	default boolean putMapping(Class<?> clazz) {
		return putMapping(createMapping(clazz));
	}

	/**
	 * 获取类上定义索引的 Mapping
	 *
	 * @return the mapping
	 */
	Map<String, Object> getMapping();

	// endregion

	// region settings
	/**
	 * 使用当前对象绑定的实体的配置创建索引的配置
	 *
	 * @return a settings document.
	 * @since 4.1
	 */
	Settings createSettings();

	/**
	 * 使用指定类上的注解创建索引的配置
	 *
	 * @param clazz the class to create the index settings from
	 * @return a settings document.
	 * @since 4.1
	 */
	Settings createSettings(Class<?> clazz);

	/**
	 * 获取索引的配置
	 *
	 * @return the settings
	 */
	Settings getSettings();

	/**
	 * 获取索引的配置
	 *
	 * @param includeDefaults whether or not to include all the default settings
	 * @return the settings
	 */
	Settings getSettings(boolean includeDefaults);
	// endregion

	// region aliases
	/**
	 * 执行给定的 {@link AliasActions}
	 *
	 * @param aliasActions the actions to execute
	 * @return if the operation is acknowledged by Elasticsearch
	 * @since 4.1
	 */
	boolean alias(AliasActions aliasActions);

	/**
	 * 获取给定别名的信息
	 *
	 * @param aliasNames alias names, must not be {@literal null}
	 * @return a {@link Map} from index names to {@link AliasData} for that index
	 * @since 4.1
	 */
	Map<String, Set<AliasData>> getAliases(String... aliasNames);

	/**
	 * 获取给定别名的信息
	 *
	 * @param indexNames index names, must not be {@literal null}
	 * @return a {@link Map} from index names to {@link AliasData} for that index
	 * @since 4.1
	 */
	Map<String, Set<AliasData>> getAliasesForIndex(String... indexNames);
	// endregion

	// region templates
	/**
	 * Creates an index template using the legacy Elasticsearch interface (@see
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-templates-v1.html).
	 *
	 * @param putTemplateRequest template request parameters
	 * @return true if successful
	 * @since 4.1
	 * @deprecated since 5.1, as the underlying Elasticsearch API is deprecated.
	 */
	@Deprecated
	boolean putTemplate(PutTemplateRequest putTemplateRequest);

	/**
	 * 创建一个索引模板
	 *
	 * @param putIndexTemplateRequest template request parameters
	 * @return {@literal true} if successful
	 * @since 5.1
	 */
	boolean putIndexTemplate(PutIndexTemplateRequest putIndexTemplateRequest);

	/**
	 * 向可组合的索引模板中添加单个组件索引模板
	 *
	 * @param putComponentTemplateRequest index template request parameters
	 * @return {@literal true} if successful
	 * @since 5.1
	 */
	boolean putComponentTemplate(PutComponentTemplateRequest putComponentTemplateRequest);

	/**
	 * 校验单个组件索引模板是否存在
	 *
	 * @param existsComponentTemplateRequest the parameters for the request
	 * @return {@literal true} if the componentTemplate exists.
	 * @since 5.1
	 */
	boolean existsComponentTemplate(ExistsComponentTemplateRequest existsComponentTemplateRequest);

	/**
	 * 获取组件索引模板
	 *
	 * @param getComponentTemplateRequest parameters for the request, may contain wildcard names
	 * @return the found {@link TemplateResponse}s, may be empty
	 * @since 5.1
	 */
	List<TemplateResponse> getComponentTemplate(GetComponentTemplateRequest getComponentTemplateRequest);

	/**
	 * 删除给定的组件索引模板
	 *
	 * @param deleteComponentTemplateRequest request parameters
	 * @return {@literal true} if successful.
	 * @since 5.1
	 */
	boolean deleteComponentTemplate(DeleteComponentTemplateRequest deleteComponentTemplateRequest);

	/**
	 * gets an index template using the legacy Elasticsearch interface.
	 *
	 * @param templateName the template name
	 * @return TemplateData, {@literal null} if no template with the given name exists.
	 * @since 4.1
	 * @deprecated since 5.1, as the underlying Elasticsearch API is deprecated.
	 */
	@Deprecated
	@Nullable
	default TemplateData getTemplate(String templateName) {
		return getTemplate(new GetTemplateRequest(templateName));
	}

	/**
	 * gets an index template using the legacy Elasticsearch interface.
	 *
	 * @param getTemplateRequest the request parameters
	 * @return TemplateData, {@literal null} if no template with the given name exists.
	 * @since 4.1
	 * @deprecated since 5.1, as the underlying Elasticsearch API is deprecated.
	 */
	@Deprecated
	@Nullable
	TemplateData getTemplate(GetTemplateRequest getTemplateRequest);

	/**
	 * check if an index template exists using the legacy Elasticsearch interface.
	 *
	 * @param templateName the template name
	 * @return {@literal true} if the index exists
	 * @since 4.1
	 * @deprecated since 5.1, as the underlying Elasticsearch API is deprecated.
	 */
	@Deprecated
	default boolean existsTemplate(String templateName) {
		return existsTemplate(new ExistsTemplateRequest(templateName));
	}

	/**
	 * check if an index template exists using the legacy Elasticsearch interface.
	 *
	 * @param existsTemplateRequest the request parameters
	 * @return {@literal true} if the index exists
	 * @since 4.1
	 * @deprecated since 5.1, as the underlying Elasticsearch API is deprecated.
	 */
	@Deprecated
	boolean existsTemplate(ExistsTemplateRequest existsTemplateRequest);

	/**
	 * 校验给定的索引模板是否存在
	 *
	 * @param templateName the template name
	 * @return true if the index template exists
	 * @since 5.1
	 */
	default boolean existsIndexTemplate(String templateName) {
		return existsIndexTemplate(new ExistsIndexTemplateRequest(templateName));
	}

	/**
	 * 校验给定的索引模板是否存在
	 *
	 * @param existsTemplateRequest the request parameters
	 * @return true if the index template exists
	 * @since 5.1
	 */
	boolean existsIndexTemplate(ExistsIndexTemplateRequest existsTemplateRequest);

	/**
	 * 获取单个索引模板
	 *
	 * @param templateName template name
	 * @since 5.1
	 */
	default List<TemplateResponse> getIndexTemplate(String templateName) {
		return getIndexTemplate(new GetIndexTemplateRequest(templateName));
	}

	/**
	 * 获取单个索引模板
	 *
	 * @param getIndexTemplateRequest the request parameters
	 * @since 5.1
	 */
	List<TemplateResponse> getIndexTemplate(GetIndexTemplateRequest getIndexTemplateRequest);

	/**
	 * 删除单个索引模板
	 *
	 * @param templateName template name
	 * @return true if successful
	 * @since 5.1
	 */
	default boolean deleteIndexTemplate(String templateName) {
		return deleteIndexTemplate(new DeleteIndexTemplateRequest(templateName));
	}

	/**
	 * 删除单个索引模板
	 *
	 * @param deleteIndexTemplateRequest template request parameters
	 * @return true if successful
	 * @since 5.1
	 */
	boolean deleteIndexTemplate(DeleteIndexTemplateRequest deleteIndexTemplateRequest);

	/**
	 * Deletes an index template using the legacy Elasticsearch interface (@see
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-templates-v1.html).
	 *
	 * @param templateName the template name
	 * @return true if successful
	 * @since 4.1
	 * @deprecated since 5.1, as the underlying Elasticsearch API is deprecated.
	 */
	@Deprecated
	default boolean deleteTemplate(String templateName) {
		return deleteTemplate(new DeleteTemplateRequest(templateName));
	}

	/**
	 * Deletes an index template using the legacy Elasticsearch interface (@see
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-templates-v1.html).
	 *
	 * @param deleteTemplateRequest template request parameters
	 * @return true if successful
	 * @since 4.1
	 * @deprecated since 5.1, as the underlying Elasticsearch API is deprecated.
	 */
	@Deprecated
	boolean deleteTemplate(DeleteTemplateRequest deleteTemplateRequest);

	// endregion

	// region index information
	/**
	 * 获取由 {@link #getIndexCoordinates()} 定义的索引的 {@link IndexInformation}
	 *
	 * @return a list of {@link IndexInformation}
	 * @since 4.2
	 */
	default List<IndexInformation> getInformation() {
		return getInformation(getIndexCoordinates());
	}

	/**
	 * 获取由 #index 定义的索引的 {@link IndexInformation}
	 *
	 * @param index defines the index names to get the information for
	 * @return a list of {@link IndexInformation}
	 * @since 4.2
	 */
	List<IndexInformation> getInformation(IndexCoordinates index);
	// endregion

	// region helper functions
	/**
	 * 获取当前的 {@link IndexCoordinates}，如果实体上使用 SpEL 表达式定义索引名称，数据可能会随时改变。
	 * 如果当前对象未绑定实体，会返回当前对象本身的索引协调点
	 *
	 * @return IndexCoordinates
	 * @since 4.1
	 */
	IndexCoordinates getIndexCoordinates();

	// endregion
}
