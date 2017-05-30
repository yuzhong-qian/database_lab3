/**
 * Created by Illyria on 4/30/17.
 */

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.*;
import java.util.logging.*;
import java.awt.event.*;
import com.mongodb.client.*;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

public class Author {

    public static final Logger logger = Logger.getLogger( Author.class.getName() );
    public static javax.swing.JFrame frame = new javax.swing.JFrame("author_welcome");
    private static JTable myTable;
    private javax.swing.JPanel Welcome_Part;
    private javax.swing.JLabel Welcome;
    private javax.swing.JButton signoutButton;
    private javax.swing.JButton submitNewButton;
    private javax.swing.JButton checkStausButton;
    private javax.swing.JPanel OpSection;
    private javax.swing.JPanel Author;
    private javax.swing.JScrollPane statusScroll;
    private javax.swing.JButton retractButton;
    private javax.swing.JComboBox retractBox;

    private static Statement stmt = null;
    private static Connection conn = null;
    public final static String QUERY = "SELECT idManuscript, title FROM Manuscript WHERE idAuthor =";

    private static List<String> titles;
    private static List<Integer> ids;
    public static int authorID;

    private MongoDatabase database;



    public static void main(String[] args) {
        JFrame frame = new JFrame("Author");
        frame.setContentPane(new Author().Author);
        frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

    }



    public Author(){

    }
//
    public Author(int idAuthor){
// Set frame
        frame = new JFrame("Author");
        frame.setContentPane(Author);
        frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setLocation(400, 0);

        database = DatebaseConnection.connection();

        MongoCollection<org.bson.Document> editor_cl = database.getCollection("Author");
        Document editor_dc = editor_cl.find(eq("idAuthor", idAuthor)).first();
        MongoCollection<Document> manuscript_cl = database.getCollection("Manuscript");

        String firstName = editor_dc.getString("authorFirstName");
        String lastName = editor_dc.getString("authorLastName");
        String addr = editor_dc.getString("mailAddress");

        String name = firstName +" "+ lastName + " from " +addr;

        Welcome.setText("Welcome! " + name );
//
        myTable = createTable("ALL",idAuthor);
        statusScroll.setViewportView(myTable);
//
        getManuBox(idAuthor);
        checkStausButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getManuStatus(idAuthor);
                getManuBox(idAuthor);
            }
        });
//
        submitNewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new submit(idAuthor, name, database);

            }
        });
        signoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                new Login();
            }
        });



////        ResultSet res  = null;
////        try {
////            res = stmt.executeQuery(QUERY + idAuthor);
////
////            // iterate through results
////            titles = new ArrayList<>();
////            ids = new ArrayList<>();
////            while(res.next()) {
////                ids.add(res.getInt(1));
////                titles.add(res.getString(2));
////                retractBox.addItem(res.getInt(1) + " " + res.getString(2));
////            }
////        }catch(java.sql.SQLException e){
////            e.printStackTrace();
////        }
//        getManuBox(idAuthor);
//
//
//
//
        retractButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int manuId = getManuId((String)retractBox.getSelectedItem());
//                String jurge = "SELECT `status` FROM Manuscript WHERE `idManuscript` = " + id;
//                ResultSet res = null;
//                try {
//                    res = stmt.executeQuery(jurge);
//                    String status = "";
//                    while (res.next())
//                        status = res.getString(1);
//                    if(status.equals("Scheduled for publication") || status.equals("Published")) {
//                        JOptionPane.showMessageDialog(frame,
//                                "You cannot retract your manuscript at this status!");
//                        return;
//                    }
//                } catch (SQLException e1) {
//                    e1.printStackTrace();
//                }
                MongoCollection<org.bson.Document> manu = database.getCollection("Manuscript");
                Document manu_doc = manu.find(eq("idManuscript", manuId)).first();
                if (manu_doc.getString("status").equals("Scheduled for publication")||manu_doc.getString("status").equals("Published")){
                    JOptionPane.showMessageDialog(frame,
                                "You cannot retract your manuscript at this status!");
                        return;
                }
//
                int flag = JOptionPane.showConfirmDialog(frame,
                        "Are you sure to retract this manuscript" + retractBox.getSelectedItem() + "?");

                if(flag == 0) {
                    retractManu(manuId);
                    myTable = createTable("ALL", authorID);
                    statusScroll.setViewportView(myTable);
                    getManuBox(idAuthor);
                }
            }
        });
//
    }
