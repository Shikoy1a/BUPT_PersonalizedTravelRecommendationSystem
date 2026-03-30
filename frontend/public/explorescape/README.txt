将 ExploreScape 原站的 5 张 PNG 放在本目录（与 `frontend` 并列路径：`TravelSystem/frontend/public/explorescape/`）。

推荐文件名（无空格，与代码中路径一致）：
  bac-4.png   ← 全幅远景 / 天空（对应原站 bac 4.png，用作 `.es-app` 整页背景）
  bac-3.png   ← bac 3.png
  bac-2.png   ← bac 2.png（中景层）
  bac-2-2.png ← bac 2.2.png（右侧山峰等细节层）
  bac-1.png   ← bac 1.png（前景棕榈等）

保存后覆盖本目录下同名文件即可（当前可能为 1×1 占位图，用于保证构建通过）。
首页与全局样式会通过 `import.meta.env.BASE_URL` + `explorescape/文件名` 加载。
若某文件缺失或仍为占位，该层几乎不可见，背景仍有渐变兜底。
