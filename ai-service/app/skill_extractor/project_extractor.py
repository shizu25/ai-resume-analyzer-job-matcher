import re
from typing import List, Optional
from app.schemas.resume_schema import ProjectItem

URL_PATTERN = re.compile(
    r"(?:https?://(?:www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b[-a-zA-Z0-9()@:%_\+.~#?&//=]*|github\.com/[-a-zA-Z0-9_./]+)"
)

# Reject lines matching contact info, section titles, skill categories, degrees, or references
PROJECT_EXCLUSION_KEYWORDS = [
    # Contact info
    "email:", "phone:", "linkedin", "location:", "address:", "portfolio:", "@",
    # Section keywords
    "professional summary", "technical skills", "work experience", "employment history",
    "education &", "references &", "certifications &", "languages known",
    # Skill category headers
    "languages:", "programming:", "frameworks:", "libraries:", "databases:",
    "tools:", "devops:", "technologies:", "core competencies:", "tech stack:",
    # Education keywords
    "bachelor of", "master of", "b.tech", "b.e.", "b.s.", "m.s.", "phd", "degree",
    "university", "college", "gpa:", "cgpa:", "graduated",
    # Reference keywords
    "references", "referee", "available upon request", "furnished upon request",
    # Work experience headers
    "software engineer at", "developer at", "intern at", "present |", "current |"
]

def extract_projects(section_text: str, full_text: str) -> List[ProjectItem]:
    """
    Extracts projects strictly from the projects section when present.
    Filters out contact lines, skill headers, job titles, and references so that
    only genuine project titles, tech stacks, links, and bullet descriptions are captured.
    Associates standalone URL lines with the active project rather than creating duplicates.
    """
    has_dedicated_section = bool(section_text and len(section_text.strip()) > 10)
    target_text = section_text if has_dedicated_section else full_text

    projects: List[ProjectItem] = []
    lines = [l.strip() for l in target_text.splitlines() if l.strip()]

    current_proj: Optional[ProjectItem] = None
    desc_lines: List[str] = []

    for line in lines:
        lowered_line = line.lower()

        # 1. Filter out all non-project content
        if any(bad in lowered_line for bad in PROJECT_EXCLUSION_KEYWORDS):
            continue

        url_match = URL_PATTERN.search(line)

        # If line is primarily a URL and we already have an active project without a link, assign it
        if url_match and current_proj and not current_proj.link and len(line) < 100:
            current_proj.link = url_match.group(0)
            cleaned_rem = URL_PATTERN.sub("", line).strip(" |-–—,\t")
            if cleaned_rem:
                desc_lines.append(cleaned_rem)
            continue

        # If searching full_text (no dedicated section), only accept explicit project indicators
        if not has_dedicated_section:
            explicit_indicator = bool(
                re.match(r'^(?:Project|Key Project|Personal Project|Academic Project)\s*[:\-]', line, re.IGNORECASE)
                or (url_match and ("github.com" in lowered_line or "project" in lowered_line))
            )
            if not explicit_indicator and not current_proj:
                continue

        is_bullet = line.startswith(("-", "•", "*", "·", ">", "+"))

        is_header = False
        if not is_bullet and len(line) < 90:
            if "|" in line or " - " in line or re.match(r'^(?:Project\s*[:\-]|[\w\s]{3,35}\s*\([\w\s,+#.]+\))', line, re.IGNORECASE):
                is_header = True
            elif not has_dedicated_section and line.lower().startswith("project:"):
                is_header = True
            elif has_dedicated_section and len(line.split()) <= 6 and not line.endswith((".", ";")) and not url_match:
                is_header = True

        if is_header:
            if current_proj:
                current_proj.description = "\n".join(desc_lines).strip()
                projects.append(current_proj)
                desc_lines = []

            link = url_match.group(0) if url_match else None
            cleaned = URL_PATTERN.sub("", line).strip(" |-–—,\t")

            # Remove prefix like "Project:"
            cleaned = re.sub(r'^(?:Project|Key Project|Selected Project)\s*[:\-]\s*', '', cleaned, flags=re.IGNORECASE).strip()

            title = cleaned
            tech = None

            for delim in [" | ", " - ", " – "]:
                if delim in cleaned:
                    parts = cleaned.split(delim, 1)
                    title = parts[0].strip()
                    tech = parts[1].strip()
                    break

            paren_match = re.search(r'\(([^)]+)\)', title)
            if paren_match and not tech:
                tech = paren_match.group(1).strip()
                title = title[:paren_match.start()].strip()

            title = re.sub(r'^[#•\-\*\s]+', '', title).strip()
            if not title:
                title = "Software Project"

            current_proj = ProjectItem(
                title=title,
                technologies=tech,
                link=link,
                description=""
            )
        else:
            if current_proj:
                if url_match and not current_proj.link:
                    current_proj.link = url_match.group(0)
                desc_lines.append(line)

    if current_proj:
        current_proj.description = "\n".join(desc_lines).strip()
        projects.append(current_proj)

    return projects
