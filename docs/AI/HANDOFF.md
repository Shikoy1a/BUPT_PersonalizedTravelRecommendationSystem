# HANDOFF LOG

> 用途：跨会话、跨 AI 的最小必要交接记录。
> 规则：每次开发结束后追加，不要覆盖历史；已解决的同类问题应合并为结果导向记录；每条记录需标注负责人（git 用户）。

## 2026-03-29（主内容收窄 / 顶栏四角圆角 / Home 双滚动条）
### 已完成
- 非 Home 路由：`.es-main-inner` 设为 `max-width: min(1040px, 86vw)` 居中，`.es-main` 使用 `clamp` 左右留白，两侧露出背景；Home 仍走 `es-main--flush`，全宽英雄区不变。
- `.es-nav` 由仅下圆角改为整体 `border-radius: 20px`，`.nav-items` 改为四角 `border-radius: 14px`；顶栏增加水平内边距避免贴边。
- 双滚动条：`html`/`body`/`#app` 去掉 `height:100%` 链，纵向仅 `html` 滚动；`.es-app` 使用 `flex` 列布局且 `overflow-y: visible`，避免壳层再生成滚动条；`.es-home` 用 `100dvh/100vh - 128px` 控制最小高度。

### 验证
- `frontend` 下 `npm run build` 通过。

### 负责人
- Ryemon

## 2026-03-29（全局毛玻璃：分区透明度）
### 已完成
- `premium.css`：定义 `--glass-page` / `--glass-card` / `--glass-input` / `--glass-subtle` 等分层变量，统一 `backdrop-filter` + `saturate`；`el-card`、输入框、标签、次要按钮、抽屉、对话框、分页、下拉浮层改为毛玻璃而非实色白底。
- `explorescape.css`：顶栏 `.es-nav`、主内容 `.es-panel-page` 使用不同白透明度与模糊强度。
- `global.css`：`.glass` 与主题变量对齐。
- `HomeView` 景区卡片、`DiaryListView` 顶栏/搜索条/日记卡片/标签激活态、`Login`/`Register` 侧栏 `hero` 同步为毛玻璃层次。

### 验证
- `frontend` 下 `npm run build` 通过。

### 负责人
- Ryemon

## 2026-03-29（ExploreScape 五张背景图：public 路径与引用）
### 已完成
- 约定资源目录：`frontend/public/explorescape/`，无空格文件名 `bac-4.png`、`bac-3.png`、`bac-2.png`、`bac-2-2.png`、`bac-1.png`；`README.txt` 说明与原版 `bac 4.png` 等对应关系。
- `explorescape.css` 中 `.es-app` 以 `bac-4.png` 为全页底层背景并保留位移动画；`HomePageView` 四层前景与参考站一致并增加中景 `bac-2`。
- 仓库内已放入 1×1 透明 PNG 占位以便 `npm run build` 通过；本地开发请用真实图层 **覆盖同名文件**。
- 首页 `img` 使用 `import.meta.env.BASE_URL + 'explorescape/...'` 拼接，避免打包器将 `/explorescape/...` 当模块解析失败。

### 验证
- `frontend` 下 `npm run build` 通过。

### 负责人
- Ryemon

## 2026-03-29（前端 UI 对齐 ExploreScape）
### 会话目标
- 以 `ExploreScape-Travel-website-main` 的静态 HTML/CSS 为视觉参考改造 `frontend`；保留既有路由、鉴权与后端 API 联调能力。

### 已完成
- 顶栏玻璃导航（文案与参考一致）：Home / About / Reviews / Gallery / Contacts，分别映射 `/home`、`/about`、`/diary`、`/recommend`（含景区详情）、`/profile` 或未登录 `/login`；次级导航保留 推荐 / 路线 / 设施 / 美食 / 管理（管理员）。
- `/home`：英雄区标题与 Kerala 段落、CTA「Explore More」跳转 `/recommend`、装饰层与左右箭头（Font Awesome）；无原站图片时用渐变占位。
- 全局：`Inter` + Font Awesome CDN、`premium.css` 主色 `#16423c`、新增 `explorescape.css`；登录/注册/404 使用 `es-auth-page` 与主壳一致的深色渐变。
- `public/explorescape/README.txt` 说明可选拷贝原站背景图文件名。

### 验证
- `frontend` 目录执行 `npm run build` 通过。

### 变更文件
- `frontend/index.html`、`frontend/src/main.ts`、`frontend/src/router/index.ts`
- `frontend/src/styles/global.css`、`premium.css`、`explorescape.css`
- `frontend/src/layouts/AppLayout.vue`、`frontend/src/views/HomePageView.vue`、`AboutView.vue`、`NotFoundView.vue`、`auth/LoginView.vue`、`auth/RegisterView.vue`
- `frontend/public/explorescape/README.txt`

### 负责人
- Ryemon

## 2026-03-29（日记列表顶部标签：随用户兴趣）
### 已完成
- `DiaryListView`：固定「历史/古镇/…」改为 `apiGetInterest()` 拉取兴趣，按权重降序生成 chips；「推荐」固定为首项。
- 筛选用规范键与目的地景区标签 `normalizeInterestKey` 对齐；未登录仅「推荐」；兴趣变更后 `watch` 刷新。
- 排除 `isExcludedTagPickerKey`（如普通景区）。

