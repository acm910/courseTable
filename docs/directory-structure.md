# 目录结构规范（courseTable）

本文档用于约束项目目录与命名，目标是：
- 降低文件耦合度，避免单文件过大
- 提高新增功能时的定位效率
- 保持 `data / domain / feature` 分层边界清晰

## 1. 顶层结构

```text
courseTable/
  app/                      # Android 应用模块
  docs/                     # 项目文档（规范、设计说明）
  gradle/                   # Gradle wrapper 与版本目录
```

## 2. app 模块结构

```text
app/src/main/java/com/example/coursetable/
  app/                      # 应用入口与 App 级组合
  core/                     # 跨 feature 的基础能力（store、工具）
  data/                     # 数据层实现（DB、DAO、Repository impl）
  domain/                   # 业务模型与仓库抽象接口
  feature/                  # 按功能拆分（推荐）
  ui/theme/                 # 全局主题
```

## 3. Feature 目录规范

以课程表功能为例：

```text
feature/course/
  presentation/
    CourseTableViewModel.kt
    CourseTableUiState.kt
    model/                  # 仅 UI 状态模型
    ui/
      CourseTableRoute.kt   # Route：状态收集 + 事件分发
      CourseTableContent.kt # 可选包装层
      CourseTablePrototype.kt
      dialog/               # 弹窗
      table/                # 课表子组件（网格、周选择器、布局计算）
```

### 3.1 `ui` 子目录建议

- `ui/dialog/`：仅放弹窗组件
- `ui/table/`：课表页面专属组件与布局函数
- `ui/component/`：通用可复用组件（跨页面）

## 4. data 与 domain 边界

- `domain/repository/`：仓库接口（抽象）
- `data/repository/impl/`：仓库实现（依赖 DAO）
- `data/local/relation/`：Room 联表查询结果模型

约束：
- `presentation` 不直接依赖 `data/local/*`
- `domain` 不依赖 `presentation`
- `data` 可以依赖 `domain`（实现接口）

## 5. 命名规范

- 文件名与主类型同名，例如：`CourseTableUiState.kt`
- 状态类后缀：`UiState`
- ViewModel 后缀：`ViewModel`
- 弹窗后缀：`Dialog`
- 仓库接口：`CourseRepository`（放在 `domain`）
- 仓库实现：`CourseRepositoryImpl`（放在 `data/repository/impl`）

## 6. 文件大小与拆分建议

- 单个 Kotlin 文件建议不超过 300 行
- 超过 300 行时优先按职责拆分：
  1. 布局计算函数
  2. 子组件（Header/Grid/Picker）
  3. 状态与事件模型
- `Route` 与 `Screen/Content` 分离，防止业务逻辑混入 UI 细节

## 7. 新增功能落地模板

新增 `feature/xxx` 时建议最小目录：

```text
feature/xxx/
  presentation/
    XxxViewModel.kt
    XxxUiState.kt
    model/
    ui/
      XxxRoute.kt
      XxxScreen.kt
```

如果有数据持久化，再按需新增：

- `domain/model`
- `domain/repository`
- `data/repository/impl`
- `data/local/entity|dao|relation`

## 8. 迁移原则

- 一次只做一层目录迁移（先 package，再重命名）
- 每次迁移后必须执行单测/编译
- 迁移优先级：
  1. `presentation` 归档
  2. `core` 抽离
  3. `domain/data` 边界修正

---

如需变更本规范，请同步更新本文档并在 PR 描述中说明原因与影响范围。


