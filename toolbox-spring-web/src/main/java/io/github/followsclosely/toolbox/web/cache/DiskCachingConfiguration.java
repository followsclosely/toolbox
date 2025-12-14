package io.github.followsclosely.toolbox.web.cache;

import lombok.Data;

@Data
public class DiskCachingConfiguration {
    private boolean enabled = true;
    private String directory = "./api-cache";
}
