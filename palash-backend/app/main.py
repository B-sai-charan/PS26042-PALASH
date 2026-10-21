import os
from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.database import init_db
from app.api.translate import router as translation_router
from app.api.lessons import router as lessons_router
from app.api.content import router as content_router
from app.api.classroom import router as classroom_router


@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Initialize the PALASH database when the application starts.
    """

    init_db()

    host = os.getenv("PALASH_HOST", "0.0.0.0")
    port = int(os.getenv("PALASH_PORT", "8000"))

    print("=" * 82)
    print("# PALASH BACKEND STARTED")
    print("=" * 82)
    print(f"Binding Host: {host}")
    print(f"Binding Port: {port}")
    print(f"API (Local) : http://127.0.0.1:{port}")
    print(f"API (LAN)   : http://0.0.0.0:{port} (accessible via PC LAN IP)")
    print(f"Swagger     : http://127.0.0.1:{port}/docs")
    print(f"Health      : http://127.0.0.1:{port}/health")
    print(f"Translation : POST http://127.0.0.1:{port}/translate")
    print(f"Lessons     : GET  http://127.0.0.1:{port}/lessons")
    print(f"Worksheets  : GET  http://127.0.0.1:{port}/worksheets")
    print(f"Flashcards  : GET  http://127.0.0.1:{port}/flashcards")
    print(f"Classroom   : /classroom/session, /classroom/publish, /classroom/messages")
    print("=" * 82)

    yield


app = FastAPI(
    title="PALASH Backend",
    description="Backend API for PALASH - Vernacular Pedagogy & Translation",
    version="0.2.0",
    lifespan=lifespan
)


# =========================================================
# ROOT
# =========================================================

@app.get("/")
def root():
    return {
        "status": "ok",
        "service": "palash-backend",
        "version": "0.2.0"
    }


# =========================================================
# HEALTH
# =========================================================

@app.get("/health")
def health():
    return {
        "status": "ok",
        "service": "palash-backend",
        "version": "0.2.0"
    }


# =========================================================
# API ROUTES
# =========================================================

# POST /translate
app.include_router(translation_router)

# GET /lessons
app.include_router(lessons_router)

# GET /worksheets
# GET /flashcards
app.include_router(content_router)

# POST/GET /classroom
app.include_router(classroom_router)


if __name__ == "__main__":
    import uvicorn
    host = os.getenv("PALASH_HOST", "0.0.0.0")
    port = int(os.getenv("PALASH_PORT", "8000"))
    uvicorn.run("app.main:app", host=host, port=port, reload=True)
