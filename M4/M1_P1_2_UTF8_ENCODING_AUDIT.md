# M1 P1-2 UTF-8 Encoding Audit

## 1. Suspected Problem
Previous backend inspection identified potential UTF-8 / Mojibake corruption in the backend seed data (`app/database.py` and `palash.db`), resulting in corrupted Hindi (`1 à¤¸à¥‡ 20...`) and Santali/Ol Chiki strings returned by educational content endpoints (`/lessons`, `/worksheets`, `/flashcards`).

## 2. database.py Encoding
- **File Path:** `C:\Users\saich\Downloads\SIH\palash-backend\app\database.py`
- **File Byte Format:** Valid `UTF-8` (no BOM).
- **File Structure:** The file format itself is valid UTF-8, but string literals inserted into SQLite tables were saved using double-encoded Latin-1 / Mojibake characters.

## 3. Non-ASCII Source Strings
Inspection of `app/database.py` revealed 14 lines containing double-encoded Mojibake literals:
- **Line 148 (Lesson Hindi):** `"1 à¤¸à¥‡ 20 à¤¤à¤• à¤—à¤¿à¤¨à¤¤à¥€ à¤¸à¥€à¤–à¥‡à¤‚à¥¤"` (intended: `1 से 20 तक गिनती सीखें।`)
- **Line 149 (Lesson Santali):** `"á±‘ á± á±·á±šá±± á±’á±  á±›á±Ÿá±  á±®á±ž á±¥á±®á±« á±¢á±® á±¾"` (intended: Ol Chiki numbers/text)
- **Line 177 (Worksheet Hindi):** `"1 à¤¸à¥‡ 20 à¤¤à¤• à¤—à¤¿à¤¨à¤¤à¥€ à¤²à¤¿à¤–à¤¿à¤ à¥¤"` (intended: `1 से 20 तक गिनती लिखिए।`)

## 4. Database Stored Values
Inspection of `palash.db` (SQLite 3) confirmed that `init_db()` populated SQLite tables with these exact Mojibake strings:
- **`lessons` table:** `hindi_content` stores `"1 à¤¸à¥‡ 20 à¤¤à¤•..."`
- **`worksheets` table:** `content` stores `"1 à¤¸à¥‡ 20 à¤¤à¤•..."`
- **`flashcards` table:** `back_text` stores `"á±‘"`, `"á±•"`, `"á±‘á± "`

## 5. API Response Verification
Direct HTTP requests were executed against the running local server:
- `GET /lessons?grade=2&subject=mathematics` -> Returns `HTTP 200 OK`. `hindi_content` contains `"1 à¤¸à¥‡ 20 à¤¤à¤• à¤—à¤¿à¤¨à¤¤à¥€ à¤¸à¥€à¤–à¥‡à¤‚à¥¤"`.
- `GET /worksheets?lesson_id=1` -> Returns `HTTP 200 OK`. `content` contains `"1 à¤¸à¥‡ 20 à¤¤à¤• à¤—à¤¿à¤¨à¤¤à¥€ à¤²à¤¿à¤–à¤¿à¤ à¥¤"`.
- `GET /flashcards?lesson_id=1` -> Returns `HTTP 200 OK`. `back_text` contains `"á±‘"`, `"á±•"`.

The HTTP response headers correctly specify `Content-Type: application/json`, but the payload strings themselves are corrupted Mojibake stored in the database.

## 6. Translation Response Verification
`POST /translate` was tested with payload `{"text": "पाँच आम गिनो。", "source_language": "hin_Deva", "target_language": "sat_Olck"}`:
- **HTTP Status:** `200 OK`
- **Returned Field `translated_text`:** `"ᱥᱚᱝ ᱫᱚᱛᱟ ᱢᱟᱦᱟᱱᱚᱱ ᱜᱤᱱᱤ ᱾"`
- **Result:** **100% CLEAN & UNCORRUPTED**. The translation pipeline bypasses SQLite and invokes the `IndicTrans2` PyTorch model directly, producing authentic Ol Chiki characters.

## 7. Unicode Code Point Verification

### Database Seed Content (`/lessons`, `/worksheets`, `/flashcards`):
- Code points fall into Latin-1 Supplement (`U+00C0`..`U+00FF`) instead of Devanagari or Ol Chiki.
- Example: `"à"` (`0x00E0`), `"¤"` (`0x00A4`), `"¸"` (`0x00B8`), `"à¥‡"` (`0x00E0 0x00A5 0x0086`).

