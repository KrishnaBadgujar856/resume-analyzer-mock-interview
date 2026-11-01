package com.resumeanalyzer.dto.resume;

import com.resumeanalyzer.entity.SkillCategory;

public class ResumeSkillDto {
    private Long id;
    private String name;
    private SkillCategory category;

    public ResumeSkillDto() {}

    public ResumeSkillDto(Long id, String name, SkillCategory category) {
        this.id = id;
        this.name = name;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SkillCategory getCategory() {
        return category;
    }

    public void setCategory(SkillCategory category) {
        this.category = category;
    }
}
