package com.example.pocketledger;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * 数据库操作单元测试
 * 测试 DatabaseHelper 的核心 CRUD 功能
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = { 28 }, manifest = Config.NONE)
public class DatabaseHelperTest {

    private DatabaseHelper dbHelper;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        dbHelper = new DatabaseHelper(context);
        // 清理测试数据
        clearAllData();
    }

    @After
    public void tearDown() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    private void clearAllData() {
        List<Transaction> all = dbHelper.getAllTransactions();
        for (Transaction t : all) {
            dbHelper.deleteTransaction(t.getId());
        }
    }

    // ==================== 添加交易测试 ====================

    @Test
    public void testAddTransaction_Expense_Success() {
        Transaction expense = new Transaction("expense", 100.50, "餐饮", "午饭", "2024-12-28");
        boolean result = dbHelper.addTransaction(expense);

        assertTrue("添加支出交易应该成功", result);

        List<Transaction> list = dbHelper.getAllTransactions();
        assertEquals("应该有1条记录", 1, list.size());
        assertEquals("类型应为expense", "expense", list.get(0).getType());
        assertEquals("金额应为100.50", 100.50, list.get(0).getAmount(), 0.01);
    }

    @Test
    public void testAddTransaction_Income_Success() {
        Transaction income = new Transaction("income", 5000.00, "职业收入", "工资", "2024-12-28");
        boolean result = dbHelper.addTransaction(income);

        assertTrue("添加收入交易应该成功", result);

        List<Transaction> list = dbHelper.getAllTransactions();
        assertEquals("应该有1条记录", 1, list.size());
        assertEquals("类型应为income", "income", list.get(0).getType());
    }

    @Test
    public void testAddTransaction_EmptyNote() {
        Transaction t = new Transaction("expense", 50.0, "交通", "", "2024-12-28");
        boolean result = dbHelper.addTransaction(t);

        assertTrue("空备注的交易应该能添加成功", result);
    }

    // ==================== 查询测试 ====================

    @Test
    public void testGetAllTransactions_Empty() {
        List<Transaction> list = dbHelper.getAllTransactions();
        assertTrue("空数据库应返回空列表", list.isEmpty());
    }

    @Test
    public void testGetAllTransactions_OrderByDateDesc() {
        dbHelper.addTransaction(new Transaction("expense", 10.0, "A", "", "2024-12-25"));
        dbHelper.addTransaction(new Transaction("expense", 20.0, "B", "", "2024-12-28"));
        dbHelper.addTransaction(new Transaction("expense", 30.0, "C", "", "2024-12-26"));

        List<Transaction> list = dbHelper.getAllTransactions();

        assertEquals("应该有3条记录", 3, list.size());
        assertEquals("最新日期应该排在第一", "2024-12-28", list.get(0).getDate());
        assertEquals("最早日期应该排在最后", "2024-12-25", list.get(2).getDate());
    }

    @Test
    public void testGetTransactionsByDate_Found() {
        dbHelper.addTransaction(new Transaction("expense", 10.0, "A", "", "2024-12-25"));
        dbHelper.addTransaction(new Transaction("expense", 20.0, "B", "", "2024-12-28"));
        dbHelper.addTransaction(new Transaction("expense", 30.0, "C", "", "2024-12-28"));

        List<Transaction> list = dbHelper.getTransactionsByDate("2024-12-28");

        assertEquals("应该找到2条12月28日的记录", 2, list.size());
    }

    @Test
    public void testGetTransactionsByDate_NotFound() {
        dbHelper.addTransaction(new Transaction("expense", 10.0, "A", "", "2024-12-25"));

        List<Transaction> list = dbHelper.getTransactionsByDate("2024-01-01");

        assertTrue("查询不存在的日期应返回空列表", list.isEmpty());
    }

    // ==================== 删除测试 ====================

    @Test
    public void testDeleteTransaction_Success() {
        dbHelper.addTransaction(new Transaction("expense", 100.0, "餐饮", "", "2024-12-28"));
        List<Transaction> before = dbHelper.getAllTransactions();
        int id = before.get(0).getId();

        dbHelper.deleteTransaction(id);

        List<Transaction> after = dbHelper.getAllTransactions();
        assertTrue("删除后应该为空", after.isEmpty());
    }

    @Test
    public void testDeleteTransaction_InvalidId() {
        // 删除不存在的ID不应抛出异常
        dbHelper.deleteTransaction(999999);
        // 测试通过即表示没有异常
    }

    // ==================== 统计测试 ====================

    @Test
    public void testGetMonthlyIncome() {
        // 添加当月收入
        String currentMonth = new java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.CHINA)
                .format(new java.util.Date());
        String testDate = currentMonth + "-15";

        dbHelper.addTransaction(new Transaction("income", 5000.0, "工资", "", testDate));
        dbHelper.addTransaction(new Transaction("income", 2000.0, "奖金", "", testDate));
        dbHelper.addTransaction(new Transaction("expense", 100.0, "餐饮", "", testDate));

        double income = dbHelper.getMonthlyIncome();

        assertEquals("当月收入应为7000", 7000.0, income, 0.01);
    }

    @Test
    public void testGetMonthlyExpense() {
        String currentMonth = new java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.CHINA)
                .format(new java.util.Date());
        String testDate = currentMonth + "-15";

        dbHelper.addTransaction(new Transaction("expense", 100.0, "餐饮", "", testDate));
        dbHelper.addTransaction(new Transaction("expense", 200.0, "交通", "", testDate));
        dbHelper.addTransaction(new Transaction("income", 5000.0, "工资", "", testDate));

        double expense = dbHelper.getMonthlyExpense();

        assertEquals("当月支出应为300", 300.0, expense, 0.01);
    }

    @Test
    public void testGetMonthlyDailySummaries() {
        dbHelper.addTransaction(new Transaction("income", 100.0, "A", "", "2024-12-25"));
        dbHelper.addTransaction(new Transaction("expense", 50.0, "B", "", "2024-12-25"));
        dbHelper.addTransaction(new Transaction("expense", 30.0, "C", "", "2024-12-26"));

        Map<String, DailyTotal> summaries = dbHelper.getMonthlyDailySummaries("2024-12");

        assertEquals("应有2天有记录", 2, summaries.size());
        assertTrue("应包含12月25日", summaries.containsKey("2024-12-25"));

        DailyTotal day25 = summaries.get("2024-12-25");
        assertEquals("25日收入应为100", 100.0, day25.getIncome(), 0.01);
        assertEquals("25日支出应为50", 50.0, day25.getExpense(), 0.01);
    }

    @Test
    public void testGetCategoryStats() {
        String currentMonth = new java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.CHINA)
                .format(new java.util.Date());
        String testDate = currentMonth + "-15";

        dbHelper.addTransaction(new Transaction("expense", 100.0, "餐饮", "", testDate));
        dbHelper.addTransaction(new Transaction("expense", 200.0, "餐饮", "", testDate));
        dbHelper.addTransaction(new Transaction("expense", 100.0, "交通", "", testDate));

        List<CategoryStat> stats = dbHelper.getCategoryStats();

        assertFalse("统计结果不应为空", stats.isEmpty());
        assertEquals("餐饮应排第一", "餐饮", stats.get(0).getCategory());
        assertEquals("餐饮金额应为300", 300.0, stats.get(0).getAmount(), 0.01);
        assertEquals("餐饮占比应为75%", 75.0, stats.get(0).getPercentage(), 0.1);
    }

    // ==================== 边界条件测试 ====================

    @Test
    public void testLargeAmount() {
        Transaction t = new Transaction("income", 999999999.99, "大额", "", "2024-12-28");
        boolean result = dbHelper.addTransaction(t);

        assertTrue("大金额应该能正常保存", result);

        List<Transaction> list = dbHelper.getAllTransactions();
        assertEquals("金额应精确保存", 999999999.99, list.get(0).getAmount(), 0.01);
    }

    @Test
    public void testZeroAmount() {
        Transaction t = new Transaction("expense", 0.0, "测试", "", "2024-12-28");
        boolean result = dbHelper.addTransaction(t);

        assertTrue("零金额应该能保存", result);
    }

    @Test
    public void testSpecialCharactersInNote() {
        Transaction t = new Transaction("expense", 10.0, "测试", "特殊字符'\"<>&测试", "2024-12-28");
        boolean result = dbHelper.addTransaction(t);

        assertTrue("特殊字符备注应该能保存", result);

        List<Transaction> list = dbHelper.getAllTransactions();
        assertEquals("备注应保持原样", "特殊字符'\"<>&测试", list.get(0).getNote());
    }
}
