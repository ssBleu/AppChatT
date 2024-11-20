package com.tuempresa.linguaconnect.Activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tuempresa.linguaconnect.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    private Spinner spinnerLanguage;
    private Spinner spinnerHobbies;
    private ListView listViewHobbies;
    private Button btnSave, btnAddHobby, btnRemoveHobby;

    private String username;

    // Firebase Firestore
    private FirebaseFirestore db;

    // Lista de idiomas disponibles con sus códigos
    private final String[] languages = {"Español", "Inglés", "Francés", "Alemán", "Italiano", "Portugués", "Ruso", "Chino", "Japonés", "Coreano"};
    private final String[] languageCodes = {"es", "en", "fr", "de", "it", "pt", "ru", "zh", "ja", "ko"};

    // Lista de hobbies disponibles
    private final String[] availableHobbies = {"Lectura", "Deportes", "Música", "Cocina", "Viajes", "Fotografía", "Arte", "Tecnología", "Jardinería", "Cine"};

    private List<String> userHobbies;
    private ArrayAdapter<String> hobbiesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Obtener el nombre de usuario desde el Intent
        username = getIntent().getStringExtra("USERNAME");

        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "Información del usuario no disponible", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Username is null or empty");
            finish();
            return;
        }

        Log.d(TAG, "USERNAME recibido: " + username);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Inicializar vistas
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        spinnerHobbies = findViewById(R.id.spinnerHobbies);
        listViewHobbies = findViewById(R.id.listViewHobbies);
        btnSave = findViewById(R.id.btnSave);
        btnAddHobby = findViewById(R.id.btnAddHobby);
        btnRemoveHobby = findViewById(R.id.btnRemoveHobby);

        // Configurar Spinner para selección de idiomas
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, languages);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(languageAdapter);

        // Configurar Spinner para hobbies
        ArrayAdapter<String> availableHobbiesAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, availableHobbies);
        availableHobbiesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHobbies.setAdapter(availableHobbiesAdapter);

        // Inicializar lista de hobbies
        userHobbies = new ArrayList<>();
        hobbiesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userHobbies);
        listViewHobbies.setAdapter(hobbiesAdapter);

        // Configurar el ListView para permitir la selección de un solo hobby
        listViewHobbies.setChoiceMode(ListView.CHOICE_MODE_SINGLE); // Esto permite seleccionar un solo item

        // Obtener la configuración actual del usuario
        fetchUserConfig();

        // Manejar el clic en el botón de guardar
        btnSave.setOnClickListener(v -> saveUserConfig());

        // Manejar el clic en el botón de agregar hobby
        btnAddHobby.setOnClickListener(v -> addHobby());

        // Manejar el clic en el botón de eliminar hobby
        btnRemoveHobby.setOnClickListener(v -> removeHobby());
    }

    private void fetchUserConfig() {
        Log.d(TAG, "Buscando configuración en Firestore para USERNAME: " + username);

        // Usar una consulta en lugar de buscar directamente por ID
        db.collection("user_config")
                .whereEqualTo("user_id", username) // Buscar donde user_id coincida con el username
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Obtenemos el primer documento que coincida
                        for (DocumentSnapshot documentSnapshot : querySnapshot) {
                            Log.d(TAG, "Documento de configuración encontrado: " + documentSnapshot.getData());

                            String preferredLanguageCode = documentSnapshot.getString("preferred_language_code");
                            List<String> hobbies = (List<String>) documentSnapshot.get("hobbies");

                            if (preferredLanguageCode != null) {
                                int spinnerPosition = getSpinnerPosition(preferredLanguageCode);
                                spinnerLanguage.setSelection(spinnerPosition);
                            }

                            if (hobbies != null) {
                                userHobbies.clear();
                                userHobbies.addAll(hobbies);
                                hobbiesAdapter.notifyDataSetChanged();
                            } else {
                                Log.w(TAG, "La lista de hobbies está vacía o es nula");
                            }
                            break; // Solo necesitamos el primer documento
                        }
                    } else {
                        Toast.makeText(this, "Configuración del usuario no encontrada en Firestore", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "No se encontró configuración para user_id: " + username);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al obtener configuración del usuario", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al obtener configuración de Firestore", e);
                });
    }



    private void saveUserConfig() {
        String selectedLanguageCode = languageCodes[spinnerLanguage.getSelectedItemPosition()];

        // Buscar el documento basado en user_id
        db.collection("user_config")
                .whereEqualTo("user_id", username)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : querySnapshot) {
                            // Obtener el ID real del documento
                            String documentId = documentSnapshot.getId();
                            Log.d(TAG, "ID del documento para actualización: " + documentId);

                            // Actualizar el documento encontrado
                            db.collection("user_config").document(documentId)
                                    .update("preferred_language_code", selectedLanguageCode)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Idioma actualizado correctamente", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "Idioma preferido actualizado para el usuario");
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Error al actualizar el idioma", Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, "Error al actualizar preferred_language_code", e);
                                    });
                            break; // Solo necesitamos actualizar el primer documento encontrado
                        }
                    } else {
                        Toast.makeText(this, "No se encontró configuración para actualizar", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "No se encontró ningún documento para user_id: " + username);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al buscar configuración del usuario", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al buscar documento para actualización", e);
                });
    }


    private void addHobby() {
        String newHobby = spinnerHobbies.getSelectedItem().toString();

        if (userHobbies.contains(newHobby)) {
            Toast.makeText(this, "El hobby ya está agregado", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("user_config")
                .whereEqualTo("user_id", username)  // Usamos `user_id` en lugar de `username`
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Obtener el primer documento del `user_config`
                        DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                        String documentId = documentSnapshot.getId(); // Obtenemos el ID del documento

                        // Ahora actualizamos el campo `hobbies` con el nuevo hobby
                        db.collection("user_config").document(documentId)
                                .update("hobbies", FieldValue.arrayUnion(newHobby))
                                .addOnSuccessListener(aVoid -> {
                                    userHobbies.add(newHobby); // Actualizamos la lista local
                                    hobbiesAdapter.notifyDataSetChanged(); // Actualizamos el ListView
                                    Toast.makeText(this, "Hobby agregado", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error al agregar hobby", e);
                                    Toast.makeText(this, "Error al agregar hobby", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Log.e(TAG, "No se encontró el documento de configuración para el usuario: " + username);
                        Toast.makeText(this, "Error al encontrar configuración del usuario", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener el documento de configuración", e);
                    Toast.makeText(this, "Error al obtener el documento de configuración", Toast.LENGTH_SHORT).show();
                });
    }


    private void removeHobby() {
        int selectedPosition = listViewHobbies.getCheckedItemPosition();

        if (selectedPosition == -1) {
            Toast.makeText(this, "Selecciona un hobby para eliminar", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedHobby = userHobbies.get(selectedPosition);

        db.collection("user_config")
                .whereEqualTo("user_id", username) // Usamos `user_id`
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                        String documentId = documentSnapshot.getId(); // Obtenemos el ID del documento

                        // Ahora eliminamos el hobby de la lista
                        db.collection("user_config").document(documentId)
                                .update("hobbies", FieldValue.arrayRemove(selectedHobby))
                                .addOnSuccessListener(aVoid -> {
                                    userHobbies.remove(selectedHobby); // Eliminamos el hobby localmente
                                    hobbiesAdapter.notifyDataSetChanged(); // Actualizamos el ListView
                                    Toast.makeText(this, "Hobby eliminado", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error al eliminar hobby", e);
                                    Toast.makeText(this, "Error al eliminar hobby", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Log.e(TAG, "No se encontró el documento de configuración para el usuario: " + username);
                        Toast.makeText(this, "Error al encontrar configuración del usuario", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener el documento de configuración", e);
                    Toast.makeText(this, "Error al obtener el documento de configuración", Toast.LENGTH_SHORT).show();
                });
    }


    private int getSpinnerPosition(String languageCode) {
        return Arrays.asList(languageCodes).indexOf(languageCode);
    }
}
