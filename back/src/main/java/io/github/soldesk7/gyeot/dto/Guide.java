package io.github.soldesk7.gyeot.dto;

import java.util.List;

public record Guide(
        String category,
        String title,
        List<Section> sections) {

    public record Section(
            String title,
            List<String> steps,
            Source source,
            List<Media> media) {

        public Section {
            media = (media == null) ? List.of() : media;
        }
    }

    public record Source(
            String name,
            String url,
            String license) {

    }

    public record Media(
            String type,
            String url,
            String alt,
            Source source) {

    }

    public record Summary(
            String category,
            String title) {

        public static Summary of(Guide guide) {
            return new Summary(guide.category(), guide.title());
        }
    }
}
