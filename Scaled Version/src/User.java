import java.util.HashMap;

/**
 * This User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
public class User {
    private final String username;
    private String currentPage = "0";
    private String numRecords = "25";
    private String title = "";
    private String year = "";
    private String director = "";
    private String starName = "";
    private String genre = "";
    private String acronym = "";
    private String sortBy = "rating desc, title asc";
    private String saleId;
    private String kind;

    // {addMovieId, addMovieCount}
    private HashMap<String, Integer> cart;
    // {addMovieId, addTitle}
    private HashMap<String, String> movieIdTitle;

    public User(String username, String kind) {
        this.username = username;
        this.cart = new HashMap<String, Integer>();
        this.movieIdTitle = new HashMap<String, String>();
        this.kind = kind;
    }

    public String getSaleId () { return this.saleId; }
    public String getCurrentPage() { return this.currentPage; }
    public String getNumRecords() { return this.numRecords; }
    public String getTitle() { return this.title; }
    public String getYear() { return this.year; }
    public String getDirector() { return this.director; }
    public String getStarName() { return this.starName; }
    public String getGenre() { return this.genre; }
    public String getAcronym() { return this.acronym; }
    public String getSortBy() { return this.sortBy; }
    public String getKind() { return kind; }


    public void setSaleId (String saleId) { this.saleId = saleId; }
    public void setCurrentPage(String currentPage) {  this.currentPage = currentPage; }
    public void setNumRecords(String numRecords) { this.numRecords = numRecords; }
    public void setTitle(String title) { this.title = title; }
    public void setYear(String year) { this.year = year; }
    public void setDirector(String director) { this.director = director; }
    public void setStarName(String starName) { this.starName = starName; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setAcronym(String acronym) { this.acronym = acronym; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
    public void setKind(String kind) { this.kind = kind; }

    // paras: addMovieId, addTitle
    // add movie to the cart and movieIdTitle
    public void addToCart(String id, String title) {
        movieIdTitle.put(id, title);
        cart.put(id, cart.getOrDefault(id, 0) + 1);
    }

    public void removeFromCart(String id) {
        cart.remove(id);
        movieIdTitle.remove(id);
    }

    public void clearCart() {
        cart = new HashMap<>();
        movieIdTitle = new HashMap<>();
    }

    public void minusFromCart(String id) {
        int qty = cart.get(id) - 1;
        if (qty <= 0) {
            cart.remove(id);
            movieIdTitle.remove(id);
        }
        else {
            cart.put(id, qty);
        }
    }

    public int totalPrice() {
        int total = 0;
        for (String id : this.cart.keySet()) {
            total += this.cart.get(id) * 20; // every movie is $20
        }
        return total;
    }

    public HashMap<String, Integer> getCart() {
        return this.cart;
    }
    public HashMap<String, String> getMovieIdTitle() {  return this.movieIdTitle; }
    public String getTitle(String movieId) { return this.movieIdTitle.get(movieId); }

}