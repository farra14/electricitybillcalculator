package com.example.electricitybillcalculator;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    ListView listViewBills;
    DataHelper dbHelper;
    ArrayList<String> billList;
    ArrayList<Integer> billIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        setTitle("Bill History");

        listViewBills = findViewById(R.id.listViewBills);
        dbHelper = new DataHelper(this);

        listViewBills.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < billIds.size()) {
                    Intent intent = new Intent(HistoryActivity.this, DetailActivity.class);
                    intent.putExtra("bill_id", billIds.get(position));
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    public void refreshList() {
        billList = new ArrayList<>();
        billIds = new ArrayList<>();

        Cursor cursor = dbHelper.getAllBills();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DataHelper.COL_ID));
                String month = cursor.getString(cursor.getColumnIndexOrThrow(DataHelper.COL_MONTH));
                double finalCost = cursor.getDouble(cursor.getColumnIndexOrThrow(DataHelper.COL_FINAL));

                billIds.add(id);
                billList.add(month + "                 RM " + String.format(java.util.Locale.getDefault(), "%.2f", finalCost));
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (billList.isEmpty()) {
            billList.add("No saved bills yet");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, billList);
        listViewBills.setAdapter(adapter);
    }
}
