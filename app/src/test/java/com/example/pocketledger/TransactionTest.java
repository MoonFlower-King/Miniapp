package com.example.pocketledger;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Transaction 模型类单元测试
 */
public class TransactionTest {

    @Test
    public void testConstructorWithId() {
        Transaction t = new Transaction(1, "expense", 100.50, "餐饮", "午饭", "2024-12-28");

        assertEquals("ID应为1", 1, t.getId());
        assertEquals("类型应为expense", "expense", t.getType());
        assertEquals("金额应为100.50", 100.50, t.getAmount(), 0.01);
        assertEquals("分类应为餐饮", "餐饮", t.getCategory());
        assertEquals("备注应为午饭", "午饭", t.getNote());
        assertEquals("日期应为2024-12-28", "2024-12-28", t.getDate());
    }

    @Test
    public void testConstructorWithoutId() {
        Transaction t = new Transaction("income", 5000.0, "职业收入", "工资", "2024-12-28");

        assertEquals("默认ID应为0", 0, t.getId());
        assertEquals("类型应为income", "income", t.getType());
        assertEquals("金额应为5000.0", 5000.0, t.getAmount(), 0.01);
    }

    @Test
    public void testEmptyNote() {
        Transaction t = new Transaction("expense", 50.0, "交通", "", "2024-12-28");

        assertEquals("空备注应为空字符串", "", t.getNote());
    }

    @Test
    public void testNullNote() {
        Transaction t = new Transaction("expense", 50.0, "交通", null, "2024-12-28");

        assertNull("null备注应为null", t.getNote());
    }

    @Test
    public void testTypeValues() {
        Transaction expense = new Transaction("expense", 10.0, "A", "", "2024-12-28");
        Transaction income = new Transaction("income", 10.0, "A", "", "2024-12-28");

        assertEquals("expense", expense.getType());
        assertEquals("income", income.getType());
    }

    @Test
    public void testCategoryWithSubcategory() {
        // 测试带子分类的分类名（如：餐饮-快餐）
        Transaction t = new Transaction("expense", 25.0, "餐饮-快餐", "麦当劳", "2024-12-28");

        assertEquals("分类应包含子分类", "餐饮-快餐", t.getCategory());
    }

    @Test
    public void testDecimalPrecision() {
        Transaction t = new Transaction("expense", 99.99, "测试", "", "2024-12-28");

        assertEquals("小数精度应保持", 99.99, t.getAmount(), 0.001);
    }

    @Test
    public void testZeroAmount() {
        Transaction t = new Transaction("expense", 0.0, "测试", "", "2024-12-28");

        assertEquals("零金额应正确存储", 0.0, t.getAmount(), 0.001);
    }

    @Test
    public void testNegativeAmount() {
        // 虽然业务上不应该有负金额，但模型应该能存储
        Transaction t = new Transaction("expense", -50.0, "测试", "", "2024-12-28");

        assertEquals("负金额应能存储", -50.0, t.getAmount(), 0.001);
    }
}
