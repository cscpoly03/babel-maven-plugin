package com.jarslab.maven.babel.plugin;

import org.apache.maven.plugin.logging.Log;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

class BabelTranspiler
{
    private static final String JAVASCRIPT_LANGUAGE = "js";
    private static final String INPUT_VARIABLE = "input";
    private static final String BABEL_EXECUTE = "Babel.transform(%s, {presets: [%s]}).code";

    private final boolean verbose;
    private final Log log;
    private final TargetFileWriter targetFileWriter;
    private final File babelSource;
    private final Path sourceFilePath;
    private final String presets;
    private final Charset charset;

    BabelTranspiler(final boolean verbose,
                    final Log log,
                    final TargetFileWriter targetFileWriter,
                    final File babelSource,
                    final Path sourceFilePath,
                    final String presets,
                    final Charset charset)
    {
        this.verbose = verbose;
        this.log = requireNonNull(log);
        this.targetFileWriter = requireNonNull(targetFileWriter);
        this.babelSource = requireNonNull(babelSource);
        this.sourceFilePath = requireNonNull(sourceFilePath);
        this.presets = requireNonNull(presets);
        this.charset = requireNonNull(charset);
    }

    void execute()
    {
        try {
            final Context.Builder contextBuilder = Context.newBuilder(JAVASCRIPT_LANGUAGE);
            try (final Context context = contextBuilder.build()) {
                final Value bindings = context.getBindings(JAVASCRIPT_LANGUAGE);
                context.eval(Source.newBuilder(JAVASCRIPT_LANGUAGE, babelSource).build());
                final String source = new String(Files.readAllBytes(sourceFilePath), charset);
                if (verbose) {
                    log.debug(String.format("%s source:\n%s", sourceFilePath, source));
                }
                bindings.putMember(INPUT_VARIABLE, source);
                final String result = context.eval(JAVASCRIPT_LANGUAGE, String.format(BABEL_EXECUTE, INPUT_VARIABLE, presets)).asString();
                if (verbose) {
                    log.debug(String.format("%s result:\n%s", sourceFilePath, result));
                }
                targetFileWriter.writeTargetFile(sourceFilePath, result);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Path getSourceFilePath()
    {
        return sourceFilePath;
    }
}
