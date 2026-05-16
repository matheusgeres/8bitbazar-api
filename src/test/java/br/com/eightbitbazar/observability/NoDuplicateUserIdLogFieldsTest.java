package br.com.eightbitbazar.observability;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class NoDuplicateUserIdLogFieldsTest {

    private static final Path PROJECT_ROOT = Path.of("").toAbsolutePath();

    @Test
    void onlyUnauthenticatedIdentityCreationLogsAddUserIdManually() throws IOException {
        Path mainSourceRoot = PROJECT_ROOT.resolve("src/main/java/br/com/eightbitbazar");
        Set<Path> allowedFiles = Set.of(
            PROJECT_ROOT.resolve("src/main/java/br/com/eightbitbazar/application/usecase/user/RegisterUser.java"),
            PROJECT_ROOT.resolve("src/main/java/br/com/eightbitbazar/adapter/in/web/AuthController.java")
        );

        try (Stream<Path> javaFiles = Files.walk(mainSourceRoot)) {
            List<Path> filesWithManualUserId = javaFiles
                .filter(path -> path.toString().endsWith(".java"))
                .filter(path -> containsManualUserIdLogField(path))
                .toList();

            assertThat(filesWithManualUserId)
                .containsExactlyInAnyOrderElementsOf(allowedFiles);
        }
    }

    @Test
    void unauthenticatedIdentityCreationLogsKeepExplicitUserId() throws IOException {
        List<Path> unauthenticatedIdentityFiles = List.of(
            PROJECT_ROOT.resolve("src/main/java/br/com/eightbitbazar/application/usecase/user/RegisterUser.java"),
            PROJECT_ROOT.resolve("src/main/java/br/com/eightbitbazar/adapter/in/web/AuthController.java")
        );

        for (Path sourceFile : unauthenticatedIdentityFiles) {
            assertThat(Files.readString(sourceFile))
                .as(sourceFile.toString())
                .contains(".addKeyValue(\"userId\"");
        }
    }

    @Test
    void textLogPatternsRenderUserIdOnlyWhenMdcHasValue() throws IOException {
        Path logbackConfig = PROJECT_ROOT.resolve("src/main/resources/logback-spring.xml");

        assertThat(Files.readString(logbackConfig))
            .contains("%replace(%X{userId}){'^(.+)$',' [userId=$1]'}")
            .doesNotContain("[userId=%X{userId}]");
    }

    private static boolean containsManualUserIdLogField(Path path) {
        try {
            return Files.readString(path).contains(".addKeyValue(\"userId\"");
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read " + path, exception);
        }
    }
}
