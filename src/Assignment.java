import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.Date;
import java.util.logging.Logger;
import org.springframework.data.mongodb.core.query.*;
import static com.mongodb.client.model.Filters.ne;

/**
 * Created by qianyuzhong on 5/2/17.
 */
public class Assignment {
    private JPanel Assignment_Manu_ID_Part;
    private JLabel Manu_ID;
    private JTextField Manuscript_ID_text;
    private JButton findReviewersButton;
    private JButton Add_Reviewer_btn;
    private JComboBox Reviewer_1_Select;
    private JComboBox Reviewer_2_Select;
    private JComboBox Reviewer_3_Select;
    private JLabel Reviewer_1;
    private JLabel Reviewer_2;
    private JLabel Reviewer_3;
    private JPanel Assignment;
    private JPanel Reviewer_List;
    private JPanel Assignment_Stauts;
    private JLabel Assignment_Status;
    private JScrollPane Assignments_List;
    private JButton Assign_btn;
    private JComboBox Status_Select;
    private JPanel Assignment_Status_Part;
    private JTextField Search_text;
    private JButton Search;
    private JComboBox Submitted_Manuscript_Select;
    private List<JComboBox> reviewers;
    private List<String> items;
    private JFrame frame;
    private JTable myTable;
    private Connection con = null;
    private Statement stmt = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet rst = null;
    private int available_reviewer_numbers;
    private int count = 3;
    private MongoDatabase database;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Assignment");
        frame.setContentPane(new Assignment().Assignment);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public Assignment() {
        frame = new JFrame("Assignment");
        frame.setContentPane(Assignment);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setLocation(480, 10);
//
        Search_text.setText("Manuscript ID");
        Reviewer_List.setVisible(false);
        Add_Reviewer_btn.setVisible(false);
        Assign_btn.setVisible(false);

        Assignment_Status_Part.setVisible(false);

        reviewers = new ArrayList<>();
        items = new ArrayList<>();
//
        reviewers.add(Reviewer_1_Select);
        reviewers.add(Reviewer_2_Select);
        reviewers.add(Reviewer_3_Select);
//
//        con = DatebaseConnection.connection();
        database = DatebaseConnection.connection();
        MongoCollection<Document> manu_cl = database.getCollection("Manuscript");
        FindIterable<Document> rs = manu_cl.find(ne("status","Rejected"));
        for (Document doc : rs) {
            Submitted_Manuscript_Select.addItem(doc.getInteger("idManuscript"));
        }

        MongoCollection<Document> assign_cl = database.getCollection("Assignment");
        MongoCollection<Document> code_cl = database.getCollection("RICode");
        MongoCollection<Document> review_cl = database.getCollection("Reviewer");
        MongoCollection<Document> inter_cl = database.getCollection("interestlist");
//        try {
//            stmt = con.createStatement();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }

//        String manu_submitted = "SELECT * FROM Manuscript WHERE `status` = 'Submitted'";
//        try {
//            rst = stmt.executeQuery(manu_submitted);
//            Submitted_Manuscript_Select.addItem("Submitted Manuscripts");
//            while (rst.next())
//                Submitted_Manuscript_Select.addItem(rst.getInt(1));
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//
        myTable = createTable("ALL");
        Assignments_List.setViewportView(myTable);
//
//        Search.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                myTable = createTable("Search");
//                Assignments_List.setViewportView(myTable);
//            }
//        });
//
        Submitted_Manuscript_Select.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                 Manuscript_ID_text.setText(Submitted_Manuscript_Select.getSelectedItem() + "");
            }
        });
//
        findReviewersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String manuid = Manuscript_ID_text.getText();
                if(manuid.equals("")) {
                    JOptionPane.showMessageDialog(frame,
                            "You must input manuscript ID!");
                    return;
                }
                if(!isNumeric(manuid)) {
                    JOptionPane.showMessageDialog(frame,
                            "Issue ID must be valid!");
                    return;
                }

                int code_int = 0;
                String status = "";
                Document temp_manu = manu_cl.find(new Document("idManuscript", Integer.valueOf(manuid))).first();
                status = temp_manu.getString("status");
                code_int = temp_manu.getDouble("idRICodes").intValue();
                if(!status.equals("Submitted")) {
                    if(status.equals("Under review"))
                        JOptionPane.showMessageDialog(frame, "This manuscript is already under review!");
                    else
                        JOptionPane.showMessageDialog(frame, "This manuscript has already finished reviewing!");
                    return;
                }
