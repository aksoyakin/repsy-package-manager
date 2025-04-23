package io.repsy.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageMetadata {
    private String name;
    private String version;
    private String author;
    private List<Dependency> dependencies;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Dependency {
        private String packageName;
        private String version;
    }
}
