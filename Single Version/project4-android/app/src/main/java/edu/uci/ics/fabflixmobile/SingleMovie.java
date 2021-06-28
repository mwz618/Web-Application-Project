package edu.uci.ics.fabflixmobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class SingleMovie extends Activity{
    //private TextView movieId;
    private TextView title;
    private TextView year;
    private TextView director;
    private TextView rating;
    private TextView genreNames;
    private TextView starNames;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.singlemovie);
        title = findViewById(R.id.title);
        year = findViewById(R.id.year);
        director = findViewById(R.id.director);
        starNames = findViewById(R.id.starNames);
        rating = findViewById(R.id.rating);
        genreNames = findViewById(R.id.genreNames);
        showSingleMovie();
    }

    private void showSingleMovie(){

        Bundle bundle = getIntent().getExtras();

        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // request type is POST
        final StringRequest loginRequest = new StringRequest(
                Request.Method.GET,
                BaseURLConstants.baseURL + "/api/single-movie?id=" + bundle.getString("movieId"),
                response -> {
                    System.out.println(response);
                    System.out.println("*****" + bundle.getString("movieId"));

                    try {
                        Movie movie = null;
                        JSONArray jsonResponse = new JSONArray(response);
                        System.out.println("****" + jsonResponse);
                        for (int i = 0; i < jsonResponse.length() - 1; i++){
                            JSONObject m = jsonResponse.getJSONObject(i);
                            System.out.println(m);
                            if (movie == null){
                                movie = new Movie(m.getString("movieId"), m.getString("title"), m.getString("year"), m.getString("director"), m.getString("rating"));
                            }
                            movie.addGenreName(m.getString("genreName"));
                            movie.addStarName(m.getString("starId"), m.getString("starName"), m.getString("starMoviesNum"));
                        }

                        title.setText(movie.getTitle());
                        year.setText("Year: " + movie.getYear());
                        director.setText("Director: " + movie.getDirector());
                        rating.setText("Rating: " + movie.getRating());
                        genreNames.setText("Genre(s): " + movie.getSortedGenreNames(0));
                        starNames.setText("Star(s): " + movie.getSortedStarNames(0));

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
    }

}