//                String code = "SELECT `code`, `status` FROM Manuscript WHERE `idManuscript` = " + manuid;
//                try {
//                    rst = stmt.executeQuery(code);
//                    int code_int = 0;
//                    String status = "";
//                    while (rst.next()) {
//                        code_int = rst.getInt(1);
//                        status = rst.getString(2);
//                    }
//
//                    if(!status.equals("Submitted")) {
//                        if(status.equals("Under review"))
//                            JOptionPane.showMessageDialog(frame, "This manuscript is already under review!");
//                        else
//                            JOptionPane.showMessageDialog(frame, "This manuscript has already finished reviewing!");
//                        return;
//                    }
//
////                    String select = "SELECT `idReviewer` FROM InterestList WHERE `code` = " + code_int;
//                    String select = "SELECT `idReviewer` FROM InterestList WHERE `code` = " + code_int;
//                    String already = "SELECT `idReviewer` FROM Assignment WHERE `idManuscript` = " + manuid;
//                    String interest = "SELECT `interest` FROM RICodes WHERE `code` = " + code_int;
//




                FindIterable<Document> rs_select = inter_cl.find(new Document("idRICodes",code_int));
                for (Document t: rs_select){
                   System.out.println(t.toJson());

                }

                BasicDBObject already = new BasicDBObject();
                already.put("idManuscript",Integer.valueOf(manuid));
                FindIterable<Document> rs_already = assign_cl.find(already);

                Set<Integer> already_assign = new HashSet<Integer>();
                for (Document doc : rs_already){
                    already_assign.add(doc.getInteger("idReviewer"));
                }

                available_reviewer_numbers = 0;
                List<Integer> available_reviewer_list = new ArrayList<>();
                for (Document doc : rs_select){
                    available_reviewer_numbers++;
                    FindIterable<Document> temp = review_cl.find(doc);
//                    for (Document dc : temp) {
                        if (already_assign.add(doc.getInteger("idReviewer"))){
                            available_reviewer_list.add(doc.getInteger("idReviewer"));
                        }
//                    }

                }

                BasicDBObject interest = new BasicDBObject();
                double code_dob = (double) code_int;
                interest.put("idRICodes",code_dob);
                String interest_str = code_cl.find(interest).first().getString("interest");

                if(available_reviewer_numbers < 3) {
                    Reviewer_List.setVisible(false);
                    Add_Reviewer_btn.setVisible(false);
                    Assign_btn.setVisible(false);
                    JOptionPane.showMessageDialog(frame, "Not enough reviewers have interest filed in " + interest_str + "!");
                    return;
                }



//                    rst = stmt.executeQuery(already);
//                    Set<Integer> already_assign = new HashSet<Integer>();
//                    while (rst.next())
//                        already_assign.add(rst.getInt(1));
//
//                    rst = stmt.executeQuery(select);
//                    available_reviewer_numbers = 0;
//                    List<Integer> available_reviewer_list = new ArrayList<>();
//                    while (rst.next()) {
//                        available_reviewer_numbers++;
//                        if(already_assign.add(rst.getInt(1)))
//                            available_reviewer_list.add(rst.getInt(1));
//                    }
//
//                    rst = stmt.executeQuery(interest);
//                    while (rst.next()) {
//                        interest = rst.getString(1);
//                    }
//
//
//                    if(available_reviewer_numbers < 3) {
//                        Reviewer_List.setVisible(false);
//                        Add_Reviewer_btn.setVisible(false);
//                        Assign_btn.setVisible(false);
//                        JOptionPane.showMessageDialog(frame, "Not enough reviewers have interest filed in " + interest + "!");
//
//                        return;
//                    }
//
//                    Reviewer_List.setVisible(true);
//                    Add_Reviewer_btn.setVisible(true);
//                    Assign_btn.setVisible(true);
//                    Reviewer_1_Select.removeAllItems();
//                    Reviewer_2_Select.removeAllItems();
//                    Reviewer_3_Select.removeAllItems();
//                    Reviewer_1_Select.addItem("-");
//                    Reviewer_2_Select.addItem("-");
//                    Reviewer_3_Select.addItem("-");
                Reviewer_List.setVisible(true);
                Add_Reviewer_btn.setVisible(true);
                Assign_btn.setVisible(true);
                Reviewer_1_Select.removeAllItems();
                Reviewer_2_Select.removeAllItems();
                Reviewer_3_Select.removeAllItems();
                Reviewer_1_Select.addItem("-");
                Reviewer_2_Select.addItem("-");
                Reviewer_3_Select.addItem("-");
                for(int i: available_reviewer_list) {
                        FindIterable<Document> rst = review_cl.find(new Document("idReviewer", i));

                        String item = "";
                        for (Document doc : rst)
                            item = item + doc.getInteger("idReviewer") + " " + doc.getString("reviewerFirstName") + " " + doc.getString("reviewerLastName");
                        Reviewer_1_Select.addItem(item);
                        Reviewer_2_Select.addItem(item);
                        Reviewer_3_Select.addItem(item);
                        items.add(item);
                    }

