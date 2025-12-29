package com.example.pocketledger;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AiManager {

    // Using DeepSeek API
    private static final String API_URL = "https://api.deepseek.com/chat/completions";
    // API Key loaded securely from BuildConfig (set in local.properties)
    private static final String API_KEY = BuildConfig.DEEPSEEK_API_KEY;

    private final OkHttpClient client;
    private final Gson gson = new Gson();

    public interface AiCallback {
        void onSuccess(Transaction transaction);

        void onError(String message);
    }

    public AiManager() {
        client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    public void parseBill(String text, AiCallback callback) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date());

        String prompt = "你是一个智能记账助手。今天是 " + today + "。\n\n" +
                "请从用户输入中提取记账信息：\"" + text + "\"\n\n" +
                "## 分类参考：\n" +
                "【支出类】\n" +
                "- 餐饮-早餐/午餐/晚餐/外卖/饮料/零食\n" +
                "- 交通-公交/地铁/打车/加油/停车\n" +
                "- 购物-服饰/日用品/电子产品/化妆品\n" +
                "- 娱乐-电影/游戏/KTV/运动\n" +
                "- 居住-房租/水电/燃气/物业\n" +
                "- 医疗-药品/挂号/体检\n" +
                "- 教育-书籍/课程/培训\n" +
                "- 人情-红包/礼物/请客\n" +
                "- 其他支出\n\n" +
                "【收入类】\n" +
                "- 职业收入-工资/奖金/兼职\n" +
                "- 其他收入-红包/退款/利息\n\n" +
                "## 日期解析规则：\n" +
                "- \"昨天\" → 昨日日期\n" +
                "- \"前天\" → 前日日期\n" +
                "- \"上周X\" → 计算对应日期\n" +
                "- \"X月X日\" → 当年对应日期\n" +
                "- 未提及日期 → 使用今天 " + today + "\n\n" +
                "## 返回格式（纯JSON，不要markdown）：\n" +
                "{\n" +
                "  \"type\": \"income\" 或 \"expense\",\n" +
                "  \"amount\": 数字金额,\n" +
                "  \"category\": \"主类-子类\",\n" +
                "  \"note\": \"简短备注\",\n" +
                "  \"date\": \"yyyy-MM-dd格式日期\"\n" +
                "}\n\n" +
                "注意：只返回JSON对象，不要包含```或任何解释文字。";

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("model", "deepseek-chat");

        com.google.gson.JsonArray messages = new com.google.gson.JsonArray();
        JsonObject msgObj = new JsonObject();
        msgObj.addProperty("role", "user");
        msgObj.addProperty("content", prompt);
        messages.add(msgObj);
        jsonBody.add("messages", messages);

        RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                sendError(callback, "网络连接失败: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (!response.isSuccessful()) {
                    sendError(callback, "AI 服务异常: " + response.code());
                    return;
                }

                try (ResponseBody responseBody = response.body()) {
                    if (responseBody == null) {
                        sendError(callback, "AI 返回内容为空");
                        return;
                    }

                    String respStr = responseBody.string();
                    JsonObject respJson = gson.fromJson(respStr, JsonObject.class);
                    String content = respJson.get("choices").getAsJsonArray().get(0)
                            .getAsJsonObject().get("message").getAsJsonObject()
                            .get("content").getAsString();

                    // Remove markdown tags if any
                    content = content.replace("```json", "").replace("```", "").trim();

                    JsonObject billJson = gson.fromJson(content, JsonObject.class);

                    String type = billJson.get("type").getAsString();
                    double amount = billJson.get("amount").getAsDouble();
                    String category = billJson.get("category").getAsString();
                    String note = billJson.has("note") ? billJson.get("note").getAsString() : "";

                    // NEW: Extract date from AI response
                    String date;
                    if (billJson.has("date")) {
                        date = billJson.get("date").getAsString();
                    } else {
                        date = today;
                    }

                    Transaction transaction = new Transaction(type, amount, category, note, date);

                    new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(transaction));
                } catch (Exception e) {
                    sendError(callback, "AI 解析数据失败: " + e.getMessage());
                }
            }
        });
    }

    private void sendError(AiCallback callback, String msg) {
        new Handler(Looper.getMainLooper()).post(() -> callback.onError(msg));
    }

    // ==================== Task Parsing ====================

    public interface TaskCallback {
        void onSuccess(TodoItem todoItem);

        void onError(String message);
    }

    public void parseTask(String text, TaskCallback callback) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date());

        String prompt = "你是一个任务管理助手。今天是 " + today + "。\n\n" +
                "请从用户输入中提取任务信息：\"" + text + "\"\n\n" +
                "## 优先级判断规则：\n" +
                "- high (高): 包含\"紧急\"、\"重要\"、\"马上\"、\"立刻\"、\"赶紧\"、\"今天必须\"等词\n" +
                "- medium (中): 普通任务，没有特别紧急或不重要的暗示\n" +
                "- low (低): 包含\"有空\"、\"以后\"、\"不急\"、\"闲了\"、\"想起来\"等词\n\n" +
                "## 截止日期解析规则：\n" +
                "- \"今天\" → " + today + "\n" +
                "- \"明天\" → 明日日期\n" +
                "- \"后天\" → 后日日期\n" +
                "- \"下周X\" → 计算对应日期\n" +
                "- \"X月X日\" → 当年对应日期\n" +
                "- 未提及日期 → 留空\n\n" +
                "## 返回格式（纯JSON，不要markdown）：\n" +
                "{\n" +
                "  \"title\": \"任务标题\",\n" +
                "  \"description\": \"任务描述或留空\",\n" +
                "  \"priority\": \"high/medium/low\",\n" +
                "  \"due_date\": \"yyyy-MM-dd格式或留空\",\n" +
                "  \"tags\": \"标签用逗号分隔或留空\"\n" +
                "}\n\n" +
                "注意：只返回JSON对象，不要包含```或任何解释文字。";

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("model", "deepseek-chat");

        com.google.gson.JsonArray messages = new com.google.gson.JsonArray();
        JsonObject msgObj = new JsonObject();
        msgObj.addProperty("role", "user");
        msgObj.addProperty("content", prompt);
        messages.add(msgObj);
        jsonBody.add("messages", messages);

        RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                sendTaskError(callback, "网络连接失败: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (!response.isSuccessful()) {
                    sendTaskError(callback, "AI 服务异常: " + response.code());
                    return;
                }

                try (ResponseBody responseBody = response.body()) {
                    if (responseBody == null) {
                        sendTaskError(callback, "AI 返回内容为空");
                        return;
                    }

                    String respStr = responseBody.string();
                    JsonObject respJson = gson.fromJson(respStr, JsonObject.class);
                    String content = respJson.get("choices").getAsJsonArray().get(0)
                            .getAsJsonObject().get("message").getAsJsonObject()
                            .get("content").getAsString();

                    // Remove markdown tags if any
                    content = content.replace("```json", "").replace("```", "").trim();

                    JsonObject taskJson = gson.fromJson(content, JsonObject.class);

                    String title = taskJson.get("title").getAsString();
                    String description = taskJson.has("description") ? taskJson.get("description").getAsString() : "";
                    String priority = taskJson.has("priority") ? taskJson.get("priority").getAsString() : "medium";
                    String dueDate = taskJson.has("due_date") ? taskJson.get("due_date").getAsString() : null;
                    String tags = taskJson.has("tags") ? taskJson.get("tags").getAsString() : "";

                    // Clean empty values
                    if (dueDate != null && dueDate.isEmpty())
                        dueDate = null;
                    if (description.equals("null"))
                        description = "";
                    if (tags.equals("null"))
                        tags = "";

                    TodoItem todoItem = new TodoItem(title, description, TodoItem.STATUS_NOT_STARTED,
                            priority, dueDate, tags, today, null, null);

                    new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(todoItem));
                } catch (Exception e) {
                    sendTaskError(callback, "AI 解析任务失败: " + e.getMessage());
                }
            }
        });
    }

    private void sendTaskError(TaskCallback callback, String msg) {
        new Handler(Looper.getMainLooper()).post(() -> callback.onError(msg));
    }
}
