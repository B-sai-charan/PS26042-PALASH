from pydantic import BaseModel, Field
from typing import Optional, List


class CreateSessionRequest(BaseModel):
    grade: int = Field(..., ge=1, le=12, description="Classroom grade level (e.g., 1, 2, 3)")
    section: str = Field(..., min_length=1, max_length=10, description="Section code (e.g., 'A', 'B')")
    subject: str = Field(..., min_length=1, max_length=50, description="Classroom subject (e.g., 'Mathematics')")
    lesson_id: Optional[int] = Field(None, description="Optional associated lesson ID")


class ClassroomSessionDto(BaseModel):
    session_id: str
    classroom_code: str
    teacher_session_id: Optional[str] = None
    grade: int
    section: str
    subject: str
    lesson_id: Optional[int] = None
    created_at: str
    expires_at: Optional[str] = None
    status: str


class CreateSessionResponse(BaseModel):
    status: str = "success"
    session: ClassroomSessionDto


class PublishMessageRequest(BaseModel):
    session_id: str
    message_id: str
    type: str = "CLASSROOM_TRANSLATION"
    grade: int
    section: str
    subject: str
    lesson_id: Optional[int] = None
    timestamp: int
    hindi_text: Optional[str] = None
    santali_text: str
    audio_id: Optional[str] = None


class PublishMessageResponse(BaseModel):
    status: str
    message_id: str
    session_id: str
    accepted_at: int


class ClassroomMessageDto(BaseModel):
    id: int
    session_id: str
    message_id: str
    type: str
    grade: int
    section: str
    subject: str
    lesson_id: Optional[int] = None
    timestamp: int
    hindi_text: Optional[str] = None
    santali_text: str
    audio_id: Optional[str] = None
    created_at: str


class GetMessagesResponse(BaseModel):
    status: str = "success"
    session_id: str
    count: int
    messages: List[ClassroomMessageDto]
