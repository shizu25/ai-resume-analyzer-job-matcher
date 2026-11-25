import io
import docx
from typing import Tuple, List

def extract_text_from_docx(file_bytes: bytes) -> Tuple[str, List[str]]:
    warnings = []
    paragraphs = []

    try:
        doc = docx.Document(io.BytesIO(file_bytes))
    except Exception as e:
        raise ValueError(f"Failed to open DOCX document: {str(e)}")

    for para in doc.paragraphs:
        text = para.text.strip()
        if text:
            paragraphs.append(text)

    # Also extract text from tables
    for table in doc.tables:
        for row in table.rows:
            row_text = [cell.text.strip() for cell in row.cells if cell.text.strip()]
            if row_text:
                paragraphs.append(" | ".join(row_text))

    combined = "\n".join(paragraphs).strip()
    if not combined:
        warnings.append("The uploaded DOCX document contains no readable text content.")

    return combined, warnings
