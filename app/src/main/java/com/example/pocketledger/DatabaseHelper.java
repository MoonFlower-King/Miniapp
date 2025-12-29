package com.example.pocketledger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "PocketLedger.db";
    private static final int DATABASE_VERSION = 8; // Enhanced todo table V2 (Visibility features)

    // Transaction table
    private static final String TABLE_NAME = "transactions";
    private static final String COL_ID = "id";
    private static final String COL_TYPE = "type";
    private static final String COL_AMOUNT = "amount";
    private static final String COL_CATEGORY = "category";
    private static final String COL_NOTE = "note";
    private static final String COL_DATE = "date";

    // Diary table (legacy)
    private static final String TABLE_DIARY = "diary";
    private static final String DIARY_ID = "id";
    private static final String DIARY_TITLE = "title";
    private static final String DIARY_CONTENT = "content";
    private static final String DIARY_MOOD = "mood";
    private static final String DIARY_DATE = "date";
    private static final String DIARY_CREATED_AT = "created_at";

    // Todo table (Notion-style task tracker) - Enhanced
    private static final String TABLE_TODO = "todos_v2";
    private static final String TODO_ID = "id";
    private static final String TODO_TITLE = "title";
    private static final String TODO_DESCRIPTION = "description";
    private static final String TODO_STATUS = "status"; // not_started, in_progress, completed
    private static final String TODO_PRIORITY = "priority"; // high, medium, low
    private static final String TODO_DUE_DATE = "due_date";
    private static final String TODO_TAGS = "tags";
    private static final String TODO_DATE = "date";
    private static final String TODO_CREATED_AT = "created_at";
    private static final String TODO_ASSIGNEE = "assignee"; // New in v8
    private static final String TODO_ATTACHMENT = "attachment"; // New in v8

    // Singleton instance
    private static volatile DatabaseHelper instance;

    /**
     * Get singleton instance of DatabaseHelper
     * Thread-safe double-checked locking pattern
     */
    public static DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                if (instance == null) {
                    instance = new DatabaseHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create transactions table
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TYPE + " TEXT, " +
                COL_AMOUNT + " REAL, " +
                COL_CATEGORY + " TEXT, " +
                COL_NOTE + " TEXT, " +
                COL_DATE + " TEXT)";
        db.execSQL(createTable);

        // Create diary table (legacy)
        String createDiaryTable = "CREATE TABLE " + TABLE_DIARY + " (" +
                DIARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DIARY_TITLE + " TEXT, " +
                DIARY_CONTENT + " TEXT, " +
                DIARY_MOOD + " TEXT, " +
                DIARY_DATE + " TEXT, " +
                DIARY_CREATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(createDiaryTable);

        // Create todo table (Notion-style enhanced)
        createTodoTableV2(db);

        // Create indexes for better query performance
        createIndexes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Migration from version 3 to 4: add indexes
        if (oldVersion < 4) {
            createIndexes(db);
        }
        // Migration from version 4 to 5: add diary table
        if (oldVersion < 5) {
            String createDiaryTable = "CREATE TABLE IF NOT EXISTS " + TABLE_DIARY + " (" +
                    DIARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DIARY_TITLE + " TEXT, " +
                    DIARY_CONTENT + " TEXT, " +
                    DIARY_MOOD + " TEXT, " +
                    DIARY_DATE + " TEXT, " +
                    DIARY_CREATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP)";
            db.execSQL(createDiaryTable);
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_diary_date ON " + TABLE_DIARY + " (" + DIARY_DATE + ")");
        }
        // Migration from version 5/6 to 7: create enhanced todo table
        if (oldVersion < 7) {
            createTodoTableV2(db);
            // Migrate data from old todos table if exists
            try {
                db.execSQL("INSERT INTO " + TABLE_TODO + " (" + TODO_TITLE + ", " + TODO_STATUS + ", " +
                        TODO_PRIORITY + ", " + TODO_DATE + ", " + TODO_CREATED_AT + ") " +
                        "SELECT title, CASE WHEN completed = 1 THEN 'completed' ELSE 'not_started' END, " +
                        "priority, date, created_at FROM todos");
            } catch (Exception e) {
                // Old table might not exist, ignore
            }
        }
        // Migration from version 7 to 8: Add assignee and attachment columns
        if (oldVersion < 8) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_TODO + " ADD COLUMN " + TODO_ASSIGNEE + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_TODO + " ADD COLUMN " + TODO_ATTACHMENT + " TEXT");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void createTodoTableV2(SQLiteDatabase db) {
        String createTodoTable = "CREATE TABLE IF NOT EXISTS " + TABLE_TODO + " (" +
                TODO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TODO_TITLE + " TEXT, " +
                TODO_DESCRIPTION + " TEXT, " +
                TODO_STATUS + " TEXT DEFAULT 'not_started', " +
                TODO_PRIORITY + " TEXT, " +
                TODO_DUE_DATE + " TEXT, " +
                TODO_TAGS + " TEXT, " +
                TODO_DATE + " TEXT, " +
                TODO_CREATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP, " +
                TODO_ASSIGNEE + " TEXT, " +
                TODO_ATTACHMENT + " TEXT)";
        db.execSQL(createTodoTable);
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_todo_v2_date ON " + TABLE_TODO + " (" + TODO_DATE + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_todo_v2_status ON " + TABLE_TODO + " (" + TODO_STATUS + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_todo_v2_priority ON " + TABLE_TODO + " (" + TODO_PRIORITY + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_todo_v2_due ON " + TABLE_TODO + " (" + TODO_DUE_DATE + ")");
    }

    /**
     * Create indexes on frequently queried columns
     */
    private void createIndexes(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_date ON " + TABLE_NAME + " (" + COL_DATE + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_type ON " + TABLE_NAME + " (" + COL_TYPE + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_date_type ON " + TABLE_NAME + " (" + COL_DATE + ", " + COL_TYPE
                    + ")");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean addTransaction(Transaction transaction) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_TYPE, transaction.getType());
            cv.put(COL_AMOUNT, transaction.getAmount());
            cv.put(COL_CATEGORY, transaction.getCategory());
            cv.put(COL_NOTE, transaction.getNote());
            cv.put(COL_DATE, transaction.getDate());

            long result = db.insert(TABLE_NAME, null, cv);
            return result != -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_DATE + " DESC, " + COL_ID + " DESC", null)) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(0);
                    String type = cursor.getString(1);
                    double amount = cursor.getDouble(2);
                    String category = cursor.getString(3);
                    String note = cursor.getString(4);
                    String date = cursor.getString(5);
                    list.add(new Transaction(id, type, amount, category, note, date));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Transaction> getTransactionsByDate(String date) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_DATE + " = ? ORDER BY " + COL_ID + " DESC",
                new String[] { date })) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(0);
                    String type = cursor.getString(1);
                    double amount = cursor.getDouble(2);
                    String category = cursor.getString(3);
                    String note = cursor.getString(4);
                    String dateVal = cursor.getString(5);
                    list.add(new Transaction(id, type, amount, category, note, dateVal));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void deleteTransaction(int id) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_NAME, COL_ID + " = ?", new String[] { String.valueOf(id) });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getTotalIncome() {
        return getMonthlySumByType("income");
    }

    public double getTotalExpense() {
        return getMonthlySumByType("expense");
    }

    public double getMonthlyExpense() {
        return getMonthlySumByType("expense");
    }

    public double getMonthlyIncome() {
        return getMonthlySumByType("income");
    }

    private double getMonthlySumByType(String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.CHINA).format(new Date());
        double total = 0;
        try (Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COL_AMOUNT + ") FROM " + TABLE_NAME + " WHERE " + COL_TYPE + " = ? AND " + COL_DATE
                        + " LIKE ?",
                new String[] { type, currentMonth + "%" })) {
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    public Map<String, DailyTotal> getMonthlyDailySummaries(String yearMonth) {
        Map<String, DailyTotal> summaries = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COL_DATE + ", " +
                "SUM(CASE WHEN " + COL_TYPE + " = 'income' THEN " + COL_AMOUNT + " ELSE 0 END), " +
                "SUM(CASE WHEN " + COL_TYPE + " = 'expense' THEN " + COL_AMOUNT + " ELSE 0 END) " +
                "FROM " + TABLE_NAME + " WHERE " + COL_DATE + " LIKE ? GROUP BY " + COL_DATE;

        try (Cursor cursor = db.rawQuery(query, new String[] { yearMonth + "%" })) {
            if (cursor.moveToFirst()) {
                do {
                    String date = cursor.getString(0);
                    double income = cursor.getDouble(1);
                    double expense = cursor.getDouble(2);
                    summaries.put(date, new DailyTotal(date, income, expense));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return summaries;
    }

    public List<CategoryStat> getCategoryStats() {
        List<CategoryStat> stats = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        double totalExpense = getMonthlyExpense();
        if (totalExpense == 0)
            return stats;

        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.CHINA).format(new Date());

        String query = "SELECT " +
                "CASE WHEN instr(" + COL_CATEGORY + ", '-') > 0 " +
                "THEN substr(" + COL_CATEGORY + ", 1, instr(" + COL_CATEGORY + ", '-') - 1) " +
                "ELSE " + COL_CATEGORY + " END as main_category, " +
                "SUM(" + COL_AMOUNT + ") as total " +
                "FROM " + TABLE_NAME +
                " WHERE " + COL_TYPE + " = 'expense' AND " + COL_DATE + " LIKE ?" +
                " GROUP BY main_category ORDER BY total DESC";

        try (Cursor cursor = db.rawQuery(query, new String[] { currentMonth + "%" })) {
            if (cursor.moveToFirst()) {
                do {
                    String category = cursor.getString(0);
                    double amount = cursor.getDouble(1);
                    double percentage = (amount / totalExpense) * 100;
                    stats.add(new CategoryStat(category, amount, percentage));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stats;
    }

    // ==================== Diary CRUD Operations ====================

    public boolean addDiaryEntry(DiaryEntry entry) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(DIARY_TITLE, entry.getTitle());
            cv.put(DIARY_CONTENT, entry.getContent());
            cv.put(DIARY_MOOD, entry.getMood());
            cv.put(DIARY_DATE, entry.getDate());

            long result = db.insert(TABLE_DIARY, null, cv);
            return result != -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<DiaryEntry> getAllDiaryEntries() {
        List<DiaryEntry> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_DIARY + " ORDER BY " + DIARY_DATE + " DESC, " + DIARY_ID + " DESC", null)) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(0);
                    String title = cursor.getString(1);
                    String content = cursor.getString(2);
                    String mood = cursor.getString(3);
                    String date = cursor.getString(4);
                    String createdAt = cursor.getString(5);
                    list.add(new DiaryEntry(id, title, content, mood, date, createdAt));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<DiaryEntry> getDiaryEntriesByDate(String date) {
        List<DiaryEntry> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_DIARY + " WHERE " + DIARY_DATE + " = ? ORDER BY " + DIARY_ID + " DESC",
                new String[] { date })) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(0);
                    String title = cursor.getString(1);
                    String content = cursor.getString(2);
                    String mood = cursor.getString(3);
                    String dateVal = cursor.getString(4);
                    String createdAt = cursor.getString(5);
                    list.add(new DiaryEntry(id, title, content, mood, dateVal, createdAt));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateDiaryEntry(DiaryEntry entry) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(DIARY_TITLE, entry.getTitle());
            cv.put(DIARY_CONTENT, entry.getContent());
            cv.put(DIARY_MOOD, entry.getMood());
            cv.put(DIARY_DATE, entry.getDate());

            int result = db.update(TABLE_DIARY, cv, DIARY_ID + " = ?",
                    new String[] { String.valueOf(entry.getId()) });
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void deleteDiaryEntry(int id) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_DIARY, DIARY_ID + " = ?", new String[] { String.valueOf(id) });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getDiaryCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_DIARY, null)) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    // ==================== Todo CRUD Operations (Notion-style Enhanced)
    // ====================

    public boolean addTodoItem(TodoItem item) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(TODO_TITLE, item.getTitle());
            cv.put(TODO_DESCRIPTION, item.getDescription());
            cv.put(TODO_STATUS, item.getStatus());
            cv.put(TODO_PRIORITY, item.getPriority());
            cv.put(TODO_DUE_DATE, item.getDueDate());
            cv.put(TODO_TAGS, item.getTags());
            cv.put(TODO_DATE, item.getDate());
            cv.put(TODO_ASSIGNEE, item.getAssignee());
            cv.put(TODO_ATTACHMENT, item.getAttachmentPath());

            long result = db.insert(TABLE_TODO, null, cv);
            return result != -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<TodoItem> getAllTodoItems() {
        List<TodoItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_TODO + " ORDER BY " +
                        "CASE " + TODO_STATUS
                        + " WHEN 'in_progress' THEN 1 WHEN 'not_started' THEN 2 WHEN 'completed' THEN 3 END, " +
                        "CASE " + TODO_PRIORITY
                        + " WHEN 'high' THEN 1 WHEN 'medium' THEN 2 WHEN 'low' THEN 3 ELSE 4 END, " +
                        TODO_CREATED_AT + " DESC",
                null)) {
            if (cursor.moveToFirst()) {
                do {
                    list.add(parseTodoItemV2(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<TodoItem> getTodoItemsByStatus(String status) {
        List<TodoItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_TODO + " WHERE " + TODO_STATUS + " = ? ORDER BY " +
                        "CASE " + TODO_PRIORITY
                        + " WHEN 'high' THEN 1 WHEN 'medium' THEN 2 WHEN 'low' THEN 3 ELSE 4 END, " +
                        TODO_CREATED_AT + " DESC",
                new String[] { status })) {
            if (cursor.moveToFirst()) {
                do {
                    list.add(parseTodoItemV2(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<TodoItem> getTodayTodoItems() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date());
        List<TodoItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_TODO + " WHERE " + TODO_DATE + " = ? OR " + TODO_DUE_DATE + " = ? ORDER BY " +
                        "CASE " + TODO_STATUS
                        + " WHEN 'in_progress' THEN 1 WHEN 'not_started' THEN 2 WHEN 'completed' THEN 3 END, " +
                        "CASE " + TODO_PRIORITY
                        + " WHEN 'high' THEN 1 WHEN 'medium' THEN 2 WHEN 'low' THEN 3 ELSE 4 END",
                new String[] { today, today })) {
            if (cursor.moveToFirst()) {
                do {
                    list.add(parseTodoItemV2(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<TodoItem> getTodoItemsByDate(String date) {
        List<TodoItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_TODO + " WHERE " + TODO_DATE + " = ? ORDER BY " +
                        "CASE " + TODO_STATUS
                        + " WHEN 'in_progress' THEN 1 WHEN 'not_started' THEN 2 WHEN 'completed' THEN 3 END, " +
                        "CASE " + TODO_PRIORITY
                        + " WHEN 'high' THEN 1 WHEN 'medium' THEN 2 WHEN 'low' THEN 3 ELSE 4 END",
                new String[] { date })) {
            if (cursor.moveToFirst()) {
                do {
                    list.add(parseTodoItemV2(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private TodoItem parseTodoItemV2(Cursor cursor) {
        int id = cursor.getInt(0);
        String title = cursor.getString(1);
        String description = cursor.getString(2);
        String status = cursor.getString(3);
        String priority = cursor.getString(4);
        String dueDate = cursor.getString(5);
        String tags = cursor.getString(6);
        String date = cursor.getString(7);
        String createdAt = cursor.getString(8);

        // Handle potential nulls for new columns during migration/older rows
        String assignee = null;
        String attachment = null;

        try {
            int assigneeIdx = cursor.getColumnIndex(TODO_ASSIGNEE);
            if (assigneeIdx != -1)
                assignee = cursor.getString(assigneeIdx);

            int attachmentIdx = cursor.getColumnIndex(TODO_ATTACHMENT);
            if (attachmentIdx != -1)
                attachment = cursor.getString(attachmentIdx);
        } catch (Exception e) {
            // ignore
        }

        return new TodoItem(id, title, description, status, priority, dueDate, tags, date, createdAt, assignee,
                attachment);
    }

    public boolean updateTodoItem(TodoItem item) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(TODO_TITLE, item.getTitle());
            cv.put(TODO_DESCRIPTION, item.getDescription());
            cv.put(TODO_STATUS, item.getStatus());
            cv.put(TODO_PRIORITY, item.getPriority());
            cv.put(TODO_DUE_DATE, item.getDueDate());
            cv.put(TODO_TAGS, item.getTags());
            cv.put(TODO_DATE, item.getDate());
            cv.put(TODO_ASSIGNEE, item.getAssignee());
            cv.put(TODO_ATTACHMENT, item.getAttachmentPath());

            int result = db.update(TABLE_TODO, cv, TODO_ID + " = ?",
                    new String[] { String.valueOf(item.getId()) });
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTodoStatus(int id, String status) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(TODO_STATUS, status);

            int result = db.update(TABLE_TODO, cv, TODO_ID + " = ?",
                    new String[] { String.valueOf(id) });
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Legacy compatibility
    public boolean toggleTodoCompleted(int id, boolean completed) {
        return updateTodoStatus(id, completed ? TodoItem.STATUS_COMPLETED : TodoItem.STATUS_NOT_STARTED);
    }

    public void deleteTodoItem(int id) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_TODO, TODO_ID + " = ?", new String[] { String.valueOf(id) });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getTodoCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TODO, null)) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public int getPendingTodoCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;
        try (Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_TODO + " WHERE " + TODO_STATUS + " != 'completed'", null)) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public int getTodoCountByStatus(String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;
        try (Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_TODO + " WHERE " + TODO_STATUS + " = ?",
                new String[] { status })) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }
}