### 变更文件
- `frontend/src/views/diary/DiaryListView.vue`

## 2026-03-29（个人中心快速添加 + 排除「普通景区」）
### 已完成
- `ProfileView`：「快速添加常用标签」改为 `apiTagsList()`（与首页同源），启动时 `loadTagCatalog`。
- `interestTags.isExcludedTagPickerKey`：下拉统一排除展示名为「普通景区」的项；`HomeView` 的合并标签同步过滤。
- `TagServiceImpl`：`GET /api/tags` 侧过滤 `name` 为「普通景区」的标签行。

### 变更文件
- `frontend/src/lib/interestTags.ts`、`frontend/src/views/HomeView.vue`、`frontend/src/views/profile/ProfileView.vue`
- `src/main/java/com/travel/service/impl/TagServiceImpl.java`

## 2026-03-29（个性化标签筛选与展示一致）
### 原因
- 筛选仅用景区关联标签名的子串匹配，未做与兴趣侧一致的 **canonicalize**（中英别名），导致「下拉键」与库里标签名不一致时筛空。
- 首页卡片在 `tags` 为空时用 **景区 type** 展示标签，但筛选未考虑 **type**，造成「看得见、筛不出」。

### 修复
- `RecommendationServiceImpl#containsTagKeyword`：规范键相等 + 原子串匹配；无关联标签时用 `ScenicArea.type` 回退。
- 单测：`RecommendationServiceImplTest` 增补中文/英文关键字与仅 type 场景。

### 变更文件
- `src/main/java/com/travel/service/impl/RecommendationServiceImpl.java`
- `src/test/java/com/travel/service/impl/RecommendationServiceImplTest.java`

## 2026-03-29（首页推荐标签下拉：tags 表）
### 已完成
- 新增 `GET /api/tags`：从 `InMemoryStore` 返回 `tags` 表预加载数据（`TagVO`：id/name/type），匿名可访问。
- `HomeView` 智能推荐标签下拉改为请求 `apiTagsList()`，与库表一致；失败时回退 `COMMON_INTEREST_KEYS`。
- `SecurityConfig`：`GET /api/tags` permitAll。

### 验证
- `mvn -DskipTests compile`、`frontend npm run build` 通过。

### 变更文件
- `src/main/java/com/travel/storage/InMemoryStore.java`
- `src/main/java/com/travel/model/vo/TagVO.java`、`TagService.java`、`TagServiceImpl.java`、`TagController.java`
- `src/main/java/com/travel/security/SecurityConfig.java`
- `frontend/src/lib/api.ts`、`frontend/src/views/HomeView.vue`

## 2026-03-29（用户兴趣持久化）
### 已完成
- `UserServiceImpl` 在 `replaceUserInterests` 之后调用 `UserInterestMapper`：按 `user_id` 删除再插入，使兴趣与权重写入 `user_interests` 表，重启后 `InMemoryDataLoader` 预加载可恢复。
- 覆盖 `updateInterests` 与 `recordEngagement` 两条路径；落库失败时 `warn` 且不抛错，避免无库连接场景下接口 500（不复用方法级 `@Transactional`）。

### 验证
- `mvn test` 通过。

### 变更文件
- `src/main/java/com/travel/service/impl/UserServiceImpl.java`
- `src/test/java/com/travel/service/impl/UserServiceImplTest.java`

## 2026-03-28（仅 dev-seed、不预载 DB）
### 说明（问答，无代码变更）
- 仅用 `dev-seed` JSON、不向内存灌库表数据：设 `app.storage.preload.enabled=false` 且 `app.dev-seed.enabled=true`（见 `InMemoryDataLoader#load`）。
- 若仍连不上 MySQL，Spring/Druid 启动是否失败取决于其它是否访问数据源；`application-dev` 中 `ignore-db-connection-failure` 仅作用于预加载 catch 分支。

## 2026-03-28
### 会话目标
- 建立统一 AI 协作与记忆机制，降低换会话/换模型的上下文丢失。

### 已完成
- 新建 AGENTS 入口与 AI 工作流文档。
- 汇总当前实现状态、差距和风险到 PROJECT_CONTEXT。

### 关键发现
- 内存索引检索结构已存在（Trie + NGram）。
- 路线规划核心算法已实现（图 + Dijkstra）。
- 自动化测试缺失。
- 数据备份恢复/AIGC动画/压缩存储尚未落地。

### 下次优先任务（建议）
1. 建立后端 `src/test` 基础测试骨架，先覆盖 Route/Food/Diary 关键路径。
2. 修复配置安全问题：移除明文数据库密码，改环境变量。
3. 补齐开发日志，保证文档进度和代码进度一致。

### 变更文件
- AGENTS.md
- docs/AI/PROJECT_CONTEXT.md
- docs/AI/WORKFLOW.md
- docs/AI/HANDOFF.md

