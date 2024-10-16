package com.example.appchatt.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appchatt.R;

public class MenuActivity extends AppCompatActivity {

    private Button btnNewChat, btnContacts, btnRecentChats;
    private String username; // Nombre de usuario del usuario actual

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);

        // Obtener el nombre de usuario pasado desde SignActivity
        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");

        // Inicializar vistas
        btnNewChat = findViewById(R.id.button);
        btnContacts = findViewById(R.id.button2);
        btnRecentChats = findViewById(R.id.button3);

        // Manejar botón de "Iniciar nuevo chat" /////////////////ACÁ FALTA ADAPTAR
        btnNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuActivity.this, ContactsActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            }
        });

        // Manejar botón de "Contactos" /////////////////ACÁ FALTA ADAPTAR
        btnContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuActivity.this, ContactsActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            }
        });

        // Manejar botón de "Chats Recientes" /////////////////ACÁ FALTA ADAPTAR
        btnRecentChats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuActivity.this, RecentChatsActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            }
        });
    }
}
