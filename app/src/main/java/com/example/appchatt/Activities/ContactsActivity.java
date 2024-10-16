package com.example.appchatt.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appchatt.R;

import java.util.ArrayList;

public class ContactsActivity extends AppCompatActivity {

    private ListView contactsListView;
    private String username; // Nombre de usuario actual
    private ArrayList<String> contactsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contacts);

        // Obtener el nombre de usuario pasado desde MenuActivity
        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");

        // Inicializar vistas
        contactsListView = findViewById(R.id.contactsListView);

        // Obtener lista de contactos
        // En una aplicación real, esto se obtendría desde una base de datos o API
        contactsList = getContacts(username);

        // Configurar adaptador
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactsList);
        contactsListView.setAdapter(adapter);

        // Manejar clic en un contacto
        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String contactUsername = contactsList.get(position);
                if (contactUsername.equals(username)) {
                    Toast.makeText(ContactsActivity.this, "No puedes chatear contigo mismo", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(ContactsActivity.this, ChatActivity.class);
                intent.putExtra("USERNAME", username);
                intent.putExtra("CONTACT", contactUsername);
                startActivity(intent);
            }
        });
    }

    // Método para obtener contactos
    private ArrayList<String> getContacts(String currentUser) {
        ArrayList<String> contacts = new ArrayList<>();
        // Para propósitos de demostración, agregar usuarios estáticos
        contacts.add("Alice");
        contacts.add("Bob");
        contacts.add("Charlie");
        contacts.add(currentUser); // Agregar al usuario actual para probar
        return contacts;
    }
}
