import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

// server endpoint URL
@WebServlet("/title-suggestion")

public class TitleSuggestion extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // setup the response json arrray
            JsonArray jsonArray = new JsonArray();

            // get the query string from parameter
            String query = request.getParameter("query");

            // return the empty json array if query is null or empty
            // ??: ' ' counts as a char
            if (query == null || query.trim().isEmpty()) {
                response.getWriter().write(jsonArray.toString());
                return;
            }

            System.out.println("enter title-suggestion java code");

            // ???: movies: good bad &  bad good | search: g d | should bad good comes out?


            // search on superheroes and add the results to JSON Array
            // this example only does a substring match
            // TODO: in project 4, you should do full text search with MySQL to find the matches on movies and stars
            int fuzzy_title_len = (query.length() + 4) / 5;

            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?user=mytestuser&password=My6$Password&allowLoadLocalInfile=true");

            String titleQuery = "select m.id, m.title from movies m, ratings r where m.id = r.movieId and (match (m.title) against ('";
            for (String word : query.split("\\s+")){
                titleQuery += "+" + word + "* ";
            }
            titleQuery += String.format("' in boolean mode) or edth(lower(m.title), lower('%s'), %d)) order by r.rating desc, m.title asc limit 10;", query, fuzzy_title_len);
            System.out.println("titleQuery: " + titleQuery);

            PreparedStatement stmt = conn.prepareStatement(titleQuery);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()){
                jsonArray.add(generateJsonObject(rs.getString("id"), rs.getString("title")));
            }

            response.getWriter().write(jsonArray.toString());
            return;
        } catch (Exception e) {
            System.out.println(e);
            response.sendError(500, e.getMessage());
        }
    }

    private static JsonObject generateJsonObject(String movieId, String title) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", title);

        JsonObject additionalDataJsonObject = new JsonObject();
        additionalDataJsonObject.addProperty("movieId", movieId);

        jsonObject.add("data", additionalDataJsonObject);
        return jsonObject;
    }
}
