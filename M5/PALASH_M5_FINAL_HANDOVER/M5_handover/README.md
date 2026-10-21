PALASH --- M5 Educational Content Module

SIH 2026 | Team PALASH

Problem Statement: PS 26042

1. M5 Role

M5 is responsible for the educational content layer of PALASH.

The purpose of this module is to provide structured bilingual learning
content that can be consumed by the PALASH application and integrated
with:

M1 --- Backend/API integration

M2 --- Hindi → Santali translation baseline

M3 --- Speech/audio assets

M4 --- Android/offline integration

M6 --- QA, PPT and final demo

The current M5 scope focuses on foundational numeracy content,
including lessons, worksheets, flashcards and assessments.

2. Current Content Scope

Grade 2

Subject: Mathematics
Topic: Counting and Numbers 1--10

Content includes:

Lesson content

Number vocabulary

Flashcards

Worksheets

Assessments

Santali/Ol Chiki content fields

Reusable number audio integration

Lesson section audio integration

Grade 3

Subject: Mathematics / Counting-related learning content

Current Grade 3 work includes:

Lesson content

Flashcards

Worksheets

Assessments

Santali audio integration

Reusable content/audio structure

The exact Grade 3 files should remain aligned with the final files
currently present in the project repository.

3. M5 Content Architecture

The general content flow is:

JSON Educational Content
        ↓
Python HTML Generator
        ↓
Generated HTML Learning Experience
        ↓
Santali Audio Assets
        ↓
Android / Offline Integration

M5 content is structured so that educational data and presentation
generation are separated.

This makes the content easier to:

Update

Reuse

Generate

Integrate

Cache for offline use

4. Grade 2 Lesson 2

Main Files

data/
└── lesson_g2_counting.json

src/
└── generate_lesson_g2.py

output/
└── lesson_g2_counting.html

Lesson Sections

The Grade 2 lesson currently contains six sections:

Section ID   Type

SEC01        Introduction
SEC02        Teacher Instruction
SEC03        Number Learning
SEC04        Example
SEC05        Student Activity
SEC06        Assessment Prompt

5. Grade 2 Lesson Audio Integration

The Grade 2 Lesson 2 audio folder currently follows this structure:

audio/
└── santali_audio/
    ├── lesson_g2/
    │   ├── L2_SEC01.wav
    │   ├── L2_SEC02.wav
    │   ├── L2_SEC03.wav
    │   ├── L2_SEC04.wav
    │   ├── L2_SEC05.wav
    │   └── L2_SEC06.wav
    │
    └── numbers/
        ├── number_1.wav
        ├── number_2.wav
        ├── ...
        └── number_10.wav

Section Audio Mapping

SEC01 → L2_SEC01.wav
SEC02 → L2_SEC02.wav
SEC03 → L2_SEC03.wav
SEC04 → L2_SEC04.wav
SEC05 → L2_SEC05.wav
SEC06 → L2_SEC06.wav

The updated Grade 2 lesson JSON contains an audio_file field for each
lesson section.

Example:

{
  "section_id": "SEC01",
  "audio_file": "L2_SEC01.wav"
}

The Python generator reads this mapping and creates the corresponding
audio button.

6. Grade 2 Number Audio Strategy

Numbers are treated as reusable vocabulary assets.

The structure is:

number_1.wav
number_2.wav
...
number_10.wav

These files are reused wherever the same number vocabulary appears.

This avoids unnecessary duplicate audio generation and keeps the audio
package smaller.

7. Grade 3 Audio Structure

Grade 3 audio has been organized into separate categories so that
different educational components can reference their own audio assets.

The project structure includes categories such as:

audio/santali_audio/
├── assessment_g3/
├── flashcards_g3/
├── worksheet_g3/
├── lesson_g3/
└── numbers/

The final audio filenames must remain consistent with the Python/HTML
generator mappings.

8. Flashcards

The flashcard generation approach supports:

Number display

Visual objects

Hindi content

Santali content

Audio playback

Card navigation

For the Grade 3 flashcard interface, the visible audio buttons were
simplified to display only:

🔊

The text labels such as:

Number
Santali

were removed from the visible buttons while keeping the audio
functionality.

9. Worksheets

Worksheets are generated from structured educational content.

The intended flow is:

Worksheet JSON
      ↓
Python Generator
      ↓
HTML Worksheet
      ↓
Associated Santali Audio Assets

Audio filenames and content IDs must remain synchronized.

10. Assessments

Assessments follow the same structured approach:

Assessment JSON
      ↓
Python Generator
      ↓
HTML Assessment
      ↓
Audio Mapping

The assessment audio should reference exact existing audio filenames.

Do not rename audio files without also updating the corresponding
mappings.

11. Audio Integration Rule

The most important integration rule is:

Content ID / Section ID
        ↓
Exact Audio Filename
        ↓
Correct Audio Folder
        ↓
Python Generator
        ↓
Generated HTML
        ↓
Audio Playback

Example:

SEC03
↓
L2_SEC03.wav
↓
audio/santali_audio/lesson_g2/
↓
generate_lesson_g2.py

A filename mismatch will cause the audio button to fail.

12. Recommended Audio Mapping Strategy

The preferred approach is to store explicit audio mappings in the JSON
whenever possible.

Example:

