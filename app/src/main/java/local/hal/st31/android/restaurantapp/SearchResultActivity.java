package local.hal.st31.android.restaurantapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SearchResultActivity extends AppCompatActivity {

    /**
     * ログに記載するタグ用の文字列
     */
    private static final String DEBUG_TAG = "RestaurantApp";

    /**
     * お店の情報のURL
     */
    private static final String ACCESS_URL = "http://webservice.recruit.co.jp/hotpepper/gourmet/v1/?key=5e5c3566841f93a7";

    /**
     * リストビューに表示されるデータ
     */
    private List<Map<String, String>> _list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        //アクションバーオブジェクト
        ActionBar actionBar = getSupportActionBar();
        //アクションバーに戻るボタンを追加
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);

        List<String> shopNames = new ArrayList<>();
        List<String> shopAccess = new ArrayList<>();
        List<String> shopImageUrls = new ArrayList<>();

        _list = createList();

        for(Map<String, String> map : _list){
            shopNames.add(map.get("name"));
            shopAccess.add(map.get("access"));
            shopImageUrls.add(map.get("imageUrl"));
        }

        ListView lvShopList = findViewById(R.id.lvShopList);
        ShopAdapter adapter = new ShopAdapter(this, shopNames, shopAccess, shopImageUrls);
        lvShopList.setAdapter(adapter);
        lvShopList.setOnItemClickListener(new ListItemClickListener());
    }


    /**
     * オプションメニューのアイテムをクリックされたときの処理
     * @param item
     * @return
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int itemId = item.getItemId();
        boolean returnVal =  true;
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
     * リストに値を格納する処理(onCreateで実行される)
     * @return
     */
    private List<Map<String, String>> createList(){
        Intent intent = getIntent();
        String keyword = intent.getStringExtra("keyword");
        int rangeId = Integer.parseInt(intent.getStringExtra("rangeId"));
        double latitude = Double.parseDouble(intent.getStringExtra("latitude"));
        double longitude = Double.parseDouble(intent.getStringExtra("longitude"));
        StringBuilder urlStringBuilder = new StringBuilder();
        urlStringBuilder.append(ACCESS_URL);
        urlStringBuilder.append("&keyword=");
        urlStringBuilder.append(keyword);
        urlStringBuilder.append("&range=");
        urlStringBuilder.append(rangeId);
        urlStringBuilder.append("&lat=");
        urlStringBuilder.append(latitude);
        urlStringBuilder.append("&lng=");
        urlStringBuilder.append(longitude);
        urlStringBuilder.append("&count=100");
        urlStringBuilder.append("&format=json");
        String strUrl = urlStringBuilder.toString();
        return receiveShopInfo(strUrl);
    }

    /**
     * リストがクリックされた時の処理
     */
    private class ListItemClickListener implements AdapterView.OnItemClickListener{
        @Override
        public  void onItemClick(AdapterView<?> parent, View view, int position, long id){
            Map<String, String> item = _list.get(position);
            String listId = item.get("id");
            Intent intent = new Intent(SearchResultActivity.this, ShopInfoActivity.class);
            intent.putExtra("shopId", item.get("id"));//店舗ID
            intent.putExtra("shopName", item.get("name"));//店舗名
            intent.putExtra("shopAddress", item.get("address"));//店舗住所
            intent.putExtra("shopImage", item.get("imageUrl"));//店舗画像
            intent.putExtra("shopAccess", item.get("access"));//アクセス
            intent.putExtra("shopOpen", item.get("open"));//店舗営業時間
            intent.putExtra("url", item.get("urls"));//店舗URL
            intent.putExtra("midnight", item.get("midnight"));//深夜営業をおこなっているか
            intent.putExtra("lat", item.get("latitude"));//緯度
            intent.putExtra("lng", item.get("longitude"));//経度
            intent.putExtra("wifi", item.get("wifi"));//wifi有無
            intent.putExtra("shopStationName", item.get("stationName"));//店舗最寄駅
            startActivity(intent);
        }
    }

    /**
     * URLにアクセスしてAPIに接続する
     * @param urlFull
     * @return
     */
    private List receiveShopInfo(String urlFull){

        shopInfoBackgroundReceiver backgroundReceiver = new shopInfoBackgroundReceiver(urlFull);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<String> future = executorService.submit(backgroundReceiver);
        String result = "";
        try {
            result = future.get();
        }catch (ExecutionException ex){
            Log.w(DEBUG_TAG, "非同期処理の取得で例外発生:", ex);
        }catch (InterruptedException ex){
            Log.w(DEBUG_TAG, "日同時処理の取得で例外発生:", ex);
        }
        return getShopInfo(result);
    }

    /**
     * URLにアクセスしJSONから値を取得してくる処理
     * @param result
     * @return
     */
    @UiThread
    private List getShopInfo(String result){
        String title = "";
        String msg = "";
        List<HashMap<String, String>> list = new ArrayList<>();
        try{
            JSONObject rootJSON = new JSONObject(result);
            String strResultNum = rootJSON.getJSONObject("results").getString("results_available");
            int resultNum = Integer.parseInt(strResultNum);
            TextView tvResult = findViewById(R.id.tvResult);
            tvResult.setText(resultNum + "件のお店が見つかりました");
            JSONArray shopInfoJSONArray = rootJSON.getJSONObject("results").getJSONArray("shop");
            for(int i = 0; i < shopInfoJSONArray.length(); i++){
                JSONObject info = shopInfoJSONArray.getJSONObject(i);
                HashMap<String, String> map = new HashMap<>();
                map.put("id", info.getString("id"));
                map.put("name", info.getString("name"));
                map.put("address", info.getString("address"));
                map.put("imageUrl", info.getString("logo_image"));
                map.put("access", info.getString("mobile_access"));
                map.put("open", info.getString("open"));
                map.put("urls", info.getJSONObject("urls").getString("pc"));
                map.put("midnight", info.getString("midnight"));
                map.put("latitude", info.getString("lat"));
                map.put("longitude", info.getString("lng"));
                map.put("wifi", info.getString("wifi"));
                map.put("stationName", info.getString("station_name"));
                list.add(map);
            }
        }catch (JSONException ex){
            Log.e(DEBUG_TAG, "JSON解析失敗");
        }
        return list;
    }

    /**
     * 非同期でサーバから値を取得
     */
    private class shopInfoBackgroundReceiver implements Callable<String>{


        private final String _url;

        public shopInfoBackgroundReceiver(String url){
            _url = url;
        }

        @WorkerThread
        @Override
        public String call(){
            HttpURLConnection con = null;
            InputStream is = null;
            String result = "";
            try {
                URL url = new URL(_url);
                con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(1000);
                con.setReadTimeout(1000);
                con.setRequestMethod("GET");
                con.connect();
                int status = con.getResponseCode();
                if(status != 200){
                    throw new IOException();
                }
                is = con.getInputStream();
                result = is2String(is);
            }catch(MalformedURLException ex) {
                Log.e(DEBUG_TAG, "URL変換失敗", ex);
            }
            catch(SocketTimeoutException ex) {
                Log.w(DEBUG_TAG, "通信タイムアウト", ex);
            }
            catch(IOException ex) {
                Log.e(DEBUG_TAG, "通信失敗", ex);
            }finally {
                if(con != null) {
                    con.disconnect();
                }
                if(is != null) {
                    try {
                        is.close();
                    }
                    catch(IOException ex) {
                        Log.e(DEBUG_TAG, "InputStream解放失敗", ex);
                    }
                }
            }
            return result;
        }

        /**
         * InputStreamオブジェクトを文字列に変換するメソッド。変換文字コードはUTF-8
         *
         * @param is 変換対象のInputStreamオブジェクト。
         * @return 変換された文字列。
         * @throws IOException 変換に失敗した時に発生。
         */
        private String is2String(InputStream is) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuffer sb = new StringBuffer();
            char[] b = new char[1024];
            int line;
            while(0 <= (line = reader.read(b))) {
                sb.append(b, 0, line);
            }
            return sb.toString();
        }

    }

    /**
     * ListViewの値を設定
     */
    public class ShopAdapter extends ArrayAdapter<String> {
        private final List<String> shopNames;
        private final List<String> shopAccesses;
        private final List<String> shopImageUrls;
        private final Context context;

        /**
         * コンストラクタ
         * @param context
         * @param shopNames
         * @param shopAccesses
         * @param shopImageUrls
         */
        public ShopAdapter(Context context, List<String> shopNames, List<String> shopAccesses, List<String> shopImageUrls) {
            super(context, R.layout.shop_item, shopNames);
            this.context = context;
            this.shopNames = shopNames;
            this.shopAccesses = shopAccesses;
            this.shopImageUrls = shopImageUrls;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.shop_item, parent, false);
            }

            TextView nameTextView = convertView.findViewById(R.id.tvShopName);
            TextView accessTextView = convertView.findViewById(R.id.tvShopAccess);
            ImageView imageView = convertView.findViewById(R.id.ivShopImage);

            nameTextView.setText(shopNames.get(position));
            accessTextView.setText(shopAccesses.get(position));
            Picasso.get().load(shopImageUrls.get(position)).into(imageView);

            return convertView;
        }
    }


}