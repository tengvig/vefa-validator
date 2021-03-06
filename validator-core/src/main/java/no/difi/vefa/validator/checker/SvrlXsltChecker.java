package no.difi.vefa.validator.checker;

import net.sf.saxon.TransformerFactoryImpl;
import no.difi.vefa.validator.api.*;
import no.difi.xsd.vefa.validator._1.AssertionType;
import no.difi.xsd.vefa.validator._1.FlagType;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.util.JAXBResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.nio.file.Files;
import java.nio.file.Path;

@CheckerInfo({".xsl", ".xslt", ".svrl.xsl", ".svrl.xslt"})
public class SvrlXsltChecker implements Checker {

    private static Logger logger = LoggerFactory.getLogger(SvrlXsltChecker.class);

    private static TransformerFactory transformerFactory = new TransformerFactoryImpl();

    private Transformer transformer;
    private JAXBResult jaxbResult;

    public void prepare(Path path) throws ValidatorException {
        try {
            transformer = transformerFactory.newTransformer(new StreamSource(Files.newInputStream(path)));
            jaxbResult = new JAXBResult(JAXBContext.newInstance(SchematronOutput.class));
        } catch (Exception e) {
            throw new ValidatorException(e.getMessage(), e);
        }
    }

    @Override
    public void check(Document document, Section section) throws ValidatorException {
        long tsStart = System.currentTimeMillis();
        try {
            transformer.transform(new StreamSource(document.getInputStream()), jaxbResult);
            long tsEnd = System.currentTimeMillis();

            SchematronOutput output = (SchematronOutput) jaxbResult.getResult();

            section.setTitle(output.getTitle());
            section.setRuntime((tsEnd - tsStart) + "ms");

            for (Object o : output.getActivePatternAndFiredRuleAndFailedAssert())
                if (o instanceof FailedAssert)
                    add(section, (FailedAssert) o);
        } catch (Exception e) {
            throw new ValidatorException(
                    String.format("Unable to perform check: %s", e.getMessage()), e);
        }
    }

    public void add(Section section, FailedAssert failedAssert) {
        AssertionType assertionType = new AssertionType();

        String text = failedAssert.getText().replaceAll("\n", "").replaceAll("\r", "").replaceAll("\t", "");
        if (text.startsWith("[") && text.contains("]-")) {
            assertionType.setIdentifier(text.substring(1, text.indexOf("]-")).trim());
            text = text.substring(text.indexOf("]-") + 2).trim();
        } else {
            assertionType.setIdentifier("UNKNOWN");
        }

        assertionType.setText(text);
        assertionType.setLocation(failedAssert.getLocation());
        assertionType.setTest(failedAssert.getTest());

        switch (failedAssert.getFlag()) {
            case "fatal":
                assertionType.setFlag(FlagType.ERROR);
                break;
            case "warning":
                assertionType.setFlag(FlagType.WARNING);
                break;
            default:
                logger.warn("Unknown: " + failedAssert.getFlag());
                break;
        }

        section.add(assertionType);
    }
}
