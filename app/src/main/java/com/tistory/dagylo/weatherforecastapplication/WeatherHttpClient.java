/*
 * Copyright (C) 2013 Surviving with Android (http://www.survivingwithandroid.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tistory.dagylo.weatherforecastapplication;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherHttpClient {

    private static String BASE_URL = "http://api.openweathermap.org/data/2.5/weather?q=";
    private static String IMG_URL = "http://openweathermap.org/img/w/";
    private static String APP_ID = "&appid=f8be828fe409fbbb558ca57208adb35e"; // api key
    private static String PNG =".png"; // .png
    private static String BASE_FORECAST_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?mode=json&q=";

    public String getWeatherData(String location, String lang) {
        HttpURLConnection httpURLConnection = null ;
        InputStream inputStream = null;

        try {
            String url = BASE_URL + location + APP_ID;

            if (lang != null){
                url = url + "&lang=" + lang;
            }

            httpURLConnection = (HttpURLConnection) ( new URL(url)).openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.connect();

            // Read the response
            StringBuffer buffer = new StringBuffer();
            inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;

            while (  (line = bufferedReader.readLine()) != null ){
                buffer.append(line + "\r\n");
            }

            inputStream.close();
            httpURLConnection.disconnect();

            return buffer.toString();
        }
        catch(Throwable t) {
            t.printStackTrace();
        }
        finally {
            try { inputStream.close(); } catch(Throwable t) {}
            try { httpURLConnection.disconnect(); } catch(Throwable t) {}
        }

        return null;

    }

    // 뷰페이저 기상 예보
    public String getForecastWeatherData(String location, String lang, String sForecastDayNum) {
        HttpURLConnection httpURLConnection = null ;
        InputStream inputStream = null;
        int forecastDayNum = Integer.parseInt(sForecastDayNum);

        try {
            // Forecast
            String url = BASE_FORECAST_URL + location + APP_ID;

            if (lang != null) {
                url = url + "&lang=" + lang;
            }

            url = url + "&cnt=" + forecastDayNum;
            httpURLConnection = (HttpURLConnection) ( new URL(url)).openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.connect();

            // Read the response
            StringBuffer buffer1 = new StringBuffer();
            inputStream = httpURLConnection.getInputStream();
            BufferedReader br1 = new BufferedReader(new InputStreamReader(inputStream));
            String line1 = null;

            while (  (line1 = br1.readLine()) != null )
                buffer1.append(line1 + "\r\n");

            inputStream.close();
            httpURLConnection.disconnect();

            System.out.println("Buffer ["+buffer1.toString()+"]");
            return buffer1.toString();
        }
        catch(Throwable t) {
            t.printStackTrace();
        }
        finally {
            try { inputStream.close(); } catch(Throwable t) {}
            try { httpURLConnection.disconnect(); } catch(Throwable t) {}
        }

        return null;

    }

    // 아이콘
    public byte[] getImage(String code) {
        HttpURLConnection httpURLConnection = null ;
        InputStream inputStream = null;

        try {
            httpURLConnection = (HttpURLConnection) ( new URL(IMG_URL + code + PNG)).openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setDoInput(true);
            //con.setDoOutput(true);
            httpURLConnection.connect();

            // Read the response
            inputStream = httpURLConnection.getInputStream();
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            while ( inputStream.read(buffer) != -1)
                baos.write(buffer);

            return baos.toByteArray();
        }
        catch(Throwable t) {
            t.printStackTrace();
        }
        finally {
            try { inputStream.close(); } catch(Throwable t) {}
            try { httpURLConnection.disconnect(); } catch(Throwable t) {}
        }

        return null;

    }
}