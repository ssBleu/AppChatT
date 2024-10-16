package com.example.appchatt.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appchatt.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class RecentChatsActivity extends AppCompatActivity {

    private ListView recentChatsListView;
    private String username;
    private ArrayList<String> recentChatsList;
    private FirebaseFirestore db;
    private CollectionReference chatsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recent_chats);

        // Obtener el nombre de usuario pasado desde MenuActivity
        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");

        // Inicializar vistas
        recentChatsListView = findViewById(R.id.recentChatsListView);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();
        chatsRef = db.collection("chats");

        // Obtener lista de chats recientes
        getRecentChats();

        // Manejar clic en un chat reciente
        recentChatsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String contactUsername = recentChatsList.get(position);
                Intent intent = new Intent(RecentChatsActivity.this, ChatActivity.class);
                intent.putExtra("USERNAME", username);
                intent.putExtra("CONTACT", contactUsername);
                startActivity(intent);
            }
        });
    }

    private void getRecentChats() {
        recentChatsList = new ArrayList<>();

        // Consulta para obtener chats donde el usuario es el remitente
        chatsRef.whereEqualTo("sender", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String contact = document.getString("recipient");
                            if (contact != null && !recentChatsList.contains(contact)) {
                                recentChatsList.add(contact);
                            }
                        }

                        // Ahora, obtener los chats donde el usuario es el destinatario
                        chatsRef.whereEqualTo("recipient", username)
                                .get()
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task2.getResult()) {
                                            String contact = document.getString("sender");
                                            if (contact != null && !recentChatsList.contains(contact)) {
                                                recentChatsList.add(contact);
                                            }
                                        }

                                        // Configurar adaptador
                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, recentChatsList);
                                        recentChatsListView.setAdapter(adapter);
                                    } else {
                                        Toast.makeText(RecentChatsActivity.this, "Error al obtener chats recientes", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(RecentChatsActivity.this, "Error al obtener chats recientes", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
