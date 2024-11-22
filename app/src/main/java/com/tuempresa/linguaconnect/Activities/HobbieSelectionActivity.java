// HobbieSelectionActivity.java

package com.tuempresa.linguaconnect.Activities;

import com.tuempresa.linguaconnect.Helpers.Constants; // Importar la clase de constantes

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tuempresa.linguaconnect.Adapter.HobbyAdapter;
import com.tuempresa.linguaconnect.R;

public class HobbieSelectionActivity extends AppCompatActivity {

    private static final String TAG = "HobbieSelectionActivity";

    // Vistas
    private ListView listViewHobbies;

    // Datos del usuario
    private String username;
    private String language;
    private String languageCode;

    // Lista de hobbies disponibles
    private static final String[] HOBBIES = {
            "lectura", "deportes", "música", "cocina", "viajes",
            "fotografía", "arte", "tecnología", "jardinería", "cine"
    };


    private static final int[] HOBBY_IMAGES = {
            R.drawable.lectura, R.drawable.deportes, R.drawable.musica, R.drawable.cocina, R.drawable.viajes,
            R.drawable.fotografia, R.drawable.arte, R.drawable.tecnologia, R.drawable.jardineria, R.drawable.cine
    };

    // Firebase Firestore
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hobbie_selection);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Inicializar vistas
        listViewHobbies = findViewById(R.id.listViewHobbies);

        // Configurar el botón de regresar
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> onBackPressed());

        listViewHobbies.setOnItemClickListener((parent, view, position, id) -> {
            String selectedHobby = HOBBIES[position].toLowerCase();
            Toast.makeText(HobbieSelectionActivity.this, "Hobby seleccionado: " + selectedHobby, Toast.LENGTH_SHORT).show();
        });

        // Obtener y validar extras
        if (!initializeExtras()) {
            finish();
            return;
        }

        Log.d(TAG, "USERNAME: " + username);

        // Recuperar la información del usuario desde user_config
        fetchUserConfigData();
    }

    /**
     * Obtiene y valida los extras recibidos en el intent.
     *
     * @return true si los extras son válidos, false de lo contrario.
     */
    private boolean initializeExtras() {
        Intent intent = getIntent();
        username = intent.getStringExtra(Constants.EXTRA_USERNAME);

        if (isStringNullOrEmpty(username)) {
            showToast("Información del usuario no disponible");
            Log.e(TAG, "El nombre de usuario es nulo o está vacío");
            return false;
        }

        return true;
    }

    /**
     * Verifica si el string proporcionado es nulo o está vacío.
     *
     * @param str El string a verificar.
     * @return true si el string es nulo o está vacío, false de lo contrario.
     */
    private boolean isStringNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Recupera la configuración del usuario desde Firestore.
     */
    private void fetchUserConfigData() {
        db.collection("user_config")
                .whereEqualTo("user_id", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Suponiendo que 'user_id' es único y retorna un solo documento
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        languageCode = documentSnapshot.getString("preferred_language_code");
                        Log.d(TAG, "LANGUAGE_CODE: " + languageCode);

                        if (isStringNullOrEmpty(languageCode)) {
                            showToast("Información de idioma no disponible");
                            Log.e(TAG, "El código de idioma es nulo o está vacío");
                            finish();
                            return;
                        }

                        // Derivar el nombre del idioma a partir del código
                        language = getLanguageDisplayName(languageCode);

                        if (isStringNullOrEmpty(language)) {
                            showToast("Código de idioma desconocido");
                            Log.e(TAG, "El código de idioma no corresponde a ningún idioma conocido");
                            finish();
                            return;
                        }

                        Log.d(TAG, "LANGUAGE: " + language);

                        // Inicializar vistas después de obtener la información del usuario
                        initializeViews();
                    } else {
                        showToast("Configuración del usuario no encontrada");
                        Log.e(TAG, "No se encontró configuración en user_config para el usuario: " + username);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    showToast("Error al obtener configuración del usuario");
                    Log.e(TAG, "Error al obtener configuración del usuario", e);
                    finish();
                });
    }

    /**
     * Inicializa las vistas y componentes de la interfaz.
     */
    private void initializeViews() {
        listViewHobbies = findViewById(R.id.listViewHobbies);
        setupHobbiesListView();
    }

    /**
     * Configura el ListView con los hobbies disponibles y sus listeners.
     */
    private void setupHobbiesListView() {
        HobbyAdapter adapter = new HobbyAdapter(this, HOBBIES, HOBBY_IMAGES);
        listViewHobbies.setAdapter(adapter);
        listViewHobbies.setChoiceMode(ListView.CHOICE_MODE_NONE);

        listViewHobbies.setOnItemClickListener((parent, view, position, id) -> {
            String selectedHobby = HOBBIES[position].toLowerCase();
            Log.d(TAG, "Hobby seleccionado: " + selectedHobby);

            // Redirigir a UserListActivity pasando el hobby seleccionado y la información del idioma
            fetchContactLanguageCodeAndStartUserListActivity(selectedHobby);
        });
    }

    /**
     * Recupera el código de idioma preferido del usuario actual y luego inicia UserListActivity.
     *
     * @param selectedHobby Hobby seleccionado por el usuario.
     */
    private void fetchContactLanguageCodeAndStartUserListActivity(String selectedHobby) {
        // Si necesitas el código de idioma del usuario para UserListActivity, ya lo tienes en languageCode
        // Aquí simplemente inicias UserListActivity con todos los extras necesarios

        Intent usersListIntent = new Intent(HobbieSelectionActivity.this, UserListActivity.class);
        usersListIntent.putExtra(Constants.EXTRA_USERNAME, username);
        usersListIntent.putExtra(Constants.EXTRA_SELECTED_HOBBY, selectedHobby);
        usersListIntent.putExtra(Constants.EXTRA_LANGUAGE, language);
        usersListIntent.putExtra(Constants.EXTRA_USER_LANGUAGE_CODE, languageCode);
        startActivity(usersListIntent);
        Log.d(TAG, "Iniciando UserListActivity con hobby: " + selectedHobby +
                ", LANGUAGE: " + language + ", LANGUAGE_CODE: " + languageCode);
        finish();
    }

    /**
     * Método para convertir el código de idioma a un nombre legible.
     * Puedes ampliar esta lista según tus necesidades.
     *
     * @param languageCode Código de idioma (e.g., "en", "es").
     * @return Nombre legible del idioma, o el código si no se encuentra una correspondencia.
     */
    private String getLanguageDisplayName(String languageCode) {
        if (languageCode == null) {
            return "";
        }

        switch (languageCode.toLowerCase()) {
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

    /**
     * Muestra un Toast con el mensaje proporcionado.
     *
     * @param message Mensaje a mostrar.
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
