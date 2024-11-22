// UserAdapter.java
package com.tuempresa.linguaconnect.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.tuempresa.linguaconnect.Models.User;
import com.tuempresa.linguaconnect.R;
import java.util.List;

public class UserAdapter extends ArrayAdapter<User> {

    public UserAdapter(@NonNull Context context, @NonNull List<User> users) {
        super(context, 0, users);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        User user = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_user, parent, false);
        }

        TextView tvUsername = convertView.findViewById(R.id.tvUsername);
        TextView tvLanguage = convertView.findViewById(R.id.tvLanguage);

        if (user != null) {
            tvUsername.setText(user.getUsername());
            tvLanguage.setText(getLanguageDisplayName(user.getLanguageCode()));
        }

        return convertView;
    }

    // Método para convertir el código de idioma a un nombre legible
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
