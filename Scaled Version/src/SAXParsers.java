import java.awt.desktop.SystemEventListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;
import java.sql.*;
import java.util.*;
import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.mysql.cj.protocol.Resultset;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SAXParsers extends DefaultHandler {

    private String xmlFile;
    private String tempVal;
    private Connection conn;
    private final String rating = "-1";
    private DataSource dataSource;

    // variables for mains243.xml
    private int newMovieId = 0;
    private Movie tempMovie;
    private String tempDirector;
    private ArrayList<String> allGenres = new ArrayList<>();
    // {[title, year, director]...}
    private HashSet<ArrayList<String>> allMoviesInDB = new HashSet<>();
    // {movie key in main.xml : movieId in db}
    private HashMap<String, String> movieKeys = new HashMap<>();

    // variables for actor63.xml
    private int newStarId = 0;
    private Star tempStar;
    // {name:  ID}
    private HashMap<String, String> allStarsInDB = new HashMap<>();
    // {star name in main.xml : starId in db}
    private HashMap<String, String> starsNameId = new HashMap<>();

    // variables for casts124.xml
    private List<Cast> casts = new ArrayList<>();
    private Cast tempCast;

    // output
    BufferedWriter movies_csv = new BufferedWriter(new FileWriter("movies.csv"));
    BufferedWriter genres_csv = new BufferedWriter(new FileWriter("genres.csv"));
    BufferedWriter ratings_csv = new BufferedWriter(new FileWriter("ratings.csv"));
    BufferedWriter genres_in_movies_csv = new BufferedWriter(new FileWriter("genres_in_movies.csv"));
    BufferedWriter invalid_movies_txt = new BufferedWriter(new FileWriter("invalid_movies.txt"));
    BufferedWriter stars_csv = new BufferedWriter(new FileWriter("stars.csv"));
    BufferedWriter invalid_stars_txt = new BufferedWriter(new FileWriter("invalid_stars.txt"));
    BufferedWriter stars_in_movies_csv = new BufferedWriter(new FileWriter("stars_in_movies.csv"));
    BufferedWriter invalid_stars_in_movies_txt = new BufferedWriter(new FileWriter("invalid_stars_in_movies.txt"));

    public SAXParsers() throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException, IOException {
        try{
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?user=mytestuser&password=My6$Password&allowLoadLocalInfile=true");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        loadAllGenresInDB();
        loadAllMoviesInDB();
        loadAllStarsInDB();
    }

    public void loadAllGenresInDB() throws SQLException {
        String selectAllGenres = "select * from genres";
        PreparedStatement stmt = conn.prepareStatement(selectAllGenres);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()){
            allGenres.add(rs.getString("name"));
        }
        rs.close();
        stmt.close();
    }

    public void loadAllStarsInDB() throws SQLException {
        String selectAllStars = "select * from stars";
        PreparedStatement stmt = conn.prepareStatement(selectAllStars);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()){
            allStarsInDB.put(rs.getString("name"), rs.getString("id"));
        }
        rs.close();
        stmt.close();
    }

    public void loadAllMoviesInDB() throws SQLException {
        String selectAllMovies = "select * from movies";
        PreparedStatement stmt = conn.prepareStatement(selectAllMovies);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()){
            allMoviesInDB.add(new ArrayList<>(Arrays.asList(rs.getString("title"), rs.getString("year"), rs.getString("director"))));
        }
        rs.close();
        stmt.close();
    }


    int getNewId(String query) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(query);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        int newId = rs.getInt(1);
        rs.close();
        stmt.close();
        return newId;
    }

    public void runMovies(String file) throws SQLException {
        // get a new movie id
        newMovieId = getNewId("SELECT SUBSTRING(MAX(m.id) FROM LOCATE('tt', MAX(m.id)) + 2) + 1 AS num FROM movies m;");
        // parse document
        this.xmlFile = file;
        parseDocument();
    }

    public void runStars(String file) throws SQLException {
        // get a new star id
        newStarId = getNewId("SELECT SUBSTRING(MAX(s.id) FROM LOCATE('nm', MAX(s.id)) + 2) + 1 AS num FROM stars s;");
        // parse document
        this.xmlFile = file;
        parseDocument();
    }


    public void runCasts(String file) throws SQLException {
        this.xmlFile = file;
        parseDocument();
    }

    public void destructor() throws IOException, SQLException{
        for (int i = 0; i < allGenres.size(); i++){
            genres_csv.write((i + 1) + "\t" + allGenres.get(i) + "\n");
        }
        genres_csv.flush();

        genres_csv.close();
        movies_csv.close();
        ratings_csv.close();
        invalid_movies_txt.close();
        genres_in_movies_csv.close();
        stars_csv.close();
        invalid_stars_in_movies_txt.close();
        stars_in_movies_csv.close();
        invalid_stars_txt.close();

        // load data to database
        load("movies");
        load("genres");
        load("genres_in_movies");
        load("stars");
        load("stars_in_movies");
        load("ratings");
        conn.close();
    }

    public void load(String fileName) throws SQLException{
        String query = "load data local infile '" + fileName + ".csv' into table " + fileName + " columns terminated by '\t'";
        System.out.println(query);
        Statement stmt = conn.createStatement();
        stmt.execute(query);
        stmt.close();
    }

    private void parseDocument() {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();
            sp.parse(xmlFile, this);
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ( qName.equalsIgnoreCase("film")){
            tempMovie = new Movie(tempDirector);
        }else if (qName.equalsIgnoreCase("actor")){
            tempStar = new Star();
        }else if (qName.equalsIgnoreCase("m")){
            tempCast = new Cast();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("film")){
            try {
                if (tempMovie.getYear().isBlank() || tempMovie.getDirector().isBlank() || tempMovie.getTitle().isBlank() || !tempMovie.getYear().matches("[0-9]+") || tempMovie.getGenres().size() == 0){
                    String genreOutput = "";
                    for (String s : tempMovie.getGenres()){
                        genreOutput += s + " ";
                    }
                    invalid_movies_txt.write(tempMovie.getTitle() + "\t" + tempMovie.getYear() + "\t" + tempMovie.getDirector() + "\t" + genreOutput + "\n");
                    invalid_movies_txt.flush();
                }else if (!allMoviesInDB.contains(new ArrayList<>(Arrays.asList(tempMovie.getTitle(), tempMovie.getYear(), tempMovie.getDirector())))){
                    String mid = "tt0" + (newMovieId++);
                    movieKeys.put(tempMovie.getKeyInMain(), mid);
                    // write to movies_csv
                    movies_csv.write( mid + "\t" + tempMovie.getTitle() + "\t" + tempMovie.getYear() + "\t" + tempMovie.getDirector() + "\n");
                    movies_csv.flush();
                    // write to genres_in_movies_csv
                    for (String g : tempMovie.getGenres()){
                        genres_in_movies_csv.write( (allGenres.indexOf(g) + 1) + "\t" + mid + "\n");
                    }
                    genres_in_movies_csv.flush();
                    // write to ratings
                    ratings_csv.write(mid + "\t" + "-1" + "\t" + "-1" + "\n");
                    ratings_csv.flush();
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }else if (qName.equalsIgnoreCase("dirname")){
            tempDirector = tempVal.trim();
        }else if (qName.equalsIgnoreCase("year")){
            tempMovie.setYear(tempVal.trim());
        }else if (qName.equalsIgnoreCase("t") && xmlFile.endsWith("mains243.xml")){
            tempMovie.setTitle(tempVal.trim());
        }else if (qName.equalsIgnoreCase("cat")){
            tempMovie.addGenre(tempVal.trim());
            if (!allGenres.contains(tempVal.trim())){
                allGenres.add(tempVal.trim());
            }
        }else if (qName.equalsIgnoreCase("fid")){
            tempMovie.setKeyInMain(tempVal);
        }

        // stars
        if (qName.equalsIgnoreCase("actor")){
            try {
                if ((tempStar.getName().isBlank() || !tempStar.getBirthYear().matches("[0-9]+")) && !tempStar.getBirthYear().isBlank()){
                    invalid_stars_txt.write(tempStar.getName() + "\t" + tempStar.getBirthYear() + "\n");
                    invalid_stars_txt.flush();
                } else if (!allStarsInDB.containsKey(tempStar.getName())){
                    String sid = "nm" + (newStarId++);
                    stars_csv.write(sid + "\t" + tempStar.getName() + "\t" + tempStar.getBirthYear() + "\n");
                    stars_csv.flush();

                    starsNameId.put(tempStar.getName(), sid);
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }else if (qName.equalsIgnoreCase("stagename")){
            tempStar.setName(tempVal.trim());
        }else if (qName.equalsIgnoreCase("dob")){
            tempStar.setBirthYear(tempVal.trim());
        }

        // casts
        if (qName.equalsIgnoreCase("m") && xmlFile.endsWith("casts124.xml")){
            String sid = starsNameId.containsKey(tempCast.getStarName()) ? starsNameId.get(tempCast.getStarName()) : allStarsInDB.get(tempCast.getStarName());
            String mid = movieKeys.get(tempCast.getKeyInMain());
            try{
                // invalid input
                if (sid == null  || mid == null){
                    invalid_stars_in_movies_txt.write(tempCast.getStarName() + "\t" + tempCast.getKeyInMain() + "\n");
                    invalid_stars_in_movies_txt.flush();
                }else {
                    stars_in_movies_csv.write(sid + "\t" + mid + "\n");
                    stars_in_movies_csv.flush();
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }else if (qName.equalsIgnoreCase("f") && xmlFile.endsWith("casts124.xml")){
            tempCast.setKeyInMain(tempVal);
        }else if (qName.equalsIgnoreCase("a") && xmlFile.endsWith("casts124.xml")){
            tempCast.setStarName(tempVal.trim());
        }
    }

    public static void main(String[] args) throws IllegalAccessException, InstantiationException, ClassNotFoundException, SQLException, IOException {
        SAXParsers spe = new SAXParsers();
        spe.runMovies("WebContent/mains243.xml");
        spe.runStars("WebContent/actors63.xml");
        spe.runCasts("WebContent/casts124.xml");
        spe.destructor();
    }
}