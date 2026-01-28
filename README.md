# 重機停車 (Moto Parking)

一款專為台灣大型重型機車（黃牌、紅牌）騎士打造的停車位查詢 App，支援 Android 與 iOS 雙平台。

## 功能特色

- 以地圖或列表方式瀏覽附近的重機友善停車位
- 顯示停車位詳細資訊（名稱、地址、適用車牌類型）
- 支援 GPS 定位，快速找到最近的停車點
- 點擊地圖標記可放大並顯示停車位名稱
- 跨平台支援：Android（Google Maps）與 iOS（Apple MapKit）

## 截圖

*（待補充）*

## 資料來源

本 App 的停車位資料主要來自以下來源：

| 來源 | 說明 |
|------|------|
| **社群資料** | 由熱心車友共同維護的 [大重停車記事](https://www.google.com/maps/d/viewer?mid=1HUmSMnFWqGMIv2TpbTvBeiaLnFGkYbI) Google 地圖 |
| **政府資料** | 政府開放資料平台（規劃中） |
| **使用者提交** | 使用者回報的停車點（規劃中） |

> **注意**：停車位資訊可能隨時變動，實際停車前請以現場標示為準。本 App 僅供參考，不保證資料的即時性與準確性。

## 技術架構

本專案採用 **Kotlin Multiplatform (KMP)** 開發，共享商業邏輯與大部分 UI 程式碼。

| 技術 | 用途 |
|------|------|
| Compose Multiplatform | 跨平台 UI 框架 |
| Koin | 依賴注入 |
| Ktor | HTTP 網路請求 |
| Supabase | 後端服務（資料庫、認證） |
| SQLDelight | 本地資料庫 |
| Google Maps Compose | Android 地圖 |
| MapKit (UIKitView) | iOS 地圖 |

### 專案結構

```
moto-parking/
├── composeApp/          # Compose Multiplatform UI
│   ├── commonMain/      # 共用 UI 程式碼
│   ├── androidMain/     # Android 專用（Google Maps）
│   └── iosMain/         # iOS 專用（MapKit）
├── shared/              # 商業邏輯與資料層
│   └── commonMain/      # Repository、資料來源、DI
└── iosApp/              # iOS App 進入點（SwiftUI）
```

## 建置與執行

### 環境需求

- JDK 17+
- Android Studio Ladybug 或更新版本
- Xcode 15+ （iOS 開發）
- CocoaPods （iOS 依賴管理）

### 建置專案

```bash
# 建置 Android 與 iOS
./gradlew compileKotlinIosSimulatorArm64 compileDebugKotlinAndroid

# 僅建置 Android APK
./gradlew :composeApp:assembleDebug
```

### 執行 Android

```bash
./gradlew :composeApp:installDebug
```

### 執行 iOS

```bash
cd iosApp
pod install
open iosApp.xcworkspace
# 在 Xcode 中選擇模擬器並執行
```

## 開源授權

本專案採用 [MIT License](LICENSE) 授權。

```
MIT License

Copyright (c) 2025 robert0ng

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## 參與貢獻

歡迎任何形式的貢獻！

- **回報問題**：發現 Bug 或有任何建議，請到 [Issues](https://github.com/robert0ng/moto-parking/issues) 提出
- **功能建議**：有新功能的想法？歡迎開 Issue 討論
- **程式碼貢獻**：歡迎 Fork 本專案並提交 Pull Request

## 致謝

特別感謝 [大重停車記事](https://www.google.com/maps/d/viewer?mid=1HUmSMnFWqGMIv2TpbTvBeiaLnFGkYbI) 的維護者與所有貢獻停車位資料的車友們。

## 聯絡方式

如有任何問題或建議，歡迎透過 [GitHub Issues](https://github.com/robert0ng/moto-parking/issues) 聯繫。
