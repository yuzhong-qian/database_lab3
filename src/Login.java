import javax.jws.soap.SOAPBinding;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by qianyuzhong on 4/29/17.
 */
public class Login {
    private JPanel Login;
    private JPanel LoginPart1;
    private JTextField Username_text;
    private JPanel Input;
    private JPanel Buttons;
    private JButton Register_btn;
    private JButton Login_btn;
    private JLabel UserID;
    private JLabel User_type;
    private JComboBox User_type_select;
    private javax.swing.JPasswordField passwordField;
    private static List<String> interests;
    public static JFrame frame = new JFrame("Login");


    private static Connection con = null;
    private static Statement stmt = null;
    private static ResultSet res = null;
    public static final String QUERY = "SELECT interest FROM RICodes;";

    public static void main(String[] args) {
        frame.setContentPane(new Login().Login);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

    }

    public Login() {


        frame.setLocation(600, 300);
        frame.setContentPane(Login);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        //Add three types of user
        User_type_select.addItem("Editor");
        User_type_select.addItem("Author");
        User_type_select.addItem("Reviewer");

        con = DatebaseConnection.connection();
        // initialize a query statement
        try {
            stmt = con.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Login_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String user = (String) User_type_select.getSelectedItem();
                String id = Username_text.getText();
                //String pw = passwordField.getPassword().toString();


                if (id.equals("")) {
                    JOptionPane.showMessageDialog(frame,
                            "You must input your UserID!");
                    return;
                }
                if (!isNumeric(id)) {
                    JOptionPane.showMessageDialog(frame,
                            "UserID must be valid!");
                    return;
                }


                int userid = Integer.parseInt(id);
                if (checkpw(user, userid)) {
                    String getUser = "SELECT * FROM " + user + " WHERE id" + user + " = " + userid;

                    boolean isEmpty = true;
                    try {
                        res = stmt.executeQuery(getUser);
                        while (res.next()) {
                            isEmpty = false;
                        }
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                    if (isEmpty) {
                        JOptionPane.showMessageDialog(frame,
                                "No such userid in " + user + "!");
                    } else {
                        frame.dispose();
                        if (user.equals("Editor")) new Editor(userid);
                        else if (user.equals("Author")) new Author(userid);
                        else if (user.equals("Reviewer")) new Reviewer(userid);
                        else JOptionPane.showMessageDialog(frame,
                                    "No such user type!");
                        try {
                            stmt.close();
                            con.close();
                            System.out.print("\nConnection terminated.");
                        } catch (Exception exp) { /* ignore cleanup errors */ }
                    }
                } else {
                    JOptionPane.showMessageDialog(frame,
                            "Your password is not correct!");
                    passwordField.setText("");
                    return;
                }
            }
        });

            Register_btn.addActionListener(e->

            {
                // display/center the jdialog when the button is pressed
                frame.setVisible(false);
                new Register();
            }

            );
        }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    private boolean checkpw(String type, int id) {
        String userPassword = String.valueOf(passwordField.getPassword());
        String check = "SELECT password FROM Credential where usertype = '"+type+"'AND userid ="+id;

        try {
            PreparedStatement checkPass = con.prepareStatement(check);
            ResultSet rs = checkPass.executeQuery(check);

            if (rs.next()) {
                if(rs.getString(1).equals(securePassword(userPassword))){
                    return true;
                }else {
                    return false;
                }
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return false;

    }

    private String securePassword(String passwordToHash){
        String generatedPassword = null;
        try{
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            md.update(passwordToHash.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++){
                sb.append(Integer.toString((bytes[i]&0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        }catch (java.security.NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return generatedPassword;
    }


}
