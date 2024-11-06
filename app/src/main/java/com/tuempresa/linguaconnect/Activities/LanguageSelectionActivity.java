// LanguageSelectionActivity.java

package com.tuempresa.linguaconnect.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tuempresa.linguaconnect.R;

public class LanguageSelectionActivity extends AppCompatActivity {

    private static final String TAG = "LanguageSelectionActivity";

    private ListView listViewLanguages;
    private String username;

    // Lista de idiomas disponibles
    private String[] languages = {"Español", "Inglés", "Francés", "Alemán", "Italiano", "Portugués"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selection);

        // Obtener el nombre de usuario pasado desde MenuActivity
        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");

        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "Información del usuario no disponible", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Username is null or empty");
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
        listViewLanguages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String selectedLanguage = languages[position];
                Log.d(TAG, "Idioma seleccionado: " + selectedLanguage);

                // Iniciar UsersListActivity pasando el idioma seleccionado
                Intent usersListIntent = new Intent(LanguageSelectionActivity.this, UserListActivity.class);
                usersListIntent.putExtra("USERNAME", username);
                usersListIntent.putExtra("LANGUAGE", selectedLanguage);
                startActivity(usersListIntent);
                finish();
            }
        });
    }
}
