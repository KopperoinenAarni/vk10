package com.example.vk10;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ListInfoActivity extends AppCompatActivity {
    private TextView cityText, yearText, carInfoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_info);

        cityText = findViewById(R.id.CityText);
        yearText = findViewById(R.id.YearText);
        carInfoText = findViewById(R.id.CarInfoText);

        CarDataStorage storage = CarDataStorage.getInstance();

        cityText.setText(storage.getCity());
        yearText.setText(String.valueOf(storage.getYear()));

        StringBuilder carData = new StringBuilder();
        int totalAmount = 0;
        for (CarData car : storage.getCarData()) {
            carData.append(car.getType()).append(": ").append(car.getAmount()).append("\n");
            totalAmount += car.getAmount();
        }
        carData.append("\nYhteensä: ").append(totalAmount);

        carInfoText.setText(carData.toString());
    }
}
