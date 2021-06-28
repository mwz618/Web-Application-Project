package edu.uci.ics.fabflixmobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

public class ListViewActivity extends Activity {
    int currentPage = 0;
    private Button prev;
    private Button next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview);
        prev = findViewById(R.id.prev);
        prev.setEnabled(false);
        next = findViewById(R.id.next);
        prev.setOnClickListener(view -> pressPrev());
        next.setOnClickListener(view -> pressNext());
        showMovies();
    }

    private void showMovies(){
        // TODO: this should be retrieved from the backend server
        final ArrayList<Movie> movies = new ArrayList<>();
        HashSet<String> hasMovieIds = new HashSet<>();
        Bundle bundle = getIntent().getExtras();

        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // request type is POST
        final StringRequest loginRequest = new StringRequest(
                Request.Method.GET,
                BaseURLConstants.baseURL + "/api/top-rating-movies" + getParamString(currentPage, bundle.getString("title"), bundle.getString("director"), bundle.getString("year"), bundle.getString("starName")),
                response -> {
                    try {
                        System.out.println(response);
                        JSONArray jsonResponse = new JSONArray(response);
                        for (int i = 0; i < jsonResponse.length() - 1; i++){
                            JSONObject movie = jsonResponse.getJSONObject(i);
                            if (!hasMovieIds.contains(movie.getString("movieId"))){
                                movies.add(new Movie(movie.getString("movieId"), movie.getString("title"), movie.getString("year"), movie.getString("director"), movie.getString("rating")));
                                hasMovieIds.add(movie.getString("movieId"));
                            }
                            movies.get(movies.size() - 1).addGenreName(movie.getString("genreName"));
                            movies.get(movies.size() - 1).addStarName(movie.getString("starId"), movie.getString("starName"), movie.getString("starMoviesNum"));
                        }

                        MovieListViewAdapter adapter = new MovieListViewAdapter(movies, this);

                        ListView listView = findViewById(R.id.list);
                        listView.setAdapter(adapter);

                        listView.setOnItemClickListener((parent, view, position, id) -> {
                            Movie movie = movies.get(position);
                            String message = String.format("Clicked on position: %d, id: %s, title: %s, year: %s", position, movie.getId(), movie.getTitle(), movie.getYear());
                            System.out.println(message);

                            Intent singleMovie = new Intent(ListViewActivity.this, SingleMovie.class);
                            // Create the bundle
                            Bundle clickBundle = new Bundle();
                            // Add your data to bundle
                            clickBundle.putString("movieId", movie.getId());
                            // Add the bundle to the intent
                            singleMovie.putExtras(clickBundle);
                            // Display the search results
                            startActivity(singleMovie);
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // error
                    Log.d("login.error", error.toString());
                }) {};

        // important: queue.add is where the login request is actually sent
        queue.add(loginRequest);

        if (this.currentPage == 0) {
            prev.setEnabled(false);
        }
    }

    private String getParamString(int currentPage, String title, String director, String year, String starName) {
        String params = "";
        params += "?numRecords=20";
        params += "&currentPage=" + currentPage;
        params += "&title=" + title;
        params += "&director=" + director;
        params += "&year=" + year;
        params += "&starName=" + starName;
        params += "&sortBy=rating desc, title asc";
        return params;
    }

    public void pressPrev() {
        System.out.println("click prev");
        if (currentPage - 1 >= 0) {
            System.out.println("this.currentPage is: " + currentPage);
            currentPage -= 1;
            System.out.println("after click prev this.currentPage is: " + currentPage);
            showMovies();
        }
        else {
            prev.setEnabled(false);
        }
    }

    public void pressNext() {
        System.out.println("click next");
        prev.setEnabled(true);
        currentPage += 1;
        showMovies();
    }
}