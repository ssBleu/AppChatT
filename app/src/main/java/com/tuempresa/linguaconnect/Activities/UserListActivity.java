
package com.tuempresa.linguaconnect.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tuempresa.linguaconnect.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserListActivity extends AppCompatActivity {

    private static final String TAG = "UsersListActivity";

    private TextView tvSelectedLanguage;
    private ListView listViewUsers;

    private String username; // Usuario actual
    private String selectedLanguage; // Idioma seleccionado

    private FirebaseFirestore db;

    private List<String> usersList; // Lista de nombres de usuario

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        // Obtener datos pasados desde LanguageSelectionActivity
        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");
        selectedLanguage = intent.getStringExtra("LANGUAGE");

        if (username == null || username.isEmpty() || selectedLanguage == null || selectedLanguage.isEmpty()) {
            Toast.makeText(this, "Información incompleta", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Username o Language está vacío");
            finish();
            return;
        }

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Inicializar vistas
        tvSelectedLanguage = findViewById(R.id.tvSelectedLanguage);
        listViewUsers = findViewById(R.id.listViewUsers);

        tvSelectedLanguage.setText("Usuarios que hablan " + selectedLanguage + ":");

        usersList = new ArrayList<>();

        // Configurar el adaptador para el ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, usersList);
        listViewUsers.setAdapter(adapter);

        // Consultar Firestore para obtener usuarios que hablan el idioma seleccionado
        db.collection("user_config")
                .whereEqualTo("preferred_language_code", getLanguageCode(selectedLanguage))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            Map<String, Object> userConfig = document.getData();
                            if (userConfig != null) {
                                String userId = (String) userConfig.get("user_id");
                                if (userId != null && !userId.equals(username)) { // Excluir al usuario actual
                                    // Obtener detalles del usuario desde la colección "users"
                                    db.collection("users").document(userId).get()
                                            .addOnSuccessListener(userDoc -> {
                                                if (userDoc.exists()) {
                                                    String email = userDoc.getString("email");
                                                    String displayName = userDoc.getString("username");
                                                    if (displayName != null && email != null) {
                                                        usersList.add(displayName + " (" + email + ")");
                                                        adapter.notifyDataSetChanged();
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.w(TAG, "Error al obtener datos del usuario: " + userId, e);
                                            });
                                }
                            }
                        }
                    } else {
                        Toast.makeText(UserListActivity.this, "No se encontraron usuarios para el idioma seleccionado.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al consultar Firestore", e);
                    Toast.makeText(UserListActivity.this, "Error al cargar usuarios.", Toast.LENGTH_SHORT).show();
                });

// Manejar clic en un usuario para iniciar chat
        listViewUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String selectedUserEntry = usersList.get(position);
                // Extraer el nombre de usuario (suponiendo que el formato es "username (email)")
                String contactUsername = selectedUserEntry.split(" \\(")[0];
                Log.d(TAG, "Usuario seleccionado para chatear: " + contactUsername);

                // Iniciar ChatActivity pasando el usuario actual y el contacto
                Intent chatIntent = new Intent(UserListActivity.this, ChatActivity.class);
                chatIntent.putExtra("USERNAME", username);
                chatIntent.putExtra("CONTACT", contactUsername);
                startActivity(chatIntent);
                finish();
            }
        });
    }

    /**
     * Mapea el nombre del lenguaje a su código correspondiente.
     *
     * @param language Nombre del lenguaje seleccionado.
     * @return Código del lenguaje.
     */
    private String getLanguageCode(String language) {
        switch (language) {
            case "Español":
                return "es";
            case "Inglés":
                return "en";
            case "Francés":
                return "fr";
            case "Alemán":
                return "de";
            case "Italiano":
                return "it";
            case "Portugués":
                return "pt";
            // Añade más casos según los idiomas soportados
            default:
                return "es";
        }
    }
}
