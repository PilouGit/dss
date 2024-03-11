package eu.europa.esig.dss.pades.signature.visible.suite;

import eu.europa.esig.dss.diagnostic.DiagnosticData;
import eu.europa.esig.dss.diagnostic.PDFRevisionWrapper;
import eu.europa.esig.dss.diagnostic.SignatureWrapper;
import eu.europa.esig.dss.diagnostic.jaxb.XmlPDFLockDictionary;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.SignatureFieldParameters;
import eu.europa.esig.dss.pades.SignatureImageParameters;
import eu.europa.esig.dss.pades.SignatureImageTextParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.test.PKIFactoryAccess;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.reports.Reports;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.Color;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class PAdESFieldMDPTest extends PKIFactoryAccess {

    public static Stream<String> getParams() {

        PAdESService service = new PAdESService(new CommonCertificateVerifier());
        DSSDocument documentToSign = new InMemoryDocument(PAdESFieldMDPTest.class.getResourceAsStream("/validation/dss-2554/fieldmdp-exclude.pdf"));
        List<String> fieldIds = service.getAvailableSignatureFields(documentToSign);

        return fieldIds.stream();
    }

    @ParameterizedTest
    @MethodSource("getParams")
    void test(String fieldId) throws IOException {

        PAdESSignatureParameters signatureParameters = new PAdESSignatureParameters();
        signatureParameters.bLevel().setSigningDate(new Date());
        signatureParameters.setSigningCertificate(getSigningCert());
        signatureParameters.setCertificateChain(getCertificateChain());
        signatureParameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);

        SignatureImageParameters imageParameters = new SignatureImageParameters();
        SignatureImageTextParameters textParameters = new SignatureImageTextParameters();
        textParameters.setText("My signature");
        textParameters.setTextColor(Color.GREEN);
        imageParameters.setTextParameters(textParameters);

        SignatureFieldParameters fieldParams = new SignatureFieldParameters();
        fieldParams.setFieldId(fieldId);
        imageParameters.setFieldParameters(fieldParams);

        signatureParameters.setImageParameters(imageParameters);

        PAdESService service = new PAdESService(getOfflineCertificateVerifier());
        DSSDocument documentToSign = new InMemoryDocument(PAdESFieldMDPTest.class.getResourceAsStream("/validation/dss-2554/fieldmdp-exclude.pdf"));

        ToBeSigned dataToSign = service.getDataToSign(documentToSign, signatureParameters);
        SignatureValue signatureValue = getToken().sign(dataToSign, signatureParameters.getDigestAlgorithm(), getPrivateKeyEntry());
        DSSDocument signedDocument = service.signDocument(documentToSign, signatureParameters, signatureValue);

        // signedDocument.save("target/Signed" + fieldId + ".pdf");

        SignedDocumentValidator validator = SignedDocumentValidator.fromDocument(signedDocument);
        validator.setCertificateVerifier(getOfflineCertificateVerifier());
        Reports reports = validator.validateDocument();

        DiagnosticData diagnosticData = reports.getDiagnosticData();

        SignatureWrapper signature = diagnosticData.getSignatureById(diagnosticData.getFirstSignatureId());
        assertTrue(signature.isSignatureValid());
        PDFRevisionWrapper pdfRevision = signature.getPDFRevision();
        assertNotNull(pdfRevision);
        XmlPDFLockDictionary sigFieldLock = pdfRevision.getSigFieldLock();
        assertNotNull(sigFieldLock);
        XmlPDFLockDictionary fieldMDP = pdfRevision.getFieldMDP();
        assertNotNull(fieldMDP);
        assertEquals(sigFieldLock.getAction(), fieldMDP.getAction());
        assertEquals(sigFieldLock.getFields(), fieldMDP.getFields());
    }

    @Override
    protected String getSigningAlias() {
        return GOOD_USER;
    }
}