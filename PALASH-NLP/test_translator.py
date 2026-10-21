from translator import translate

sentences = [
    "The government has launched a new agricultural scheme.",
    "Farmers can apply for this scheme online.",
    "The weather is expected to be good tomorrow.",
    "Please visit the nearest office for more information.",
    "Education is important for the development of our society.",
]

for text in sentences:
    print("\nEnglish:", text)

    result = translate(
        text,
        tgt_lang="sat_Olck"
    )

    print("Santali:", result)