import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // add recaptcha verification
        
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");

        if (gRecaptchaResponse == null || gRecaptchaResponse.length() == 0) {
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Please verify your identity through Recaptcha!");
            response.getWriter().write(responseJsonObject.toString());
            return;
        }
        

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            // Construct a query with parameter represented by "?"
            String query = "select password from customers where email = ?";

            // Declare our statement
            PreparedStatement statement = dbcon.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, username);

            // Perform the query
            ResultSet rs = statement.executeQuery();
            JsonObject responseJsonObject = new JsonObject();
            boolean success;

            // User exists
            if (rs.next()){
                // password matches
                String encryptedPassword = rs.getString("password");
                success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
                if (success) {
                    // Login Success
                    // set this user into the session
                    request.getSession().setAttribute("user", new User(username, "customer"));

                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "customer");
                }
                else{
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "incorrect password");
                }
            }
            else{
                // check if an employee tries to login
                String query1 = "select password from employees where email = ?";
                PreparedStatement statement1 = dbcon.prepareStatement(query1);
                statement1.setString(1, username);
                ResultSet rs1 = statement1.executeQuery();
                if (rs1.next()){
                    // password matches
                    String encryptedPassword = rs1.getString("password");
                    success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
                    if (success) {
                        request.getSession().setAttribute("user", new User(username, "employee"));
                        responseJsonObject.addProperty("status", "success");
                        responseJsonObject.addProperty("message", "employee");
                        System.out.println("here4");
                    }
                    else {
                        responseJsonObject.addProperty("status", "fail");
                        responseJsonObject.addProperty("message", "employee " + username + " doesn't exist");
                    }
                }
                else {
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "user " + username + " doesn't exist");
                }
                rs1.close();
                statement1.close();
            }

            // write JSON string to output
            response.getWriter().write(responseJsonObject.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

            rs.close();
            statement.close();
            dbcon.close();

        } catch (Exception e) {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            System.out.println("errorMessage");
            jsonObject.addProperty("errorMessage", e.getMessage());
            response.getWriter().write(jsonObject.toString());

            // set reponse status to 500 (Internal Server Error)
            response.setStatus(500);
        }
    }
}