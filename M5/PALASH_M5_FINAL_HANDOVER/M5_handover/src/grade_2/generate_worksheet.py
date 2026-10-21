import json
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent
INPUT_FILE = BASE_DIR / "data" / "worksheet_g2_counting.json"
OUTPUT_DIR = BASE_DIR / "output"
OUTPUT_FILE = OUTPUT_DIR / "worksheet_g2_counting.html"
OUTPUT_DIR.mkdir(exist_ok=True)

SANTALI_NUMBERS = {
    "1": "ᱢᱤᱫ", "2": "ᱵᱟᱨ", "3": "ᱯᱮ", "4": "ᱯᱩᱱ", "5": "ᱢᱚᱬᱮ",
    "6": "ᱛᱩᱨᱩᱭ", "7": "ᱮᱭᱟᱭ", "8": "ᱤᱨᱟᱹᱞ", "9": "ᱟᱨᱮ", "10": "ᱜᱮᱞ"
}

with open(INPUT_FILE, "r", encoding="utf-8") as file:
    worksheet = json.load(file)

html = f"""<!DOCTYPE html>
<html lang="hi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>{worksheet["title"]}</title>
<style>
body {{ font-family: Arial, sans-serif; margin: 0; padding: 20px; }}
.container {{ max-width: min(95vw, 1100px); margin: auto; }}
.header {{ text-align: center; margin-bottom: 30px; }}
.header h1 {{ font-size: clamp(28px, 4vw, 44px); margin-bottom: 8px; }}
.header h2 {{ font-size: clamp(20px, 3vw, 32px); margin: 8px 0; }}
.header p {{ font-size: clamp(16px, 2.2vw, 24px); }}
.question {{ border: 1px solid #999; border-radius: 12px; padding: clamp(16px, 2.5vw, 32px); margin-bottom: 25px; }}
.question h3 {{ margin-top: 0; font-size: clamp(20px, 3vw, 30px); }}
.hindi {{ font-size: clamp(22px, 3.5vw, 36px); font-weight: bold; margin-bottom: 10px; }}
.santali-row {{ display: flex; align-items: center; gap: 10px; margin-bottom: 15px; flex-wrap: wrap; }}
.santali {{ font-size: clamp(20px, 3vw, 32px); }}
.visual {{ font-size: clamp(30px, 5.5vw, 60px); margin: 15px 0; }}
.audio-button {{ padding: clamp(7px, 1.2vw, 16px) clamp(10px, 1.8vw, 20px); font-size: clamp(16px, 2.2vw, 28px); cursor: pointer; border: 1px solid #777; border-radius: 8px; background: white; }}
.option {{ display: flex; align-items: center; gap: 8px; padding: clamp(12px, 1.8vw, 22px); margin: 8px 0; border: 1px solid #aaa; border-radius: 8px; cursor: pointer; color: black; background-color: white; font-size: clamp(18px, 2.5vw, 28px); }}
.option input {{ margin-right: 2px; }}
.option-number {{ font-weight: bold; }}
.option-santali {{ margin-left: 8px; }}
.correct-answer {{ color: green !important; font-weight: bold; background-color: rgba(0, 255, 0, 0.6) !important; border: 1px solid green !important; }}
.wrong {{ color: black !important; font-weight: bold; background-color: rgba(255, 0, 0, 0.6) !important; border: 1px solid red !important; }}
.unanswered {{ color: black !important; background-color: #d3d3d3 !important; border: 1px solid #999 !important; }}
.text-answer {{ width: 100%; max-width: 400px; padding: clamp(10px, 1.5vw, 18px); font-size: clamp(18px, 2.5vw, 28px); margin-top: 10px; border: 1px solid #999; border-radius: 6px; }}
.check-button {{ padding: clamp(14px, 2vw, 24px) clamp(25px, 3.5vw, 45px); font-size: clamp(18px, 2.5vw, 30px); cursor: pointer; margin-top: 10px; }}
.result {{ margin-top: 25px; padding: 20px; font-size: clamp(20px, 3vw, 32px); font-weight: bold; text-align: center; }}
.answer-feedback {{ margin-top: 10px; padding: 10px; font-size: clamp(16px, 2.2vw, 24px); font-weight: bold; border-radius: 6px; }}
.correct-feedback {{ color: green; background-color: rgba(0, 255, 0, 0.6); }}
</style>
</head>
<body>
<div class="container">
<div class="header">
<h1>PALASH 🌸</h1>
<h2>{worksheet["title"]}</h2>
<p>Grade {worksheet["grade"]} | {worksheet["subject"]} | {worksheet["topic"]}</p>
<p>Answer all questions and click Submit Answers.</p>
</div>
<form id="worksheetForm">
"""

