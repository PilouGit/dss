package eu.europa.esig.dss.xades.requirements;

import java.io.File;
import java.util.Date;

import javax.xml.xpath.XPathExpressionException;

import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.FileDocument;
import eu.europa.esig.dss.SignatureAlgorithm;
import eu.europa.esig.dss.SignatureLevel;
import eu.europa.esig.dss.signature.SignaturePackaging;
import eu.europa.esig.dss.test.TestUtils;
import eu.europa.esig.dss.test.gen.CertificateService;
import eu.europa.esig.dss.test.mock.MockPrivateKeyEntry;
import eu.europa.esig.dss.test.mock.MockTSPSource;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;
import eu.europa.esig.dss.xades.signature.XAdESService;

public class XAdESBaselineLTTest extends AbstractRequirementChecks {

	@Override
	protected DSSDocument getSignedDocument() throws Exception {
		DSSDocument documentToSign = new FileDocument(new File("src/test/resources/sample.xml"));

		CertificateService certificateService = new CertificateService();
		MockPrivateKeyEntry privateKeyEntry = certificateService.generateCertificateChain(SignatureAlgorithm.RSA_SHA256);

		XAdESSignatureParameters signatureParameters = new XAdESSignatureParameters();
		signatureParameters.setSigningCertificate(privateKeyEntry.getCertificate());
		signatureParameters.setCertificateChain(privateKeyEntry.getCertificateChain());
		signatureParameters.setSignaturePackaging(SignaturePackaging.ENVELOPING);
		signatureParameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_LT);

		CertificateVerifier certificateVerifier = new CommonCertificateVerifier();
		XAdESService service = new XAdESService(certificateVerifier);
		service.setTspSource(new MockTSPSource(certificateService.generateTspCertificate(SignatureAlgorithm.RSA_SHA1), new Date()));

		byte[] dataToSign = service.getDataToSign(documentToSign, signatureParameters);
		byte[] signature = TestUtils.sign(SignatureAlgorithm.RSA_SHA256, privateKeyEntry, dataToSign);
		return service.signDocument(documentToSign, signatureParameters, signature);
	}

	@Override
	public void checkArchiveTimeStampPresent() throws XPathExpressionException {
		// No ArchiveTimestamp in Baseline Profile LT
	}

}
