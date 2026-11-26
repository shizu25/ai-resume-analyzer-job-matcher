import pytest
from app.schemas.resume_schema import (
    RoadmapGenerateRequest,
    ImprovementSuggestionsRequest,
    SimulationCompareRequest
)
from app.roadmap_engine.roadmap_generator import generate_roadmap
from app.simulator_engine.suggestion_generator import generate_suggestions
from app.simulator_engine.version_comparator import compare_versions

def test_skill_roadmap_categorization_and_prioritization():
    # Candidate with Python, SQL, Machine Learning, Scikit-learn
    # Job requires: Python, SQL, Machine Learning, TensorFlow, Deep Learning
    req = RoadmapGenerateRequest(
        resume_skills=["Python", "SQL", "Machine Learning", "Scikit-learn"],
        resume_text="Engineered data pipelines using Python and SQL. Built predictive models with Machine Learning.",
        experience_descriptions=["Developed scalable microservices using Python and SQL."],
        project_descriptions=["Built churn prediction model with Machine Learning and pandas."],
        job_title="AI & Deep Learning Engineer",
        job_description="Seeking AI engineer with strong Python, TensorFlow, and Deep Learning skills.",
        required_skills=["Python", "SQL", "TensorFlow", "Deep Learning"],
        preferred_skills=["Docker", "Scikit-learn"]
    )

    roadmap = generate_roadmap(req)

    # 1. Python & SQL should be demonstrated
    assert "Python" in roadmap.demonstrated_skills
    assert "SQL" in roadmap.demonstrated_skills

    # 2. TensorFlow & Deep Learning should be missing
    assert "TensorFlow" in roadmap.missing_skills
    assert "Deep Learning" in roadmap.missing_skills

    # 3. Scikit-learn is present and related to TensorFlow, but TensorFlow remains missing
    assert "Scikit-learn" in roadmap.related_skills or "Scikit-learn" in roadmap.demonstrated_skills or "Scikit-learn" in roadmap.mentioned_skills

    # 4. Check priority: Mandatory missing skill TensorFlow should be HIGH priority
    tf_item = next((item for item in roadmap.items if item.skill_name == "TensorFlow"), None)
    assert tf_item is not None
    assert tf_item.status == "MISSING"
    assert tf_item.priority == "HIGH"
    assert "Mandatory requirement" in tf_item.priority_reason
    assert "week" in tf_item.estimated_duration.lower()
    assert len(tf_item.learning_objectives) >= 3

def test_resume_improvement_suggestions_no_hallucinations():
    req = ImprovementSuggestionsRequest(
        resume_content={
            "summary": "Passionate developer looking for challenging opportunities.",
            "skills": [{"name": "Python"}, {"name": "Docker"}, {"name": "Kafka"}],
            "projects": [
                {
                    "title": "Data Pipeline",
                    "description": "Worked on data extraction and helped the team.",
                    "technologies": "Python"
                }
            ],
            "experience": []
        },
        job_title="Data Platform Engineer",
        job_description="Looking for an engineer experienced in Python, Docker, and Kafka.",
        required_skills=["Python", "Docker", "Kafka"]
    )

    suggestions = generate_suggestions(req)
    assert len(suggestions) >= 2

    # Verify weak verb suggestion
    weak_sugg = next((s for s in suggestions if s.category == "WEAK_PROJECT_DESCRIPTION"), None)
    assert weak_sugg is not None
    assert "worked on" in weak_sugg.original_text.lower()
    assert "worked on" not in weak_sugg.suggested_text.lower()

    # Verify missing evidence for Docker or Kafka
    missing_ev = [s for s in suggestions if s.category == "MISSING_SKILL_EVIDENCE"]
    assert len(missing_ev) >= 1
    skill_targets = [s.related_requirement for s in missing_ev]
    assert "Docker" in skill_targets or "Kafka" in skill_targets

def test_resume_simulation_gain_and_regression_detection():
    job_reqs = ["Python", "FastAPI", "Docker", "PostgreSQL"]

    # Version A: Only Python and Postgres
    version_a = {
        "skills": [{"name": "Python"}, {"name": "PostgreSQL"}],
        "projects": [
            {"title": "Web App", "description": "Built simple app with Python and PostgreSQL.", "technologies": "Python, PostgreSQL"}
        ]
    }

    # Version B: Enhanced with FastAPI and Docker
    version_b = {
        "skills": [{"name": "Python"}, {"name": "FastAPI"}, {"name": "Docker"}, {"name": "PostgreSQL"}],
        "projects": [
            {"title": "High-Throughput API", "description": "Engineered async REST microservices using FastAPI, Docker, and PostgreSQL.", "technologies": "FastAPI, Docker, PostgreSQL"}
        ]
    }

    sim_req = SimulationCompareRequest(
        version_a_content=version_a,
        version_b_content=version_b,
        job_title="Backend Microservices Engineer",
        job_description="Seeking engineer to build FastAPI microservices deployed with Docker.",
        required_skills=job_reqs,
        preferred_skills=[]
    )

    result = compare_versions(sim_req)
    # Score for B should be higher than A
    assert result.improved_score > result.base_score
    assert result.score_delta > 0
    assert len(result.positive_changes) >= 1
    assert any("FastAPI" in c or "Docker" in c for c in result.positive_changes)

    # Test reverse: B to A (Regression)
    regression_req = SimulationCompareRequest(
        version_a_content=version_b,
        version_b_content=version_a,
        job_title="Backend Microservices Engineer",
        required_skills=job_reqs
    )
    reg_result = compare_versions(regression_req)
    assert reg_result.score_delta < 0
    assert len(reg_result.regressions) >= 1