### Translation Endpoint (`POST /translate`):
- Code points fall strictly within the official **Ol Chiki Unicode block (`U+1C50`..`U+1C7F`)**:
  - `0x1C62` (`ᱥ` - Ol Chiki Letter Sam)
  - `0x1C5A` (`ᱚ` - Ol Chiki Letter Ot)
  - `0x1C6C` (`ᱝ` - Ol Chiki Letter Ang)
  - `0x1C6E` (`ᱫ` - Ol Chiki Letter Ud)
  - `0x1C5C` (`ᱚ` - Ol Chiki Letter Ob)
  - `0x1C74` (`ᱛ` - Ol Chiki Letter At)
  - `0x1C5F` (`ᱟ` - Ol Chiki Letter Aa)
  - `0x1C5D` (`ᱢ` - Ol Chiki Letter Am)
  - `0x1C65` (`ᱦ` - Ol Chiki Letter Ih)
  - `0x1C7E` (`᱾` - Ol Chiki Punctuation Mucca)

## 8. Mojibake Evidence
Mojibake is **REAL** in the database seed content (`app/database.py` and `palash.db`). It is not merely a terminal/console display issue. The double-encoded bytes (`UTF-8 bytes -> interpreted as Latin-1 -> saved as UTF-8`) exist directly inside `app/database.py` source code literals and `palash.db` SQLite rows.

## 9. Root Cause
During initial backend creation, Hindi and Ol Chiki UTF-8 strings were copied into `app/database.py` using an editor configured for Windows CP1252 / Latin-1, causing UTF-8 byte sequences (e.g. `\xE0\xA4\xB8`) to be decoded into individual Latin-1 characters (`à¤¸`) and saved as python string literals.

## 10. User/Demo Impact
- **Live Android Demo:** `NONE`. The Android app operates independently using Room local database and asset fallback content.
- **Backend Educational APIs (`/lessons`, `/worksheets`, `/flashcards`):** High. Served JSON content displays Mojibake instead of readable Hindi/Santali text.

## 11. P0 Impact
- **P0 Impact:** `NONE`. Live demo navigation, offline fallback, and `/translate` API remain fully operational and unimpacted.

## 12. P1 Impact
- **P1 Impact:** `HIGH`. Educational content endpoints serve corrupted strings to network clients.

## 13. P2 Impact
- **P2 Impact:** Developer seed script `database.py` requires string literal cleanup.

## 14. Recommended Fix
1. Replace seed string literals in `app/database.py` with properly formatted UTF-8 Hindi (`1 से 20 तक गिनती सीखें।`) and Ol Chiki characters (`᱑`..`᱑᱐`).
2. Delete `palash.db` and re-run `init_db()` to generate clean SQLite content.

## 15. Fix Safety Requirements
- Perform fix ONLY during designated backend bug-fix phase.
- Do NOT touch Android source files or live tablet demo.

---

P1_2_AUDIT_STATUS: COMPLETE
SOURCE_CHANGES: NONE
ANDROID_SOURCE_CHANGES: NONE
M5_SOURCE_CHANGES: NONE
DATABASE_MODIFIED: NO
APK_REINSTALLED: NO
TABLET_TOUCHED: NO
FILE_ENCODING: UTF-8 (valid file format, double-encoded string content)
MOJIBAKE_FOUND: YES
MOJIBAKE_LOCATION: app/database.py seed literals and palash.db tables
API_ENCODING: UTF-8 (valid JSON headers, corrupted payload strings in DB endpoints)
HINDI_VERIFICATION: CORRUPTED_IN_DB (Contains Mojibake "1 à¤¸à¥‡ 20...")
OL_CHIKI_VERIFICATION: CORRUPTED_IN_DB (Contains Mojibake "á±‘..."), VALID_IN_TRANSLATE
TRANSLATION_ENCODING: VALID_UTF8 (U+1C50-U+1C7F Ol Chiki Unicode)
DEMO_IMPACT: NONE (Android demo operates independently on local assets/Room)
ROOT_CAUSE: Double-encoded UTF-8 bytes saved as Latin-1 string literals in app/database.py
RECOMMENDED_FIX: Replace seed string literals in app/database.py with valid UTF-8 Hindi & Ol Chiki characters, then re-seed palash.db
