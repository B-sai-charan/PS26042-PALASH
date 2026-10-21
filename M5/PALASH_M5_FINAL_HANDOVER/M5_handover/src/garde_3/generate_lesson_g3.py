import json
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent
INPUT_FILE = BASE_DIR / "data" / "lesson_g3_counting.json"
OUTPUT_DIR = BASE_DIR / "output"
OUTPUT_FILE = OUTPUT_DIR / "lesson_g3_counting.html"

OUTPUT_DIR.mkdir(exist_ok=True)

with open(INPUT_FILE, "r", encoding="utf-8") as file:
    lesson = json.load(file)


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
    max-width: 900px;
    margin: auto;
}}

.header {{
    text-align: center;
    margin-bottom: 25px;
}}

.meta {{
    background: white;
    border-radius: 12px;
    padding: 16px;
    margin-bottom: 20px;
}}

.section {{
    background: white;
    border: 1px solid #ccc;
    border-radius: 14px;
    padding: 20px;
    margin-bottom: 20px;
}}

.hindi {{
    font-size: 22px;
    font-weight: bold;
    margin: 10px 0;
}}

.santali {{
    font-size: 20px;
    margin: 10px 0;
}}

.audio-row {{
    display: flex;
    align-items: center;
    gap: 10px;
    flex-wrap: wrap;
}}

.audio-button {{
    padding: 8px 12px;
    font-size: 18px;
    border: 1px solid #777;
    border-radius: 8px;
    background: white;
    cursor: pointer;
}}

.number-grid {{
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    gap: 15px;
    margin-top: 15px;
}}

.number-card {{
    border: 1px solid #999;
    border-radius: 12px;
    padding: 18px;
    text-align: center;
    background: #fff;
}}

.number {{
    font-size: 42px;
    font-weight: bold;
}}

</style>
</head>

<body>

<div class="container">

<div class="header">
<h1>PALASH 🌸</h1>
<h2>{lesson["title"]}</h2>

<p>
Grade {lesson["grade"]} |
{lesson["subject"]} |
{lesson["topic"]}
</p>
</div>

<div class="meta">
<b>Learning Objective:</b>
{lesson["learning_objective"]}
</div>
"""


for section in lesson.get("sections", []):

    section_id = section.get("section_id", "L1")

    html += f"""
<div class="section">

<div class="hindi">
Hindi: {section.get("hindi", "")}
</div>

<div class="audio-row">

<div class="santali">
Santali: {section.get("santali_instruction", "")}
</div>

<button
class="audio-button"
onclick="playAudio('../audio/santali_audio/lesson_g3/{section_id}_instruction.wav')"
>
🔊
</button>

</div>
"""

    numbers = section.get("numbers", [])

    if numbers:

        html += """
<div class="number-grid">
"""

        for item in numbers:

            number = item.get("number", "")

            html += f"""
<div class="number-card">

<div class="audio-row">

<div class="number">
{number}
</div>

<button
class="audio-button"
onclick="playAudio('../audio/santali_audio/numbers/number_{number}.wav')"
>
🔊
</button>

</div>

<div class="hindi">
{item.get("hindi", "")}
</div>

<div class="audio-row">

<div class="santali">
{item.get("santali", "")}
</div>

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

    html += """
</div>
"""


html += """
<script>

let currentAudio = null;

function playAudio(path) {

    if (currentAudio) {
        currentAudio.pause();
        currentAudio.currentTime = 0;
    }

    const audioUrl = new URL(path, window.location.href).href;

    currentAudio = new Audio(audioUrl);

    currentAudio.addEventListener("error", function() {
        console.error("Audio could not be loaded:", audioUrl);
        alert("Audio file could not be loaded. Please check the file name and folder path.");
    }, { once: true });

    currentAudio.play().catch(function(error) {
        console.error("Audio playback error:", error);
        alert("Audio could not be played.");
    });
}

</script>

</div>
</body>
</html>
"""


OUTPUT_FILE.write_text(html, encoding="utf-8")

print("Lesson with audio generated successfully!")
print(f"Saved to: {OUTPUT_FILE}")