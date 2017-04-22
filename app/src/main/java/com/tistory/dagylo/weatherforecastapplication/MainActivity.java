package com.tistory.dagylo.weatherforecastapplication;

import android.app.SearchManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;


import com.tistory.dagylo.weatherforecastapplication.adapter.DailyForecastPageAdapter;
import com.tistory.dagylo.weatherforecastapplication.model.Weather;
import com.tistory.dagylo.weatherforecastapplication.model.WeatherForecast;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{//

    /* Forecast Weather App */
    private TextView cityText;
    private TextView condDescr;
    private TextView temp;
    private TextView press;
    private TextView windSpeed;
    private TextView windDeg;
    private TextView unitTemp;
    private TextView hum;
    private ImageView imgView;
    private ListView listView; // 도시 검색 리스트
    private ArrayAdapter<String> adapter; // 도시 리스트 어댑터
    private List<String> possibleCities = new ArrayList<>();// 도시 리스트



    private static String forecastDaysNum = "7"; // 7일로 수정.
    private ViewPager pager;
    /* end of the F.W.A */

    private final String lang = "en";
    //private final String TAG = "jyp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Action Bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* Forecast Weather App */
        cityText = (TextView) findViewById(R.id.cityText);
        temp = (TextView) findViewById(R.id.temp); // today's temperature
        unitTemp = (TextView) findViewById(R.id.unittemp); // celcius
        unitTemp.setText("°C");
        condDescr = (TextView) findViewById(R.id.skydesc);

        pager = (ViewPager) findViewById(R.id.pager);
        imgView = (ImageView) findViewById(R.id.condIcon);
        /* end of the F.W.A */


        initPossibleCities();

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, possibleCities);//

        //listView = (ListView) findViewById(R.id.list_country);//
        //listView.setAdapter(adapter);//
    }

    private void initPossibleCities() {
        // todo 1 여기다가 도시랑 국가 다 써주기.
        possibleCities.add("Seoul, KR");
        possibleCities.add("Japan, JP"); // 예시
        possibleCities.add("Hokkaido, JP");
        possibleCities.add("Hashimoto, JP");
        possibleCities.add("Tsukuba-kenkyūgakuen-toshi, JP");
        possibleCities.add("Higashi-asahimachi, JP");
        possibleCities.add("Hanabatachō, JP");
        possibleCities.add("Senzaki, JP");

    }
    // 가능한 city 검색
    private List<String> searchInPossibleCities(String text){
        List<String> results = new ArrayList<>();

        for (String city:
                possibleCities) {
            if (city.contains(text)) {
                //Log.d(TAG, "searchInPossibleCities: "+city);
                results.add(city);
            }
        }

        return results;
    }

    // 뿌려주기
    private void searchWeatherNshow(String city) {
        JSONWeatherTask task = new JSONWeatherTask();
        task.execute(new String[]{city,lang});

        JSONForecastWeatherTask task1 = new JSONForecastWeatherTask(); // Forecast in Fragments
        task1.execute(new String[]{city,lang, forecastDaysNum});
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    /* Forecast Weather App */
    private class JSONWeatherTask extends AsyncTask<String, Void, Weather> {

        @Override
        protected Weather doInBackground(String... params) {
            Weather weather = new Weather();
            String data = ((new WeatherHttpClient()).getWeatherData(params[0], params[1]));

            try {
                weather = JSONWeatherParser.getWeather(data);
                System.out.println("Weather [" + weather + "]");
                // Let's retrieve the icon
                weather.iconData = ((new WeatherHttpClient()).getImage(weather.currentCondition.getIcon()));

            }catch (JSONException e) {
                e.printStackTrace();
            }

            return weather;

        }

        // 텍스트, 이미지 뿌려주기
        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);

            if (weather.iconData != null && weather.iconData.length > 0) {
                Bitmap img = BitmapFactory.decodeByteArray(weather.iconData, 0, weather.iconData.length);
                imgView.setImageBitmap(img);
            }

            cityText.setText(weather.location.getCity() + "," + weather.location.getCountry()); // Seoul, KR
            temp.setText("" + Math.round((weather.temperature.getTemp() - 275.15))); // ex. 16c(celcius)
            condDescr.setText(weather.currentCondition.getCondition() + "(" + weather.currentCondition.getDescr() + ")"); // ex. Clear(clear sky)

        }
    }

    private class JSONForecastWeatherTask extends AsyncTask<String, Void, WeatherForecast> {

        @Override
        protected WeatherForecast doInBackground(String... params) {

            String data = ( (new WeatherHttpClient()).getForecastWeatherData(params[0], params[1], params[2]));

            WeatherForecast forecast = new WeatherForecast();

            try {
                forecast = JSONWeatherParser.getForecastWeather(data);
                System.out.println("Weather ["+forecast+"]");
                // Let's retrieve the icon
                //weather.iconData = ( (new WeatherHttpClient()).getImage(weather.currentCondition.getIcon()));

            }catch (JSONException e) {
                e.printStackTrace();
            }
            return forecast;

        }

        // Adapting
        @Override
        protected void onPostExecute(WeatherForecast forecastWeather) {
            super.onPostExecute(forecastWeather);

            DailyForecastPageAdapter adapter = new DailyForecastPageAdapter(Integer.parseInt(forecastDaysNum), getSupportFragmentManager(), forecastWeather);

            pager.setAdapter(adapter);
        }
    }
    /* end of the F.W.A */


    // SearchView
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);//
        SearchView searchView = (SearchView)MenuItemCompat.getActionView(searchItem);//

        //SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();


        //listView = (ListView)menu.findItem(R.id.list_country);
        //listView.setAdapter(adapter);

        searchView.setOnQueryTextListener(queryTextListener);

        return true;

    }

    private SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {

        @Override
        public boolean onQueryTextSubmit(String query) {
            //searchWeatherNshow(possibleCities.get(0)); // 엔터치면 이 줄이 실행됨. 0 : seoul, get만 바꿈. get할 필요없이 searchinPossivlecities 가 리스트를 뿌리면 그 중의 아이템
            searchWeatherNshow(query);//
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            //Log.d(TAG, "onQueryTextChange: "+newText);
            searchInPossibleCities(newText);

            return false;
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();//

        /*noinspection SimplifiableIfStatement
        if (id == R.id.menu_search) {
            return true;
        }*/

        searchWeatherNshow(possibleCities.get(id));//

        return super.onOptionsItemSelected(item);

    }

}

