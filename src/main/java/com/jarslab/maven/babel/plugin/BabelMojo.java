package com.jarslab.maven.babel.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mojo(name = "babel", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, threadSafe = true)
public class BabelMojo extends AbstractMojo
{
    @Parameter(property = "verbose", defaultValue = "false")
    private boolean verbose = false;
    @Parameter(property = "parallel", defaultValue = "true")
    private boolean parallel = true;
    @Parameter(property = "babelSrc", required = true)
    private String babelSrc;
    @Parameter(property = "sourceDir", required = true)
    private String sourceDir;
    @Parameter(property = "targetDir", required = true)
    private String targetDir;
    @Parameter(property = "jsSourceFiles", alias = "jsFiles")
    private List<String> jsSourceFiles;
    @Parameter(property = "jsSourceIncludes", alias = "jsIncludes")
    private List<String> jsSourceIncludes;
    @Parameter(property = "jsSourceExcludes", alias = "jsExcludes")
    private List<String> jsSourceExcludes;
    @Parameter(property = "prefix")
    private String prefix;
    @Parameter(property = "presets", defaultValue = "es2015")
    private String presets;
    @Parameter(property = "encoding")
    private String encoding = Charset.defaultCharset().name();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        final Charset charset = Charset.forName(encoding);
        if (verbose) {
            getLog().info("Run in the verbose mode.");
            getLog().info(String.format("Charset: %s.", charset));
        }
        final File babelSrcFile = Paths.get(babelSrc).toFile();
        if (!babelSrcFile.exists() || !babelSrcFile.canRead()) {
            getLog().error("Given Babel file is not reachable.");
            throw new MojoFailureException("Given Babel file is not reachable.");
        }
        if (presets.isEmpty()) {
            throw new MojoFailureException("No Babel presets defined.");
        }
        if (jsSourceFiles.isEmpty() && jsSourceIncludes.isEmpty()) {
            getLog().warn("No source files provided, nothing to do.");
            return;
        }

        final SourceFilesExtractor sourceFilesExtractor = new SourceFilesExtractor(
                sourceDir, jsSourceFiles, jsSourceIncludes, jsSourceExcludes);
        final TargetFileWriter targetFileWriter = new TargetFileWriter(sourceDir, targetDir, prefix, charset);
        final String formattedPresets = getFormattedPresets(presets);
        final Set<Path> sourceFiles = sourceFilesExtractor.getSourceFiles();
        if (verbose) {
            getLog().info(String.format("Found %s files to transpile.", sourceFiles.size()));
        }
        try {
            final Stream<BabelTranspiler> transpilers = sourceFiles.stream()
                    .map(sourceFile -> new BabelTranspiler(
                            verbose,
                            getLog(),
                            targetFileWriter,
                            babelSrcFile,
                            sourceFile,
                            formattedPresets,
                            charset));
            if (parallel) {
                getLog().info("Run in parallel mode.");
                transpilers.parallel();
            }
            transpilers
                    .peek(this::logExtractedFile)
                    .forEach(BabelTranspiler::execute);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed on Babel transpile execution.", e);
        }
        getLog().info("Babel transpile execution successful.");
    }

    private String getFormattedPresets(final String presets)
    {
        return Stream.of(presets.split(","))
                .map(String::trim)
                .map(preset -> String.format("'%s'", preset))
                .collect(Collectors.joining(","));
    }

    private void logExtractedFile(final BabelTranspiler babelTranspiler)
    {
        if (verbose) {
            getLog().info(String.format("About to transpile: `%s`.",
                    babelTranspiler.getSourceFilePath()));
        }
    }

    void setVerbose(final boolean verbose)
    {
        this.verbose = verbose;
    }

    public void setParallel(final boolean parallel)
    {
        this.parallel = parallel;
    }

    void setBabelSrc(final String babelSrc)
    {
        this.babelSrc = babelSrc;
    }

    void setSourceDir(final String sourceDir)
    {
        this.sourceDir = sourceDir;
    }

    void setTargetDir(final String targetDir)
    {
        this.targetDir = targetDir;
    }

    void setJsSourceFiles(final List<String> jsSourceFiles)
    {
        this.jsSourceFiles = jsSourceFiles;
    }

    void setJsSourceIncludes(final List<String> jsSourceIncludes)
    {
        this.jsSourceIncludes = jsSourceIncludes;
    }

    void setJsSourceExcludes(final List<String> jsSourceExcludes)
    {
        this.jsSourceExcludes = jsSourceExcludes;
    }

    void setPrefix(final String prefix)
    {
        this.prefix = prefix;
    }

    void setPresets(final String presets)
    {
        this.presets = presets;
    }
}
