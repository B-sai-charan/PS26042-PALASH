import json
from pathlib import Path


# ============================================================
# PATHS
# ============================================================

BASE_DIR = Path(__file__).resolve().parent.parent

INPUT_FILE = (
    BASE_DIR /
    "data" /
    "flashcards_g2_counting.json"
)

OUTPUT_DIR = BASE_DIR / "output"

OUTPUT_FILE = (
    OUTPUT_DIR /
    "flashcards_g2_counting.html"
)

OUTPUT_DIR.mkdir(exist_ok=True)


# ============================================================
# LOAD FLASHCARD DATA
# ============================================================

with open(
    INPUT_FILE,
    "r",
    encoding="utf-8"
) as file:

    flashcard_data = json.load(file)


# ============================================================
# START HTML
# ============================================================

html = f"""<!DOCTYPE html>

<html lang="hi">

<head>

<meta charset="UTF-8">

<meta
name="viewport"
content="width=device-width, initial-scale=1.0"
>

<title>{flashcard_data["title"]}</title>


<style>

body {{
    font-family: Arial, sans-serif;
    margin: 0;
    padding: 20px;
    text-align: center;
}}

.container {{
    max-width: 800px;
    margin: auto;
}}

.header {{
    margin-bottom: 30px;
}}

.flashcard {{
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
    font-size: 80px;
    font-weight: bold;
}}

.visual {{
    font-size: 45px;
    margin: 25px 0;
}}

.hindi {{
    font-size: 28px;
    font-weight: normal;
    margin: 15px;
}}

.santali {{
    font-size: 25px;
    font-weight: bold;
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
    margin: 0;
    font-size: 18px;
    border: 1px solid #777;
    border-radius: 8px;
    background: white;
    cursor: pointer;
}}

.equation-audio-row {{
    margin: 20px 0;
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 15px;
    flex-wrap: wrap;
}}

.equation-audio-col {{
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
}}

.equation-audio-number {{
    font-size: 32px;
    font-weight: bold;
}}

.equation-audio-symbol {{
    font-size: 32px;
    font-weight: bold;
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


<div class="header">

<h1>PALASH 🌸</h1>

<h2>{flashcard_data["title"]}</h2>

<p>
Grade {flashcard_data["grade"]}
|
Mathematics
|
Counting and Numbers
</p>

</div>
"""


# ============================================================
# GENERATE FLASHCARDS
# ============================================================

for index, card in enumerate(
    flashcard_data["cards"]
):

    active_class = (
        "active"
        if index == 0
        else ""
    )

    card_id = card["card_id"]

    number = str(
        card["number"]
    )


    html += f"""

<div
class="flashcard {active_class}"
id="card_{index}"
>


<!-- NUMBER -->

<div class="audio-row">

<div class="card-number">

{number}

</div>
"""


    # ========================================================
    # OPERATION CARD AUDIO (equation number row)
    # ========================================================

    if card["type"] == "operation":

        html += f"""

<button
class="audio-button"
onclick="playAudio('../audio/santali_audio/flashcards/{card_id}.wav')"
>

🔊

</button>
"""


    html += """

</div>
"""


    # ========================================================
    # CARD CONTENT
    # ========================================================

    html += f"""

<div class="visual">

{card["visual"]}

</div>


<div class="hindi">

Hindi:
{card["hindi"]}

</div>


<div class="audio-row">


<div class="santali">

Santali:
{card["santali"]}

</div>


<button
class="audio-button"
onclick="playAudio('../audio/santali_audio/flashcards/{card_id}.wav')"
>

🔊

</button>


</div>
"""


    # ========================================================
    # OPERATION NUMBER AUDIO
    # ========================================================

    if card["type"] == "operation":

        operator_symbol = card["operator"]

        operation_values = [

            card["operand1"],

            card["operand2"],

            card["result"]

        ]

        symbols_between = [
            operator_symbol,
            "="
        ]


        html += """

<div class="equation-audio-row">
"""

        for position, value in enumerate(operation_values):

            html += f"""

<div class="equation-audio-col">

<div class="equation-audio-number">

{value}

</div>

<button
class="audio-button"
onclick="playAudio('../audio/santali_audio/numbers/number_{value}.wav')"
>

🔊

</button>

</div>
"""

            if position < len(symbols_between):

                html += f"""

<div class="equation-audio-symbol">

{symbols_between[position]}

</div>
"""

        html += """

</div>
"""


    # ========================================================
    # CARD COUNT
    # ========================================================

    html += f"""

<div class="card-count">

Card
{index + 1}
of
{len(flashcard_data["cards"])}

</div>


</div>
"""


# ============================================================
# NAVIGATION + JAVASCRIPT
# ============================================================

html += f"""

<button
class="nav-button"
onclick="previousCard()"
>

⬅ Previous

</button>


<button
class="nav-button"
onclick="nextCard()"
>

Next ➡

</button>


<script>


let currentCard = 0;


const totalCards =
{len(flashcard_data["cards"])};


let currentAudio = null;



// ============================================================
// PLAY AUDIO
// ============================================================

function playAudio(path) {{

    if (currentAudio) {{

        currentAudio.pause();

        currentAudio.currentTime = 0;

    }}


    // Convert relative path into a browser-safe URL

    const audioUrl =
        new URL(
            path,
            window.location.href
        ).href;


    currentAudio =
        new Audio();


    currentAudio.preload =
        "auto";


    currentAudio.src =
        audioUrl;



    // AUDIO LOAD ERROR

    currentAudio.addEventListener(

        "error",

        () => {{

            console.error(

                "Audio could not be loaded:",

                audioUrl,

                currentAudio.error

            );


            alert(

                "Audio file could not be loaded. Please verify the file name and folder structure."

            );

        }},

        {{ once: true }}

    );



    // PLAY AUDIO

    currentAudio.play()

    .catch(

        error => {{

            console.error(

                "Audio could not be played:",

                audioUrl,

                error

            );


            alert(

                "Audio could not be played."

            );

        }}

    );

}}



// ============================================================
// SHOW CARD
// ============================================================

function showCard(index) {{

    const cards =

        document.querySelectorAll(

            ".flashcard"

        );


    cards.forEach(

        card =>

            card.classList.remove(

                "active"

            )

    );


    cards[index].classList.add(

        "active"

    );

}}



// ============================================================
// NEXT CARD
// ============================================================

function nextCard() {{

    currentCard =

        (

            currentCard + 1

        )

        %

        totalCards;


    showCard(

        currentCard

    );

}}



// ============================================================
// PREVIOUS CARD
// ============================================================

function previousCard() {{

    currentCard =

        (

            currentCard - 1
            +
            totalCards

        )

        %

        totalCards;


    showCard(

        currentCard

    );

}}


</script>


</div>


</body>

</html>
"""


# ============================================================
# SAVE OUTPUT
# ============================================================

with open(

    OUTPUT_FILE,

    "w",

    encoding="utf-8"

) as file:

    file.write(
        html
    )


print(
    "Flashcards with audio generated successfully!"
)


print(
    f"Saved to: {OUTPUT_FILE}"
)