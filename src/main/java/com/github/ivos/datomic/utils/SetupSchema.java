package com.github.ivos.datomic.utils;

import datomic.Connection;
import datomic.Peer;

public class SetupSchema {

	public static void setupSchema(Connection conn) {
		IO.transactAllFromResource(conn, "db/sample-schema.edn");
	}

	public static void main(String[] args) throws Exception{
		String uri = "datomic:mem://db";
		Peer.createDatabase(uri);
		Connection conn = Peer.connect(uri);
		setupSchema(conn);

		PrintSchema.printAttributeSchema(conn.sync().get());
	}

}
