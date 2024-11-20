// SettingsActivity.java

package com.tuempresa.linguaconnect.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.tuempresa.linguaconnect.R;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    private Spinner spinnerLanguage;
    private Button btnSave;
    private String username;

    // Lista de idiomas disponibles con sus códigos
    private String[] languages = {"Español", "Inglés", "Francés", "Alemán", "Italiano", "Portugués", "Ruso", "Chino", "Japonés", "Coreano"};
    private String[] languageCodes = {"es", "en", "fr", "de", "it", "pt", "ru", "zh", "ja", "ko"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Obtener el nombre de usuario pasado desde MenuActivity
        username = getIntent().getStringExtra("USERNAME");

        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "Información del usuario no disponible", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Username is null or empty");
            finish();
            return;
        }

        // Inicializar vistas
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        btnSave = findViewById(R.id.btnSave);

        // Configurar el Spinner con opciones de idiomas
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);

        // Obtener la configuración actual del usuario
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(username).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String preferredLanguageCode = documentSnapshot.getString("preferred_language_code");
                        if (preferredLanguageCode != null && !preferredLanguageCode.isEmpty()) {
                            // Encontrar la posición en el Spinner que corresponde al código de idioma
                            int spinnerPosition = getSpinnerPosition(preferredLanguageCode);
                            if (spinnerPosition >= 0) {
                                spinnerLanguage.setSelection(spinnerPosition);
                            } else {
                                Log.w(TAG, "Código de idioma no encontrado en el Spinner: " + preferredLanguageCode);
                            }
                        }
                    } else {
                        Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Documento del usuario actual no existe");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al obtener la configuración", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error getting user document", e);
                });

        // Manejar clic en el botón de guardar
        btnSave.setOnClickListener(view -> {
            int selectedPosition = spinnerLanguage.getSelectedItemPosition();
            String selectedLanguage = languages[selectedPosition];
            String selectedLanguageCode = languageCodes[selectedPosition];
            Log.d(TAG, "Idioma seleccionado: " + selectedLanguage + " (" + selectedLanguageCode + ")");

            // Actualizar la configuración en Firestore
            db.collection("users").document(username)
                    .update("preferred_language_code", selectedLanguageCode)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Configuración actualizada", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Configuración de usuario actualizada");
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al actualizar la configuración", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Error updating user document", e);
                    });
        });
    }

    /**
     * Encuentra la posición del Spinner que corresponde al código de idioma.
     *
     * @param languageCode El código de idioma a buscar.
     * @return La posición del Spinner o -1 si no se encuentra.
     */
    private int getSpinnerPosition(String languageCode) {
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equalsIgnoreCase(languageCode)) {
                return i;
            }
        }
        return -1; // No encontrado
    }
}
