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

import java.time.Duration;
import java.util.List;

import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.MoreLikeThisQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.lang.Nullable;

/**
 * The operations for the
 * <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/search.html">Elasticsearch Document
 * APIs</a>.
 *
 * @author Peter-Josef Meisch
 * @author Sascha Woo
 * @author Hamid Rahimi
 * @since 4.0
 */
public interface SearchOperations {

	// region count

	/**
	 * 返回满足查询的元素数量
	 *
	 * @param query the query to execute
	 * @param index the index to run the query against
	 * @return count
	 */
	default long count(Query query, IndexCoordinates index) {
		return count(query, null, index);
	}

	/**
	 * 返回满足查询的元素数量
	 *
	 * @param query the query to execute
	 * @param clazz the entity clazz used for property mapping and index name extraction
	 * @return count
	 */
	long count(Query query, Class<?> clazz);

	/**
	 * 返回满足查询的元素数量
	 *
	 * @param query the query to execute
	 * @param clazz the entity clazz used for property mapping
	 * @param index the index to run the query against
	 * @return count
	 */
	long count(Query query, @Nullable Class<?> clazz, IndexCoordinates index);

	// endregion

	// region searchOne

	/**
	 * 返回满足查询返回的第一个结果
	 *
	 * @param query the query to execute
	 * @param clazz the entity clazz used for property mapping and indexname extraction
	 * @return the first found object
	 */
	@Nullable
	default <T> SearchHit<T> searchOne(Query query, Class<T> clazz) {
		List<SearchHit<T>> content = search(query, clazz).getSearchHits();
		return content.isEmpty() ? null : content.get(0);
	}

	/**
	 * 返回满足查询的第一个结果
	 *
	 * @param query the query to execute
	 * @param clazz the entity clazz used for property mapping
	 * @param index the index to run the query against
	 * @return the first found object
	 */
	@Nullable
	default <T> SearchHit<T> searchOne(Query query, Class<T> clazz, IndexCoordinates index) {
		List<SearchHit<T>> content = search(query, clazz, index).getSearchHits();
		return content.isEmpty() ? null : content.get(0);
	}

	// endregion

	// region multiSearch

	/**
	 * 返回满足查询（multiGet）的结果列表
	 *
	 * @param queries the queries to execute
	 * @param clazz the entity clazz
	 * @param <T> element return type
	 * @return list of SearchHits
	 * @since 4.1
	 */
	<T> List<SearchHits<T>> multiSearch(List<? extends Query> queries, Class<T> clazz);

	/**
	 * 返回满足查询（multiGet）的结果列表
	 *
	 * @param queries the queries to execute
	 * @param clazz the entity clazz used for property mapping
	 * @param index the index to run the query against
	 * @param <T> element return type
	 * @return list of SearchHits
	 */
	<T> List<SearchHits<T>> multiSearch(List<? extends Query> queries, Class<T> clazz, IndexCoordinates index);

	/**
	 * 返回满足查询（multiGet）的结果列表
	 *
	 * @param queries the queries to execute
	 * @param classes the entity classes
	 * @return list of SearchHits
	 * @since 4.1
	 */
	List<SearchHits<?>> multiSearch(List<? extends Query> queries, List<Class<?>> classes);

	/**
	 * 返回满足查询（multiGet）的结果列表
	 *
	 * @param queries the queries to execute
	 * @param classes the entity classes used for property mapping
	 * @param index the index to run the queries against
	 * @return list of SearchHits
	 */
	List<SearchHits<?>> multiSearch(List<? extends Query> queries, List<Class<?>> classes, IndexCoordinates index);

	/**
	 * 返回满足查询（multiGet）的结果列表
	 *
	 * @param queries the queries to execute
	 * @param classes the entity classes used for property mapping
	 * @param indexes the indexes to run the queries against
	 * @return list of SearchHits
	 * @since 5.1
	 */
	List<SearchHits<?>> multiSearch(List<? extends Query> queries, List<Class<?>> classes,
			List<IndexCoordinates> indexes);

	// endregion

	// region search

