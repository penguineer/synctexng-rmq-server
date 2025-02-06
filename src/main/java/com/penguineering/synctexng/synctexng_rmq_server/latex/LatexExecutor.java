package com.penguineering.synctexng.synctexng_rmq_server.latex;

import com.penguineering.synctexng.synctexng_rmq_server.WorkdirPathOperatorBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

public class LatexExecutor extends WorkdirPathOperatorBase {
    private static final Logger logger = LoggerFactory.getLogger(LatexExecutor.class);


    private final Path nameRoot;
    private final Consumer<Path> resultArchivePathConsumer;

    public LatexExecutor(Path workDir,
                         Path nameRoot,
                         Consumer<Path> resultArchivePathConsumer) {
        super(workDir);
        this.nameRoot = nameRoot;
        this.resultArchivePathConsumer = resultArchivePathConsumer;
    }

    public void compileOnePass() throws InterruptedException, IOException {
        ProcessBuilder pb = new ProcessBuilder(
                "pdflatex",
                "-interaction=nonstopmode",
                renderTexRootPath().toString());
        pb.directory(getWorkDir().toFile());
        Process p = pb.start();
        p.waitFor();
    }

    public Path renderTexRootPath() {
        return nameRoot.resolveSibling(nameRoot.getFileName().toString() + ".tex");
    }


    public void compileDocument() {
        LatexLogWrangler latexLogWrangler = new LatexLogWrangler(getWorkDir(), nameRoot);
        LatexLogSummary lastLogSummary = null;

        int passes = 0;
        do
            try {
                passes++;

                if (passes > 8) {
                    logger.error("Too many passes, aborting");
                    break;
                }

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
    }
}
