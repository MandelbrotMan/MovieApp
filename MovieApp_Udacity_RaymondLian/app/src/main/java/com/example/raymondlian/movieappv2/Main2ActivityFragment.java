package com.example.raymondlian.movieappv2;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class Main2ActivityFragment extends Fragment {
    //Global variables start with Capital letters
    ArrayList<MovieObject> MoviesListed = new ArrayList<MovieObject>();
    static ArrayList<MovieObject> FavoriteMovies = new ArrayList<MovieObject>();

    GridView Gridview;
    ImageAdapter JsonAdapter;
    ImageAdapter LocalAdapter;
    TextView ListTitle;

    static int CurrentList = 0; //0 if its MoviesListed, 1 if FavoriteMovies --used for item selection

    LinearLayout HeaderProgress;
    Bundle formMovieDetailPackage;

    public Main2ActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = (View)inflater.inflate(R.layout.fragment_main2, container, false);

        getActivity().setTitle("Blue Ray Movies");
        HeaderProgress = (LinearLayout) root.findViewById(R.id.ProgressBarLayout);

        Gridview = (GridView) root.findViewById(R.id.gridview);
        ListTitle = (TextView) root.findViewById(R.id.textView);

        JsonAdapter = new ImageAdapter(getActivity(), MoviesListed);
        LocalAdapter = new ImageAdapter(getActivity(), FavoriteMovies);

        Intent intent = getActivity().getIntent();
        formMovieDetailPackage = intent.getExtras();



        if(formMovieDetailPackage != null){
            if(!checkInList(formMovieDetailPackage.getString("movieIdString"), FavoriteMovies)){
                MovieObject newFavorite = new MovieObject(
                        formMovieDetailPackage.getString("title"),
                        formMovieDetailPackage.getString("releaseDate"),
                        formMovieDetailPackage.getString("rating"),
                        formMovieDetailPackage.getString("plot"),
                        formMovieDetailPackage.getString("movieIdString"),
                        formMovieDetailPackage.getString("imageURLString"));
                FavoriteMovies.add(newFavorite);
            }


        }





        //For preserving screen data during screen rotation
        if (savedInstanceState == null || !savedInstanceState.containsKey("movies")) {
            new DownloadImageTask((GridView) root.findViewById(R.id.gridview)).execute("popular");


        } else {
            if(!isNetworkAvailable()){
                ListTitle.setText("No Network Connection");
            }

            MoviesListed = savedInstanceState.getParcelableArrayList("movies");
            FavoriteMovies = savedInstanceState.getParcelableArrayList("favorites");
            Context context = getActivity().getApplicationContext();
            if(CurrentList == 0) {
                JsonAdapter.restore(getActivity(), MoviesListed);
                JsonAdapter.notifyDataSetChanged();
                Gridview.setAdapter(JsonAdapter);

            }else if (CurrentList == 1) {
                LocalAdapter.restore(getActivity(), FavoriteMovies);
                LocalAdapter.notifyDataSetChanged();
                Gridview.setAdapter(LocalAdapter);
            }

            //new ReloadImageTask((GridView)findViewById(R.id.gridview)).execute("");

        }


        Gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                final Intent i = new Intent(getActivity(), MovieDetailActivityFragment.class);

                //Prepare information to be sent to the next activity
                Bundle moviePackage = new Bundle();
                if (CurrentList == 0) {
                    moviePackage.putString("title", MoviesListed.get(position).savedTitle);
                    moviePackage.putString("image", MoviesListed.get(position).savedURL);
                    moviePackage.putString("release_date", MoviesListed.get(position).savedDate);
                    moviePackage.putString("vote_average", MoviesListed.get(position).savedRating);
                    moviePackage.putString("synopsis", MoviesListed.get(position).savedPlot);
                    moviePackage.putString("id", MoviesListed.get(position).savedId);
                    if (checkInList(MoviesListed.get(position).savedId, FavoriteMovies)) {
                        moviePackage.putBoolean("favStatus", true);
                    } else {
                        moviePackage.putBoolean("favStatus", false);
                    }
                } else if (CurrentList == 1) {
                    moviePackage.putString("title", FavoriteMovies.get(position).savedTitle);
                    moviePackage.putString("image", FavoriteMovies.get(position).savedURL);
                    moviePackage.putString("release_date", FavoriteMovies.get(position).savedDate);
                    moviePackage.putString("vote_average", FavoriteMovies.get(position).savedRating);
                    moviePackage.putString("synopsis", FavoriteMovies.get(position).savedPlot);
                    moviePackage.putString("id", FavoriteMovies.get(position).savedId);
                    moviePackage.putBoolean("favStatus", true); // by default


                }

                MovieDetailActivityFragment nextFrag= new MovieDetailActivityFragment();
                Fragment newFragment = new MovieDetailActivityFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

// Replace whatever is in the fragment_container view with this fragment,
// and add the transaction to the back stack
                transaction.replace(R.id.fragmentMain, newFragment);
                transaction.addToBackStack(null);

// Commit the transaction
                transaction.commit();


            }
        });

        setHasOptionsMenu(true);
        return root;

    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu, menuInflater);
        menu.clear();
        menuInflater.inflate(R.menu.menu_main, menu);


    }
    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putParcelableArrayList("movies", MoviesListed);
        bundle.putParcelableArrayList("favorites", FavoriteMovies);
        super.onSaveInstanceState(bundle);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_popularityMenu
                ) {
            Gridview.setAdapter(null);

            new DownloadImageTask((GridView) getActivity().findViewById(R.id.gridview)).execute("popular");
            return true;
        }
        if (id == R.id.action_ratingMenu
                ) {
            Gridview.setAdapter(null);
            new DownloadImageTask((GridView) getActivity().findViewById(R.id.gridview)).execute("top_rated");


            return true;
        }
        if (id == R.id.action_FavoriteMenu){
            CurrentList = 1;
            MoviesListed.clear();
            LocalAdapter.restore(getActivity(), FavoriteMovies);
            Gridview.setAdapter(null);
            LocalAdapter.notifyDataSetChanged();
            Gridview.setAdapter(LocalAdapter);

        }

        return super.onOptionsItemSelected(item);
    }
    private class DownloadImageTask extends AsyncTask<String, Void, ArrayList<MovieObject>> {
        GridView bmImage;
        String listInfo = "";

        public DownloadImageTask(GridView bmImage) {
            this.bmImage = bmImage;
        }

        protected ArrayList<MovieObject> doInBackground(String... urls) {
            //Clear to prevent multiple copies in arrays
            MoviesListed.clear();
            CurrentList = 0;

            if (urls[0] == "popular") {
                listInfo = "Most Popular";
            } else if (urls[0] == "top_rated") {
                listInfo = "Top Rated";
            }


            String popularURL = getJsonURL(urls[0]);
            String[] jsonArray = {};


            if (isNetworkAvailable()) {
                try {
                    if (jsonArray == null) {
                    }
                    jsonArray = getJsonData(popularURL, 100);
                    getBitMapURL(jsonArray);

                } catch (JSONException e) {
                } catch (IOException e) {

                }
            }
            return MoviesListed;
        }

        @Override
        protected void onPreExecute() {
            // SHOW THE SPINNER WHILE LOADING FEEDS
           HeaderProgress.setVisibility(View.VISIBLE);
            //setProgressBarIndeterminateVisibility(true);
        }


        protected void onPostExecute(ArrayList<MovieObject> movies) {

            JsonAdapter.notifyDataSetChanged();
            Gridview.setAdapter(JsonAdapter);
            ListTitle.setText(listInfo);
            Context context = getActivity();
            HeaderProgress.setVisibility(View.GONE);


        }

        public String getJsonURL(String sort) {


            String JsonUrl = "";
            HttpURLConnection urlConnection = null;
            BufferedReader reader;

            InputStream stream;
            URL popularURL;


            Uri base = Uri.parse("https://api.themoviedb.org").buildUpon().
                    appendPath("3").
                    appendPath("movie").
                    appendPath(sort).
                    appendQueryParameter("api_key", "0109ddff503db8186924929b1814320e").
                    appendQueryParameter("language", "en").
                    appendQueryParameter("include_image)langauge", "en, us").build();


            try {
                popularURL = new URL(base.toString());

                urlConnection = (HttpURLConnection) popularURL.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();



                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {

                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                JsonUrl = buffer.toString();


            } catch (IOException e) {
                Log.e("error", String.valueOf(e));
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return JsonUrl;
        }

        protected String[] getJsonData(String s, int top) throws JSONException {
            ArrayList<String> imagePathArray = new ArrayList<>();
            final String get_RESULTS = "results";

            final String get_PATH = "poster_path";
            final String get_AVERAGE = "vote_average";
            final String get_SYNOPSIS = "overview";
            final String get_RELEASE_DATE = "release_date";
            final String get_TITLE = "title";
            final String get_ID = "id";


            JSONObject popularJSON = new JSONObject(s);

            JSONArray movieArray = popularJSON.getJSONArray(get_RESULTS);

            //Extracting details of movies from json
            for (int j = 0; j < movieArray.length(); j++) {
                JSONObject singleMovie = movieArray.getJSONObject(j);
                imagePathArray.add(singleMovie.getString(get_PATH).substring(1));

                MovieObject temp = new MovieObject(singleMovie.getString(get_TITLE),
                        singleMovie.getString(get_RELEASE_DATE),
                        Integer.toString(singleMovie.getInt(get_AVERAGE)),
                        singleMovie.getString(get_SYNOPSIS), Integer.toString(singleMovie.getInt(get_ID)));
                MoviesListed.add(temp);

            }


            String[] convertedArray = new String[imagePathArray.size()];
            convertedArray = imagePathArray.toArray(convertedArray);
            return convertedArray;
        }

        protected Void getBitMapURL(String[] paths) throws IOException {
            HttpURLConnection posterUrlConnection = null;
            InputStream inputStream = null;
            Bitmap bitmap;
            BufferedReader reader;


            for (int i = 0; i < paths.length; ++i) {
                Uri builtUri = Uri.parse("http://image.tmdb.org").buildUpon()
                        .appendPath("t")
                        .appendPath("p")
                        .appendPath("w500")
                        .appendPath(paths[i]).build();
                try {
                    URL bitmapURL = new URL(builtUri.toString());


                    posterUrlConnection = (HttpURLConnection) bitmapURL.openConnection();
                    posterUrlConnection.setRequestMethod("GET");
                    posterUrlConnection.connect();



                    MoviesListed.get(i).savedURL = bitmapURL.toString();

                } catch (IOException e) {
                    return null;
                } finally {
                    if (posterUrlConnection != null) {
                        posterUrlConnection.disconnect();
                    }

                }
            }
            return null;

        }


    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<MovieObject> loadList = new ArrayList<MovieObject>();

        public ImageAdapter(Context c, ArrayList<MovieObject> toDisplay) {
            loadList = toDisplay;
            mContext = c;
        }

        public int getCount() {
            return loadList.size();
        }

        public Object getItem(int position) {

            return null;
        }

        public long getItemId(int position) {

            return 0;
        }
        private void restore(Context rContext, ArrayList<MovieObject> rList){
            mContext = rContext;
            loadList = rList;
        }
        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(300, 425));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(4, 4, 4, 4);
            } else {
                imageView = (ImageView) convertView;
            }


            Picasso.with(mContext).load(loadList.get(position).savedURL).into(imageView);

            return imageView;
        }


        // references to our images

    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    //Holds all the movie contents Information


    private boolean checkInList( String id, ArrayList<MovieObject> objects){
        boolean status = false;
        for(int i = 0; i < objects.size(); ++i){
            if(id.equals(objects.get(i).savedId)){
                Log.v("Favorite Id list:", objects.get(i).savedId);
                status = true;
            }
        }
        return  status;

    }
}
