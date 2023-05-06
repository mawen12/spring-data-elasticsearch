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
package org.springframework.data.elasticsearch.repository.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.AbstractElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.RefreshPolicy;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.BaseQuery;
import org.springframework.data.elasticsearch.core.query.MoreLikeThisQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.util.StreamUtils;
import org.springframework.data.util.Streamable;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Elasticsearch specific repository implementation. Likely to be used as target within
 * {@link ElasticsearchRepositoryFactory}
 *
 * @author Rizwan Idrees
 * @author Mohsin Husen
 * @author Ryan Henszey
 * @author Kevin Leturc
 * @author Mark Paluch
 * @author Christoph Strobl
 * @author Michael Wirth
 * @author Sascha Woo
 * @author Murali Chevuri
 * @author Peter-Josef Meisch
 * @author Aleksei Arsenev
 * @author Jens Schauder
 */
public class SimpleElasticsearchRepository<T, ID> implements ElasticsearchRepository<T, ID> {

	/**
	 * Elasticsearch 操作，包含索引、文档、脚本
	 */
	protected ElasticsearchOperations operations;

	/**
	 * 索引操作
	 */
	protected IndexOperations indexOperations;

	/**
	 * 实体类
	 */
	protected Class<T> entityClass;

	/**
	 * 实体信息
	 */
	protected ElasticsearchEntityInformation<T, ID> entityInformation;

	// region _initialization
	public SimpleElasticsearchRepository(ElasticsearchEntityInformation<T, ID> metadata,
			ElasticsearchOperations operations) {
		this.operations = operations;

		Assert.notNull(metadata, "ElasticsearchEntityInformation must not be null!");

		this.entityInformation = metadata;
		this.entityClass = this.entityInformation.getJavaType();
		this.indexOperations = operations.indexOps(this.entityClass);

		if (shouldCreateIndexAndMapping() && !indexOperations.exists()) {
			indexOperations.createWithMapping();
		}
	}

	// endregion

	/**
	 * 返回是否创建实体对应的索引和映射
	 */
	private boolean shouldCreateIndexAndMapping() {

		final ElasticsearchPersistentEntity<?> entity = operations.getElasticsearchConverter().getMappingContext()
				.getRequiredPersistentEntity(entityClass);
		return entity.isCreateIndexAndMapping();
	}

	/**
	 * 返回给定id对应的结果（Optional）
	 */
	@Override
	public Optional<T> findById(ID id) {
		return Optional.ofNullable(
				execute(operations -> operations.get(stringIdRepresentation(id), entityClass, getIndexCoordinates())));
	}

	/**
	 * 返回所有结果列表（Iterable）
	 */
	@Override
	public Iterable<T> findAll() {
		int itemCount = (int) this.count();

		if (itemCount == 0) {
			return new PageImpl<>(Collections.emptyList());
		}
		return this.findAll(PageRequest.of(0, Math.max(1, itemCount)));
	}

	@SuppressWarnings("unchecked")
	/**
	 * 返回所有结果列表（Page）
	 */
	@Override
	public Page<T> findAll(Pageable pageable) {

		Assert.notNull(pageable, "pageable must not be null");

		Query query = Query.findAll();
		query.setPageable(pageable);
		SearchHits<T> searchHits = execute(operations -> operations.search(query, entityClass, getIndexCoordinates()));
		SearchPage<T> page = SearchHitSupport.searchPageFor(searchHits, query.getPageable());
		// noinspection ConstantConditions
		return (Page<T>) SearchHitSupport.unwrapSearchHits(page);
	}

