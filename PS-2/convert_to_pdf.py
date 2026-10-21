import os
import sys

def convert_pptx_to_pdf_and_images():
    pptx_path = os.path.abspath("iTantra_SIH2026_PS26173_Presentation.pptx")
    pdf_path = os.path.abspath("iTantra_SIH2026_PS26173_Presentation.pdf")
    
    try:
        import comtypes.client
        powerpoint = comtypes.client.CreateObject("PowerPoint.Application")
        powerpoint.Visible = 1
        deck = powerpoint.Presentations.Open(pptx_path)
        # FormatType 32 is PDF
        deck.SaveAs(pdf_path, 32)
        deck.Close()
        powerpoint.Quit()
        print(f"Successfully converted to PDF: {pdf_path}")
        return True
    except Exception as e:
        print(f"COM conversion failed: {e}")
        return False

if __name__ == "__main__":
    convert_pptx_to_pdf_and_images()
