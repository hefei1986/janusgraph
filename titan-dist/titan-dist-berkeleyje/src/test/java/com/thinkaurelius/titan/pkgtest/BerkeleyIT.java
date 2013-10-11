package com.thinkaurelius.titan.pkgtest;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

import org.junit.Test;

public class BerkeleyIT {
    
    @Test
    public void testSimpleGremlinSession() throws IOException, InterruptedException {
        unzip("target", "titan-berkeleydb-0.4.0-SNAPSHOT.zip");
        expect("target" + File.separator + "titan-berkeleydb-0.4.0-SNAPSHOT", "../../bdb-single-vertex.expect");
    }
    
    private static void expect(String dir, String expectScript) throws IOException, InterruptedException {
        command(new File(dir), "expect", expectScript);
    }
    
    private static void unzip(String dir, String zipfile) throws IOException, InterruptedException {
        command(new File(dir), "unzip", "-q", zipfile);
    }
    
    private static void command(File dir, String... command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(dir);
        pb.redirectInput(Redirect.PIPE);
        /*
         * Using Redirect.INHERIT with expect breaks maven-failsafe-plugin when
         * failsafe is configured to fork. After running a test that invokes
         * expect, the child process running maven-failsafe-plugin echoes
         * failsafe-internal test result strings to the console instead of
         * through a pipe to its parent. The parent never sees these strings and
         * assumes no tests ran. expect is probably doing something nasty to its
         * file descriptors and neglecting to clean up after itself. Invoking
         * unzip does not break failsafe+forks in this way. So expect must be
         * doing something bad.
         * 
         * Redirect.INHERIT works fine if failsafe is configured to never fork.
         */
//        pb.redirectOutput(Redirect.appendTo(new File("crap.stdout")));
//        pb.redirectError(Redirect.appendTo(new File("crap.stderr")));
        pb.redirectOutput(Redirect.INHERIT);
        pb.redirectError(Redirect.INHERIT);
        Process p = pb.start();
        // Sense of "input" and "output" are reversed between ProcessBuilder and Process
        p.getOutputStream().close(); // Child zip process sees EOF on stdin (if it reads stdin at all)
        assertEquals(0, p.waitFor());
    }
}