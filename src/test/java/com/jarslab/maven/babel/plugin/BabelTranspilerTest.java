package com.jarslab.maven.babel.plugin;

import org.apache.maven.plugin.logging.Log;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class BabelTranspilerTest
{
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private TargetFileWriter targetFileWriter;
    @Mock
    private Log log;

    @Test
    public void shouldTranspileEs6File()
    {
        //given
        final Path sourceFilePath = Paths.get(TestUtils.getBasePath(), "/src/a/test-es6.js");
        final BabelTranspiler babelTranspiler = new BabelTranspiler(
                false, log, targetFileWriter,
                Paths.get(TestUtils.getBabelPath()).toFile(),
                sourceFilePath,
                "'es2015'",
                Charset.defaultCharset());
        //when
        babelTranspiler.execute();
        //then
        verify(targetFileWriter, times(1))
                .writeTargetFile(
                        eq(sourceFilePath),
                        argThat(arg -> !arg.contains("let")));
    }

    @Test
    public void shouldTranspileReactFile()
    {
        //given
        final Path sourceFilePath = Paths.get(TestUtils.getBasePath(), "/src/a/test-react.js");
        final BabelTranspiler babelTranspiler = new BabelTranspiler(
                false, log, targetFileWriter,
                Paths.get(TestUtils.getBabelPath()).toFile(),
                sourceFilePath,
                "'react'",
                Charset.defaultCharset());
        //when
        babelTranspiler.execute();
        //then
        verify(targetFileWriter, times(1))
                .writeTargetFile(
                        eq(sourceFilePath),
                        argThat(arg -> arg.contains("createElement")));
    }
}