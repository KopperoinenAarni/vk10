package com.example.vk10;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class SearchActivity extends AppCompatActivity {

    private TextView statusText;
    private EditText cityEdit;
    private EditText yearEdit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_seach);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

        statusText = findViewById(R.id.StatusText);
        cityEdit = findViewById(R.id.CityNameEdit);
        yearEdit = findViewById(R.id.YearEdit);
        int total = 0;
    }


    public void switchToList(View view) {
        Intent intent = new Intent(this, ListInfoActivity.class);
        startActivity(intent);
    }

    public void switchToMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    public void searchButton(View view) {
        String city = cityEdit.getText().toString().trim();
        String yearText = yearEdit.getText().toString().trim();
        runOnUiThread(() -> statusText.setText(("Haetaan Haku")));

        if (city.isEmpty()) {
            statusText.setText("Haku epäonnistui, et syöttänyt kaupunkia!");
            return;
        }

        if (yearText.isEmpty()) {
            statusText.setText("Haku epäonnistui, et syöttänyt vuotta!");
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearText);
        } catch (NumberFormatException e) {
            statusText.setText("Haku epäonnistui, vuoden täytyy olla numero!");
            return;
        }

        CarDataStorage storage1 = CarDataStorage.getInstance();
        storage1.clearData();
        storage1.setCity(city);
        storage1.setYear(year);

        getData(this, city, year);

    }

    public void getData(Context context, String city, int year) {
        new Thread(() -> {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode areas = null;

            try {
                areas = objectMapper.readTree(new URL("https://pxdata.stat.fi/PxWeb/api/v1/fi/StatFin/mkan/statfin_mkan_pxt_11ic.px"));
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            ArrayList<String> keys = new ArrayList<>();
            ArrayList<String> values = new ArrayList<>();

            for (JsonNode node : areas.get("variables").get(0).get("values")) {
                values.add(node.asText());
            }
            for (JsonNode node : areas.get("variables").get(0).get("valueTexts")) {
                keys.add(node.asText());
            }

            HashMap<String, String> cityCodes = new HashMap<>();

            for (int i = 0; i < keys.size(); i++) {
                cityCodes.put(keys.get(i), values.get(i));
            }

            if (!cityCodes.containsKey(city)) {
                runOnUiThread(() -> statusText.setText("Haku epäonnistui, syöttämäsi kaupunki ei ole olemassa!"));
                System.out.println(CarDataStorage.getInstance().getCity());
                return;
            }

            String code = null;
            code = cityCodes.get(city);


            try {
                URL url = new URL("https://pxdata.stat.fi/PxWeb/api/v1/fi/StatFin/mkan/statfin_mkan_pxt_11ic.px");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json; utf-8");
                con.setRequestProperty("Accept", "application/json");
                con.setDoOutput(true);

                JsonNode jsonInputString = objectMapper.readTree(context.getResources().openRawResource(R.raw.query));


                // Ensin hakee kaupungit, indeksi 0 ja toinen hakee vuodet, indeksi 3
                ((ObjectNode) jsonInputString.get("query").get(0).get("selection")).putArray("values").removeAll().add(code);
                ((ObjectNode) jsonInputString.get("query").get(3).get("selection")).putArray("values").removeAll().add(String.valueOf(year));

                byte[] input = objectMapper.writeValueAsBytes(jsonInputString);
                OutputStream os = con.getOutputStream();
                os.write(input);

                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }

                System.out.println("Jos kaupunki ok, se on:" + CarDataStorage.getInstance().getCity());

                JsonNode carData = objectMapper.readTree(response.toString());

                String[] vehicleCodes = {"01", "02", "03", "04", "05"};

                JsonNode types = carData.get("dimension").get("Ajoneuvoluokka").get("category");
                JsonNode typesLabel = types.get("label");
                JsonNode carIndex = types.get("index");
                JsonNode amounts = carData.get("value");

                CarDataStorage storage = CarDataStorage.getInstance();
                storage.clearData();
                storage.setCity(city);
                storage.setYear(year);

                int totalAmount = 0;

                for (String vehicleCode : vehicleCodes) {
                    String label = typesLabel.get(vehicleCode).asText();
                    int index = carIndex.get(vehicleCode).asInt();
                    int amount = amounts.get(index).asInt();
                    storage.addCarData(new CarData(label, amount));
                    totalAmount += amount;
                }


                runOnUiThread(() -> statusText.setText("Haku onnistui"));


            } catch (IOException e) {
                runOnUiThread(() -> statusText.setText("Haku epäonnistui: " + e.getMessage()));
                e.printStackTrace();
                return;
            }
        }).start();
    }
}