## 2026-03-28（约束澄清补充）
### 会话目标
- 将“数据库仅持久化、运行时内存处理”与“核心模块禁用 Java 内置集合实现”明确写入项目文档。

### 已完成
- 在需求文档（根目录与 docs 目录）补充课程实现约束与数据库职责约束。
- 在开发规范文档（根目录与 docs 目录）补充“数据结构实现约束（课程要求）”。
- 在技术设计文档（根目录与 docs 目录）补充课程约束说明。
- 在 AI 协作文档（AGENTS/WORKFLOW/PROJECT_CONTEXT）同步新增硬约束。

### 关键决策
- 数据库定位为持久化层，不承担核心检索/匹配/排序/关联计算。
- 课程考核范围内的核心数据结构与算法模块，禁止直接使用 Java 内置集合实现作为最终实现。

### 变更文件
- Requirements Documendation.md
- docs/Requirements/Requirements Documendation.md
- Specification.md
- docs/Development Specification/Specification.md
- Technical Design Document.md
- docs/Tech/Technical Design Document.md
- AGENTS.md
- docs/AI/WORKFLOW.md
- docs/AI/PROJECT_CONTEXT.md
- docs/AI/HANDOFF.md

## 2026-03-28（阶段变更总览）
### 记录目的
- 汇总截至当前会话的全部关键变更，作为对外同步和阶段验收依据。

### 新增文件
- AGENTS.md
- .github/copilot-instructions.md
- CLAUDE.md
- GEMINI.md
- .cursorrules
- docs/AI/PROJECT_CONTEXT.md
- docs/AI/WORKFLOW.md
- docs/AI/HANDOFF.md

### 已修改文件
- Requirements Documendation.md
- docs/Requirements/Requirements Documendation.md
- Specification.md
- docs/Development Specification/Specification.md
- Technical Design Document.md
- docs/Tech/Technical Design Document.md
- AGENTS.md
- docs/AI/PROJECT_CONTEXT.md
- docs/AI/WORKFLOW.md
- docs/AI/HANDOFF.md

### 变更主题归档
- 建立跨会话、跨 AI 的统一协作入口与记忆机制。
- 明确检索约束：数据库仅持久化，运行时采用“预加载 -> 内存索引/数据结构 -> 应用层算法”。
- 明确课程实现约束：核心数据结构与算法模块不得直接使用 Java 内置集合实现，需采用自定义结构（MyList/MyMap/MySet/MyPriorityQueue）。
- 在需求、规范、技术设计、AI 协作文档中完成双份（根目录与 docs）同步。

### 当前状态备注
- 文档层约束已统一。
- 代码层仍存在大量 `java.util` 集合使用，后续需按模块逐步替换并补测试。

## 2026-03-28（项目计划书初稿）
### 会话目标
- 产出课程设计“项目计划书”混合版初稿，突出需求实现主线并降低 AI 章节比重。

### 已完成
- 新建计划书文档：`docs/Project Plan/Project Plan.md`。
- 完成项目计划书主体结构与正文初稿，覆盖目标、范围、约束、WBS、里程碑、质量、风险、交付与验收。
- AI 辅助开发章节采用轻量定位（提效工具），强调项目核心评分仍是需求实现与约束合规。
- 文档结构按 Markdown -> HTML -> Word 迁移友好原则编排。

### 变更文件
- docs/Project Plan/Project Plan.md
- docs/AI/HANDOFF.md

## 2026-03-28（成员分工与数据结构优先级优化）
### 会话目标
- 按真实团队成员信息细化分工，并强化数据结构实现在计划中的优先级与可见度。

### 已完成
- 将成员分工替换为真实信息：陈逸（组长）、程小路，并明确“全栈参与+偏重分工”。
- 将 WBS、RACI、周计划中的负责人与协作关系同步改为真实姓名。
- 新增“数据结构实现优先级”章节，明确 MyList/MyMap/MySet/MyPriorityQueue 的前置实现策略。
- 在周计划（第2-8周）与里程碑退出标准中前置并强化数据结构替换工作。
- 调整部分表达为更接地气、可执行导向，减少空泛表述。

### 变更文件
- docs/Project Plan/Project Plan.md
- docs/AI/HANDOFF.md

## 2026-03-28（项目经理级细化迭代）
### 会话目标
- 在既有全学期计划基础上进一步细化到可执行管理层面，提升专业性与落地性。

### 已完成
- 在计划书中新增 WBS 三级任务包（含 FR 关联、前置依赖、负责人、DoD）。
- 新增 RACI 责任矩阵与固定会议节奏（计划会、复盘会、Gate评审会）。
- 新增关键路径与依赖关系表，明确延误影响与纠偏动作。
- 新增阶段化测试计划明细（进入条件/退出条件/产出）。
- 新增 AI 全流程 SOP、人工确认点、AI相关风险与控制策略。
- 新增 FR-里程碑-交付物追踪表，强化验收可追踪性。

### 变更文件
- docs/Project Plan/Project Plan.md
- docs/AI/HANDOFF.md

## 2026-03-28（项目计划书重构 v1.0）
### 会话目标
- 按真实项目标准重构项目计划书，增强可执行性与落地细节，降低空泛描述。

