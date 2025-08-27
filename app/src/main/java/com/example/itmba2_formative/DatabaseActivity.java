package com.example.itmba2_formative;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseActivity extends AppCompatActivity {

    private TextView tvDatabaseContent;
    private DatabaseHelper dbHelper;
    private ExecutorService executor;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);

        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        Toolbar toolbar = findViewById(R.id.toolbar_database);
        setSupportActionBar(toolbar);

        TextView btnBack = findViewById(R.id.btn_back_database);
        btnBack.setOnClickListener(v -> finish());

        tvDatabaseContent = findViewById(R.id.tv_database_content);
        dbHelper = DatabaseHelper.getInstance(this);

        loadDatabaseContent();
    }

    private void loadDatabaseContent() {
        tvDatabaseContent.setText(getString(R.string.loading_database));

        // Run database operations on background thread
        executor.execute(() -> {
            String dbDump = dbHelper.getDebugDatabaseDump();

            // Update UI on main thread
            mainHandler.post(() -> {
                if (dbDump != null && !dbDump.isEmpty()) {
                    tvDatabaseContent.setText(dbDump);
                } else {
                    tvDatabaseContent.setText(getString(R.string.no_database_data));
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}