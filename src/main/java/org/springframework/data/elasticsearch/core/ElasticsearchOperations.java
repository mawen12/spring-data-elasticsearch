/*
 * Copyright 2013-2023 the original author or authors.
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

import java.util.Objects;

import org.springframework.data.elasticsearch.core.cluster.ClusterOperations;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.routing.RoutingResolver;
import org.springframework.data.elasticsearch.core.script.ScriptOperations;
import org.springframework.lang.Nullable;

/**
 * ElasticsearchOperations. Since 4.0 this interface only contains common helper functions, the other methods have been
 * moved to the different interfaces that are extended by ElasticsearchOperations. The interfaces now reflect the
 * <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/rest-apis.html">REST API structure</a> of
 * Elasticsearch.
 *
 * @author Rizwan Idrees
 * @author Mohsin Husen
 * @author Kevin Leturc
 * @author Zetang Zeng
 * @author Dmitriy Yakovlev
 * @author Peter-Josef Meisch
 */
public interface ElasticsearchOperations extends DocumentOperations, SearchOperations, ScriptOperations {

	/**
	 * 返回给定类上绑定的索引操作
	 *
	 * @return IndexOperations
	 */
	IndexOperations indexOps(Class<?> clazz);

	/**
	 * 返回给定索引上绑定的索引操作
	 *
	 * @return IndexOperations
	 */
	IndexOperations indexOps(IndexCoordinates index);

	/**
	 * 返回与当前对象相同配置的集群操作
	 *
	 * @return ClusterOperations implementation
	 * @since 4.2
	 */
	ClusterOperations cluster();

	/**
	 * 返回 ES 转换器
	 */
	ElasticsearchConverter getElasticsearchConverter();

	/**
	 * 返回给定类上绑定的索引协调节点
	 */
	IndexCoordinates getIndexCoordinatesFor(Class<?> clazz);

	/**
	 * 返回给定可能定义为连接类型关系的实体的路由
	 *
	 * @param entity the entity
	 * @return the routing, may be null if not set.
	 * @since 4.1
	 */
	@Nullable
	String getEntityRouting(Object entity);

	// region helper
	/**
	 * gets the String representation for an id.
	 *
	 * @param id
	 * @return String representation
	 * @since 4.0
	 * @deprecated since 5.0, use {@link ElasticsearchOperations#convertId(Object)}.
	 */
	@Deprecated
	@Nullable
	default String stringIdRepresentation(@Nullable Object id) {
		return Objects.toString(id, null);
	}

	/**
	 * 将给定转换为字符串表示，默认实现调用
	 * {@link ElasticsearchConverter#convertId(Object)}
	 *
	 * @param idValue the value to convert
	 * @return the converted value or {@literal null} if idValue is null
	 * @since 5.0
	 */
	@Nullable
	default String convertId(@Nullable Object idValue) {
		return idValue != null ? getElasticsearchConverter().convertId(idValue) : null;
	}
	// endregion

	// region routing
	/**
	 * 返回与当前示例除 {@link RoutingResolver} 以外一样的示例
	 *
	 * @param routingResolver the {@link RoutingResolver} value, must not be {@literal null}.
	 * @return DocumentOperations instance
	 * @since 4.2
	 */
	ElasticsearchOperations withRouting(RoutingResolver routingResolver);
	// endregion
}
