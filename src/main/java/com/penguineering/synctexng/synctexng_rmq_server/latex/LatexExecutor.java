package com.penguineering.synctexng.synctexng_rmq_server.latex;

import com.penguineering.synctexng.synctexng_rmq_server.rmq.JobResult;
import com.penguineering.synctexng.synctexng_rmq_server.rmq.JobStatus;
import com.penguineering.synctexng.synctexng_rmq_server.workdir.WorkDir;
import com.penguineering.synctexng.synctexng_rmq_server.workdir.WorkDirSupplied;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

public class LatexExecutor extends WorkDirSupplied {
    private static final Logger logger = LoggerFactory.getLogger(LatexExecutor.class);


    private final Path nameRoot;
    private final Consumer<Path> resultArchivePathConsumer;
    private final LatexLogWrangler latexLogWrangler;

    public LatexExecutor(WorkDir workDir,
                         Path nameRoot,
                         Consumer<Path> resultArchivePathConsumer) {
        super(workDir);
        this.nameRoot = nameRoot;
        this.resultArchivePathConsumer = resultArchivePathConsumer;
        latexLogWrangler = new LatexLogWrangler(workDir, nameRoot);
    }

    public void compileOnePass() throws InterruptedException, IOException {
        ProcessBuilder pb = new ProcessBuilder(
                "pdflatex",
                "-interaction=nonstopmode",
                renderTexRootPath().toString());
        pb.directory(getWorkPath().toFile());
        Process p = pb.start();
        p.waitFor();
    }

    public Path renderTexRootPath() {
        return nameRoot.resolveSibling(nameRoot.getFileName().toString() + ".tex");
    }


    public JobResult compileDocument() {
        LatexLogSummary lastLogSummary = null;
        JobStatus status = JobStatus.FINISHED;

        final int maxPasses = 8;
        int passes = 0;
        do
            try {
                if (passes > maxPasses - 1) {
                    logger.error("Too many passes, aborting");
                    status = JobStatus.THROTTLED;
                    break;
                }

                passes++;

                logger.info("Compiling LaTeX document, pass: {}", passes);

                this.compileOnePass();

                // Copy log to distinct pass file
                Path passLogPath = latexLogWrangler.copyPassLog(passes);

                // Add the log file to the result archive
                resultArchivePathConsumer.accept(passLogPath);

                // Read the log line by line and look for "LaTeX Warning:"
                lastLogSummary = latexLogWrangler.analyzeLatestLog();
                if (lastLogSummary.hasErrors()) {
                    logger.info("LaTeX document contains errors: {}", lastLogSummary.errors());
                    break;
                }
            } catch (IOException | InterruptedException e) {
                logger.info("Failed to compile LaTeX document", e);
            } while (Objects.nonNull(lastLogSummary) && lastLogSummary.isRepeatPass());

        logger.info("Done compiling LaTeX document");

        // Add LaTeX result files to the result archive
        if (Objects.nonNull(lastLogSummary) && !lastLogSummary.hasErrors())
            resultArchivePathConsumer.accept(
                    nameRoot.resolveSibling(nameRoot.getFileName().toString() + ".pdf"));

        return new JobResult(
                (Objects.nonNull(lastLogSummary) && lastLogSummary.hasErrors()) ? JobStatus.FAILED : status,
                (Objects.nonNull(lastLogSummary) && lastLogSummary.hasErrors())
                        ? null
                        : getRelativePdfPath().toString(),
                passes,
                null,
                Objects.nonNull(lastLogSummary) && lastLogSummary.hasWarnings() ? lastLogSummary.warnings() : null,
                Objects.nonNull(lastLogSummary) && lastLogSummary.hasErrors() ? lastLogSummary.errors() : null
        );
    }

    private Path getRelativePdfPath() {
        return getWorkPath().relativize(nameRoot.resolveSibling(nameRoot.getFileName().toString() + ".pdf"));
    }

}