### 已完成
- 对 `docs/Project Plan/Project Plan.md` 执行推倒重写，升级为可执行版本。
- 增加核心需求清单与优先级、技术栈总览、关键模块实施计划。
- 增加迭代节奏、里程碑退出条件、质量门禁、风险触发信号与升级机制。
- 调整 AI 章节为低比重辅助定位，强调需求实现与工程执行主线。
- 保留 Markdown 到 HTML 再到 Word 的格式迁移规范，确保后续排版可控。

### 变更文件
- docs/Project Plan/Project Plan.md
- docs/AI/HANDOFF.md

## 2026-03-28（全学期计划重构与AI章节修正）
### 会话目标
- 将项目计划改为面向全学期（16周）的详细执行计划，并修正 AI 章节表达偏差。

### 已完成
- 将项目周期改为 16 周全期计划，替换原 6 周短期迭代描述。
- 按课程教学节点建立 Gate 表，明确每个课堂节点对应目标与必交付。
- 重写周计划为第1周到第16周逐周任务清单，明确“何时做什么、输出什么”。
- 增加里程碑退出标准（M1-M4）与可量化判定条件。
- 重写 AI 章节为中等篇幅：明确 AI 在需求、设计、开发、测试、交付全流程参与方式，并补充治理机制与效果度量。

### 变更文件
- docs/Project Plan/Project Plan.md
- docs/AI/HANDOFF.md

## 2026-03-28（分工均衡与交叉协作修订）
### 会话目标
- 将项目分工从“单人负责”改为“共同执行、各有侧重、平均贡献”的表达方式。

### 已完成
- 重写 WBS 责任字段：由单一“负责人”改为“主责+协作”，并在任务层面轮换主责，体现两人共同投入。
- 调整角色职责描述：明确“组长负责节奏协调，不代表主要功劳归属”。
- 重写 RACI 矩阵：核心工作项统一为双人共同执行与共同负责，避免割裂分工。
- 保留“侧重”而非“独占”：陈逸偏算法与架构收敛，程小路偏前端体验与测试闭环。

### 变更文件
- docs/Project Plan/Project Plan.md
- docs/AI/HANDOFF.md

## 2026-03-28（计划书去标注与HTML排版导出）
### 会话目标
- 移除计划书中的编号型标注信息，输出更易读的 HTML 版本。

### 已完成
- 清理计划书中的编号标注字段与空标注列（如需求编号/WBS编码/Gate/风险编号等）。
- 修复自动清理后出现的残留异常文本，统一为可读表达。
- 重新生成带样式的 HTML 文档，优化标题层次、表格样式、移动端阅读与整体视觉排版。

### 变更文件
- docs/Project Plan/Project Plan.md
- docs/Project Plan/Project Plan.html
- docs/AI/HANDOFF.md

## 2026-03-28（新增 git-commit-mentor skill）
### 会话目标
- 将 `minEngine` 仓库中的 `git-commit-mentor` 协作技能引入当前项目，统一提交信息生成流程。

### 已完成
- 新增技能文件：`skills/git-commit-mentor/SKILL.md`。
- 在 Copilot 指令入口中增加该技能触发约定与安全约束（未经用户批准不得执行 `git commit`）。

### 变更文件
- skills/git-commit-mentor/SKILL.md
- .github/copilot-instructions.md
- docs/AI/HANDOFF.md

## 2026-03-28（自定义数据结构框架首版落地）
### 会话目标
- 先盘点项目中 `java.util` 数据结构使用现状，再提供同名接口/实现的自定义数据结构包，支持后续通过替换 import 解耦。

### 已完成
- 完成 `java.util` 使用清单扫描（去重）：ArrayList、Collections、Comparator、Date、HashMap、Iterator、List、Map、NoSuchElementException、Objects、PriorityQueue、Set。
- 新增自定义数据结构包 `com.travel.ds`，接口名与 Java 习惯保持一致：`Collection`、`List`、`Set`、`Map`、`Queue`、`Deque`。
- 新增实现类：`ArrayList`、`LinkedList`、`HashMap`、`HashSet`、`PriorityQueue`、`ArrayDeque`。
- 新增工具类：`Collections`（含 `sort/reverse/swap`）。
- 通过编辑器诊断验证新增目录无语法错误（当前环境无 Maven 命令，未执行 `mvn compile`）。

### 变更文件
- src/main/java/com/travel/ds/Collection.java
- src/main/java/com/travel/ds/List.java
- src/main/java/com/travel/ds/Set.java
- src/main/java/com/travel/ds/Map.java
- src/main/java/com/travel/ds/Queue.java
- src/main/java/com/travel/ds/Deque.java
- src/main/java/com/travel/ds/ArrayList.java
- src/main/java/com/travel/ds/LinkedList.java
- src/main/java/com/travel/ds/HashMap.java
- src/main/java/com/travel/ds/HashSet.java
- src/main/java/com/travel/ds/PriorityQueue.java
- src/main/java/com/travel/ds/ArrayDeque.java
- src/main/java/com/travel/ds/Collections.java
- docs/AI/HANDOFF.md

