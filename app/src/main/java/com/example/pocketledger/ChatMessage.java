package com.example.pocketledger;

public class ChatMessage {
    public static final int TYPE_USER = 0;
    public static final int TYPE_AI = 1;

    private int type;
    private String content;
    private Transaction pendingTransaction;
    private TodoItem pendingTodoItem;
    private boolean isThinking;
    private boolean isWelcomeMessage;

    public ChatMessage(int type, String content) {
        this.type = type;
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public Transaction getPendingTransaction() {
        return pendingTransaction;
    }

    public void setPendingTransaction(Transaction pendingTransaction) {
        this.pendingTransaction = pendingTransaction;
    }

    public TodoItem getPendingTodoItem() {
        return pendingTodoItem;
    }

    public void setPendingTodoItem(TodoItem pendingTodoItem) {
        this.pendingTodoItem = pendingTodoItem;
    }

    public boolean isThinking() {
        return isThinking;
    }

    public void setThinking(boolean thinking) {
        isThinking = thinking;
    }

    public boolean isWelcomeMessage() {
        return isWelcomeMessage;
    }

    public void setWelcomeMessage(boolean welcomeMessage) {
        isWelcomeMessage = welcomeMessage;
    }

    public boolean hasPendingTask() {
        return pendingTodoItem != null;
    }

    public boolean hasPendingTransaction() {
        return pendingTransaction != null;
    }
}