	@SuppressWarnings("unchecked")
	/**
	 * 返回所有排序后的结果列表（Iterable）
	 */
	@Override
	public Iterable<T> findAll(Sort sort) {

		Assert.notNull(sort, "sort must not be null");

		int itemCount = (int) this.count();

		if (itemCount == 0) {
			return new PageImpl<>(Collections.emptyList());
		}
		Pageable pageable = PageRequest.of(0, itemCount, sort);
		Query query = Query.findAll();
		query.setPageable(pageable);
		List<SearchHit<T>> searchHitList = execute(
				operations -> operations.search(query, entityClass, getIndexCoordinates()).getSearchHits());
		// noinspection ConstantConditions
		return (List<T>) SearchHitSupport.unwrapSearchHits(searchHitList);
	}

	/**
	 * 返回给定ID列表对应的结果列表（Iterable）
	 */
	@Override
	public Iterable<T> findAllById(Iterable<ID> ids) {

		Assert.notNull(ids, "ids can't be null.");

		List<String> stringIds = stringIdsRepresentation(ids);
		Query query = getIdQuery(stringIds);
		if (!stringIds.isEmpty()) {
			query.setPageable(PageRequest.of(0, stringIds.size()));
		}
		List<SearchHit<T>> searchHitList = execute(
				operations -> operations.search(query, entityClass, getIndexCoordinates()).getSearchHits());
		// noinspection ConstantConditions
		return (List<T>) SearchHitSupport.unwrapSearchHits(searchHitList);
	}

	/**
	 * 获取当前实体的结果数量
	 */
	@Override
	public long count() {
		Query query = Query.findAll();
		((BaseQuery) query).setMaxResults(0);
		return execute(operations -> operations.count(query, entityClass, getIndexCoordinates()));
	}

	/**
	 * 保存单个实体
	 */
	@Override
	public <S extends T> S save(S entity) {

		Assert.notNull(entity, "Cannot save 'null' entity.");

		// noinspection ConstantConditions
		return executeAndRefresh(operations -> operations.save(entity, getIndexCoordinates()));
	}

	/**
	 * 保存多个实体
	 */
	public <S extends T> List<S> save(List<S> entities) {

		Assert.notNull(entities, "Cannot insert 'null' as a List.");

		return Streamable.of(saveAll(entities)).stream().collect(Collectors.toList());
	}

	/**
	 * 保存多个实体
	 */
	@Override
	public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {

		Assert.notNull(entities, "Cannot insert 'null' as a List.");

		IndexCoordinates indexCoordinates = getIndexCoordinates();
		executeAndRefresh(operations -> operations.save(entities, indexCoordinates));

		return entities;
	}

	/**
	 * 返回给定id是否存在对应实体
	 */
	@Override
	public boolean existsById(ID id) {
		// noinspection ConstantConditions
		return execute(operations -> operations.exists(stringIdRepresentation(id), getIndexCoordinates()));
	}

	@SuppressWarnings("unchecked")
	/**
	 * 返回满足查询(search more like this)的结果列表（Page）
	 */
	@Override
	public Page<T> searchSimilar(T entity, @Nullable String[] fields, Pageable pageable) {

		Assert.notNull(entity, "Cannot search similar records for 'null'.");
		Assert.notNull(pageable, "'pageable' cannot be 'null'");

		MoreLikeThisQuery query = new MoreLikeThisQuery();
		query.setId(stringIdRepresentation(extractIdFromBean(entity)));
		query.setPageable(pageable);

		if (fields != null) {
			query.addFields(fields);
		}

		SearchHits<T> searchHits = execute(operations -> operations.search(query, entityClass, getIndexCoordinates()));
		SearchPage<T> searchPage = SearchHitSupport.searchPageFor(searchHits, pageable);
		return (Page<T>) SearchHitSupport.unwrapSearchHits(searchPage);
	}

	/**
	 * 删除给定id对应的文档
	 */
	@Override
	public void deleteById(ID id) {

		Assert.notNull(id, "Cannot delete entity with id 'null'.");

		doDelete(id, getIndexCoordinates());
	}