## 2026-03-28（数据库连接失败忽略开关）
### 会话目标
- 支持在非数据库开发场景下启动应用：数据库连接失败时可按开关忽略并继续运行。

### 已完成
- 在 `InMemoryDataLoader` 增加配置开关：
  - `app.storage.preload.enabled`（是否执行启动预加载，默认 `true`）
  - `app.debug.ignore-db-connection-failure`（数据库连接失败时是否忽略，默认 `false`）
- 在 `application.yml` 新增上述配置项与说明注释。
- 实测结果：
  - 依赖解析成功（`mvn dependency:resolve` -> `BUILD SUCCESS`）
  - 编译成功（`mvn -DskipTests compile` -> `BUILD SUCCESS`）
  - 启动时设置 `APP_DEBUG_IGNORE_DB_CONNECTION_FAILURE=true`，数据库连接失败被告警后跳过预加载，应用仍成功启动（Tomcat 8080 started）。

### 变更文件
- src/main/java/com/travel/storage/InMemoryDataLoader.java
- src/main/resources/application.yml
- docs/AI/HANDOFF.md

## 2026-03-28（开发环境默认关闭鉴权）
### 会话目标
- 增加用户登录鉴权开关，并在开发环境默认关闭鉴权，提升无数据库场景下的开发与调试效率。

### 已完成
- 在安全配置中增加开关：`app.security.auth-enabled`（默认 `true`）。
- 当开关为 `false` 时，放行全部请求并跳过 JWT 过滤链注入。
- 在主配置中设置默认值为开启鉴权。
- 新增 `application-dev.yml`，在 `dev` profile 下默认将鉴权开关设为关闭。
- 验证结果：
  - `mvn -DskipTests compile` 编译成功。
  - 使用 `SPRING_PROFILES_ACTIVE=dev` 启动时，日志出现 `Security auth is disabled by config: app.security.auth-enabled=false`。
  - 同时在数据库不可达场景下，应用仍可启动并监听 8080 端口（依赖此前数据库失败忽略开关）。

### 变更文件
- src/main/java/com/travel/security/SecurityConfig.java
- src/main/resources/application.yml
- src/main/resources/application-dev.yml
- docs/AI/HANDOFF.md

## 2026-03-28（开发种子数据自动加载）
### 会话目标
- 在开发环境数据库不可用时，自动注入 5~10 条可测种子数据，保障前端联调与检索/推荐/路径功能验证。

### 已完成
- 新增 `DevSeedDataLoader`，覆盖以下内存数据：用户、兴趣、景区、标签及关联、建筑、道路、设施、餐厅、美食、游记、游记目的地、评论。
- 在 `InMemoryDataLoader` 中接入种子数据回填触发点：
  - 预加载显式关闭时触发
  - 数据库连接失败且开启忽略开关时触发
- 新增配置：
  - `app.dev-seed.enabled`（全局默认 `false`）
  - 在 `application-dev.yml` 中默认开启 `app.dev-seed.enabled=true`
  - 在 `application-dev.yml` 中默认开启 `app.debug.ignore-db-connection-failure=true`
- 验证结果：
  - `mvn -DskipTests compile` -> BUILD SUCCESS
  - `mvn spring-boot:run -Dspring-boot.run.profiles=dev` 启动日志出现 `Dev seed data loaded successfully`。
  - 接口验证通过：
    - `/api/recommendation/hot?page=1&size=3` 返回 5 条景区总量中的分页结果
    - `/api/food/search?areaId=201&page=1&size=5` 返回种子美食数据
    - `/api/route/map-data?areaId=201` 返回节点与边数据

### 变更文件
- src/main/java/com/travel/storage/DevSeedDataLoader.java
- src/main/java/com/travel/storage/InMemoryDataLoader.java
- src/main/resources/application.yml
- src/main/resources/application-dev.yml
- docs/AI/HANDOFF.md

## 2026-03-28（开发种子数据改造为 JSON 文件驱动）
### 会话目标
- 将 `DevSeedDataLoader` 中写死测试数据迁移为资源目录 JSON 文件，并补齐全量数据类型，确保无库开发可覆盖核心联调场景。

### 已完成
- 重构 `DevSeedDataLoader`：改为从 `app.dev-seed.path` 指定目录读取 JSON 并注入内存仓库。
- 新增 13 个 seed 文件（全量覆盖）：
  - `users`、`user_interests`、`scenic_areas`、`tags`、`scenic_area_tags`
  - `buildings`、`roads`、`facilities`、`restaurants`、`foods`
  - `diaries`、`diary_destinations`、`comments`
- 保持原有回退触发策略不变：数据库预加载失败时（dev 下开启忽略）自动加载种子数据。
- 新增配置：`app.dev-seed.path: classpath:dev-seed`。

### 验证结果
- `mvn -DskipTests compile` -> BUILD SUCCESS。
- `mvn spring-boot:run -Dspring-boot.run.profiles=dev` 启动日志显示：
  - `Dev seed data loaded from JSON path classpath:dev-seed successfully`
  - 已加载计数：users=6, scenicAreas=6, facilities=7, foods=8, diaries=6, comments=8。
