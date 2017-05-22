/**
 * Created by Illyria on 5/4/17.
 */

import javax.swing.*;

import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;
import  java.io.*;
import java.util.Date;

public class submit {
    private JTextField title;
    private JTextField Affiliation;
    private JComboBox RICodeBox;
    private JTextField Author2;
    private JTextField Author3;
    private JTextField Author4;
    private JButton chooseFileButton;
    private JButton cancelButton;
    private JButton submitButton1;
    private JPanel SubmitOrCancel;
    private JPanel panel1;

    private Connection conn = null;
    private Statement stmt = null;
    private ResultSet rst = null;

    public  File selectedFile;
    private static JFrame frame = new javax.swing.JFrame("submit");
    private static List<String> interests;
    private static List<Integer> codes;
    public static final String QUERY    = "SELECT * FROM RICodes;";


//    public static void main(String[] args) {
//
//        frame.setContentPane(new submit(1).panel1);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.pack();
//        frame.setVisible(true);
//    }
    public submit(){}

//    public submit(int authorID, String name) {
//
//        frame.setContentPane(panel1);
//        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//        frame.pack();
//        frame.setVisible(true);
//
//        ResultSet res  = null;
//
//        try {
//            conn = DatebaseConnection.connection();
//            // initialize a query statement
//            stmt = conn.createStatement();
//
//            // query db and save results
//            res = stmt.executeQuery(QUERY);
//
//            // iterate through results
//            interests = new ArrayList<>();
//            codes = new ArrayList<>();
//            while(res.next()) {
//                codes.add(res.getInt(1));
//                interests.add(res.getString(2));
//                RICodeBox.addItem(res.getInt(1) + " " + res.getString(2));
//            }
//
//        } catch (SQLException e ) {          // catch SQL errors
//            System.err.format("SQL Error: %s", e.getMessage());
//        } catch (Exception e) {              // anything else
//            e.printStackTrace();
//        }
//
//        // Choose your file button action.
//        chooseFileButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(java.awt.event.ActionEvent e) {
//                javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
//                int returnValue = fileChooser.showOpenDialog(null);
//                if (returnValue == javax.swing.JFileChooser.APPROVE_OPTION)
//                {
//                    selectedFile = fileChooser.getSelectedFile();
//                    chooseFileButton.setText(selectedFile.getName());
//                }
//            }
//        });
//
//
//
//        // submit button action.
//        submitButton1.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                int interestCode = getCode((String) RICodeBox.getSelectedItem());
//                setSubmitButton(authorID, interestCode, name);
//            }
//        });
//
//        cancelButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                submit.frame.dispose();
//                Author.frame.setVisible(true);
//            }
//        });
//
//    }
//
//    public void setSubmitButton(int authorId, int interestCode, String name){
//        String insert = "INSERT Manuscript (`title`, `date`,`status`,`authorList`,`content`, `typesetPages`, `idAuthor`, `idEditor`, `code`) VALUES"
//                + " (?, ?, ?, ?, ?, ?, ?, ?, ?);";
//        PreparedStatement submit_table = null;
//        Statement find_id = null;
//        try {
//            submit_table = conn.prepareStatement(insert);
//            if(title.getText().equals("")){
//                JOptionPane.showMessageDialog(frame, "You should enter title!");
//            }
//            submit_table.setString(1, title.getText());
//            Date date = new Date();
//            Timestamp timeStamp = new Timestamp(date.getTime());
//            submit_table.setTimestamp(2, timeStamp);
//            submit_table.setString(3, "Submitted");
//            submit_table.setString(4, getAuthorList(name));
//            try {
//                if (selectedFile==null) {
//                    JOptionPane.showMessageDialog(frame, "You should select your file!");
//                }
//                submit_table.setBlob(5, new java.io.FileInputStream(selectedFile));
//            } catch (java.io.FileNotFoundException e) {
//
//                e.printStackTrace();
//            }
//            submit_table.setNull(6,java.sql.Types.INTEGER);
//
//            submit_table.setInt(7, authorId);
//            submit_table.setNull(8, java.sql.Types.INTEGER);
//            submit_table.setInt(9, interestCode);
//
//            if(checkRICode(interestCode)){
//                submit_table.executeUpdate();
//                JOptionPane.showMessageDialog(frame, "You have submitted one manuscript!");
//                submit.frame.dispose();
//
//                Author.frame.setVisible(true);
//
//            } else {
//                JOptionPane.showMessageDialog(frame, "You can not submit the manuscript because of no enough reviewers!");
//            }
//
//        } catch (SQLException e){
//            e.printStackTrace();
//        }
//    }
//
//    public String getAuthorList(String name){
//        String res = name;
//        if (!Author2.getText().equals("") ){
//            res += ", " +  Author2.getText();
//        }
//        if (!Author3.getText().equals("")) {
//            res += ", " + Author3.getText() ;
//        }
//        if (!Author4.getText().equals("")) {
//            res += ", " + Author4.getText();
//        }
//        return res;
//    }
//
//    public boolean checkRICode(int code){
//        ResultSet rst = null;
//        String check = "SELECT COUNT(*)  FROM InterestList WHERE code =" + code;
//        try {
//            Statement checkInterest = conn.createStatement();
//            rst = checkInterest.executeQuery(check);
//            rst.next();
//            if (rst.getInt(1) > 0) {
//                System.out.println("here"+rst.getInt(1));
//                return true;
//            }
//        } catch (java.sql.SQLException e) {
//            e.printStackTrace();
//        }
//        return false;
//
//    }
//
//    private int getCode(String s) {
//        String[] array = s.split("\\s+");
//        return Integer.parseInt(array[0]);
//    }
//


}
