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

def test_no_cross_contamination_between_sections():
    resume_with_all_sections = """
    Alex Rivera
    alex@example.com | 555-0199 | Austin, TX
    
    PROFESSIONAL SUMMARY
    Backend developer with 3 years building web systems.
    
    TECHNICAL SKILLS & TOOLS
    Languages: Python, Java, SQL
    Frameworks: FastAPI, Spring Boot
    Databases: PostgreSQL, Redis
    
    WORK EXPERIENCE & EMPLOYMENT HISTORY
    Backend Developer - CloudScale Solutions
    Jan 2022 - Present
    - Developed microservices using Python and FastAPI.
    - Expressed appreciation to team in Spring 2022.
    
    EDUCATION & QUALIFICATIONS
    B.S. in Computer Science - University of Texas at Austin
    Aug 2018 - May 2022 | GPA: 3.8
    
    PROJECTS & HIGHLIGHTS
    Cloud Task Manager | Python, Redis
    https://github.com/alex/task-manager
    - Created asynchronous queue system.
    
    REFERENCES & RECOMMENDATIONS
    Sarah Connor - Director of Engineering at CloudScale
    sarah@cloudscale.com | 555-0122
    References available upon request.
    """
    
    sections = detect_sections(resume_with_all_sections)
    assert "references" in sections
    assert "education" in sections
    assert "experience" in sections
    assert "projects" in sections
    
    exp_list = extract_experience(sections.get("experience", ""), resume_with_all_sections)
    edu_list = extract_education(sections.get("education", ""), resume_with_all_sections)
    proj_list = extract_projects(sections.get("projects", ""), resume_with_all_sections)
    
    # 1. Ensure Education is NOT in Experience
    for exp in exp_list:
        assert "University of Texas" not in (exp.company or "")
        assert "Computer Science" not in (exp.position or "")
        assert "B.S." not in (exp.position or "")
    
    # 2. Ensure Work Experience is NOT in Education
    for edu in edu_list:
        assert "CloudScale" not in (edu.institution or "")
        assert "Backend Developer" not in (edu.degree or "")
    
    # 3. Ensure References are NOT in Education or Projects
    for edu in edu_list:
        assert "Sarah Connor" not in (edu.institution or "")
        assert "Sarah Connor" not in (edu.degree or "")
    for proj in proj_list:
        assert "Sarah Connor" not in proj.title
        assert "References" not in proj.title
        assert "alex@example.com" not in proj.title
        assert "Languages:" not in proj.title
    
    # 4. Ensure only legitimate project is captured
    assert len(proj_list) == 1
    assert "Cloud Task Manager" in proj_list[0].title

def test_precise_skill_matching_avoids_false_positives():
    prose = "He expressed enthusiasm to the team in Spring 2022 and had to go to the store."
    # Outside skills section, ambiguous words should NOT trigger
    skills = extract_skills(skills_section_text="", full_text=prose)
    names = [s.name.lower() for s in skills]
    assert "express" not in names
    assert "spring" not in names
    assert "go" not in names
