from fastapi import APIRouter, HTTPException, Query

from app.database import get_connection


router = APIRouter(
    tags=["Content"]
)


# =========================================================
# GET /worksheets
# =========================================================

@router.get("/worksheets")
def get_worksheets(
    lesson_id: int | None = Query(default=None)
):
    """
    Return worksheet content.

    Optional:
        lesson_id - filter worksheets belonging to a lesson.
    """

    try:
        connection = get_connection()
        try:
            cursor = connection.cursor()

            if lesson_id is not None:
                cursor.execute(
                    """
                    SELECT
                        id,
                        lesson_id,
                        title,
                        content,
                        language,
                        version,
                        created_at,
                        updated_at
                    FROM worksheets
                    WHERE lesson_id = ?
                    ORDER BY id
                    """,
                    (lesson_id,)
                )
            else:
                cursor.execute(
                    """
                    SELECT
                        id,
                        lesson_id,
                        title,
                        content,
                        language,
                        version,
                        created_at,
                        updated_at
                    FROM worksheets
                    ORDER BY id
                    """
                )

            rows = cursor.fetchall()

            worksheets = [dict(row) for row in rows]

            return {
                "status": "success",
                "count": len(worksheets),
                "worksheets": worksheets
            }
        finally:
            connection.close()

    except Exception as exc:
        raise HTTPException(
            status_code=500,
            detail="Unable to retrieve worksheets"
        ) from exc


# =========================================================
# GET /flashcards
# =========================================================

@router.get("/flashcards")
def get_flashcards(
    lesson_id: int | None = Query(default=None)
):
    """
    Return flashcard content.

    Optional:
        lesson_id - filter flashcards belonging to a lesson.
    """

    try:
        connection = get_connection()
        try:
            cursor = connection.cursor()

            if lesson_id is not None:
                cursor.execute(
                    """
                    SELECT
                        id,
                        lesson_id,
                        front_text,
                        back_text,
                        language,
                        version,
                        created_at,
                        updated_at
                    FROM flashcards
                    WHERE lesson_id = ?
                    ORDER BY id
                    """,
                    (lesson_id,)
                )
            else:
                cursor.execute(
                    """
                    SELECT
                        id,
                        lesson_id,
                        front_text,
                        back_text,
                        language,
                        version,
                        created_at,
                        updated_at
                    FROM flashcards
                    ORDER BY id
                    """
                )

            rows = cursor.fetchall()

            flashcards = [dict(row) for row in rows]

            return {
                "status": "success",
                "count": len(flashcards),
                "flashcards": flashcards
            }
        finally:
            connection.close()

    except Exception as exc:
        raise HTTPException(
            status_code=500,
            detail="Unable to retrieve flashcards"
        ) from exc
