package com.example.appchatt.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appchatt.ChatAdapter;
import com.example.appchatt.Message;
import com.example.appchatt.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private RecyclerView chatRecyclerView;
    private EditText etMessage;
    private Button btnSend;

    private String username;      // Nombre de usuario actual
    private String contactName;   // Nombre del contacto

    private ArrayList<Message> chatMessages;
    private ChatAdapter chatAdapter;

    private FirebaseFirestore db;
    private CollectionReference chatsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        // Obtener datos pasados desde ContactsActivity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString("USERNAME");
            contactName = extras.getString("CONTACT");
        } else {
            Toast.makeText(this, "Error al obtener información del chat", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Inicializar vistas
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        // Inicializar lista de mensajes
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, chatMessages, username);
        chatRecyclerView.setAdapter(chatAdapter);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();
        chatsRef = db.collection("chats");

        // Escuchar mensajes en Firestore
        listenForMessages();

        // Manejar botón de envío
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mensaje = etMessage.getText().toString().trim();
                if (!mensaje.isEmpty()) {
                    sendMessage(mensaje);
                    etMessage.setText("");
                }
            }
        });
    }

    private void listenForMessages() {
        // Escuchar mensajes enviados por el usuario actual al contacto
        chatsRef
                .whereEqualTo("sender", username)
                .whereEqualTo("recipient", contactName)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    Message message = dc.getDocument().toObject(Message.class);
                                    chatMessages.add(message);
                                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                                    chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                                    break;
                                case MODIFIED:
                                    // Manejar si es necesario
                                    break;
                                case REMOVED:
                                    // Manejar si es necesario
                                    break;
                            }
                        }
                    }
                });

        // Escuchar mensajes recibidos del contacto al usuario actual
        chatsRef
                .whereEqualTo("sender", contactName)
                .whereEqualTo("recipient", username)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    Message message = dc.getDocument().toObject(Message.class);
                                    chatMessages.add(message);
                                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                                    chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                                    break;
                                case MODIFIED:
                                    // Manejar si es necesario
                                    break;
                                case REMOVED:
                                    // Manejar si es necesario
                                    break;
                            }
                        }
                    }
                });
    }

    private void sendMessage(String mensaje) {
        Message message = new Message(username, contactName, mensaje, Timestamp.now());

        chatsRef.add(message)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Mensaje enviado con ID: " + documentReference.getId());
                    // El mensaje se agregará automáticamente mediante el listener
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al enviar el mensaje", e);
                    Toast.makeText(ChatActivity.this, "Error al enviar el mensaje", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // No es necesario cerrar nada ya que Firestore maneja las conexiones
    }
}
