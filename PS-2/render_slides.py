import fitz # PyMuPDF
import os

def render_pdf_to_images():
    pdf_path = "iTantra_SIH2026_PS26173_Presentation.pdf"
    output_dir = "slide_images"
    os.makedirs(output_dir, exist_ok=True)
    
    doc = fitz.open(pdf_path)
    print(f"Total pages in PDF: {len(doc)}")
    
    for i, page in enumerate(doc):
        pix = page.get_pixmap(dpi=150)
        img_path = os.path.join(output_dir, f"slide_{i+1}.png")
        pix.save(img_path)
        print(f"Saved: {img_path}")

if __name__ == "__main__":
    render_pdf_to_images()