- 接口验证通过：
  - `/api/recommendation/hot?page=1&size=10`
  - `/api/diary?page=1&size=10`
  - `/api/facility/search?limit=20`

### 变更文件
- src/main/java/com/travel/storage/DevSeedDataLoader.java
- src/main/resources/application.yml
- src/main/resources/dev-seed/users.json
- src/main/resources/dev-seed/user_interests.json
- src/main/resources/dev-seed/scenic_areas.json
- src/main/resources/dev-seed/tags.json
- src/main/resources/dev-seed/scenic_area_tags.json
- src/main/resources/dev-seed/buildings.json
- src/main/resources/dev-seed/roads.json
- src/main/resources/dev-seed/facilities.json
- src/main/resources/dev-seed/restaurants.json
- src/main/resources/dev-seed/foods.json
- src/main/resources/dev-seed/diaries.json
- src/main/resources/dev-seed/diary_destinations.json
- src/main/resources/dev-seed/comments.json
- docs/AI/HANDOFF.md

## 2026-03-28（自定义数据结构可用性与 java.util 一致性验证）
### 会话目标
- 验证 `com.travel.ds` 全部自定义数据结构可用，并与 `java.util` 对应容器在核心行为上保持一致。

### 已完成
- 修正原有基线测试中 `ArrayList#indexOf` 的错误断言。
- 新增对照测试文件 `CustomDataStructuresParityTest`，覆盖：
  - `ArrayList`、`LinkedList`、`HashMap`、`HashSet`、`PriorityQueue`、`ArrayDeque`、`Collections`
  - 核心行为：增删改查、空值处理、越界/空容器异常、迭代与顺序、排序/反转/交换效果
  - 对照方式：与 `java.util` 同操作、同断言结果
- 修复实现差异：`PriorityQueue#offer` 增加 null 拒绝（抛 `NullPointerException`），对齐 `java.util.PriorityQueue`。

### 验证结果
- 执行：`mvn "-Dtest=CustomDataStructuresTest,CustomDataStructuresParityTest" test -q`
- 结果：`TESTS_OK`（全部通过）。

### 变更文件
- src/main/java/com/travel/ds/PriorityQueue.java
- src/test/java/com/travel/ds/CustomDataStructuresTest.java
- src/test/java/com/travel/ds/CustomDataStructuresParityTest.java
- docs/AI/HANDOFF.md

## 2026-03-28（提交信息换行格式修复与 commit-mentor 强化）
### 会话目标
- 修复最近一次提交正文中 `\\n` 字面量导致的格式问题，并增强 `git-commit-mentor` 规则避免复发。

### 已完成
- 准备对最近一次提交执行 amend，改为真实多行提交正文（不含 `\\n` 字面量）。
- 更新 `skills/git-commit-mentor/SKILL.md`：新增提交执行硬规则，强制使用真实多行文本并优先 `git commit -F`。
- 更新 `.github/copilot-instructions.md`：补充仓库级禁止 `\\n` 字面量规则。

### 变更文件
- skills/git-commit-mentor/SKILL.md
- .github/copilot-instructions.md
- docs/AI/HANDOFF.md

## 2026-03-28（阶段合并摘要：推荐链路与体验优化）
### 负责人
- Max1122Chen（max1122chen@126.com）

### 新增/完成功能
- 兴趣权重闭环：统一前后端字段为 `type/weight`，支持“标签:权重”输入与服务端校验。
- 种子账号登录：dev-seed 明文密码启动时自动 BCrypt 编码，开发账号可直接登录。
- 联调稳定性修复：美食推荐响应兼容、设施定位拒绝降级、日记详情无库环境可用。
- 体验优化：后端 devtools 热更新接入，美食页面去技术 ID 化并增强名称展示。

### 验证结果
- 关键接口与页面回归通过（登录、推荐、设施、日记详情、兴趣保存/回显）。
- 前后端构建/编译通过（含 `mvn -DskipTests compile`、`npm.cmd run build`）。

### 主要变更文件（合并）
- frontend/src/lib/api.ts
- frontend/src/views/profile/ProfileView.vue
- frontend/src/views/facility/FacilityView.vue
- frontend/src/views/food/FoodView.vue
- frontend/src/views/food/FoodDetailView.vue
- src/main/java/com/travel/storage/DevSeedDataLoader.java
- src/main/java/com/travel/service/impl/UserServiceImpl.java
- src/main/java/com/travel/service/impl/DiaryServiceImpl.java
- src/main/java/com/travel/model/dto/auth/InterestItemRequest.java
- src/main/java/com/travel/model/dto/auth/UpdateInterestRequest.java
- pom.xml
- docs/AI/HANDOFF.md

## 2026-03-28（功能合并记录：美食详情可读化 + 兴趣回显）
### 负责人
- Max1122Chen（max1122chen@126.com）

### 新增/完成功能
- 美食详情支持展示真实“餐厅名称/所属景区名称”。
- 用户中心支持兴趣与权重回显（登录后自动读取）。
- 前端兼容 `detail-view` 嵌套响应结构，统一为页面可直接消费的数据模型。