for index, question in enumerate(worksheet["questions"], start=1):
    question_id = question["question_id"]
    html += f"""
<div class="question">
<h3>Question {index}</h3>
<div class="hindi">{question["hindi"]}</div>
<div class="santali-row">
<div class="santali">{question["santali"]}</div>
<button type="button" class="audio-button" onclick="playAudio('../audio/santali_audio/worksheet/{question_id}_question.wav')">🔊</button>
</div>
"""

    if "visual" in question:
        html += f"""<div class="visual">{question["visual"]}</div>"""

    options = question.get("options", question.get("numbers", []))

    if options:
        for option in options:
            option = str(option)
            santali_option = SANTALI_NUMBERS.get(option, "")
            option_display = f'<span class="option-number">{option}</span>'
            if santali_option:
                option_display += f'<span class="option-santali">— {santali_option}</span>'

            html += f"""
<label class="option">
<input type="radio" name="{question_id}" value="{option}">
{option_display}
<button type="button" class="audio-button" onclick="event.preventDefault(); event.stopPropagation(); playAudio('../audio/santali_audio/numbers/number_{option}.wav')">🔊</button>
</label>
"""
    else:
        html += f"""
<input type="text" id="{question_id}" class="text-answer" placeholder="ᱟᱢᱟᱜ ᱛᱮᱞᱟ ᱚᱞ ᱢᱮ">
"""

    html += "</div>"

html += """
<button type="button" class="check-button" onclick="checkAnswers()">Submit Answers ✅</button>
<div id="result" class="result"></div>

<script>
let currentAudio = null;

function playAudio(path) {
    if (currentAudio) {
        currentAudio.pause();
        currentAudio.currentTime = 0;
    }

    // Convert the relative path into a browser-safe URL.
    const audioUrl = new URL(path, window.location.href).href;

    currentAudio = new Audio();
    currentAudio.preload = "auto";
    currentAudio.src = audioUrl;

    currentAudio.addEventListener("error", () => {
        console.error("Audio could not be loaded:", audioUrl, currentAudio.error);
        alert("Audio file could not be loaded. Please verify the file name and folder structure.");
    }, { once: true });

    currentAudio.play().catch(error => {
        console.error("Audio could not be played:", audioUrl, error);
        alert("Audio could not be played. Open the generated HTML through a local web server if your browser blocks local audio playback.");
    });
}

const answers = {
"""

for question in worksheet["questions"]:
    html += f'    "{question["question_id"]}": "{question["correct_answer"]}",\n'

html += """
};

function checkAnswers() {
    let score = 0;
    const totalQuestions = Object.keys(answers).length;

    for (const questionId in answers) {
        const correctAnswer = answers[questionId];
        const allOptions = document.querySelectorAll(`input[name="${questionId}"]`);

        if (allOptions.length > 0) {
            const selectedOption = document.querySelector(`input[name="${questionId}"]:checked`);

            allOptions.forEach(option => {
                const label = option.parentElement;
                label.classList.remove("correct-answer", "wrong", "unanswered");

                if (option.value === correctAnswer) {
                    label.classList.add("correct-answer");
                } else if (option.checked) {
                    label.classList.add("wrong");
                } else {
                    label.classList.add("unanswered");
                }
            });

            if (selectedOption && selectedOption.value === correctAnswer) score++;
        } else {
            const textInput = document.getElementById(questionId);
            if (textInput) {
                const userAnswer = textInput.value.trim();
                textInput.classList.remove("correct-answer", "wrong");

                const oldFeedback = document.getElementById(questionId + "_feedback");
                if (oldFeedback) oldFeedback.remove();

                const feedback = document.createElement("div");
                feedback.id = questionId + "_feedback";
                feedback.classList.add("answer-feedback");

                if (userAnswer === correctAnswer) {
                    textInput.classList.add("correct-answer");
                    feedback.innerHTML = "✓ Correct Answer";
                    feedback.classList.add("correct-feedback");
                    score++;
                } else {
                    textInput.classList.add("wrong");
                    feedback.innerHTML = "✓ Correct Answer: " + correctAnswer;
                    feedback.classList.add("correct-feedback");
                }

                textInput.parentNode.appendChild(feedback);
            }
        }
    }

    const percentage = (score / totalQuestions) * 100;
    document.getElementById("result").innerHTML =
        "Your Score: " + score + " / " + totalQuestions + "<br>" +
        percentage.toFixed(0) + "%";
}
</script>
</form>
</div>
</body>
</html>
"""

with open(OUTPUT_FILE, "w", encoding="utf-8") as file:
    file.write(html)

print("Worksheet with audio generated successfully!")
print(f"Saved to: {OUTPUT_FILE}")