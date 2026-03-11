专注统计图表 (Data Visualization)

光有计时器不够，用户需要看到自己的努力。


功能点：记录每天完成的“番茄数”，用周视图或月视图展示专注时长。

技术挑战：使用 Room 数据库 存储本地数据，配合 MPAndroidChart 库绘制折线图或柱状图。 

📅 第一阶段：底层数据建模 (Data Persistence with Room)

在统计之前，App 必须先“记住”你每次专注的情况。

    1. 定义实体类 (Entity)：

        创建一个 FocusSession 类。

        字段包括：id (主键)、startTime (开始时间戳)、duration (实际专注时长)、isCompleted (是否成功完成)、tag (分类，如：学习、代码、运动)。

    2. 编写访问接口 (DAO)：

        insertSession：每次计时结束存入数据。

        getAllSessions：获取所有记录。

        getSessionsByRange(start, end)：核心方法，用于查询本周或本月的数据。

    3. 数据库管理类 (Database)：

        继承 RoomDatabase，单例模式实现。

📊 第二阶段：统计逻辑处理 (Business Logic)

原始数据是凌乱的，我们需要把它们转化成图表能懂的“语言”。

    1. 数据聚合：

        编写一个工具类，将 FocusSession 列表按日期分组。

        例如：把 10 条“25分钟”的记录合并为“3月11日：250分钟”。

    2. 视图模型 (ViewModel)：

        使用 LiveData 或 StateFlow 包装数据。

        当数据库有新纪录插入时，UI 层的图表能自动刷新（响应式编程）。

🎨 第三阶段：图表视觉呈现 (UI with MPAndroidChart)

这是最拉风的部分，让枯燥的数据变成直观的柱状图。

    1. 引入依赖：在 build.gradle 中添加 jitpack 仓库和 MPAndroidChart 库。

    2. 配置图表样式：

        X轴：显示日期（周一到周日，或 1号到 31号）。

        Y轴：显示专注时长（分钟或小时）。

        交互：点击柱状图弹出 MarkerView，显示具体数值。

    3. 动态加载：

        实现“本周”和“本月”的切换按钮。

        动画效果：加载图表时使用 chart.animateY(1000) 让柱状图“长”出来。