package com.example.pocketledger;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fragment for the AI Assistant section
 */
public class AiFragment extends Fragment {

    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1001;

    private RecyclerView rvChat;
    private EditText etInput;
    private ImageButton btnSend;
    private ImageButton btnVoice;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList = new ArrayList<>();
    private AiManager aiManager;
    private DatabaseHelper dbHelper;

    private ActivityResultLauncher<Intent> speechRecognizerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        aiManager = new AiManager(requireContext());
        dbHelper = DatabaseHelper.getInstance(requireContext());
        initSpeechRecognizerLauncher();
    }

    private void initSpeechRecognizerLauncher() {
        speechRecognizerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                        ArrayList<String> matches = result.getData()
                                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        if (matches != null && !matches.isEmpty()) {
                            String recognizedText = matches.get(0);
                            etInput.setText(recognizedText);
                            etInput.setSelection(recognizedText.length());
                            sendMessage();
                        }
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ai, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        if (messageList.isEmpty()) {
            showWelcomeMessage();
        }
    }

    private void initViews(View view) {
        rvChat = view.findViewById(R.id.rvChat);
        etInput = view.findViewById(R.id.etInput);
        btnSend = view.findViewById(R.id.btnSend);
        btnVoice = view.findViewById(R.id.btnVoice);

        chatAdapter = new ChatAdapter(messageList,
                (transaction, position) -> {
                    boolean success = dbHelper.addTransaction(transaction);
                    if (success) {
                        Toast.makeText(requireContext(), "账单已创建", Toast.LENGTH_SHORT).show();
                        messageList.remove(position);
                        chatAdapter.notifyItemRemoved(position);
                    }
                },
                (todoItem, position) -> {
                    boolean success = dbHelper.addTodoItem(todoItem);
                    if (success) {
                        Toast.makeText(requireContext(), "任务已创建", Toast.LENGTH_SHORT).show();
                        messageList.remove(position);
                        chatAdapter.notifyItemRemoved(position);
                    }
                });

        rvChat.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvChat.setAdapter(chatAdapter);

        btnSend.setOnClickListener(v -> sendMessage());
        btnVoice.setOnClickListener(v -> startVoiceInput());
    }

    private void startVoiceInput() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.RECORD_AUDIO },
                    PERMISSION_REQUEST_RECORD_AUDIO);
            return;
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINA.toString());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh-CN");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "请说话...");

        try {
            speechRecognizerLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            new android.app.AlertDialog.Builder(requireContext())
                    .setTitle("语音服务未安装")
                    .setMessage("您的设备似乎没有安装系统级语音识别服务。\n\n建议您直接点击键盘上的“麦克风”图标进行语音输入，效果是一样的！")
                    .setPositiveButton("知道了", (dialog, which) -> {
                        // Open keyboard automatically for convenience
                        etInput.requestFocus();
                        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) requireContext()
                                .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(etInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                    })
                    .show();
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
                Toast.makeText(requireContext(), "需要麦克风权限", Toast.LENGTH_SHORT).show();
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
                "• \"任务：完成作业\"\n" +
                "• \"任务 紧急提交报告\"\n\n" +
                "🎤 **语音输入** - 点击麦克风按钮";

        ChatMessage welcome = new ChatMessage(ChatMessage.TYPE_AI, welcomeText);
        welcome.setWelcomeMessage(true);
        messageList.add(welcome);
        chatAdapter.notifyItemInserted(0);
    }

    private void sendMessage() {
        String text = etInput.getText().toString().trim();
        if (text.isEmpty())
            return;

        messageList.add(new ChatMessage(ChatMessage.TYPE_USER, text));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rvChat.scrollToPosition(messageList.size() - 1);
        etInput.setText("");

        ChatMessage aiMsg = new ChatMessage(ChatMessage.TYPE_AI, "");
        aiMsg.setThinking(true);
        messageList.add(aiMsg);
        int aiPos = messageList.size() - 1;
        chatAdapter.notifyItemInserted(aiPos);
        rvChat.scrollToPosition(aiPos);

        String lowerText = text.toLowerCase();
        boolean isTask = lowerText.startsWith("任务") ||
                lowerText.startsWith("添加任务") ||
                lowerText.startsWith("新建任务") ||
                lowerText.startsWith("创建任务") ||
                lowerText.contains("任务：") ||
                lowerText.contains("任务:");

        if (isTask) {
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
                    AiManager.handleError(requireContext(), message);
                }
            });
        } else {
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
                    AiManager.handleError(requireContext(), message);
                }
            });
        }
    }
}
