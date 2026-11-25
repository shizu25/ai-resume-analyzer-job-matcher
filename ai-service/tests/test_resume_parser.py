import pytest
from app.resume_parser.text_cleaner import clean_text
from app.resume_parser.section_detector import detect_sections
from app.skill_extractor.skill_extractor import extract_skills
from app.skill_extractor.education_extractor import extract_education
from app.skill_extractor.experience_extractor import extract_experience
from app.skill_extractor.project_extractor import extract_projects
from app.suggestion_engine.rules import analyze_rules

SAMPLE_RESUME_TEXT = """
Jane Doe
jane.doe@example.com | (555) 123-4567 | San Francisco, CA | github.com/janedoe

Professional Summary
Senior Full-Stack Engineer with 5+ years of experience designing high-throughput microservices and responsive web applications.

Technical Skills
Programming: Python, Java, TypeScript, Go
Frontend: React, Tailwind CSS, Next.js, HTML5, CSS3
Backend: Spring Boot, FastAPI, Node.js, Express, PostgreSQL, Redis
Cloud & DevOps: Docker, Kubernetes, AWS, Git, CI/CD

Work Experience
Senior Software Engineer | Tech Innovators Inc.
June 2022 – Present
- Architected and delivered event-driven microservices with Spring Boot and Kafka, reducing latency by 42%.
- Optimized PostgreSQL queries and caching with Redis, saving $15,000 annually in AWS compute costs.
- Led a team of 6 engineers across Agile sprints.

Software Engineer | Startup Dynamics
August 2020 – May 2022
- Developed customer-facing React components and REST APIs using FastAPI and Python.
- Automated CI/CD pipelines with GitHub Actions and Docker.

Education
Stanford University
Bachelor of Science in Computer Science
2016 – 2020 | GPA: 3.9

Key Projects
AI Resume Matcher | Python, FastAPI, React
https://github.com/janedoe/resume-matcher
- Engineered semantic analysis pipeline extracting skill vectors from PDF resumes.
"""

def test_text_cleaner():
    raw = "Hello   World!\n\n\n\nTest   Spaces"
    cleaned = clean_text(raw)
    assert "Hello World!" in cleaned
    assert "Test Spaces" in cleaned

def test_section_detector():
    sections = detect_sections(SAMPLE_RESUME_TEXT)
    assert "summary" in sections
    assert "skills" in sections
    assert "experience" in sections
    assert "education" in sections
    assert "projects" in sections
    assert "Senior Full-Stack Engineer" in sections["summary"]

def test_skill_extractor():
    skills = extract_skills(SAMPLE_RESUME_TEXT)
    skill_names = [s.name for s in skills]
    assert "Python" in skill_names
    assert "Java" in skill_names
    assert "React" in skill_names
    assert "Spring Boot" in skill_names
    assert "PostgreSQL" in skill_names
    assert "Docker" in skill_names

def test_education_extractor():
    sections = detect_sections(SAMPLE_RESUME_TEXT)
    edu_list = extract_education(sections.get("education", ""), SAMPLE_RESUME_TEXT)
    assert len(edu_list) >= 1
    edu = edu_list[0]
    assert "Stanford" in (edu.institution or "")
    assert "Science" in (edu.degree or "") or "Bachelor" in (edu.degree or "")
    assert edu.end_year == 2020

def test_experience_extractor():
    sections = detect_sections(SAMPLE_RESUME_TEXT)
    exp_list = extract_experience(sections.get("experience", ""), SAMPLE_RESUME_TEXT)
    assert len(exp_list) >= 1
    assert any("Tech Innovators" in (e.company or "") or "Engineer" in (e.position or "") for e in exp_list)

def test_project_extractor():
    sections = detect_sections(SAMPLE_RESUME_TEXT)
    proj_list = extract_projects(sections.get("projects", ""), SAMPLE_RESUME_TEXT)
    assert len(proj_list) >= 1
    assert any("Resume Matcher" in p.title for p in proj_list)

def test_suggestion_engine():
    sections = detect_sections(SAMPLE_RESUME_TEXT)
    skills = extract_skills(SAMPLE_RESUME_TEXT)
    edu = extract_education(sections.get("education", ""), SAMPLE_RESUME_TEXT)
    exp = extract_experience(sections.get("experience", ""), SAMPLE_RESUME_TEXT)
    proj = extract_projects(sections.get("projects", ""), SAMPLE_RESUME_TEXT)
    suggestions = analyze_rules(sections, skills, edu, exp, proj)
    # Suggestions should exist and be structured
    assert isinstance(suggestions, list)