### 验证结果
- `GET /api/auth/interest` 返回 `200`。
- `GET /api/food/detail-view/901` 返回 `200`。
- 前端构建通过：`npm.cmd run build`。

### 变更文件
- src/main/java/com/travel/controller/AuthController.java
- src/main/java/com/travel/controller/FoodController.java
- src/main/java/com/travel/service/FoodService.java
- src/main/java/com/travel/service/UserService.java
- src/main/java/com/travel/service/impl/FoodServiceImpl.java
- src/main/java/com/travel/service/impl/UserServiceImpl.java
- src/main/java/com/travel/model/vo/food/FoodDetailVO.java
- src/main/java/com/travel/model/vo/auth/InterestItemVO.java
- frontend/src/lib/api.ts
- frontend/src/views/food/FoodDetailView.vue
- frontend/src/views/profile/ProfileView.vue
- docs/AI/HANDOFF.md

## 2026-03-28（流程需求新增：HANDOFF 精简与负责人）
### 负责人
- Max1122Chen（max1122chen@126.com）

### 新增规则
- HANDOFF 中同一功能/bug 若已解决，后续记录自动合并为“结果导向”条目。
- 记录默认省略中间排障曲折，仅保留实现结果、验证结论、变更文件。
- 每条 HANDOFF 新增 `负责人` 字段，按当前仓库 git 用户填写。

### 变更文件
- docs/AI/WORKFLOW.md
- docs/AI/HANDOFF.md

## 2026-03-28（美食推荐：算法正确性校验与前端距离对接）
### 负责人
- Max1122Chen（max1122chen@126.com）

### 新增/完成功能
- 推荐算法健壮性增强：权重参数负值会被归零；若三项权重总和非正，自动回退默认权重 `0.3/0.5/0.2`。
- 新增美食推荐算法单测：覆盖评分排序、分页、距离过滤、异常权重回退等核心路径。
- 前端“距离排序”接入浏览器定位：可用时传 `lat/lng` 给后端；不可用时提示并回退景区中心点估算。

### 验证结果
- 后端单测通过：`FoodServiceImplRecommendationTest`（`passed=4, failed=0`）。
- 前端构建通过：`npm.cmd run build`。

### 变更文件
- src/main/java/com/travel/service/impl/FoodServiceImpl.java
- src/test/java/com/travel/service/impl/FoodServiceImplRecommendationTest.java
- frontend/src/views/food/FoodView.vue
- docs/AI/HANDOFF.md

## 2026-03-28（景点推荐验证 + 兴趣可视化升级）
### 负责人
- Max1122Chen（max1122chen@126.com）

### 新增/完成功能
- 扩充景点测试数据：新增多类型景点（nature/history/photo/science/art/culture/food/hiking/night）及对应标签权重关系。
- 景点推荐算法正确性验证：新增 `RecommendationServiceImplTest`，覆盖个性化匹配排序、标签关键字过滤、热度排序。
- 修复推荐稳定性问题：推荐服务对候选集合使用可变副本排序，避免不可变列表触发异常。
- 用户兴趣体验升级：个人中心改为“兴趣行编辑（标签+权重）+ 饼图可视化”，支持常用标签快速添加与实时图表反馈。

### 验证结果
- 后端推荐相关测试通过：`passed=8, failed=0`（含 `RecommendationServiceImplTest` 与 `FoodServiceImplRecommendationTest`）。
- 前端构建通过：`npm.cmd run build`。

### 变更文件
- src/main/resources/dev-seed/scenic_areas.json
- src/main/resources/dev-seed/tags.json
- src/main/resources/dev-seed/scenic_area_tags.json
- src/main/resources/dev-seed/user_interests.json
- src/main/java/com/travel/service/impl/RecommendationServiceImpl.java
- src/test/java/com/travel/service/impl/RecommendationServiceImplTest.java
- frontend/src/views/profile/ProfileView.vue
- docs/AI/HANDOFF.md

## 2026-03-28（行为学习偏好 V1：点赞/收藏驱动兴趣更新）
### 负责人
- Max1122Chen（max1122chen@126.com）

### 新增/完成功能
- 新增行为采集接口：`POST /api/behavior/engage`，支持 `SCENIC/FOOD + LIKE/FAVORITE/VIEW`。
- 新增行为数据结构：`UserBehavior`（内存落地）与 `EngagementRequest`。
- 兴趣学习逻辑落地：行为会按目标标签权重转化为兴趣增量，并与现有兴趣合并（上限 5.0）。
- 前端对接：景点详情与美食详情新增“点赞/收藏”按钮，点击后触发行为上报并提示“已学习偏好”。

### 验证结果
- 后端服务测试通过：`UserServiceImplTest`、`RecommendationServiceImplTest`、`FoodServiceImplRecommendationTest`（总计 `passed=13, failed=0`）。
- 前端构建通过：`npm.cmd run build`。

