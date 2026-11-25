import re
from typing import Optional, Dict, Tuple

_nlp = None

def get_nlp():
    global _nlp
    if _nlp is None:
        import spacy
        try:
            _nlp = spacy.load("en_core_web_sm")
        except Exception:
            import spacy.cli
            spacy.cli.download("en_core_web_sm")
            _nlp = spacy.load("en_core_web_sm")
    return _nlp

# Explicit non-heading keywords (schools, companies, roles) that must never be treated as section headings
NON_HEADING_KEYWORDS = [
    "university", "college", "institute", "school of", "academy of",
    "engineer", "developer", "manager", "director", "intern", "consultant",
    "architect", "analyst", "specialist", "coordinator", "administrator"
]

# Canonical section anchors with semantic keywords
SEMANTIC_ANCHORS: Dict[str, list] = {
    "experience": [
        "experience", "employment", "career", "work", "milestones",
        "journey", "positions held", "where i worked", "work history",
        "career journey", "what i did", "industry experience", "professional background"
    ],
    "education": [
        "education", "academic background", "academic qualifications",
        "alma mater", "pedigree", "scholastic", "qualifications", "studies",
        "academics", "coursework", "formal education", "degrees earned"
    ],
    "skills": [
        "skills", "technologies", "tools", "stack", "toolbox", "arsenal",
        "proficiencies", "competencies", "expertise", "weapons of choice",
        "technical stack", "core skills", "tech arsenal", "what i use", "capabilities"
    ],
    "projects": [
        "projects", "creations", "portfolio", "things i built", "selected works",
        "software", "open source", "applications developed", "code repositories",
        "side projects", "key works", "what i made"
    ],
    "summary": [
        "summary", "profile", "about me", "bio", "overview", "introduction",
        "who i am", "executive summary", "snapshot", "personal statement"
    ],
    "certifications": [
        "certifications", "certificates", "licenses", "credentials", "training",
        "accreditations", "courses completed", "professional certificates"
    ],
    "references": [
        "references", "referees", "recommendations", "vouches", "testimonials",
        "endorsements", "people who can vouch"
    ],
    "awards": [
        "awards", "honors", "achievements", "accomplishments", "scholarships",
        "recognitions", "accolades", "prizes"
    ],
    "publications": [
        "publications", "papers", "research", "patents", "articles", "conferences"
    ],
    "languages": [
        "languages", "linguistic", "spoken languages", "language proficiency"
    ],
    "volunteer": [
        "volunteer", "volunteering", "community", "leadership", "extracurricular",
        "activities", "community service"
    ]
}

def classify_heading_semantically(heading_text: str) -> Tuple[Optional[str], float]:
    """
    Uses spaCy linguistic analysis and semantic token overlap to classify
    unfamiliar, custom, or creative resume headings into canonical sections.
    Returns (canonical_section, confidence_score).
    """
    cleaned = re.sub(r'^(?:[0-9\.\-\*\#\•\·\>\s]+)', '', heading_text).strip()
    cleaned = re.sub(r'[\s:\-\|\/]+$', '', cleaned).strip().lower()

    if not cleaned or len(cleaned) < 2:
        return None, 0.0

    # Never classify names containing school or job indicators as headings
    if any(k in cleaned for k in NON_HEADING_KEYWORDS):
        return None, 0.0

    nlp = get_nlp()
    doc = nlp(cleaned)
    lemmas = {token.lemma_.lower() for token in doc if not token.is_stop and not token.is_punct}

    best_section = None
    best_score = 0.0

    for section, anchors in SEMANTIC_ANCHORS.items():
        score = 0.0
        for anchor in anchors:
            anchor_lower = anchor.lower()
            if anchor_lower == cleaned:
                return section, 0.98

            if anchor_lower in cleaned or cleaned in anchor_lower:
                score = max(score, 0.90)

            anchor_doc = nlp(anchor_lower)
            anchor_lemmas = {t.lemma_.lower() for t in anchor_doc if not t.is_stop}
            if lemmas and anchor_lemmas:
                overlap = lemmas.intersection(anchor_lemmas)
                if overlap:
                    calc_score = len(overlap) / max(len(lemmas), len(anchor_lemmas))
                    score = max(score, min(0.85, 0.60 + 0.30 * calc_score))

        if score > best_score:
            best_score = score
            best_section = section

    if best_score >= 0.65:
        return best_section, round(best_score, 2)

    return None, 0.0

def classify_content_block_semantically(block_text: str) -> Tuple[Optional[str], float]:
    """
    When a block lacks an explicit heading or has an ambiguous heading (e.g. 'Details'),
    this analyzes the semantic contents using spaCy NER, date patterns, and vocabulary
    density to infer which functional section it represents.
    """
    if not block_text or len(block_text.strip()) < 20:
        return None, 0.0

    lowered = block_text.lower()
    nlp = get_nlp()
    doc = nlp(block_text[:1000])

    edu_terms = ["bachelor", "master", "phd", "b.s.", "b.tech", "degree", "diploma", "gpa", "university", "college"]
    edu_count = sum(1 for term in edu_terms if term in lowered)

    exp_date_pattern = re.compile(r'\b(?:19\d\d|20\d\d)\s*[-–—to]+\s*(?:19\d\d|20\d\d|present|current)\b', re.IGNORECASE)
    exp_dates = len(exp_date_pattern.findall(block_text))
    job_roles = ["engineer", "developer", "manager", "architect", "analyst", "intern", "consultant", "lead"]
    role_count = sum(1 for role in job_roles if role in lowered)
    org_entities = sum(1 for ent in doc.ents if ent.label_ == "ORG")

    has_github = "github.com" in lowered or "http://" in lowered or "https://" in lowered
    has_proj_words = "built " in lowered or "developed an app" in lowered or "implemented a" in lowered

    comma_count = block_text.count(",") + block_text.count("•") + block_text.count("|")
    is_dense_list = comma_count > 5 and len(block_text.splitlines()) <= 5

    if edu_count >= 2:
        return "education", 0.85
    if exp_dates >= 1 and (role_count >= 1 or org_entities >= 1):
        return "experience", 0.88
    if has_github or (has_proj_words and len(block_text) < 400):
        return "projects", 0.82
    if is_dense_list and ("python" in lowered or "java" in lowered or "react" in lowered or "sql" in lowered):
        return "skills", 0.85

    return None, 0.0
