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
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


// Declaring a WebServlet called StarsServlet, which maps to url "/api/top-rating-movies"
@WebServlet(name = "TopRatingMoviesServlet", urlPatterns = "/api/top-rating-movies")
public class TopRatingMovies extends HttpServlet{
    private static final long serialVersionUID = 3L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        long TS_start = System.nanoTime();
        long TJ_start = -1;

        long TS_end = -1;
        long TJ_end = -1;

        long TS_elapsed = -1;
        long TJ_elapsed = -1;

        response.setContentType("application/json");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        System.out.println("start printing");

        String copy_currentPage = request.getParameter("currentPage");
        if (copy_currentPage == null) { copy_currentPage = "";}
        String copy_numRecords = request.getParameter("numRecords");
        if (copy_numRecords == null) { copy_numRecords = "";}
        String copy_title = request.getParameter("title");
        if (copy_title == null) { copy_title = "";}
        String copy_year = request.getParameter("year") ;
        if (copy_year == null) { copy_year = "";}
        String copy_director = request.getParameter("director");
        if (copy_director == null) { copy_director = "";}
        String copy_starName = request.getParameter("starName");
        if (copy_starName == null) { copy_starName = "";}
        String copy_genre = request.getParameter("genre");
        if (copy_genre == null) { copy_genre = "";}
        String copy_acronym = request.getParameter("acronym");
        if (copy_acronym == null) { copy_acronym = "";}
        String copy_sortBy = request.getParameter("sortBy");
        System.out.println("copy_sortby is: " + copy_sortBy);
        if (copy_sortBy == null) { copy_sortBy = "";}

        String currentPage = request.getParameter("currentPage");
        String numRecords = request.getParameter("numRecords");
        String year = request.getParameter("year") == null || request.getParameter("year").isEmpty() ? "%" : request.getParameter("year");
        String director = request.getParameter("director") == null || request.getParameter("director").isEmpty() ? "%" : "%" + request.getParameter("director") + "%";
        String starName = request.getParameter("starName") == null || request.getParameter("starName").isEmpty() ? "%" : "%" + request.getParameter("starName") + "%";
        String genre = request.getParameter("genre") == null || request.getParameter("genre").isEmpty() ? "%" : request.getParameter("genre");
        String acronym = request.getParameter("acronym") == null || request.getParameter("acronym").isEmpty() ? "m.title like '%'"
                : (request.getParameter("acronym").equals("*") ? "m.title REGEXP '^[^a-z0-9A-z]'"
                : "m.title like '" + request.getParameter("acronym") + "%'");
        String sortBy = request.getParameter("sortBy");
        String addMovieId = request.getParameter("addMovieId");
        String addTitle = request.getParameter("addTitle");

        int fuzzy_title_len = (copy_title.length() + 4) / 5;
        int fuzzy_director_len = (copy_director.length() + 4) / 5;
        int fuzzy_starName_len = (copy_starName.length() + 4) / 5;


        String title = "(match (m.title) against ('";
        if (request.getParameter("title") == null || request.getParameter("title").isEmpty()){
            title = "";
        }else{
            for (String word : request.getParameter("title").split("\\s+")){
                title += "+" + word + "* ";
            }
            title += String.format("' in boolean mode) or edth(lower(m.title), lower('%s'), %d)) and", copy_title, fuzzy_title_len);
        }

        System.out.println("title: "  +  title);
        System.out.println("year: " + year);
        System.out.println("director: " + director);
        System.out.println("starName: " + starName);
        System.out.println("currentPage: " + currentPage);
        System.out.println("genre: " + genre);
        System.out.println("acronym: " + acronym);
        System.out.println("numRecords: " + numRecords);
        System.out.println("sortBy: " + sortBy);
        System.out.println("addMovieId: " + addMovieId);
        System.out.println("addTitle: " + addTitle);

