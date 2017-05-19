//import com.sun.deploy.panel.SecurityLevel;
import com.sun.tools.corba.se.idl.InterfaceGen;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

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
    private JPanel Search_Part;
    private JTextField Search_text;
    private JButton Search;
    private javax.swing.JLabel Warning_Message;
    private Connection con = null;
    private Statement stmt = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet rst = null;

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

        Warning_Message.setVisible(false);
        Typesetting_Part.setVisible(false);
        Issues_Select_Part.setVisible(false);
        Manuscript_ID.setText("Manuscript ID");
        String query = "SELECT * FROM Editor WHERE idEditor = " + userid;
        String warning = "SELECT `idManuscript`, COUNT(*) AS number FROM Assignment GROUP BY `idManuscript`";
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

            rst = stmt.executeQuery(warning);
            String warning_message = "<html>Some reviewer(s) resigned!<br>Manuscript(s) ";
            boolean flag = false;
            while (rst.next()) {
                if(rst.getInt(2) < 3) {
                    warning_message += rst.getInt(1) + ", ";
                    flag = true;
                }
            }
            warning_message = warning_message.substring(0, warning_message.length() - 2);
            warning_message += " need you<br>to assign new reviewer(s)!";
            Warning_Message.setText(warning_message);
            if(flag) Warning_Message.setVisible(true);
            Welcome.setText("Welcome, " + firstname + " " + lastname +"!");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        myTable = createTable("ALL");
        Manuscripts.setViewportView(myTable);

        Sign_Out.addActionListener(new ActionListener() {
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

        Search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nameList = Search_text.getText();
                keyword = nameList.split("\\s+");

                myTable = createTable("Search");
                Manuscripts.setViewportView(myTable);
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
                String update;
                int pages = 0;

                String temp = "SELECT `status` FROM Manuscript WHERE `idManuscript` = " + manuid;
                String currtent_status = "";
                try {
                    rst = stmt.executeQuery(temp);
                    while (rst.next())
                        currtent_status = rst.getString(1);
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }

                switch (change_status) {
                    case "Rejected":
                        if(currtent_status.equals("Scheduled for publication") || currtent_status.equals("Published")) {
                            JOptionPane.showMessageDialog(frame, "You cannot reject this manuscript at this status!");
                            return;
                        }
                        update = "UPDATE Manuscript SET `status` = '" + change_status + "' WHERE idManuscript = " + manuid;
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
                        update = "UPDATE Manuscript SET `status` = '" + change_status + "' WHERE idManuscript = " + manuid;
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

                        update = "UPDATE Manuscript SET `status` = '" + change_status + "', `typesetPages` = " + pages + " WHERE `idManuscript` = " + manuid;
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
                        update = "UPDATE Manuscript SET `status` = '" + change_status + "' WHERE idManuscript = " + manuid + ";";
                        String getPage = "SELECT `typesetPages` FROM Manuscript WHERE `idManuscript` = " + manuid;

                        pages = 0;
                        try {
                            rst = stmt.executeQuery(getPage);
                            while (rst.next())
                                pages = rst.getInt(1);
                        } catch (SQLException e1) {
                            e1.printStackTrace();
                        }
                        String[] issue = getIssue((String)Issues_Select.getSelectedItem());

                        String insert = "INSERT Typesetting (`idManuscript`, `publicationYear`,`publicationPeriod`,`beginPage`,`order`) VALUES (?, ?, ?, ?, ?);";
                        String maxOrder = "SELECT MAX(`order`) FROM Typesetting WHERE `publicationYear` = '" + issue[0] + "' AND `publicationPeriod` = '" + issue[1] +"';";

                        int order = 0;
                        try {
                            rst = stmt.executeQuery(maxOrder);
                            while (rst.next())
                                order = rst.getInt(1);
                            preparedStatement = con.prepareStatement(insert);
                            preparedStatement.setInt(1, manuid);
                            preparedStatement.setInt(2, Integer.parseInt(issue[0]));
                            preparedStatement.setInt(3, Integer.parseInt(issue[1]));
                            preparedStatement.setInt(4, Integer.parseInt(issue[2]) + 1);
                            preparedStatement.setInt(5, order + 1);
                            preparedStatement.executeUpdate();
//                        String update2 =  "UPDATE Issue SET `pages` = `pages` + " + pages +
//                                 " WHERE `publicationYear` = '" + issue[0] + "' AND `publicationPeriod` = '" + issue[1] +"';";

//                            stmt.execute(update2);
                        } catch (SQLException e1) {
                            e1.printStackTrace();
                        }
                        break;
                    default:
                        update = "UPDATE Manuscript SET `status` = '" + change_status + "' WHERE idManuscript = " + manuid;
                        break;
                }

                try {
                    stmt.execute(update);
                    JOptionPane.showMessageDialog(frame,
                            "Manuscript " + manuid + "'s status has been changed to " + change_status + "!");
                    String status = (String)Status_Select.getSelectedItem();

                    myTable = createTable(status);
                    Manuscripts.setViewportView(myTable);

                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
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
                        String getPage = "SELECT `typesetPages` FROM Manuscript WHERE `idManuscript` = " + manuid;

                        int pages = 0;
                        try {
                            rst = stmt.executeQuery(getPage);
                            while (rst.next())
                                pages = rst.getInt(1);
                            String select = "SELECT `publicationYear`, `publicationPeriod`, `pages` FROM Issue WHERE `pages` + " + pages + " <= 100";
//                            System.out.println(select);
                            rst = stmt.executeQuery(select);
                            List<String> availableIssues = new ArrayList<>();
                            Issues_Select.removeAllItems();
                            while (rst.next()) {
                                String temp = "Issue:" + rst.getInt(1) + " " + rst.getInt(2) + "; pages:" + rst.getInt(3);
                                availableIssues.add(temp);
                                Issues_Select.addItem(temp);
                            }
                        } catch (SQLException e1) {
                            e1.printStackTrace();
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

        String sql = null;
        if(status.equals("ALL"))
            sql = "SELECT `idManuscript`,`title`,`code`, `date`,`status`,`authorList`,`typesetPages` FROM Manuscript ORDER BY FIELD(`status`, " +
                    "'Submitted', 'Under review', 'Rejected', 'Accepted', 'In typesetting', 'Scheduled for publication', 'Published')" ;
        else if(status.equals("Search"))
            sql = constructSql();
        else
            sql = "SELECT `idManuscript`,`title`, `code`, `date`,`status`,`authorList`,`typesetPages` FROM Manuscript WHERE `status` = '" + status + "' ORDER BY FIELD(`status`, " +
                    "'Submitted', 'Under review', 'Rejected', 'Accepted', 'In typesetting', 'Scheduled for publication', 'Published')" ;
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
