from fastapi import APIRouter, HTTPException
from typing import Optional

from app.database import get_connection


router = APIRouter(
    prefix="/lessons",
    tags=["Lessons"]
)


@router.get("")
def get_lessons(
    grade: Optional[int] = None,
    subject: Optional[str] = None
):
    """
    Return available educational lessons.

    Optional filters:
        ?grade=2
        ?subject=mathematics
        ?grade=2&subject=mathematics
    """

    connection = get_connection()

    try:
        query = """
            SELECT
                id,
                grade,
                subject,
                title,
                hindi_content,
                santali_content,
                language,
                version,
                created_at,
                updated_at
            FROM lessons
            WHERE 1=1
        """

        parameters = []

        if grade is not None:
            query += " AND grade = ?"
            parameters.append(grade)

        if subject is not None:
            query += " AND LOWER(subject) = LOWER(?)"
            parameters.append(subject)

        query += " ORDER BY id ASC"

        cursor = connection.cursor()
        cursor.execute(query, parameters)

        rows = cursor.fetchall()

        lessons = [dict(row) for row in rows]

        return {
            "status": "success",
            "count": len(lessons),
            "lessons": lessons
        }

    except Exception as exc:
        raise HTTPException(
            status_code=500,
            detail="Unable to retrieve lessons"
        ) from exc

    finally:
        connection.close()