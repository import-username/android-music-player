package com.importusername.musicplayer.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.importusername.musicplayer.R;

public class MusicPlayerActivity extends AppCompatActivity {
    private final FragmentManager fragmentManager = this.getSupportFragmentManager();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_music_player_menu);

        BottomNavigationView navigationView = findViewById(R.id.music_player_navbar);

//        navigationView.setOnItemSelectedListener((item) -> {
//            switch(item.getTitle().toString()) {
//                case "Home":
//                    System.out.println("abc");
//                    break;
//            }
//        });
    }
}