{
  "section_id": "SEC04",
  "audio_file": "L2_SEC04.wav"
}

The Python generator then uses:

audio_file = section.get("audio_file")

This is preferable to manually maintaining many hardcoded mappings
inside Python because:

The content owns its metadata.

Audio mappings are easier to inspect.

Future updates are simpler.

Fewer code changes are required when content changes.

13. Content Validation Status

Important project status:

The Santali content currently used in the internal MVP must not be
described as native-speaker validated unless documented evidence is
available.

The current evidence distinction is:

Translation/software validation: VALIDATED FOR MVP FUNCTIONAL USE
Functional pipeline validation: VALIDATED
Native-speaker linguistic validation: FUTURE VALIDATION / OUT OF CURRENT MVP SCOPE

Therefore, M5 must not fabricate:

Native-speaker approval

Validator names

Validation dates

Validation percentages

Linguistic accuracy percentages

Official certification

The internal hackathon MVP may use the existing translation/content
baseline, but evidence must remain accurately represented.

14. M5 Deliverables

The M5 handoff should include the final versions of:

Lessons
Worksheets
Flashcards
Assessments
Lesson metadata
Content screenshots where required
Audio mapping information

The content should be frozen before final integration except for:

Critical bug fixes

Broken mappings

Required integration fixes

Team Lead approved corrections

15. M5 → M4 Dependency

M4 requires stable educational content and stable filenames.

The handoff must ensure:

JSON files are final.

Python generators are final.

Generated HTML is tested.

Audio files exist in the expected folders.

Audio filenames match the code.

Content IDs are not changed without updating references.

Offline content can be packaged consistently.

16. Recommended Final Project Structure

kovidh_palash/
│
├── audio/
│   └── santali_audio/
│       ├── assessment/
│       ├── assessment_g3/
│       ├── flashcards/
│       ├── flashcards_g3/
│       ├── lesson_g2/
│       ├── lesson_g3/
│       ├── numbers/
│       ├── worksheet/
│       └── worksheet_g3/
│
├── data/
│   ├── lesson_g2_counting.json
│   ├── lesson_g3_*.json
│   ├── worksheet_g2_*.json
│   ├── worksheet_g3_*.json
│   ├── flashcards_g2_*.json
│   ├── flashcards_g3_*.json
│   ├── assessments_g2_*.json
│   └── assessments_g3_*.json
│
├── src/
│   ├── generate_lesson_g2.py
│   ├── generate_lesson_g3.py
│   ├── generate_worksheets_g2.py
│   ├── generate_worksheets_g3.py
│   ├── generate_flashcards_g2.py
│   ├── generate_flashcards_g3.py
│   ├── generate_assessments_g2.py
│   └── generate_assessments_g3.py
│
└── output/
    ├── lesson_g2_counting.html
    ├── lesson_g3_*.html
    ├── worksheets_*.html
    ├── flashcards_*.html
    └── assessments_*.html

Actual filenames may vary depending on the final repository.

17. M5 Final Testing Checklist

Before handoff, verify:

Content

JSON opens without errors.

All required sections are present.

Content IDs are unique.

Hindi content displays correctly.

Santali/Ol Chiki content displays correctly.

Audio

Every referenced audio file exists.

Filename capitalization matches.

Folder paths match the Python code.

Lesson section audio plays.

Number audio plays.

Flashcard audio plays.

Worksheet audio plays where configured.

Assessment audio plays where configured.

Generators

Python generator runs successfully.

HTML file is generated.

No missing JSON key errors occur.

No broken audio paths occur.

Demo Readiness

Grade selection works.

Lesson opens.

Flashcards work.

Worksheets work.

Assessments work.

Audio works.

Offline content can be demonstrated.

18. Important "Do Not Do" Rules

Do not:

Rename audio files randomly.

Move audio files without updating paths.

Change content IDs without updating mappings.

Create duplicate audio unnecessarily.

Claim native-speaker validation without evidence.

Claim unsupported accuracy percentages.

Expand content scope during final integration.

Add unnecessary features during the final hackathon phase.

19. Current M5 Status

M5 work has progressed from creating and structuring educational content
to integrating reusable audio assets into the generated learning
content.

Current major focus:

CONTENT
+
AUDIO
+
STABLE FILE MAPPINGS
+
GENERATOR TESTING
+
M4 INTEGRATION

The objective is no longer to create isolated content modules.

The objective is to provide stable, integrated educational content
that works inside the PALASH product.

20. Final Handoff Summary

M5 provides the educational foundation for PALASH:

Structured Learning Content
        +
Hindi Content
        +
Santali / Ol Chiki Content
        +
Reusable Audio Assets
        +
Lesson Audio
        +
Flashcards
        +
Worksheets
        +
Assessments
        +
Stable JSON/Python Generation

The final priority is:

ONE STABLE CONTENT PIPELINE
        ↓
ONE WORKING PRODUCT
        ↓
ONE CLEAR DEMO

Current Status

M5 Educational Content: Active / Integration Phase
Primary Goal: Stable content + audio integration
Next Priority: Final end-to-end testing with M4 and the PALASH
application

Evidence and Claiming Rule

Use only claims supported by project evidence.

For the current MVP, avoid claiming native-speaker linguistic validation
unless documented validation evidence is added later.