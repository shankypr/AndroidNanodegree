package com.simplyrishta.popularmoviesp1;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class GridViewActivity extends ActionBarActivity {
    public static final String MOVIEDB_IMAGE_API_URL= "http://image.tmdb.org/t/p/";
    public static final String MOVIE_DB_API_KEY = "fc453236c3fb1ee6a6b64e583e46bc80";

    private static final String LOG_TAG = GridViewActivity.class.getSimpleName();


    private GridView mGridView;
    private ProgressBar mProgressBar;
    private GridViewAdapter mGridAdapter;
    private ArrayList<GridItem> mGridData;
    private AsyncHttpTask asyncHttpTask;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mGridView = (GridView) findViewById(R.id.gridView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        //Initialize with empty data
        mGridData = new ArrayList<>();
        mGridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, mGridData);
        mGridView.setAdapter(mGridAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //Get item at position
                GridItem item = (GridItem) parent.getItemAtPosition(position);

                Intent intent = new Intent(GridViewActivity.this, DetailsActivity.class);
                ImageView imageView = (ImageView) v.findViewById(R.id.grid_item_image);
                int[] screenLocation = new int[2];
                imageView.getLocationOnScreen(screenLocation);

                intent.putExtra("left", screenLocation[0]).
                        putExtra("top", screenLocation[1]).
                        putExtra("width", imageView.getWidth()).
                        putExtra("height", imageView.getHeight()).
                        putExtra("title", item.getTitle()).
                        putExtra("image", item.getImage()).
                        putExtra("overview", item.getOverview()).
                        putExtra("rating", item.getRating()).
                        putExtra("relDate", item.getReleaseDate())
                ;
                startActivity(intent);
            }
        });

        //Start download
        asyncHttpTask = new AsyncHttpTask();
        asyncHttpTask.execute("default");
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.sort_popularity);
        //item.setEnabled(false);
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu=menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.sort_popularity);
        item.setEnabled(false);
        return true;
    }

    @Override
        public boolean onOptionsItemSelected(MenuItem item) {
        MenuItem ratingsItem  = menu.findItem(R.id.sort_ratings);
        MenuItem popItem = menu.findItem(R.id.sort_popularity);

        //Log.d(this.getClass().getSimpleName(),"onOptionsItemsSelectedMenu: "+item.getTitle());
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.sort_popularity) {
            asyncHttpTask = new AsyncHttpTask();
            asyncHttpTask.execute("popularity");
            popItem.setEnabled(false);
            ratingsItem.setEnabled(true);
            return true;
        }
        else if (id == R.id.sort_ratings) {
            asyncHttpTask = new AsyncHttpTask();
            asyncHttpTask.execute("ratings");
            popItem.setEnabled(true);
            ratingsItem.setEnabled(false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //Downloading data asynchronously
    public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {


        @Override
        protected Integer doInBackground(String... params) {
            if(params.length == 0) {
                return null;
            }

            String sortOrder = params[0];
            if(sortOrder.equals("popularity")) {
                sortOrder="popularity.desc";
            }
            else if (sortOrder.equals("ratings")) {
                sortOrder = "vote_average.desc";
            }
            else {
                sortOrder = "popularity.desc";
            }
            Integer result = 0;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            Uri.Builder builder = null;


            try {
                Log.d("null","inn dooBackground");

                builder = Uri.parse("http://api.themoviedb.org/").buildUpon();
                builder.path("3/discover/movie");
                builder.appendQueryParameter("sort_by", sortOrder);
                builder.appendQueryParameter("api_key", MOVIE_DB_API_KEY);
                //     builder.appendQueryParameter("page","100");

                URL url = new URL(builder.toString());
                // Create the request to OpenWeatherMap, and open the connection

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                Log.d(this.getClass().getSimpleName(), "Request URL: " + url.toString());
                urlConnection.connect();
                int statusCode = urlConnection.getResponseCode();


                // 200 represents HTTP OK
                if (statusCode == 200) {
                    String response = streamToString(urlConnection.getInputStream());
                    parseResult(response);
                    result = 1; // Successful
                } else {
                    Log.d(this.getClass().getSimpleName(),"Http Failure Response Code: "+statusCode);

                    result = 0; //"Failed
                }
            } catch (Exception e) {
                Log.d(LOG_TAG, e.getLocalizedMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            // Download complete. Let us update UI
            Log.d("null","inn post execute");

            if (result == 1) {
                mGridAdapter.setGridData(mGridData);
            } else {
                Toast.makeText(GridViewActivity.this, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }
            mProgressBar.setVisibility(View.GONE);
        }
    }

    String streamToString(InputStream stream) throws IOException {
        Log.d("null","inn streamToString");

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }

        // Close stream
        if (null != stream) {
            stream.close();
        }
        return result;
    }

    /**
     * Parsing the feed results and get the list
     * @param result
     */
    private void parseResult(String result) {
        mGridData.clear();
        try {
            Log.d("In parseResult",""+result);

            JSONObject response = new JSONObject(result);
            JSONArray results = response.optJSONArray("results");
            GridItem item;
            for (int i = 0; i < results.length(); i++) {

                try {
                    //Log.d("Results are: ",""+results);
                    JSONObject post = results.optJSONObject(i);
                    // Log.d("Data IS: ",""+post);

                    item = new GridItem();
                    if (post.get("id") !=null) {
                        item.setId((Integer) post.get("id"));
                    }
                    if (post.get("original_title")!=null) {
                        item.setTitle((String)post.get("original_title"));
                    }
                    if (post.get("poster_path")!=null) {
                        if(post.get("poster_path").getClass() != String.class) {
                            Log.d("Expected String: ",""+post.get("poster_path")) ;
                            Log.d(LOG_TAG,""+post.toString());
                        }
                        else {
                            item.setImage(MOVIEDB_IMAGE_API_URL + "w185/" + (String) post.get("poster_path"));
                        }

                    }
                    if (post.get("overview")!=null) {
                        item.setOverview((String) post.get("overview"));
                    }
                    if (post.get("vote_average")!=null) {
                        item.setRating((Number) post.get("vote_average"));
                    }
                    if (post.get("release_date")!=null) {
                        item.setReleaseDate((String) post.get("release_date"));
                    }
                    //Integer id = (Integer) post.get("id");


                /*JSONArray attachments = post.getJSONArray("fartttachments");
                if (null != attachments && attachments.length() > 0) {
                    JSONObject attachment = attachments.getJSONObject(0);
                    if (attachment != null)
                        item.setImage(attachment.getString("url"));
                }*/
                    mGridData.add(item);
                }catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}