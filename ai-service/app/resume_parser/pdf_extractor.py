import fitz  # PyMuPDF
from typing import Tuple, List

def extract_text_from_pdf(file_bytes: bytes) -> Tuple[str, List[str]]:
    warnings = []
    full_text = []

    try:
        doc = fitz.open(stream=file_bytes, filetype="pdf")
    except Exception as e:
        raise ValueError(f"Failed to open PDF document: {str(e)}")

    page_count = len(doc)
    if page_count == 0:
        warnings.append("The uploaded PDF has 0 pages.")
        return "", warnings

    total_characters = 0
    for page_num in range(page_count):
        page = doc.load_page(page_num)
        text = page.get_text()
        total_characters += len(text.strip())
        full_text.append(text)

    combined_text = "\n\n".join(full_text)

    # Scanned PDF detection: if document has pages but nearly no text
    if page_count > 0 and total_characters < 40:
        warnings.append(
            "This document appears to be a scanned image or empty PDF. OCR is required to extract text from image-based PDFs."
        )

    return combined_text, warnings
