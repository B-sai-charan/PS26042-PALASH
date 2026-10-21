import sqlite3
from pathlib import Path


# =========================================================
# DATABASE CONFIGURATION
# =========================================================

BASE_DIR = Path(__file__).resolve().parent.parent
DATABASE_PATH = BASE_DIR / "palash.db"


# =========================================================
# DATABASE CONNECTION
# =========================================================

def get_connection():
    """
    Create and return a SQLite database connection.
    """

    connection = sqlite3.connect(DATABASE_PATH)

    # Return rows that behave like dictionaries.
    connection.row_factory = sqlite3.Row

    return connection


# =========================================================
# DATABASE INITIALIZATION
# =========================================================

def init_db():
    """
    Create PALASH MVP database tables and seed demo content.

    Current MVP content:
        - Grade 2 Mathematics
        - Counting 1 to 20
        - Worksheet
        - Flashcards
        - Sync metadata
        - Classroom Sessions & Messages
    """

    connection = get_connection()
    cursor = connection.cursor()

    try:

        # -------------------------------------------------
        # LESSONS
        # -------------------------------------------------

        cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS lessons (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                grade INTEGER NOT NULL,
                subject TEXT NOT NULL,
                title TEXT NOT NULL,
                hindi_content TEXT NOT NULL,
                santali_content TEXT,
                language TEXT NOT NULL DEFAULT 'hin_Deva-sat_Olck',
                version INTEGER NOT NULL DEFAULT 1,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                updated_at TEXT DEFAULT CURRENT_TIMESTAMP
            )
            """
        )

        # -------------------------------------------------
        # WORKSHEETS
        # -------------------------------------------------

        cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS worksheets (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                lesson_id INTEGER,
                title TEXT NOT NULL,
                content TEXT NOT NULL,
                language TEXT NOT NULL DEFAULT 'hin_Deva-sat_Olck',
                version INTEGER NOT NULL DEFAULT 1,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (lesson_id) REFERENCES lessons(id)
            )
            """
        )

        # -------------------------------------------------
        # FLASHCARDS
        # -------------------------------------------------

        cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS flashcards (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                lesson_id INTEGER,
                front_text TEXT NOT NULL,
                back_text TEXT NOT NULL,
                language TEXT NOT NULL DEFAULT 'hin_Deva-sat_Olck',
                version INTEGER NOT NULL DEFAULT 1,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (lesson_id) REFERENCES lessons(id)
            )
            """
        )

        # -------------------------------------------------
        # SYNC METADATA
        # -------------------------------------------------

        cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS sync_metadata (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                content_version INTEGER NOT NULL DEFAULT 1,
                updated_at TEXT DEFAULT CURRENT_TIMESTAMP
            )
            """
        )

        # -------------------------------------------------
        # CLASSROOM SESSIONS
        # -------------------------------------------------

        cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS classroom_sessions (
                session_id TEXT PRIMARY KEY,
                classroom_code TEXT UNIQUE NOT NULL,
                teacher_session_id TEXT,
                grade INTEGER NOT NULL,
                section TEXT NOT NULL,
                subject TEXT NOT NULL,
                lesson_id INTEGER,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                expires_at TEXT,
                status TEXT NOT NULL DEFAULT 'ACTIVE'
            )
            """
        )
        cursor.execute("CREATE INDEX IF NOT EXISTS idx_sessions_code ON classroom_sessions(classroom_code)")
        cursor.execute("CREATE INDEX IF NOT EXISTS idx_sessions_status ON classroom_sessions(status)")

        # -------------------------------------------------
        # CLASSROOM MESSAGES
        # -------------------------------------------------

        cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS classroom_messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                session_id TEXT NOT NULL,
                message_id TEXT NOT NULL,
                type TEXT NOT NULL DEFAULT 'CLASSROOM_TRANSLATION',
                grade INTEGER NOT NULL,
                section TEXT NOT NULL,
                subject TEXT NOT NULL,
                lesson_id INTEGER,
                timestamp INTEGER NOT NULL,
                hindi_text TEXT,
                santali_text TEXT NOT NULL,
                audio_id TEXT,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (session_id) REFERENCES classroom_sessions(session_id),
                UNIQUE(session_id, message_id)
            )
            """
        )
        cursor.execute("CREATE INDEX IF NOT EXISTS idx_messages_session ON classroom_messages(session_id)")
        cursor.execute("CREATE INDEX IF NOT EXISTS idx_messages_msgid ON classroom_messages(session_id, message_id)")
        cursor.execute("CREATE INDEX IF NOT EXISTS idx_messages_timestamp ON classroom_messages(session_id, timestamp)")

        # =================================================
        # SEED LESSON
        # =================================================

        cursor.execute(
            "SELECT id FROM lessons WHERE grade = ? AND subject = ? AND title = ?",
            (2, "mathematics", "Counting")
        )

        lesson = cursor.fetchone()

        if lesson is None:

            cursor.execute(
                """
                INSERT INTO lessons
                (
                    grade,
                    subject,
                    title,
                    hindi_content,
                    santali_content,
                    language,
                    version
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                (
                    2,
                    "mathematics",
                    "Counting",
                    "1 से 20 तक गिनती सीखें",
                    "᱑ ᱠᱷᱚᱱ ᱒᱐ ᱫᱷᱟᱹᱵᱤᱡ ᱮᱞ ᱪᱮᱫ ᱢᱮ",
                    "hin_Deva-sat_Olck",
                    1,
                ),
            )

            lesson_id = cursor.lastrowid

        else:

            lesson_id = lesson["id"]

        # =================================================
        # SEED WORKSHEET
        # =================================================

        cursor.execute(
            """
            SELECT id
            FROM worksheets
            WHERE lesson_id = ?
            """,
            (lesson_id,)
        )

        worksheet = cursor.fetchone()

        if worksheet is None:

            worksheet_content = """PALASH Grade 2 Mathematics Worksheet

Topic: Counting 1 to 20

Hindi:
1 से 20 तक खाली स्थान भरो।

1. 1, 2, 3, __, __
2. 6, 7, __, __, 10
3. 11, __, 13, __, 15
4. 16, 17, __, 19, __

Santali:
᱑ ᱠᱷᱚᱱ ᱒᱐ ᱫᱷᱟᱹᱵᱤᱡ ᱯᱮᱨᱮᱡ ᱢᱮ
"""

            cursor.execute(
                """
                INSERT INTO worksheets
                (
                    lesson_id,
                    title,
                    content,
                    language,
                    version
                )
                VALUES (?, ?, ?, ?, ?)
                """,
                (
                    lesson_id,
                    "Counting 1 to 20",
                    worksheet_content,
                    "hin_Deva-sat_Olck",
                    1,
                ),
            )

        # =================================================
        # SEED FLASHCARDS
        # =================================================

        flashcards = [
            ("1", "᱑"),
            ("2", "᱒"),
            ("3", "᱓"),
            ("4", "᱔"),
            ("5", "᱕"),
            ("6", "᱖"),
            ("7", "᱗"),
            ("8", "᱘"),
            ("9", "᱙"),
            ("10", "᱑᱐"),
        ]

        for front_text, back_text in flashcards:

            cursor.execute(
                """
                SELECT id
                FROM flashcards
                WHERE lesson_id = ?
                AND front_text = ?
                """,
                (
                    lesson_id,
                    front_text,
                ),
            )

            existing_card = cursor.fetchone()

            if existing_card is None:

                cursor.execute(
                    """
                    INSERT INTO flashcards
                    (
                        lesson_id,
                        front_text,
                        back_text,
                        language,
                        version
                    )
                    VALUES (?, ?, ?, ?, ?)
                    """,
                    (
                        lesson_id,
                        front_text,
                        back_text,
                        "hin_Deva-sat_Olck",
                        1,
                    ),
                )

        # =================================================
        # SYNC VERSION
        # =================================================

        cursor.execute(
            "SELECT id FROM sync_metadata LIMIT 1"
        )

        sync_record = cursor.fetchone()

        if sync_record is None:

            cursor.execute(
                """
                INSERT INTO sync_metadata
                (
                    content_version
                )
                VALUES (1)
                """
            )

        # =================================================
        # COMMIT
        # =================================================

        connection.commit()

        print("Database initialized successfully.")
        print(f"Database path: {DATABASE_PATH}")
        print(f"Demo lesson ID: {lesson_id}")
        print("Worksheet: ready")
        print("Flashcards: 10 ready")
        print("Classroom hub tables: ready")

    finally:

        connection.close()
