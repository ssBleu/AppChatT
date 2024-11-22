// SettingsActivity.java
package com.tuempresa.linguaconnect.Activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
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

    // Claves para extras
    public static final String EXTRA_USERNAME = "USERNAME";

    // Claves de Firestore
    private static final String COLLECTION_USER_CONFIG = "user_config";
    private static final String FIELD_USER_ID = "user_id";
    private static final String FIELD_PREFERRED_LANGUAGE_CODE = "preferred_language_code";
    private static final String FIELD_HOBBIES = "hobbies";

    // Vistas
    private Spinner spinnerLanguage, spinnerHobbies;
    private ListView listViewHobbies;
    private Button btnSave, btnAddHobby, btnRemoveHobby;

    private String username;

    // Firebase Firestore
    private FirebaseFirestore db;

    // Listas de idiomas y hobbies disponibles
    private static final String[] LANGUAGES = {
            "Español", "Inglés", "Francés", "Alemán",
            "Italiano", "Portugués", "Ruso", "Chino",
            "Japonés", "Coreano"
    };
    private static final String[] LANGUAGE_CODES = {
            "es", "en", "fr", "de", "it", "pt", "ru", "zh", "ja", "ko"
    };
    private static final String[] AVAILABLE_HOBBIES = {
            "lectura", "deportes", "música", "cocina",
            "viajes", "fotografía", "arte", "tecnología",
            "jardinería", "cine"
    };

    private List<String> userHobbies;
    private ArrayAdapter<String> hobbiesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);



        // Obtener el nombre de usuario desde el Intent
        username = getIntent().getStringExtra(EXTRA_USERNAME);

        if (isStringNullOrEmpty(username)) {
            showToast("Información del usuario no disponible");
            Log.e(TAG, "Username is null or empty");
            finish();
            return;
        }

        Log.d(TAG, "USERNAME recibido: " + username);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Inicializar vistas y componentes
        initializeViews();

        // Configurar Spinners
        setupSpinners();

        // Inicializar lista de hobbies
        initializeHobbiesList();

        // Obtener la configuración actual del usuario
        fetchUserConfig();

        // Configurar listeners para los botones
        setupButtonListeners();

        // Configurar el botón de regreso
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            // Finaliza la actividad actual y regresa a la anterior
            onBackPressed();
        });
    }

    /**
     * Inicializa las vistas y componentes de la interfaz.
     */
    private void initializeViews() {
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        spinnerHobbies = findViewById(R.id.spinnerHobbies);
        listViewHobbies = findViewById(R.id.listViewHobbies);
        btnSave = findViewById(R.id.btnSave);
        btnAddHobby = findViewById(R.id.btnAddHobby);
        btnRemoveHobby = findViewById(R.id.btnRemoveHobby);
    }

    /**
     * Configura los adaptadores para los Spinners de idiomas y hobbies disponibles.
     */
    private void setupSpinners() {
        // Configurar el Spinner de idiomas
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, LANGUAGES);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(languageAdapter);

        // Configurar el Spinner de hobbies disponibles
        ArrayAdapter<String> availableHobbiesAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, AVAILABLE_HOBBIES);
        availableHobbiesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHobbies.setAdapter(availableHobbiesAdapter);
    }

    /**
     * Inicializa la lista de hobbies del usuario y su adaptador.
     */
    private void initializeHobbiesList() {
        userHobbies = new ArrayList<>();
        hobbiesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userHobbies);
        listViewHobbies.setAdapter(hobbiesAdapter);

        // Permitir la selección de un solo hobby
        listViewHobbies.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    /**
     * Configura los listeners para los botones de la interfaz.
     */
    private void setupButtonListeners() {
        // Manejar clic en el botón de guardar
        btnSave.setOnClickListener(v -> saveUserConfig());

        // Manejar clic en el botón de agregar hobby
        btnAddHobby.setOnClickListener(v -> addHobby());

        // Manejar clic en el botón de eliminar hobby
        btnRemoveHobby.setOnClickListener(v -> removeHobby());
    }

    /**
     * Obtiene la configuración actual del usuario desde Firestore.
     */
    private void fetchUserConfig() {
        Log.d(TAG, "Buscando configuración en Firestore para USERNAME: " + username);

        db.collection(COLLECTION_USER_CONFIG)
                .whereEqualTo(FIELD_USER_ID, username)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Obtenemos el primer documento que coincida
                        DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                        Log.d(TAG, "Documento de configuración encontrado: " + documentSnapshot.getData());

                        String preferredLanguageCode = documentSnapshot.getString(FIELD_PREFERRED_LANGUAGE_CODE);
                        List<String> hobbies = (List<String>) documentSnapshot.get(FIELD_HOBBIES);

                        if (preferredLanguageCode != null) {
                            int spinnerPosition = getSpinnerPosition(preferredLanguageCode);
                            if (spinnerPosition >= 0) {
                                spinnerLanguage.setSelection(spinnerPosition);
                            } else {
                                Log.w(TAG, "Código de idioma no encontrado en la lista de idiomas");
                            }
                        }

                        if (hobbies != null) {
                            userHobbies.clear();
                            userHobbies.addAll(hobbies);
                            hobbiesAdapter.notifyDataSetChanged();
                        } else {
                            Log.w(TAG, "La lista de hobbies está vacía o es nula");
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

    /**
     * Guarda la configuración actual del usuario en Firestore.
     */
    private void saveUserConfig() {
        String selectedLanguageCode = LANGUAGE_CODES[spinnerLanguage.getSelectedItemPosition()];

        db.collection(COLLECTION_USER_CONFIG)
                .whereEqualTo(FIELD_USER_ID, username)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                        String documentId = documentSnapshot.getId();
                        Log.d(TAG, "ID del documento para actualización: " + documentId);

                        // Actualizar el código de idioma preferido
                        db.collection(COLLECTION_USER_CONFIG).document(documentId)
                                .update(FIELD_PREFERRED_LANGUAGE_CODE, selectedLanguageCode)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Idioma actualizado correctamente", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "Idioma preferido actualizado para el usuario");
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error al actualizar el idioma", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Error al actualizar preferred_language_code", e);
                                });

                        // Opcional: actualizar la lista de hobbies si se han realizado cambios
                        // Este ejemplo asume que la lista de hobbies ya está actualizada en Firestore
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

    /**
     * Agrega un hobby seleccionado a la configuración del usuario en Firestore.
     */
    private void addHobby() {
        String newHobby = spinnerHobbies.getSelectedItem().toString();

        if (userHobbies.contains(newHobby)) {
            Toast.makeText(this, "El hobby ya está agregado", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection(COLLECTION_USER_CONFIG)
                .whereEqualTo(FIELD_USER_ID, username)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                        String documentId = documentSnapshot.getId();

                        // Agregar el hobby a la lista en Firestore
                        db.collection(COLLECTION_USER_CONFIG).document(documentId)
                                .update(FIELD_HOBBIES, FieldValue.arrayUnion(newHobby))
                                .addOnSuccessListener(aVoid -> {
                                    userHobbies.add(newHobby);
                                    hobbiesAdapter.notifyDataSetChanged();
                                    Toast.makeText(this, "Hobby agregado", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "Hobby agregado: " + newHobby);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error al agregar hobby", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Error al agregar hobby", e);
                                });
                    } else {
                        Toast.makeText(this, "Error al encontrar configuración del usuario", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "No se encontró el documento de configuración para el usuario: " + username);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al obtener el documento de configuración", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al obtener el documento de configuración", e);
                });
    }

    /**
     * Elimina un hobby seleccionado de la configuración del usuario en Firestore.
     */
    private void removeHobby() {
        int selectedPosition = listViewHobbies.getCheckedItemPosition();

        if (selectedPosition == -1) {
            Toast.makeText(this, "Selecciona un hobby para eliminar", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedHobby = userHobbies.get(selectedPosition);

        db.collection(COLLECTION_USER_CONFIG)
                .whereEqualTo(FIELD_USER_ID, username)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                        String documentId = documentSnapshot.getId();

                        // Eliminar el hobby de la lista en Firestore
                        db.collection(COLLECTION_USER_CONFIG).document(documentId)
                                .update(FIELD_HOBBIES, FieldValue.arrayRemove(selectedHobby))
                                .addOnSuccessListener(aVoid -> {
                                    userHobbies.remove(selectedHobby);
                                    hobbiesAdapter.notifyDataSetChanged();
                                    Toast.makeText(this, "Hobby eliminado", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "Hobby eliminado: " + selectedHobby);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error al eliminar hobby", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Error al eliminar hobby", e);
                                });
                    } else {
                        Toast.makeText(this, "Error al encontrar configuración del usuario", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "No se encontró el documento de configuración para el usuario: " + username);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al obtener el documento de configuración", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al obtener el documento de configuración", e);
                });
    }

    /**
     * Obtiene la posición del Spinner correspondiente al código de idioma proporcionado.
     *
     * @param languageCode El código de idioma.
     * @return La posición en el Spinner, o -1 si no se encuentra.
     */
    private int getSpinnerPosition(String languageCode) {
        return Arrays.asList(LANGUAGE_CODES).indexOf(languageCode);
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

    /**
     * Muestra un Toast con el mensaje proporcionado.
     *
     * @param message Mensaje a mostrar.
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
