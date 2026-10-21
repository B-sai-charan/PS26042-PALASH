import time
import uuid
import random
import string
from fastapi import APIRouter, HTTPException, Query, Response
from typing import Optional

from app.database import get_connection
from app.schemas.classroom import (
    CreateSessionRequest,
    CreateSessionResponse,
    ClassroomSessionDto,
    PublishMessageRequest,
    PublishMessageResponse,
    GetMessagesResponse,
    ClassroomMessageDto,
)


router = APIRouter(
    prefix="/classroom",
    tags=["Classroom"]
)


def generate_classroom_code() -> str:
    """
    Generate dynamic 4-character classroom code: PALASH-XXXX
    """
    chars = string.ascii_uppercase + string.digits
    code = "".join(random.choices(chars, k=4))
    return f"PALASH-{code}"


# =========================================================
# POST /classroom/session
# =========================================================

@router.post("/session", status_code=201, response_model=CreateSessionResponse)
def create_session(request: CreateSessionRequest):
    session_id = str(uuid.uuid4())
    classroom_code = generate_classroom_code()
    normalized_section = request.section.strip().upper()

    conn = get_connection()
    try:
        cursor = conn.cursor()
        
        # Ensure unique classroom_code
        for _ in range(10):
            cursor.execute("SELECT session_id FROM classroom_sessions WHERE classroom_code = ?", (classroom_code,))
            if cursor.fetchone() is None:
                break
            classroom_code = generate_classroom_code()

        cursor.execute(
            """
            INSERT INTO classroom_sessions (
                session_id,
                classroom_code,
                teacher_session_id,
                grade,
                section,
                subject,
                lesson_id,
                status
            ) VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE')
            """,
            (
                session_id,
                classroom_code,
                None,
                request.grade,
                normalized_section,
                request.subject.strip(),
                request.lesson_id
            )
        )
        conn.commit()

        cursor.execute("SELECT * FROM classroom_sessions WHERE session_id = ?", (session_id,))
        row = cursor.fetchone()
        session_dto = ClassroomSessionDto(**dict(row))

        return CreateSessionResponse(status="success", session=session_dto)
    finally:
        conn.close()


# =========================================================
# GET /classroom/session/{session_id}
# =========================================================

@router.get("/session/{session_id}", response_model=CreateSessionResponse)
def get_session(session_id: str):
    conn = get_connection()
    try:
        cursor = conn.cursor()
        cursor.execute("SELECT * FROM classroom_sessions WHERE session_id = ?", (session_id,))
        row = cursor.fetchone()
        if not row:
            raise HTTPException(status_code=404, detail=f"Classroom session '{session_id}' not found")

        session_dto = ClassroomSessionDto(**dict(row))
        return CreateSessionResponse(status="success", session=session_dto)
    finally:
        conn.close()


# =========================================================
# POST /classroom/session/{session_id}/end
# =========================================================

@router.post("/session/{session_id}/end")
def end_session(session_id: str):
    conn = get_connection()
    try:
        cursor = conn.cursor()
        cursor.execute("SELECT status FROM classroom_sessions WHERE session_id = ?", (session_id,))
        row = cursor.fetchone()
        if not row:
            raise HTTPException(status_code=404, detail=f"Classroom session '{session_id}' not found")

        cursor.execute("UPDATE classroom_sessions SET status = 'ENDED' WHERE session_id = ?", (session_id,))
        conn.commit()

        return {
            "status": "success",
            "message": "Classroom session ended successfully",
            "session_id": session_id,
            "session_status": "ENDED"
        }
    finally:
        conn.close()


# =========================================================
# POST /classroom/publish
# =========================================================

@router.post("/publish", status_code=201, response_model=PublishMessageResponse)
def publish_message(request: PublishMessageRequest, response: Response):
    now_ms = int(time.time() * 1000)
    conn = get_connection()
    try:
        cursor = conn.cursor()

        # 1. Validate session exists
        cursor.execute("SELECT * FROM classroom_sessions WHERE session_id = ?", (request.session_id,))
        session = cursor.fetchone()
        if not session:
            raise HTTPException(status_code=404, detail=f"Classroom session '{request.session_id}' not found")

        # 2. Validate session is ACTIVE
        if session["status"] != "ACTIVE":
            raise HTTPException(status_code=400, detail="Cannot publish to an ended or inactive classroom session")

        # 3. Validate grade isolation
        if session["grade"] != request.grade:
            raise HTTPException(
                status_code=400,
                detail=f"Grade mismatch: Message grade ({request.grade}) does not match session grade ({session['grade']})"
            )

        # 4. Validate section isolation
        if session["section"].strip().upper() != request.section.strip().upper():
            raise HTTPException(
                status_code=400,
                detail=f"Section mismatch: Message section ('{request.section}') does not match session section ('{session['section']}')"
            )

        # 5. Deduplication check
        cursor.execute(
            "SELECT message_id, created_at FROM classroom_messages WHERE session_id = ? AND message_id = ?",
            (request.session_id, request.message_id)
        )
        existing = cursor.fetchone()
        if existing:
            response.status_code = 200
            return PublishMessageResponse(
                status="duplicate",
                message_id=request.message_id,
                session_id=request.session_id,
                accepted_at=now_ms
            )

        # 6. Insert message
        cursor.execute(
            """
            INSERT INTO classroom_messages (
                session_id,
                message_id,
                type,
                grade,
                section,
                subject,
                lesson_id,
                timestamp,
                hindi_text,
                santali_text,
                audio_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (
                request.session_id,
                request.message_id,
                request.type,
                request.grade,
                request.section.strip().upper(),
                request.subject.strip(),
                request.lesson_id,
                request.timestamp,
                request.hindi_text,
                request.santali_text,
                request.audio_id
            )
        )
        conn.commit()

        return PublishMessageResponse(
            status="success",
            message_id=request.message_id,
            session_id=request.session_id,
            accepted_at=now_ms
        )
    finally:
        conn.close()


# =========================================================
# GET /classroom/messages
# =========================================================

@router.get("/messages", response_model=GetMessagesResponse)
def get_messages(
    session_id: str = Query(..., description="Target classroom session ID"),
    after_message_id: Optional[str] = Query(None, description="Retrieve messages created after this message_id"),
    after_timestamp: Optional[int] = Query(None, description="Retrieve messages timestamped after this epoch ms"),
    limit: int = Query(50, ge=1, le=200, description="Max messages to return")
):
    conn = get_connection()
    try:
        cursor = conn.cursor()

        # Validate session exists
        cursor.execute("SELECT session_id FROM classroom_sessions WHERE session_id = ?", (session_id,))
        if not cursor.fetchone():
            raise HTTPException(status_code=404, detail=f"Classroom session '{session_id}' not found")

        query = "SELECT * FROM classroom_messages WHERE session_id = ?"
        params = [session_id]

        if after_timestamp is not None:
            query += " AND timestamp > ?"
            params.append(after_timestamp)

        if after_message_id is not None:
            cursor.execute(
                "SELECT id FROM classroom_messages WHERE session_id = ? AND message_id = ?",
                (session_id, after_message_id)
            )
            row = cursor.fetchone()
            if row:
                query += " AND id > ?"
                params.append(row["id"])

        query += " ORDER BY timestamp ASC, id ASC LIMIT ?"
        params.append(limit)

        cursor.execute(query, params)
        rows = cursor.fetchall()

        messages = [ClassroomMessageDto(**dict(r)) for r in rows]

        return GetMessagesResponse(
            status="success",
            session_id=session_id,
            count=len(messages),
            messages=messages
        )
    finally:
        conn.close()
