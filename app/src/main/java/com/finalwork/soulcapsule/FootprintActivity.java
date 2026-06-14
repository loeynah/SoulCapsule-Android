package com.finalwork.soulcapsule;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FootprintActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_footprint);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView rvFootprints = findViewById(R.id.rv_footprints);
        rvFootprints.setLayoutManager(new LinearLayoutManager(this));
        FootprintAdapter adapter = new FootprintAdapter(FootprintAdapter.createMockData());
        adapter.setOnItemClickListener(item ->
                startActivity(MoodDetailActivity.createIntent(this, item)));
        rvFootprints.setAdapter(adapter);

        ImageView fabNewRecord = findViewById(R.id.fab_new_record);
        fabNewRecord.setOnClickListener(v ->
                startActivity(new Intent(this, RecordMoodActivity.class)));

        findViewById(R.id.nav_today).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        findViewById(R.id.nav_mine).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
    }
}