//                    for(int i: available_reviewer_list) {
//                        select = "SELECT `idReviewer`, `reviewerLastName`,`reviewerFirstName` FROM Reviewer WHERE `idReviewer` = " + i;
//                        rst = stmt.executeQuery(select);
//                        String item = "";
//                        while (rst.next())
//                            item = item + rst.getInt(1) + " " + rst.getString(2) + " " + rst.getString(3);
//                        Reviewer_1_Select.addItem(item);
//                        Reviewer_2_Select.addItem(item);
//                        Reviewer_3_Select.addItem(item);
//                        items.add(item);
//                    }
//
//                } catch (SQLException e1) {
//                    e1.printStackTrace();
//                }
            }
        });
//
        Add_Reviewer_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox temp = new JComboBox();
                ++count;
                String s = "Reviewer #" + count;

                JLabel temp2 = new JLabel(s);
                reviewers.add(temp);
                for(String item: items) temp.addItem(item);
//                Reviewer_List.setLayout(new GridBagLayout(2,2));
                Reviewer_List.add(temp2);
                Reviewer_List.add(temp);
            }
        });
//
        Assign_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String manuid = Manuscript_ID_text.getText();

//                String numberReviews = "SELECT COUNT(*) FROM Assignment WHERE `idManuscript` = " + manuid;
                int need_number = 0;
                long rs = assign_cl.count(new Document("idManuscript", Integer.valueOf(manuid)));

                need_number = 3 - (int)rs;

//                try {
//                    rst = stmt.executeQuery(numberReviews);
//                    while (rst.next()) {
//                        need_number = 3 - rst.getInt(1);
//                    }
//                } catch (SQLException e1) {
//                    e1.printStackTrace();
//                }
//
//

                Set<String> set = new HashSet<String>();
                for(JComboBox jb: reviewers)
                    if(!((String) jb.getSelectedItem()).equals("-"))
                        set.add((String) jb.getSelectedItem());

                if(set.size() < need_number) {
                    JOptionPane.showMessageDialog(frame, "You must select enough different reviewers!");
                    return;
                }


                String message = "Manuscript " +  Manuscript_ID_text.getText() + " has been assign to Reviewers ";

                for(String s: set) {
                    String[] array = s.split("\\s+");
                    Document document = new org.bson.Document();
                    document.append("idManuscript",Integer.valueOf(Manuscript_ID_text.getText()));
                    document.append("idReviewer", Integer.valueOf(array[0]));
                    Date date = new java.util.Date();
                    document.append("assignDate", date);

                    assign_cl.insertOne(document);
                    message += array[0] + ", ";
                }

//                Set<String> set = new HashSet<String>();
//                for(JComboBox jb: reviewers)
//                    if(!((String) jb.getSelectedItem()).equals("-"))
//                        set.add((String) jb.getSelectedItem());
//
//                if(set.size() < need_number) {
//                    JOptionPane.showMessageDialog(frame, "You must select enough different reviewers!");
//                    return;
//                }
//                String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
//                String message = "Manuscript " +  Manuscript_ID_text.getText() + " has been assign to Reviewers ";
//                for(String s: set) {
//                    String[] array = s.split("\\s+");
//                    String insert = "INSERT INTO Assignment (`idManuscript`,`idReviewer`,`assignDate`) VALUES (" +
//                            Manuscript_ID_text.getText() + ", " + array[0] + ", '" + timeStamp+ "')";
//
//                    message += array[0] + ", ";
//                    try {
//                        stmt.execute(insert);
//                    } catch (SQLException e1) {
//                        e1.printStackTrace();
//                    }
//                }

                Document select_doc = new Document("idManuscript", Integer.valueOf(Manuscript_ID_text.getText()));
                Document update_doc = new Document();
                update_doc.append("$set", new BasicDBObject().append("status", "Under Review"));
                manu_cl.findOneAndUpdate(select_doc, update_doc);

