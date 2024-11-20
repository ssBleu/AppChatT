// HobbieSelectionActivity.java
package com.tuempresa.linguaconnect.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.QuerySnapshot;
import com.tuempresa.linguaconnect.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HobbieSelectionActivity extends AppCompatActivity {

    private static final String TAG = "HobbySelectionActivity";

    private String username;
    private String language;
    private String languageCode;
    private ListView listViewHobbies;

    // Lista de hobbies disponibles
    private String[] hobbies = {"Lectura", "Deportes", "Música", "Cocina", "Viajes", "Fotografía", "Arte", "Tecnología", "Jardinería", "Cine"};

    // Firebase Firestore
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hobbie_selection);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Obtener los datos pasados desde MenuActivity
        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");

        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "Información del usuario no disponible", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "El nombre de usuario es nulo o está vacío");
            finish();
            return;
        }

        Log.d(TAG, "USERNAME: " + username);

        // Recuperar la información del usuario desde user_config
        fetchUserConfigData();
    }

    private void fetchUserConfigData() {
        db.collection("user_config")
                .whereEqualTo("user_id", username)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Suponiendo que 'user_id' es único y retorna un solo documento
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            languageCode = documentSnapshot.getString("preferred_language_code");
                            Log.d(TAG, "LANGUAGE_CODE: " + languageCode);

                            if (languageCode == null || languageCode.isEmpty()) {
                                Toast.makeText(HobbieSelectionActivity.this, "Información de idioma no disponible", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "El código de idioma es nulo o está vacío");
                                finish();
                                return;
                            }

                            // Derivar el nombre del idioma a partir del código
                            language = getLanguageDisplayName(languageCode);

                            if (language == null || language.isEmpty()) {
                                Toast.makeText(HobbieSelectionActivity.this, "Código de idioma desconocido", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "El código de idioma no corresponde a ningún idioma conocido");
                                finish();
                                return;
                            }

                            Log.d(TAG, "LANGUAGE: " + language);

                            // Inicializar vistas después de obtener la información del usuario
                            initializeViews();
                        } else {
                            Toast.makeText(HobbieSelectionActivity.this, "Configuración del usuario no encontrada", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "No se encontró configuración en user_config para el usuario: " + username);
                            finish();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HobbieSelectionActivity.this, "Error al obtener configuración del usuario", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error al obtener configuración del usuario", e);
                        finish();
                    }
                });
    }


    private void initializeViews() {
        // Inicializar vistas
        listViewHobbies = findViewById(R.id.listViewHobbies);

        // Configurar el adaptador para el ListView sin selección múltiple
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, hobbies);
        listViewHobbies.setAdapter(adapter);
        listViewHobbies.setChoiceMode(ListView.CHOICE_MODE_NONE);

        // Manejar clic en cada hobby
        listViewHobbies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedHobby = hobbies[position].toLowerCase();

                // Redirigir a UserListActivity pasando el hobby seleccionado y la información del idioma
                Intent usersListIntent = new Intent(HobbieSelectionActivity.this, UserListActivity.class);
                usersListIntent.putExtra("USERNAME", username);
                usersListIntent.putExtra("SELECTED_HOBBY", selectedHobby); // Nuevo extra para el hobby
                usersListIntent.putExtra("LANGUAGE", language);
                usersListIntent.putExtra("LANGUAGE_CODE", languageCode);
                startActivity(usersListIntent);
                finish();
            }
        });
    }

    /**
     * Método para convertir el código de idioma a un nombre legible.
     * Puedes ampliar esta lista según tus necesidades.
     */
    private String getLanguageDisplayName(String languageCode) {
        switch (languageCode) {
            case "en":
                return "Inglés";
            case "es":
                return "Español";
            case "fr":
                return "Francés";
            case "de":
                return "Alemán";
            case "it":
                return "Italiano";
            case "pt":
                return "Portugués";
            case "ru":
                return "Ruso";
            case "zh":
                return "Chino";
            case "ja":
                return "Japonés";
            case "ko":
                return "Coreano";
            // Agrega más casos según los idiomas que soportes
            default:
                return languageCode; // Retorna el código si no hay una correspondencia
        }
    }
}
