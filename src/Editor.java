//import com.sun.deploy.panel.SecurityLevel;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sun.tools.corba.se.idl.InterfaceGen;
import org.bson.Document;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import static com.mongodb.client.model.Filters.eq;

/**
 * Created by qianyuzhong on 5/1/17.
 */
public class Editor {
    private JPanel Editor;
    private JFrame frame;
    private JTable myTable;
    private JLabel Welcome;
    private JScrollPane Manuscripts;
    private JButton Sign_Out;
    private JButton Change_Status_btn;
    private JComboBox Status_Select;
    private JLabel Status;
    private JTextField Manuscript_ID;
    private JComboBox Status_Change_Select;
    private JPanel Operation_Part;
    private JPanel Welcome_Part;
    private JPanel Content_Part;
    private JLabel Operation;
    private JPanel Typesetting_Part;
    private JLabel Pages;
    private JTextField Pages_text;
    private JPanel Issue_and_assignment_part;
    private JButton Issues_btn;
    private JButton Assignment_btn;
    private JLabel Issues;
    private JComboBox Issues_Select;
    private JPanel Issues_Select_Part;
    private JTextField Search_text;
    private JButton Search;
    private javax.swing.JLabel Warning_Message;
    private MongoDatabase database;

    private String[] keyword;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Editor");
        frame.setContentPane(new Editor().Editor);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public Editor() {

    }

