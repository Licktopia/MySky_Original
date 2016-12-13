package jltechnologies.com.mysky.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import butterknife.BindView;
import butterknife.ButterKnife;

import butterknife.OnClick;
import im.delight.android.location.SimpleLocation;
import jltechnologies.com.mysky.weather.Current;
import jltechnologies.com.mysky.weather.Day;
import jltechnologies.com.mysky.weather.Forecast;
import jltechnologies.com.mysky.weather.Hour;
import me.grantland.widget.AutofitHelper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String DAILY_FORECAST = "DAILY_FORECAST";
    public static final String HOURLY_FORECAST = "HOURLY_FORECAST";

private SimpleLocation mLocation;
    private static final String[] INITIAL_PERMS={
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private static final String[] LOCATION_PERMS={
            android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static final int INITIAL_REQUEST=1337;
    private static final int LOCATION_REQUEST=INITIAL_REQUEST+3;

    private RequestQueue requestQueue;

    private double latitude;
    private double longitude;
    public static String location;


    private Forecast mForecast;

    @BindView(R.id.timeLabel)
    TextView mTimeLabel;
    @BindView(R.id.locationLabel)
    TextView mLocationLabel;
    @BindView(R.id.temperatureLabel)
    TextView mTemperatureLabel;
    @BindView(R.id.humidityValue)
    TextView mHumidityValue;
    @BindView(R.id.precipValue)
    TextView mPrecipValue;
    @BindView(R.id.summaryLabel)
    TextView mSummaryLabel;
    @BindView(R.id.iconImageView)
    ImageView mIconImageView;
    @BindView(R.id.refreshImageView)
    ImageView mRefreshImageView;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    //construct a new instance of SimpleLocation
        mLocation = new SimpleLocation(this);
        ButterKnife.bind(this);
        mProgressBar.setVisibility(View.INVISIBLE);
        latitude = mLocation.getLatitude();
        longitude = mLocation.getLongitude();
        mLocation.beginUpdates();
        AutofitHelper.create(mLocationLabel);
        // if we can't access the location yet
        if (!mLocation.hasLocationEnabled()) {
            Toast.makeText(this, "Please turn on location and refresh", Toast.LENGTH_SHORT).show();
            //SimpleLocation.openSettings(this);
        }
            if(mLocation.hasLocationEnabled()) {
                getForecast(latitude, longitude);
            }
                mRefreshImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mLocation.beginUpdates();
                        latitude = mLocation.getLatitude();
                        longitude = mLocation.getLongitude();
                        if(mLocation.hasLocationEnabled()) {
                            getForecast(latitude, longitude);
                        }
                    }
                });
            Log.d(TAG, "Main UI code is running!");

    }
    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            mLocation.beginUpdates();
            latitude = mLocation.getLatitude();
            longitude = mLocation.getLongitude();
            if(mLocation.hasLocationEnabled()) {
                getForecast(latitude, longitude);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[] {
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION },
                    LOCATION_REQUEST);
            mLocation.beginUpdates();
        }
    }

    @Override
    protected void onPause() {
        // stop location updates (saves battery)
        mLocation.endUpdates();
        super.onPause();
    }
    private void getForecast(double latitude, double longitude) {
        if(mLocation.hasLocationEnabled()) {
            mLocation.beginUpdates();
            String apiKey = "6cd32aa5325c547bed3025087445c51b";
            String forecastUrl = "https://api.darksky.net/forecast/" + apiKey + "/" + latitude + "," + longitude;
            setLocation();
            if (isNetworkAvailable()) {
                toggleRefresh();
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(forecastUrl)
                        .build();
                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                toggleRefresh();
                            }
                        });
                        alertUserAboutError();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                toggleRefresh();
                            }
                        });
                        try {
                            String jsonData = response.body().string();
                            Log.v(TAG, jsonData);
                            if (response.isSuccessful()) {
                                mForecast = parseForecastDetails(jsonData);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateDisplay();
                                    }
                                });

                            } else {
                                alertUserAboutError();

                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Exception caught: ", e);
                        } catch (JSONException e) {
                            Log.e(TAG, "Exception caught: ", e);
                        }
                    }
                });
            } else {
                Toast.makeText(this, R.string.network_unavailable_message, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
    };

    private void updateDisplay() {
        Current current = mForecast.getCurrent();
        mLocationLabel.setText(current.getLocation());
        mTemperatureLabel.setText(current.getTemperature() + "");
        mTimeLabel.setText("At " + current.getFormattedTime() + " it will be");
        mHumidityValue.setText(current.getHumidity() + "");
        mPrecipValue.setText(current.getPrecipChance() + "%");
        mSummaryLabel.setText(current.getSummary());

        Drawable drawable = getResources().getDrawable(mForecast.getCurrent().getIconId());
        mIconImageView.setImageDrawable(drawable);
    }

    private Forecast parseForecastDetails(String jsonData)throws JSONException{
        Forecast forecast = new Forecast();

        forecast.setCurrent(getCurrentDetails(jsonData));
        forecast.setDailyForecast(getDailyForecast(jsonData));
        forecast.setHourlyForecast(getHourlyForecast(jsonData));
        return forecast;

    }

    private Day[] getDailyForecast(String jsonData) throws JSONException{
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");

        JSONObject daily = forecast.getJSONObject("daily");
        JSONArray data = daily.getJSONArray("data");

        Day[] days = new Day[data.length()];

        for(int i = 0; i < data.length(); i++){
            JSONObject jsonDay = data.getJSONObject(i);
            Day day = new Day();

            day.setSummary(jsonDay.getString("summary"));
            day.setIcon(jsonDay.getString("icon"));
            day.setTemperatureMax(jsonDay.getDouble("temperatureMax"));
            day.setTime(jsonDay.getLong("time"));
            day.setTimezone(timezone);

            days[i] = day;
        }
        return days;
    }

    private Hour[] getHourlyForecast(String jsonData) throws JSONException{
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");

        JSONObject hourly = forecast.getJSONObject("hourly");
        JSONArray data = hourly.getJSONArray("data");

        Hour[] hours = new Hour[data.length()];

        for(int i = 0; i <data.length(); i++){
            JSONObject jsonHour = data.getJSONObject(i);
            Hour hour = new Hour();

            hour.setSummary(jsonHour.getString("summary"));
            hour.setTemperature(jsonHour.getDouble("temperature"));
            hour.setIcon((jsonHour.getString("icon")));
            hour.setTime(jsonHour.getLong("time"));
            hour.setTimezone(timezone);
            hours[i] = hour;
        }
        return hours;

    }

    private Current getCurrentDetails(String jsonData) throws JSONException{
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        Log.i(TAG, "From JSON: " + timezone);

        JSONObject currently = forecast.getJSONObject("currently");
        Current current = new Current();
        current.setLocation(location);
        current.setHumidity(currently.getDouble("humidity"));
        current.setTime(currently.getLong("time"));
        current.setIcon(currently.getString("icon"));
        current.setPrecipChance(currently.getDouble("precipProbability"));
        current.setSummary(currently.getString("summary"));
        current.setTemperature(currently.getDouble("temperature"));
        current.setTimeZone(timezone);

        Log.d(TAG, current.getFormattedTime());

        return current;

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if(networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }
        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }
    @OnClick(R.id.dailyButton)
    public void startDailyActivity(View view){
        if(mLocation.hasLocationEnabled()) {
        Intent intent = new Intent(this,DailyForecastActivity.class);
        intent.putExtra(DAILY_FORECAST, mForecast.getDailyForecast());

            startActivity(intent);
        }
        else
        {
            Toast.makeText(this, "Please turn on location and refresh", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick (R.id.hourlyButton)
    public void startHourlyActivity(View view){
        if(mLocation.hasLocationEnabled()) {
        Intent intent = new Intent(this, HourlyForecastActivity.class);
        intent.putExtra(HOURLY_FORECAST, mForecast.getHourlyForecast());

            startActivity(intent);
        }
        else
        {
            Toast.makeText(this, "Please turn on location and refresh", Toast.LENGTH_SHORT).show();
        }
    }

    private void setLocation(){
        requestQueue = Volley.newRequestQueue(this);
        String lat = latitude +"";
        String lon = longitude + "";
        String key = "AIzaSyBn2PrpyCBOvZZCAB2Po0UXYnpnkpf10IQ";
        String jReq = "https://maps.googleapis.com/maps/api/geocode/json?latlng="+lat+","+lon+"&key"+key;

        JsonObjectRequest request = new JsonObjectRequest(jReq, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    location = response.getJSONArray("results").getJSONObject(2).getString("formatted_address");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(request);
    }
    public static String getLocation(){
        return location;
    }
}



