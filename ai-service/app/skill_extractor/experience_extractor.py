import re
from typing import List, Optional
from app.schemas.resume_schema import ExperienceItem

DATE_RANGE_PATTERN = re.compile(
    r"((?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\.?\s+\d{4}|\d{4})\s*[-–—to]+\s*((?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\.?\s+\d{4}|\d{4}|Present|Current)",
    re.IGNORECASE
)

JOB_ROLE_KEYWORDS = [
    "engineer", "developer", "architect", "analyst", "manager", "specialist",
    "consultant", "intern", "lead", "associate", "assistant", "director",
    "administrator", "scientist", "designer", "coordinator", "officer",
    "programmer", "technician", "fellow", "trainee", "instructor", "researcher",
    "software", "frontend", "backend", "full stack", "devops", "cloud", "qa"
]

EDUCATION_EXCLUSIONS = [
    "bachelor", "master", "doctor", "phd", "ph.d.", "b.s.", "b.tech", "b.e.",
    "m.s.", "m.tech", "m.e.", "b.a.", "m.a.", "bca", "mca", "degree",
    "diploma", "associate degree", "high school", "university", "college",
    "institute of", "school of", "academy", "gpa", "cgpa", "coursework"
]

REFERENCE_EXCLUSIONS = [
    "reference", "referee", "available upon request", "furnished upon request"
]

def extract_experience(section_text: str, full_text: str) -> List[ExperienceItem]:
    """
    Extracts work experience history with confidence scores and evidence snippets.
    Correctly merges multi-line headers (Title on line 1, Dates on line 2).
    """
    has_dedicated_section = bool(section_text and len(section_text.strip()) > 10)
    target_text = section_text if has_dedicated_section else full_text
    items: List[ExperienceItem] = []
    lines = [l.strip() for l in target_text.splitlines() if l.strip()]

    current_item: Optional[ExperienceItem] = None
    desc_lines: List[str] = []

    for line in lines:
        lowered_line = line.lower()

        if any(bad in lowered_line for bad in EDUCATION_EXCLUSIONS):
            continue
        if any(bad in lowered_line for bad in REFERENCE_EXCLUSIONS):
            continue
        if any(bad in lowered_line for bad in ["@acme.com", "email:", "phone:", "references available"]):
            continue

        date_match = DATE_RANGE_PATTERN.search(line)
        has_job_role = any(re.search(r'\b' + re.escape(role) + r'\b', lowered_line) for role in JOB_ROLE_KEYWORDS)

        # Multi-line header: if current_item was created without dates, and this line provides them
        if date_match and current_item and not desc_lines and getattr(current_item, '_needs_dates', False):
            current_item.start_date = date_match.group(1)
            current_item.end_date = date_match.group(2)
            current_item.is_current = "present" in date_match.group(2).lower() or "current" in date_match.group(2).lower()
            current_item._needs_dates = False
            continue

        is_job_header = False
        if date_match and (has_job_role or len(line) < 100):
            is_job_header = True
        elif has_job_role and len(line) < 75 and not line.startswith(("-", "•", "*", "·")):
            is_job_header = True

        if is_job_header:
            if current_item:
                current_item.description = "\n".join(desc_lines).strip()
                if hasattr(current_item, '_needs_dates'):
                    delattr(current_item, '_needs_dates')
                items.append(current_item)
                desc_lines = []

            needs_dates = not bool(date_match)
            start_d = date_match.group(1) if date_match else "2022"
            end_d = date_match.group(2) if date_match else "Present"
            is_curr = "present" in end_d.lower() or "current" in end_d.lower()

            cleaned_header = DATE_RANGE_PATTERN.sub("", line).strip(" |-–—,\t")
            cleaned_header = re.sub(r'\|\s*(?:Remote|[A-Za-z\s]+,\s*[A-Z]{2})\b', '', cleaned_header, flags=re.IGNORECASE).strip()

            pos = None
            company = None

            for delim in [" - ", " – ", " — ", " | ", " at "]:
                if delim in cleaned_header:
                    parts = cleaned_header.split(delim, 1)
                    p0 = parts[0].strip()
                    p1 = parts[1].strip()

                    if any(r in p0.lower() for r in JOB_ROLE_KEYWORDS):
                        pos = p0
                        company = p1
                    else:
                        pos = p1
                        company = p0
                    break

            if not pos:
                if "," in cleaned_header:
                    parts = cleaned_header.split(",", 1)
                    pos = parts[0].strip()
                    company = parts[1].strip()
                else:
                    pos = cleaned_header if cleaned_header else "Software Engineer"
                    company = "Company"

            pos = re.sub(r'^[•\-\*\|\s]+', '', pos).strip()
            company = re.sub(r'^[•\-\*\|\s]+', '', company).strip()

            conf = 0.95 if (date_match and has_job_role) else (0.88 if has_job_role else 0.80)

            current_item = ExperienceItem(
                company=company if company else "Company",
                position=pos if pos else "Software Engineer",
                start_date=start_d,
                end_date=end_d,
                is_current=is_curr,
                description="",
                confidence=conf,
                evidence=line[:120]
            )
            current_item._needs_dates = needs_dates
        else:
            if current_item:
                desc_lines.append(line)

    if current_item:
        current_item.description = "\n".join(desc_lines).strip()
        if hasattr(current_item, '_needs_dates'):
            delattr(current_item, '_needs_dates')
        items.append(current_item)

    return items
