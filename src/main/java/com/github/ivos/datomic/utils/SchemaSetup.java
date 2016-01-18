package com.github.ivos.datomic.utils;

import datomic.Connection;
import datomic.Peer;

public class SchemaSetup {

	public void setupSchema(Connection connection) {
		IO.transactAllFromResource(connection, "db/sample-schema.edn");
	}

	public static void main(String[] args) throws Exception {
		String uri = "datomic:mem://db";
		Peer.createDatabase(uri);
		Connection connection = Peer.connect(uri);

		SchemaSetup schemaSetup = new SchemaSetup();
		schemaSetup.setupSchema(connection);

		new SchemaPrinter().printAttributeSchema(connection.sync().get());
	}

}