//                String update = "UPDATE Manuscript SET `status` = 'Under review' WHERE `idManuscript` = " + Manuscript_ID_text.getText();
//                try {
//                    stmt.execute(update);
//                } catch (SQLException e1) {
//                    e1.printStackTrace();
//                }
                myTable = createTable((String)Status_Select.getSelectedItem());
                Assignments_List.setViewportView(myTable);
                Reviewer_List.setVisible(false);
                Add_Reviewer_btn.setVisible(false);
                Assign_btn.setVisible(false);
//
//                try {
//                    rst = stmt.executeQuery(manu_submitted);
//                    Submitted_Manuscript_Select.removeAllItems();
//                    Submitted_Manuscript_Select.addItem("Submitted Manuscripts");
//                    while (rst.next())
//                        Submitted_Manuscript_Select.addItem(rst.getInt(1));
//                } catch (SQLException e1) {
//                    e1.printStackTrace();
//                }
//
                message = message.substring(0, message.length() - 2) + "!";
                JOptionPane.showMessageDialog(frame, message);
            }
        });
//
        Status_Select.addItem("ALL");
        Status_Select.addItem("Received all feedback");
        Status_Select.addItem("Wait for feedback");
        Status_Select.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                myTable = createTable((String)Status_Select.getSelectedItem());
                Assignments_List.setViewportView(myTable);
            }
        });
//
//        Reviewer_1_Select.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//
//            }
//        });
//        Reviewer_2_Select.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//
//            }
//        });
//        Reviewer_3_Select.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//
//            }
//        });
    }
//
    public JTable createTable(String status){
//        Logger logger = Logger.getLogger( Assignment.class.getName() );
//
//        String sql = null;
//        if(status.equals("ALL"))
//            sql = "SELECT * FROM ReviewStatus";
//        else if(status.equals("Received all feedback"))
//            sql = "SELECT * FROM ReviewStatus WHERE `appropriateness` IS NOT NULL ORDER BY `idManuscript`";
//        else if(status.equals("Search"))
//            sql = constructSql();
//        else
//            sql = "SELECT * FROM ReviewStatus WHERE `appropriateness` IS NULL ORDER BY `idManuscript`";
//        DefaultTableModel dtm = buildTableModel(stmt, sql);
//
        String[] content = new String[] {"idManuscript", "title", "idAuthor", "idRICodes", "date", "status", "typesetPages"};

        DefaultTableModel dtm = buildTableModel(status, content);

        JTable table = new JTable(dtm);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        return table;
//        JTable table = new JTable(dtm);
//        table.setFillsViewportHeight(true);
//        table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
//        return table;
    }
//
//    public static DefaultTableModel buildTableModel (Statement stmt, String sql){
//        Vector<String> columnNames = new Vector<>();
//        Vector<Vector<Object>> data = new Vector<>();
//        try {
//            ResultSet rs = stmt.executeQuery(sql);
//
//            ResultSetMetaData metaData = rs.getMetaData();
//
//            int columnCount = metaData.getColumnCount();
//            for (int column = 1; column <= columnCount; column++) {
//                columnNames.add(metaData.getColumnName(column));
//            }
//            while (rs.next()) {
//                Vector<Object> vector = new Vector<Object>();
//                for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
//                    vector.add(rs.getObject(columnIndex));
//                }
//                data.add(vector);
//            }
//
//        } catch (java.sql.SQLException sqle){
//            sqle.printStackTrace();
//        }
//        return new DefaultTableModel(data, columnNames);
//    }

    public DefaultTableModel buildTableModel (String status, String[] content ){
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
            rs = manuscript_cl.find(ne("status","Rejected"));
        else {
            org.bson.conversions.Bson filter = com.mongodb.client.model.Filters.or(
                    com.mongodb.client.model.Filters.eq("status", "Submitted"),
                    ne("feedback", null)
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
//
//    public String constructSql() {
//        String sql = "SELECT * FROM ReviewStatus";
//        String manuid = Search_text.getText();
//        if(manuid.equals("")) {
//            JOptionPane.showMessageDialog(frame,
//                    "You must input Issue ID!");
//            return sql;
//        }
//        if(!isNumeric(manuid)) {
//            JOptionPane.showMessageDialog(frame,
//                    "Issue ID must be valid!");
//            return sql;
//        }
//        sql = sql + " WHERE `idManuscript` = " + manuid;
//
//        return sql;
//    }
//
    public static boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

}
