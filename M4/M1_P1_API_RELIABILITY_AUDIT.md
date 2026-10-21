# M1 P1 API Reliability Audit

## 1. Endpoint Inventory

| Endpoint | Method | Path Parameters / Query | Request Body | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| `/` | `GET` | None | None | Root status check |
| `/health` | `GET` | None | None | Service health status check |
| `/lessons` | `GET` | `grade` (int), `subject` (str) | None | Retrieve educational lessons |
| `/worksheets` | `GET` | `lesson_id` (int) | None | Retrieve worksheets |
| `/flashcards` | `GET` | `lesson_id` (int) | None | Retrieve flashcards |
| `/translate` | `POST` | None | `TranslationRequest` JSON | Translate Hindi to Santali (Ol Chiki) |
| `/translation/` | `POST` | None | `TranslationRequest` JSON | Legacy duplicate translation route |

## 2. Normal Request Behavior
All endpoints respond with `HTTP 200 OK` under normal parameters:
- `GET /health` -> `{"status": "ok", "service": "palash-backend", "version": "0.1.0"}`
- `GET /lessons?grade=2&subject=mathematics` -> Returns Grade 2 Counting lesson.
- `GET /worksheets?lesson_id=1` -> Returns Grade 2 Worksheet.
- `GET /flashcards?lesson_id=1` -> Returns 10 Ol Chiki flashcards (`᱑` to `᱑᱐`).
- `POST /translate` (`{"text": "पाँच आम गिनो。", "source_language": "hin_Deva", "target_language": "sat_Olck"}`) -> Returns translated Ol Chiki string with status `"success"`.

## 3. Invalid Request Tests

Direct HTTP tests were executed against all endpoints using invalid/malformed parameters:

| Test ID | Test Scenario | HTTP Status | Response Body | Client Useful? | Stack Trace Exposed? |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **A** | `GET /lessons?grade=invalid` | `422 Unprocessable Entity` | Structured Pydantic validation error JSON | Yes | No |
| **B** | `GET /lessons?subject=nonexistent_12345` | `200 OK` | `{"status": "success", "count": 0, "lessons": []}` | Yes | No |
| **C** | `GET /worksheets` (no lesson_id) | `200 OK` | `{"status": "success", "count": 1, "worksheets": [...]}` | Yes | No |
| **D** | `GET /worksheets?lesson_id=999` | `200 OK` | `{"status": "success", "count": 0, "worksheets": []}` | Yes | No |
| **E** | `GET /flashcards` (no lesson_id) | `200 OK` | `{"status": "success", "count": 10, "flashcards": [...]}` | Yes | No |
| **F** | `GET /flashcards?lesson_id=999` | `200 OK` | `{"status": "success", "count": 0, "flashcards": []}` | Yes | No |
| **G** | `POST /translate` (missing `text`) | `422 Unprocessable Entity` | `{"detail": [{"loc": ["body", "text"], "msg": "Field required"}]}` | Yes | No |
| **H** | `POST /translate` (empty `text`: `""`) | `400 Bad Request` | `{"detail": "text cannot be empty"}` | Yes | No |
| **I** | `POST /translate` (invalid `source_language`: `"eng_Latn"`) | `400 Bad Request` | `{"detail": "Unsupported source language: eng_Latn"}` | Yes | No |
| **J** | `POST /translate` (invalid `target_language`: `"eng_Latn"`) | `400 Bad Request` | `{"detail": "Unsupported target language: eng_Latn"}` | Yes | No |
| **K** | `POST /translate` (malformed JSON) | `422 Unprocessable Entity` | Structured JSON decode error detail | Yes | No |
| **L** | `POST /translate` (extra/unexpected fields) | `200 OK` | Ignored extra fields, returned translation | Yes | No |

## 4. Database Error Handling
- **`GET /lessons` (`app/api/lessons.py`):**
  Includes explicit `try/except Exception as exc` block. On SQLite database errors, it catches the exception and raises `HTTPException(status_code=500, detail="Unable to retrieve lessons")`.
- **`GET /worksheets` & `GET /flashcards` (`app/api/content.py`):**
  Lacks `try/except` exception handling. If a database file error or lock occurs, an uncaught exception will occur.

