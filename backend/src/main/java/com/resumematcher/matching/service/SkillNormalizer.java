package com.resumematcher.matching.service;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SkillNormalizer {

    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("react.js", "react"),
            Map.entry("reactjs", "react"),
            Map.entry("next.js", "next.js"),
            Map.entry("nextjs", "next.js"),
            Map.entry("vue.js", "vue"),
            Map.entry("vuejs", "vue"),
            Map.entry("nuxt.js", "nuxt.js"),
            Map.entry("nuxtjs", "nuxt.js"),
            Map.entry("node", "node.js"),
            Map.entry("nodejs", "node.js"),
            Map.entry("express.js", "express"),
            Map.entry("expressjs", "express"),
            Map.entry("postgres", "postgresql"),
            Map.entry("golang", "go"),
            Map.entry("k8s", "kubernetes"),
            Map.entry("ts", "typescript"),
            Map.entry("js", "javascript"),
            Map.entry("restful api", "rest api"),
            Map.entry("restful apis", "rest api"),
            Map.entry("rest apis", "rest api"),
            Map.entry("amazon web services", "aws"),
            Map.entry("microsoft azure", "azure"),
            Map.entry("google cloud platform", "gcp"),
            Map.entry("google cloud", "gcp"),
            Map.entry("c sharp", "c#"),
            Map.entry("csharp", "c#"),
            Map.entry("cpp", "c++"),
            Map.entry("html5", "html"),
            Map.entry("css3", "css"),
            Map.entry("tailwindcss", "tailwind css"),
            Map.entry("springboot", "spring boot"),
            Map.entry("sklearn", "scikit-learn"),
            Map.entry("scikit learn", "scikit-learn"),
            Map.entry("open cv", "opencv"),
            Map.entry("visual studio code", "vs code"),
            Map.entry("microsoft excel", "excel"),
            Map.entry("ms excel", "excel"),
            Map.entry("team work", "teamwork"),
            Map.entry("natural language processing", "nlp"),
            Map.entry("artificial intelligence", "ai"),
            Map.entry("machine learning", "ml"),
            Map.entry("test-driven development", "tdd"),
            Map.entry("ci/cd pipeline", "ci/cd"),
            Map.entry("dotnet core", ".net core"),
            Map.entry("aspnet", "asp.net")
    );

    public String normalize(String skill) {
        if (skill == null) {
            return "";
        }
        String collapsed = skill.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        if (collapsed.isEmpty()) {
            return "";
        }
        return ALIASES.getOrDefault(collapsed, collapsed);
    }

    public Set<String> normalizeSet(Collection<String> skills) {
        if (skills == null || skills.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> normalized = new HashSet<>();
        for (String skill : skills) {
            String key = normalize(skill);
            if (!key.isEmpty()) {
                normalized.add(key);
            }
        }
        return normalized;
    }

    public boolean containsSkill(Set<String> normalizedResumeSkills, String jobSkill) {
        if (normalizedResumeSkills == null || normalizedResumeSkills.isEmpty()) {
            return false;
        }
        String key = normalize(jobSkill);
        return !key.isEmpty() && normalizedResumeSkills.contains(key);
    }
}