	/**
	 * 删除给定实体对应的文档
	 */
	@Override
	public void delete(T entity) {

		Assert.notNull(entity, "Cannot delete 'null' entity.");

		doDelete(extractIdFromBean(entity), getIndexCoordinates());
	}

	/**
	 * 删除给定id列表对应的文档
	 */
	@Override
	public void deleteAllById(Iterable<? extends ID> ids) {

		Assert.notNull(ids, "Cannot delete 'null' list.");

		List<String> idStrings = new ArrayList<>();
		for (ID id : ids) {
			idStrings.add(stringIdRepresentation(id));
		}

		if (idStrings.isEmpty()) {
			return;
		}

		Query query = operations.idsQuery(idStrings);
		executeAndRefresh((OperationsCallback<Void>) operations -> {
			operations.delete(query, entityClass, getIndexCoordinates());
			return null;
		});
	}

	/**
	 * 删除给定实体对应的文档
	 */
	@Override
	public void deleteAll(Iterable<? extends T> entities) {

		Assert.notNull(entities, "Cannot delete 'null' list.");

		List<ID> ids = new ArrayList<>();
		for (T entity : entities) {
			ID id = extractIdFromBean(entity);
			if (id != null) {
				ids.add(id);
			}
		}

		deleteAllById(ids);
	}

	/**
	 * 根据id和索引协调点删除文档
	 *
	 * @param id 文档id
	 * @param indexCoordinates 索引协调点
	 */
	private void doDelete(@Nullable ID id, IndexCoordinates indexCoordinates) {

		if (id != null) {
			executeAndRefresh(operations -> operations.delete(stringIdRepresentation(id), indexCoordinates));
		}
	}

	/**
	 * 删除当前实体下所有的文档
	 */
	@Override
	public void deleteAll() {
		IndexCoordinates indexCoordinates = getIndexCoordinates();

		executeAndRefresh((OperationsCallback<Void>) operations -> {
			operations.delete(Query.findAll(), entityClass, indexCoordinates);
			return null;
		});
	}

	/**
	 * 刷新
	 */
	private void doRefresh() {
		RefreshPolicy refreshPolicy = null;

		if (operations instanceof AbstractElasticsearchTemplate) {
			refreshPolicy = ((AbstractElasticsearchTemplate) operations).getRefreshPolicy();
		}

		if (refreshPolicy == null) {
			indexOperations.refresh();
		}
	}

	// region helper functions

	/**
	 * 从实体中提取id
	 */
	@Nullable
	protected ID extractIdFromBean(T entity) {
		return entityInformation.getId(entity);
	}

	/**
	 * 将id集合转换为字符串表示
	 */
	private List<String> stringIdsRepresentation(Iterable<? extends ID> ids) {

		Assert.notNull(ids, "ids can't be null.");

		return StreamUtils.createStreamFromIterator(ids.iterator()).map(this::stringIdRepresentation)
				.collect(Collectors.toList());
	}

	/**
	 * 将单个id转换为字符串表示
	 */
	protected @Nullable String stringIdRepresentation(@Nullable ID id) {
		return operations.convertId(id);
	}

	/**
	 * 获取当前实体对应的索引协调点
	 */
	private IndexCoordinates getIndexCoordinates() {
		return operations.getIndexCoordinatesFor(entityClass);
	}

	/**
	 * 返回给定id对应的查询
	 */
	private Query getIdQuery(List<String> stringIds) {
		return operations.idsQuery(stringIds);
	}
	// endregion

	// region operations callback
	@FunctionalInterface
	public interface OperationsCallback<R> {
		@Nullable
		R doWithOperations(ElasticsearchOperations operations);
	}

	@Nullable
	public <R> R execute(OperationsCallback<R> callback) {
		return callback.doWithOperations(operations);
	}

	@Nullable
	public <R> R executeAndRefresh(OperationsCallback<R> callback) {
		R result = callback.doWithOperations(operations);
		doRefresh();
		return result;
	}
	// endregion
}
