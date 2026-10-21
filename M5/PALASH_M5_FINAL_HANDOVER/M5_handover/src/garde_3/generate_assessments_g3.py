import json
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent
INPUT_FILE = BASE_DIR / "data" / "assessments_g3_counting.json"
OUTPUT_DIR = BASE_DIR / "output"
OUTPUT_FILE = OUTPUT_DIR / "assessments_g3_counting.html"

OUTPUT_DIR.mkdir(exist_ok=True)

with open(INPUT_FILE, "r", encoding="utf-8") as file:
    assessment = json.load(file)


html = f"""<!DOCTYPE html>
<html lang="hi">

<head>

<meta charset="UTF-8">

<meta name="viewport" content="width=device-width, initial-scale=1.0">

<title>{assessment["title"]}</title>

<style>

body {{
    font-family: Arial, sans-serif;
    margin: 0;
    padding: 20px;
}}

.container {{
    max-width: 1100px;
    margin: auto;
}}

.header {{
    text-align: center;
    margin-bottom: 30px;
}}

.question {{
    border: 1px solid #999;
    border-radius: 12px;
    padding: 25px;
    margin-bottom: 25px;
}}

.question h3 {{
    margin-top: 0;
}}

.hindi {{
    font-size: 26px;
    font-weight: bold;
    margin-bottom: 10px;
}}

.santali {{
    font-size: 24px;
    margin-bottom: 15px;
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

.option {{
    display: block;
    padding: 15px;
    margin: 8px 0;
    border: 1px solid #aaa;
    border-radius: 8px;
    cursor: pointer;
}}

.correct-answer {{
    font-weight: bold;
    border: 2px solid green;
}}

.wrong {{
    font-weight: bold;
    border: 2px solid red;
}}

.unanswered {{
    background-color: #eeeeee;
}}

.check-button {{
    padding: 14px 30px;
    font-size: 20px;
    cursor: pointer;
}}

.result {{
    margin-top: 20px;
    font-size: 24px;
    font-weight: bold;
}}

</style>

</head>

<body>

<div class="container">

<div class="header">

<h1>PALASH 🌸</h1>

<h2>{assessment["title"]}</h2>

<p>
Grade {assessment["grade"]} |
{assessment["subject"]} |
{assessment["topic"]}
</p>

<p>
<b>Learning Objective:</b>
{assessment["learning_objective"]}
</p>

</div>
"""


for index, q in enumerate(assessment["questions"], start=1):

    qid = q["question_id"]

    html += f"""
<div class="question">

<h3>
Question {index}
</h3>


<div class="hindi">
{q.get("hindi_text", q.get("question", ""))}
</div>


<div class="audio-row">

<div class="santali">
{q.get("santali_text", "")}
</div>

<button
class="audio-button"
onclick="playAudio('../audio/santali_audio/assessment_g3/{qid}.wav')"
>
🔊
</button>

</div>


<p>

<b>
{q.get("question", "")}
</b>

</p>
"""


    for option in q.get("options", []):

        html += f"""
<label class="option">

<input
type="radio"
name="{qid}"
value="{option}"
>

<span>
{option}
</span>

</label>
"""


    html += """
</div>
"""


answers = {
    q["question_id"]: str(q["correct_answer"])
    for q in assessment["questions"]
}


html += f"""

<button
type="button"
class="check-button"
onclick="checkAnswers()"
>
Submit Assessment ✅
</button>


<div id="result" class="result"></div>


<script>

const answers = {json.dumps(answers, ensure_ascii=False)};

let currentAudio = null;


function playAudio(path) {{

    if (currentAudio) {{
        currentAudio.pause();
        currentAudio.currentTime = 0;
    }}

    const audioUrl = new URL(path, window.location.href).href;

    currentAudio = new Audio(audioUrl);

    currentAudio.addEventListener("error", function() {{
        console.error("Audio could not be loaded:", audioUrl);
        alert("Audio file could not be loaded. Please check the filename and folder path.");
    }}, {{ once: true }});

    currentAudio.play().catch(function(error) {{
        console.error("Audio playback error:", error);
        alert("Audio could not be played.");
    }});
}}


function checkAnswers() {{

    let score = 0;

    const total = Object.keys(answers).length;


    for (const questionId in answers) {{

        const correctAnswer = answers[questionId];

        const allOptions =
            document.querySelectorAll(
                `input[name="${{questionId}}"]`
            );

        const selected =
            document.querySelector(
                `input[name="${{questionId}}"]:checked`
            );


        allOptions.forEach(option => {{

            const label = option.parentElement;

            label.classList.remove(
                "correct-answer",
                "wrong",
                "unanswered"
            );


            if (option.value === correctAnswer) {{

                label.classList.add("correct-answer");

            }}

            else if (option.checked) {{

                label.classList.add("wrong");

            }}

            else {{

                label.classList.add("unanswered");

            }}

        }});


        if (
            selected &&
            selected.value === correctAnswer
        ) {{

            score++;

        }}

    }}


    const percentage =
        Math.round(
            (score / total) * 100
        );


    document.getElementById("result").innerHTML =
        `Score: ${{score}} / ${{total}} (${{percentage}}%)`;

}}

</script>

</div>

</body>
</html>
"""


OUTPUT_FILE.write_text(html, encoding="utf-8")

print("Assessment with audio generated successfully!")
print(f"Saved to: {OUTPUT_FILE}")