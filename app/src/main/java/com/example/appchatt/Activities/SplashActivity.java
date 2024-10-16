package com.example.appchatt.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appchatt.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Agregar un pequeño retardo para simular la pantalla de carga
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Aquí puedes definir la lógica de inicio de sesión o simplemente redirigir a la actividad principal
                Intent intent = new Intent(SplashActivity.this, SingActivity.class);
                startActivity(intent);
                finish(); // Asegurarse de cerrar el SplashActivity para que no esté en el backstack
            }
        }, 2000); // 2 segundos de retardo
    }
}
