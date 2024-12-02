package org.sc.themis;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Set;

public class Profiles {

    public static class TagWithUiTest implements QuarkusTestProfile {
        @Override
        public Set<String> tags() {
            return Set.of( "with-ui" );
        }
    }

    public static class TagWithoutUiTest implements QuarkusTestProfile {
        @Override
        public Set<String> tags() {
            return Set.of( "without-ui" );
        }
    }

}
