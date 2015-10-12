package com.example.raymondlian.movieappv2;

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

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailActivityFragment extends Fragment {
    String ImageURLString;
    String MovieIdString;
    ImageView PosterView;
    ArrayAdapter<String> adapter;
    ListView listView;

    Context mContext = getContext();
    int size;

    ArrayList<String> trailerTitles = new ArrayList<>();

    public MovieDetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflater1 = inflater.inflate(R.layout.fragment_movie_detail, container);

        super.onCreate(savedInstanceState);
        Intent intent = getActivity().getIntent();
        Bundle recievedPackage = intent.getExtras();
        ImageURLString = recievedPackage.getString("image");
        MovieIdString = recievedPackage.getString("id");




        if(isNetworkAvailable()) {
            new imageTask().execute("");

            TextView titleView = (TextView) inflater1.findViewById(R.id.movieTitleText);
            TextView dateView = (TextView) inflater1.findViewById(R.id.releaseDateText);
            TextView ratingView = (TextView) inflater1.findViewById(R.id.voteAverageText);
            TextView synopsisView = (TextView) inflater1.findViewById(R.id.synopsisText);
            PosterView = (ImageView) inflater1.findViewById(R.id.posterImageView);


            titleView.setText(recievedPackage.getString("title"));
            dateView.setText(recievedPackage.getString("release_date"));
            ratingView.setText(recievedPackage.getString("vote_average"));
            synopsisView.setText(recievedPackage.getString("synopsis"));
        } else {
            TextView titleView = (TextView) inflater1.findViewById(R.id.movieTitleText);
            titleView.setText("Connection lost");
        }


        adapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.trailer_item,
                R.id.list_item_trailer_textview,
                trailerTitles
        );

        listView = (ListView) inflater1.findViewById(R.id.trailerListView);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

            }
        });



       return  inflater1;
    }
    private class imageTask extends AsyncTask<String, Void, Void> {
        HttpURLConnection posterUrlConnection = null;


        protected Void doInBackground(String... param){
            Picasso.with(mContext).load(ImageURLString).into(PosterView);
            String movieTrailersUrl = getTrailerJsonURL();

            try {
                getTrailersJSON(movieTrailersUrl);
                Log.v("Size:", Integer.toString(trailerTitles.size()));
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;

        }
        protected void onPostExecute(){
            adapter.clear();
            String trailerName = "";
            for(int i = 0; i < trailerTitles.size(); ++i) {
                trailerName = trailerTitles.get(i);
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
                String stringTemp = temp.getString("name");
                trailerTitles.add(stringTemp);

            }



        }

    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }




}
