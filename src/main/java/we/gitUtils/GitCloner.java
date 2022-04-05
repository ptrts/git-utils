package we.gitUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import we.processUtils.ProcessRunner;

import java.io.File;

@Slf4j
public class GitCloner {

    @SneakyThrows
    public static void cloneOrUpdateGitRepository(String url, String username, String password, File directory) {
        if (directory.exists()) {
            try {
                updateGitRepository(directory);
            } catch (Exception e) {
                log.error("Could not update git repository", e);
                FileUtils.deleteDirectory(directory);
                cloneGitRepository(url, username, password, directory);
            }
        } else {
            cloneGitRepository(url, username, password, directory);
        }
    }

    private static void cloneGitRepository(String url, String username, String password, File directory) {
        if (StringUtils.isNotBlank(username)) {
            url = addCredentialsToGitRepositoryUrl(url, username, password);
        }
        File parentDirectory = directory.getParentFile();
        String directoryName = directory.getName();
        //noinspection ResultOfMethodCallIgnored
        directory.mkdirs();
        ProcessRunner.runProcess(parentDirectory, "git", "clone", url, directoryName);
    }

    private static String addCredentialsToGitRepositoryUrl(String url, String username, String password) {

        String userInfoString = getUserInfoString(username, password);

        UriComponents uriComponents = UriComponentsBuilder
                .fromHttpUrl(url)
                .userInfo(userInfoString)
                .encode()
                .build();

        return uriComponents.toString();
    }

    private static String getUserInfoString(String username, String password) {
        StringBuilder sb = new StringBuilder(username);
        if (StringUtils.isNotBlank(password)) {
            sb.append(":").append(password);
        }
        return sb.toString();
    }

    private static void updateGitRepository(File directory) {
        ProcessRunner.runProcess(directory, "git", "fetch");
        ProcessRunner.runProcess(directory, "git", "pull");
    }
}
