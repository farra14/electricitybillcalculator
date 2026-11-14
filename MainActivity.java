package com.example.electricitybillcalculator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Spinner spinnerMonth;
    EditText editUnits;
    SeekBar seekRebate;
    TextView textRebate, textTotal, textFinal;
    Button buttonCalculate, buttonSave, buttonHistory, buttonAbout;

    DataHelper dbHelper;
    double totalCharges = 0;
    double finalCost = 0;

    String[] months = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Electricity Bill Calculator");

        dbHelper = new DataHelper(this);

        spinnerMonth = findViewById(R.id.spinnerMonth);
        editUnits = findViewById(R.id.editUnits);
        seekRebate = findViewById(R.id.seekRebate);
        textRebate = findViewById(R.id.textRebate);
        textTotal = findViewById(R.id.textTotal);
        textFinal = findViewById(R.id.textFinal);
        buttonCalculate = findViewById(R.id.buttonCalculate);
        buttonSave = findViewById(R.id.buttonSave);
        buttonHistory = findViewById(R.id.buttonHistory);
        buttonAbout = findViewById(R.id.buttonAbout);

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        seekRebate.setMax(5);
        seekRebate.setProgress(0);
        textRebate.setText("Rebate: 0%");
        seekRebate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textRebate.setText("Rebate: " + progress + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        buttonCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateBill();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!calculateBill()) {
                    return;
                }

                String month = spinnerMonth.getSelectedItem().toString();
                int units = Integer.parseInt(editUnits.getText().toString().trim());
                int rebate = seekRebate.getProgress();

                long result = dbHelper.insertBill(month, units, totalCharges, rebate, finalCost);
                if (result != -1) {
                    Toast.makeText(MainActivity.this, "Bill saved successfully", Toast.LENGTH_SHORT).show();
                    editUnits.setText("");
                } else {
                    Toast.makeText(MainActivity.this, "Unable to save bill", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        buttonAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean calculateBill() {
        String unitText = editUnits.getText().toString().trim();

        if (unitText.isEmpty()) {
            editUnits.setError("Please enter electricity units");
            editUnits.requestFocus();
            Toast.makeText(this, "Enter units from 1 to 1000 kWh", Toast.LENGTH_SHORT).show();
            return false;
        }

        int units;
        try {
            units = Integer.parseInt(unitText);
        } catch (NumberFormatException e) {
            editUnits.setError("Please enter a valid number");
            return false;
        }

        if (units < 1 || units > 1000) {
            editUnits.setError("Units must be between 1 and 1000");
            editUnits.requestFocus();
            return false;
        }

        totalCharges = 0;

        int remaining = units;

        int block1 = Math.min(remaining, 200);
        totalCharges += block1 * 0.218;
        remaining -= block1;

        if (remaining > 0) {
            int block2 = Math.min(remaining, 100);
            totalCharges += block2 * 0.334;
            remaining -= block2;
        }

        if (remaining > 0) {
            int block3 = Math.min(remaining, 300);
            totalCharges += block3 * 0.516;
            remaining -= block3;
        }

        if (remaining > 0) {
            int block4 = Math.min(remaining, 300);
            totalCharges += block4 * 0.546;
        }

        int rebate = seekRebate.getProgress();
        finalCost = totalCharges - (totalCharges * rebate / 100.0);

        textTotal.setText(String.format(Locale.getDefault(), "Total Charges: RM %.2f", totalCharges));
        textFinal.setText(String.format(Locale.getDefault(), "Final Cost: RM %.2f", finalCost));
        return true;
    }
}
