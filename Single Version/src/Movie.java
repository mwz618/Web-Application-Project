import java.util.ArrayList;
import java.util.HashSet;

public class Movie {

    private String title = "";

    private String year = "";

    private String director = "";

    private String keyInMain = "";

    private HashSet<String> genres;

    public Movie(){
        genres = new HashSet<>();
    }

    public Movie(String director){
        this.director = director;
        genres = new HashSet<>();
    }

    public String getKeyInMain() {
        return keyInMain;
    }

    public void setKeyInMain(String keyInMain) {
        this.keyInMain = keyInMain;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public HashSet<String> getGenres() {
        return genres;
    }

    public void setGenres(HashSet<String> genres) {
        this.genres = genres;
    }

    public void addGenre(String genre){
        genres.add(genre);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Movie Details - ");
        sb.append("title:" + getTitle());
        sb.append(", ");
        sb.append("year:" + getYear());
        sb.append(", ");
        sb.append("director:" + getDirector());
        sb.append(", ");
        sb.append("genre:");
        for (String s: genres){
            sb.append("\"" + s + "\"" +  "  ");
        };
        sb.append(".");

        return sb.toString();
    }
}

