package com.tistory.dagylo.weatherforecastapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import com.tistory.dagylo.weatherforecastapplication.adapter.DailyForecastPageAdapter;
import com.tistory.dagylo.weatherforecastapplication.model.Weather;
import com.tistory.dagylo.weatherforecastapplication.model.WeatherForecast;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{//

    private TextView cityText;
    private TextView condDescr;
    private TextView temp;
    private TextView unitTemp;
    private ImageView imgView;
    private ViewPager pager;

    //private ArrayAdapter<String> adapter; // 도시 리스트 어댑터
    private List<String> possibleCities = new ArrayList<>();// 도시 리스트

    private static String forecastDaysNum = "7"; // 7일로 수정.
    private final String lang = "en";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ToolBar(action bar)
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cityText = (TextView) findViewById(R.id.cityText);
        temp = (TextView) findViewById(R.id.temp); // today's temperature
        unitTemp = (TextView) findViewById(R.id.unittemp); // celcius
        //unitTemp.setText("°C");//
        condDescr = (TextView) findViewById(R.id.skydesc);

        pager = (ViewPager) findViewById(R.id.pager);
        imgView = (ImageView) findViewById(R.id.condIcon);

        /* Help Toast */
        LayoutInflater inflater = getLayoutInflater();
        View helpLayout = inflater.inflate(R.layout.activity_help,
                (ViewGroup) findViewById(R.id.custom_toast));

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(helpLayout);
        toast.show();
        //adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, possibleCities);
    }

    // 가능한 city 검색
    private List<String> searchInPossibleCities(String text){
        List<String> results = new ArrayList<>();

        for (String city:
                possibleCities) {
            if (city.contains(text)) {
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

            // "City, Country"
            cityText.setText(weather.location.getCity() + "," + weather.location.getCountry());
            // ex. 16c(celcius)
            temp.setText("" + Math.round((weather.temperature.getTemp() - 275.15)));
            unitTemp.setText("°C");
            // ex. Clear(clear sky)
            condDescr.setText(weather.currentCondition.getCondition() + "(" + weather.currentCondition.getDescr() + ")");

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

            }catch (JSONException e) {
                e.printStackTrace();
            }
            return forecast;
        }

        // Adapting
        @Override
        protected void onPostExecute(WeatherForecast forecastWeather) {
            super.onPostExecute(forecastWeather);

            DailyForecastPageAdapter adapter =
                    new DailyForecastPageAdapter(Integer.parseInt(forecastDaysNum),
                            getSupportFragmentManager(), forecastWeather);

            pager.setAdapter(adapter);
        }
    }

    // SearchView
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);

        SearchView searchView = (SearchView)MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint("City, Country");
        searchView.setOnQueryTextListener(queryTextListener);

        return true;
    }

    private SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {

        @Override
        public boolean onQueryTextSubmit(String query) { // when entered the city&country
            searchWeatherNshow(query);//
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) { // when typing strings

            searchInPossibleCities(newText);

            return false;
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();//

        searchWeatherNshow(possibleCities.get(id));//

        return super.onOptionsItemSelected(item);
    }

}

