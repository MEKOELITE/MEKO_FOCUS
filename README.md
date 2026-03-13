# MEKO FOCUS - 极简主义番茄钟应用

<p align="center">
  <img src="app/src/main/res/drawable/ic_launcher_foreground.png" width="100" />
</p>

<p align="center">
  <a href="https://github.com/MEKOELITE/MEKO_FOCUS/actions">
    <img src="https://github.com/MEKOELITE/MEKO_FOCUS/workflows/Android%20CI/CD/badge.svg" />
  </a>
  <a href="https://kotlinlang.org">
    <img src="https://img.shields.io/badge/Kotlin-1.9.x-blue.svg" />
  </a>
  <a href="https://developer.android.com/studio">
    <img src="https://img.shields.io/badge/Android-SDK%2034-green.svg" />
  </a>
</p>

## 项目概述

MEKO FOCUS 是一个遵循极简主义设计理念的Android番茄钟应用，旨在帮助用户实现深度专注。应用核心理念是"恒久喜悦，恒定志向"。

## 功能特性

- **全能计时体系**: 高度自定义专注、短休、长休时间，支持自动切换
- **多维反馈**: 震动提醒、通知铃声、励志语录显示
- **系统常驻**: ForegroundService + WakeLock 后台持续运行，通知栏显示倒计时
- **极简UI**: 高对比度黑白设计，单页面核心交互
- **数据统计**: 本周/本月专注时长图表，热力图展示

## 技术栈

| 分类 | 技术 |
|------|------|
| **语言** | Kotlin |
| **UI** | Jetpack Compose + Material3 |
| **架构** | MVVM + Clean Architecture |
| **DI** | Hilt |
| **存储** | DataStore + Room |
| **后台** | ForegroundService |
| **测试** | JUnit4 + MockK |

## 项目结构

```
com.meko.focus/
├── data/                      # 数据层
│   ├── datastore/             # DataStore 偏好设置
│   ├── local/                 # Room 数据库
│   └── repository/            # Repository 实现
├── domain/                    # 领域层
│   ├── model/                 # 数据模型
│   └── repository/            # Repository 接口
├── presentation/              # 表现层
│   ├── component/             # Compose 组件
│   ├── navigation/            # 导航
│   ├── screen/                # 屏幕
│   ├── theme/                 # 主题
│   └── viewmodel/             # ViewModel
├── di/                        # Hilt 模块
├── service/                   # ForegroundService
├── receiver/                  # 广播接收器
└── util/                      # 工具类
```

## 构建

### 环境要求

- Android Studio Flamingo+
- Android SDK 34
- Java 17

### 构建命令

```bash
# 调试构建
./gradlew assembleDebug

# 运行测试
./gradlew test

# 检查代码
./gradlew lint
```

## 测试

项目包含单元测试覆盖核心业务逻辑：

- `TimerSettingsTest` - 设置模型测试
- `FocusSessionTest` - 专注会话测试
- `ChartDataAggregatorTest` - 图表数据聚合测试
- `SessionTypeTest` - 枚举类型测试

运行测试：
```bash
./gradlew test
```

## CI/CD

项目使用 GitHub Actions 实现自动化：

- 自动构建调试 APK
- 自动运行单元测试
- 自动上传构建产物

## 设计规范

### 配色方案

- **浅色主题**: 纯白背景(#FFFFFF)，纯黑文字(#000000)
- **深色主题**: 纯黑背景(#000000)，荧光黄文字
- **状态颜色**: 专注(黑)、短休(绿)、长休(蓝)

## 许可证

MIT License
