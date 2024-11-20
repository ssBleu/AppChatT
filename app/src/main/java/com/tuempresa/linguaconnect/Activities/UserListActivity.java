// UserListActivity.java
package com.tuempresa.linguaconnect.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.tuempresa.linguaconnect.Adapters.UserAdapter;
import com.tuempresa.linguaconnect.Models.User;
import com.tuempresa.linguaconnect.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    private static final String TAG = "UserListActivity";

    private String username;
    private String selectedLanguage;
    private String selectedLanguageCode;
    private String selectedHobby; // Nuevo campo para el hobby seleccionado

    private ListView listViewUsers;
    private ArrayList<User> usersList;
    private UserAdapter userAdapter;

    // Firebase Firestore
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Obtener los datos pasados desde HobbieSelectionActivity
        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");
        selectedLanguage = intent.getStringExtra("LANGUAGE");
        selectedLanguageCode = intent.getStringExtra("LANGUAGE_CODE"); // Código de idioma para traducción
        selectedHobby = intent.getStringExtra("SELECTED_HOBBY"); // Hobby seleccionado

        // Logging para depuración
        Log.d(TAG, "USERNAME: " + username);
        Log.d(TAG, "LANGUAGE: " + selectedLanguage);
        Log.d(TAG, "LANGUAGE_CODE: " + selectedLanguageCode);
        Log.d(TAG, "SELECTED_HOBBY: " + selectedHobby);

        if (username == null || username.isEmpty() || selectedLanguageCode == null || selectedLanguageCode.isEmpty() || selectedHobby == null || selectedHobby.isEmpty()) {
            Toast.makeText(this, "Información del usuario, idioma o hobby no disponible", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "El nombre de usuario, el código de idioma o el hobby es nulo o está vacío");
            finish();
            return;
        }

        // Inicializar vistas
        listViewUsers = findViewById(R.id.listViewUsers);
        usersList = new ArrayList<>();
        userAdapter = new UserAdapter(this, usersList); // Uso del adaptador personalizado
        listViewUsers.setAdapter(userAdapter);

        // Cargar usuarios basados en el hobby seleccionado
        loadUsersBySelectedHobby();
    }

    private void loadUsersBySelectedHobby() {
        if (selectedHobby == null || selectedHobby.isEmpty()) {
            Toast.makeText(this, "Hobby seleccionado no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        // Usar 'whereArrayContains' en 'user_config' para buscar usuarios que tengan el hobby seleccionado
        db.collection("user_config")
                .whereArrayContains("hobbies", selectedHobby)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Log.d(TAG, "Consulta exitosa en user_config. Número de documentos: " + queryDocumentSnapshots.size());
                        usersList.clear();

                        // Lista para almacenar los IDs de usuarios encontrados
                        List<String> userIds = new ArrayList<>();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String userId = document.getString("user_id");
                            String langCode = document.getString("preferred_language_code");
                            List<String> hobbies = (List<String>) document.get("hobbies");

                            Log.d(TAG, "user_config encontrado: user_id=" + userId + ", langCode=" + langCode + ", hobbies=" + hobbies);

                            if (userId != null && langCode != null && !userId.equals(username)) {
                                userIds.add(userId);
                                // Crear un objeto User con la información disponible en user_config
                                usersList.add(new User(userId, langCode, hobbies));
                            }
                        }

                        if (usersList.isEmpty()) {
                            Toast.makeText(UserListActivity.this, "No hay usuarios con el hobby seleccionado", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "No se encontraron usuarios con el hobby seleccionado.");
                        }

                        // Actualizar el adaptador
                        userAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserListActivity.this, "Error al cargar los usuarios", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error al cargar los usuarios desde user_config", e);
                    }
                });

        // Manejar clic en un usuario después de cargar la lista
        listViewUsers.setOnItemClickListener((adapterView, view, position, l) -> {
            User selectedUser = usersList.get(position);
            // Iniciar ChatActivity
            Intent chatIntent = new Intent(UserListActivity.this, ChatActivity.class);
            chatIntent.putExtra("USERNAME", username);
            chatIntent.putExtra("CONTACT", selectedUser.getUsername());
            chatIntent.putExtra("USER_LANGUAGE_CODE", selectedLanguageCode); // Idioma preferido para traducción
            chatIntent.putExtra("CONTACT_LANGUAGE_CODE", selectedUser.getLanguageCode()); // Idioma del contacto
            startActivity(chatIntent);
        });
    }
}
