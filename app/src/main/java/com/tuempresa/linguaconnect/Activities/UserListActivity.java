// UserListActivity.java
package com.tuempresa.linguaconnect.Activities;

import com.tuempresa.linguaconnect.Constants; // Importar la clase de constantes

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.tuempresa.linguaconnect.UserAdapter;
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

    // Claves de Firestore
    private static final String COLLECTION_USER_CONFIG = "user_config";
    private static final String FIELD_USER_ID = "user_id";
    private static final String FIELD_PREFERRED_LANGUAGE_CODE = "preferred_language_code";
    private static final String FIELD_HOBBIES = "hobbies";

    // Vistas
    private ListView listViewUsers;

    // Variables de usuario
    private String username;
    private String selectedLanguage;
    private String selectedLanguageCode;
    private String selectedHobby; // Hobby seleccionado

    // Lista y adaptador de usuarios
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

        // Obtener los datos pasados desde la actividad anterior
        retrieveIntentData();

        // Inicializar vistas y componentes
        initializeViews();

        // Inicializar lista de usuarios y adaptador
        setupUserList();

        // Cargar usuarios basados en el hobby seleccionado
        loadUsersBySelectedHobby();

        // Configurar listener para los clics en la lista de usuarios
        setupUserListClickListener();


        // Configurar listener para el botón de regreso
        setupBackButtonListener();
    }

    /**
     * Recupera los datos enviados a través del Intent.
     */
    private void retrieveIntentData() {
        Intent intent = getIntent();
        username = intent.getStringExtra(Constants.EXTRA_USERNAME);
        selectedLanguage = intent.getStringExtra(Constants.EXTRA_LANGUAGE);
        selectedLanguageCode = intent.getStringExtra(Constants.EXTRA_USER_LANGUAGE_CODE);
        selectedHobby = intent.getStringExtra(Constants.EXTRA_SELECTED_HOBBY);

        // Logging para depuración
        Log.d(TAG, "USERNAME: " + username);
        Log.d(TAG, "LANGUAGE: " + selectedLanguage);
        Log.d(TAG, "LANGUAGE_CODE: " + selectedLanguageCode);
        Log.d(TAG, "SELECTED_HOBBY: " + selectedHobby);

        // Validar los datos recibidos
        if (isStringNullOrEmpty(username) || isStringNullOrEmpty(selectedLanguageCode) || isStringNullOrEmpty(selectedHobby)) {
            Toast.makeText(this, "Información del usuario, idioma o hobby no disponible", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "El nombre de usuario, el código de idioma o el hobby es nulo o está vacío");
            finish();
        }
    }

    /**
     * Inicializa las vistas de la interfaz.
     */
    private void initializeViews() {
        listViewUsers = findViewById(R.id.listViewUsers);
    }

    /**
     * Configura la lista de usuarios y su adaptador.
     */
    private void setupUserList() {
        usersList = new ArrayList<>();
        userAdapter = new UserAdapter(this, usersList);
        listViewUsers.setAdapter(userAdapter);
    }

    /**
     * Carga los usuarios desde Firestore que tienen el hobby seleccionado.
     */
    private void loadUsersBySelectedHobby() {
        if (isStringNullOrEmpty(selectedHobby)) {
            Toast.makeText(this, "Hobby seleccionado no disponible", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Hobby seleccionado es nulo o está vacío");
            return;
        }

        Log.d(TAG, "Buscando usuarios con el hobby: " + selectedHobby);

        db.collection(COLLECTION_USER_CONFIG)
                .whereArrayContains(FIELD_HOBBIES, selectedHobby)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Log.d(TAG, "Consulta exitosa en user_config. Número de documentos: " + queryDocumentSnapshots.size());
                        usersList.clear();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String userId = document.getString(FIELD_USER_ID);
                            String langCode = document.getString(FIELD_PREFERRED_LANGUAGE_CODE);
                            List<String> hobbies = (List<String>) document.get(FIELD_HOBBIES);

                            Log.d(TAG, "user_config encontrado: user_id=" + userId + ", langCode=" + langCode + ", hobbies=" + hobbies);

                            // Evitar agregar al mismo usuario que inició sesión
                            if (userId != null && langCode != null && !userId.equals(username)) {
                                usersList.add(new User(userId, langCode, hobbies));
                            }
                        }

                        if (usersList.isEmpty()) {
                            Toast.makeText(UserListActivity.this, "No hay usuarios con el hobby seleccionado", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "No se encontraron usuarios con el hobby seleccionado.");
                        }

                        // Notificar al adaptador que los datos han cambiado
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
    }

    /**
     * Configura el listener para los clics en la lista de usuarios.
     */
    private void setupUserListClickListener() {
        listViewUsers.setOnItemClickListener((adapterView, view, position, l) -> {
            User selectedUser = usersList.get(position);
            navigateToChatActivity(selectedUser);
        });
    }

    /**
     * Navega a ChatActivity pasando la información del usuario seleccionado.
     *
     * @param selectedUser El usuario seleccionado de la lista.
     */
    private void navigateToChatActivity(User selectedUser) {
        Intent chatIntent = new Intent(UserListActivity.this, ChatActivity.class);
        chatIntent.putExtra(Constants.EXTRA_USERNAME, username);
        chatIntent.putExtra(Constants.EXTRA_CONTACT, selectedUser.getUsername());
        chatIntent.putExtra(Constants.EXTRA_USER_LANGUAGE_CODE, selectedLanguageCode); // Idioma preferido para traducción
        chatIntent.putExtra(Constants.EXTRA_CONTACT_LANGUAGE_CODE, selectedUser.getLanguageCode()); // Idioma del contacto
        startActivity(chatIntent);
        Log.d(TAG, "Iniciando chat con: " + selectedUser.getUsername() +
                " (User Lang: " + selectedLanguageCode + ", Contact Lang: " + selectedUser.getLanguageCode() + ")");
    }

    /**
     * botón de regreso.
     */
    private void setupBackButtonListener() {
        ImageButton backButton = findViewById(R.id.btnBack);
        backButton.setOnClickListener(v -> {
            // Cerrar la actividad actual y volver a la anterior
            finish();
        });
    }

    /**
     * Verifica si una cadena es nula o está vacía.
     *
     * @param str La cadena a verificar.
     * @return true si la cadena es nula o está vacía, false de lo contrario.
     */
    private boolean isStringNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
