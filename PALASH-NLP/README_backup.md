\# PALASH Translation Service



Hindi → Santali Translation Module for PALASH



PALASH is an AI-powered vernacular education system designed to support

mother-tongue education in multilingual and tribal-language classrooms.



This repository contains the NLP translation component responsible for

Hindi → Santali text translation.



\---



\## 1. Purpose



The purpose of this module is to provide a simple, integration-ready

Hindi → Santali translation service for the PALASH educational platform.



The current MVP focuses on:



\- Hindi → Santali translation

\- FLN-oriented educational sentences

\- Simple Python service interface

\- Input validation and error handling

\- Latency measurement

\- Functional testing

\- Speech-pipeline integration readiness



The current implementation is intended for the internal hackathon MVP.



\---



\## 2. Current Status



\### NLP Translation Module



\*\*Status: INTEGRATED AND FUNCTIONALLY TESTED\*\*



The Hindi → Santali translation baseline is working using

AI4Bharat IndicTrans2.



The translation service has been successfully integrated into the

PALASH speech pipeline.



\### Current validated pipeline



```text

Microphone

&#x20;   ↓

Faster Whisper ASR

&#x20;   ↓

Hindi Text

&#x20;   ↓

translate(hindi\_text)

&#x20;   ↓

Santali Text (Ol Chiki)

&#x20;   ↓

Transliteration Fallback

&#x20;   ↓

TTS

&#x20;   ↓

Audio Output

```



\### Translation Evidence



\- Translation model loads successfully on CUDA.

\- Hindi → Santali translation is working.

\- `translation\_service.py` provides the `translate()` function.

\- Translation service returns structured results.

\- Empty input is handled correctly.

\- Unsupported source languages are rejected correctly.

\- Unsupported target languages are rejected correctly.

\- Short sentences are handled.

\- Long sentences are handled.

\- 6/6 translation test groups passed.

\- 5/5 FLN functional translation tests passed.

\- FLN dataset contains 33 evaluation sentences.

\- Translation latency has been measured on an NVIDIA RTX 3060 Laptop GPU.

\- PALASH-Speech has successfully integrated the translation function.



\### Integration Status



The translation module is no longer an integration blocker for the

PALASH speech pipeline.



The speech pipeline can call the translation service directly through

the Python `translate()` function.



\---



\## 3. Integration API



The translation service exposes the following Python function:



```python

from translation\_service import translate



result = translate(hindi\_text)



santali\_text = result\["translated\_text"]

```



\### Example



```python

from translation\_service import translate



result = translate("आज हम गिनती सीखेंगे")



print(result\["translated\_text"])

```



Example response structure:



```python

{

&#x20;   "translated\_text": "...",

&#x20;   "source\_language": "hin\_Deva",

&#x20;   "target\_language": "sat\_Olck",

&#x20;   "latency\_ms": 0,

&#x20;   "status": "success"

}

```



The actual `latency\_ms` value depends on the hardware and inference

conditions.



\### Error response



If an input or language configuration is invalid, the service returns

an error response containing:



```python

{

&#x20;   "translated\_text": None,

&#x20;   "source\_language": "...",

&#x20;   "target\_language": "...",

&#x20;   "latency\_ms": 0,

&#x20;   "status": "error",

&#x20;   "error": "..."

}

```



\---



\## 4. Translation Model



\### Model



AI4Bharat IndicTrans2



\### Model Identifier



```text

ai4bharat/indictrans2-indic-indic-1B

```



\### Language Direction



```text

Hindi → Santali

```



\### Source Language Code



```text

hin\_Deva

```



\### Target Language Code



```text

sat\_Olck

```



The current implementation supports Hindi as the source language and

Santali in Ol Chiki as the target language.



\---



\## 5. Hardware Tested



The translation service has been tested on:



```text

GPU:

NVIDIA GeForce RTX 3060 Laptop GPU



VRAM:

6 GB

```



The model currently runs using CUDA on the development machine.



