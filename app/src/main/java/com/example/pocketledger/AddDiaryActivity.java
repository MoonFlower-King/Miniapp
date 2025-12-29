package com.example.pocketledger;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddDiaryActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private TextView tvDate;
    private DatabaseHelper dbHelper;

    private String selectedMood = "neutral";
    private String selectedDate;
    private int editDiaryId = -1; // -1 means new entry

    private View[] moodViews;
    private final String[] moods = { "happy", "excited", "neutral", "sad", "love" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_diary);

        dbHelper = DatabaseHelper.getInstance(this);

        initViews();
        setupMoodSelector();
        setupDatePicker();
        loadEditData();
    }

    private void initViews() {
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        tvDate = findViewById(R.id.tvDate);

        // Set default date to today
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new java.util.Date());
        tvDate.setText(selectedDate);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> saveDiary());

        moodViews = new View[] {
                findViewById(R.id.moodHappy),
                findViewById(R.id.moodExcited),
                findViewById(R.id.moodNeutral),
                findViewById(R.id.moodSad),
                findViewById(R.id.moodLove)
        };
    }

    private void setupMoodSelector() {
        for (int i = 0; i < moodViews.length; i++) {
            final int index = i;
            moodViews[i].setOnClickListener(v -> selectMood(index));
        }
        // Default to neutral
        selectMood(2);
    }

    private void selectMood(int index) {
        selectedMood = moods[index];

        // Update visual feedback
        for (int i = 0; i < moodViews.length; i++) {
            if (i == index) {
                moodViews[i].setAlpha(1.0f);
                moodViews[i].setScaleX(1.2f);
                moodViews[i].setScaleY(1.2f);
            } else {
                moodViews[i].setAlpha(0.5f);
                moodViews[i].setScaleX(1.0f);
                moodViews[i].setScaleY(1.0f);
            }
        }
    }

    private void setupDatePicker() {
        findViewById(R.id.layoutDate).setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            // Parse current selected date
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
                calendar.setTime(sdf.parse(selectedDate));
            } catch (Exception e) {
                e.printStackTrace();
            }

            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedDate = String.format(Locale.CHINA, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                tvDate.setText(selectedDate);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void loadEditData() {
        // Check if editing existing entry
        editDiaryId = getIntent().getIntExtra("diary_id", -1);

        if (editDiaryId != -1) {
            String title = getIntent().getStringExtra("diary_title");
            String content = getIntent().getStringExtra("diary_content");
            String mood = getIntent().getStringExtra("diary_mood");
            String date = getIntent().getStringExtra("diary_date");

            etTitle.setText(title);
            etContent.setText(content);

            if (date != null) {
                selectedDate = date;
                tvDate.setText(date);
            }

            if (mood != null) {
                for (int i = 0; i < moods.length; i++) {
                    if (moods[i].equals(mood)) {
                        selectMood(i);
                        break;
                    }
                }
            }

            // Update title to "Edit diary"
            ((TextView) findViewById(R.id.btnSave).getRootView()
                    .findViewById(android.R.id.content))
                    .getRootView().findViewWithTag("header_title");
        }
    }

    private void saveDiary() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (content.isEmpty()) {
            Toast.makeText(this, "请输入日记内容", Toast.LENGTH_SHORT).show();
            return;
        }

        DiaryEntry entry = new DiaryEntry(title, content, selectedMood, selectedDate);

        boolean success;
        if (editDiaryId != -1) {
            // Update existing
            entry.setId(editDiaryId);
            success = dbHelper.updateDiaryEntry(entry);
        } else {
            // Create new
            success = dbHelper.addDiaryEntry(entry);
        }

        if (success) {
            Toast.makeText(this, editDiaryId != -1 ? "日记已更新" : "日记已保存", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "保存失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }
}
