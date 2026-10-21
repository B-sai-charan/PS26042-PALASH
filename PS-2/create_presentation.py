import os
import sys
from pptx import Presentation
from pptx.util import Inches, Pt
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.enum.shapes import MSO_SHAPE

def build_presentation():
    prs = Presentation()
    # 16:9 Widescreen standard: 13.333 x 7.5 inches
    prs.slide_width = Inches(13.333)
    prs.slide_height = Inches(7.5)
    blank_slide_layout = prs.slide_layouts[6]

    # Theme Colors - Curated Navy, Slate, Forest Green & Saffron
    NAVY = RGBColor(12, 38, 79)         # #0C264F
    PRIMARY_BLUE = RGBColor(24, 76, 142) # #184C8E
    TEAL_HEADER = RGBColor(18, 90, 130)  # #125A82
    LIGHT_BG = RGBColor(246, 248, 252)   # #F6F8FC
    CARD_BG = RGBColor(255, 255, 255)    # White
    CARD_BORDER = RGBColor(205, 218, 235)
    HEADER_GREY = RGBColor(70, 85, 105)
    TEXT_DARK = RGBColor(20, 30, 45)
    TEXT_MUTED = RGBColor(80, 90, 105)
    ACCENT_GREEN = RGBColor(22, 120, 52) # #167834
    ACCENT_ORANGE = RGBColor(220, 90, 15)# #DC5A0F
    FOOTER_BLUE = RGBColor(15, 50, 100)
    PILL_BG = RGBColor(238, 244, 252)
    WARN_RED = RGBColor(170, 35, 35)

    def add_header(slide, title_text):
        # Logo text left
        tx_logo = slide.shapes.add_textbox(Inches(0.6), Inches(0.18), Inches(3.2), Inches(0.55))
        tf_logo = tx_logo.text_frame
        tf_logo.word_wrap = True
        p_l = tf_logo.paragraphs[0]
        p_l.text = "iTantra"
        p_l.font.size = Pt(24)
        p_l.font.bold = True
        p_l.font.color.rgb = NAVY
        p_l.font.name = "Arial"
        
        p_sub = tf_logo.add_paragraph()
        p_sub.text = "ISRO PS-26173 | Smart Automation"
        p_sub.font.size = Pt(9.5)
        p_sub.font.bold = True
        p_sub.font.color.rgb = ACCENT_ORANGE
        p_sub.font.name = "Arial"

        # Main Title Center
        tx_title = slide.shapes.add_textbox(Inches(3.6), Inches(0.18), Inches(6.6), Inches(0.6))
        tf_title = tx_title.text_frame
        tf_title.word_wrap = True
        p_t = tf_title.paragraphs[0]
        p_t.text = title_text
        p_t.alignment = PP_ALIGN.CENTER
        p_t.font.size = Pt(21)
        p_t.font.bold = True
        p_t.font.color.rgb = NAVY
        p_t.font.name = "Arial"

        # SIH Badge Right
        badge = slide.shapes.add_shape(
            MSO_SHAPE.ROUNDED_RECTANGLE, Inches(10.6), Inches(0.16), Inches(2.133), Inches(0.68)
        )
        badge.fill.solid()
        badge.fill.fore_color.rgb = RGBColor(242, 246, 255)
        badge.line.color.rgb = PRIMARY_BLUE
        badge.line.width = Pt(1.5)
        tf_b = badge.text_frame
        tf_b.vertical_anchor = MSO_ANCHOR.MIDDLE
        p_b1 = tf_b.paragraphs[0]
        p_b1.text = "SMART INDIA"
        p_b1.alignment = PP_ALIGN.CENTER
        p_b1.font.size = Pt(9.5)
        p_b1.font.bold = True
        p_b1.font.color.rgb = NAVY
        p_b2 = tf_b.add_paragraph()
        p_b2.text = "HACKATHON 2026"
        p_b2.alignment = PP_ALIGN.CENTER
        p_b2.font.size = Pt(9.5)
        p_b2.font.bold = True
        p_b2.font.color.rgb = ACCENT_ORANGE

    def add_footer(slide, slide_num):
        footer = slide.shapes.add_shape(
            MSO_SHAPE.RECTANGLE, Inches(0), Inches(7.12), Inches(13.333), Inches(0.38)
        )
        footer.fill.solid()
        footer.fill.fore_color.rgb = FOOTER_BLUE
        footer.line.fill.background()
        
        tx_foot = slide.shapes.add_textbox(Inches(0.6), Inches(7.12), Inches(12.133), Inches(0.38))
        tf = tx_foot.text_frame
        tf.vertical_anchor = MSO_ANCHOR.MIDDLE
        p = tf.paragraphs[0]
        p.text = f"@[TEAM NAME] | SIH 2026 Idea Submission | PS ID: 26173 | Indian Space Research Organisation (ISRO)"
        p.font.size = Pt(10)
        p.font.bold = True
        p.font.color.rgb = RGBColor(255, 255, 255)
        
        p_num = tf.add_paragraph()
        p_num.text = str(slide_num)
        p_num.alignment = PP_ALIGN.RIGHT
        p_num.font.size = Pt(11)
        p_num.font.bold = True
        p_num.font.color.rgb = RGBColor(255, 255, 255)

    def draw_card(slide, left, top, width, height, title="", title_bg=None, border_color=CARD_BORDER):
        card = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left, top, width, height)
        card.fill.solid()
        card.fill.fore_color.rgb = CARD_BG
        card.line.color.rgb = border_color
        card.line.width = Pt(1.2)
        
        if title:
            header_h = Inches(0.4)
            hdr = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left, top, width, header_h)
            hdr.fill.solid()
            hdr.fill.fore_color.rgb = title_bg if title_bg else HEADER_GREY
            hdr.line.fill.background()
            tf_h = hdr.text_frame
            tf_h.vertical_anchor = MSO_ANCHOR.MIDDLE
            p_h = tf_h.paragraphs[0]
            p_h.text = f"  {title.upper()}"
            p_h.font.size = Pt(10.5)
            p_h.font.bold = True
            p_h.font.color.rgb = RGBColor(255, 255, 255)
            p_h.font.name = "Arial"
        return card

    # ==========================================
    # SLIDE 1: TITLE PAGE
    # ==========================================
    s1 = prs.slides.add_slide(blank_slide_layout)
    
    bg1 = s1.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0), Inches(0), Inches(13.333), Inches(7.5))
    bg1.fill.solid()
    bg1.fill.fore_color.rgb = LIGHT_BG
    bg1.line.fill.background()

    # Top SIH Banner
    banner = s1.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0), Inches(0), Inches(13.333), Inches(1.15))
    banner.fill.solid()
    banner.fill.fore_color.rgb = NAVY
    banner.line.fill.background()
    
    tb_sih = s1.shapes.add_textbox(Inches(0.8), Inches(0.15), Inches(9.5), Inches(0.85))
    tf_sih = tb_sih.text_frame
    p_s1 = tf_sih.paragraphs[0]
    p_s1.text = "SMART INDIA HACKATHON 2026"
    p_s1.font.size = Pt(28)
    p_s1.font.bold = True
    p_s1.font.color.rgb = RGBColor(255, 255, 255)
    p_s1.font.name = "Arial"
    
    p_s2 = tf_sih.add_paragraph()
    p_s2.text = "OFFICIAL IDEA PRESENTATION FORMAT | PRELIMINARY ROUND"
    p_s2.font.size = Pt(12)
    p_s2.font.bold = True
    p_s2.font.color.rgb = ACCENT_ORANGE
    p_s2.font.name = "Arial"

    # SIH / ISRO Badge right top
    top_badge = s1.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(10.5), Inches(0.2), Inches(2.2), Inches(0.75))
    top_badge.fill.solid()
    top_badge.fill.fore_color.rgb = RGBColor(255, 255, 255)
    top_badge.line.color.rgb = ACCENT_ORANGE
    top_badge.line.width = Pt(1.5)
    tf_tb = top_badge.text_frame
    tf_tb.vertical_anchor = MSO_ANCHOR.MIDDLE
    p_tb1 = tf_tb.paragraphs[0]
    p_tb1.text = "ISRO PS #26173"
    p_tb1.alignment = PP_ALIGN.CENTER
    p_tb1.font.bold = True
    p_tb1.font.size = Pt(12)
    p_tb1.font.color.rgb = NAVY
    p_tb2 = tf_tb.add_paragraph()
    p_tb2.text = "SOFTWARE CATEGORY"
    p_tb2.alignment = PP_ALIGN.CENTER
    p_tb2.font.bold = True
    p_tb2.font.size = Pt(8.5)
    p_tb2.font.color.rgb = ACCENT_GREEN

    # Left Info Box
    card_info = draw_card(s1, Inches(0.8), Inches(1.4), Inches(7.3), Inches(5.45), "PROBLEM STATEMENT SUBMISSION DETAILS", PRIMARY_BLUE)
    
    info_tb = s1.shapes.add_textbox(Inches(1.0), Inches(1.9), Inches(6.9), Inches(4.8))
    tf_info = info_tb.text_frame
    tf_info.word_wrap = True

    details = [
        ("Problem Statement ID:", "26173", ACCENT_ORANGE),
        ("Problem Statement Title:", "iTantra - Indian Multilingual TTS & STT Aided Neural Transceiver Radio Access for Low Bitrate Links", PRIMARY_BLUE),
        ("Organization:", "Indian Space Research Organisation (ISRO)", NAVY),
        ("Department:", "Department of Space / ISRO", TEXT_DARK),
        ("Theme:", "Smart Automation", ACCENT_GREEN),
        ("Category:", "Software", TEXT_DARK),
        ("Team ID:", "[TEAM ID]", ACCENT_ORANGE),
        ("Team Name:", "[TEAM NAME]", PRIMARY_BLUE)
    ]

    for i, (label, val, col) in enumerate(details):
        p = tf_info.paragraphs[0] if i == 0 else tf_info.add_paragraph()
        p.space_after = Pt(7)
        run_lbl = p.add_run()
        run_lbl.text = f"• {label} "
        run_lbl.font.bold = True
        run_lbl.font.size = Pt(12)
        run_lbl.font.color.rgb = NAVY
        
        run_val = p.add_run()
        run_val.text = val
        run_val.font.bold = True if "ID" in label or "Title" in label or "Team" in label else False
        run_val.font.size = Pt(12)
        run_val.font.color.rgb = col

    # Right Side: Visual Innovation Hook
    card_hook = draw_card(s1, Inches(8.35), Inches(1.4), Inches(4.2), Inches(5.45), "ACOUSTIC → SEMANTIC → ACOUSTIC", NAVY)
    
    steps = [
        ("🎙 SENDER SPEECH", "Vocal Audio Input (Continuous / PTT)", PRIMARY_BLUE),
        ("⚡ OFFLINE STT + VAD", "IndicConformer / Silero VAD Edge Gating", ACCENT_GREEN),
        ("📄 COMPACT SEMANTIC TEXT", "Serialized Lightweight Binary Packet (<150 B)", ACCENT_ORANGE),
        ("📡 LOW-BITRATE LINK", "Wi-Fi Direct / BLE / Local Mesh Channel", NAVY),
        ("🔊 OFFLINE TTS RECONSTRUCTION", "VITS / Piper Native C++ Neural Synthesis", PRIMARY_BLUE),
        ("👂 RECEIVER AUDIBLE VOICE", "Instant Speech Playback / Non-stop Alert", ACCENT_GREEN)
    ]

    cur_y = Inches(1.92)
    for i, (title, sub, clr) in enumerate(steps):
        st_box = s1.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(8.55), cur_y, Inches(3.8), Inches(0.48))
        st_box.fill.solid()
        st_box.fill.fore_color.rgb = PILL_BG
        st_box.line.color.rgb = clr
        st_box.line.width = Pt(1.5)
        
        tf_st = st_box.text_frame
        tf_st.vertical_anchor = MSO_ANCHOR.MIDDLE
        p1 = tf_st.paragraphs[0]
        p1.text = title
        p1.font.bold = True
        p1.font.size = Pt(10)
        p1.font.color.rgb = clr
        
        p2 = tf_st.add_paragraph()
        p2.text = sub
        p2.font.size = Pt(8.5)
        p2.font.color.rgb = TEXT_MUTED

        cur_y += Inches(0.53)
        if i < len(steps) - 1:
            arr = s1.shapes.add_shape(MSO_SHAPE.DOWN_ARROW, Inches(10.3), cur_y - Inches(0.04), Inches(0.3), Inches(0.18))
            arr.fill.solid()
            arr.fill.fore_color.rgb = ACCENT_ORANGE
            arr.line.fill.background()
            cur_y += Inches(0.18)

    tag_box = s1.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(8.55), Inches(6.22), Inches(3.8), Inches(0.5))
    tag_box.fill.solid()
    tag_box.fill.fore_color.rgb = NAVY
    tag_box.line.fill.background()
    tf_tag = tag_box.text_frame
    tf_tag.vertical_anchor = MSO_ANCHOR.MIDDLE
    pt = tf_tag.paragraphs[0]
    pt.text = "TRANSMIT MEANING. RECONSTRUCT VOICE."
    pt.alignment = PP_ALIGN.CENTER
    pt.font.bold = True
    pt.font.size = Pt(9.5)
    pt.font.color.rgb = RGBColor(255, 255, 255)

    add_footer(s1, 1)

    # ==========================================
    # SLIDE 2: IDEA / PROPOSED SOLUTION
    # ==========================================
    s2 = prs.slides.add_slide(blank_slide_layout)
    add_header(s2, "IDEA / PROPOSED SOLUTION")

    # 3-Card Column on Left (Width: 8.0 in)
    # Card 1: Problem Existing
    c_prob = draw_card(s2, Inches(0.6), Inches(0.98), Inches(8.0), Inches(1.9), "PROBLEM EXISTING", WARN_RED)
    tb_p = s2.shapes.add_textbox(Inches(0.7), Inches(1.42), Inches(7.8), Inches(1.4))
    tf_p = tb_p.text_frame
    tf_p.word_wrap = True
    prob_points = [
        ("Data-Intensive Raw Voice: ", "Uncompressed / standard audio requires high continuous bandwidth (>64 kbps), making transmission impossible over low-bitrate, embedded radio, or congested distress channels."),
        ("Cloud Speech Failure in Disasters: ", "Existing voice translation and communication tools depend on internet-hosted APIs (Google/AWS), completely failing when local telecom infrastructure collapses."),
        ("Text Inaccessibility in Distress: ", "Text-only alerts fail non-literate populations who require immediate, actionable voice broadcasts in their regional mother tongues.")
    ]
    for i, (hd, body) in enumerate(prob_points):
        p = tf_p.paragraphs[0] if i == 0 else tf_p.add_paragraph()
        p.space_after = Pt(2)
        r1 = p.add_run()
        r1.text = f"➢ {hd}"
        r1.font.bold = True
        r1.font.size = Pt(9.5)
        r1.font.color.rgb = WARN_RED
        r2 = p.add_run()
        r2.text = body
        r2.font.size = Pt(9)
        r2.font.color.rgb = TEXT_DARK

    # Card 2: Proposed Solution
    c_sol = draw_card(s2, Inches(0.6), Inches(2.98), Inches(8.0), Inches(1.95), "PROPOSED SOLUTION: iTANTRA NEURAL TRANSCEIVER", PRIMARY_BLUE)
    tb_s = s2.shapes.add_textbox(Inches(0.7), Inches(3.42), Inches(7.8), Inches(1.45))
    tf_s = tb_s.text_frame
    tf_s.word_wrap = True
    sol_points = [
        ("Acoustic-Semantic-Acoustic Paradigm: ", "Converts voice to text on transmitter via offline STT, transmits compact binary packets over Wi-Fi/BLE, and reconstructs speech via offline TTS at receiver."),
        ("10 Indian Languages On-Device: ", "Lightweight, INT8 quantized neural models for Hindi, Marathi, Gujarati, Tamil, Telugu, Kannada, Malayalam, Bengali, Odia & English running 100% locally."),
        ("Dual Operating Modes & Distress Alerts: ", "Supports Push-to-Talk (Walkie-Talkie), full-duplex phone mode with Acoustic Echo Cancellation (AEC), and non-interruptible maximum-volume emergency broadcasts.")
    ]
    for i, (hd, body) in enumerate(sol_points):
        p = tf_s.paragraphs[0] if i == 0 else tf_s.add_paragraph()
        p.space_after = Pt(2)
        r1 = p.add_run()
        r1.text = f"➢ {hd}"
        r1.font.bold = True
        r1.font.size = Pt(9.5)
        r1.font.color.rgb = PRIMARY_BLUE
        r2 = p.add_run()
        r2.text = body
        r2.font.size = Pt(9)
        r2.font.color.rgb = TEXT_DARK

    # Card 3: UVP
    c_uvp = draw_card(s2, Inches(0.6), Inches(5.02), Inches(8.0), Inches(1.98), "UVP (UNIQUE VALUE PROPOSITION)", ACCENT_GREEN)
    tb_u = s2.shapes.add_textbox(Inches(0.7), Inches(5.46), Inches(7.8), Inches(1.48))
    tf_u = tb_u.text_frame
    tf_u.word_wrap = True
    uvp_points = [
        ("1. Voice Accessibility Without Voice Bandwidth: ", "Reduces transmission payload to compact text (<150 bytes per sentence), overcoming physical link bitrate bottlenecks."),
        ("2. Zero Cloud Dependency & Edge-First: ", "Native C++ Sherpa-ONNX runtime and Android AAudio engine operate fully off-grid with zero cloud subscriptions or API latency."),
        ("3. Symmetrical Transceiver Architecture: ", "Every node functions as both neural sender and receiver with seamless automatic role switching.")
    ]
    for i, (hd, body) in enumerate(uvp_points):
        p = tf_u.paragraphs[0] if i == 0 else tf_u.add_paragraph()
        p.space_after = Pt(2)
        r1 = p.add_run()
        r1.text = f"➢ {hd}"
        r1.font.bold = True
        r1.font.size = Pt(9.5)
        r1.font.color.rgb = ACCENT_GREEN
        r2 = p.add_run()
        r2.text = body
        r2.font.size = Pt(9)
        r2.font.color.rgb = TEXT_DARK

    # Right Side: Architecture Flow Diagram (Width: 4.1 in)
    c_arch = draw_card(s2, Inches(8.8), Inches(0.98), Inches(3.933), Inches(6.02), "TRANSCEIVER ARCHITECTURE", NAVY)
    
    flow_boxes = [
        ("DEVICE A: TRANSMITTER", NAVY, True),
        ("🎙 Speech Input (Microphone)", TEXT_DARK, False),
        ("⚡ Silero VAD (Speech Gating)", ACCENT_GREEN, False),
        ("🧠 Streaming IndicConformer STT", PRIMARY_BLUE, False),
        ("📄 Text Serialization (Cap'n Proto)", ACCENT_ORANGE, False),
        ("📡 LOW-BITRATE LINK (Wi-Fi / BLE)", NAVY, True),
        ("DEVICE B: RECEIVER", NAVY, True),
        ("📦 Packet De-serialization & Queue", ACCENT_ORANGE, False),
        ("🔤 Indic Normalization / Translit.", PRIMARY_BLUE, False),
        ("🔊 VITS / Piper Lightweight TTS", PRIMARY_BLUE, False),
        ("👂 Audio Playback / Non-stop Alert", ACCENT_GREEN, False)
    ]
    
    ay = Inches(1.45)
    for title, clr, is_header in flow_boxes:
        h_box = Inches(0.33) if not is_header else Inches(0.36)
        bx = s2.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(8.98), ay, Inches(3.57), h_box)
        bx.fill.solid()
        if is_header:
            bx.fill.fore_color.rgb = clr
            bx.line.fill.background()
        else:
            bx.fill.fore_color.rgb = PILL_BG
            bx.line.color.rgb = clr
            bx.line.width = Pt(1.2)
            
        tf_bx = bx.text_frame
        tf_bx.vertical_anchor = MSO_ANCHOR.MIDDLE
        p = tf_bx.paragraphs[0]
        p.text = title
        p.alignment = PP_ALIGN.CENTER if is_header else PP_ALIGN.LEFT
        p.font.size = Pt(9.5) if is_header else Pt(8.5)
        p.font.bold = True
        p.font.color.rgb = RGBColor(255, 255, 255) if is_header else clr
        
        ay += h_box + Inches(0.07)

    add_footer(s2, 2)

    # ==========================================
    # SLIDE 3: TECHNICAL APPROACH
    # ==========================================
    s3 = prs.slides.add_slide(blank_slide_layout)
    add_header(s3, "TECHNICAL APPROACH & ARCHITECTURE")

    # Top Section: End-to-End Pipeline Card (Width: 12.133, Height: 2.1)
    c_pipe = draw_card(s3, Inches(0.6), Inches(0.95), Inches(12.133), Inches(2.15), "END-TO-END ACOUSTIC-SEMANTIC-ACOUSTIC PIPELINE", PRIMARY_BLUE)
    
    p_stages = [
        ("1. Audio Capture", "AAudio Native NDK\n16kHz PCM Buffer\nLow-latency callback", NAVY),
        ("2. Voice Gating", "Silero VAD Gating\nIdle STT Sleep\nPause Detection", ACCENT_GREEN),
        ("3. Edge ASR", "IndicConformer\nStreaming RNN-T/CTC\nINT8 Quantized", PRIMARY_BLUE),
        ("4. Binary Packet", "Cap'n Proto Schema\nSender ID + Lang ID\n<150 B / sentence", ACCENT_ORANGE),
        ("5. P2P Transport", "Wi-Fi Direct / BLE\nLocal Mesh Bridge\nCRC Integrity Check", NAVY),
        ("6. Text Synthesis", "VITS / Piper TTS\nIndicXlit Phonemes\nSherpa-ONNX Core", PRIMARY_BLUE),
        ("7. Voice Playback", "AAudio Engine\nAEC Loop Filter\nDistress Audio Focus", ACCENT_GREEN)
    ]
    
    px = Inches(0.72)
    for i, (stitle, sdesc, sclr) in enumerate(p_stages):
        sbx = s3.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, px, Inches(1.42), Inches(1.48), Inches(1.58))
        sbx.fill.solid()
        sbx.fill.fore_color.rgb = CARD_BG
        sbx.line.color.rgb = sclr
        sbx.line.width = Pt(1.5)
        
        tf_s = sbx.text_frame
        tf_s.word_wrap = True
        p1 = tf_s.paragraphs[0]
        p1.text = stitle
        p1.font.bold = True
        p1.font.size = Pt(9.5)
        p1.font.color.rgb = sclr
        p1.space_after = Pt(3)
        
        p2 = tf_s.add_paragraph()
        p2.text = sdesc
        p2.font.size = Pt(8)
        p2.font.color.rgb = TEXT_DARK
        
        px += Inches(1.52)
        if i < len(p_stages) - 1:
            arr = s3.shapes.add_shape(MSO_SHAPE.RIGHT_ARROW, px - Inches(0.04), Inches(2.1), Inches(0.16), Inches(0.22))
            arr.fill.solid()
            arr.fill.fore_color.rgb = ACCENT_ORANGE
            arr.line.fill.background()
            px += Inches(0.18)

    # Middle Section: 3 Deep Technical Modules (Height: 2.65)
    # Box 1: STT & VAD Pipeline
    c_stt = draw_card(s3, Inches(0.6), Inches(3.2), Inches(3.9), Inches(2.65), "SPEECH-TO-TEXT & VAD GATING", NAVY)
    tb_st = s3.shapes.add_textbox(Inches(0.7), Inches(3.65), Inches(3.7), Inches(2.15))
    tf_st = tb_st.text_frame
    tf_st.word_wrap = True
    stt_items = [
        ("IndicConformer ASR: ", "Selected research direction for streaming Indic ASR; hybrid RNN-T / CTC decoding for instant token emission."),
        ("Silero VAD CPU Gating: ", "Lightweight VAD continuously monitors audio; heavy STT model stays dormant during silence (Idle CPU target <2%)."),
        ("Sentence Boundary Logic: ", "Detects acoustic pauses (~400-600ms) to finalize sentence tokens for immediate low-latency packetization."),
        ("INT8 & mmap Optimization: ", "Dynamic quantization & memory mapping reduce memory footprint to ~180MB (research reference).")
    ]
    for i, (h, b) in enumerate(stt_items):
        p = tf_st.paragraphs[0] if i == 0 else tf_st.add_paragraph()
        p.space_after = Pt(2)
        r1 = p.add_run()
        r1.text = f"• {h}"
        r1.font.bold = True
        r1.font.size = Pt(8.5)
        r1.font.color.rgb = NAVY
        r2 = p.add_run()
        r2.text = b
        r2.font.size = Pt(8)
        r2.font.color.rgb = TEXT_DARK

    # Box 2: TTS & Audio Engine
    c_tts = draw_card(s3, Inches(4.7), Inches(3.2), Inches(3.9), Inches(2.65), "TEXT-TO-SPEECH & NATIVE AUDIO", PRIMARY_BLUE)
    tb_tt = s3.shapes.add_textbox(Inches(4.8), Inches(3.65), Inches(3.7), Inches(2.15))
    tf_tt = tb_tt.text_frame
    tf_tt.word_wrap = True
    tts_items = [
        ("VITS / Piper Architecture: ", "Lightweight, end-to-end neural acoustic model synthesized in native C++ via Sherpa-ONNX (RTF target <0.25)."),
        ("Indic Text Normalization: ", "Rule-based numeral/date expansion and IndicXlit transliteration for high phonetic naturalness across 10 scripts."),
        ("Android AAudio Engine: ", "Native NDK C++ audio callbacks bypass Java JVM overhead for ultra-low output latency."),
        ("AEC Feedback Protection: ", "Acoustic Echo Cancellation suppresses speaker output from feeding back into microphone in full-duplex.")
    ]
    for i, (h, b) in enumerate(tts_items):
        p = tf_tt.paragraphs[0] if i == 0 else tf_tt.add_paragraph()
        p.space_after = Pt(2)
        r1 = p.add_run()
        r1.text = f"• {h}"
        r1.font.bold = True
        r1.font.size = Pt(8.5)
        r1.font.color.rgb = PRIMARY_BLUE
        r2 = p.add_run()
        r2.text = b
        r2.font.size = Pt(8)
        r2.font.color.rgb = TEXT_DARK

    # Box 3: Dual Modes & Distress Alerts
    c_modes = draw_card(s3, Inches(8.8), Inches(3.2), Inches(3.933), Inches(2.65), "COMMUNICATION & ALERT MODES", ACCENT_GREEN)
    tb_m = s3.shapes.add_textbox(Inches(8.9), Inches(3.65), Inches(3.7), Inches(2.15))
    tf_m = tb_m.text_frame
    tf_m.word_wrap = True
    mode_items = [
        ("Push-to-Talk (PTT): ", "Walkie-talkie mode; press to buffer audio, VAD finalizes sentence on pause, instant burst packet transport."),
        ("Continuous Phone Mode: ", "Hands-free conversational streaming with active bi-directional AEC suppression on both devices."),
        ("Distress Non-Stop Alert: ", "High-priority alert payload triggers highest permitted / configured alert volume via Android Audio Focus."),
        ("Architecture Model Matrix: ", "Whisper (High accuracy / heavy), Vosk (Lightweight), IndicConformer (Streaming + Indic-optimized - Selected).")
    ]
    for i, (h, b) in enumerate(mode_items):
        p = tf_m.paragraphs[0] if i == 0 else tf_m.add_paragraph()
        p.space_after = Pt(2)
        r1 = p.add_run()
        r1.text = f"• {h}"
        r1.font.bold = True
        r1.font.size = Pt(8.5)
        r1.font.color.rgb = ACCENT_GREEN
        r2 = p.add_run()
        r2.text = b
        r2.font.size = Pt(8)
        r2.font.color.rgb = TEXT_DARK

    # Bottom Tech Stack Strip
    t_stack = s3.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.6), Inches(5.95), Inches(12.133), Inches(1.02))
    t_stack.fill.solid()
    t_stack.fill.fore_color.rgb = NAVY
    t_stack.line.fill.background()
    
    tb_stk = s3.shapes.add_textbox(Inches(0.7), Inches(5.97), Inches(11.9), Inches(0.95))
    tf_stk = tb_stk.text_frame
    tf_stk.word_wrap = True
    p_stk = tf_stk.paragraphs[0]
    p_stk.text = "TECHNOLOGY STACK ARCHITECTURE"
    p_stk.font.bold = True
    p_stk.font.size = Pt(9.5)
    p_stk.font.color.rgb = ACCENT_ORANGE
    
    stack_cols = [
        ("Mobile OS", "Kotlin, Android NDK (C++20), AAudio"),
        ("Speech AI", "IndicConformer, Silero VAD, VITS"),
        ("Edge Inference", "Sherpa-ONNX, ONNX Runtime INT8"),
        ("Audio Engine", "AAudio Native, Oboe, WebRTC AEC"),
        ("P2P Network", "Wi-Fi Direct (P2P), BLE, Cap'n Proto")
    ]
    p_stk_sub = tf_stk.add_paragraph()
    for cat, tech in stack_cols:
        r_c = p_stk_sub.add_run()
        r_c.text = f" [{cat}] "
        r_c.font.bold = True
        r_c.font.size = Pt(8)
        r_c.font.color.rgb = RGBColor(255, 255, 255)
        
        r_t = p_stk_sub.add_run()
        r_t.text = f"{tech}  | "
        r_t.font.size = Pt(8)
        r_t.font.color.rgb = RGBColor(200, 225, 255)

    add_footer(s3, 3)

    # ==========================================
    # SLIDE 4: FEASIBILITY AND VIABILITY
    # ==========================================
    s4 = prs.slides.add_slide(blank_slide_layout)
    add_header(s4, "FEASIBILITY AND VIABILITY")

    # 4 Large Information Cards (2x2 Grid)
    # Card 1: Technical Feasibility
    c_tf = draw_card(s4, Inches(0.6), Inches(0.98), Inches(5.9), Inches(2.28), "TECHNICAL FEASIBILITY", PRIMARY_BLUE)
    tb_tf = s4.shapes.add_textbox(Inches(0.7), Inches(1.42), Inches(5.7), Inches(1.78))
    tf_tf = tb_tf.text_frame
    tf_tf.word_wrap = True
    tf_points = [
        ("Offline Edge Inference: ", "Sherpa-ONNX C++ runtime enables low-latency execution of IndicConformer & VITS on arm64-v8a Android devices without cloud APIs."),
        ("Streaming Low Latency: ", "Chunk-based STT streaming with acoustic sentence boundary detection achieves near real-time text emission (<1.5s total target)."),
        ("Thermal & Power Management: ", "Silero VAD gating prevents continuous neural inference, maintaining low idle CPU usage (<2%) and battery preservation.")
    ]
    for i, (h, b) in enumerate(tf_points):
        p = tf_tf.paragraphs[0] if i == 0 else tf_tf.add_paragraph()
        p.space_after = Pt(2)
        r1 = p.add_run()
        r1.text = f"➢ {h}"
        r1.font.bold = True
        r1.font.size = Pt(9)
        r1.font.color.rgb = PRIMARY_BLUE
        r2 = p.add_run()
        r2.text = b
        r2.font.size = Pt(8.5)
        r2.font.color.rgb = TEXT_DARK

    # Card 2: Resource & Model Feasibility
    c_rf = draw_card(s4, Inches(6.8), Inches(0.98), Inches(5.933), Inches(2.28), "RESOURCE & MODEL FEASIBILITY", NAVY)
    tb_rf = s4.shapes.add_textbox(Inches(6.9), Inches(1.42), Inches(5.7), Inches(1.78))
    tf_rf = tb_rf.text_frame
    tf_rf.word_wrap = True
    rf_points = [
        ("AI4Bharat Indic Ecosystem: ", "Leverages open-source IndicASR & IndicTTS architectures pre-trained on 10,000+ hours of Indic speech data."),
        ("Modular Dynamic Language Packs: ", "On-demand model loading avoids APK bloat; base app remains <25 MB, loading desired language weights dynamically."),
        ("INT8 Model Optimization: ", "Dynamic INT8 quantization compresses acoustic models by ~75% (research reference: ~180MB ASR / ~25MB TTS footprint).")
    ]
    for i, (h, b) in enumerate(rf_points):
        p = tf_rf.paragraphs[0] if i == 0 else tf_rf.add_paragraph()
        p.space_after = Pt(2)
        r1 = p.add_run()
        r1.text = f"➢ {h}"
        r1.font.bold = True
        r1.font.size = Pt(9)
        r1.font.color.rgb = NAVY
        r2 = p.add_run()
        r2.text = b
        r2.font.size = Pt(8.5)
        r2.font.color.rgb = TEXT_DARK

    # Card 3: Viability & Operational Impact
    c_vb = draw_card(s4, Inches(0.6), Inches(3.34), Inches(5.9), Inches(2.32), "VIABILITY & OPERATIONAL READINESS", ACCENT_GREEN)
    tb_vb = s4.shapes.add_textbox(Inches(0.7), Inches(3.78), Inches(5.7), Inches(1.82))
    tf_vb = tb_vb.text_frame
    tf_vb.word_wrap = True
    vb_points = [
        ("Disaster & Defense Resilience: ", "Ensures life-saving voice links during cyclone/flood telecom blackouts and remote tactical field operations."),
        ("Commodity Hardware Cost: ", "Functions entirely on standard commercial low/mid-range Android smartphones without requiring expensive satellite radios."),
        ("Modular Mesh Bridge Extension: ", "Extensible architecture capable of bridging over BLE-connected LoRa radio nodes for multi-kilometer coverage.")
    ]
    for i, (h, b) in enumerate(vb_points):
        p = tf_vb.paragraphs[0] if i == 0 else tf_vb.add_paragraph()
        p.space_after = Pt(2)
        r1 = p.add_run()
        r1.text = f"➢ {h}"
        r1.font.bold = True
        r1.font.size = Pt(9)
        r1.font.color.rgb = ACCENT_GREEN
        r2 = p.add_run()
        r2.text = b
        r2.font.size = Pt(8.5)
        r2.font.color.rgb = TEXT_DARK

    # Card 4: Technical Challenges & Mitigation
    c_tc = draw_card(s4, Inches(6.8), Inches(3.34), Inches(5.933), Inches(2.32), "TECHNICAL CHALLENGES & MITIGATION", WARN_RED)
    tb_tc = s4.shapes.add_textbox(Inches(6.9), Inches(3.78), Inches(5.7), Inches(1.82))
    tf_tc = tb_tc.text_frame
    tf_tc.word_wrap = True
    tc_points = [
        ("Acoustic Noise / Distortion: ", "Mitigation: Native RNNoise / WebRTC pre-processing + fine-tuned noise-robust IndicConformer checkpoint."),
        ("TTS Audio Feedback Loop: ", "Mitigation: Native Acoustic Echo Cancellation (AEC) and hardware audio routing suppress mic self-capture."),
        ("Packet Loss on Low-Bitrate: ", "Mitigation: Lightweight binary sequence headers + checksum validation + fast localized retransmission.")
    ]
    for i, (h, b) in enumerate(tc_points):
        p = tf_tc.paragraphs[0] if i == 0 else tf_tc.add_paragraph()
        p.space_after = Pt(2)
        r1 = p.add_run()
        r1.text = f"➢ {h}"
        r1.font.bold = True
        r1.font.size = Pt(9)
        r1.font.color.rgb = WARN_RED
        r2 = p.add_run()
        r2.text = b
        r2.font.size = Pt(8.5)
        r2.font.color.rgb = TEXT_DARK

    # Bottom: 36-Hour Hackathon Implementation Roadmap
    plan_card = draw_card(s4, Inches(0.6), Inches(5.74), Inches(12.133), Inches(1.28), "36-HOUR REALISTIC IMPLEMENTATION ROADMAP", NAVY)
    
    phases = [
        ("PHASE 1 (0–12 Hrs): Core Transceiver Loop", "2-Device P2P link (Wi-Fi Direct/BLE), AAudio capture, baseline Indic STT + TTS pipeline, Push-to-Talk verified.", PRIMARY_BLUE),
        ("PHASE 2 (12–24 Hrs): Edge Optimization", "Silero VAD integration, INT8 quantization benchmarks, Latency profiling (RTF <0.25 target), AEC loop tuning.", ACCENT_GREEN),
        ("PHASE 3 (24–36 Hrs): Scaling & Alert Mode", "Multi-language expansion (10 Indic scripts), Distress non-interruptible alert broadcast, field test demo.", ACCENT_ORANGE)
    ]
    
    ph_x = Inches(0.75)
    for title, desc, clr in phases:
        pbx = s4.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, ph_x, Inches(6.16), Inches(3.8), Inches(0.76))
        pbx.fill.solid()
        pbx.fill.fore_color.rgb = PILL_BG
        pbx.line.color.rgb = clr
        pbx.line.width = Pt(1.2)
        
        tf_p = pbx.text_frame
        tf_p.word_wrap = True
        p1 = tf_p.paragraphs[0]
        p1.text = title
        p1.font.bold = True
        p1.font.size = Pt(8.5)
        p1.font.color.rgb = clr
        
        p2 = tf_p.add_paragraph()
        p2.text = desc
        p2.font.size = Pt(7.5)
        p2.font.color.rgb = TEXT_DARK
        
        ph_x += Inches(3.95)

    add_footer(s4, 4)

    # ==========================================
    # SLIDE 5: IMPACT AND BENEFITS
    # ==========================================
    s5 = prs.slides.add_slide(blank_slide_layout)
    add_header(s5, "IMPACTS & BENEFITS")

    # Left: 4 Strategic Impact Pillars + ISRO Evaluation Breakdown (Width: 5.6 in, Height: 4.35 in)
    c_imp = draw_card(s5, Inches(0.6), Inches(0.98), Inches(5.6), Inches(4.4), "STRATEGIC IMPACT & EVALUATION TARGETS", PRIMARY_BLUE)
    tb_im = s5.shapes.add_textbox(Inches(0.7), Inches(1.42), Inches(5.4), Inches(3.9))
    tf_im = tb_im.text_frame
    tf_im.word_wrap = True
    
    imp_items = [
        ("Ultra-Low Bitrate Efficiency (20% Eval): ", "Replaces 64 kbps continuous audio stream with <150-byte semantic text packets. (Illustrative advantage: >98% transmission footprint reduction)."),
        ("Speech Accuracy & Intelligibility (40% Eval): ", "State-of-the-art Conformer ASR for low Word Error Rate (WER) across noisy conditions + natural phonetic synthesis via VITS TTS."),
        ("Low Latency Bounds (20% Eval): ", "Speech-to-STT completion <600ms, TTS Real-Time Factor ~0.15–0.25 on ARMv8, Speech-to-Speech total delay ~1.5s (prototype benchmark target)."),
        ("10 Indian Languages & Disaster Inclusion: ", "Hindi, Marathi, Gujarati, Tamil, Telugu, Kannada, Malayalam, Bengali, Odia & English support for non-literate citizen accessibility.")
    ]
    for i, (h, b) in enumerate(imp_items):
        p = tf_im.paragraphs[0] if i == 0 else tf_im.add_paragraph()
        p.space_after = Pt(3)
        r1 = p.add_run()
        r1.text = f"• {h}"
        r1.font.bold = True
        r1.font.size = Pt(9)
        r1.font.color.rgb = PRIMARY_BLUE
        r2 = p.add_run()
        r2.text = b
        r2.font.size = Pt(8.5)
        r2.font.color.rgb = TEXT_DARK

    # Right: Feature Comparison Table (Width: 6.333 in, Height: 4.4 in)
    c_cmp = draw_card(s5, Inches(6.4), Inches(0.98), Inches(6.333), Inches(4.4), "FEATURE COMPARISON: CONVENTIONAL VS iTANTRA", NAVY)
    
    rows, cols = 8, 3
    tbl_shape = s5.shapes.add_table(rows, cols, Inches(6.5), Inches(1.45), Inches(6.133), Inches(3.8))
    table = tbl_shape.table
    table.columns[0].width = Inches(2.533)
    table.columns[1].width = Inches(1.8)
    table.columns[2].width = Inches(1.8)

    headers = ["Evaluation Metric / Capability", "Conventional Voice", "iTantra Transceiver"]
    for j, h_text in enumerate(headers):
        cell = table.cell(0, j)
        cell.fill.solid()
        cell.fill.fore_color.rgb = NAVY if j < 2 else ACCENT_GREEN
        p = cell.text_frame.paragraphs[0]
        p.text = h_text
        p.alignment = PP_ALIGN.CENTER if j > 0 else PP_ALIGN.LEFT
        p.font.bold = True
        p.font.size = Pt(9.5)
        p.font.color.rgb = RGBColor(255, 255, 255)

    comp_data = [
        ("Transmission Payload", "Raw Audio Stream (>64 kbps)", "Semantic Text (<1 kbps) ✓"),
        ("Zero Internet / Offline", "Limited / Cloud ASR Dependent", "100% Fully Offline Edge ✓"),
        ("10 Indian Languages", "Inconsistent / Fragmented", "Native Indic Pipeline ✓"),
        ("Hardware Compatibility", "Requires High Bandwidth", "Low/Mid-Range Phones ✓"),
        ("Push-to-Talk (PTT)", "Analog / Continuous Stream", "Burst Semantic Packet ✓"),
        ("Full-Duplex + AEC", "Acoustic Feedback Issues", "Native C++ AEC Engine ✓"),
        ("Non-Interrupt Alert", "Standard Notifications", "Max-Volume Audio Focus ✓")
    ]

    for i, row in enumerate(comp_data):
        for j, val in enumerate(row):
            cell = table.cell(i + 1, j)
            cell.fill.solid()
            cell.fill.fore_color.rgb = RGBColor(245, 248, 253) if i % 2 == 0 else RGBColor(255, 255, 255)
            p = cell.text_frame.paragraphs[0]
            p.text = val
            p.alignment = PP_ALIGN.CENTER if j > 0 else PP_ALIGN.LEFT
            p.font.size = Pt(8.5)
            if j == 2:
                p.font.bold = True
                p.font.color.rgb = ACCENT_GREEN
            elif j == 1:
                p.font.color.rgb = WARN_RED if "Issues" in val or "Requires" in val or "Dependent" in val else TEXT_DARK
            else:
                p.font.bold = True
                p.font.color.rgb = NAVY

    # Bottom: How iTantra Works (3 Operating Modes)
    m_card = draw_card(s5, Inches(0.6), Inches(5.46), Inches(12.133), Inches(1.55), "HOW iTANTRA WORKS: 3 OPERATIONAL WORKFLOWS", HEADER_GREY)
    
    op_modes = [
        ("MODE 1: WALKIE-TALKIE (PTT)", "Press PTT → Stream Speech → VAD Sentence Formation → Transmit Packet → Receiver TTS Synthesis → Voice Playback.", PRIMARY_BLUE),
        ("MODE 2: CONTINUOUS PHONE MODE", "Hands-Free Auto-VAD Detection → Real-Time Streaming STT ↔ Bi-directional Text Packets ↔ VITS TTS + AEC Filter.", ACCENT_GREEN),
        ("MODE 3: HIGH-PRIORITY DISTRESS ALERT", "Distress Text Ingestion → Instant Priority Queue → Bypasses DND / Max Configured Alert Volume Announcement.", ACCENT_ORANGE)
    ]
    
    mx = Inches(0.75)
    for title, desc, clr in op_modes:
        mbx = s5.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, mx, Inches(5.92), Inches(3.8), Inches(1.0))
        mbx.fill.solid()
        mbx.fill.fore_color.rgb = PILL_BG
        mbx.line.color.rgb = clr
        mbx.line.width = Pt(1.5)
        
        tf_m = mbx.text_frame
        tf_m.word_wrap = True
        p1 = tf_m.paragraphs[0]
        p1.text = title
        p1.font.bold = True
        p1.font.size = Pt(9)
        p1.font.color.rgb = clr
        
        p2 = tf_m.add_paragraph()
        p2.text = desc
        p2.font.size = Pt(8)
        p2.font.color.rgb = TEXT_DARK
        
        mx += Inches(3.95)

    add_footer(s5, 5)

    # ==========================================
    # SLIDE 6: RESEARCH AND REFERENCES
    # ==========================================
    s6 = prs.slides.add_slide(blank_slide_layout)
    add_header(s6, "RESEARCH AND REFERENCES")

    # 4 Quadrants
    # Top-Left: Research & Technical Foundation
    c_fnd = draw_card(s6, Inches(0.6), Inches(0.98), Inches(5.9), Inches(2.55), "RESEARCH & TECHNICAL FOUNDATION", PRIMARY_BLUE)
    tb_fd = s6.shapes.add_textbox(Inches(0.7), Inches(1.42), Inches(5.7), Inches(2.05))
    tf_fd = tb_fd.text_frame
    tf_fd.word_wrap = True
    fnd_points = [
        ("Acoustic-Semantic-Acoustic Transceiver: ", "Replaces bandwidth-heavy voice streaming with semantic token serialization over low-bitrate physical links."),
        ("Streaming Conformer RNN-T/CTC: ", "Enables instant token emission within ~200ms chunks on mobile ARM CPUs without full-sequence buffer latency."),
        ("Lightweight VAD Speech Gating: ", "Silero VAD integration stops idle inference, eliminating thermal throttling and background noise transcription."),
        ("Indic Transliteration & Phonemes: ", "IndicXlit script mapping enables cross-dialect phonetic accuracy across 10 major Indian language families.")
    ]
    for i, (h, b) in enumerate(fnd_points):
        p = tf_fd.paragraphs[0] if i == 0 else tf_fd.add_paragraph()
        p.space_after = Pt(2)
        r1 = p.add_run()
        r1.text = f"• {h}"
        r1.font.bold = True
        r1.font.size = Pt(8.5)
        r1.font.color.rgb = PRIMARY_BLUE
        r2 = p.add_run()
        r2.text = b
        r2.font.size = Pt(8)
        r2.font.color.rgb = TEXT_DARK

    # Top-Right: Why iTantra Stands Out
    c_diff = draw_card(s6, Inches(6.8), Inches(0.98), Inches(5.933), Inches(2.55), "WHY iTANTRA STANDS OUT", ACCENT_GREEN)
    tb_df = s6.shapes.add_textbox(Inches(6.9), Inches(1.42), Inches(5.7), Inches(2.05))
    tf_df = tb_df.text_frame
    tf_df.word_wrap = True
    diff_points = [
        ("Semantic vs Waveform Transmission: ", "Transmits language semantics rather than audio waveforms, achieving ultra-resilient communications on congested channels."),
        ("Zero-Cloud Edge Multilingualism: ", "Designed natively for 10 Indian languages with local transliteration and on-device synthesis."),
        ("Unified Transceiver Architecture: ", "Single unified mobile codebase handles PTT walkie-talkie, full-duplex phone, and emergency alert broadcasts seamlessly."),
        ("Indigenous Space & Defense Alignment: ", "Directly addresses ISRO disaster management and tactical off-grid communication challenges.")
    ]
    for i, (h, b) in enumerate(diff_points):
        p = tf_df.paragraphs[0] if i == 0 else tf_df.add_paragraph()
        p.space_after = Pt(2)
        r1 = p.add_run()
        r1.text = f"• {h}"
        r1.font.bold = True
        r1.font.size = Pt(8.5)
        r1.font.color.rgb = ACCENT_GREEN
        r2 = p.add_run()
        r2.text = b
        r2.font.size = Pt(8)
        r2.font.color.rgb = TEXT_DARK

    # Bottom-Left: Project References & Authoritative Citations
    c_ref = draw_card(s6, Inches(0.6), Inches(3.62), Inches(7.9), Inches(3.38), "PROJECT REFERENCES & AUTHORITATIVE CITATIONS", NAVY)
    tb_rf = s6.shapes.add_textbox(Inches(0.7), Inches(4.06), Inches(7.7), Inches(2.88))
    tf_rf = tb_rf.text_frame
    tf_rf.word_wrap = True
    
    citations = [
        ("1. ISRO Disaster Management Support: ", "Disaster communication requirements and satellite payload standards. ", "https://www.isro.gov.in/DisasterManagementSupport.html"),
        ("2. AI4Bharat IndicConformer & IndicASR: ", "State-of-the-art multilingual speech recognition for Indian languages. ", "https://ai4bharat.iitm.ac.in/areas/asr/"),
        ("3. AI4Bharat IndicTTS & IndicXlit: ", "Lightweight Indian language text-to-speech & transliteration engines. ", "https://github.com/AI4Bharat/IndicTTS"),
        ("4. Sherpa-ONNX (k2-fsa): ", "High-performance C++ edge inference runtime for next-gen ASR/TTS. ", "https://github.com/k2-fsa/sherpa-onnx"),
        ("5. VITS & Piper Neural TTS: ", "Lightweight end-to-end conditional variational speech synthesis on ARM. ", "https://github.com/rhasspy/piper"),
        ("6. Android NDK AAudio Architecture: ", "High-performance low-latency audio callbacks and routing on Android. ", "https://developer.android.com/ndk/guides/audio/aaudio"),
        ("7. ONNX Runtime Mobile Optimizations: ", "INT8 dynamic quantization and memory-mapped model execution. ", "https://onnxruntime.ai/docs/performance/model-optimizations/")
    ]

    for i, (num_title, desc, url) in enumerate(citations):
        p = tf_rf.paragraphs[0] if i == 0 else tf_rf.add_paragraph()
        p.space_after = Pt(2)
        r1 = p.add_run()
        r1.text = num_title
        r1.font.bold = True
        r1.font.size = Pt(8.2)
        r1.font.color.rgb = NAVY
        
        r2 = p.add_run()
        r2.text = desc
        r2.font.size = Pt(7.8)
        r2.font.color.rgb = TEXT_DARK
        
        r3 = p.add_run()
        r3.text = f"[{url}]"
        r3.font.size = Pt(7.2)
        r3.font.color.rgb = PRIMARY_BLUE
        r3.hyperlink.address = url

    # Bottom-Right: Future Extension - ISRO NavIC Alert Integration Box
    c_nav = draw_card(s6, Inches(8.7), Inches(3.62), Inches(4.033), Inches(3.38), "FUTURE EXTENSION: NAVIC INTEGRATION", ACCENT_ORANGE)
    tb_nv = s6.shapes.add_textbox(Inches(8.8), Inches(4.06), Inches(3.8), Inches(2.88))
    tf_nv = tb_nv.text_frame
    tf_nv.word_wrap = True
    
    p_nv_hd = tf_nv.paragraphs[0]
    p_nv_hd.text = "SATELLITE DISTRESS BROADCAST"
    p_nv_hd.font.bold = True
    p_nv_hd.font.size = Pt(9.5)
    p_nv_hd.font.color.rgb = ACCENT_ORANGE
    p_nv_hd.space_after = Pt(3)
    
    nv_points = [
        ("NavIC Messaging Service: ", "Direct integration with indigenous NavIC short-message satellite broadcast receivers for national early warning and tsunami alerts."),
        ("Local Language Re-synthesis: ", "NavIC satellite text alerts received by iTantra node are instantly synthesized into local regional language voice alerts."),
        ("Illustrative Satellite Bridge: ", "NavIC Satellite Alert → BLE Dongle → iTantra Edge App → Offline Indic TTS → Non-Interrupt Loudspeaker Announcement (Future Roadmap).")
    ]
    for h, b in nv_points:
        p = tf_nv.add_paragraph()
        p.space_after = Pt(2)
        r1 = p.add_run()
        r1.text = f"➢ {h}"
        r1.font.bold = True
        r1.font.size = Pt(8.5)
        r1.font.color.rgb = NAVY
        r2 = p.add_run()
        r2.text = b
        r2.font.size = Pt(8)
        r2.font.color.rgb = TEXT_DARK

    add_footer(s6, 6)

    output_pptx = "iTantra_SIH2026_PS26173_Presentation.pptx"
    prs.save(output_pptx)
    print(f"Presentation successfully updated and saved to {output_pptx}")

if __name__ == "__main__":
    build_presentation()
