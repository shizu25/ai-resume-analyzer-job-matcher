import re
from typing import List
from app.schemas.resume_schema import ExperienceItem

DATE_RANGE_PATTERN = re.compile(
    r"((?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\.?\s+\d{4}|\d{4})\s*[-–—to]+\s*((?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\.?\s+\d{4}|\d{4}|Present|Current)",
    re.IGNORECASE
)

JOB_TITLES = [
    "Software Engineer", "Frontend Developer", "Backend Developer", "Full Stack Developer",
    "Data Scientist", "DevOps Engineer", "Cloud Architect", "Product Manager",
    "Intern", "Software Developer", "Web Developer", "System Administrator",
    "Technical Lead", "Senior Engineer", "QA Engineer", "Mobile Developer"
]

def extract_experience(section_text: str, full_text: str) -> List[ExperienceItem]:
    target_text = section_text if section_text and len(section_text.strip()) > 30 else full_text
    items = []
    lines = [l.strip() for l in target_text.splitlines() if l.strip()]

    current_item = None
    desc_lines = []

    for line in lines:
        date_match = DATE_RANGE_PATTERN.search(line)
        title_found = any(re.search(r'\b' + re.escape(t) + r'\b', line, re.IGNORECASE) for t in JOB_TITLES)

        if date_match or (title_found and len(line) < 80):
            if current_item:
                current_item.description = "\n".join(desc_lines).strip()
                items.append(current_item)
                desc_lines = []

            start_d = date_match.group(1) if date_match else "2022"
            end_d = date_match.group(2) if date_match else "Present"
            is_curr = "present" in end_d.lower() or "current" in end_d.lower()

            pos = None
            company = None

            cleaned_header = DATE_RANGE_PATTERN.sub("", line).strip(" |-–—,\t")
            parts = [p.strip() for p in re.split(r"[,|–—-]", cleaned_header) if p.strip()]

            if len(parts) >= 2:
                pos = parts[0]
                company = parts[1]
            elif len(parts) == 1:
                pos = parts[0]
                company = "Company"
            else:
                pos = "Software Engineer"
                company = "Tech Firm"

            current_item = ExperienceItem(
                company=company,
                position=pos,
                start_date=start_d,
                end_date=end_d,
                is_current=is_curr,
                description=""
            )
        else:
            if current_item:
                desc_lines.append(line)

    if current_item:
        current_item.description = "\n".join(desc_lines).strip()
        items.append(current_item)

    return items
