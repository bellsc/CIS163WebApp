package edu.gvsu.cis.bellsc.HW4;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class HW4WebAppActivity extends Activity {
	private ListView lview;
	private ArrayAdapter<String> adapter;
	private ArrayList<String> albumInfo = new ArrayList<String>();
	private ArrayList<String> albumName = new ArrayList<String>();
	private String correctURL, partialURL, input;
	private View footer;

	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		// Set adapter and listview with header/footer
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, albumInfo);
		lview = (ListView) findViewById(R.id.listview1);
		View header = getLayoutInflater().inflate(R.layout.header, null);
		lview.addHeaderView(header);
		footer = getLayoutInflater().inflate(R.layout.footer, null);
		
		
		// Search button
		Button confirm = (Button) findViewById(R.id.confirm);
		View.OnClickListener cHandler = new View.OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				for (int i = albumInfo.size() - 1; i >= 0; i--)
					albumInfo.remove(i);
				adapter.notifyDataSetChanged();

				EditText artist = (EditText) findViewById(R.id.artist_name);
				input = artist.getText().toString();
				String editString = "";

				// Changes spaces in input into underscores
				for (int k = 0; k < input.length(); k++) {
					if (input.substring(k, k + 1).equals(" "))
						editString += "_";
					else
						editString += input.substring(k, k + 1);
				}

				MyTask doIt = new MyTask();
				// For showing albums and years
				correctURL = "http://lyrics.wikia.com/api.php?artist="
						+ editString + "&fmt=json";
				// For showing songs and lyrics in next activity
				partialURL = "http://lyrics.wikia.com/api.php?artist="
						+ editString;

				doIt.execute(correctURL);
			}

		};
		confirm.setOnClickListener(cHandler);

		
		lview.setAdapter(adapter);
		lview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				
				// Starts next activity if the artist is found
				if (albumInfo.contains("No albums found by " + input) == false) {
					Log.d("Switch Activities", "" + position);
					Intent next = new Intent(HW4WebAppActivity.this,
							MoreInfo.class);
					next.putExtra("my_albumName", albumInfo.get(position - 1));
					next.putExtra("my_artist", correctURL);
					next.putExtra("my_partialURL", partialURL);

					startActivity(next);

					adapter.notifyDataSetChanged();
				}
			}
		});

		super.onCreate(savedInstanceState);
	}

	public class MyTask extends AsyncTask<String, Void, Void> {
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

				// Get albums and years from JSON and put into the main array
				JSONObject obj = new JSONObject(total);
				JSONArray blds = obj.getJSONArray("albums");
				for (int k = 0; k < blds.length(); k++) {
					JSONObject one = blds.getJSONObject(k);
					String album = one.getString("album");
					Log.d("check", " " + album);
					albumName.add(album);
					String year = one.getString("year");
					if (year.equals("null"))
						year = "other";
					albumInfo.add("Album: " + album + " - Year: " + year);
					
				}

			} catch (MalformedURLException oops) {
				Log.e("Main", "Are you sure the URL is correct?" + oops);
			} catch (IOException oops_again) {
				Log.e("Main", "Can't access the remote resource: " + oops_again);
			} catch (JSONException somethingWrong) {
				Log.e("Main", "Something Wrong with JSON: " + somethingWrong);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			Log.d("Execute", "  " + albumInfo.size());
			// If the artist was not found
			if (albumInfo.size() == 0) {
				albumInfo.add("No albums found by " + input);
				adapter.notifyDataSetChanged();
			} else
			{
			lview.addFooterView(footer);
			
			Button albumSort = (Button) findViewById(R.id.albumSortButton);
			View.OnClickListener aHandler = new View.OnClickListener() {
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
					// Sorts list by album alphabetically
					if(albumInfo.get(0).substring(0,5).equals("Album")){
						Collections.sort(albumInfo);
						adapter.notifyDataSetChanged();
					}  
					
					// Sorts list if "Year:" comes first in list items
					else if (albumInfo.get(0).substring(0,9).equals("No albums") == false){
					ArrayList<String> newAlbumInfo = new ArrayList<String>();
					for(String s : albumInfo){	
						String newAlbumString = "", newYearString = "", newString = "";						
						int albumBeginLoc = 0, albumEndLoc = 0, yearBeginLoc = 0, yearEndLoc = 0;
						for(int i = 0; i < s.length() - 9; i++){
							if(s.substring(i, i+10).equals(" - Album: ")){
								albumBeginLoc = i+10;
								yearEndLoc = i;
							}
							if(s.substring(i, i+6).equals("Year: ")){
								albumEndLoc = i;
								yearBeginLoc = i+6;
							}
							
						}
						Log.d("check beginLoc", " " + albumBeginLoc);
						Log.d("check endLoc", " " + albumEndLoc);
						Log.d("check YbeginLoc", " " + yearBeginLoc);
						Log.d("check YendLoc", " " + yearEndLoc);
						if(albumEndLoc > albumBeginLoc)
							newAlbumString = s.substring(albumBeginLoc, albumEndLoc);
						else if(albumEndLoc < albumBeginLoc)
							newAlbumString = s.substring(albumBeginLoc);
						
						Log.d("check newAlbumString", " " + newAlbumString);
						if(yearEndLoc > yearBeginLoc){
							if(s.substring(0,4).equals("Year"))
							newYearString = s.substring(yearBeginLoc, yearEndLoc);
							else 
								newYearString = s.substring(yearBeginLoc - 1);
							}
						
						else if(yearEndLoc < yearBeginLoc)
							newYearString = s.substring(yearBeginLoc -1);
						
						newString = "Album: " + newAlbumString + " - Year: " + newYearString;
						Log.d("check newYearString", " " + newYearString);
						Log.d("check newString", " " + newString);
						newAlbumInfo.add(newString);
						

					}
					
					for( int b = 0; b < albumInfo.size(); b++)
						albumInfo.set(b, newAlbumInfo.get(b));
					Collections.sort(albumInfo);
					adapter.notifyDataSetChanged();
				}
				}
			};
			albumSort.setOnClickListener(aHandler);  

			Button yearSort = (Button) findViewById(R.id.yearSortButton);
			View.OnClickListener yHandler = new View.OnClickListener() {
				public void onClick(View v) {
					
					// Sorts list by year
					if(albumInfo.get(0).substring(0,4).equals("Year")){
						Collections.sort(albumInfo);
						adapter.notifyDataSetChanged();
					}
					
					// Sorts list if "Album:" comes first in list items
					else if (albumInfo.get(0).substring(0,9).equals("No albums") == false){
					ArrayList<String> newAlbumInfo = new ArrayList<String>();
					for(String s : albumInfo){
						String newAlbumString = "", newYearString = "", newString = "";
						int albumBeginLoc = 0, albumEndLoc = 0, yearBeginLoc = 0, yearEndLoc = 0;
						for(int i = 0; i < s.length() - 10; i++){
							if(s.substring(i, i+7).equals("Album: "))
								albumBeginLoc = i+8;
								yearEndLoc = i;
							if(s.substring(i, i+9).equals(" - Year: ")){
								albumEndLoc = i;
								yearBeginLoc = i+10;
							}
							
						}
						Log.d("check beginLoc", " " + albumBeginLoc);
						Log.d("check endLoc", " " + albumEndLoc);
						if(albumEndLoc > albumBeginLoc)
							newAlbumString = s.substring(albumBeginLoc - 1, albumEndLoc);
						else if(albumEndLoc < albumBeginLoc)
							newAlbumString = s.substring(albumBeginLoc-1);
						
						if(yearEndLoc > yearBeginLoc)
							newYearString = s.substring(yearBeginLoc - 1, yearEndLoc);
						else if(yearEndLoc < yearBeginLoc)
							newYearString = s.substring(yearBeginLoc-1);
						
						newString = "Year: " + newYearString + " - Album: " + newAlbumString;
						
						Log.d("check newString", " " + newString);
						newAlbumInfo.add(newString);
						
					}
					for( int b = 0; b < albumInfo.size(); b++)
						albumInfo.set(b, newAlbumInfo.get(b));
					Collections.sort(albumInfo);
					adapter.notifyDataSetChanged();
				}
				}
			};
			yearSort.setOnClickListener(yHandler);  
			
			adapter.notifyDataSetChanged();

		}
		}
	}
}