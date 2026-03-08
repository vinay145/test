package com.resumechecker.service;

import com.resumechecker.model.ResumeRequest;
import com.resumechecker.model.ResumeResponse;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ResumeService {

    private static final List<String> REQUIRED_SECTIONS = Arrays.asList(
            "experience", "education", "skills", "summary", "objective",
            "projects", "certifications", "achievements", "work history"
    );

    private static final List<String> ACTION_VERBS = Arrays.asList(
            "achieved", "built", "created", "designed", "developed", "enhanced",
            "implemented", "improved", "increased", "led", "managed", "optimized",
            "reduced", "resolved", "streamlined", "delivered", "launched", "drove",
            "collaborated", "coordinated", "analyzed", "automated", "deployed",
            "established", "executed", "generated", "integrated", "mentored",
            "monitored", "negotiated", "organized", "planned", "produced",
            "researched", "solved", "supervised", "trained", "transformed"
    );

    private static final List<String> COMMON_SKILLS = Arrays.asList(
            "java", "python", "javascript", "typescript", "c++", "c#", "go", "ruby",
            "react", "angular", "vue", "node", "spring", "django", "flask",
            "sql", "mysql", "postgresql", "mongodb", "redis", "elasticsearch",
            "aws", "azure", "gcp", "docker", "kubernetes", "terraform",
            "git", "agile", "scrum", "ci/cd", "rest", "graphql", "microservices",
            "machine learning", "deep learning", "data analysis", "spark", "hadoop",
            "linux", "bash", "html", "css", "excel", "tableau", "power bi"
    );

    public ResumeResponse analyzeResume(ResumeRequest request) {
        String resumeText = request.getResumeText() != null ? request.getResumeText().toLowerCase() : "";
        String jobDescription = request.getJobDescription() != null ? request.getJobDescription().toLowerCase() : "";

        ResumeResponse response = new ResumeResponse();

        Map<String, Integer> categoryScores = new LinkedHashMap<>();
        List<String> strengths = new ArrayList<>();
        List<String> improvements = new ArrayList<>();
        List<String> matchedKeywords = new ArrayList<>();

        // Score each category
        int sectionScore = scoreSections(resumeText, strengths, improvements);
        int actionVerbScore = scoreActionVerbs(resumeText, strengths, improvements);
        int quantifiableScore = scoreQuantifiableAchievements(resumeText, strengths, improvements);
        int skillsScore = scoreSkills(resumeText, strengths, improvements);
        int lengthScore = scoreLength(resumeText, strengths, improvements);
        int keywordScore = scoreKeywordMatch(resumeText, jobDescription, matchedKeywords, improvements);

        categoryScores.put("Resume Sections", sectionScore);
        categoryScores.put("Action Verbs", actionVerbScore);
        categoryScores.put("Quantifiable Achievements", quantifiableScore);
        categoryScores.put("Skills Coverage", skillsScore);
        categoryScores.put("Resume Length", lengthScore);
        categoryScores.put("Job Keyword Match", keywordScore);

        // Calculate overall score (weighted average)
        int overallScore;
        if (jobDescription.isEmpty()) {
            // Without job description, keyword match is excluded
            overallScore = (sectionScore + actionVerbScore + quantifiableScore + skillsScore + lengthScore) / 5;
        } else {
            overallScore = (sectionScore + actionVerbScore + quantifiableScore + skillsScore + lengthScore + keywordScore) / 6;
        }

        response.setOverallScore(overallScore);
        response.setGrade(calculateGrade(overallScore));
        response.setCategoryScores(categoryScores);
        response.setStrengths(strengths);
        response.setImprovements(improvements);
        response.setMatchedKeywords(matchedKeywords);
        response.setSummary(generateSummary(overallScore));

        return response;
    }

    private int scoreSections(String resumeText, List<String> strengths, List<String> improvements) {
        long foundSections = REQUIRED_SECTIONS.stream()
                .filter(resumeText::contains)
                .count();

        // Key sections that must be present
        boolean hasExperience = resumeText.contains("experience") || resumeText.contains("work history");
        boolean hasEducation = resumeText.contains("education");
        boolean hasSkills = resumeText.contains("skills");
        boolean hasSummary = resumeText.contains("summary") || resumeText.contains("objective") || resumeText.contains("profile");

        int score = 0;
        if (hasExperience) score += 25;
        if (hasEducation) score += 25;
        if (hasSkills) score += 25;
        if (hasSummary) score += 15;
        if (foundSections >= 5) score += 10;

        score = Math.min(score, 100);

        if (score >= 75) {
            strengths.add("Resume includes all key sections (Experience, Education, Skills)");
        }
        if (!hasExperience) improvements.add("Add a clear 'Experience' or 'Work History' section");
        if (!hasEducation) improvements.add("Add an 'Education' section");
        if (!hasSkills) improvements.add("Add a 'Skills' section");
        if (!hasSummary) improvements.add("Add a professional summary or objective statement");

        return score;
    }

    private int scoreActionVerbs(String resumeText, List<String> strengths, List<String> improvements) {
        long verbCount = ACTION_VERBS.stream()
                .filter(verb -> resumeText.contains(verb))
                .count();

        int score;
        if (verbCount >= 10) {
            score = 100;
            strengths.add("Excellent use of action verbs (" + verbCount + " found)");
        } else if (verbCount >= 6) {
            score = 75;
            strengths.add("Good use of action verbs (" + verbCount + " found)");
        } else if (verbCount >= 3) {
            score = 50;
            improvements.add("Use more action verbs (e.g., achieved, implemented, developed)");
        } else {
            score = 25;
            improvements.add("Add strong action verbs to describe your accomplishments");
        }

        return score;
    }

    private int scoreQuantifiableAchievements(String resumeText, List<String> strengths, List<String> improvements) {
        // Look for numbers, percentages, dollar amounts
        Pattern numberPattern = Pattern.compile("\\d+%|\\$\\d+|\\d+\\+|increased by \\d+|reduced by \\d+|\\d+ (years|months|users|clients|projects|teams|members)");
        long quantCount = numberPattern.matcher(resumeText).results().count();

        // Also look for general numbers in context
        Pattern simpleNumbers = Pattern.compile("\\b\\d{2,}\\b");
        long simpleCount = simpleNumbers.matcher(resumeText).results().count();

        long total = quantCount + (simpleCount / 3);

        int score;
        if (total >= 8) {
            score = 100;
            strengths.add("Strong use of quantifiable achievements and metrics");
        } else if (total >= 5) {
            score = 75;
            strengths.add("Good use of numbers and metrics in accomplishments");
        } else if (total >= 2) {
            score = 50;
            improvements.add("Add more quantifiable achievements (e.g., 'increased sales by 30%')");
        } else {
            score = 25;
            improvements.add("Include specific numbers, percentages, and metrics to quantify your impact");
        }

        return score;
    }

    private int scoreSkills(String resumeText, List<String> strengths, List<String> improvements) {
        long skillCount = COMMON_SKILLS.stream()
                .filter(resumeText::contains)
                .count();

        int score;
        if (skillCount >= 8) {
            score = 100;
            strengths.add("Comprehensive technical skills section (" + skillCount + " recognizable skills found)");
        } else if (skillCount >= 5) {
            score = 75;
            strengths.add("Good range of technical skills (" + skillCount + " recognizable skills found)");
        } else if (skillCount >= 2) {
            score = 50;
            improvements.add("Expand your skills section with more relevant technical skills");
        } else {
            score = 25;
            improvements.add("Add a detailed skills section listing your technical and soft skills");
        }

        return score;
    }

    private int scoreLength(String resumeText, List<String> strengths, List<String> improvements) {
        String[] words = resumeText.trim().split("\\s+");
        int wordCount = words.length;

        int score;
        if (wordCount >= 300 && wordCount <= 800) {
            score = 100;
            strengths.add("Resume length is optimal (" + wordCount + " words)");
        } else if (wordCount >= 200 && wordCount < 300) {
            score = 70;
            improvements.add("Resume is a bit short. Consider adding more detail to your experience (" + wordCount + " words)");
        } else if (wordCount > 800 && wordCount <= 1200) {
            score = 70;
            improvements.add("Resume is slightly long. Consider condensing to 1-2 pages (" + wordCount + " words)");
        } else if (wordCount < 200) {
            score = 40;
            improvements.add("Resume is too short. Add more detail about your experience and skills (" + wordCount + " words)");
        } else {
            score = 40;
            improvements.add("Resume is too long. Aim for 300-800 words for best results (" + wordCount + " words)");
        }

        return score;
    }

    private int scoreKeywordMatch(String resumeText, String jobDescription,
                                   List<String> matchedKeywords, List<String> improvements) {
        if (jobDescription.isEmpty()) {
            return 50; // Neutral score when no JD provided
        }

        // Extract meaningful words from job description (ignore stop words)
        Set<String> stopWords = new HashSet<>(Arrays.asList(
                "the", "a", "an", "and", "or", "but", "in", "on", "at", "to",
                "for", "of", "with", "by", "from", "is", "are", "was", "were",
                "be", "been", "being", "have", "has", "had", "do", "does", "did",
                "will", "would", "could", "should", "may", "might", "must", "shall",
                "this", "that", "these", "those", "we", "you", "they", "he", "she"
        ));

        String[] jdWords = jobDescription.split("[\\s,\\.\\-\\(\\)]+");
        Set<String> jdKeywords = Arrays.stream(jdWords)
                .filter(word -> word.length() > 3)
                .filter(word -> !stopWords.contains(word))
                .collect(Collectors.toSet());

        long matched = jdKeywords.stream()
                .filter(resumeText::contains)
                .peek(matchedKeywords::add)
                .count();

        double matchPercentage = jdKeywords.isEmpty() ? 0 : (double) matched / jdKeywords.size() * 100;

        int score;
        if (matchPercentage >= 60) {
            score = 100;
        } else if (matchPercentage >= 40) {
            score = 75;
            improvements.add("Tailor your resume more closely to the job description");
        } else if (matchPercentage >= 20) {
            score = 50;
            improvements.add("Include more keywords from the job description in your resume");
        } else {
            score = 25;
            improvements.add("Your resume has low keyword match with the job description. Align your experience with the role requirements");
        }

        return score;
    }

    private String calculateGrade(int score) {
        if (score >= 90) return "A+";
        if (score >= 80) return "A";
        if (score >= 70) return "B+";
        if (score >= 60) return "B";
        if (score >= 50) return "C+";
        if (score >= 40) return "C";
        return "D";
    }

    private String generateSummary(int score) {
        if (score >= 80) {
            return "Excellent resume! Your resume is well-structured and comprehensive. It should perform well in ATS systems and impress recruiters.";
        } else if (score >= 65) {
            return "Good resume with room for improvement. Address the suggestions below to make your resume stand out more.";
        } else if (score >= 50) {
            return "Average resume. Several areas need attention. Follow the improvement suggestions to significantly boost your score.";
        } else {
            return "Your resume needs significant improvements. Please review the suggestions carefully and update your resume accordingly.";
        }
    }
}
