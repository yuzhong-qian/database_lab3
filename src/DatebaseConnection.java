/**
 * Created by qianyuzhong on 4/27/17.
 */

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.MongoClient;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;


public class DatebaseConnection {

    public static void main(String[] args) {
        try{
            // connect to the mongodb server
            MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
            // get database
            MongoDatabase mongoDatabase = mongoClient.getDatabase("luyang_test2");
            System.out.println("Connect to database successfully");

            MongoCollection<Document> RICode = mongoDatabase.getCollection("RICode");
            FindIterable<Document> result_RICode = RICode.find(eq("idRICodes", 1));

            System.out.println("Outside the loop");
            Document d = new Document();
//            d.append("idRICodes", "getNextRICodeSequence(\"RICode_ID\")");
//            d.append("interest", "music");
            RICode.insertOne(d);
            for(Document document: result_RICode) {
                System.out.println("In the loop");
//                System.out.println(document);
                System.out.println(document.get("interest"));
            }

            MongoCollection<Document> Reviewer = mongoDatabase.getCollection("Reviewer");
            MongoCollection<Document> manuscript_cl = mongoDatabase.getCollection("Manuscript");
            manuscript_cl.updateOne(new Document("idManuscript", 3), new Document("$set", new Document("status", "Submitted")));


            MongoCollection<Document> Issue = mongoDatabase.getCollection("Issue");
            Document issue_dc = Issue.find(new Document("publicationYear", 2017).append("publicationPeriod", 1)).first();
            System.out.println(issue_dc.get("typesetting"));

            issue_dc = Issue.find(new Document("publicationYear", 2017).append("publicationPeriod", 1)).first();

            System.out.println(issue_dc.get("typesetting"));
            ArrayList<Document> typesetting = (ArrayList<Document>) issue_dc.get("typesetting");

            for(Document d2 : typesetting)
                 System.out.println(d2.get("idManuscript"));
//            Issue.updateOne(new BasicDBObject("publicationYear", 2017).append("publicationPeriod", 1), new BasicDBObject("$push", new BasicDBObject("typesetting", new BasicDBObject("idManuscript", 5).append("beginPage",  11).append("order",11))));
//            Issue.updateOne(new BasicDBObject("publicationYear", 2017).append("publicationPeriod", 1), new Document("$set", new Document("pages", 21)));

//            Reviewer.updateOne(eq("idEditor", 1), new BasicDBObject("$push", new BasicDBObject("interestlist", new BasicDBObject("idRICode", 103).append("interest","Astronomy"))));
//
//            List<BasicDBObject> l = new ArrayList<>();
////            l.add(new BasicDBObject("$match", new BasicDBObject("idEditor", 1)));
////            l.add(new BasicDBObject("$unwind", "$interestlist"));
////            l.add(new BasicDBObject("$group", new BasicDBObject("idRICode", new BasicDBObject("_id", null).append("$max", "interestlist.idRICode"))));
////            Document dc = Reviewer.find(eq("idEditor", 1)).sort(new BasicDBObject("interestlist.idRICode", -1)).limit(1).first();
//            Document dc = Reviewer.aggregate(l).first();
//            System.out.println(dc.get("interestlist"));
        }catch(Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    public static MongoDatabase connection() {
        MongoDatabase mongoDatabase = null;
        try{
            // connect to the mongodb server
            MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
            // get database
            mongoDatabase = mongoClient.getDatabase("luyang_test2");
            System.out.println("Connect to database successfully");

        }catch(Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        return mongoDatabase;
    }
}
