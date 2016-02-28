package com.simplyrishta.popularmoviesp1;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class DetailsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_details);
        Bundle bundle = getIntent().getExtras();

        String title = bundle.getString(getString(R.string.ORIGINAL_TITLE));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);


        String image = bundle.getString(getString(R.string.IMAGE));
        String overview = bundle.getString(getString(R.string.OVERVIEW));
        Number rating = (Number)bundle.get(getString(R.string.RATING));
        String releaseDate = bundle.getString(getString(R.string.RELEASE_DATE));

        final GridItem item = (GridItem)getIntent().getSerializableExtra(getString(R.string.grid_item));


        Log.d(this.getClass().getSimpleName(), "Information: " + title.concat("\ndate: " + releaseDate + "\n").concat("" + rating));

        //Set image url
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        Picasso.with(this).load(image).into(imageView);

        TextView  desc = (TextView) findViewById(R.id.descriptionTextView);
        desc.setText(overview);

        TextView  ratingTv = (TextView) findViewById(R.id.ratingTextView);
        ratingTv.setText(""+rating+"/10");

        SimpleDateFormat dateFormatter = new SimpleDateFormat ("yyyy-mm-dd" );
        java.util.Date dateObject;

        try {
            dateObject = dateFormatter.parse(releaseDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateObject);
            TextView  dateTv = (TextView) findViewById(R.id.yearTextView);
            dateTv.setText(""+calendar.get(Calendar.YEAR));
        }catch(Exception e) {
            Log.d( "Exception parsing: ",e.getStackTrace().toString());
        }

        final Button button = (Button) findViewById(R.id.markAsFavorite);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MovieStorageHelper helper = new MovieStorageHelper(DetailsActivity.this);
                boolean isInserted = helper.saveGridItemToDB(item);
                if(!isInserted) {
                    List<GridItem> items = helper.getAllItems();
                    Log.d( "Error: ","couldn't save the item, but here are all the saved ones so far!: ");
                    for(GridItem item:items) {
                        Log.d("MovieName: "+item.getTitle(),"--- Overview: "+item.getOverview());
                    }
                }
                else {
                    Button button = (Button) findViewById(R.id.markAsFavorite);
                    button.setVisibility(View.GONE);
                }

            }
        });

    }

}
