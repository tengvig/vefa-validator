package no.difi.vefa.validator.api.build;

import java.io.ByteArrayOutputStream;
import java.io.File;

public interface Preparer {
    ByteArrayOutputStream prepare(Build build, File file) throws Exception;
}
