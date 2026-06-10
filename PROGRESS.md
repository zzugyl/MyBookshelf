# MyBookshelf 升级进度

## 已完成

### 1. 升级支持 Android 16 (API 36)
- AGP 4.1.0 → 8.10.0, Gradle 6.5 → 8.12
- compileSdk/targetSdk 29 → 36
- 所有模块添加 namespace，移除 AndroidManifest 中的 package 属性
- 移除废弃的 bintray/maven 发布插件
- jcenter → mavenCentral
- 添加 buildFeatures { buildConfig true }
- 添加 android.nonFinalResIds=false 保持 switch-case 兼容
- 添加 android:exported="true" 到 launcher Activity
- minSdk (compressor) 9 → 21

### 2. 移除 AlipayZeroSdk 及残留捐赠代码
- 删除 AlipayZeroSdk 模块目录
- 从 settings.gradle、build.gradle 移除引用
- 清理 AboutFragment、MainActivity 中的注释代码
- 清理 strings.xml（中英文）中的捐赠字符串
- 清理 about_preference.xml 中的捐赠配置

### 3. 接入 Google Books API
- 新增 GoogleBooksFetcher.java、GoogleBooksJson.java
- 作为第三图书数据源（豆瓣、OpenLibrary、Google Books）
- API Key 通过 apiKeys.properties 和 BuildConfig 管理
- 设置页面添加 Google Books 选项

### 4. 修复高优先级废弃 API
- `getSerializableExtra()` → 带类型参数版本（BookDetailActivity、BookEditActivity）
- `onBackPressed()` → `OnBackPressedCallback`（MainActivity、BookEditActivity、BatchAddActivity）
- `startActivityForResult()` → `ActivityResultLauncher`（BookEditActivity）
- 添加 `androidx.activity:activity:1.6.0` 依赖

### 5. 修复 SAF Document Provider 误报
- SettingsFragment 中 `resolveActivity()` 对 SAF 意图返回 null 导致备份/恢复/CSV 导出不可用
- 改用 `try-catch ActivityNotFoundException` 替代预先检查（3 处）

### 6. 迁移 PreferenceFragment → PreferenceFragmentCompat
- 添加 `androidx.preference:preference:1.2.1` 依赖
- SettingsFragment/AboutFragment 改继承 `PreferenceFragmentCompat`
- `addPreferencesFromResource` 迁移到 `onCreatePreferences()`
- Host Activity 改用 `getSupportFragmentManager()`
- XML: `SwitchPreference` → `SwitchPreferenceCompat`，修复 `defaultValue="1"`

### 7. SettingsFragment startActivityForResult → ActivityResultLauncher
- 3 个请求码常量替换为 `ActivityResultLauncher` 字段
- 移除 `onActivityResult()` 方法
- 高优先级废弃 API 修复全部完成

### 8. AsyncTask → ExecutorService（SettingsFragment）
- 3 个 AsyncTask 类替换为 `executorService.execute()` + `runOnUiThread()`

### 9. MenuItemCompat → setShowAsAction()
- SingleAddActivity、BatchScanFragment 直接调用 `menuItem.setShowAsAction()`

### 10. ViewPager → ViewPager2（BatchAddActivity）
- 添加 `androidx.viewpager2:viewpager2:1.0.0` 依赖
- PagerAdapter 改为 FragmentStateAdapter
- TabLayout 使用 TabLayoutMediator

### 11. 移除 App Center SDK
- 移除 appcenter-analytics、appcenter-crashes 依赖
- 移除所有 Analytics.trackEvent() 调用及相关代码

### 12. versionCode 改用 git commit 次数
- 新增 `getGitCommitCount()` 函数
- versionCode 从硬编码改为 `git rev-list --count HEAD`

### 13. 清理死代码和过期注释
- 移除 build.gradle 中注释掉的依赖
- 移除各文件中注释掉的代码和过期 TODO/FIXME

### 14. 清理残留死代码
- SingleAddActivity：移除注释掉的 Handler.postDelayed 和 resumeCameraPreview
- CoverDownloader：移除注释掉的 Toast 和未完成的 //todo

### 15. 移除 compressor 模块 RxJava 依赖
- compressToFileAsObservable / compressToBitmapAsObservable 未被 app 调用
- 移除两个 Observable 方法和 rx.Observable / rx.functions.Func0 导入
- 移除 build.gradle 中 io.reactivex:rxjava:1.3.0 依赖

## 当前版本
- versionName: "2.0"
- versionCode: 动态获取（git commit 次数）

## 签名配置
- Keystore 文件: `app/release.keystore`
- Key Alias: `mybookshelf`
- Store Password: `android`
- Key Password: `android`
- APK 输出: `app/build/outputs/apk/release/app-release.apk`

## Git 提交记录
| Commit | 说明 |
|--------|------|
| d8c48d5 | 升级支持 Android 16，版本号更新为 2.0 |
| 1495051 | 移除 AlipayZeroSdk 模块及残留捐赠代码 |
| 56f15a2 | 接入 Google Books API 作为第三图书数据源 |
| b85ff7f | 修复 SAF Document Provider 误报问题 |
| d5949c1 | 迁移 PreferenceFragment → PreferenceFragmentCompat |
| f54ec4d | SettingsFragment: startActivityForResult → ActivityResultLauncher |
| ff7b15c | 完成中优先级废弃 API 迁移 |
| 162718f | 移除 App Center SDK，versionCode 改用 git commit 次数 |
| e8838ca | 清理死代码和过期注释 |
| a589a94 | 清理残留死代码，移除 compressor 模块 RxJava 依赖 |
| 6a80319 | 升级 Material Components 1.2.1 → 1.12.0 |

## 待处理

### 高优先级（全部完成）
- [x] getSerializableExtra 修复
- [x] onBackPressed 修复
- [x] startActivityForResult 修复（BookEditActivity）
- [x] startActivityForResult 修复（SettingsFragment）

### 中优先级（全部完成）
- [x] 迁移 android.preference.PreferenceFragment → androidx.preference.PreferenceFragmentCompat
- [x] AsyncTask → ExecutorService（SettingsFragment）
- [x] MenuItemCompat → MenuItem.setShowAsAction()（SingleAddActivity、BatchScanFragment）
- [x] ViewPager + FragmentPagerAdapter → ViewPager2（BatchAddActivity）

### 低优先级
- [ ] 替换已停维库：clans FAB、barcodescanner
- [ ] 升级过旧库版本：material-dialogs 0.9.6 → 3.3.0、materialdrawer 6.1.2 → 9.x、material 1.2.1 → 1.12.x
- [x] App Center SDK 退役处理（已移除）
- [ ] RxJava 1.x → 3.x（compressor 模块）
- [ ] Gradle 语法现代化（plugins DSL、移除 allprojects/buildscript）
- [x] 清理注释掉的死代码和过期 TODO/FIXME
- [x] 清理残留死代码（SingleAddActivity、CoverDownloader）
- [x] 移除 compressor 模块 RxJava 依赖（未被调用，直接移除）
- [x] 升级 Material Components 1.2.1 → 1.12.0

### API 服务
- [ ] 修复豆瓣 API 代理服务（47.108.87.209:9268）
  - 服务器当前无法连接，所有端口超时
  - 修复后需将 server.gradle 改为 `http://47.108.87.209:9268`

## 已知问题
- 豆瓣 API 代理服务故障，暂时不可用
- SettingsFragment 使用废弃的 android.preference.PreferenceFragment，暂未迁移
- 项目无测试用例，无 CI/CD
