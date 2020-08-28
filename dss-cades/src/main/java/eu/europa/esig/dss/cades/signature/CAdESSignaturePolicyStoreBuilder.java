package eu.europa.esig.dss.cades.signature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.esig.dss.cades.CMSUtils;
import eu.europa.esig.dss.cades.validation.CAdESSignature;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.Digest;
import eu.europa.esig.dss.model.SignaturePolicyStore;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.spi.OID;
import eu.europa.esig.dss.validation.SignaturePolicy;
import eu.europa.esig.dss.validation.policy.SignaturePolicyValidator;
import eu.europa.esig.dss.validation.policy.SignaturePolicyValidatorLoader;

public class CAdESSignaturePolicyStoreBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(CAdESSignaturePolicyStoreBuilder.class);
	
	public CMSSignedData addSignaturePolicyStore(CMSSignedData cmsSignedData, SignaturePolicyStore signaturePolicyStore) {
		Objects.requireNonNull(cmsSignedData, "CMSSignedData must be provided");
		Objects.requireNonNull(signaturePolicyStore, "SignaturePolicyStore must be provided");
		Objects.requireNonNull(signaturePolicyStore.getSpDocSpecification(), "SpDocSpecification must be provided");
		Objects.requireNonNull(signaturePolicyStore.getSpDocSpecification().getId(), "ID (OID or URI) for SpDocSpecification must be provided");
		Objects.requireNonNull(signaturePolicyStore.getSignaturePolicyContent(), "Signature policy content must be provided");
		
		Collection<SignerInformation> signerInformationCollection = cmsSignedData.getSignerInfos().getSigners();
		final List<SignerInformation> newSignerInformationList = new ArrayList<>();
		
		for (SignerInformation signerInformation : signerInformationCollection) {
			assertSignaturePolicyStoreExtensionPossible(signerInformation);
			SignerInformation newSignerInformation = signerInformation;
			
			CAdESSignature cadesSignature = new CAdESSignature(cmsSignedData, signerInformation);
			SignaturePolicy signaturePolicy = cadesSignature.getSignaturePolicy();
			if (signaturePolicy != null) {
				Digest expectedDigest = signaturePolicy.getDigest();
				if (expectedDigest != null) {
					signaturePolicy.setPolicyContent(signaturePolicyStore.getSignaturePolicyContent());
					SignaturePolicyValidator validator = new SignaturePolicyValidatorLoader(signaturePolicy).loadValidator();
					Digest computedDigest = validator.getComputedDigest(expectedDigest.getAlgorithm());
					if (expectedDigest.equals(computedDigest)) {
						newSignerInformation = addSignaturePolicyStore(signerInformation, signaturePolicyStore);
					} else {
						LOG.warn("Signature policy's digest doesn't match the document {} for signature {}", expectedDigest, cadesSignature.getId());
					}
				} else {
					LOG.warn("SignaturePolicyIdentifier Digest is not found for a signature with id {}", cadesSignature.getId());
				}
			} else {
				LOG.warn("SignaturePolicyIdentifier is not defined for a signature with id {}", cadesSignature.getId());
			}
			newSignerInformationList.add(newSignerInformation);
		}
		final SignerInformationStore newSignerStore = new SignerInformationStore(newSignerInformationList);
		
		return CMSSignedData.replaceSigners(cmsSignedData, newSignerStore);
	}
	
	private SignerInformation addSignaturePolicyStore(SignerInformation signerInformation, SignaturePolicyStore signaturePolicyStore) {
		AttributeTable unsignedAttributes = CMSUtils.getUnsignedAttributes(signerInformation);
		ASN1Sequence sigPolicyStore = getSignaturePolicyStore(signaturePolicyStore);
		AttributeTable unsignedAttributesWithPolicyStore = unsignedAttributes.add(OID.id_aa_ets_sigPolicyStore, sigPolicyStore);
		return SignerInformation.replaceUnsignedAttributes(signerInformation, unsignedAttributesWithPolicyStore);
	}

	/**
	 * SignaturePolicyStore ::= SEQUENCE {
	 *  spDocSpec SPDocSpecification ,
	 *  spDocument SignaturePolicyDocument
	 * }
	 * SignaturePolicyDocument ::= CHOICE {
	 *  sigPolicyEncoded OCTET STRING,
	 *  sigPolicyLocalURI IA5String
	 * }
	 */
	private ASN1Sequence getSignaturePolicyStore(SignaturePolicyStore signaturePolicyStore) {
		final ASN1EncodableVector sigPolicyStore = new ASN1EncodableVector();
		// spDocSpec
		sigPolicyStore.add(getSPDocSpecificationId(signaturePolicyStore.getSpDocSpecification().getId()));
		// spDocument : only complete octets supported
		sigPolicyStore.add(new DEROctetString(DSSUtils.toByteArray(signaturePolicyStore.getSignaturePolicyContent()))); 
		return new DERSequence(sigPolicyStore);
	}
	
	/**
	 * SPDocSpecification ::= CHOICE {
	 *  oid OBJECT IDENTIFIER,
	 *  uri IA5String
	 * }
	 */
	private ASN1Primitive getSPDocSpecificationId(String oidOrUri) {
		ASN1Primitive spDocSpecification = null;
		if (DSSUtils.isOidCode(oidOrUri)) {
			spDocSpecification = new ASN1ObjectIdentifier(oidOrUri);
		} else {
			spDocSpecification = new DERIA5String(oidOrUri);
		}
		return spDocSpecification;
	}
	
	private void assertSignaturePolicyStoreExtensionPossible(SignerInformation signerInformation) {
		if (CMSUtils.containsATSTv2(signerInformation)) {
			throw new DSSException("Cannot add signature policy store to a CAdES containing an archiveTimestampV2");
		}
	}

}
