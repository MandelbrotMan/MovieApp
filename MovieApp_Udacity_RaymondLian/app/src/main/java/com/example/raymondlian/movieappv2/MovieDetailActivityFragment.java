package com.example.raymondlian.movieappv2;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
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
import android.widget.Button;
import android.widget.Toast;
import android.view.KeyEvent;
/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailActivityFragment extends Fragment {
   OnFavoriteSelectedListener mCallback; // used to communicate between fragment and main activity




    //To be sent back if selected as favorite
    String ImageURLString; //For posterpath
    String MovieIdString;  //For pulling additional data of selected movie
    String Title;
    String Rating;
    String ReleaseDate;
    String Plot;
    Toast MToast;


    //Used to send back MovieObject if Selected as favorite
    Bundle MoviePackage = null;

    MovieObject Movie; //For on rotation saveInstance



    ImageView PosterView;
    ArrayAdapter<String> adapter;
    ListView listView;
    Button FavoriteButton;
    Button ReviewButton;
    Context mContext = getActivity();
    boolean FavStatus;
    Intent I;
    ArrayList<TrailerObject> trailerObjects = new ArrayList<>();


    public MovieDetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflater1 = inflater.inflate(R.layout.fragment_movie_detail, container);
        super.onCreate(savedInstanceState);

        final Intent intent = getActivity().getIntent();
        Bundle recievedPackage = intent.getExtras();     //UI Components
        ReviewButton = (Button) inflater1.findViewById(R.id.reviewButton);
        FavoriteButton = (Button) inflater1.findViewById(R.id.favoriteButton);
        TextView titleView = (TextView) inflater1.findViewById(R.id.movieTitleText);
        TextView dateView = (TextView) inflater1.findViewById(R.id.releaseDateText);
        TextView ratingView = (TextView) inflater1.findViewById(R.id.voteAverageText);
        TextView synopsisView = (TextView) inflater1.findViewById(R.id.synopsisText);
        PosterView = (ImageView) inflater1.findViewById(R.id.posterImageView);
        if(savedInstanceState == null) {
            if (isNetworkAvailable() && recievedPackage != null) {
                ImageURLString = recievedPackage.getString("image");
                MovieIdString = recievedPackage.getString("id");
                Plot = recievedPackage.getString("synopsis");
                Title = recievedPackage.getString("title");
                ReleaseDate = recievedPackage.getString("release_date");
                Rating = recievedPackage.getString("vote_average");
                FavStatus = recievedPackage.getBoolean("favStatus");

                Movie = new MovieObject(Title, ReleaseDate, Rating, Plot, MovieIdString, ImageURLString);

                new imageTask().execute("");

            } else {

                titleView.setText("Connection lost");
            }
        } else {
            Movie = savedInstanceState.getParcelable("movie");
            FavStatus = savedInstanceState.getBoolean("status");
            Title = Movie.savedTitle;
            ReleaseDate = Movie.savedDate;
            Rating = Movie.savedRating;
            MovieIdString = Movie.savedId;
            ImageURLString = Movie.savedURL;
            Plot = Movie.savedPlot;
            new imageTask().execute("");
        }
        //mCallback must be initialize with some value to prevent a void error
        if (FavStatus == true) {
            FavoriteButton.setBackgroundResource(R.drawable.stargold);
            mCallback.onFavoriteSelected(Title, ReleaseDate, Rating, Plot, MovieIdString, ImageURLString);

        } else {
          mCallback.onFavoriteSelected("","","","","","");

            FavoriteButton.setBackgroundResource(R.drawable.starblack);
        }
        titleView.setText(Title);
        dateView.setText(ReleaseDate);
        ratingView.setText(Rating);
        synopsisView.setText(Plot);

     ;

        TrailerAdapter adapter = new TrailerAdapter(this, trailerObjects);
        Log.v("Size of trailers list: ", Integer.toString(trailerObjects.size()));
        listView = (ListView) inflater1.findViewById(R.id.trailerListView);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(trailerObjects.get(position).trailer_url));
                startActivity(intent);

            }
        });


        FavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (FavStatus == false) {

                    CharSequence text = "Added!";
                    int duration = Toast.LENGTH_SHORT;
                    MToast = Toast.makeText(getActivity(), text, duration);
                    MToast.show();
                    FavStatus = true;
                    MoviePackage = new Bundle();
                    MovieObject newFavorite = new MovieObject(Title, ReleaseDate, Rating, Plot, MovieIdString, ImageURLString);
                    FavoriteButton.setBackgroundResource(R.drawable.stargold);
                    MoviePackage.putString("title", Title);
                    MoviePackage.putString("releaseDate", ReleaseDate);
                    MoviePackage.putString("rating", Rating);
                    MoviePackage.putString("plot", Plot);
                    MoviePackage.putString("movieIdString", MovieIdString);
                    MoviePackage.putString("imageURLString", ImageURLString);
                    mCallback.onFavoriteSelected(Title, ReleaseDate, Rating, Plot, MovieIdString, ImageURLString);

                }



            }
        });

        ReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Intent intentReviewPage = new Intent(getActivity(), re.class);
               // intentReviewPage.putExtra("id", MovieIdString);


               // startActivity(intentReviewPage);
            }
        });

        getActivity().setResult(Activity.RESULT_OK, I);


       return  inflater1;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        I = new Intent(getActivity(), Main2Activity.class);
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putParcelable("movie", Movie);
        bundle.putBoolean("status", FavStatus);
        super.onSaveInstanceState(bundle);
    }








    // Container Activity must implement this interface
    public interface OnFavoriteSelectedListener {
        public void onFavoriteSelected(String title, String date, String rating, String plot, String id, String url);
    }

    @Override
    public void onAttach(Context c) {
        super.onAttach(c);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnFavoriteSelectedListener) c;
        } catch (ClassCastException e) {
            throw new ClassCastException(c
                    + " must implement OnHeadlineSelectedListener");
        }
    }


    private class imageTask extends AsyncTask<String, Void, Void> {
        HttpURLConnection posterUrlConnection = null;


        protected Void doInBackground(String... param){
            Picasso.with(mContext).load(ImageURLString).into(PosterView);
            String movieTrailersUrl = getTrailerJsonURL();

            try {
                getTrailersJSON(movieTrailersUrl);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;

        }
        protected void onPostExecute(){
            adapter.clear();
            String trailerName = "";
            for(int i = 0; i < trailerObjects.size(); ++i) {
                trailerName = trailerObjects.get(i).trailer_title;
                adapter.add(trailerName);
            }
            adapter.notifyDataSetChanged();



        }

        private String getTrailerJsonURL() {
            String JsonUrl = "";
            HttpURLConnection urlConnection = null;
            BufferedReader reader;

            InputStream stream;
            URL popularURL;


            Uri base = Uri.parse("https://api.themoviedb.org").buildUpon().
                    appendPath("3").
                    appendPath("movie").
                    appendPath(MovieIdString).
                    appendPath("videos").
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
        private void getTrailersJSON (String urlString)  throws JSONException {

            JSONObject trailersObject = new JSONObject(urlString);
            JSONArray trailerArray = trailersObject.getJSONArray("results");

            for(int i = 0; i < trailerArray.length(); ++i){
                JSONObject temp = trailerArray.getJSONObject(i);
                String trailerLink = null;

                Uri base = Uri.parse("https://youtube.com").buildUpon().
                        appendPath("watch").
                        appendQueryParameter("v", temp.getString("key")).build();

                HttpURLConnection urlConnection = null;

                try {
                  URL  trailerURL = new URL(base.toString());

                urlConnection = (HttpURLConnection) trailerURL.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                } catch (IOException e) {
                    Log.e("error", String.valueOf(e));
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                        trailerLink = base.toString();
                    }
                }

                TrailerObject tempTrailer = new TrailerObject(temp.getString("name"),trailerLink);

                trailerObjects.add(tempTrailer);




            }



        }

    }
    public class TrailerAdapter extends ArrayAdapter<TrailerObject> {
        public TrailerAdapter(MovieDetailActivityFragment context, ArrayList<TrailerObject> trailer) {
            super(getActivity(), 0, trailer);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            TrailerObject item = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.trailer_item, parent, false);
            }
            // Lookup view for data population
            TextView Name = (TextView) convertView.findViewById(R.id.list_item_trailer_textview);
            // Populate the data into the template view using the data object
            Name.setText((CharSequence) item.trailer_title);
            // Return the completed view to render on screen
            return convertView;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }




}
