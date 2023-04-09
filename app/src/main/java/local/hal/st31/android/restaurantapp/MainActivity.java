package local.hal.st31.android.restaurantapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

public class MainActivity extends AppCompatActivity implements LocationListener {

    LocationManager locationManager;

    /**
     * 緯度を表すフィールド。
     */
    private double _latitude = 0;
    /**
     * 経度を表すフィールド。
     */
    private double _longitude = 0;

    /**
     * 距離の値を表すフィールド
     */
    private int _range;

    /**
     * キーワードの値を渡すフィールド
     */
    private String _keyword;


    private final ActivityResultLauncher<String>
            requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    locationStart();
                }
                else {
                    Toast toast = Toast.makeText(this,
                            "これ以上なにもできません", Toast.LENGTH_SHORT);
                    toast.show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION);
        }
        else{
            locationStart();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        _latitude = location.getLatitude();//緯度を取得
        _longitude = location.getLongitude();//経度を取得
    }

    private void locationStart(){
        Log.d("debug","locationStart()");
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager != null && locationManager.isProviderEnabled(
                LocationManager.GPS_PROVIDER)) {
        } else {
            // GPSを設定するように促す
            Intent settingsIntent =
                    new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);

            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000, 50, this);

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //検索ボタンが押された時の処理
    public void onSearchButtonClick(View view){
        TextView etKeyword = findViewById(R.id.etKeyword);
        _keyword = etKeyword.getText().toString();
        RadioGroup rg = findViewById(R.id.rgRangeGroup);
        int id = rg.getCheckedRadioButtonId();
        if(id != -1){
            RadioButton rb = (RadioButton) findViewById(id);
            String strRb = rb.getText().toString();
            if(strRb.equals("300m")){
                _range = 1;
            }else if (strRb.equals("500m")){
                _range = 2;
            }else if (strRb.equals("1000m")){
                _range = 3;
            }else if (strRb.equals("2000m")){
                _range = 4;
            }else if (strRb.equals("3000m")){
                _range = 5;
            }
            Intent intent = new Intent(MainActivity.this, SearchResultActivity.class);
            intent.putExtra("keyword", _keyword);//検索キーワード
            intent.putExtra("rangeId", Integer.valueOf(_range).toString());//距離ID
            intent.putExtra("latitude", Double.valueOf(_latitude).toString());//現在地の緯度
            intent.putExtra("longitude", Double.valueOf(_longitude).toString());//現在地の経度
            startActivity(intent);
        }else{
            Toast.makeText(this, "検索範囲が指定されていません。", Toast.LENGTH_SHORT).show();
        }

    }
}