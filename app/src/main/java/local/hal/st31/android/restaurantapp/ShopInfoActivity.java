package local.hal.st31.android.restaurantapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ShopInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_info);
        //アクションバーオブジェクト
        ActionBar actionBar = getSupportActionBar();
        //アクションバーに戻るボタンを追加
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        Intent intent  = getIntent();
        String name = intent.getStringExtra("shopName");
        String access = intent.getStringExtra("shopAccess");
        String address = intent.getStringExtra("shopAddress");
        String image = intent.getStringExtra("shopImage");
        String open = intent.getStringExtra("shopOpen");
        String url = intent.getStringExtra("url");
        String midnight = intent.getStringExtra("midnight");
        String stationName = intent.getStringExtra("shopStationName");
        String wifi = intent.getStringExtra("wifi");
        TextView tvName = findViewById(R.id.tvNameValue);
        tvName.setText(name);
        TextView tvAccess = findViewById(R.id.tvAccessValue);
        tvAccess.setText(access);
        TextView tvAddress = findViewById(R.id.tvAddressValue);
        tvAddress.setText(address);
        TextView tvOpen = findViewById(R.id.tvOpenValue);
        tvOpen.setText(open);
        TextView tvUrl = findViewById(R.id.tvUrlValue);
        tvUrl.setText(url);
        TextView tvMidnight = findViewById(R.id.tvMidnightValue);
        tvMidnight.setText(midnight);
        TextView tvStationName = findViewById(R.id.tvStationValue);
        tvStationName.setText(stationName);
        TextView tvWifi = findViewById(R.id.tvWifiValue);
        tvWifi.setText(wifi);
        ImageView ivLogoImage = findViewById(R.id.tvLogoImage);
        setImageUrl(image, ivLogoImage);
    }

    /**
     * アクションバーのボタンが押された時の処理
     * @param item
     * @return
     */
    public boolean onOptionsItemSelected(MenuItem item){
        int itemId = item.getItemId();
        boolean returnVal = true;
        switch (itemId){
            case android.R.id.home:
                finish();
                break;
            default:
                returnVal = super.onOptionsItemSelected(item);
        }
        return returnVal;
    }

    /**
     * URLで検索するボタンが押された時の処理
     * @param view
     */
    public void onUrlSearchButtonClick(View view){
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        Uri uri = Uri.parse(url);
        Intent intent2 = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent2);
    }

    /**
     * マップで開くボタンが押された時の処理
     * @param view
     */
    public void onMapSearchButtonClick(View view){
        Intent intent = getIntent();
        double latitude = Double.parseDouble(intent.getStringExtra("lat"));
        double longitude = Double.parseDouble(intent.getStringExtra("lng"));
        String strUri = "geo:" + latitude + "," + longitude;
        Uri uri = Uri.parse(strUri);
        Intent intent2 = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent2);
    }

    /**
     * 画像を表示するためにURLからロードしてくる処理
     * @param imageUrl
     * @param imageView
     */
    public void setImageUrl(String imageUrl, ImageView imageView){
        Picasso.get().load(imageUrl).into(imageView);
    }
}