	/**
	 * 返回满足查询（search）的结果列表
	 *
	 * @param <T> element return type
	 * @param query the query to execute
	 * @param clazz the entity clazz used for property mapping and index name extraction
	 * @return SearchHits containing the list of found objects
	 */
	<T> SearchHits<T> search(Query query, Class<T> clazz);

	/**
	 * 返回满足查询（search）的结果列表
	 *
	 * @param <T> element return type
	 * @param query the query to execute
	 * @param clazz the entity clazz used for property mapping
	 * @param index the index to run the query against
	 * @return SearchHits containing the list of found objects
	 */
	<T> SearchHits<T> search(Query query, Class<T> clazz, IndexCoordinates index);

	/**
	 * 返回满足查询（search like）的结果列表
	 *
	 * @param <T> element return type
	 * @param query the query to execute
	 * @param clazz the entity clazz used for property mapping and index name extraction
	 * @return SearchHits containing the list of found objects
	 */
	<T> SearchHits<T> search(MoreLikeThisQuery query, Class<T> clazz);

	/**
	 * 返回满足查询（search like）的结果列表
	 *
	 * @param <T> element return type
	 * @param query the query to execute
	 * @param clazz the entity clazz used for property mapping
	 * @param index the index to run the query against
	 * @return SearchHits containing the list of found objects
	 */
	<T> SearchHits<T> search(MoreLikeThisQuery query, Class<T> clazz, IndexCoordinates index);

	// endregion

	/**
	 * 返回满足查询（search）的结果列表（Stream）
	 *
	 * @param <T> element return type
	 * @param query the query to execute
	 * @param clazz the entity clazz used for property mapping and index name extraction
	 * @return a {@link SearchHitsIterator} that wraps an Elasticsearch scroll context that needs to be closed. The
	 *         try-with-resources construct should be used to ensure that the close method is invoked after the operations
	 *         are completed.
	 */
	<T> SearchHitsIterator<T> searchForStream(Query query, Class<T> clazz);

	/**
	 * 返回满足查询（search）的结果列表（Stream）
	 *
	 * @param <T> element return type
	 * @param query the query to execute
	 * @param clazz the entity clazz used for property mapping
	 * @param index the index to run the query against
	 * @return a {@link SearchHitsIterator} that wraps an Elasticsearch scroll context that needs to be closed. The
	 *         try-with-resources construct should be used to ensure that the close method is invoked after the operations
	 *         are completed.
	 */
	<T> SearchHitsIterator<T> searchForStream(Query query, Class<T> clazz, IndexCoordinates index);

	/**
	 * 查询所有文档，需要由具体的客户端进行实现
	 *
	 * @return a query to find all documents
	 * @since 4.3
	 */
	Query matchAllQuery();

	/**
	 * 返回满足 ID 列表的结果，需要由具体的客户端进行实现
	 *
	 * @param ids the list of ids must not be {@literal null}
	 * @return query returning the documents with the given ids
	 * @since 4.3
	 */
	Query idsQuery(List<String> ids);

	/**
	 * 打开 point in time
	 *
	 * @param index the index name(s) to use
	 * @param keepAlive the duration the pit shoult be kept alive
	 * @return the pit identifier
	 * @since 5.0
	 */
	default String openPointInTime(IndexCoordinates index, Duration keepAlive) {
		return openPointInTime(index, keepAlive, false);
	}

	/**
	 * 打开 point in time
	 *
	 * @param index the index name(s) to use
	 * @param keepAlive the duration the pit shoult be kept alive
	 * @param ignoreUnavailable if {$literal true} the call will fail if any of the indices is missing or closed
	 * @return the pit identifier
	 * @since 5.0
	 */
	String openPointInTime(IndexCoordinates index, Duration keepAlive, Boolean ignoreUnavailable);

	/**
	 * 关闭 point in time
	 *
	 * @param pit the pit identifier as returned by {@link #openPointInTime(IndexCoordinates, Duration, Boolean)}
	 * @return {@literal true} on success
	 * @since 5.0
	 */
	Boolean closePointInTime(String pit);
}
