package com.example.itmba2_formative;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            findViewById(R.id.btn_get_started).setOnClickListener(button -> {
                Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                startActivity(intent);
            });

            findViewById(R.id.tv_login_prompt).setOnClickListener(textView -> {
                Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                startActivity(intent);
            });

            return insets;
        });
    }
}