package com.github.ivos.datomic.customer;

import datomic.Connection;
import datomic.Entity;
import datomic.Peer;
import datomic.Util;
import lombok.Setter;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class CustomerRepository {

	@Setter
	private Connection connection;

	public Customer create(Customer customer) {
		Object tempId = Peer.tempid(":sample");
		List tx = Util.list(Util.map(
				":db/id", tempId,
				":customer/name", customer.getName(),
				":customer/email", customer.getEmail(),
				":customer/phone", customer.getPhone()
		));
		Map txResult = null;
		try {
			txResult = connection.transact(tx).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
		Long id = (Long) Peer.resolveTempid(connection.db(), txResult.get(Connection.TEMPIDS), tempId);
		return customer.withId(id);
	}

	public Customer get(Long id) {
		Entity entity = connection.db().entity(id);
		return Customer.builder()
				.id(id)
				.name((String) entity.get(":customer/name"))
				.email((String) entity.get(":customer/email"))
				.phone((String) entity.get(":customer/phone"))
				.build();
	}

	public List<Customer> list(Customer query) {
		List<Field> fields = Arrays.stream(Customer.class.getDeclaredFields())
				.filter(field -> null != getField(field, query))
				.collect(toList());
		String whereClause = fields.stream()
				.map(field -> "[?e :customer/" + field.getName() + " ?" + field.getName() + "]")
				.collect(joining());
		String inputClause = fields.stream()
				.map(field -> "?" + field.getName())
				.collect(joining(" "));
		List<Object> values = fields.stream()
				.map(field -> getField(field, query))
				.collect(toList());
		values.add(0, connection.db());
		String edn = "[:find ?e :in $ " + inputClause + " :where " + whereClause + "]";
		Set<?> data = (Set) Peer.q(edn, values.toArray());
		return data.stream()
				.sorted()
				.map(instance -> get((Long) ((List) instance).get(0)))
				.collect(toList());
	}

	private Object getField(Field field, Object object) {
		try {
			field.setAccessible(true);
			return field.get(object);
		} catch (IllegalAccessException e) {
			return null;
		}
	}

}
