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
    private Connection con = null;
    private Statement stmt = null;
    private ResultSet rst = null;


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

        con = DatebaseConnection.connection();
        try {
            stmt = con.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }


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
                String update_printdate = "UPDATE Issue SET `printDate` = '" + timeStamp + "' WHERE `publicationYear` = '" + year_int + "' AND `publicationPeriod` = '" + period +"';";
                String manu_published = "SELECT idManuscript FROM Typesetting WHERE `publicationYear` = '" + year_int + "' AND `publicationPeriod` = '" + period_int +"';";

                try {
                    stmt.execute(update_printdate);
                    JOptionPane.showMessageDialog(frame, "Issue(" + year + " " + period +") has been published!");
                    rst = stmt.executeQuery(manu_published);
                    List<Integer> manuidlist = new ArrayList<>();
                    String update_status = null;
                    Statement temp = con.createStatement();
                    while (rst.next()) {
                        manuidlist.add(rst.getInt(1));
                        update_status = "UPDATE Manuscript SET `status` = 'Published' WHERE `idManuscript` = " + rst.getInt(1);
                        temp.execute(update_status);
                    }
                    String message = "Manuscript ";
                    for(int id: manuidlist) message = message + id + ", ";
                    message = message.substring(0, message.length() -2);
                    message += "have been published!";
                    JOptionPane.showMessageDialog(frame, message);

                    myTable = createTable((String) Status_Select.getSelectedItem());
                    Issues_List.setViewportView(myTable);
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
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

        String sql = null;
        if(status.equals("ALL"))
            sql = "SELECT `publicationYear`,`publicationPeriod`,`volume`,`pages`,`printDate` FROM Issue ORDER BY `publicationYear` DESC,`publicationPeriod`";
        else if(status.equals("Published"))
            sql = "SELECT `publicationYear`,`publicationPeriod`,`volume`,`pages`,`printDate` FROM Issue WHERE `printDate` IS NOT NULL ORDER BY `publicationYear` DESC,`publicationPeriod`";
        else
            sql = "SELECT `publicationYear`,`publicationPeriod`,`volume`,`pages`,`printDate` FROM Issue WHERE `printDate` IS NULL ORDER BY `publicationYear` DESC,`publicationPeriod`";
        DefaultTableModel dtm = buildTableModel(stmt, sql);

        JTable table = new JTable(dtm);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        return table;
    }

    public static DefaultTableModel buildTableModel (Statement stmt, String sql){
        Vector<String> columnNames = new Vector<>();
        Vector<Vector<Object>> data = new Vector<>();
        try {
            ResultSet rs = stmt.executeQuery(sql);

            ResultSetMetaData metaData = rs.getMetaData();

            int columnCount = metaData.getColumnCount();
            for (int column = 1; column <= columnCount; column++) {
                columnNames.add(metaData.getColumnName(column));
            }
            while (rs.next()) {
                Vector<Object> vector = new Vector<Object>();
                for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                    vector.add(rs.getObject(columnIndex));
                }
                data.add(vector);
            }

        } catch (java.sql.SQLException sqle){
            sqle.printStackTrace();
        }
        return new DefaultTableModel(data, columnNames);
    }

    public static boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
}
