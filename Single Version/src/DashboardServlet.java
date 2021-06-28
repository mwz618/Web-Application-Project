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
import java.sql.*;

@WebServlet(name = "DashboardServlet", urlPatterns = "/api/dashboard")
public class DashboardServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

        JsonObject responseJsonObject = new JsonObject();

        User userInfo = (User)request.getSession().getAttribute("user");
        if (userInfo.getKind().equals("customer")){
            responseJsonObject.addProperty("access", "no");
            response.getWriter().write(responseJsonObject.toString());
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            if (request.getParameter("addType").equals("star")){
                String starName = request.getParameter("starName");
                String birthYear = request.getParameter("birthYear");
                if (birthYear.isBlank()) birthYear = null;

                System.out.println(starName);
                System.out.println(birthYear);

                // get the new star id
                String getNewStarId = "with starIdNum as ( \n" +
                        "SELECT SUBSTRING(MAX(s.id) FROM LOCATE('nm', MAX(s.id)) + 2) + 1 AS num \n " +
                        "FROM stars s)\n " +
                        "SELECT CONCAT('nm', substring('0000000', 1,  7 - char_length(s.num)), s.num) as newStarId\n " +
                        "FROM starIdNum s;";

                PreparedStatement selectStatement = conn.prepareStatement(getNewStarId);
                ResultSet selectRs = selectStatement.executeQuery();
                selectRs.next();
                String newStarId = selectRs.getString("newStarId");
                selectRs.close();
                selectStatement.close();

                // insert to stars table
                String insert = "insert into stars values (?, ?, ?)";
                PreparedStatement insertStatement = conn.prepareStatement(insert);
                insertStatement.setString(1, newStarId);
                insertStatement.setString(2, starName);
                insertStatement.setString(3, birthYear);
                insertStatement.executeUpdate();

                insertStatement.close();

                System.out.println("add_star executed successfully");
                responseJsonObject.addProperty("message", "Add Star " + starName + " with ID " + newStarId + " Successfully!");

            }else  if (request.getParameter(("addType")).equals("movie")){
                String title = request.getParameter("title");
                String year = request.getParameter("year");
                String director = request.getParameter("director");
                String genre  = request.getParameter("genre");
                String starName = request.getParameter("starName");
                int rating  = (request.getParameter("rating") == null || request.getParameter("rating").trim() == "") ? -1 : Integer.parseInt(request.getParameter("rating"));

                System.out.println(starName);
                System.out.println(title);
                System.out.println(year);
                System.out.println(director);
                System.out.println(genre);
                System.out.println(rating);

                //  call add_move procedure
                CallableStatement statement = conn.prepareCall("{call add_movie(?, ?, ?, ?, ?, ?)}");
                statement.setString(1, title);
                statement.setString(2, year);
                statement.setString(3, director);
                statement.setString(4, starName);
                statement.setString(5, genre);
                statement.setInt(6, rating);

                ResultSet rs = statement.executeQuery();
                rs.next();

                if (rs.getInt("success") == 0 ) {
                    responseJsonObject.addProperty("message", title + " cannot be added because of duplication!!!");
                }
                else {
                    String message = title + " added successfully with generated movieId: " + rs.getString("movieId");
                    System.out.println(rs.getInt("existingGenre"));
                    if (rs.getInt("existingGenre") == 0) {
                        System.out.println("here1");
                        message += ", generated genreId: " + rs.getString("genreId");
                    }
                    else {
                        System.out.println("here2");
                        message += ", existed genreId: " + rs.getString("genreId");
                    }
                    System.out.println(rs.getInt("existingStar"));
                    if (rs.getInt("existingStar") == 0) {
                        System.out.println("here3");
                        message += ", and generated starId: " + rs.getString("starId");
                    }
                    else {
                        System.out.println("here4");
                        message += ", and existed starId: " + rs.getString("starId");
                    }
                    System.out.println("message is: " + message);
                    responseJsonObject.addProperty("message", message);
                }
                rs.close();
                statement.close();

                // change to alert better close easily
            }
            responseJsonObject.addProperty("status", "success");
            response.setStatus(200);
            response.getWriter().write(responseJsonObject.toString());

        }catch (Exception e) {
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Adding Error!");
            // write JSON string to output
            response.getWriter().write(responseJsonObject.toString());
            // set response status to 200 (OK)
            response.setStatus(200);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPost(request, response);
    }
}
