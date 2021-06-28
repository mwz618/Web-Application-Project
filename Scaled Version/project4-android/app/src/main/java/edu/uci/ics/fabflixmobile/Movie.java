package edu.uci.ics.fabflixmobile;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class Movie {
    private final String id;
    private final String title;
    private final String year;
    private final String director;
    private final String rating;
    private ArrayList<String> genreNames;
    // {starId : [starName, starMoviesNum]
    private HashMap<String, ArrayList<String>> starsMap;

    public Movie(String id, String title, String year, String director, String rating) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
        this.rating = rating;
        genreNames = new ArrayList<>();
        starsMap = new HashMap<>();
    }

    public String getId() {return id;}

    public String getTitle() {
        return title;
    }

    public String getYear() {
        return year;
    }

    public String getDirector() { return director; }

    public String getRating() { return  rating; }

    public ArrayList<String> getGenreNames() { return genreNames; }

    public HashMap<String, ArrayList<String>> getStarNames() { return starsMap; }

    public void addGenreName(String genre){
        if (!genreNames.contains(genre)) genreNames.add(genre);
    }

    public void addStarName(String starId, String starName, String starMoviesNum){
        starsMap.put(starId, new ArrayList<>(Arrays.asList(starName, starMoviesNum)));
    }

    public String getSortedStarNames(int numLimit){
        if (numLimit == 0) numLimit = starsMap.size();
        ArrayList<String> sortedStarId = new ArrayList<>(starsMap.keySet());
        Collections.sort(sortedStarId, (a, b) -> starsMap.get(a).get(0).compareTo(starsMap.get(b).get(0)));
        Collections.sort(sortedStarId, (a, b) -> starsMap.get(b).get(1).compareTo(starsMap.get(a).get(1)));
        String topThree = "";
        for (int i = 0; i < Math.min(sortedStarId.size(), numLimit); i++){
            topThree += starsMap.get(sortedStarId.get(i)).get(0) + ", ";
        }
        return topThree.substring(0, topThree.length() - 2);
    }

    public String getSortedGenreNames(int numLimit) {
        if (numLimit == 0) numLimit = genreNames.size();
        Collections.sort(genreNames);
        String topThree = "";
        for (int i = 0; i < Math.min(genreNames.size(), numLimit); i++){
            topThree += genreNames.get(i) + ", ";
        }
        return topThree.substring(0, topThree.length() - 2);
    }

}