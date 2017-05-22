import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;
import java.util.logging.Logger;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.ne;


/**
 * Created by qianyuzhong on 5/1/17.
 */
public class Reviewer {
    private JPanel Reviewer_Head;
    private JLabel Welcome;
    private JButton Sign_out_btn;
    private JButton Resign_btn;
    private JScrollPane Manuscript_List;
    private JPanel Reviewer;
    private JButton History_btn;
    private JPanel Feedback_Part;
    private JTextField Manu_id_text;
    private JLabel Manu_id;
    private JPanel Score;
    private JComboBox appropriateness_select;
    private JComboBox clarity_select;
    private JComboBox recommendation_select;
    private JComboBox methodology_select;
    private JComboBox contribution_select;
    private JLabel appropriateness;
    private JLabel clarity;
    private JLabel methodology;
    private JLabel contribution;
    private JLabel recommendation;
    private JButton Submit_btn;
    private JButton Reviewing_btn;
    private JFrame frame;

    private JTable myTable;
    private MongoDatabase database = null;
    private int reviewer_id;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Reviewer");
        frame.setContentPane(new Reviewer().Reviewer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public Reviewer() {

    }

    public Reviewer(int userid) {
        frame = new JFrame("Reviewer");
        frame.setContentPane(Reviewer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setLocation(400, 0);

        reviewer_id = userid;

        database = DatebaseConnection.connection();
        MongoCollection<Document> reviewer_cl = database.getCollection("Reviewer");
        Document reviewer_dc = reviewer_cl.find(eq("idReviewer", userid)).first();

        String firstname = reviewer_dc.getString("reviewerFirstName");
        String lastname = reviewer_dc.getString("reviewerLastName");

        Welcome.setText("Welcome, " + firstname + " " + lastname +"!");

        myTable = createTable("ALL");
        Manuscript_List.setViewportView(myTable);

        Sign_out_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                new Login();
            }
        });

        Reviewing_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                myTable = createTable("ALL");
                Manuscript_List.setViewportView(myTable);
            }
        });

        History_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                myTable = createTable("History");
                Manuscript_List.setViewportView(myTable);

            }
        });

        Submit_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String score_appropriateness = (String) appropriateness_select.getSelectedItem();
                String score_clarity = (String) clarity_select.getSelectedItem();
                String score_methodology = (String) methodology_select.getSelectedItem();
                String score_contribution = (String) contribution_select.getSelectedItem();
                String recommendation = (String) recommendation_select.getSelectedItem();

                if(score_appropriateness.equals("-") || score_clarity.equals("-") || score_methodology.equals("-")
                        || score_contribution.equals("-") || recommendation.equals("-")) {
                    JOptionPane.showMessageDialog(frame, "You should score all the attribute!");
                    return;
                }
                String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());

                MongoCollection<Document> assignment = database.getCollection("Assignment");
                BasicDBObject feedback = new BasicDBObject("appropriateness", score_appropriateness).append("clarity", score_clarity)
                        .append("methodology", score_methodology).append("contribution", score_contribution)
                        .append("recommendation", recommendation).append("dateReceive", timeStamp);

                assignment.updateOne(new BasicDBObject("idManuscript", Manu_id_text.getText()).append("idReviewer", reviewer_id), new BasicDBObject("$push", new BasicDBObject("feedback", feedback)));

                myTable = createTable("ALL");
                Manuscript_List.setViewportView(myTable);
                JOptionPane.showMessageDialog(frame, "Your feedback successfully submitted!");
            }
        });

        Resign_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int flag = JOptionPane.showConfirmDialog(frame, "Are you sure to resign?");

                if(flag == 0) {
                    reviewer_cl.deleteOne(eq("idReviewer", reviewer_id));
                    JOptionPane.showMessageDialog(frame, "Thank you for your service.");

                    frame.dispose();
                    new Login();
                }
            }
        });

        appropriateness_select.addItem("-");
        for(int i = 1; i <= 10; i++)
            appropriateness_select.addItem(i + "");
        appropriateness_select.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                judge();
            }
        });

        clarity_select.addItem("-");
        for(int i = 1; i <= 10; i++)
            clarity_select.addItem(i + "");
        clarity_select.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                judge();
            }
        });

        methodology_select.addItem("-");
        for(int i = 1; i <= 10; i++)
            methodology_select.addItem(i + "");
        methodology_select.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                judge();
            }
        });

        contribution_select.addItem("-");
        for(int i = 1; i <= 10; i++)
            contribution_select.addItem(i + "");
        contribution_select.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                judge();
            }
        });

        recommendation_select.addItem("-");
        recommendation_select.addItem("REVIEW-ACCEPT");
        recommendation_select.addItem("REVIEW-REJECT");
        recommendation_select.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                judge();
            }
        });
    }

    public JTable createTable(String status){
        Logger logger = Logger.getLogger( Editor.class.getName());

        String[] content = null;
        if(status.equals("ALL"))
            content = new String[] {"idReviewer","idManuscript", "assignDate"};
        else if(status.equals("History"))
            content = new String[] {"idReviewer","idManuscript", "assignDate", "feedback.appropriateness", "feedback.clarity"
            , "feedback.methodology", "feedback.contribution", "feedback.recommendation", "feedback.dateReceive"};

        DefaultTableModel dtm = buildTableModel(status, content);

        JTable table = new JTable(dtm);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        if(status.equals("ALL")) {
            ButtonColumn buttonColumn = new ButtonColumn(table, feedback, columnCount);
            buttonColumn.setMnemonic(KeyEvent.VK_D);
        }
        return table;
    }

    static int columnCount = 0;

    public DefaultTableModel buildTableModel (String status, String[] content){
        Vector<String> columnNames = new Vector<>();
        Vector<Vector<Object>> data = new Vector<>();

        int columnCount = content.length;
        for (int column = 0; column < columnCount; column++)
            columnNames.add(content[column]);

        if(status.equals("ALL"))
            columnNames.add("");

        MongoCollection<Document> assignment_cl = database.getCollection("Assignment");
        FindIterable<Document> rs = null;
        if(status.equals("ALL"))
            rs = assignment_cl.find(new Document("idReviewer", reviewer_id).append("feedback", null));
        else {
            Bson filter = Filters.or(
                    Filters.eq("idReviewer", reviewer_id),
                    Filters.ne("feedback", null)
            );
            rs = assignment_cl.find(filter);
        }

        Vector<Object> vector = new Vector<Object>();
        for(Document d: rs) {
            for(String s: content) {
                vector.add(d.get(s));
            }
            if(status.equals("ALL"))
                vector.add("Feedback");
            data.add(vector);
        }

        return new DefaultTableModel(data, columnNames);
    }

    Action feedback = new AbstractAction()
    {
        public void actionPerformed(ActionEvent e)
        {
            JTable table = (JTable)e.getSource();
            int modelRow = Integer.valueOf( e.getActionCommand() );
            int idReviewer = (int) ((DefaultTableModel)table.getModel()).getValueAt(modelRow, 0);
            int idManuscript = (int)((DefaultTableModel)table.getModel()).getValueAt(modelRow, 1);
            new Feedback(idReviewer, idManuscript);
        }
    };

    public static boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public void judge() {
        String id = Manu_id_text.getText();
        if(id.equals("")) {
            String manuid = JOptionPane.showInputDialog(frame,
                    "You must input Manuscript ID!");
            Manu_id_text.setText(manuid);
            return;
        }
        if(!isNumeric(id)) {
            String manuid = JOptionPane.showInputDialog(frame,
                    "Manuscript ID must be valid!");
            Manu_id_text.setText(manuid);
            return;
        }
    }
}
