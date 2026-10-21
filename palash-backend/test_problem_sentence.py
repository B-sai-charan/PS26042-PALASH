from app.services.translation_service import translate

tests = [
    "लाभ प्राप्त करने के लिए किसानों को आवश्यक दस्तावेज जमा करने होंगे।",
    "लाभ लेने के लिए किसानों को जरूरी दस्तावेज जमा करने होंगे।",
    "किसानों को लाभ पाने के लिए सभी आवश्यक दस्तावेज जमा करने होंगे।",
    "किसानों को योजना का लाभ लेने के लिए जरूरी कागजात जमा करने होंगे।",
    "लाभ प्राप्त करने हेतु किसानों को आवश्यक दस्तावेज प्रस्तुत करने होंगे।",
]

print("=" * 90)
print("PALASH DOCUMENT-REQUIREMENT TRANSLATION TEST")
print("=" * 90)

for i, text in enumerate(tests, 1):

    result = translate(
        text,
        source_language="hin_Deva",
        target_language="sat_Olck",
    )

    print(f"\n[{i}]")
    print("Hindi   :", text)
    print("Santali :", result.get("translated_text"))
    print("Latency :", result.get("latency_ms"), "ms")
    print("Status  :", result.get("status"))

print("\n" + "=" * 90)
