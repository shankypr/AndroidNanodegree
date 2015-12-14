package com.simplyrishta.popularmoviesp1;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_details);
        Bundle bundle = getIntent().getExtras();
        String title = bundle.getString("title");

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        toolbar.setTitle(title);
//        setSupportActionBar(toolbar);

        String image = bundle.getString("image");
        String overview = bundle.getString("overview");
        Number rating = (Number)bundle.get("rating");
        String releaseDate = bundle.getString("release_date");

        //Log.d(this.getClass().getSimpleName(), "Information: " + title.concat("\n"+overview+"\n").concat("" + rating));

        //Set image url
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        Picasso.with(this).load(image).into(imageView);

        TextView  desc = (TextView) findViewById(R.id.descriptionTextView);
        desc.setText(overview);

        TextView  ratingTv = (TextView) findViewById(R.id.ratingTextView);
        ratingTv.setText(""+rating);

        TextView  titleTv = (TextView) findViewById(R.id.titleTextView);
        titleTv.setText(title);

        TextView  dateTv = (TextView) findViewById(R.id.yearTextView);
        titleTv.setText(releaseDate);
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

}