        try  {
            Connection conn = dataSource.getConnection();

            User user_info = (User)request.getSession().getAttribute("user");
            if (addMovieId != null) {
                user_info.addToCart(addMovieId, addTitle);
                conn.close();
                return;
            }
            user_info.setCurrentPage(copy_currentPage);
            user_info.setNumRecords(copy_numRecords);
            user_info.setTitle(copy_title);
            user_info.setYear(copy_year);
            user_info.setDirector(copy_director);
            user_info.setStarName(copy_starName);
            user_info.setGenre(copy_genre);
            user_info.setAcronym(copy_acronym);
            user_info.setSortBy(copy_sortBy);


            // query1 &  rs1: retrieve top 20
            // change limit 100 below
            String query = String.format(
                    "with starMoviesCount as ( \n" +
                            "select sm.starId, count(*) as starMoviesNum from stars s, stars_in_movies sm where s.id = sm.starId group by s.id), \n" +
                            "starsMovies as ( \n" +
                            "select s.id as starId, s.name as starName, sm.movieId from stars s, stars_in_movies sm where s.id = sm.starId), \n" +
                            "genresMovies as ( \n" +
                            "select g.name as genreName, gm.movieId from genres g, genres_in_movies gm where g.id = gm.genreId), \n" +
                            "topRatingMovies as ( \n" +
                            "select m.id as movieId, m.title, m.year, m.director, r.rating\n" +
                            "from movies m, starsMovies sm, genresMovies gm, ratings r\n" +
                            "where m.id = sm.movieId and m.id = gm.movieId and m.id = r.movieId\n" +
                            "    and %s %s and m.year like '%s' and (m.director like '%s' or edth(lower(m.director), lower('%s'), %d))     \n" +
                            "\tand (sm.starName like '%s' or edth(lower(sm.starName), lower('%s'), %d))    \n" +
                            "\tand gm.genreName like '%s' \n" +
                            "group by m.id, rating\n" +
                            "order by %s\n" +
                            "limit %s offset %d\n" +
                            ")\n" +
                            "\n" +
                            "select trm.movieId, trm.title, trm.year, trm.director, trm.rating, sm.starId, sm.starName, gm.genreName, smc.starMoviesNum\n" +
                            "from topRatingMovies trm, starsMovies sm, genresMovies gm, ratings r, starMoviesCount smc \n" +
                            "where trm.movieId = sm.movieId and trm.movieId = gm.movieId and trm.movieId = r.movieId and sm.starId = smc.starId;\n",
                    title, acronym, year, director, copy_director, fuzzy_director_len, starName, copy_starName, fuzzy_starName_len, genre, sortBy, numRecords, Integer.parseInt(numRecords) * Integer.parseInt(currentPage));

            System.out.println("query: " + query);

            TJ_start = System.nanoTime();

            PreparedStatement statement = conn.prepareStatement(query);

            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();

            // Add results to the jsonArrays
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
                jsonObject.addProperty("starMoviesNum", rs.getString("starMoviesNum"));
                jsonArray.add(jsonObject);
            }

            JsonObject jsonObject1 = new JsonObject();
            jsonObject1.addProperty("numRecords", copy_numRecords);

            String[] str = copy_sortBy.split(", ", 2);
            String left = str[0];
            String right = str[1];
            String[] ll = left.split(" ", 2);
            String[] rr = right.split(" ", 2);
            jsonObject1.addProperty("firstSort", ll[0]);
            jsonObject1.addProperty("firstSortOrder", ll[1]);
            jsonObject1.addProperty("secondSort", rr[0]);
            jsonObject1.addProperty("secondSortOrder", rr[1]);

            jsonArray.add(jsonObject1);

            out.write(jsonArray.toString());
            response.setStatus(200);

            rs.close();
            statement.close();
            conn.close();

            TJ_end = System.nanoTime();

        } catch (Exception e) {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // set reponse status to 500 (Internal Server Error)
            response.setStatus(500);

            TJ_end = System.nanoTime();
        }
        out.close();

        TS_end = System.nanoTime();

        // output to files
        String path = request.getSession().getServletContext().getRealPath("/");
        BufferedWriter TS_output = new BufferedWriter(new FileWriter(path + "P5_Task4/TS.txt", true));
        BufferedWriter TJ_output = new BufferedWriter(new FileWriter(path + "P5_Task4/TJ.txt", true));

        synchronized (TS_output){
            TS_elapsed = TS_end - TS_start;
            TS_output.write(String.valueOf(TS_elapsed) + "\n");
        }

        synchronized (TJ_output){
            TJ_elapsed = TJ_end - TJ_start;
            TJ_output.write(String.valueOf(TJ_elapsed) + "\n");
        }

        TS_output.close();
        TJ_output.close();
        
    }
}
