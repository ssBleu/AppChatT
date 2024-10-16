package com.example.appchatt;

import com.google.firebase.Timestamp;

public class Message {
    private String sender;
    private String recipient;
    private String content;
    private Timestamp timestamp;

    // Constructor vacío necesario para Firestore
    public Message() {}

    public Message(String sender, String recipient, String content, Timestamp timestamp) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Getters y Setters
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    // Opcional: Para comparar mensajes y evitar duplicados
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Message)) return false;
        Message other = (Message) obj;
        return sender.equals(other.sender) &&
                recipient.equals(other.recipient) &&
                content.equals(other.content) &&
                timestamp.equals(other.timestamp);
    }

    @Override
    public int hashCode() {
        return sender.hashCode() + recipient.hashCode() + content.hashCode() + timestamp.hashCode();
    }
}
