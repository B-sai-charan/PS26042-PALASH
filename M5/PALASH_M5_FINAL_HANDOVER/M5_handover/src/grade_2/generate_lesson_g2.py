import json
from pathlib import Path


# =========================================================
# PATHS
# =========================================================

BASE_DIR = Path(__file__).resolve().parent.parent

INPUT_FILE = BASE_DIR / "data" / "lesson_g2_counting.json"

OUTPUT_DIR = BASE_DIR / "output"

OUTPUT_FILE = OUTPUT_DIR / "lesson_g2_counting.html"

OUTPUT_DIR.mkdir(exist_ok=True)


# =========================================================
# LOAD LESSON DATA
# =========================================================

with open(INPUT_FILE, "r", encoding="utf-8") as file:
    lesson = json.load(file)


# =========================================================
# START HTML
# =========================================================

html = f"""<!DOCTYPE html>
<html lang="hi">

<head>

<meta charset="UTF-8">

<meta name="viewport" content="width=device-width, initial-scale=1.0">

<title>{lesson["title"]}</title>


<style>

body {{
    font-family: Arial, sans-serif;
    margin: 0;
    padding: 20px;
    background: #f7f7f7;
}}

.container {{
    max-width: min(95vw, 1150px);
    margin: auto;
}}

.header {{
    text-align: center;
    margin-bottom: 25px;
}}

.header h1 {{
    font-size: clamp(28px, 4vw, 44px);
    margin-bottom: 8px;
}}

.header h2 {{
    font-size: clamp(20px, 3vw, 32px);
    margin: 8px 0;
}}

.header p {{
    font-size: clamp(16px, 2.2vw, 24px);
}}

.meta {{
    background: white;
    border-radius: 12px;
    padding: clamp(16px, 2.5vw, 28px);
    margin-bottom: 20px;
    font-size: clamp(16px, 2.2vw, 24px);
}}

.section {{
    background: white;
    border: 1px solid #ccc;
    border-radius: 14px;
    padding: clamp(16px, 2.5vw, 32px);
    margin-bottom: 20px;
}}

.section h3 {{
    margin-top: 0;
    font-size: clamp(20px, 3vw, 30px);
}}

.hindi {{
    font-size: clamp(20px, 3vw, 32px);
    font-weight: bold;
    margin: 10px 0;
}}

.santali {{
    font-size: clamp(18px, 2.8vw, 30px);
    margin: 10px 0;
}}

.visual {{
    font-size: clamp(32px, 5.5vw, 60px);
    margin: 15px 0;
}}

.audio-row {{
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    flex-wrap: wrap;
    margin: 10px 0;
}}

.audio-button {{
    padding: 8px 12px;
    font-size: 20px;
    border: 1px solid #777;
    border-radius: 8px;
    background: white;
    cursor: pointer;
}}

.number-grid {{
    display: grid;
    grid-template-columns:
        repeat(auto-fit, minmax(min(150px, 100%), 1fr));
    gap: 15px;
}}

.number-card {{
    border: 1px solid #999;
    border-radius: 12px;
    padding: clamp(14px, 2vw, 24px);
    text-align: center;
    background: #fff;
}}

.number {{
    font-size: clamp(36px, 6vw, 60px);
    font-weight: bold;
}}

.number-visual {{
    font-size: clamp(20px, 3vw, 32px);
    margin: 10px 0;
}}

</style>

</head>


<body>

<div class="container">


<div class="header">

<h1>PALASH 🌸</h1>

<h2>{lesson["title"]}</h2>

<p>
Grade {lesson["grade"]}
|
{lesson["subject"].title()}
|
{lesson["topic"].replace("_", " ").title()}
</p>

</div>


<div class="meta">

<b>Learning Objective:</b>
{lesson["learning_objective"]}

<br><br>

<b>Expected Learning Outcome:</b>
{lesson["expected_learning_outcome"]}

</div>
"""


# =========================================================
# GENERATE LESSON SECTIONS
# =========================================================

for section in lesson["sections"]:

    html += f"""

<div class="section">

<h3>
{section["heading"]}
</h3>


<div class="hindi">

Hindi: {section["hindi"]}

</div>


<div class="santali">

Santali: {section["santali"]}

</div>
"""


    # =====================================================
    # SECTION AUDIO
    # Uses audio_file from JSON:
    # SEC01 -> L2_SEC01.wav
    # ...
    # SEC06 -> L2_SEC06.wav
    # =====================================================

    audio_file = section.get("audio_file")

    if audio_file:

        html += f"""

<div class="audio-row">

<button
class="audio-button"
onclick="playAudio('../audio/santali_audio/lesson_g2/{audio_file}')"
>
🔊
</button>

</div>
"""


    # =====================================================
    # VISUAL
    # =====================================================

    if section.get("visual"):

        html += f"""

<div class="visual">

{section["visual"]}

</div>
"""


    # =====================================================
    # NUMBER CARDS
    # =====================================================

    if section.get("numbers"):

        html += """

<div class="number-grid">
"""


        for item in section["numbers"]:

            number = item["number"]

            html += f"""

<div class="number-card">


<div class="number">

{number}

</div>


<div class="number-visual">

{item["visual"]}

</div>


<div class="hindi">

Hindi: {item["hindi"]}

</div>


<div class="santali">

Santali: {item["santali"]}

</div>


<div class="audio-row">

<button
class="audio-button"
onclick="playAudio('../audio/santali_audio/numbers/number_{number}.wav')"
>
🔊
</button>

</div>


</div>
"""


        html += """

</div>
"""


    # =====================================================
    # CLOSE SECTION
    # =====================================================

    html += """

</div>
"""


# =========================================================
# AUDIO JAVASCRIPT
# =========================================================

html += """

<script>

let currentAudio = null;


function playAudio(path) {

    if (currentAudio) {

        currentAudio.pause();

        currentAudio.currentTime = 0;

    }


    const audioUrl =
        new URL(path, window.location.href).href;


    currentAudio =
        new Audio(audioUrl);


    currentAudio.addEventListener(
        "error",
        function() {

            console.error(
                "Audio could not be loaded:",
                audioUrl
            );

            alert(
                "Audio file could not be loaded. Please check the filename and folder path."
            );

        },
        { once: true }
    );


    currentAudio.play().catch(
        function(error) {

            console.error(
                "Audio playback error:",
                error
            );

            alert(
                "Audio could not be played."
            );

        }
    );

}

</script>


</div>

</body>

</html>
"""


# =========================================================
# SAVE HTML
# =========================================================

OUTPUT_FILE.write_text(
    html,
    encoding="utf-8"
)


print("Grade 2 lesson with all section audio generated successfully!")

print(f"Saved to: {OUTPUT_FILE}")
