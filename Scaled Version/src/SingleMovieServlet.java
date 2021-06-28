import com.google.gson.JsonArray;
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
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-movie"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 4L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");
        String addMovieId = request.getParameter("addMovieId");
        String addTitle = request.getParameter("addTitle");
        User user_info = (User)request.getSession().getAttribute("user");
        if (addMovieId != null) {
            user_info.addToCart(addMovieId, addTitle);
            return;
        }

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            // Construct a query with parameter represented by "?"
            String query = "with starMoviesCount as ( " +
                    "select sm.starId, s.name as starName, count(*) as starMoviesNum " +
                    "from stars s, stars_in_movies sm " +
                    "where s.id = sm.starId " +
                    "group by s.id) " +
                    "select m.id as movieId, m.title, m.year, m.director, r.rating, g.name as genreName, smc.starName, smc.starId, smc.starMoviesNum " +
                    "from movies m, stars_in_movies sm, starMoviesCount smc, genres_in_movies gm, genres g, ratings r " +
                    "where m.id = sm.movieId and sm.starId = smc.starId and m.id = gm.movieId and gm.genreId = g.id and m.id = r.movieId and m.id = ?" +
                    "order by smc.starMoviesNum desc, smc.starName asc;";
            // Declare our statement
            PreparedStatement statement = dbcon.prepareStatement(query);
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movieId", id);
                jsonObject.addProperty("title", rs.getString("title"));
                jsonObject.addProperty("year", rs.getString("year"));
                jsonObject.addProperty("director", rs.getString("director"));
                jsonObject.addProperty("rating", rs.getString("rating"));
                jsonObject.addProperty("genreName", rs.getString("genreName"));
                jsonObject.addProperty("starName", rs.getString("starName"));
                jsonObject.addProperty("starId", rs.getString("starId"));
                jsonObject.addProperty("starMoviesNum", rs.getString("starMoviesNum"));
                jsonArray.add(jsonObject);
            }

            String currentPage = user_info.getCurrentPage();
            String numRecords = user_info.getNumRecords();
            String title = user_info.getTitle();
            String year = user_info.getYear();
            String director = user_info.getDirector();
            String starName = user_info.getStarName();
            String genre = user_info.getGenre();
            String acronym = user_info.getAcronym();
            String sortBy = user_info.getSortBy();
            
            JsonObject temp = new JsonObject();
            temp.addProperty("currentPage", currentPage);
            temp.addProperty("numRecords", numRecords);
            temp.addProperty("title", title);
            temp.addProperty("year", year);
            temp.addProperty("director", director);
            temp.addProperty("starName", starName);
            temp.addProperty("genre", genre);
            temp.addProperty("acronym", acronym);
            temp.addProperty("sortBy", sortBy);
            System.out.println(sortBy);
            jsonArray.add(temp);

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
            System.out.println("errorMessage");
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // set reponse status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        out.close();

    }

}
