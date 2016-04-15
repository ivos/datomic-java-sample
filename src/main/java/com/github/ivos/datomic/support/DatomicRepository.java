package com.github.ivos.datomic.support;

import datomic.Connection;
import datomic.Database;
import datomic.Entity;
import datomic.Peer;
import datomic.Util;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@Slf4j
public class DatomicRepository<E> {

	private final String partition;
	private final Class<E> entityClass;
	private final List<Field> declaredFields;
	private final String attributePrefix;

	@Setter
	protected Connection connection;

	public DatomicRepository(String partition, Class<E> entityClass) {
		this.partition = partition;
		this.entityClass = entityClass;
		declaredFields = Arrays.asList(entityClass.getDeclaredFields());
		attributePrefix = getAttributePrefix(entityClass);
	}

	public E get(Long id) {
		Entity entity = connection.db().entity(id).touch();
		log.debug("Got entity {}", entity);
		if (entity.keySet().size() == 0) {
			throw new EntityNotFoundException("Entity with id " + id + " was not found in the database.");
		}
		return map(entity);
	}

	public E create(E instance) {
		Object tempId = Peer.tempid(partition);
		Map<Object, Object> map = new HashMap<>();
		for (Field field : declaredFields) {
			Object value = getFieldValue(field, instance);
			String name = getFieldAttributeName(field, attributePrefix);
			if ("db/id".equals(name)) {
				value = tempId;
			} else if (name.endsWith("/version")) {
				value = 1L;
			}
			map.put(name, value);
		}
		List tx = Util.list(map);

		Map txResult;
		try {
			txResult = connection.transact(tx).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
		Long id = (Long) Peer.resolveTempid(connection.db(), txResult.get(Connection.TEMPIDS), tempId);

		Database db = (Database) txResult.get(Connection.DB_AFTER);
		Entity entity = db.entity(id).touch();
		return map(entity);
	}

	public E update(E instance) {
		long instanceVersion = 0, id = 0;
		Object versionAttribute = null;
		Map<Object, Object> map = new HashMap<>();
		for (Field field : declaredFields) {
			Object value = getFieldValue(field, instance);
			String name = getFieldAttributeName(field, attributePrefix);
			if (name.endsWith("/version")) {
				instanceVersion = (long) value;
				versionAttribute = name;
			} else {
				map.put(name, value);
				if ("db/id".equals(name)) {
					id = (long) value;
				}
			}
		}
		long newVersion = instanceVersion + 1;
		List tx = Util.list(map,
				Util.list("db.fn/cas", id, versionAttribute, instanceVersion, newVersion)
		);

		Map txResult;
		try {
			txResult = connection.transact(tx).get();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new OptimisticLockException(entityClass.getSimpleName() +
					" data out of date. Please refresh the data before update.");
		}

		Database db = (Database) txResult.get(Connection.DB_AFTER);
		Entity entity = db.entity(id).touch();
		return map(entity);
	}

	public E map(Entity entity) {
		E instance;
		try {
			instance = entityClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("Cannot instantiate " + entityClass.getSimpleName() +
					". Does it have a public no-args constructor?", e);
		}
		for (Field field : declaredFields) {
			Object value = entity.get(getFieldAttributeName(field, attributePrefix));
			setFieldValue(field, instance, value);
		}
		return instance;
	}

	public List<E> list(E query) {
		List<Field> fields = getFilledFields(declaredFields, query);
		String inputClause = getInputClause(fields);
		String whereClause = getWhereClause(fields, attributePrefix);
		List<Object> values = getQueryParameters(fields, query);
		values.add(0, connection.db());
		String edn = getQueryEdn(inputClause, whereClause);
		log.debug("Query EDN {}", edn);

		Set<?> data = (Set) Peer.q(edn, values.toArray());

		return data.stream()
				.sorted()
				.map(instance -> get((Long) ((List) instance).get(0)))
				.collect(toList());
	}

	public static String getAttributePrefix(Class<?> entityClass) {
		String entityClassName = entityClass.getSimpleName();
		return Character.toLowerCase(entityClassName.charAt(0)) + entityClassName.substring(1);
	}

	public static String getFieldAttributeName(Field field, String attributePrefix) {
		String fieldName = field.getName();
		String prefix = ("id".equals(fieldName)) ? "db" : attributePrefix;
		return prefix + "/" + fieldName;
	}

	public static Object getFieldValue(Field field, Object object) {
		try {
			field.setAccessible(true);
			return field.get(object);
		} catch (IllegalAccessException e) {
			return null;
		}
	}

	public static void setFieldValue(Field field, Object object, Object value) {
		try {
			log.debug("Set {} on {} to {}.", field, object, value);
			field.setAccessible(true);
			field.set(object, value);
		} catch (IllegalAccessException | IllegalArgumentException e) {
			throw new RuntimeException("Cannot set " + field + " on " + object + " to " + value, e);
		}
	}

	public static List<Field> getFilledFields(List<Field> fields, Object object) {
		return fields.stream()
				.filter(field -> null != getFieldValue(field, object))
				.collect(toList());
	}

	public static String getFieldInputClause(Field field) {
		return "?" + field.getName();
	}

	public static String getInputClause(List<Field> fields) {
		return fields.stream()
				.map(DatomicRepository::getFieldInputClause)
				.collect(joining(" "));
	}

	public static String getFieldWhereClause(Field field, String attributePrefix) {
		return "[?e " + getFieldAttributeName(field, attributePrefix) + " " + getFieldInputClause(field) + "]";
	}

	public static String getWhereClause(List<Field> fields, String attributePrefix) {
		return fields.stream()
				.map(field -> getFieldWhereClause(field, attributePrefix))
				.collect(joining());
	}

	public static List<Object> getQueryParameters(List<Field> fields, Object query) {
		return fields.stream()
				.map(field -> getFieldValue(field, query))
				.collect(toList());
	}

	public static String getQueryEdn(String inputClause, String whereClause) {
		return "[:find ?e :in $ " + inputClause + " :where " + whereClause + "]";
	}

}
