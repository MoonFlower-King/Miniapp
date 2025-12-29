package com.example.pocketledger;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AiAssistantActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1001;

    private RecyclerView rvChat;
    private EditText etInput;
    private ImageButton btnSend;
    private ImageButton btnVoice;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList = new ArrayList<>();
    private AiManager aiManager;
    private DatabaseHelper dbHelper;

    // Speech recognition using Intent approach (more compatible)
    private ActivityResultLauncher<Intent> speechRecognizerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_assistant);

        aiManager = new AiManager();
        dbHelper = DatabaseHelper.getInstance(this);

        initSpeechRecognizerLauncher();
        initViews();
    }

    private void initSpeechRecognizerLauncher() {
        speechRecognizerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        ArrayList<String> matches = result.getData()
                                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        if (matches != null && !matches.isEmpty()) {
                            String recognizedText = matches.get(0);
                            etInput.setText(recognizedText);
                            etInput.setSelection(recognizedText.length());
                            // Auto send after voice input
                            sendMessage();
                        }
                    }
                });
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
        rvChat = findViewById(R.id.rvChat);
        etInput = findViewById(R.id.etInput);
        btnSend = findViewById(R.id.btnSend);
        btnVoice = findViewById(R.id.btnVoice);

        chatAdapter = new ChatAdapter(messageList,
                // Transaction confirm listener
                (transaction, position) -> {
                    boolean success = dbHelper.addTransaction(transaction);
                    if (success) {
                        Toast.makeText(this, "账单已创建", Toast.LENGTH_SHORT).show();
                        messageList.remove(position);
                        chatAdapter.notifyItemRemoved(position);
                    }
                },
                // Task confirm listener
                (todoItem, position) -> {
                    boolean success = dbHelper.addTodoItem(todoItem);
                    if (success) {
                        Toast.makeText(this, "任务已创建", Toast.LENGTH_SHORT).show();
                        messageList.remove(position);
                        chatAdapter.notifyItemRemoved(position);
                    }
                });

        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(chatAdapter);

        btnSend.setOnClickListener(v -> sendMessage());
        btnVoice.setOnClickListener(v -> startVoiceInput());

        // Show welcome message with examples
        showWelcomeMessage();
    }

    private void startVoiceInput() {
        // Check permission first
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.RECORD_AUDIO },
                    PERMISSION_REQUEST_RECORD_AUDIO);
            return;
        }

        // Use Intent-based speech recognition (more compatible across devices)
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINA.toString());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh-CN");
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "请说话...");

        try {
            speechRecognizerLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "您的设备不支持语音识别，请安装语音输入法", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceInput();
            } else {
                Toast.makeText(this, "需要麦克风权限才能使用语音输入", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showWelcomeMessage() {
        String welcomeText = "👋 你好！我是你的AI助手\n\n" +
                "我可以帮你：\n\n" +
                "💰 **记账** - 用自然语言记录收支\n" +
                "• \"午饭花了35元\"\n" +
                "• \"收到工资5000元\"\n\n" +
                "✅ **创建任务** - 说\"任务\"开头\n" +
                "• \"任务：完成作业，明天截止\"\n" +
                "• \"任务 紧急提交报告\"\n\n" +
                "🎤 **语音输入** - 点击麦克风按钮\n" +
                "直接说话，我会转成文字并处理！";

        ChatMessage welcome = new ChatMessage(ChatMessage.TYPE_AI, welcomeText);
        welcome.setWelcomeMessage(true);
        messageList.add(welcome);
        chatAdapter.notifyItemInserted(0);
    }

    private void sendMessage() {
        String text = etInput.getText().toString().trim();
        if (text.isEmpty())
            return;

        // 1. Add User Message
        messageList.add(new ChatMessage(ChatMessage.TYPE_USER, text));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rvChat.scrollToPosition(messageList.size() - 1);
        etInput.setText("");

        // 2. Add AI Thinking Message
        ChatMessage aiMsg = new ChatMessage(ChatMessage.TYPE_AI, "");
        aiMsg.setThinking(true);
        messageList.add(aiMsg);
        int aiPos = messageList.size() - 1;
        chatAdapter.notifyItemInserted(aiPos);
        rvChat.scrollToPosition(aiPos);

        // 3. Determine if this is a task or a bill
        String lowerText = text.toLowerCase();
        boolean isTask = lowerText.startsWith("任务") ||
                lowerText.startsWith("添加任务") ||
                lowerText.startsWith("新建任务") ||
                lowerText.startsWith("创建任务") ||
                lowerText.contains("任务：") ||
                lowerText.contains("任务:");

        if (isTask) {
            // Parse as task
            aiManager.parseTask(text, new AiManager.TaskCallback() {
                @Override
                public void onSuccess(TodoItem todoItem) {
                    aiMsg.setThinking(false);
                    aiMsg.setPendingTodoItem(todoItem);
                    chatAdapter.notifyItemChanged(aiPos);
                }

                @Override
                public void onError(String message) {
                    messageList.remove(aiPos);
                    chatAdapter.notifyItemRemoved(aiPos);
                    Toast.makeText(AiAssistantActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Parse as bill
            aiManager.parseBill(text, new AiManager.AiCallback() {
                @Override
                public void onSuccess(Transaction transaction) {
                    aiMsg.setThinking(false);
                    aiMsg.setPendingTransaction(transaction);
                    chatAdapter.notifyItemChanged(aiPos);
                }

                @Override
                public void onError(String message) {
                    messageList.remove(aiPos);
                    chatAdapter.notifyItemRemoved(aiPos);
                    Toast.makeText(AiAssistantActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