//

    public JTable createTable(String status, int id){
        Logger logger = Logger.getLogger( Author.class.getName() );
        String[] content = new String[] {"idManuscript", "title", "idAuthor", "idRICodes", "date", "typesetPages"};

        DefaultTableModel dtm = buildTableModel(status, content,id);

        JTable table = new JTable(dtm);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        return table;
    }

    public DefaultTableModel buildTableModel (String status, String[] content , int idAuthor){
        Vector<String> columnNames = new Vector<>();
        Vector<Vector<Object>> data = new Vector<>();

        int columnCount = content.length;
        for (int column = 0; column < columnCount; column++)
            columnNames.add(content[column]);

        if(status.equals("ALL"))
            columnNames.add("");

        MongoCollection<Document> manuscript_cl = database.getCollection("Manuscript");
        FindIterable<Document> rs = null;
        if(status.equals("ALL"))
            rs = manuscript_cl.find(new Document("idAuthor", idAuthor));
        else {
            org.bson.conversions.Bson filter = com.mongodb.client.model.Filters.or(
                    com.mongodb.client.model.Filters.eq("idAuthor", idAuthor),
                    com.mongodb.client.model.Filters.ne("feedback", null)
            );
            rs = manuscript_cl.find(filter);
        }


        for(Document d: rs) {
            Vector<Object> vector = new Vector<Object>();
            for(String s: content) {
                vector.add(d.get(s));
            }
//            if(status.equals("ALL"))
//                vector.add("Feedback");
            data.add(vector);
        }

        return new DefaultTableModel(data, columnNames);
    }


    public void retractManu(int id){
//        ResultSet res  = null;
//        try {
//            String sql = "DELETE from Manuscript where idManuscript = ?";
//            PreparedStatement preparedStmt = conn.prepareStatement(sql);
//            preparedStmt.setInt(1, id);
//
//            // execute the preparedstatement
//            preparedStmt.execute();
//            JTable myTable = createTable(authorID);
//            statusScroll.setViewportView(myTable);
//
//        }catch (Exception e){
//            logger.log(Level.SEVERE, "Exception in retract Manu!");
//            e.printStackTrace();
//        }
        MongoCollection<Document> manu = database.getCollection("Manuscript");
        manu.findOneAndDelete(new org.bson.Document("idManuscript",id));


    }
//

//
//    public static JTable createTable2(int idAuthor){
//
//
//
//        DefaultTableModel dtm = new DefaultTableModel();
//        try {
//            stmt = conn.createStatement();
//            String sql = "SELECT title, idManuscript, authorList, code, status, date, typesetPages FROM Manuscript WHERE idAuthor=" + idAuthor ;
//            dtm = buildTableModel(stmt, sql);
//
//        }catch (Exception e){
//            logger.log(Level.SEVERE, "Exception in createTable2.");
//            e.printStackTrace();
//        }
//
//        JTable table = new JTable(dtm);
//        table.setFillsViewportHeight(true);
//        table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
//        return table;
//    }
//

    public JTable createTable2(String status, int id){
        String[] content = new String[] {"idManuscript", "title", "idAuthor", "idRICodes", "date", "status"};

        DefaultTableModel dtm = buildTableModel(status, content,id);

        JTable table = new JTable(dtm);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        return table;
    }


//
    public void getManuStatus(int idAuthor) {
        JTable myTable = createTable2("ALL", idAuthor);
        statusScroll.setViewportView(myTable);
    }
//
    public int getManuId(String s){
        String[] array = s.split("\\s+");
        return Integer.parseInt(array[1].substring(0, array[1].length() - 1));
    }
//
    public void getManuBox(int idAuthor){
        retractBox.removeAllItems();
        MongoCollection<Document> manuscript_cl = database.getCollection("Manuscript");
        FindIterable<Document> rs = manuscript_cl.find(eq("idAuthor", idAuthor));
        titles = new ArrayList<>();
        ids = new ArrayList<>();
        for (Document doc : rs) {
            ids.add(doc.getInteger("idManuscript"));
            titles.add(doc.getString("title"));
            String s = "ID: " + doc.getInteger("idManuscript") + ", " + doc.getString("title") ;
            if (s.length()>20){
                s = s.substring(0,20)+"...";
            }
            retractBox.addItem(s);
        }

//        ResultSet res  = null;
//        try {
//            res = stmt.executeQuery(QUERY + idAuthor);
//
//            // iterate through results
//            titles = new ArrayList<>();
//            ids = new ArrayList<>();
//            while(res.next()) {
//                ids.add(res.getInt(1));
//                titles.add(res.getString(2));
//                String s = "ID: " + res.getInt(1) + ", " + res.getString(2) ;
//
//                String temp = s;
//                if (s.length()>20) {
//                    temp = s.substring(0,25) + "...";
//                }
//
//                retractBox.addItem( temp);
//            }
//        }catch(java.sql.SQLException e){
//            e.printStackTrace();
//        }
    }

}
