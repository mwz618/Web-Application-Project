import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.sql.PreparedStatement;

/**
 * A servlet that takes input from a html <form> and talks to MySQL moviedbexample,
 * generates output as a html <table>
 */

// Declaring a WebServlet called FormServlet, which maps to url "/form"
@WebServlet(name = "BrowseServlet", urlPatterns = "/api/browse")
public class BrowseServlet extends HttpServlet {

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    // Use http GET
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");

        PrintWriter out = response.getWriter();
        JsonArray jsonArray = new JsonArray();
        System.out.println("print: " + request.getParameter("browseType"));

        try{
            if (request.getParameter("browseType").equals("genre")){
                Connection dbcon = dataSource.getConnection();
                String query = "select name from genres;";

                // Declare our statement
                PreparedStatement statement = dbcon.prepareStatement(query);

                // Perform the query
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    jsonArray.add(rs.getString("name"));
                }

                rs.close();
                statement.close();
                dbcon.close();
            }else if (request.getParameter("browseType").equals("acronym")){
                for (int i = 0; i < 10; i++){
                    jsonArray.add(Integer.toString(i));
                }
                for (char c = 'A'; c <= 'Z'; c++){
                    jsonArray.add(Character.toString(c));
                }
                jsonArray.add("*");
            }
            // jsonArray = [genre..., userKind]
            User info = (User)request.getSession().getAttribute("user");
            jsonArray.add(info.getKind());
            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // set reponse status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        out.close();

    }
}