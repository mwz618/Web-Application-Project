import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class UpdateSecurePassword {

    /*
     * 
     * This program updates your existing moviedb customers table to change the
     * plain text passwords to encrypted passwords.
     * 
     * You should only run this program **once**, because this program uses the
     * existing passwords as real passwords, then replace them. If you run it more
     * than once, it will treat the encrypted passwords as real passwords and
     * generate wrong values.
     * 
     */
    public static void main(String[] args) throws Exception {

        String loginUser = "mytestuser";
        String loginPasswd = "My6$Password";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        String[] updateUsers = {"customers", "employees"};
        for (String users:  updateUsers) {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
            Statement statement = connection.createStatement();
            String key = users.equals("customers") ? "id" :  "email";

            // change the customers/employees table password column from VARCHAR(20) to VARCHAR(128)
            String alterQuery = String.format("ALTER TABLE %s MODIFY COLUMN password VARCHAR(128);", users);
            int alterResult = statement.executeUpdate(alterQuery);
            System.out.println("altering " + users + " table schema completed, " + alterResult + " rows affected");

            // get the ID and password for each customer
            // or get the email and password for each customer
            String query = String.format("SELECT %s, password from %s;", key, users);

            ResultSet rs = statement.executeQuery(query);

            // we use the StrongPasswordEncryptor from jasypt library (Java Simplified Encryption)
            //  it internally use SHA-256 algorithm and 10,000 iterations to calculate the encrypted password
            PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();

            ArrayList<String> updateQueryList = new ArrayList<>();

            System.out.println("encrypting password in " + users + " table (this might take a while)");
            while (rs.next()) {
                // get plain text password from current table
                String password = rs.getString("password");

                // encrypt the password using StrongPasswordEncryptor
                String encryptedPassword = passwordEncryptor.encryptPassword(password);

                // generate the update query
                String updateQuery = String.format("UPDATE %s SET password = '%s' WHERE %s = '%s';", users, encryptedPassword, key, rs.getString(key));
                updateQueryList.add(updateQuery);
            }
            rs.close();

            // execute the update queries to update the password
            System.out.println("updating password in " + users + " table");
            int count = 0;
            for (String updateQuery : updateQueryList) {
                int updateResult = statement.executeUpdate(updateQuery);
                count += updateResult;
            }
            System.out.println("updating password completed, " + count + " rows affected");

            statement.close();
            connection.close();

            System.out.println("finished");
        }

    }

}
