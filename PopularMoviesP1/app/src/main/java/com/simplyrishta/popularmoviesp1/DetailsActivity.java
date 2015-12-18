package com.simplyrishta.popularmoviesp1;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_details);
        Bundle bundle = getIntent().getExtras();
        String title = bundle.getString("title");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);


        String image = bundle.getString("image");
        String overview = bundle.getString("overview");
        Number rating = (Number)bundle.get("rating");
        String releaseDate = bundle.getString("relDate");

        Log.d(this.getClass().getSimpleName(), "Information: " + title.concat("\ndate: " + releaseDate  + "\n").concat("" + rating));

        //Set image url
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        Picasso.with(this).load(image).into(imageView);

        TextView  desc = (TextView) findViewById(R.id.descriptionTextView);
        desc.setText(overview);

        TextView  ratingTv = (TextView) findViewById(R.id.ratingTextView);
        ratingTv.setText(""+rating+"/10");

        SimpleDateFormat dateFormatter = new SimpleDateFormat ( "yyyy-mm-dd" );
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



//        }catch (Exception e) {
//            Log.d( "Exception parsing: ",e.getStackTrace().toString());
//        };




    }

}
