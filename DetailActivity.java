package com.example.electricitybillcalculator;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    EditText editMonth, editUnits, editRebate;
    TextView textTotal, textFinal;
    Button buttonUpdate, buttonDelete, buttonBack;

    DataHelper dbHelper;
    int billId;
    double totalCharges;
    double finalCost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        setTitle("Bill Details");

        dbHelper = new DataHelper(this);
        billId = getIntent().getIntExtra("bill_id", -1);

        editMonth = findViewById(R.id.editMonth);
        editUnits = findViewById(R.id.editUnitsDetail);
        editRebate = findViewById(R.id.editRebateDetail);
        textTotal = findViewById(R.id.textTotalDetail);
        textFinal = findViewById(R.id.textFinalDetail);
        buttonUpdate = findViewById(R.id.buttonUpdate);
        buttonDelete = findViewById(R.id.buttonDelete);
        buttonBack = findViewById(R.id.buttonBackDetail);

        loadBill();

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBill();
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(DetailActivity.this)
                        .setTitle("Delete Bill")
                        .setMessage("Are you sure you want to delete this bill?")
                        .setPositiveButton("Yes", new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(android.content.DialogInterface dialog, int which) {
                                dbHelper.deleteBill(billId);
                                Toast.makeText(DetailActivity.this, "Bill deleted", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadBill() {
        Cursor cursor = dbHelper.getBill(billId);
        if (cursor.moveToFirst()) {
            editMonth.setText(cursor.getString(cursor.getColumnIndexOrThrow(DataHelper.COL_MONTH)));
            editUnits.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(DataHelper.COL_UNITS))));
            editRebate.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(DataHelper.COL_REBATE))));
            totalCharges = cursor.getDouble(cursor.getColumnIndexOrThrow(DataHelper.COL_TOTAL));
            finalCost = cursor.getDouble(cursor.getColumnIndexOrThrow(DataHelper.COL_FINAL));
            showAmounts();
        }
        cursor.close();
    }

    private void updateBill() {
        String month = editMonth.getText().toString().trim();
        String unitText = editUnits.getText().toString().trim();
        String rebateText = editRebate.getText().toString().trim();

        if (month.isEmpty() || unitText.isEmpty() || rebateText.isEmpty()) {
            Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        boolean validMonth = false;
        for (int i = 0; i < months.length; i++) {
            if (month.equalsIgnoreCase(months[i])) {
                month = months[i];
                validMonth = true;
                break;
            }
        }
        if (!validMonth) {
            editMonth.setError("Enter a month from January to December");
            return;
        }

        int units;
        int rebate;
        try {
            units = Integer.parseInt(unitText);
            rebate = Integer.parseInt(rebateText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter valid numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        if (units < 1 || units > 1000) {
            editUnits.setError("Enter 1 to 1000 kWh");
            return;
        }
        if (rebate < 0 || rebate > 5) {
            editRebate.setError("Enter 0% to 5%");
            return;
        }

        totalCharges = calculateCharges(units);
        finalCost = totalCharges - (totalCharges * rebate / 100.0);

        int result = dbHelper.updateBill(billId, month, units, totalCharges, rebate, finalCost);
        if (result > 0) {
            showAmounts();
            Toast.makeText(this, "Bill updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Unable to update bill", Toast.LENGTH_SHORT).show();
        }
    }

    private double calculateCharges(int units) {
        double total = 0;
        int remaining = units;

        int block1 = Math.min(remaining, 200);
        total += block1 * 0.218;
        remaining -= block1;

        if (remaining > 0) {
            int block2 = Math.min(remaining, 100);
            total += block2 * 0.334;
            remaining -= block2;
        }
        if (remaining > 0) {
            int block3 = Math.min(remaining, 300);
            total += block3 * 0.516;
            remaining -= block3;
        }
        if (remaining > 0) {
            int block4 = Math.min(remaining, 300);
            total += block4 * 0.546;
        }
        return total;
    }

    private void showAmounts() {
        textTotal.setText(String.format(java.util.Locale.getDefault(),
                "Total Charges: RM %.2f", totalCharges));
        textFinal.setText(String.format(java.util.Locale.getDefault(),
                "Final Cost: RM %.2f", finalCost));
    }
}