### 变更文件
- src/main/java/com/travel/model/entity/UserBehavior.java
- src/main/java/com/travel/model/dto/behavior/EngagementRequest.java
- src/main/java/com/travel/controller/BehaviorController.java
- src/main/java/com/travel/storage/InMemoryStore.java
- src/main/java/com/travel/service/UserService.java
- src/main/java/com/travel/service/impl/UserServiceImpl.java
- src/test/java/com/travel/service/impl/UserServiceImplTest.java
- frontend/src/lib/api.ts
- frontend/src/views/scenic/ScenicDetailView.vue
- frontend/src/views/food/FoodDetailView.vue
- docs/AI/HANDOFF.md

## 2026-03-28（问题修复：点赞与兴趣保存 500）
### 负责人
- Max1122Chen（max1122chen@126.com）

### 原因与修复
- 根因：`UserServiceImpl` 的用户侧内存写操作（注册/兴趣更新/行为采集）使用了事务注解，在无数据库连接的 dev-seed 场景下会触发事务取连接并抛 500。
- 修复：移除上述内存操作方法的事务依赖，保留纯内存更新路径。

### 验证结果
- `PUT /api/auth/interest` 返回 `200`。
- `POST /api/behavior/engage` 返回 `200`。
- `GET /api/auth/interest` 返回 `200` 且可读回更新后的兴趣权重。
- 编译/测试通过：`BACKEND_COMPILE_OK`，`UserServiceImplTest passed=5 failed=0`。

### 变更文件
- src/main/java/com/travel/service/impl/UserServiceImpl.java
- docs/AI/HANDOFF.md

## 2026-03-28（兴趣体验修复：中文回显 + 两位小数 + 多标签学习完整增强）
### 负责人
- Max1122Chen（max1122chen@126.com）

### 新增/完成功能
- 兴趣标签中文回显：前端新增兴趣标签映射工具，个人中心与推荐页标签统一显示中文名称。
- 兴趣权重精度统一：手动配置改为两位小数（`step=0.01`），后端保存与行为学习增量统一按两位小数归一化。
- 修复“点赞多标签只增强部分标签”：后端在兴趣更新与行为学习时统一做中英别名归一化（canonical key），并合并同义标签后再增权，确保景区多标签都参与学习。

### 验证结果
- 后端测试通过：`UserServiceImplTest`、`RecommendationServiceImplTest`（`passed=11, failed=0`）。
- 前端构建通过：`npm.cmd run build`。

### 变更文件
- src/main/java/com/travel/service/impl/UserServiceImpl.java
- src/main/java/com/travel/service/impl/RecommendationServiceImpl.java
- src/test/java/com/travel/service/impl/UserServiceImplTest.java
- frontend/src/lib/interestTags.ts
- frontend/src/views/profile/ProfileView.vue
- frontend/src/views/HomeView.vue
- docs/AI/HANDOFF.md

## 2026-03-28（登录态修复：dev 放行模式下仍可识别当前用户）
### 负责人
- Max1122Chen（max1122chen@126.com）

### 原因与修复
- 根因：`app.security.auth-enabled=false` 时仅做了 `permitAll`，未挂载 JWT 过滤器，导致 `SecurityUtil.getCurrentUserId()` 始终为空，前端登录后调用个性化/兴趣接口仍提示“未登录”。
- 修复：在 `SecurityConfig` 的 dev 放行分支中同样注入 `JwtAuthenticationFilter`，实现“放行权限 + 保留登录态解析”。

### 验证结果
- `POST /api/auth/login`（dev 种子账号）返回 token。
- 携带 token 调用：`GET /api/auth/interest` 返回 `200`。
- 携带 token 调用：`GET /api/recommendation/personalized` 返回 `200`。

### 变更文件
- src/main/java/com/travel/security/SecurityConfig.java
- docs/AI/HANDOFF.md

## 2026-03-28（推荐景点改造：从列表输出到真实个性化推荐）
### 负责人
- Max1122Chen（max1122chen@126.com）

### 新增/完成功能
- 修复“个性化推荐看起来像普通列表”的核心问题：增加兴趣/标签归一化与中英别名映射（如“自然”->`nature`、“夜景”->`night`），避免兴趣语言不一致导致匹配分失效。
- 优化推荐打分：提升兴趣匹配在总分中的权重（`0.7*match + 0.2*heat + 0.1*rating`），使结果更体现用户偏好而非仅热门排序。
- 增加推荐可解释性：个性化结果返回 `reason` 字段，展示“匹配了哪个兴趣标签及强度”或热门/高分兜底理由。
- 前端推荐页升级：登录后默认进入“智能推荐”标签页，卡片展示推荐分与推荐理由，区分“热门列表”和“个性化推荐”。

### 验证结果
- 推荐服务测试通过：`RecommendationServiceImplTest passed=5 failed=0`（新增中文兴趣别名命中与推荐理由断言）。
- 编辑器诊断通过：本次改动文件无编译/类型错误。

### 变更文件
- src/main/java/com/travel/service/impl/RecommendationServiceImpl.java
- src/main/java/com/travel/model/vo/recommendation/ScenicAreaRecommendVO.java
- src/test/java/com/travel/service/impl/RecommendationServiceImplTest.java
- frontend/src/views/HomeView.vue
- docs/AI/HANDOFF.md

