import re

def clean_text(text: str) -> str:
    if not text:
        return ""
    # Normalize unicode spaces and characters
    text = text.replace('\xa0', ' ').replace('\u200b', '')
    # Replace common unicode ligatures
    text = text.replace('ﬁ', 'fi').replace('ﬂ', 'fl').replace('–', '-').replace('—', '-')
    text = text.replace('“', '"').replace('”', '"').replace('‘', "'").replace('’', "'")
    # Replace multiple spaces with a single space while preserving linebreaks
    lines = [re.sub(r'[ \t]+', ' ', line).strip() for line in text.splitlines()]
    # Remove excessive blank lines
    cleaned = []
    blank_count = 0
    for line in lines:
        if not line:
            blank_count += 1
            if blank_count <= 1:
                cleaned.append("")
        else:
            blank_count = 0
            cleaned.append(line)
    return "\n".join(cleaned).strip()
