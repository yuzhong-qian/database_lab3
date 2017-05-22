import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.ne;

/**
 * Created by qianyuzhong on 5/2/17.
 */
public class Issue {
    private JPanel Issues;
    private JLabel Issue_Part;
    private JPanel Operation_Part;
    private JPanel Status_Part;
    private JLabel Status;
    private JComboBox Status_Select;
    private JPanel Publish_Part;
    private JLabel Issue_Year;
    private JTextField Issue_Year_text;
    private JButton Publish_btn;
    private JScrollPane Issues_List;
    private JLabel Period;
    private JTextField Issue_Period_text;
    private JFrame frame;
    private JTable myTable;
    private MongoDatabase database = null;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Issue");
        frame.setContentPane(new Issue().Issues);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public Issue() {
        JFrame frame = new JFrame("Issue");
        frame.setLocation(400, 10);
        frame.setContentPane(Issues);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
//        Status_Part.setVisible(false);

        database = DatebaseConnection.connection();
        MongoCollection<Document> issue_cl = database.getCollection("Issue");
        MongoCollection<Document> manuscript_cl = database.getCollection("Manuscript");

        myTable = createTable("ALL");
        Issues_List.setViewportView(myTable);

        Publish_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String year = Issue_Year_text.getText();
                if(year.equals("")) {
                    JOptionPane.showMessageDialog(frame,
                            "You must input Issue ID!");
                    return;
                }
                if(!isNumeric(year)) {
                    JOptionPane.showMessageDialog(frame,
                            "Issue ID must be valid!");
                    return;
                }
                int year_int = Integer.parseInt(year);
                String period = Issue_Period_text.getText();
                if(period.equals("")) {
                    JOptionPane.showMessageDialog(frame,
                            "You must input Issue ID!");
                    return;
                }
                if(!isNumeric(period)) {
                    JOptionPane.showMessageDialog(frame,
                            "Issue ID must be valid!");
                    return;
                }
                int period_int = Integer.parseInt(period);

                String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
                issue_cl.updateOne(new Document("publicationYear", year_int).append("publicationPeriod", period_int), new Document("$set", new Document("printDate", timeStamp)));

                Document issue_dc = issue_cl.find(new Document("publicationYear", year_int).append("publicationPeriod", period_int)).first();
                List<Document> typesetting = (ArrayList<Document>) issue_dc.get("typesetting");
                List<Integer> manuidlist = new ArrayList<>();

                for(Document ty: typesetting) {
                    Double manuid_d = Double.parseDouble(ty.get("idManuscript").toString());
                    manuidlist.add(manuid_d.intValue());
                    manuscript_cl.updateOne(new Document("idManuscript", manuid_d.intValue()), new Document("$set", new Document("status", "Published")));
                }

                JOptionPane.showMessageDialog(frame, "Issue(" + year + " " + period +") has been published!");

                String message = "Manuscript ";
                for(int id: manuidlist) message = message + id + ", ";
                message = message.substring(0, message.length() -2);
                message += "have been published!";
                JOptionPane.showMessageDialog(frame, message);

                myTable = createTable((String) Status_Select.getSelectedItem());
                Issues_List.setViewportView(myTable);

            }
        });

        Status_Select.addItem("ALL");
        Status_Select.addItem("Published");
        Status_Select.addItem("Unpublished");
        Status_Select.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String status = (String)Status_Select.getSelectedItem();

                myTable = createTable(status);
                Issues_List.setViewportView(myTable);
            }
        });
    }

    public JTable createTable(String status){
        Logger logger = Logger.getLogger( Editor.class.getName() );

        String[] content = new String[] {"publicationYear", "publicationPeriod", "volume", "pages", "printDate"};
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

        MongoCollection<Document> issue_cl = database.getCollection("Issue");
        FindIterable<Document> rs = null;
        if(status.equals("ALL"))
            rs = issue_cl.find();
        else if(status.equals("Published"))
            rs = issue_cl.find(ne("printDate", null));
        else
            rs = issue_cl.find(eq("printData", null));

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
}
