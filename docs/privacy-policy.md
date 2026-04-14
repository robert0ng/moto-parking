---
title: 隱私權政策 | Privacy Policy
---

# 隱私權政策

**最後更新日期：2026-04-14**

「重機停車」（以下簡稱「本 App」）由 robert0ng 開發與維護。本政策說明本 App 在使用過程中蒐集、使用及保護你個人資料的方式。

## 1. 我們蒐集的資料

### 1.1 位置資訊（精確位置）

本 App 會請求你的裝置位置權限（`ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`），用途如下：

- 在地圖上顯示你目前的位置
- 計算附近重機停車位與你之間的距離，並依距離排序

位置資料僅在你的裝置上本機處理，**不會上傳至我們的伺服器，也不會與第三方分享**。

### 1.2 帳號資訊（Google 登入）

當你選擇使用 Google 帳號登入以使用打卡、回報問題、收藏等功能時，我們會透過 Supabase 驗證服務取得並儲存：

- 使用者 ID（UUID）
- 電子郵件地址（來自 Google 帳號）

這些資料僅用於識別你的帳號，使你能：
- 對停車位進行打卡（24 小時內同一停車位僅能打卡一次）
- 回報停車位問題
- 收藏停車位

你未登入時，仍可正常瀏覽、搜尋停車位，無需提供任何帳號資訊。

### 1.3 使用者產生的內容

登入後，你所產生的下列資料會與你的使用者 ID 一同儲存於 Supabase：

- 打卡紀錄（停車位、時間）
- 停車位回報（類別、留言內容、時間）
- 收藏的停車位

## 2. 我們不蒐集的資料

- 廣告識別碼（AAID / IDFA）
- 裝置識別碼
- 通話或簡訊紀錄
- 聯絡人、行事曆、照片
- 任何形式的分析（Analytics）或崩潰回報（Crash Reporting）資料

本 App 未整合任何第三方分析、廣告或追蹤工具。

## 3. 第三方服務

本 App 使用下列第三方服務：

| 服務 | 用途 | 隱私權政策 |
|------|------|----------|
| Google Maps SDK | Android 地圖顯示 | https://policies.google.com/privacy |
| Google Sign-In | 使用者登入 | https://policies.google.com/privacy |
| Apple MapKit | iOS 地圖顯示 | https://www.apple.com/legal/privacy/ |
| Supabase | 後端資料庫與驗證 | https://supabase.com/privacy |

## 4. 資料保存與刪除

- 位置資料不會離開你的裝置，因此無需刪除。
- 帳號相關資料（使用者 ID、電子郵件、打卡、回報、收藏）會保存至你要求刪除或帳號註銷為止。
- 若你希望刪除帳號及所有相關資料，請以下列方式聯繫我們：<https://github.com/robert0ng/moto-parking/issues>

## 5. 兒童隱私

本 App 並非專為 13 歲以下兒童設計，且不會刻意蒐集兒童的個人資料。

## 6. 政策變更

本政策若有更新，將於本頁面公告，並更新頂部的「最後更新日期」。建議你定期檢視本政策。

## 7. 聯絡我們

如對本隱私權政策有任何疑問，請透過以下方式聯繫：

- GitHub Issues: <https://github.com/robert0ng/moto-parking/issues>

---

# Privacy Policy (English)

**Last updated: 2026-04-14**

"Moto Parking" (重機停車, the "App") is developed and maintained by robert0ng. This policy explains what information the App collects, how it is used, and how it is protected.

## 1. Information We Collect

### 1.1 Location (Precise)

The App requests device location permission (`ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`) to:

- Display your current position on the map
- Sort nearby motorcycle parking spots by distance

Location data is processed **on-device only**. It is **never uploaded to our servers** and is not shared with any third party.

### 1.2 Account Information (Google Sign-In)

When you sign in with Google to use check-in, spot reporting, or favorites, we receive and store the following via Supabase Auth:

- User ID (UUID)
- Email address (from your Google account)

This information is used solely to identify your account so you can check in to parking spots, submit reports, and save favorites.

You can browse and search parking spots without signing in. No account information is required for read-only use.

### 1.3 User-Generated Content

Once signed in, the following data is stored in Supabase, associated with your user ID:

- Check-ins (spot, timestamp)
- Spot reports (category, comment, timestamp)
- Favorited spots

## 2. What We Do NOT Collect

- Advertising IDs (AAID / IDFA)
- Device identifiers
- Call logs, SMS, contacts, calendar, photos
- Analytics or crash-reporting data

The App does not integrate any third-party analytics, advertising, or tracking SDK.

## 3. Third-Party Services

| Service | Purpose | Privacy Policy |
|---------|---------|---------------|
| Google Maps SDK | Android map rendering | https://policies.google.com/privacy |
| Google Sign-In | User authentication | https://policies.google.com/privacy |
| Apple MapKit | iOS map rendering | https://www.apple.com/legal/privacy/ |
| Supabase | Backend database & auth | https://supabase.com/privacy |

## 4. Data Retention and Deletion

- Location data never leaves your device; no deletion is needed.
- Account-related data (user ID, email, check-ins, reports, favorites) is retained until you request deletion.
- To request account and data deletion, please contact us via <https://github.com/robert0ng/moto-parking/issues>.

## 5. Children's Privacy

The App is not directed at children under 13 and does not knowingly collect personal information from children.

## 6. Changes to This Policy

We may update this policy from time to time. Changes will be posted on this page with an updated "Last updated" date.

## 7. Contact

For questions about this privacy policy, please contact us via:

- GitHub Issues: <https://github.com/robert0ng/moto-parking/issues>
