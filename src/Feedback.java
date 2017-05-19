import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by qianyuzhong on 5/3/17.
 */
public class Feedback {
    private JPanel Feedback;
    private JPanel Attribute_List;
    private JComboBox appropriateness_select;
    private JComboBox clarity_select;
    private JComboBox methodology_select;
    private JComboBox contribution_select;
    private JButton Submit_btn;
    private JComboBox recommendation_select;
    private JLabel appropriateness;
    private JLabel clarity;
    private JLabel methodology;
    private JLabel contribution;
    private JLabel recommendation;
    private JLabel Feedback_title;
    private JFrame frame;

    private JTable myTable;
    private Connection con = null;
    private Statement stmt = null;
    private ResultSet rst = null;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Feedback");
        frame.setContentPane(new Feedback().Feedback);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public Feedback(){

    }

    public Feedback(int idReviewer, int idManuscript) {
        frame = new JFrame("Feedback");
        frame.setContentPane(Feedback);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setLocation(540, 300);

        con = DatebaseConnection.connection();

        try {
            stmt = con.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Feedback_title.setText("Feedback Form of Manuscript ID " + idManuscript);

        appropriateness_select.addItem("-");
        clarity_select.addItem("-");
        methodology_select.addItem("-");
        contribution_select.addItem("-");
        for(int i = 1; i <= 10; i++) {
            appropriateness_select.addItem(i + "");
            clarity_select.addItem(i + "");
            methodology_select.addItem(i + "");
            contribution_select.addItem(i + "");
        }
        recommendation_select.addItem("-");
        recommendation_select.addItem("REVIEW-ACCEPT");
        recommendation_select.addItem("REVIEW-REJECT");

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

                    System.out.println("Feedback ID " + id_max);
                    String update = "UPDATE Assignment SET `idFeedback` = " + id_max + " WHERE `idManuscript` = " +  idManuscript +
                            " AND `idReviewer` = " + idReviewer;
                    System.out.println(update);
                    stmt.execute(update);

                    JOptionPane.showMessageDialog(frame, "Your feedback successfully submitted!");

                    stmt.close();
                    con.close();
                    frame.dispose();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }
}
