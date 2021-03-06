package ca.gristle.hadoop.datastores;

import ca.gristle.support.FSTestCase;
import ca.gristle.support.TestUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VersionedStoreTest extends FSTestCase {

    @Test
    public void testCleanup() throws Exception{
        String tmp1 = TestUtils.getTmpPath(fs, "versions");
        VersionedStore vs = new VersionedStore(tmp1);
        for (int i = 1; i <= 4; i ++) {
            String version = vs.createVersion(i);
            fs.mkdirs(new Path(version));
            vs.succeedVersion(i);
        }
        FileStatus[] files = fs.listStatus(new Path(tmp1));
        Assertions.assertEquals(files.length, 8);
        vs.cleanup(2);
        files = fs.listStatus(new Path(tmp1));
        Assertions.assertEquals(files.length, 4);
        for (FileStatus f : files) {
            String path = f.getPath().toString();
            Assertions.assertTrue(path.endsWith("3") ||
            path.endsWith("4") ||
            path.endsWith("3.version") ||
            path.endsWith(("4.version")));
        }
    }

    @Test
    public void testMultipleVersions() throws Exception{
        String tmp1 = TestUtils.getTmpPath(fs, "versions_checker");
        VersionedStore vs = new VersionedStore(tmp1);
        for (int i = 1; i <= 4; i ++) {
            String version = vs.createVersion(i);
            fs.mkdirs(new Path(version));
            vs.succeedVersion(i);
        }
        new File(new Path(tmp1, "5" + VersionedStore.FINISHED_VERSION_SUFFIX).toString()).createNewFile();
        Path invalidPath = new Path(tmp1, "_test");
        fs.mkdirs(invalidPath);
        new File(new Path(invalidPath, VersionedStore.HADOOP_SUCCESS_FLAG).toString()).createNewFile();

        List<Long> allVersions = vs.getAllVersions();
        Set<Long> output = new HashSet<Long>();
        output.addAll(allVersions);
        Set<Long> expected = new HashSet<Long>();
        for (int i = 1; i <= 4; i ++) {
            expected.add(Long.valueOf(i));
        }

        Assertions.assertEquals(output, expected);
    }

}
