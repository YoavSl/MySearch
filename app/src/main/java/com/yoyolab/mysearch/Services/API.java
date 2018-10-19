package com.yoyolab.mysearch.Services;

import android.util.Log;

import com.yoyolab.mysearch.Model.Product;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class API {
    private final static String DATA_ENDPOINT_BY_NAME = "https://platform.shopyourway.com/products/search?" +
            "q=%s" +
            "&page=1" +
            "&token=0_20975_253402300799_1_39c0fd9abf524b96985688e78892212c05f34203a46ac36a4117f211b41c7f5d&hash=16eba7802b35f6cb1b03dbf6262d4db0808f437a14f070019a6fa98da45b3d90";
    private final static String DATA_ENDPOINT_BY_PRODUCT_ID = "https://platform.shopyourway.com/products/get?" +
            "ids=%s" +
            "&page=1" +
            "&token=0_20975_253402300799_1_39c0fd9abf524b96985688e78892212c05f34203a46ac36a4117f211b41c7f5d&hash=16eba7802b35f6cb1b03dbf6262d4db0808f437a14f070019a6fa98da45b3d90";
    private final static String DATA_ENDPOINT_BY_CATEGORY_ID = "https://platform.shopyourway.com/products/search?" +
            "filter=tags:%s" +
            "&page=1" +
            "&token=0_18401_253402300799_1_c78a591a5ecaf201c77c315dae461f0647bbbe90bc5f999d782de90e6b5bdb6f&hash=b8b5adaf022fcbc2f70476b3d0181bd2a12b859d440cc40aa9638aa2513eaebe";

    public ArrayList<Product> get(String query, String searchType) throws IOException {
        URL url;

        if (searchType.equals("ByName"))
            url = new URL(String.format(DATA_ENDPOINT_BY_NAME, query));
        else if (searchType.equals("ByProductId"))
            url = new URL(String.format(DATA_ENDPOINT_BY_PRODUCT_ID, query));
        else {
            url = new URL(String.format(DATA_ENDPOINT_BY_CATEGORY_ID, query));
        }

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setReadTimeout(5*1000);
        connection.connect();

        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null)
            stringBuilder.append(line + "\n");

        return this.parseApiResults(stringBuilder.toString(), searchType);
    }

    private ArrayList<Product> parseApiResults(String res, String searchType) {
        JSONArray products;
        ArrayList<Product> results = new ArrayList<>();
        try {
            if (searchType.equals("ByProductId")){
                products = new JSONArray(res);
                JSONObject productObj = (JSONObject) products.get(0);
                results.add(parseProduct(productObj));
            }
            else {   //ByName or ByCategoryId

                JSONObject json = new JSONObject(res);
                products = json.getJSONArray("products");

                for (int i = 0; i < products.length(); i++) {
                    JSONObject productObj = (JSONObject) products.get(i);
                    results.add(parseProduct(productObj));
                }
            }
        } catch (JSONException e) {
            Log.e("Error parsing JSON", e.getMessage());
        }
        Log.d("myTag", "myCheck4, products: " + results);
        return results;
    }

    private Product parseProduct(JSONObject productObj) throws JSONException{
        int price;

        //Not every product has a price so we need to check it
        try {
            price = productObj.getInt("price");
        } catch (JSONException e) {
            price = 0;
        }

        return new Product(
                productObj.getInt("id"),
                price,
                productObj.getString("name"),
                productObj.getString("description"),
                productObj.getString("imageUrl")
        );
    }
}