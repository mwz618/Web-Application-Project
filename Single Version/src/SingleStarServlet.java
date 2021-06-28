

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

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet {
	private static final long serialVersionUID = 2L;

	// Create a dataSource which registered in web.xml
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

		// Output stream to STDOUT
		PrintWriter out = response.getWriter();

		try {
			// Get a connection from dataSource
			Connection dbcon = dataSource.getConnection();

			// Construct a query with parameter represented by "?"
			String query = "select s.name as starName, s.birthYear, m.title as movieTitle, m.id as movieId, m.year as movieYear, m.director from stars s, movies m, stars_in_movies sm where sm.starId = s.id and m.id = sm.movieId and s.id = ? order by m.year desc, m.title asc;";

			// Declare our statement
			PreparedStatement statement = dbcon.prepareStatement(query);

			// Set the parameter represented by "?" in the query to the id we get from url,
			// num 1 indicates the first "?" in the query
			statement.setString(1, id);

			// Perform the query
			ResultSet rs = statement.executeQuery();

			JsonArray jsonArray = new JsonArray();

			// Iterate through each row of rs
			while (rs.next()) {
				// Create a JsonObject based on the data we retrieve from rs
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("starId", id);
				jsonObject.addProperty("starName", rs.getString("starName"));
				jsonObject.addProperty("birthYear", rs.getString("birthYear"));
				jsonObject.addProperty("movieTitle", rs.getString("movieTitle"));
				jsonObject.addProperty("movieId", rs.getString("movieId"));
				jsonObject.addProperty("movieYear", rs.getString("movieYear"));
				jsonObject.addProperty("director", rs.getString("director"));
				if (rs.getString("birthYear") == null){
					jsonObject.addProperty("birthYear", "N/A");
				}

				jsonArray.add(jsonObject);
			}

			User user_info = (User)request.getSession().getAttribute("user");
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
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());

			// set reponse status to 500 (Internal Server Error)
			response.setStatus(500);
		}
		out.close();

	}

}
