package com.transformedge.teewb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@ConfigurationProperties(prefix = "template-configuration")
@Configuration
public class TemplateConfiguration {
    private String templatePath;
    private String pageTemplatePath;
    private List<Template> templates;

    public List<Template> getTemplates() {
        return templates;
    }

    public void setTemplates(List<Template> templates) {
        this.templates = templates;
    }

    public Template findByName(String name) {
        return templates.stream().filter(t -> t.getName().equals(name))
                        .findFirst().get();
    }

    public String getPageTemplatePath() {
        return pageTemplatePath;
    }

    public void setPageTemplatePath(String pageTemplatePath) {
        this.pageTemplatePath = pageTemplatePath;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    public static class Template {
        private String name;
        private String path;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

}
