package eu.europa.esig.dss.pades.signature;

import eu.europa.esig.dss.cades.signature.CMSSignedDocument;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.PAdESTimestampParameters;
import eu.europa.esig.dss.pades.signature.suite.AbstractPAdESTestSignature;
import eu.europa.esig.dss.pdf.DSSMessageDigest;
import eu.europa.esig.dss.signature.DocumentSignatureService;
import eu.europa.esig.dss.validation.CertificateVerifier;
import org.junit.jupiter.api.BeforeEach;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class AbstractExternalCMSServiceTest extends AbstractPAdESTestSignature {

    private PAdESSignatureParameters signatureParameters;
    private Date signingTime;
    private DSSDocument documentToSign;

    @BeforeEach
    public void init() throws Exception {
        signingTime = new Date();
        documentToSign = new InMemoryDocument(getClass().getResourceAsStream("/sample.pdf"));
    }

    @Override
    protected DSSDocument sign() {
        ExternalCMSPAdESService service = new ExternalCMSPAdESService(getOfflineCertificateVerifier());

        PAdESSignatureParameters signatureParameters = new PAdESSignatureParameters();
        signatureParameters.bLevel().setSigningDate(signingTime);
        signatureParameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
        signatureParameters.setGenerateTBSWithoutCertificate(true);
        signatureParameters.setReason("DSS testing");

        DSSMessageDigest messageDigest = service.computeDocumentDigest(documentToSign, signatureParameters);
        assertNotNull(messageDigest);

        // Embedded CAdES is generated by a third party
        byte[] cmsSignedData = getSignedCMSignedData(messageDigest);
        assertNotNull(cmsSignedData);

        // Stateless
        service = new ExternalCMSPAdESService(getOfflineCertificateVerifier());
        service.setCmsSignedData(cmsSignedData);
        return service.signDocument(documentToSign, signatureParameters, null);
    }

    /**
     * Computes a CAdES with specific things for PAdES
     */
    private byte[] getSignedCMSignedData(DSSMessageDigest messageDigest) {
        signatureParameters = new PAdESSignatureParameters();
        signatureParameters.bLevel().setSigningDate(signingTime);
        signatureParameters.setSigningCertificate(getSigningCert());
        signatureParameters.setCertificateChain(getCertificateChain());
        signatureParameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
        signatureParameters.setReason("DSS testing");

        ExternalCMSService cmsForPAdESGenerationService = new ExternalCMSService(getOfflineCertificateVerifier());
        ToBeSigned dataToSign = cmsForPAdESGenerationService.getDataToSign(messageDigest, signatureParameters);

        SignatureValue signatureValue = getToken().sign(dataToSign, signatureParameters.getDigestAlgorithm(), getPrivateKeyEntry());

        // stateless
        cmsForPAdESGenerationService = new ExternalCMSService(getOfflineCertificateVerifier());
        CMSSignedDocument cmsSignedDocument = cmsForPAdESGenerationService.signMessageDigest(messageDigest, signatureParameters, signatureValue);
        return cmsSignedDocument.getBytes();
    }

    @Override
    protected DocumentSignatureService<PAdESSignatureParameters, PAdESTimestampParameters> getService() {
        return null;
    }

    @Override
    protected PAdESSignatureParameters getSignatureParameters() {
        return signatureParameters;
    }

    @Override
    protected DSSDocument getDocumentToSign() {
        return documentToSign;
    }

    @Override
    protected String getSigningAlias() {
        return GOOD_USER;
    }

    private static class ExternalCMSPAdESService extends PAdESService {

        private static final long serialVersionUID = -2003453716888412577L;

        private byte[] cmsSignedData;

        public ExternalCMSPAdESService(CertificateVerifier certificateVerifier) {
            super(certificateVerifier);
        }

        @Override
        protected byte[] generateCMSSignedData(final DSSDocument toSignDocument, final PAdESSignatureParameters parameters,
                                               final SignatureValue signatureValue) {
            if (this.cmsSignedData == null) {
                throw new NullPointerException("A CMS signed data must be provided");
            }
            return this.cmsSignedData;
        }

        public void setCmsSignedData(final byte[] cmsSignedData) {
            this.cmsSignedData = cmsSignedData;
        }

    }

}
