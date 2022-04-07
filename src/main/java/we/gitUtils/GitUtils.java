package we.gitUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import we.processUtils.ProcessRunner;

import java.io.File;
import java.io.IOException;

@Slf4j
public class GitUtils {

    public static void cloneOrUpdate(String url, File directory) {
        cloneOrUpdate(url, null, null, directory);
    }

    public static void cloneOrUpdate(String url, String accessToken, File directory) {
        cloneOrUpdate(url, accessToken, null, directory);
    }

    @SneakyThrows
    public static void cloneOrUpdate(String url, String username, String password, File directory) {
        directory = new File(directory.getCanonicalPath());

        if (directory.exists()) {
            try {
                updateWorkingTree(directory);
            } catch (Exception e) {
                log.error("Could not update git repository", e);
                FileUtils.deleteDirectory(directory);
                clone(url, username, password, directory);
            }
        } else {
            clone(url, username, password, directory);
        }
    }

    public static void forceClone(String url, File directory) {
        forceClone(url, null, null, directory);
    }

    public static void forceClone(String url, String accessToken, File directory) {
        forceClone(url, accessToken, null, directory);
    }

    @SneakyThrows
    public static void forceClone(String url, String username, String password, File directory) {
        directory = new File(directory.getCanonicalPath());
        deleteFileIfExists(directory);
        clone(url, username, password, directory);
    }

    public static void clone(String url, File directory) {
        clone(url, null, null, directory);
    }

    public static void clone(String url, String accessToken, File directory) {
        clone(url, accessToken, null, directory);
    }

    public static void clone(String url, String username, String password, File directory) {
        if (StringUtils.isNotBlank(username)) {
            url = addCredentialsToGitRepositoryUrl(url, username, password);
        }
        File parentDirectory = directory.getParentFile();
        String directoryName = directory.getName();
        //noinspection ResultOfMethodCallIgnored
        parentDirectory.mkdirs();
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

    private static void updateWorkingTree(File directory) {
        ProcessRunner.runProcess(directory, "git", "fetch");
        ProcessRunner.runProcess(directory, "git", "pull");
    }

    private static void deleteFileIfExists(File directory) {
        if (directory.exists()) {
            try {
                FileUtils.forceDelete(directory);
            } catch (IOException e) {
                String message = (
                        "Could not delete %s. It must be blocked by a process. " +
                                "Restarting the IDE may help"
                ).formatted(directory);
                throw new RuntimeException(message, e);
            }
        }
    }
}
