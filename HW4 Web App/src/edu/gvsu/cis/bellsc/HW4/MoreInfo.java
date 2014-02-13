package edu.gvsu.cis.bellsc.HW4;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MoreInfo extends Activity {

	private ListView lview;
	private ArrayAdapter<String> adapter;
	private ArrayList<String> songsArray = new ArrayList<String>();
	private ArrayList<String> lyricsArray = new ArrayList<String>();
	private ArrayList<String> masterList = new ArrayList<String>();
	private String tempSong, tempLyrics;
	private String sentAlbumName = "";
	private String artistURL = "";
	private String lyricsURL = "";
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.info);

		Bundle params = getIntent().getExtras();

		sentAlbumName = params.getString("my_albumName");
		String fixedAlbumName = "";
		if(sentAlbumName.substring(0, 7).equals("Album: "))
		for(int i = 0; i < sentAlbumName.length() - 8; i++){
			if(sentAlbumName.substring(i, i+8).equals(" - Year:")){
				fixedAlbumName = sentAlbumName.substring(7, i);
				sentAlbumName = fixedAlbumName;
				Log.d("check song name", " " + sentAlbumName);
			}
		}
		else if (sentAlbumName.substring(0, 6).equals("Year: "))
			for(int i = 0; i < sentAlbumName.length() - 8; i++){
				if(sentAlbumName.substring(i, i+9).equals(" - Album:")){
					fixedAlbumName = sentAlbumName.substring(i+10);
					sentAlbumName = fixedAlbumName;
					Log.d("check song name", " " + sentAlbumName);
				}
			}
		
		artistURL = params.getString("my_artist");
		lyricsURL = params.getString("my_partialURL");
		// TextView title = (TextView) findViewById(R.id.songList);
		// title.setText("Classes taught by : " + instructor);

		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, masterList);
		lview = (ListView) findViewById(R.id.songList);
		lview.setAdapter(adapter);

		ClassTask doIt = new ClassTask();

		doIt.execute(artistURL);


		adapter.notifyDataSetChanged();
	}

	public class ClassTask extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... params) {

			String total = "";
			try {
				URL myurl = new URL(params[0]);
				InputStream mystream = myurl.openStream();
				Scanner myscan = new Scanner(mystream);
				while (myscan.hasNextLine()) {
					String aLine = myscan.nextLine();
					Log.d("Main", aLine);
					total += aLine;
				}

				JSONObject obj = new JSONObject(total);
				JSONArray blds = obj.getJSONArray("albums");

				for (int k = 0; k < blds.length(); k++) {
					JSONObject one = blds.getJSONObject(k);
					String albumName = one.getString("album");

					if (albumName.equals(sentAlbumName)) {
						JSONArray songList = one.getJSONArray("songs");

						for (int m = 0; m < songList.length(); m++) {
							String songName = songList.getString(m);
							Log.d("check song name", " " + songName);
							songsArray.add(songName);
							
							String editSong = "";

							// Changes spaces into underscores
							for (int x = 0; x < songName.length(); x++) {
								if (songName.substring(x, x + 1).equals(" "))
									editSong += "_";
								else
									editSong += songName.substring(x, x + 1);
							}
							
							// Start next task
							LyricsTask getLyrics = new LyricsTask();
							Log.d("check URL", " " + lyricsURL + "&song="
									+ editSong + "&fmt=json");
							getLyrics.execute(lyricsURL + "&song=" + editSong
									+ "&fmt=json");
						}

					}

				}

			} catch (MalformedURLException oops) {
				Log.e("Second", "Are you sure the URL is correct?" + oops);
			} catch (IOException oops_again) {
				Log.e("Second", "Can't access the remote resource: "
						+ oops_again);
			} catch (JSONException somethingWrong) {
				Log.e("Second", "Something Wrong with JSON: " + somethingWrong);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			
			adapter.notifyDataSetChanged();
		}

		public class LyricsTask extends AsyncTask<String, Void, Void> {
			@Override
			protected Void doInBackground(String... params) {

				String total = "";
				try {
					URL myurl = new URL(params[0]);
					InputStream mystream = myurl.openStream();
					Scanner myscan = new Scanner(mystream);
					while (myscan.hasNextLine()) {
						String aLine = myscan.nextLine();
						Log.d("Main", aLine);
						if (aLine.equals("song = {") == false)
							total += aLine;

					}

					// Get Song and lyrics from JSON
					total = "{" + total + "}";
					Log.d("JSON object", total);
					JSONObject obj = new JSONObject(total);
					String songTitle = obj.getString("song");
					String lyrics = obj.getString("lyrics");
					Log.d("check", " " + lyrics);
					lyricsArray.add(lyrics);
					tempLyrics = lyrics;
					tempSong = songTitle;

				} catch (MalformedURLException oops) {
					Log.e("Third", "Are you sure the URL is correct?" + oops);
				} catch (IOException oops_again) {
					Log.e("Third", "Can't access the remote resource: "
							+ oops_again);
				} catch (JSONException somethingWrong) {
					Log.e("Third", "Something Wrong with JSON: "
							+ somethingWrong);
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {

				Log.d("Lyrics Done", " done ");
				String temp = "";
				temp = "Song: " + tempSong + "\n" + "Lyrics: \n" + tempLyrics + "\n";
				masterList.add(temp);
				adapter.notifyDataSetChanged();
			}
		}
	}
	// @Override
	// protected void onDestroy() {
	// TODO Auto-generated method stub
	// super.onDestroy();
	// Log.d(TAG, "+++++ Done with this Activity +++++");
	// }
}
