package io.github.followsclosely.toolbox;

import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class PathBuilderTest {

    @Test
    void explodeOnGroups() {
        Collection<String> path = new PathBuilder()
                .add("minifigures")
                .explodeOnGroups("pb36a", "^(.*[a-zA-Z]+)\\d+.*$")
                .build();

        path.forEach(System.out::println);
    }
}