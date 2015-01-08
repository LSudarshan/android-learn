package com.example.vagrant.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.vagrant.myapplication.model.FictionEntry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
    private static final String[] RoyalRoadIds = new String[]{
            "544", //Master of all Jack of non,
            "403", // Dont fear the reaper
            "383", //seeker of myths
            "804", //RE: Lovely
            "165", //chronicles of blade
            "638", //life in new world
            "162", //tec mage
            "469", //beyond the other world
            "841", //master of dungeons
            "322", //my second life
            "432", //tale of adventure
            "873", //No longer a game
//            "908", //beast wars
            "283", //dragons road
            "756"  //ages online
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void refresh(View view){
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            refresh();
        } else {
            handleNetworkFailure();
        }
    }

    private void refresh() {
        new DownloadWebpageTask(new Action<List<FictionEntry>>(){
            @Override
            public void call(List<FictionEntry> items) {
                initializeList(items);
            }
        }).execute();
    }

    private void initializeList(List<FictionEntry> items) {
        ListView listView = (ListView) findViewById(R.id.listView);
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(arrayAdapter);
    }

    private void handleNetworkFailure() {
        initializeList(Collections.singletonList(new FictionEntry("Network failure!", "")));
    }

    private class DownloadWebpageTask extends AsyncTask<String, String, List<FictionEntry>> {
        private Action<List<FictionEntry>> refreshItems;
        private ProgressDialog progressDialog;

        public DownloadWebpageTask(Action<List<FictionEntry>> items){
            this.refreshItems = items;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(MainActivity.this, "Progress dialog", "Checking royal road....");
        }

        @Override
        protected List<FictionEntry> doInBackground(String... params) {
            List<FictionEntry> items = new ArrayList<FictionEntry>();
            for (int i = 0; i < RoyalRoadIds.length; i++) {
                try {
                    Log.v("Royal road", "Trying to download fiction with id: " + RoyalRoadIds[i]);
                    String url = "http://www.royalroadl.com/fiction/" + RoyalRoadIds[i];
                    Document document = Jsoup.connect(url).get();
                    String title = document.select(".fiction-title").text();
                    publishProgress(title);
                    String date = document.select(".chapter .date").last().text();
                    FictionEntry item = new FictionEntry(title, date);
                    Log.v("Royal road", item.toString());
                    items.add(item);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            Collections.sort(items, new Comparator<FictionEntry>() {
                @Override
                public int compare(FictionEntry lhs, FictionEntry rhs) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yy");
                    try {
                        return simpleDateFormat.parse(rhs.getUpdateDate()).compareTo(simpleDateFormat.parse(lhs.getUpdateDate()));
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            return items;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            progressDialog.setMessage("Now downloading: " + values[0]);
        }

        @Override
        protected void onPostExecute(List<FictionEntry> items) {
            super.onPostExecute(items);
            progressDialog.dismiss();
            refreshItems.call(items);
        }
    }

    private interface Action<T>{
        public void call(T t);
    }
}
