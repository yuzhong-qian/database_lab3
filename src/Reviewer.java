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
    private Connection con = null;
    private Statement stmt = null;
    private ResultSet rst = null;
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
        String query = "SELECT * FROM Reviewer WHERE idReviewer = " + userid;
        con = DatebaseConnection.connection();
        try {
            stmt = con.createStatement();
            rst = stmt.executeQuery(query);
            String firstname = null;
            String lastname = null;
            while (rst.next()) {
                firstname = rst.getString(3);
                lastname = rst.getString(2);
            }

            Welcome.setText("Welcome, " + firstname + " " + lastname +"!");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        myTable = createTable("ALL");
        Manuscript_List.setViewportView(myTable);

        Sign_out_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    stmt.close();
                    con.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
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
                String insert = "INSERT INTO Feedback (`appropriateness`,`clarity`,`methodology`,`contribution`,`recommendation`,`dateReceive`) VALUES ("
                        + score_appropriateness + "," + score_clarity + "," + score_contribution + "," + score_methodology + ",'" +
                        recommendation + "','" + timeStamp + "')";

                try {
                    stmt.execute(insert);

                    String id = "SELECT MAX(`idFeedback`) FROM Feedback";
                    rst = stmt.executeQuery(id);
                    rst.next();
                    int id_max = rst.getInt(1);

                    String update = "UPDATE Assignment SET `idFeedback` = " + id_max + " WHERE `idManuscript` = " +  Manu_id_text.getText() +
                            " AND `idReviewer` = " + reviewer_id;
                    stmt.execute(update);

                    myTable = createTable("ALL");
                    Manuscript_List.setViewportView(myTable);
                    JOptionPane.showMessageDialog(frame, "Your feedback successfully submitted!");
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        });

        Resign_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String delete = "DELETE FROM Reviewer WHERE `idReviewer` = " + reviewer_id;

                System.out.println(delete);
                int flag = JOptionPane.showConfirmDialog(frame, "Are you sure to resign?");
                System.out.println(flag);
                if(flag == 0) {
                    JOptionPane.showMessageDialog(frame, "Thank you for your service.");

                    try {
                        stmt.execute(delete);
                        stmt.close();
                        con.close();
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
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
        Logger logger = Logger.getLogger( Editor.class.getName() );

        String sql = null;
        if(status.equals("ALL"))
            sql = "SELECT `idReviewer`,`idManuscript`,`assignDate` FROM Assignment WHERE `idReviewer` = " + reviewer_id
                    + " AND `idFeedback` IS NULL ORDER BY `assignDate`";
        else if(status.equals("History"))
            sql = "SELECT * FROM ReviewStatus WHERE `idReviewer` = " + reviewer_id
                    + " AND `clarity` IS NOT NULL ORDER BY `assignDate`";
//        else
//            sql = "SELECT `publicationYear`,`publicationPeriod`,`volume`,`pages`,`printDate` FROM Issue WHERE `printDate` IS NULL ORDER BY `publicationYear` DESC,`publicationPeriod`";
        DefaultTableModel dtm = buildTableModel(stmt, sql, status);

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
    public static DefaultTableModel buildTableModel (Statement stmt, String sql, String status){
        Vector<String> columnNames = new Vector<>();
        Vector<Vector<Object>> data = new Vector<>();
        try {
            ResultSet rs = stmt.executeQuery(sql);

            ResultSetMetaData metaData = rs.getMetaData();

            columnCount = metaData.getColumnCount();
            for (int column = 1; column <= columnCount; column++) {
                columnNames.add(metaData.getColumnName(column));
            }
            if(status.equals("ALL"))
                columnNames.add("");
            while (rs.next()) {
                Vector<Object> vector = new Vector<Object>();
                for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                    vector.add(rs.getObject(columnIndex));
                }

                if(status.equals("ALL"))
                    vector.add("Feedback");
                data.add(vector);
            }

        } catch (java.sql.SQLException sqle){
            sqle.printStackTrace();
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
