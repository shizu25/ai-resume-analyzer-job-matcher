import fitz  # PyMuPDF
from typing import Tuple, List

def _extract_page_text_layout_aware(page) -> str:
    """
    Extracts text from a PDF page preserving logical reading order.
    Correctly identifies two-column and sidebar resume layouts so that
    columns are parsed contiguously rather than interleaved line-by-line.
    """
    # blocks format: (x0, y0, x1, y1, text, block_no, block_type)
    blocks = page.get_text("blocks")
    text_blocks = [b for b in blocks if b[6] == 0 and b[4].strip()]
    if not text_blocks:
        return page.get_text()

    page_width = page.rect.width
    page_height = page.rect.height

    # 1. Separate full-width top header (e.g. Name, Contact Info) if present
    header_threshold_y = 0.0
    top_candidates = [
        b for b in text_blocks 
        if b[1] < 0.25 * page_height and (b[2] - b[0]) > 0.55 * page_width
    ]
    if top_candidates:
        header_threshold_y = max(b[3] for b in top_candidates)

    header_blocks = [b for b in text_blocks if b[3] <= header_threshold_y + 5]
    body_blocks = [b for b in text_blocks if b[3] > header_threshold_y + 5]

    # 2. Check if body blocks form a distinct two-column layout
    best_split = None
    min_cut_blocks = 999

    if len(body_blocks) >= 4:
        for split_x in range(int(0.20 * page_width), int(0.80 * page_width), 15):
            left = [b for b in body_blocks if b[2] <= split_x + 10]
            right = [b for b in body_blocks if b[0] >= split_x - 10]
            crossing = [b for b in body_blocks if b[0] < split_x - 10 and b[2] > split_x + 10]

            if len(left) >= 2 and len(right) >= 2 and len(crossing) < min_cut_blocks:
                min_cut_blocks = len(crossing)
                best_split = split_x

    if best_split is not None and min_cut_blocks <= 2:
        # Two-column layout: process header, then left column top-to-bottom, then right column top-to-bottom
        left_blocks = sorted([b for b in body_blocks if b[0] < best_split], key=lambda b: (b[1], b[0]))
        right_blocks = sorted([b for b in body_blocks if b[0] >= best_split], key=lambda b: (b[1], b[0]))
        ordered = sorted(header_blocks, key=lambda b: (b[1], b[0])) + left_blocks + right_blocks
    else:
        # Single-column / standard layout: sort by y-bands to preserve natural lines
        ordered = sorted(text_blocks, key=lambda b: (round(b[1] / 8) * 8, b[0]))

    return "\n\n".join(b[4].strip() for b in ordered if b[4].strip())

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
        text = _extract_page_text_layout_aware(page)
        total_characters += len(text.strip())
        full_text.append(text)

    combined_text = "\n\n".join(full_text)

    # Scanned PDF detection: if document has pages but nearly no text
    if page_count > 0 and total_characters < 40:
        warnings.append(
            "This document appears to be a scanned image or empty PDF. OCR is required to extract text from image-based PDFs."
        )

    return combined_text, warnings
