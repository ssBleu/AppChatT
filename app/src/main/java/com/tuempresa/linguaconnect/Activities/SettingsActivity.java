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
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.languages_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);

        // Obtener la configuración actual del usuario
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user_config").document(username).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String preferredLanguage = documentSnapshot.getString("preferred_language_code");
                        if (preferredLanguage != null) {
                            int spinnerPosition = adapter.getPosition(preferredLanguage);
                            spinnerLanguage.setSelection(spinnerPosition);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al obtener la configuración", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error getting user_config document", e);
                });

        // Manejar clic en el botón de guardar
        btnSave.setOnClickListener(view -> {
            String selectedLanguage = spinnerLanguage.getSelectedItem().toString();

            // Mapear el lenguaje seleccionado a un código de idioma
            String languageCode = mapLanguageToCode(selectedLanguage);

            db.collection("user_config").document(username)
                    .update("preferred_language_code", languageCode)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Configuración actualizada", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Configuración de usuario actualizada");
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al actualizar la configuración", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Error updating user_config", e);
                    });
        });
    }

    private String mapLanguageToCode(String language) {
        switch (language) {
            case "Español":
                return "es";
            case "Inglés":
                return "en";
            // Añade más casos según los idiomas soportados
            default:
                return "es";
        }
    }
}