CPU inference is also supported by the implementation, although the

reported latency measurements in this repository were obtained on the

RTX 3060 development GPU.



\---



\## 6. Software Environment



\### Python



```text

Python 3.10.8

```



\### PyTorch



```text

2.11.0+cu128

```



\### Transformers



```text

4.38.2

```



\### huggingface-hub



```text

0.23.5

```



\### Additional Dependencies



```text

IndicTransToolkit

sentencepiece

```



See `requirements.txt` for the complete dependency list.



\---



\## 7. Project Structure



```text

PALASH-NLP/

│

├── data/

│   ├── gold/

│   ├── processed/

│   ├── raw/

│   └── hindi\_santali\_test.csv

│

├── evaluation/

│   └── latency\_report.txt

│

├── glossary/

│

├── models/

│

├── scripts/

│

├── IndicTrans2/

│

├── fln\_translation.csv

├── evaluation\_sentences.csv

│

├── translation\_service.py

├── translation\_test.py

├── translator.py

│

├── test\_indictrans.py

├── test\_hindi\_santali.py

├── test\_hindi\_santali\_compare.py

├── test\_translator.py

│

├── evaluate\_hindi\_santali.py

├── diagnose\_translation.py

│

├── requirements.txt

└── README.md

```



The `.venv/` directory is intentionally not included in the documented

project structure because it is a local Python virtual environment and

should not be treated as application source code.



\---



\## 8. Installation



\### Step 1: Create a virtual environment



From the `PALASH-NLP` directory:



```powershell

python -m venv .venv

```



\### Step 2: Activate the virtual environment



On Windows PowerShell:



```powershell

.\\.venv\\Scripts\\Activate.ps1

```



The terminal should show:



```text

(.venv)

```



\### Step 3: Install dependencies



```powershell

python -m pip install -r requirements.txt

```



\### Step 4: Verify Python



```powershell

python --version

```



Expected development environment:



```text

Python 3.10.x

```



\### Step 5: Verify PyTorch



```powershell

python -c "import torch; print(torch.\_\_version\_\_)"

```



The tested environment reports:



```text

2.11.0+cu128

```



\### Step 6: Verify CUDA availability



```powershell

python -c "import torch; print(torch.cuda.is\_available())"

```



On the tested development machine this should return:



```text

True

```



\---



\## 9. Running the Translation Service



The translation service can be imported directly from Python.



Run:



```powershell

python -c "from translation\_service import translate; print(translate('आज हम गिनती सीखेंगे'))"

```



The model will load on the available device.



Expected output structure:



```text

Loading translation model on cuda...

Translation model ready.



{

&#x20;   'translated\_text': '...',

&#x20;   'source\_language': 'hin\_Deva',

&#x20;   'target\_language': 'sat\_Olck',

&#x20;   'latency\_ms': ...,

&#x20;   'status': 'success'

}

```



The first invocation may take longer because of model loading and

GPU warm-up.



\---



\## 10. Functional Testing



The main translation test suite is:



```text

translation\_test.py

```



Run:



```powershell

python .\\translation\_test.py

```



The test suite covers:



1\. Valid Hindi input

2\. Empty input

3\. Unsupported source language

4\. Short sentence

5\. Long sentence

6\. FLN educational sentences



The current validated result is:



```text

6/6 test groups passed

```



\---



\## 11. FLN Functional Testing



The current FLN functional test set contains 5 sentences.



The latest validated result is:



```text

FLN translations successful: 5/5

```



The tested examples include:



\- Counting from one to ten

\- Identifying triangle sides

\- Basic addition

\- Shape identification

\- Classroom reading instructions



These tests demonstrate functional model behavior for selected

FLN-oriented educational inputs.



\---



\## 12. FLN Evaluation Dataset



The repository contains:



```text

fln\_translation.csv

```



The current dataset contains:



```text

33 evaluation sentences

```



The dataset includes fields for:



