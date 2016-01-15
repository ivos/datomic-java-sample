package com.github.ivos.datomic.utils;

import datomic.Connection;
import datomic.Database;
import datomic.Entity;
import datomic.Peer;

import java.util.Collection;
import java.util.List;

public class PrintSchema {

	/**
	 * Print an entity
	 */
	public static void printEntity(Entity entity) {
		for (Object key : entity.keySet()) {
			System.out.println(key + " = " + entity.get(key));
		}
	}

	/**
	 * Print results from a query that returns entities in 1-tuples.
	 *
	 * @param tuples entities in 1-tuples
	 */
	public static void printEntities(Collection<List<Object>> tuples) {
		for (List<Object> tuple : tuples) {
			System.out.println();
			printEntity((Entity) tuple.get(0));
		}
	}

	/**
	 * Schemas are plain data, like everything else. Values of
	 * :db.install/attribute are the attributes defined in the schema.
	 *
	 * @param db database
	 */
	public static void printAttributeSchema(Database db) {
		Collection<List<Object>> tuples =
				Peer.q("[:find ?entity :where " +
						"[_ :db.install/attribute ?v]" +
						"[(.entity $ ?v) ?entity]]", db);
		printEntities(tuples);
	}

	public static void printDatabaseContent(Database db, String dbName) {
		Collection<List<Object>> results =
				Peer.q("[:find ?entity :in $ ?s :where " +
						"[?e :db/valueType]" +
						"[?e :db/ident ?a]" +
						"[(namespace ?a) ?ns]" +
						"[(= ?ns ?s)]" +
						"[(.entity $ ?e) ?entity]]", db, dbName);
		printEntities(results);
	}

	public static void main(String[] args) {
		String uri = "datomic:mem://db";
		Peer.createDatabase(uri);
		Connection conn = Peer.connect(uri);

//		printAttributeSchema(conn.db());
		printDatabaseContent(conn.db(), "db");
	}

}
