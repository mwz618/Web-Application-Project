package edu.uci.ics.fabflixmobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;

public class MainPage extends Activity {

    private EditText title;
    private EditText year;
    private EditText director;
    private EditText starName;
    private TextView message;
    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainpage);
        title = findViewById(R.id.title);
        year = findViewById(R.id.year);
        director = findViewById(R.id.director);
        starName = findViewById(R.id.starName);
        message = findViewById(R.id.message);
        searchButton = findViewById(R.id.search);

        title.setOnKeyListener((v, keyCode, event) -> enterKey(v, keyCode, event));
        year.setOnKeyListener((v, keyCode, event) -> enterKey(v, keyCode, event));
        director.setOnKeyListener((v, keyCode, event) -> enterKey(v, keyCode, event));
        starName.setOnKeyListener((v, keyCode, event) -> enterKey(v, keyCode, event));

        //assign a listener to call a function to handle the user request when clicking a button
        searchButton.setOnClickListener(view -> search());
    }

    private boolean enterKey(View v, int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            System.out.println("Enter key clicked!");
            searchButton.performClick();
            return true;
        }
        return false;
    }


    private void search() {
        message.setText("Trying to search");
        Intent listPage = new Intent(MainPage.this, ListViewActivity.class);
        // Create the bundle
        Bundle bundle = new Bundle();
        // Add your data to bundle
        bundle.putString("title", title.getText().toString());
        bundle.putString("director", director.getText().toString());
        bundle.putString("year", year.getText().toString());
        bundle.putString("starName", starName.getText().toString());
        // Add the bundle to the intent
        listPage.putExtras(bundle);
        // Display the search results
        startActivity(listPage);
    }
}
