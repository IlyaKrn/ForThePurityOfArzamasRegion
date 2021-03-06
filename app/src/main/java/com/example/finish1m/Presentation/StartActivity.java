package com.example.finish1m.Presentation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.finish1m.Data.Firebase.EventRepositoryImpl;
import com.example.finish1m.Data.Other.NotificationRepositoryImpl;
import com.example.finish1m.Domain.UseCases.StartListeningEventsUseCase;
import com.example.finish1m.R;
import com.example.finish1m.databinding.ActivityStartBinding;

public class StartActivity extends AppCompatActivity {

    private ActivityStartBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // вход
        binding.btEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartActivity.this, EnterActivity.class);
                startActivity(intent);
            }
        });

        // регистрация
        binding.btRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}