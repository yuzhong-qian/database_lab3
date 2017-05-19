/**
 * Created by qianyuzhong on 4/27/17.
 */

import java.sql.*;
import com.mongodb.MongoClient;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


public class DatebaseConnection {

    public static void main(String[] args) {
        try{
            // 连接到 mongodb 服务
            MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
            // 连接到数据库
            MongoDatabase mongoDatabase = mongoClient.getDatabase("Author");
            System.out.println("Connect to database successfully");
            MongoCollection mc = mongoDatabase.getCollection("ds");

        }catch(Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    public static MongoDatabase connection() {
        MongoDatabase mongoDatabase = null;
        try{
            // 连接到 mongodb 服务
            MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
            // 连接到数据库
            mongoDatabase = mongoClient.getDatabase("Author");
            System.out.println("Connect to database successfully");
            MongoCollection mc = mongoDatabase.getCollection("ds");

        }catch(Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        return mongoDatabase;
    }
}
