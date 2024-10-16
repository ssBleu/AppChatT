package com.example.appchatt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<Message> messagesList;
    private String currentUsername;

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(Context context, ArrayList<Message> messagesList, String currentUsername) {
        this.context = context;
        this.messagesList = messagesList;
        this.currentUsername = currentUsername;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messagesList.get(position);
        if (message.getSender().equals(currentUsername)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == VIEW_TYPE_SENT){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messagesList.get(position);
        if(holder.getItemViewType() == VIEW_TYPE_SENT){
            ((SentMessageViewHolder) holder).bind(message);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    // ViewHolder para mensajes enviados
    class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvSender, tvContent, tvTimestamp;

        SentMessageViewHolder(View itemView) {
            super(itemView);
            tvSender = itemView.findViewById(R.id.tvSender);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }

        void bind(Message message) {
            tvSender.setText("Tú");
            tvContent.setText(message.getContent());
            tvTimestamp.setText(formatTimestamp(message.getTimestamp()));
            // Opcional: Personalizar apariencia
            tvSender.setTextColor(context.getResources().getColor(R.color.my_primary));
            tvContent.setBackgroundResource(R.drawable.bg_message_sent);
        }
    }

    // ViewHolder para mensajes recibidos
    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvSender, tvContent, tvTimestamp;

        ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            tvSender = itemView.findViewById(R.id.tvSender);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }

        void bind(Message message) {
            tvSender.setText(message.getSender());
            tvContent.setText(message.getContent());
            tvTimestamp.setText(formatTimestamp(message.getTimestamp()));
            // Opcional: Personalizar apariencia
            tvSender.setTextColor(context.getResources().getColor(R.color.my_secondary));
            tvContent.setBackgroundResource(R.drawable.bg_message_received);
        }
    }

    private String formatTimestamp(Timestamp timestamp){
        if(timestamp == null){
            return "Sin fecha";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(timestamp.toDate());
    }
}
