# SoulCapsule · Android 客户端

> 心灵胶囊 Android 端 — 记录心情、回顾足迹、与 AI 伴侣「小旅」倾诉，并获得个性化解压建议。

后端服务文档请参阅：[SoulCapsule_Backend/README.md](../SoulCapsule_Backend/README.md)

---

## 目录

- [项目简介](#项目简介)
- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [环境要求](#环境要求)
- [快速开始](#快速开始)
- [页面说明](#页面说明)
- [网络层](#网络层)
- [配置说明](#配置说明)
- [心情色彩规范](#心情色彩规范)
- [开发规范](#开发规范)
- [常见问题](#常见问题)

---

## 项目简介

**SoulCapsule Android 客户端** 是情绪树洞 App 的用户交互层，通过 Retrofit 与 Spring Boot 后端通信，提供完整的情绪记录、分析与疗愈体验。

| 模块 | 说明 |
|------|------|
| **今日** | 问候语、记录入口、AI 聊天、解压小锦囊 |
| **记录心情** | 四步流程：心情 → 归因 → 情绪 → 日记 + 配图 |
| **足迹** | 历史时间轴、搜索、详情、修改、删除 |
| **我的** | 周/月/年统计、日历圆环、分布条、趋势图 |
| **小旅 AI** | ChatKit 聊天界面，带上下文 |

---

## 功能特性

### 用户
- 注册 / 登录，登录态持久化（`SessionManager` + SharedPreferences）
- 已登录自动跳转首页，未登录拦截「我的」页

### 心情
- 五档心情评分（很好 → 很不好）
- Step 2/3 标签合并写入 `tags` 字段
- 日记正文 + `PickVisualMedia` 选图上传
- 足迹 CRUD、内容关键词搜索
- 详情页展示 AI 治愈回复与配图

### AI
- 小旅聊天（`AiChatActivity` + ChatKit）
- 今日页 AI 解压小锦囊（动态拉取，非假数据）

### 统计
- `MoodRatioRingView` 日历多色圆环
- `MoodTrendView` 心情趋势折线
- `MoodStatsHelper` 周期过滤与分布计算

### 工程
- `BaseActivity` 统一 Loading / 网络错误提示
- Glide 圆角图片加载，`ImageUrlHelper` 适配模拟器地址

---

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Java 11 |
| 最低 SDK | API 24 |
| 目标 SDK | API 36 |
| UI | Material 3、ConstraintLayout、RecyclerView |
| 网络 | Retrofit 2.11、OkHttp 4.12、Gson |
| 图片 | Glide 4.16 |
| 聊天 UI | ChatKit 0.4.1 |
| 本地库 | Room 2.6（遗留，账号已走后端） |

---

## 项目结构

```
SoulCapsule/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/finalwork/soulcapsule/
│       │   ├── config/
│       │   │   └── AppConfig.java          # BASE_URL、超时配置
│       │   ├── dto/                        # ApiResponse、MoodRequest 等
│       │   ├── network/
│       │   │   ├── ApiService.java         # Retrofit 接口定义
│       │   │   ├── RetrofitClient.java     # 单例客户端
│       │   │   └── NetworkErrorHandler.java
│       │   ├── util/
│       │   │   ├── SessionManager.java     # 登录态
│       │   │   ├── MoodColorConstants.java # 五档颜色
│       │   │   ├── MoodStatsHelper.java    # 统计聚合
│       │   │   ├── MoodDateUtils.java
│       │   │   ├── ImageUrlHelper.java
│       │   │   └── ImageLoaderHelper.java
│       │   ├── ui/
│       │   │   └── CustomLoadingDialog.java
│       │   ├── BaseActivity.java
│       │   ├── LoginActivity.java
│       │   ├── RegisterActivity.java
│       │   ├── MainActivity.java
│       │   ├── RecordMoodActivity.java
│       │   ├── FootprintActivity.java
│       │   ├── MoodDetailActivity.java
│       │   ├── EditMoodActivity.java
│       │   ├── ProfileActivity.java
│       │   ├── AiChatActivity.java
│       │   ├── MoodRatioRingView.java
│       │   └── MoodTrendView.java
│       └── res/
│           ├── layout/                     # 17 个布局文件
│           ├── values/                       # strings、colors、themes
│           └── drawable/
├── gradle/
└── build.gradle
```

---

## 环境要求

| 工具 | 版本 |
|------|------|
| Android Studio | 最新稳定版 |
| Android SDK | 36 |
| JDK | 11+ |
| 后端服务 | 需先启动 [SoulCapsule_Backend](../SoulCapsule_Backend/README.md) |

---

## 快速开始

### 1. 启动后端

请先按后端 README 完成 MySQL 初始化并启动服务（默认 `http://localhost:8080`）。

### 2. 配置 API 地址

编辑 `app/src/main/java/com/finalwork/soulcapsule/config/AppConfig.java`：

```java
// 模拟器访问本机
public static final String BASE_URL = "http://10.0.2.2:8080/";

// 真机调试（改为电脑局域网 IP）
// public static final String BASE_URL = "http://192.168.1.100:8080/";
```

### 3. 运行

1. Android Studio 打开 **本目录**（`SoulCapsule`）
2. Sync Gradle
3. 连接模拟器或真机，Run `app`

### 4. 验证

1. 注册并登录  
2. 记录一条心情（可选上传图片）  
3. 在「足迹」查看列表与 AI 回复  
4. 在「今日」查看解压小锦囊  
5. 进入「小旅」聊天  

---

## 页面说明

| Activity | 功能 | 调用的 API |
|----------|------|------------|
| `LoginActivity` | 登录、已登录跳转 | `POST /api/user/login` |
| `RegisterActivity` | 注册 | `POST /api/user/register` |
| `MainActivity` | 今日首页、解压小锦囊 | `GET /api/ai/tips` |
| `RecordMoodActivity` | 四步记录 + 图片上传 | `POST /api/upload`、`POST /api/mood/add` |
| `FootprintActivity` | 足迹列表、搜索 | `GET /api/mood/list` |
| `MoodDetailActivity` | 详情、删除 | `DELETE /api/mood/delete/{id}` |
| `EditMoodActivity` | 修改心情 | `POST /api/mood/update` |
| `ProfileActivity` | 统计分析、退出 | `GET /api/mood/list` |
| `AiChatActivity` | 小旅聊天 | `POST /api/chat/send` |

### 底部导航

```
今日 (MainActivity)  |  足迹 (FootprintActivity)  |  我的 (ProfileActivity)
```

---

## 网络层

### ApiService 接口一览

| 方法 | 路径 |
|------|------|
| `login` | `POST api/user/login` |
| `register` | `POST api/user/register` |
| `addMoodRecord` | `POST api/mood/add` |
| `updateMood` | `POST api/mood/update` |
| `deleteMood` | `DELETE api/mood/delete/{id}` |
| `getMoodList` | `GET api/mood/list?userId=&keyword=` |
| `sendChatMessage` | `POST api/chat/send` |
| `getDecompressionTips` | `GET api/ai/tips?userId=` |
| `uploadImage` | `POST api/upload` |

完整请求/响应格式见 [后端 API 文档](../SoulCapsule_Backend/README.md#api-接口文档)。

### 统一响应

```json
{ "code": 200, "message": "success", "data": { } }
```

### 网络调用规范

需联网的页面继承 `BaseActivity`：

```java
showLoading();
RetrofitClient.getInstance().getApiService()
    .someApi(...)
    .enqueue(new Callback<ApiResponse<T>>() {
        @Override
        public void onResponse(...) {
            hideLoading();
            if (!isActivityAlive()) return;
            // 处理成功
        }
        @Override
        public void onFailure(...) {
            hideLoading();
            if (!isActivityAlive()) return;
            showNetworkError(t);
        }
    });
```

---

## 配置说明

### AppConfig.java

| 常量 | 说明 | 默认值 |
|------|------|--------|
| `BASE_URL` | 后端根地址 | `http://10.0.2.2:8080/` |
| `CONNECT_TIMEOUT_SECONDS` | 连接超时 | 15 |
| `READ_TIMEOUT_SECONDS` | 读取超时 | 15 |
| `WRITE_TIMEOUT_SECONDS` | 写入超时 | 15 |

### 网络环境对照

| 场景 | BASE_URL |
|------|----------|
| 模拟器 → 本机 | `http://10.0.2.2:8080/` |
| 真机 → 局域网 | `http://<电脑IP>:8080/` |

`ImageUrlHelper` 会将后端返回的 `localhost` 自动替换为 `10.0.2.2`，便于模拟器加载图片。

### AndroidManifest 要点

- `INTERNET` 权限
- `usesCleartextTraffic="true"`（开发环境 HTTP）

---

## 心情色彩规范

定义于 `MoodColorConstants.java`，全 App 统一：

| 心情 | Score | Hex |
|------|-------|-----|
| 很好 | 5 | `#FF5252` |
| 好 | 4 | `#FFAB40` |
| 一般 | 3 | `#66BB6A` |
| 不好 | 2 | `#42A5F5` |
| 很不好 | 1 | `#AB47BC` |

用于足迹圆点、统计分布条、日历圆环、趋势图色阶。

---

## 开发规范

- 新页面若需请求网络，继承 `BaseActivity`
- 图片加载统一走 `ImageLoaderHelper`
- 心情分数/颜色统一走 `MoodColorConstants`
- 修改 `BASE_URL` 后需重新编译安装

---

## 常见问题

**无法连接后端？**  
确认后端已启动；模拟器用 `10.0.2.2`，真机用局域网 IP；检查防火墙。

**图片无法显示？**  
确认 `BASE_URL` 与图片 URL 主机一致；模拟器依赖 `ImageUrlHelper` 的 localhost 替换。

**Gradle Sync 失败？**  
确认 SDK 36 已安装；检查 Maven / Google 仓库网络。

**中文输入异常？**  
记录/编辑页 EditText 已配置 `textMultiLine`，若仍有问题请检查输入法设置。

---

## 致谢

- [ChatKit for Android](https://github.com/stfalcon-studio/ChatKit)
- [Glide](https://github.com/bumptech/glide)
- [Retrofit](https://square.github.io/retrofit/)

---

<p align="center"><strong>SoulCapsule Android · 把每一种心情，都珍藏进胶囊里</strong></p>
