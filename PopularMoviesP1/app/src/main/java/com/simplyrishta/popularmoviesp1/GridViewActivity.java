package com.simplyrishta.popularmoviesp1;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GridViewActivity extends ActionBarActivity {
    //public static final String MOVIEDB_IMAGE_API_URL= "http://image.tmdb.org/t/p/";
    //public static final String MOVIE_DB_API_KEY = "fc453236c3fb1ee6a6b64e583e46bc80";

    private static final String LOG_TAG = GridViewActivity.class.getSimpleName();


    private GridView mGridView;
    //private ProgressBar mProgressBar;
    private GridViewAdapter mGridAdapter;
    private ArrayList<GridItem> mGridData;
    private AsyncHttpTask asyncHttpTask;
    private Toolbar mToolBar;
    private Menu menu;

    private ArrayList<GridItem> moviesOffScreen = new ArrayList<>();
    private boolean isMainMoviesVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);

        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        mToolBar.setTitle("Sort by: "+getString(R.string.popularity));
        setActionBar(mToolBar);

        mGridView = (GridView) findViewById(R.id.gridView);

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
                        putExtra(getString(R.string.ORIGINAL_TITLE), item.getTitle()).
                        putExtra(getString(R.string.IMAGE), item.getImage()).
                        putExtra(getString(R.string.OVERVIEW), item.getOverview()).
                        putExtra(getString(R.string.RATING), item.getRating()).
                        putExtra(getString(R.string.RELEASE_DATE), item.getReleaseDate());

                intent.putExtra(getString(R.string.grid_item),item);
                startActivity(intent);
            }
        });

        //Start download
        asyncHttpTask = new AsyncHttpTask();
        asyncHttpTask.execute("default");
        //mProgressBar.setVisibility(View.VISIBLE);
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
            MenuItem favItem = menu.findItem(R.id.sort_favorties);

            //Log.d(this.getClass().getSimpleName(),"onOptionsItemsSelectedMenu: "+item.getTitle());
            int id = item.getItemId();
            //noinspection SimplifiableIfStatement
            if (id == R.id.sort_popularity) {
                if(isMainMoviesVisible == false) {
                    mGridData.clear();
                    mGridData.addAll(moviesOffScreen);
                    mGridAdapter.notifyDataSetChanged();
                    isMainMoviesVisible = true;
                    return true;
                }
                asyncHttpTask = new AsyncHttpTask();
                asyncHttpTask.execute(getString(R.string.popularity));
                popItem.setEnabled(false);
                ratingsItem.setEnabled(true);
                favItem.setEnabled(true);
                mToolBar.setTitle("Sort by: "+getString(R.string.popularity));
                return true;
            }
            else if (id == R.id.sort_ratings) {
                if(isMainMoviesVisible == false) {
                    mGridData.clear();
                    mGridData.addAll(moviesOffScreen);
                    mGridAdapter.notifyDataSetChanged();
                    isMainMoviesVisible = true;
                    return true;
                }
                asyncHttpTask = new AsyncHttpTask();
                asyncHttpTask.execute(getString(R.string.ratings));
                popItem.setEnabled(true);
                favItem.setEnabled(true);
                ratingsItem.setEnabled(false);
                mToolBar.setTitle("Sort by: " + getString(R.string.ratings));
                return true;
            }
            else if (id== R.id.sort_favorties) {

                MovieStorageHelper helper = new MovieStorageHelper(this);
                List<GridItem> items = helper.getAllItems();
                ArrayList<GridItem> list = new ArrayList(items);

                Log.d("FavItem COunt: ", "" + list.size());
                popItem.setEnabled(true);
                ratingsItem.setEnabled(true);
                favItem.setEnabled(false);

                moviesOffScreen.clear();
                moviesOffScreen.addAll(mGridData);
                mGridData.clear();
                mGridData.addAll(list);
                mGridAdapter.notifyDataSetChanged();
                isMainMoviesVisible=false;
                mToolBar.setTitle("Showing " + getString(R.string.favorites));
            }
            return true;
    }


    //Downloading data asynchronously
    public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {


        @Override
        protected Integer doInBackground(String... params) {
            if(params.length == 0) {
                return null;
            }

            String sortOrder = params[0];
            if(sortOrder.equals(getString(R.string.popularity))) {
                sortOrder=getString(R.string.POPULARITY_DESC);
            }
            else if (sortOrder.equals(getString(R.string.ratings))) {
                sortOrder = getString(R.string.VOTE_AVG_DESC);
            }
            else {
                sortOrder = getString(R.string.POPULARITY_DESC);
            }
            Integer result = 0;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            Uri.Builder builder = null;


            try {
                Log.d("null","inn dooBackground");

                builder = Uri.parse(getString(R.string.MOVIE_DB_URL)).buildUpon();
                builder.path(getString(R.string.MOVIEDB_DISCOVER_ENDPOINT));
                builder.appendQueryParameter(getString(R.string.SORT_BY), sortOrder);
                builder.appendQueryParameter(getString(R.string.API_KEY), getString(R.string.MOVIEDB_API_KEY));

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
            Log.d("null", "inn post execute");

            if (result == 1) {
                mGridAdapter.setGridData(mGridData);
            } else {
                Toast.makeText(GridViewActivity.this, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }
            //mProgressBar.setVisibility(View.GONE);
        }
    }

    String streamToString(InputStream stream) throws IOException {
        //Log.d("null","inn streamToString");

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
            //Log.d("In parseResult",""+result);

            JSONObject response = new JSONObject(result);
            JSONArray results = response.optJSONArray(getString(R.string.RESULTS));
            GridItem item;
            for (int i = 0; i < results.length(); i++) {

                try {
                    //Log.d("Results are: ",""+results);
                    JSONObject post = results.optJSONObject(i);
                    // Log.d("Data IS: ",""+post);

                    item = new GridItem();
                    if (post.get(getString(R.string.MOVIE_ID)) !=null) {
                        item.setId((Integer) post.get(getString(R.string.MOVIE_ID)));
                    }
                    if (post.get(getString(R.string.ORIGINAL_TITLE))!=null) {
                        item.setTitle((String)post.get(getString(R.string.ORIGINAL_TITLE)));
                    }
                    if (post.get(getString(R.string.POSTER_PATH))!=null) {
                        if(post.get(getString(R.string.POSTER_PATH)).getClass() != String.class) {
                            Log.d("Expected String: ",""+post.get(getString(R.string.POSTER_PATH))) ;
                            Log.d(LOG_TAG,""+post.toString());
                        }
                        else {
                            item.setImage(getString(R.string.MOVIEDB_IMAGE_API_URL) +
                                    getString(R.string.THUMBNAIL_185)+ (String) post.get(getString(R.string.POSTER_PATH)));
                        }

                    }
                    if (post.get(getString(R.string.OVERVIEW))!=null) {
                        item.setOverview((String) post.get(getString(R.string.OVERVIEW)));
                    }
                    if (post.get(getString(R.string.vote_average)) !=null) {
                        item.setRating((Number) post.get(getString(R.string.vote_average)));
                    }
                    if (post.get(getString(R.string.RELEASE_DATE))!=null) {
                        item.setReleaseDate((String) post.get(getString(R.string.RELEASE_DATE)));
                    }
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