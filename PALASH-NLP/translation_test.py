import time

from translation_service import translate


def print_result(test_name, result):
    print(f"\n[{test_name}]")

    if result.get("status") == "success":
        print("PASS")
        print("Hindi/Sentence:", result.get("input_text"))
        print("Santali:", result.get("translated_text"))
        print("Latency:", result.get("latency_ms"), "ms")
    else:
        print("PASS (expected error)")
        print("Status:", result.get("status"))
        print("Error:", result.get("error"))


def test_valid_hindi():
    result = translate("नमस्ते, आप कैसे हैं?")
    result["input_text"] = "नमस्ते, आप कैसे हैं?"
    return result


def test_empty_input():
    try:
        result = translate("")
        return result
    except Exception as e:
        return {
            "status": "error",
            "error": str(e)
        }


def test_unsupported_language():
    try:
        result = translate(
            "नमस्ते",
            source_language="eng_Latn",
            target_language="sat_Olck"
        )
        return result
    except Exception as e:
        return {
            "status": "error",
            "error": str(e)
        }


def test_short_sentence():
    result = translate("पढ़ो।")
    result["input_text"] = "पढ़ो।"
    return result


def test_long_sentence():
    text = (
        "आज हम कक्षा में एक से दस तक की गिनती सीखेंगे "
        "और फिर सभी बच्चे मिलकर संख्याओं को पहचानने का अभ्यास करेंगे।"
    )

    result = translate(text)
    result["input_text"] = text
    return result


def test_fln_sentences():
    sentences = [
        "गिनती एक से दस तक बोलो।",
        "त्रिभुज में कितनी भुजाएँ होती हैं?",
        "दो और तीन को जोड़ने पर कितना होगा?",
        "वृत्त को पहचानो।",
        "किताब खोलो और पाठ पढ़ो।",
    ]

    results = []

    for sentence in sentences:
        result = translate(sentence)
        result["input_text"] = sentence
        results.append(result)

    return results


def main():

    print("=" * 60)
    print("PALASH TRANSLATION SERVICE TEST")
    print("=" * 60)

    passed = 0
    failed = 0

    # --------------------------------------------------
    # TEST 1: Valid Hindi
    # --------------------------------------------------

    try:
        result = test_valid_hindi()
        print_result("TEST 1 - Valid Hindi", result)

        if result.get("status") == "success":
            passed += 1
        else:
            failed += 1

    except Exception as e:
        print("\n[TEST 1 - Valid Hindi]")
        print("FAIL:", e)
        failed += 1

    # --------------------------------------------------
    # TEST 2: Empty input
    # --------------------------------------------------

    try:
        result = test_empty_input()

        print("\n[TEST 2 - Empty Input]")

        if result.get("status") == "error":
            print("PASS")
            print("Error handled correctly:", result.get("error"))
            passed += 1
        else:
            print("FAIL")
            print("Empty input was accepted unexpectedly.")
            failed += 1

    except Exception as e:
        print("FAIL:", e)
        failed += 1

    # --------------------------------------------------
    # TEST 3: Unsupported language
    # --------------------------------------------------

    try:
        result = test_unsupported_language()

        print("\n[TEST 3 - Unsupported Language]")

        if result.get("status") == "error":
            print("PASS")
            print("Error handled correctly:", result.get("error"))
            passed += 1
        else:
            print("FAIL")
            print(result)
            failed += 1

    except Exception as e:
        print("FAIL:", e)
        failed += 1

    # --------------------------------------------------
    # TEST 4: Short sentence
    # --------------------------------------------------

    try:
        result = test_short_sentence()
        print_result("TEST 4 - Short Sentence", result)

        if result.get("status") == "success":
            passed += 1
        else:
            failed += 1

    except Exception as e:
        print("\n[TEST 4 - Short Sentence]")
        print("FAIL:", e)
        failed += 1

    # --------------------------------------------------
    # TEST 5: Long sentence
    # --------------------------------------------------

    try:
        result = test_long_sentence()
        print_result("TEST 5 - Long Sentence", result)

        if result.get("status") == "success":
            passed += 1
        else:
            failed += 1

    except Exception as e:
        print("\n[TEST 5 - Long Sentence]")
        print("FAIL:", e)
        failed += 1

    # --------------------------------------------------
    # TEST 6: FLN sentences
    # --------------------------------------------------

    try:
        results = test_fln_sentences()

        print("\n[TEST 6 - FLN Sentences]")
        print("-" * 60)

        fln_success = 0

        for i, result in enumerate(results, start=1):

            print(f"\nFLN {i}")
            print("Hindi:", result.get("input_text"))
            print("Santali:", result.get("translated_text"))
            print("Latency:", result.get("latency_ms"), "ms")
            print("Status:", result.get("status"))

            if result.get("status") == "success":
                fln_success += 1

        if fln_success == len(results):
            print("\nPASS")
            print(f"FLN translations successful: {fln_success}/{len(results)}")
            passed += 1
        else:
            print("\nFAIL")
            print(f"FLN translations successful: {fln_success}/{len(results)}")
            failed += 1

    except Exception as e:
        print("\n[TEST 6 - FLN Sentences]")
        print("FAIL:", e)
        failed += 1

    # --------------------------------------------------
    # FINAL SUMMARY
    # --------------------------------------------------

    print("\n" + "=" * 60)
    print("TEST SUMMARY")
    print("=" * 60)

    print("Tests Passed:", passed)
    print("Tests Failed:", failed)

    if failed == 0:
        print("\nALL TESTS PASSED")
    else:
        print("\nSOME TESTS FAILED")

    print("=" * 60)


if __name__ == "__main__":
    main()