package com.resumematcher.matching;

import com.resumematcher.matching.service.SkillNormalizer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SkillNormalizerTest {

    private final SkillNormalizer normalizer = new SkillNormalizer();

    @Test
    void normalizesCaseWhitespaceAndAliases() {
        assertEquals("spring boot", normalizer.normalize("  Spring   Boot  "));
        assertEquals("react", normalizer.normalize("React.js"));
        assertEquals("react", normalizer.normalize("ReactJS"));
        assertEquals("postgresql", normalizer.normalize("Postgres"));
        assertEquals("kubernetes", normalizer.normalize("K8s"));
        assertEquals("rest api", normalizer.normalize("RESTful APIs"));
        assertEquals("node.js", normalizer.normalize("NodeJS"));
        assertEquals("go", normalizer.normalize("Golang"));
    }

    @Test
    void enforcesExactEqualityAndNeverMatchesSubstrings() {
        Set<String> resumeSkills = normalizer.normalizeSet(List.of("Java", "Spring Boot"));

        assertTrue(normalizer.containsSkill(resumeSkills, "java"));
        assertTrue(normalizer.containsSkill(resumeSkills, "Spring   Boot"));
        assertFalse(normalizer.containsSkill(resumeSkills, "JavaScript"));
        assertFalse(normalizer.containsSkill(resumeSkills, "Spring"));
        assertFalse(normalizer.containsSkill(resumeSkills, "Boot"));
    }

    @Test
    void handlesNullAndBlankInputsSafely() {
        assertEquals("", normalizer.normalize(null));
        assertEquals("", normalizer.normalize("   "));
        assertTrue(normalizer.normalizeSet(null).isEmpty());
        assertFalse(normalizer.containsSkill(Set.of("java"), ""));
        assertFalse(normalizer.containsSkill(Set.of(), "Java"));
    }
}
