import re
from typing import List
from app.schemas.resume_schema import ProjectItem

URL_PATTERN = re.compile(r"https?://(?:www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b(?:[-a-zA-Z0-9()@:%_\+.~#?&//=]*)")

def extract_projects(section_text: str, full_text: str) -> List[ProjectItem]:
    target_text = section_text if section_text and len(section_text.strip()) > 30 else full_text
    projects = []
    lines = [l.strip() for l in target_text.splitlines() if l.strip()]

    current_proj = None
    desc_lines = []

    for line in lines:
        # Check if line looks like a project header (Title | Technologies or short bold title)
        is_header = False
        if ("|" in line or " - " in line or ":" in line) and len(line) < 100:
            is_header = True
        elif line.startswith("#") or line.isupper() and len(line) < 50:
            is_header = True

        if is_header and not line.startswith("-") and not line.startswith("•"):
            if current_proj:
                current_proj.description = "\n".join(desc_lines).strip()
                projects.append(current_proj)
                desc_lines = []

            url_match = URL_PATTERN.search(line)
            link = url_match.group(0) if url_match else None
            cleaned = URL_PATTERN.sub("", line).strip()

            parts = [p.strip() for p in re.split(r"[|:]", cleaned) if p.strip()]
            title = parts[0] if parts else "Project"
            tech = parts[1] if len(parts) > 1 else None

            current_proj = ProjectItem(
                title=title,
                technologies=tech,
                link=link,
                description=""
            )
        else:
            if current_proj:
                url_match = URL_PATTERN.search(line)
                if url_match and not current_proj.link:
                    current_proj.link = url_match.group(0)
                desc_lines.append(line)

    if current_proj:
        current_proj.description = "\n".join(desc_lines).strip()
        projects.append(current_proj)

    return projects
