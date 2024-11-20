// LanguageSelectionActivity.java

package com.tuempresa.linguaconnect.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tuempresa.linguaconnect.R;

public class LanguageSelectionActivity extends AppCompatActivity {

    private static final String TAG = "LanguageSelectionActivity";

    private String username;

    // Lista de idiomas disponibles con sus códigos
    private String[] languages = {"Español", "Inglés", "Francés", "Alemán", "Italiano", "Portugués", "Ruso", "Chino", "Japonés", "Coreano"};
    private String[] languageCodes = {"es", "en", "fr", "de", "it", "pt", "ru", "zh", "ja", "ko"};

    private ListView listViewLanguages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selection);

        // Obtener el nombre de usuario pasado desde la actividad anterior
        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");

        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "Información del usuario no disponible", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "El nombre de usuario es nulo o está vacío");
            finish();
            return;
        }

        // Inicializar vistas
        listViewLanguages = findViewById(R.id.listViewLanguages);

        // Configurar el adaptador para el ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, languages);
        listViewLanguages.setAdapter(adapter);

        // Manejar clic en un idioma
        listViewLanguages.setOnItemClickListener((adapterView, view, position, l) -> {
            String selectedLanguage = languages[position];
            String selectedLanguageCode = languageCodes[position];
            Log.d(TAG, "Idioma seleccionado: " + selectedLanguage + " (" + selectedLanguageCode + ")");

            // Iniciar UserListActivity pasando el idioma seleccionado
            Intent usersListIntent = new Intent(LanguageSelectionActivity.this, UserListActivity.class);
            usersListIntent.putExtra("USERNAME", username);
            usersListIntent.putExtra("LANGUAGE", selectedLanguage);
            usersListIntent.putExtra("LANGUAGE_CODE", selectedLanguageCode);
            startActivity(usersListIntent);
        });
    }
}