```text

id

hindi

santali

category

topic

translation\_status

```



The current translation status for the dataset entries is:



```text

model\_generated\_unvalidated

```



This means the Santali reference translations are currently

model-generated and have not been independently validated by a native

Santali speaker.



Therefore, these references must not be described as verified

ground-truth translations.



\---



\## 13. Translation Quality



No formal translation accuracy score is currently claimed.



In particular, the current functional tests do not establish:



\- BLEU score

\- chrF score

\- human evaluation score

\- native-speaker accuracy

\- formal translation quality



Native-speaker validation is required before using the current

model-generated Santali references as formal ground truth for a

translation-quality evaluation.



\---



\## 14. Latency Evaluation



Translation latency has been measured on the development machine:



```text

GPU:

NVIDIA GeForce RTX 3060 Laptop GPU



VRAM:

6 GB

```



The evaluation results are stored in:



```text

evaluation/latency\_report.txt

```



The latest functional test demonstrates that FLN translations can

execute successfully on the development GPU.



\### Important latency limitation



The first translation invocation may include:



\- Model loading

\- CUDA initialization

\- GPU warm-up



Therefore, the first invocation can be significantly slower than

subsequent inference calls.



The reported translation latency is a development-machine inference

measurement.



It is not an end-to-end PALASH application latency measurement.



It does not establish performance on an Android device.



It does not establish Android offline inference.



It does not establish a guaranteed latency for every translation.



\---



\## 15. Speech Pipeline Integration



The translation service has been integrated with the PALASH-Speech

pipeline.



The integration path is:



```text

Microphone

&#x20;   ↓

Faster Whisper ASR

&#x20;   ↓

Hindi Text

&#x20;   ↓

translation\_service.translate()

&#x20;   ↓

Santali Text

&#x20;   ↓

Transliteration Fallback

&#x20;   ↓

TTS

&#x20;   ↓

Audio Output

```



The speech pipeline can use:



```python

from translation\_service import translate



result = translate(hindi\_text)



if result\["status"] == "success":

&#x20;   santali\_text = result\["translated\_text"]

```



This provides a simple interface between the ASR component and the

Santali output/TTS component.



\---



\## 16. Language Configuration



Default source language:



```text

hin\_Deva

```



Default target language:



```text

sat\_Olck

```



The current service validates these language codes before performing

translation.



Unsupported source languages return an error instead of attempting

translation.



\---



\## 17. Model Loading



The translation model is loaded when `translation\_service.py` is

imported.



On a CUDA-enabled machine, the service uses:



```text

CUDA

```



and loads the model using half-precision:



```text

float16

```



On systems without CUDA, the implementation falls back to CPU

execution.



The model is placed in evaluation mode before inference.



\---



\## 18. Input Processing



The service performs the following processing steps:



```text

Hindi Text

&#x20;   ↓

Input Validation

&#x20;   ↓

IndicTransToolkit Preprocessing

&#x20;   ↓

IndicTrans2 Tokenization

&#x20;   ↓

Model Generation

&#x20;   ↓

Token Decoding

&#x20;   ↓

IndicTransToolkit Postprocessing

&#x20;   ↓

Santali Text

```



The current implementation uses deterministic generation rather than

sampling.



\---



\## 19. Error Handling



The translation service validates:



\### Empty input



Example:



```python

translate("")

```



Expected behavior:



```text

status: error

error: text cannot be empty

```



\### Invalid input type



The service rejects non-string input.



\### Unsupported source language



Example:



```python

translate(

&#x20;   "Hello",

&#x20;   source\_language="eng\_Latn"

)

```



Expected behavior:



```text

status: error

error: Unsupported source language: eng\_Latn

```



\### Unsupported target language



The service similarly rejects target languages that are not currently

supported by this implementation.



\---



\## 20. Current Test Evidence



\### Translation Functional Tests



```text

Test groups:

6



Passed:

6



Failed:

0



Result:

6/6 test groups passed

```



\### FLN Functional Tests



