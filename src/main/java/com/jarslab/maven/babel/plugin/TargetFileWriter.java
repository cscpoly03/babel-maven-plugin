package com.jarslab.maven.babel.plugin;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Objects.requireNonNull;

class TargetFileWriter
{
    private final String sourceDir;
    private final String targetDir;
    private final String prefix;
    private final Charset charset;

    TargetFileWriter(final String sourceDir,
                     final String targetDir,
                     final String prefix,
                     final Charset charset)
    {
        this.sourceDir = requireNonNull(sourceDir);
        this.targetDir = requireNonNull(targetDir);
        this.prefix = prefix != null ? prefix : "";
        this.charset = requireNonNull(charset);
    }

    void writeTargetFile(final Path sourceFile,
                         final String content)
    {
        final Path sourceFileName = sourceFile.getFileName();
        final String targetDirectory = replaceLast(sourceFileName.toString(), "",
                sourceFile.toString().replace(sourceDir, targetDir));
        try {
            Files.createDirectories(Paths.get(targetDirectory));
            Files.write(Paths.get(targetDirectory, prefix + sourceFileName),
                    content.getBytes(charset));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String replaceLast(final String target,
                               final String replacement,
                               final String value)
    {
        final int lastIndex = value.lastIndexOf(target);
        if (lastIndex == -1) {
            return value;
        }
        return String.join("",
                value.substring(0, lastIndex),
                replacement,
                value.substring(lastIndex + target.length()));
    }
}