## 5. Translation Error Handling
- **Service Layer (`app/services/translation_service.py`):**
  Uses `try/except Exception as e` to catch model inference errors or invalid inputs. Returns structured dictionary `{"translated_text": None, "status": "error", "error": str(e)}`.
- **API Layer (`app/api/translate.py`):**
  Checks `result["status"] == "error"` and raises `HTTPException(status_code=400, detail=result.get("error", "Translation failed"))`.
- **Process Resilience:** The FastAPI Uvicorn worker process survives translation exceptions cleanly and handles subsequent requests without degradation.

## 6. Health Endpoint Behavior
- `GET /health` returns static JSON: `{"status": "ok", "service": "palash-backend", "version": "0.1.0"}`.
- Current behavior: Confirms process responsiveness only. Does NOT execute a live query on SQLite or verify PyTorch CUDA model state.

## 7. CORS Assessment
- **Status:** Absent (`CORSMiddleware` is not registered in `app/main.py`).
- **Practical Impact on Native Android App:** **None**. Native Android apps using Retrofit / OkHttp bypass browser CORS preflight checks (`OPTIONS`). Web browsers or web dashboards would be blocked by missing CORS headers.

## 8. Security / Information Leakage
- **Tracebacks / System Paths:** No Python tracebacks, database file paths, or model file paths were exposed during any invalid HTTP test.
- **Sensitive Data:** No secrets or environment variables exposed.

## 9. Regression Results
All core endpoints tested post-audit and confirmed functioning with `200 OK`:
- `GET /health` -> `200 OK`
- `GET /lessons?grade=2&subject=mathematics` -> `200 OK`
- `GET /worksheets?lesson_id=1` -> `200 OK`
- `GET /flashcards?lesson_id=1` -> `200 OK`
- `POST /translate` -> `200 OK`

## 10. Existing Test Coverage
- `test_translation_api.py` exists in root, but targets legacy route `/translation/` and lacks assertions for invalid request shapes or exception handling.

## 11. P0 Findings
- All P0 issues (`P0-1` LAN binding, `P0-2` Translation contract) are resolved and verified complete.

## 12. P1 Findings
- **P1-1:** Missing CORS headers (No impact on native Android client).
- **P1-2:** UTF-8 Mojibake encoding in `app/database.py` seed content (`1 à¤¸à¥‡ 20...`).
- **P1-3:** Synchronous heavy model import at startup.

## 13. P2 Findings
- **P2-1:** Missing exception handling in `app/api/content.py` (`/worksheets`, `/flashcards`).
- **P2-2:** Duplicate route `/translation/` alongside `/translate`.
- **P2-3:** Missing SQLite table indices on `lesson_id` foreign keys.

## 14. Recommended Fix Order
1. **P1-2**: Fix UTF-8 seed encoding in `app/database.py` and re-seed database.
2. **P1-3**: Move PyTorch model loading to lazy loading / app lifespan hook.
3. **P2-1**: Wrap `/worksheets` and `/flashcards` in `try/except` blocks.
4. **P1-1**: Add `CORSMiddleware` for web client support.

---

P1_API_AUDIT_STATUS: COMPLETE
SOURCE_CHANGES: NONE
ANDROID_SOURCE_CHANGES: NONE
M5_SOURCE_CHANGES: NONE
APK_REINSTALLED: NO
TABLET_TOUCHED: NO
NORMAL_API_REGRESSION: PASS (200 OK across all normal endpoints)
INVALID_REQUEST_HANDLING: PASS (HTTP 400/422 with structured JSON error details)
DATABASE_ERROR_HANDLING: PARTIAL (Caught in /lessons, missing in /worksheets and /flashcards)
TRANSLATION_ERROR_HANDLING: PASS (Caught in translation_service.py and converted to HTTP 400)
STACK_TRACE_LEAK: LOW_RISK (None exposed in tested endpoints; potential risk in unhandled DB endpoints)
CORS_STATUS: ABSENT (No impact on native Android client; required for web dashboards)
RECOMMENDED_NEXT_FIX: P1-2 (UTF-8 seed content encoding fix in database.py)
