import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.print.Doc;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.security.*;

/**
 * Created by qianyuzhong on 4/29/17.
 */
public class Register {
    private JPanel Register_Panel;
    private JPanel Input;
    private JTextField last_name_text;
    private JTextField first_name_text;
    private JButton Cancel_btn;
    private JButton Register_btn;
    private JPanel Button;
    private JLabel Register_Label;
    private JPanel Other_info;
    private JTextField mailaddress_text;
    private JTextField emailAddress_text;
    private JLabel EmailAddress;
    private JLabel MailAddress;
    private JComboBox user_type;
    private JPanel Interest;
    private JComboBox Interest1_select;
    private JComboBox Interest2_select;
    private JComboBox Interest3_select;
    private JLabel Interest1;
    private JLabel Interest2;
    private JLabel Interest3;
    private JTextField affiliation_text;
    private JLabel Affiliation;
    private javax.swing.JPasswordField passwordField1;
    private javax.swing.JPasswordField passwordField2;

    private MongoDatabase database = null;

    public static JFrame frame = new JFrame("Register_Label");
    private static List<String> interests;
    private static List<Integer> codes;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Register_Label");
        frame.setContentPane(new Register().Register_Panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public Register() {
        frame.setContentPane(Register_Panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setLocation(540, 200);

        user_type.addItem("Editor");
        user_type.addItem("Author");
        user_type.addItem("Reviewer");

        database = DatebaseConnection.connection();

        MongoCollection<Document> RICode = database.getCollection("RICode");
        FindIterable<Document> result_RICode = RICode.find();
        interests = new ArrayList<>();
        codes = new ArrayList<>();

        for(Document document: result_RICode) {
//            System.out.println(document);
            System.out.println(document.getString("interest"));
//            int code = Integer.valueOf(document.getDouble("idRICodes").intValue());
            Double code = Double.parseDouble(document.get("idRICodes").toString());

            codes.add(code.intValue());

            interests.add(document.getString("interest"));
        }

        Interest2_select.addItem("-");
        Interest3_select.addItem("-");

        for(int i = 0; i < interests.size(); i++) {
            Interest1_select.addItem(codes.get(i) + " " + interests.get(i));
            Interest2_select.addItem(codes.get(i) + " " + interests.get(i));
            Interest3_select.addItem(codes.get(i) + " " + interests.get(i));
        }

        Other_info.setVisible(false);
        Interest.setVisible(false);

        Cancel_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                Login.frame.setVisible(true);
            }
        });

        Register_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String s = (String) user_type.getSelectedItem();//get the selected item
                int id = -1;
                switch (s) {
                    case "Editor":
                        if (checkInput()) {
                            MongoCollection<Document> temp = database.getCollection("Editor_Counters");
                            Document seq = temp.find().first();
//                            System.out.println(seq);
                            Double seq_d = Double.parseDouble(seq.get("seq").toString());
                            id = seq_d.intValue() + 1;
                            temp.updateOne(new Document("_id", "Editor_ID"), new Document("$set", new Document("seq", id)));
                            Document document = new Document();
                            document.append("idEditor", id);
                            document.append("EditorLastName", last_name_text.getText());
                            document.append("EditorFirstName", first_name_text.getText());
                            document.append("Password", securePassword(String.valueOf(passwordField1.getPassword())));
                            MongoCollection<Document> editor = database.getCollection("Editor");
                            editor.insertOne(document);
                        }
                        break;
                    case "Author":
                        if (checkInput() && checkOtherInfo()) {
                            MongoCollection<Document> temp = database.getCollection("Author_Counters");
                            Document seq = temp.find().first();
                            Double seq_d = Double.parseDouble(seq.get("seq").toString());
                            id = seq_d.intValue() + 1;
                            temp.updateOne(new Document("_id", "Author_ID"), new Document("$set", new Document("seq", id)));
                            Document document = new Document();
                            document.append("idAuthor", id);
                            document.append("authorLastName", last_name_text.getText());
                            document.append("authorFirstName", first_name_text.getText());
                            document.append("mailAddress", mailaddress_text.getText());
                            document.append("emailAddress", emailAddress_text.getText());
                            document.append("affiliation", affiliation_text.getText());
                            document.append("Password", securePassword(String.valueOf(passwordField1.getPassword())));
                            MongoCollection<Document> author = database.getCollection("Author");
                            author.insertOne(document);
                        }
                        break;
                    case "Reviewer":
                        if (checkInput() && checkOtherInfo()) {
                            MongoCollection<Document> temp = database.getCollection("Reviewer_Counters");
                            Document seq = temp.find().first();
                            Double seq_d = Double.parseDouble(seq.get("seq").toString());
                            id = seq_d.intValue() + 1;
                            temp.updateOne(new Document("_id", "Reviewer_ID"), new Document("$set", new Document("seq", id)));
                            Document document = new Document();
                            document.append("idReviewer", id);
                            document.append("reviewerLastName", last_name_text.getText());
                            document.append("reviewerFirstName", first_name_text.getText());
                            document.append("emailAddress", mailaddress_text.getText());
                            document.append("affiliation", emailAddress_text.getText());
                            document.append("Password", securePassword(String.valueOf(passwordField1.getPassword())));


                            String[] reviewer_interest = new String[3];
                            reviewer_interest[0] = (String) Interest1_select.getSelectedItem();
                            reviewer_interest[1] = (String) Interest2_select.getSelectedItem();
                            reviewer_interest[2] = (String) Interest3_select.getSelectedItem();

                            BasicDBList interests_list = new BasicDBList();
                            temp = database.getCollection("interest_Counters");
                            seq = temp.find().first();
                            Double seq_num = Double.parseDouble(seq.get("seq").toString());
                            int id_interest = seq_num.intValue() + 1;
//                            int seq_num = seq.getInteger("seq");
//                             = seq_num + 1;
                            temp.updateOne(new Document("_id", "Reviewer_ID"), new Document("$set", new Document("seq", id_interest)));
                            MongoCollection<org.bson.Document> interest_col = database.getCollection("interestlist");

                            for (String interest : reviewer_interest) {
                                if (!interest.equals("-")) {
                                    DBObject db_obj = new BasicDBObject();
                                    db_obj.put("idRICode", getCode(interest));
                                    db_obj.put("interest", getInterest(interest));
                                    interests_list.add(db_obj);

                                    interest_col.insertOne(new Document("idItem",id_interest).append("idReviewer", id).append("idRICodes", getCode(interest)));
                                }
                            }
                            document.append("interestlist", interests_list);

//                            String interests_list = "[";
//                            for (String interest : reviewer_interest) {
//                                if (!interest.equals("-")) {
//                                    interests_list += "\n{\n" +
//                                            "idRICode: " + getCode(interest) +
//                                            "interest: " + getInterest(interest) +
//                                            "},";
//                                }
//                            }
//
//                            interests_list = interests_list.substring(0, interests_list.length() - 1);
//                            interests_list += "\n]";

//                            System.out.println(interests_list);

                            MongoCollection<Document> reviewer = database.getCollection("Reviewer");
                            reviewer.insertOne(document);
                        }
                        break;
                    default:
                        frame.setSize(new Dimension(250, 200));
                        Input.setVisible(true);
                        Other_info.setVisible(false);
                        Interest.setVisible(false);
                        break;
                }

