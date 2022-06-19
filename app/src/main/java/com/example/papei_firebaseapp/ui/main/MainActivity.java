package com.example.papei_firebaseapp.ui.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.example.papei_firebaseapp.R;
import com.example.papei_firebaseapp.data.viewmodels.MainViewModel;
import com.example.papei_firebaseapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    MainViewModel vm ;
    ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        vm = new MainViewModel(MainActivity.this);

        binding.setVm(vm);
    }
}