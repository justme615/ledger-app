package com.example.personal;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvBalance, tvIncome, tvExpense;
    private Spinner spType, spCategory;
    private EditText etAmount, etDate, etNote;
    private Button btnAdd;
    private ListView lvRecords;

    private DatabaseHelper dbHelper;
    private ArrayList<LedgerRecord> records;
    private ArrayAdapter<LedgerRecord> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        initViews();
        initSpinners();
        initDate();
        initList();
        refreshData();

        btnAdd.setOnClickListener(v -> addRecord());

        lvRecords.setOnItemLongClickListener((parent, view, position, id) -> {
            LedgerRecord record = records.get(position);

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("删除账单")
                    .setMessage("确定删除这条账单吗？")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("删除", (dialog, which) -> {
                        int result = dbHelper.deleteRecord(record.id);

                        if (result > 0) {
                            Toast.makeText(
                                    MainActivity.this,
                                    "删除成功",
                                    Toast.LENGTH_SHORT
                            ).show();
                            refreshData();
                        } else {
                            Toast.makeText(
                                    MainActivity.this,
                                    "删除失败",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    })
                    .show();

            return true;
        });
    }

    private void initViews() {
        tvBalance = findViewById(R.id.tvBalance);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);

        spType = findViewById(R.id.spType);
        spCategory = findViewById(R.id.spCategory);

        etAmount = findViewById(R.id.etAmount);
        etDate = findViewById(R.id.etDate);
        etNote = findViewById(R.id.etNote);

        btnAdd = findViewById(R.id.btnAdd);
        lvRecords = findViewById(R.id.lvRecords);
    }

    private void initSpinners() {
        String[] types = {"支出", "收入"};
        String[] categories = {
                "餐饮", "交通", "购物", "学习",
                "娱乐", "工资", "兼职", "其他"
        };

        ArrayAdapter<String> typeAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        types
                );

        typeAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spType.setAdapter(typeAdapter);

        ArrayAdapter<String> categoryAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        categories
                );

        categoryAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spCategory.setAdapter(categoryAdapter);
    }

    private void initDate() {
        String today = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());

        etDate.setText(today);
    }

    private void initList() {
        records = new ArrayList<>();

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                records
        );

        lvRecords.setAdapter(adapter);
    }

    private void addRecord() {
        String amountText = etAmount.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        if (amountText.isEmpty()) {
            etAmount.setError("请输入金额");
            etAmount.requestFocus();
            return;
        }

        double amount;

        try {
            amount = Double.parseDouble(amountText);

            if (!Double.isFinite(amount) || amount <= 0) {
                etAmount.setError("金额必须大于0");
                return;
            }
        } catch (NumberFormatException e) {
            etAmount.setError("请输入有效金额");
            return;
        }

        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            etDate.setError("日期格式应为 yyyy-MM-dd");
            return;
        }

        String type = spType.getSelectedItem().toString();
        String category = spCategory.getSelectedItem().toString();

        long result = dbHelper.insertRecord(
                type, amount, category, date, note
        );

        if (result != -1) {
            Toast.makeText(this, "记账成功", Toast.LENGTH_SHORT).show();

            etAmount.setText("");
            etNote.setText("");

            initDate();
            refreshData();
        } else {
            Toast.makeText(this, "保存失败，请检查数据库", Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshData() {
        records.clear();
        records.addAll(dbHelper.getAllRecords());
        adapter.notifyDataSetChanged();

        double income = dbHelper.getTotal("收入");
        double expense = dbHelper.getTotal("支出");
        double balance = income - expense;

        tvIncome.setText(String.format(
                Locale.getDefault(), "总收入：%.2f 元", income
        ));

        tvExpense.setText(String.format(
                Locale.getDefault(), "总支出：%.2f 元", expense
        ));

        tvBalance.setText(String.format(
                Locale.getDefault(), "当前结余：%.2f 元", balance
        ));
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}