```text

FLN test cases:

5



Successful:

5



Failed:

0



Result:

5/5 successful

```



\### Direct API Test



The following API call has been successfully tested:



```python

from translation\_service import translate



result = translate("आज हम गिनती सीखेंगे")

```



The service successfully returned:



```text

status: success

```



with Santali output in Ol Chiki.



\---



\## 21. Current Limitations



The current internal hackathon MVP has the following limitations:



\- Santali outputs are model-generated.

\- FLN Santali references are currently unvalidated.

\- Native-speaker validation is pending.

\- No formal translation accuracy score is claimed.

\- Android offline inference has not yet been demonstrated.

\- End-to-end PALASH application latency has not yet been formally measured by this repository.

\- Translation latency measurements were performed on the RTX 3060 development machine.

\- Initial model/GPU warm-up can be slower than subsequent inference.

\- The current implementation focuses on Hindi → Santali rather than

&#x20; supporting all Indian-language translation directions.



\---



\## 22. Recommended Integration Pattern



The backend or speech pipeline should call:



```python

from translation\_service import translate



result = translate(hindi\_text)

```



Then check:



```python

if result\["status"] == "success":

&#x20;   santali\_text = result\["translated\_text"]

else:

&#x20;   error = result\["error"]

```



This allows the calling application to handle successful translations

and failures explicitly.



\---



\## 23. Development Commands



\### Run direct translation test



```powershell

python -c "from translation\_service import translate; print(translate('आज हम गिनती सीखेंगे'))"

```



\### Run complete translation tests



```powershell

python .\\translation\_test.py

```



\### Check PyTorch



```powershell

python -c "import torch; print(torch.\_\_version\_\_)"

```



\### Check CUDA



```powershell

python -c "import torch; print(torch.cuda.is\_available())"

```



\### Check dataset



```powershell

python -c "import pandas as pd; df=pd.read\_csv('./fln\_translation.csv'); print('Rows:',len(df)); print('Columns:',list(df.columns)); print(df\['translation\_status'].value\_counts().to\_dict())"

```



\---



\## 24. Internal Hackathon Scope



This implementation is intended for the internal PALASH hackathon MVP.



The immediate objective is to demonstrate a working:



```text

Hindi Speech

&#x20;   ↓

Hindi Text

&#x20;   ↓

Hindi → Santali Translation

&#x20;   ↓

Santali Text

&#x20;   ↓

Santali Speech

```



The current NLP module provides the Hindi → Santali translation stage

of this pipeline.



Further production-level improvements such as native-speaker

validation, formal translation-quality evaluation, model optimization,

mobile deployment, and Android offline inference are outside the

current demonstrated scope.



\---



\## 25. Final Status



```text

PALASH Hindi → Santali Translation Service



Translation Model:

AI4Bharat IndicTrans2



Model:

ai4bharat/indictrans2-indic-indic-1B



Language:

Hindi → Santali



Source:

hin\_Deva



Target:

sat\_Olck



Translation API:

translate(text)



Functional Translation Tests:

6/6 PASSED



FLN Functional Tests:

5/5 PASSED



FLN Evaluation Dataset:

33 sentences



CUDA Development Testing:

COMPLETED



PALASH-Speech Integration:

SUCCESSFUL



End-to-End Speech Pipeline:

INTEGRATED



Native-Speaker Validation:

PENDING



Formal Translation Accuracy:

NOT YET EVALUATED



Android Offline Inference:

NOT YET DEMONSTRATED

```



\---



\## 26. Conclusion



The PALASH NLP translation module currently provides a working

Hindi → Santali translation service using AI4Bharat IndicTrans2.



The module exposes a simple Python API through:



```python

translate(text)

```



Functional testing has passed for the current test suite, including

FLN-oriented educational sentences.



The service has also been successfully integrated into the

PALASH-Speech pipeline.



The current implementation should be considered an internal hackathon

MVP and not a formally validated production translation system.