    public Editor(int userid) {
        frame = new JFrame("Editor");
        frame.setContentPane(Editor);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setLocation(340, 5);

        Typesetting_Part.setVisible(false);
        Issues_Select_Part.setVisible(false);
        Manuscript_ID.setText("Manuscript ID");

        database = DatebaseConnection.connection();

        MongoCollection<Document> editor_cl = database.getCollection("Editor");
        Document editor_dc = editor_cl.find(eq("idEditor", userid)).first();
        MongoCollection<Document> manuscript_cl = database.getCollection("Manuscript");

        String firstname = editor_dc.getString("EditorFirstName");
        String lastname = editor_dc.getString("EditorLastName");

        Welcome.setText("Welcome, " + firstname + " " + lastname +"!");

        myTable = createTable("ALL");
        Manuscripts.setViewportView(myTable);

        Sign_Out.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                new Login();
            }
        });

        Issues_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Issue();
            }
        });

        Assignment_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Assignment();
            }
        });


        Change_Status_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String id = Manuscript_ID.getText();
                if(id.equals("")) {
                    JOptionPane.showMessageDialog(frame,
                            "You must input your UserID!");
                    return;
                }
                if(!isNumeric(id)) {
                    JOptionPane.showMessageDialog(frame,
                            "UserID must be valid!");
                    return;
                }
                int manuid = Integer.parseInt(id);

                String change_status = (String) Status_Change_Select.getSelectedItem();
                int pages = 0;


                Document manuscript_dc = manuscript_cl.find(eq("idManuscript", manuid)).first();
                String currtent_status = manuscript_dc.getString("status");

                switch (change_status) {
                    case "Rejected":
                        if(currtent_status.equals("Scheduled for publication") || currtent_status.equals("Published")) {
                            JOptionPane.showMessageDialog(frame, "You cannot reject this manuscript at this status!");
                            return;
                        }
                        break;
                    case "Accepted":
                        if(currtent_status.equals("In typesetting") || currtent_status.equals("Scheduled for publication") || currtent_status.equals("Published")) {
                            JOptionPane.showMessageDialog(frame, "This manuscript has already been accepted!");
                            return;
                        }
                        if(currtent_status.equals("Submitted")) {
                            JOptionPane.showMessageDialog(frame, "This manuscript should be first assign to reviewers to review!");
                            return;
                        }
                        break;
                    case "In typesetting":
                        if(!currtent_status.equals("Accepted")) {
                            JOptionPane.showMessageDialog(frame, "This manuscript cannot be typeset! Only the manuscript with accepted status can be typeset!");
                            return;
                        }
                        String s = Pages_text.getText();
                        if(!isNumeric(s)) {
                            JOptionPane.showMessageDialog(frame, "Invaild page number!");
                            return;
                        }
                        pages = Integer.parseInt(s);
                        if(pages <= 0 || pages >= 100) {
                            JOptionPane.showMessageDialog(frame, "Too few or too many page number!");
                            return;
                        }

                        manuscript_cl.updateOne(new Document("idManuscript", manuid), new Document("$set", new Document("typesetPages", pages)));
                        break;
                    case "Scheduled for publication":
                        if(currtent_status.equals("Scheduled for publication")) {
                            JOptionPane.showMessageDialog(frame, "This manuscript has already been typesetting!");
                            return;
                        }
                        if(currtent_status.equals("Published")) {
                            JOptionPane.showMessageDialog(frame, "This manuscript has already been published!");
                            return;
                        }
                        if(!currtent_status.equals("In typesetting")) {
                            JOptionPane.showMessageDialog(frame, "This manuscript should be typesetting first!");
                            return;
                        }

                        pages = manuscript_dc.getInteger("typesetPages");

                        String[] issue = getIssue((String)Issues_Select.getSelectedItem());
                        MongoCollection<Document> Issue = database.getCollection("Issue");

                        Document temp = Issue.find(new BasicDBObject("publicationYear", issue[0]).append("publicationPeriod", issue[1])).first();
                        int cur_page = temp.getInteger("pages");

//                        Issue.updateOne(new BasicDBObject("publicationYear", issue[0]).append("publicationPeriod", issue[1]), new BasicDBObject("$push", new BasicDBObject("typesetting", new BasicDBObject("idManuscript", 6).append("beginPage",1).append("order",1))));
                        Issue.updateOne(new BasicDBObject("publicationYear", issue[0]).append("publicationPeriod", issue[1]), new BasicDBObject("$push", new BasicDBObject("typesetting", new BasicDBObject("idManuscript", manuid).append("beginPage", cur_page + 1).append("order",1))));
                        Issue.updateOne(new BasicDBObject("publicationYear", issue[0]).append("publicationPeriod", issue[1]), new Document("$set", new Document("pages", cur_page + pages)));

//                        String insert = "INSERT Typesetting (`idManuscript`, `publicationYear`,`publicationPeriod`,`beginPage`,`order`) VALUES (?, ?, ?, ?, ?);";
//                        String maxOrder = "SELECT MAX(`order`) FROM Typesetting WHERE `publicationYear` = '" + issue[0] + "' AND `publicationPeriod` = '" + issue[1] +"';";

                        break;
                    default:
                        break;
                }
                manuscript_cl.updateOne(new Document("idManuscript", manuid), new Document("$set", new Document("status", change_status)));

                JOptionPane.showMessageDialog(frame,
                        "Manuscript " + manuid + "'s status has been changed to " + change_status + "!");

                myTable = createTable(change_status);
                Manuscripts.setViewportView(myTable);
            }
        });

        Status_Change_Select.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String change_status = (String) Status_Change_Select.getSelectedItem();

                switch (change_status) {
                    case "Rejected":
                        Typesetting_Part.setVisible(false);
                        Issues_Select_Part.setVisible(false);
                        break;
                    case "Accepted":
                        Typesetting_Part.setVisible(false);
                        Issues_Select_Part.setVisible(false);
                        break;
                    case "In typesetting":
                        Typesetting_Part.setVisible(true);
                        Issues_Select_Part.setVisible(false);
                        break;
                    case "Scheduled for publication":
                        String manuid_s = Manuscript_ID.getText();

                        if(manuid_s.equals("") || !isNumeric(manuid_s)) {
                            manuid_s = JOptionPane.showInputDialog(frame, "Please input a valid manuscript ID first:");
                            if (manuid_s == null || !isNumeric(manuid_s)) {
                                JOptionPane.showMessageDialog(frame,
                                        "ManuID must be valid!");
                                return;
                            }
                            Manuscript_ID.setText(manuid_s);
                        }

                        int manuid = Integer.parseInt(manuid_s);
                        Typesetting_Part.setVisible(false);
                        Issues_Select_Part.setVisible(true);

                        Document manuscript_dc = manuscript_cl.find(eq("idManuscript", manuid)).first();
                        int pages = manuscript_dc.getInteger("typesetPages");

                        MongoCollection<Document> issue_cl = database.getCollection("Issue");
                        FindIterable<Document> d2 = issue_cl.find();

                        for(Document temp: d2) {
                            int issuePage = temp.getInteger("pages");
                            if(issuePage + pages <= 100) {
                                String item = "Issue:" + temp.get("publicationYear") + " " + temp.get("publicationPeriod") + "; pages:" + temp.get("pages");
                                Issues_Select.addItem(item);
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        Status_Select.addItem("ALL");
        Status_Select.addItem("Submitted");
        Status_Select.addItem("Under review");
        Status_Select.addItem("Rejected");
        Status_Select.addItem("Accepted");
        Status_Select.addItem("In typesetting");
        Status_Select.addItem("Scheduled for publication");
        Status_Select.addItem("Published");
        Status_Select.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String status = (String)Status_Select.getSelectedItem();

                myTable = createTable(status);
                Manuscripts.setViewportView(myTable);
            }
        });

        Status_Change_Select.addItem("Accepted");
        Status_Change_Select.addItem("In typesetting");
        Status_Change_Select.addItem("Scheduled for publication");
        Status_Change_Select.addItem("Rejected");
    }

    public JTable createTable(String status){
        Logger logger = Logger.getLogger( Editor.class.getName() );
        String[] content = new String[] {"idManuscript", "title", "idRICodes", "date", "status", "typesetPages"};
//        String sql = null;
//        if(status.equals("ALL"))
//            sql = "SELECT `idManuscript`,`title`,`code`, `date`,`status`,`authorList`,`typesetPages` FROM Manuscript ORDER BY FIELD(`status`, " +
//                    "'Submitted', 'Under review', 'Rejected', 'Accepted', 'In typesetting', 'Scheduled for publication', 'Published')" ;
//        else if(status.equals("Search"))
//            sql = constructSql();
//        else
//            sql = "SELECT `idManuscript`,`title`, `code`, `date`,`status`,`authorList`,`typesetPages` FROM Manuscript WHERE `status` = '" + status + "' ORDER BY FIELD(`status`, " +
//                    "'Submitted', 'Under review', 'Rejected', 'Accepted', 'In typesetting', 'Scheduled for publication', 'Published')" ;
        DefaultTableModel dtm = buildTableModel(status, content);

        JTable table = new JTable(dtm);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        return table;
    }

    public DefaultTableModel buildTableModel (String status, String[] content){
        Vector<String> columnNames = new Vector<>();
        Vector<Vector<Object>> data = new Vector<>();

        int columnCount = content.length;
        for (int column = 0; column < columnCount; column++)
            columnNames.add(content[column]);

        MongoCollection<Document> manuscript_cl = database.getCollection("Manuscript");
        FindIterable<Document> rs = null;
        if(status.equals("ALL")) rs = manuscript_cl.find();
        else rs = manuscript_cl.find(eq("status", status));

        for(Document d: rs) {
            Vector<Object> vector = new Vector<Object>();
            for(String s: content) {
                vector.add(d.get(s));
            }
            data.add(vector);
        }

        return new DefaultTableModel(data, columnNames);
    }

    public static boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public String[] getIssue(String s) {
        String[] temp = s.split(";");
        String[] temp2 = temp[0].split(":");
        String[] temp3 = temp2[1].split("\\s+");
        String[] temp4 = temp[1].split(":");
        String[] issue = new String[]{temp3[0], temp3[1], temp4[1]};
        return issue;
    }

    public String constructSql() {
        String sql = "";
        String core_part = "";
        for(String s: keyword) {
            core_part += " `authorList` LIKE '%" + s + "%' OR `title` LIKE '%" + s + "%' OR";
        }
        core_part = core_part.substring(0, core_part.length() - 2);
        String status = (String)Status_Select.getSelectedItem();
        if(status.equals("ALL")) {
            sql = "SELECT `idManuscript`,`title`,`code`, `date`,`status`,`authorList`,`typesetPages` FROM Manuscript WHERE " + core_part + " ORDER BY FIELD(`status`, " +
                    "'Submitted', 'Under review', 'Rejected', 'Accepted', 'In typesetting', 'Scheduled for publication', 'Published')" ;
        } else {
            sql = "SELECT `idManuscript`,`title`,`code`,`date`,`status`,`authorList`,`typesetPages` FROM Manuscript WHERE " + core_part + " AND `status` = '" + status + "' ORDER BY FIELD(`status`, " +
                    "'Submitted', 'Under review', 'Rejected', 'Accepted', 'In typesetting', 'Scheduled for publication', 'Published')" ;
        }

        return sql;
    }
}
