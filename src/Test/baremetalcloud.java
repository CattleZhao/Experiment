package Test;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import JDBC.MongoJDBC;

public class baremetalcloud {
	public static void main(String[] args) {
		MongoJDBC jdbc = new MongoJDBC();
		DB db = jdbc.connection();
		DBCollection collection = db.getCollection("SerStoPro");
		DBCursor cursor = collection.find();
		while(cursor.hasNext()){
			DBObject doc = cursor.next();
			if(doc.get("serviceId").toString().equalsIgnoreCase("alibaba:storage"))
				System.out.println(doc);
		}
	}
	
}
