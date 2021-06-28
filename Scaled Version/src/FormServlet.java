
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
@WebServlet(name = "FormServlet", urlPatterns = "/form")
public class FormServlet extends HttpServlet {

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

        String title = request.getParameter("title") == null ? request.getParameter("title") : "%";
        String year = request.getParameter("year") == null ?  request.getParameter("year") : "%";
        String director = request.getParameter("director") == null ?  request.getParameter("director") : "%";
        String starName = request.getParameter("starName") == null ?  request.getParameter("starName") : "%";

        PrintWriter out = response.getWriter();

        try{
            Connection dbcon = dataSource.getConnection();
            String query = "select m.id as movieId, m.title, m.year, m.director, s.name as starName, s.id as starId, g.name as genreName, r.rating " +
                    "from movies m, ratings r, stars_in_movies sm, stars s, genres_in_movies gm, genres g " +
                    "where m.title like ? and m.year like ? and m.director like ? and s.name like ? and m.id = r.movieId and m.id = sm.movieId and sm.starId = s.id and m.id = gm.movieId and gm.genreId = g.id;";

            // Declare our statement
            PreparedStatement statement = dbcon.prepareStatement(query);
            statement.setString(1, title);
            statement.setString(2, year);
            statement.setString(3, director);
            statement.setString(4, starName);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();
            while (rs.next()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movieId", rs.getString("movieId"));
                jsonObject.addProperty("title", rs.getString("title"));
                jsonObject.addProperty("year", rs.getString("year"));
                jsonObject.addProperty("director", rs.getString("director"));
                jsonObject.addProperty("starName", rs.getString("starName"));
                jsonObject.addProperty("starId", rs.getString("starId"));
                jsonObject.addProperty("genreName", rs.getString("genreName"));
                jsonObject.addProperty("rating", rs.getString("rating"));

                jsonArray.add(jsonObject);
            }
            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

            rs.close();
            statement.close();
            dbcon.close();
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