                JOptionPane.showMessageDialog(frame, "Your ID is " + id);
                frame.dispose();
                Login.frame.setVisible(true);
            }
        });

        user_type.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String s = (String) user_type.getSelectedItem();//get the selected item

                switch (s) {//check for a match
                    case "Editor":
                        frame.setSize(new Dimension(250, 200));
                        Other_info.setVisible(false);
                        Interest.setVisible(false);
                        break;
                    case "Author":
                        frame.setSize(new Dimension(250, 300));
                        Other_info.setVisible(true);
                        Interest.setVisible(false);
                        break;
                    case "Reviewer":
                        frame.setSize(new Dimension(450, 400));
                        Other_info.setVisible(true);
                        Interest.setVisible(true);
                        break;
                    default:
                        frame.setSize(new Dimension(250, 200));
                        Input.setVisible(true);
                        Other_info.setVisible(false);
                        Interest.setVisible(false);
                        break;
                }
            }
        });

        Interest1_select.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String s = (String) Interest1_select.getSelectedItem();//get the selected item
                if(s.equals((String) Interest2_select.getSelectedItem()) || s.equals((String) Interest3_select.getSelectedItem())) {
                    Interest1_select.setSelectedIndex(0);
                    JOptionPane.showConfirmDialog(frame,
                            "You cannot choose duplicated interests!");
                }
            }
        });

        Interest2_select.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String s = (String) Interest2_select.getSelectedItem();//get the selected item
                if(s.equals((String) Interest1_select.getSelectedItem()) || s.equals((String) Interest3_select.getSelectedItem())) {
                    Interest2_select.setSelectedIndex(0);
                    JOptionPane.showConfirmDialog(frame,
                            "You cannot choose duplicated interests!");
                }
            }
        });

        Interest3_select.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String s = (String) Interest3_select.getSelectedItem();//get the selected item
                if(s.equals((String) Interest1_select.getSelectedItem()) || s.equals((String) Interest2_select.getSelectedItem())) {
                    Interest3_select.setSelectedIndex(0);
                    JOptionPane.showConfirmDialog(frame,
                            "You cannot choose duplicated interests!");
                }
            }
        });
    }
    private boolean checkPassword(char[] s1, char[] s2){
        int len = s1.length;
        if (len == 0 || s1.length != s2.length) {return false;}
        for (int i = 0; i < len; i++){
            if(s1[i] != s2[i]){return false;}
        }
        return true;
    }

    private int getCode(String s) {
        String[] array = s.split("\\s+");
        return Integer.parseInt(array[0]);
    }

    private String getInterest(String s) {
        String[] array = s.split("\\s+");
        return array[1];
    }

    private boolean checkInput(){
        if (last_name_text.getText().equals("")){
            JOptionPane.showMessageDialog(frame,
                    "You missed the last name!");
            return false;
        }
        if (first_name_text.getText().equals("")){
            JOptionPane.showMessageDialog(frame,
                    "You missed the first name!");
            return false;
        }
        if (!checkPassword(passwordField1.getPassword(), passwordField2.getPassword())){
            JOptionPane.showMessageDialog(frame,
                    "Your passwords are not the same!");
            return false;
        }
        return true;
    }

    private boolean checkOtherInfo(){
        if (mailaddress_text.getText().equals("")){
            JOptionPane.showMessageDialog(frame,
                    "You missed the mail address!");
            return false;
        }
        if (emailAddress_text.getText().equals("")){
            JOptionPane.showMessageDialog(frame,
                    "You missed the Email address!");
            return false;
        }

        return true;
    }
    private String securePassword(String passwordToHash){
        String generatedPassword = null;
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(passwordToHash.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++){
                sb.append(Integer.toString((bytes[i]&0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return generatedPassword;
    }
}
