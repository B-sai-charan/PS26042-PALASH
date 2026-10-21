import json
from pathlib import Path


# =========================================================
# PATHS
# =========================================================

BASE_DIR = Path(__file__).resolve().parent.parent

INPUT_FILE = BASE_DIR / "data" / "flashcards_g3_counting.json"

OUTPUT_DIR = BASE_DIR / "output"

OUTPUT_FILE = OUTPUT_DIR / "flashcards_g3_counting.html"

OUTPUT_DIR.mkdir(exist_ok=True)


# =========================================================
# LOAD FLASHCARD DATA
# =========================================================

with open(INPUT_FILE, "r", encoding="utf-8") as file:
    flashcard_data = json.load(file)


# =========================================================
# START HTML
# =========================================================

html = f"""<!DOCTYPE html>
<html lang="hi">

<head>

<meta charset="UTF-8">

<meta name="viewport" content="width=device-width, initial-scale=1.0">

<title>{flashcard_data["title"]}</title>


<style>

body {{
    font-family: Arial, sans-serif;
    margin: 0;
    padding: 20px;
    text-align: center;
    background: #f7f7f7;
}}


.container {{
    max-width: 1000px;
    margin: auto;
}}


.header {{
    margin-bottom: 30px;
}}


.flashcard {{
    background: white;
    border: 2px solid #333;
    border-radius: 20px;
    padding: 40px 20px;
    min-height: 350px;
    display: none;
    margin-top: 20px;
}}


.flashcard.active {{
    display: block;
}}


.card-number {{
    font-size: 90px;
    font-weight: bold;
}}


.visual {{
    font-size: 45px;
    margin: 25px 0;
    word-break: break-word;
}}


.hindi {{
    font-size: 28px;
    font-weight: bold;
    margin: 15px;
}}


.santali {{
    font-size: 25px;
    margin: 15px;
}}


.audio-row {{
    margin: 12px 0;
    display: flex;
    justify-content: center;
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


.card-count {{
    margin-top: 20px;
    font-size: 16px;
}}


.nav-button {{
    padding: 12px 25px;
    margin: 20px 10px;
    font-size: 18px;
    cursor: pointer;
}}

</style>

</head>


<body>


<div class="container">


<!-- =====================================================
     HEADER
===================================================== -->

<div class="header">

<h1>PALASH 🌸</h1>

<h2>{flashcard_data["title"]}</h2>

<p>
Grade {flashcard_data["grade"]} |
{flashcard_data["subject"]} |
{flashcard_data["topic"]}
</p>

</div>

"""


# =========================================================
# GENERATE FLASHCARDS
# =========================================================

for index, card in enumerate(flashcard_data["cards"]):

    active_class = "active" if index == 0 else ""

    card_id = card["card_id"]

    number = str(card["number"])

    html += f"""

<div class="flashcard {active_class}" id="card_{index}">


<!-- NUMBER -->

<div class="card-number">

{number}

</div>


<!-- NUMBER AUDIO BUTTON -->

<div class="audio-row">

<button
class="audio-button"
onclick="playAudio('../audio/santali_audio/numbers/number_{number}.wav')"
>
🔊
</button>

</div>


<!-- VISUAL -->

<div class="visual">

{card["visual"]}

</div>


<!-- HINDI -->

<div class="hindi">

Hindi: {card["hindi"]}

</div>


<!-- SANTALI -->

<div class="santali">

Santali: {card["santali"]}

</div>


<!-- SANTALI AUDIO BUTTON -->

<div class="audio-row">

<button
class="audio-button"
onclick="playAudio('../audio/santali_audio/flashcards_g3/{card_id}.wav')"
>
🔊
</button>

</div>


<!-- CARD COUNT -->

<div class="card-count">

Card {index + 1} of {len(flashcard_data["cards"])}

</div>


</div>

"""


# =========================================================
# NAVIGATION + JAVASCRIPT
# =========================================================

html += f"""

<button class="nav-button" onclick="previousCard()">

⬅ Previous

</button>


<button class="nav-button" onclick="nextCard()">

Next ➡

</button>


<script>

let currentCard = 0;

const totalCards = {len(flashcard_data["cards"])};

let currentAudio = null;


function playAudio(path) {{

    if (currentAudio) {{

        currentAudio.pause();
        currentAudio.currentTime = 0;

    }}


    const audioUrl =
        new URL(path, window.location.href).href;


    currentAudio =
        new Audio(audioUrl);


    currentAudio.addEventListener(
        "error",
        function() {{

            console.error(
                "Audio could not be loaded:",
                audioUrl
            );

            alert(
                "Audio file could not be loaded. Please check the file name and folder structure."
            );

        }},
        {{ once: true }}
    );


    currentAudio.play().catch(
        function(error) {{

            console.error(
                "Audio playback error:",
                error
            );

            alert(
                "Audio could not be played."
            );

        }}
    );

}}


function showCard(index) {{

    const cards =
        document.querySelectorAll(".flashcard");


    cards.forEach(card => {{

        card.classList.remove("active");

    }});


    cards[index].classList.add("active");

}}


function nextCard() {{

    currentCard =
        (currentCard + 1) % totalCards;


    showCard(currentCard);

}}


function previousCard() {{

    currentCard =
        (currentCard - 1 + totalCards)
        % totalCards;


    showCard(currentCard);

}}


</script>


</div>


</body>


</html>

"""


# =========================================================
# SAVE HTML
# =========================================================

with open(
    OUTPUT_FILE,
    "w",
    encoding="utf-8"
) as file:

    file.write(html)


# =========================================================
# SUCCESS MESSAGE
# =========================================================

print("Grade 3 flashcards generated successfully!")

print(f"Saved to: {OUTPUT_FILE}")