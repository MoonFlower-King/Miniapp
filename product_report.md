# 🎋 知岁 (ZhiSui) - 产品实验报告

**项目名称**: 知岁 (ZhiSui) - 原 PocketLedger
**开发者**: 王庆
**开发日期**: 2025年12月
**版本**: v1.0.0

---

## 1. 产品功能介绍 (Product Features)

**知岁** 取“知晓岁月”之意，是一款集“个人记账”与“高效任务管理”于一体的 Android 效率工具。它通过极简的交互和强大的 AI 辅助，帮助用户更好地规划生活与财务。

### 1.1 核心功能模块

#### 💰 知岁账本 (Smart Ledger)

* **多维记录**: 支持“支出”与“收入”双向记录，内置餐饮、交通、购物等 10+ 种常用分类。
* **可视化报表**: 首页动态展示收支圆环图 (Ring Chart)，配合百分比统计，财务状况一目了然。
* **流水明细**: 清晰的时间轴流水列表，支持长按删除与编辑。

#### ✅ 知岁清单 (Pro Tasks)

* **Notion 风格管理**: 每一条任务都拥有丰富的属性，包括 **状态** (未开始/进行中/完成)、**优先级** (高/中/低)、**截止日期**、**标签**、**负责人** 及 **附件**。
* **灵活视图**: 提供“看板视图”、“今日任务”、“所有任务”等多种筛选维度。
* **可视性控制**: 独创“属性可见性”开关，用户可自定义列表展示哪些字段，保持界面清爽。

#### 🤖 AI 智能助手 (AI Assistant)

* **自然语言记账**: 用户只需输入（或语音输入）“今天中午吃牛肉面花了25元”，AI 自动解析金额、分类和备注并生成账单。
* **智能任务创建**: 输入“周五下午3点交实验报告”，AI 自动解析出任务标题、截止时间和优先级。
* **智能问答**: 集成 DeepSeek 大模型，支持通用问答与建议。

#### 📅 生活日历 (Calendar)

* **时间轴总览**: 在日历视图上查看每天的记账和任务打点。
* **快捷记录**: 点击日历空白处即可快速唤起“记录生活点滴”菜单。

---

## 2. 程序概要设计 (Program Design)

### 2.1 交互设计

应用采用 **Bottom Navigation + ViewPager2** 的主流架构，实现了左右滑动切换三大核心模块（账本、任务、AI）丝滑体验。

* **首页 (Ledger)**: 侧重数据展示，头部为统计面板，底部为流水列表。
* **任务页 (Task)**: 侧重操作效率，顶部为筛选 Tab，右下角悬浮添加按钮。
* **AI 页**: 采用对话式 UI (Chat Interface)，模拟微信/ChatGPT 的自然交互。

### 2.2 数据存储设计

使用 Android 原生 **SQLite** 数据库进行本地离线存储，确保数据隐私与安全。

* **表 1: transactions (账单表)**
  * 字段: `id`, `amount` (金额), `type` (收入/支出), `category` (分类), `note` (备注), `date` (日期)。
* **表 2: todos_v2 (任务表)**
  * 字段: `id`, `title`, `status`, `priority`, `due_date`, `assignee` (负责人), `attachment_path` (附件), `tags`...
  * *亮点*: 随版本迭代进行了数据库版本升级 (v7 -> v8)，实现了无损数据迁移。

---

## 3. 软件架构图 (Software Architecture)

本应用采用了经典的 **MVC (Model-View-Controller)** 分层架构，并结合了 **Event-Driven** 的思想。

```mermaid
graph TD
    subgraph UI_Layer [UI 交互层]
        MainActivity[MainContainerActivity]
        L_Frag[LedgerFragment]
        T_Frag[TaskFragment]
        A_Frag[AiFragment]
        ViewPager[ViewPager2 Adapter]
      
        MainActivity --> ViewPager
        ViewPager --> L_Frag
        ViewPager --> T_Frag
        ViewPager --> A_Frag
    end

    subgraph Logic_Layer [业务逻辑层]
        TodoAdapter[TodoAdapter]
        TransAdapter[TransactionAdapter]
        AiMgr[AiManager (Network)]
        CalendarMgr[CalendarLogic]
    end

    subgraph Data_Layer [数据持久层]
        DB[DatabaseHelper (SQLite)]
        SP[SharedPreferences (Config)]
        DeepSeek[DeepSeek API (Cloud)]
    end

    %% Connections
    L_Frag --> TransAdapter
    L_Frag --> DB
  
    T_Frag --> TodoAdapter
    T_Frag --> DB
    T_Frag --> SP
  
    A_Frag --> AiMgr
    AiMgr --> DeepSeek
```

---

## 4. 技术亮点与实现原理 (Technical Highlights)

### 4.1 智能语义解析 (AI Parsing)

* **原理**: 利用 **OkHttp** 封装网络请求，对接 **DeepSeek V3** 大模型 API。
* **实现**: 系统构建了特定的 System Prompt（提示词），强制 AI 以 JSON 格式返回解析结果。
  * *User*: "买苹果花了 10 块"
  * *AI output*: `{"type": "expense", "amount": 10, "category": "餐饮", "item": "苹果"}`
  * 应用接收 JSON 后，使用 GSON 库反序列化为实体对象，并自动写入 SQLite 数据库。

### 4.2 工业级列表优化 (Industrial UI)

* **原理**: 针对长列表（记账流水、任务清单），摒弃了老旧的 `ListView`，全面采用 **RecyclerView**。
* **实现**:
  - 使用 `ViewHolder` 模式复用视图，减少内存抖动。
  - 任务列表支持 **动态属性显隐**：在 `onBindViewHolder` 中根据配置 (`SharedPreference`) 动态设置 `View.VISIBLE` / `View.GONE`，实现 Notion 般的自定义视图能力。

### 4.3 数据库无缝迁移 (Database Migration)

* **原理**: 随着功能迭代，数据表结构发生变化（如新增“负责人”字段）。直接修改表结构会导致旧版应用崩溃。
* **实现**: 在 `SQLiteOpenHelper.onUpgrade(oldVer, newVer)` 方法中编写 SQL 迁移脚本。
  - 检测到版本从 7 升至 8 时，执行 `ALTER TABLE todos_v2 ADD COLUMN assignee TEXT;`。
  - 这种“热更新”式的数据库升级保证了用户在更新 App 后，原有数据不丢失且能使用新功能。

### 4.4 沉浸式滑动体验

* **原理**: 使用 Google 官方推荐的 **ViewPager2** 组件。
* **实现**: 将 Fragment 作为 ViewPager 的 Item，通过 `FragmentStateAdapter` 进行管理。相比传统的 `Activity` 跳转，这种方式内存开销更小，且支持左右手势滑动切换，体验极佳。
