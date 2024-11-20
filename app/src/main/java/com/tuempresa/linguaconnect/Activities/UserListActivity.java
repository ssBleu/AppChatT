// UserListActivity.java
package com.tuempresa.linguaconnect.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tuempresa.linguaconnect.Adapters.UserAdapter; // Import correcto
import com.tuempresa.linguaconnect.Models.User;
import com.tuempresa.linguaconnect.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class UserListActivity extends AppCompatActivity {

    private static final String TAG = "UserListActivity";

    private String username;
    private String selectedLanguage;
    private String selectedLanguageCode;
    private String userLanguageCode;

    private ListView listViewUsers;
    private ArrayList<User> usersList;
    private UserAdapter userAdapter;

    // Firebase Firestore
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        // Obtener los datos pasados desde LanguageSelectionActivity
        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");
        selectedLanguage = intent.getStringExtra("LANGUAGE");
        selectedLanguageCode = intent.getStringExtra("LANGUAGE_CODE"); // Código de idioma para filtrar

        if (username == null || username.isEmpty() || selectedLanguageCode == null || selectedLanguageCode.isEmpty()) {
            Toast.makeText(this, "Información del usuario o idioma no disponible", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "El nombre de usuario o el código de idioma es nulo o está vacío");
            finish();
            return;
        }

        // Inicializar vistas
        listViewUsers = findViewById(R.id.listViewUsers);
        usersList = new ArrayList<>();
        userAdapter = new UserAdapter(this, usersList); // Uso del adaptador personalizado
        listViewUsers.setAdapter(userAdapter);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Obtener el idioma preferido del usuario actual
        fetchCurrentUserLanguageCode();
    }

    private void fetchCurrentUserLanguageCode() {
        db.collection("users").document(username)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        userLanguageCode = documentSnapshot.getString("preferred_language_code");
                        if (userLanguageCode == null || userLanguageCode.isEmpty()) {
                            Toast.makeText(this, "El código de idioma del usuario actual no está disponible", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "userLanguageCode es nulo o está vacío");
                            finish();
                            return;
                        }

                        // Ahora cargar los usuarios filtrados por el idioma seleccionado
                        loadUsersBySelectedLanguage();
                    } else {
                        Toast.makeText(this, "Usuario actual no encontrado", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Documento del usuario actual no existe");
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al obtener el idioma del usuario actual", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al obtener el idioma del usuario actual", e);
                    finish();
                });
    }

    private void loadUsersBySelectedLanguage() {
        db.collection("users")
                .whereEqualTo("preferred_language_code", selectedLanguageCode)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    usersList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String user = document.getString("username");
                        String langCode = document.getString("preferred_language_code");
                        if (user != null && langCode != null && !user.equals(username)) {
                            usersList.add(new User(user, langCode));
                        }
                    }
                    if (usersList.isEmpty()) {
                        Toast.makeText(this, "No hay usuarios disponibles para el idioma seleccionado", Toast.LENGTH_SHORT).show();
                    }
                    // Actualizar el adaptador
                    userAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar los usuarios", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al cargar los usuarios", e);
                });

        // Manejar clic en un usuario después de cargar la lista
        listViewUsers.setOnItemClickListener((adapterView, view, position, l) -> {
            User selectedUser = usersList.get(position);
            // Iniciar ChatActivity
            Intent chatIntent = new Intent(UserListActivity.this, ChatActivity.class);
            chatIntent.putExtra("USERNAME", username);
            chatIntent.putExtra("CONTACT", selectedUser.getUsername());
            chatIntent.putExtra("USER_LANGUAGE_CODE", userLanguageCode); // Idioma del usuario actual
            chatIntent.putExtra("CONTACT_LANGUAGE_CODE", selectedUser.getLanguageCode()); // Idioma del contacto
            startActivity(chatIntent);
        });
    }
}
