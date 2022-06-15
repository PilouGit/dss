/**
 * DSS - Digital Signature Services
 * Copyright (C) 2015 European Commission, provided under the CEF programme
 * 
 * This file is part of the "DSS - Digital Signature Services" project.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package eu.europa.esig.dss.pades.validation.dss2236;

import eu.europa.esig.dss.diagnostic.DiagnosticData;
import eu.europa.esig.dss.diagnostic.SignatureWrapper;
import eu.europa.esig.dss.diagnostic.jaxb.XmlModification;
import eu.europa.esig.dss.diagnostic.jaxb.XmlModificationDetection;
import eu.europa.esig.dss.diagnostic.jaxb.XmlObjectModifications;
import eu.europa.esig.dss.diagnostic.jaxb.XmlPDFRevision;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.pades.validation.suite.AbstractPAdESTestValidation;
import eu.europa.esig.dss.pdf.modifications.DefaultPdfDifferencesFinder;
import eu.europa.esig.dss.pdf.modifications.DefaultPdfObjectModificationsFinder;
import eu.europa.esig.dss.pdf.modifications.PdfModificationDetectionUtils;
import eu.europa.esig.dss.utils.Utils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DSS2236HideTest extends AbstractPAdESTestValidation {

	@Override
	protected DSSDocument getSignedDocument() {
		return new InMemoryDocument(getClass().getResourceAsStream("/validation/dss-2236/hide.pdf"));
	}

	@BeforeAll
	public static void init() {
		PdfModificationDetectionUtils pdfModificationDetectionUtils = PdfModificationDetectionUtils.getInstance();

		DefaultPdfDifferencesFinder pdfDifferencesFinder = new DefaultPdfDifferencesFinder();
		pdfDifferencesFinder.setMaximalPagesAmountForVisualComparison(1);
		pdfModificationDetectionUtils.setPdfDifferencesFinder(pdfDifferencesFinder);

		DefaultPdfObjectModificationsFinder pdfObjectModificationsFinder = new DefaultPdfObjectModificationsFinder();
		pdfObjectModificationsFinder.setMaximumObjectVerificationDeepness(10);
		pdfModificationDetectionUtils.setPdfObjectModificationsFinder(pdfObjectModificationsFinder);
	}
	
	@Override
	protected void checkPdfRevision(DiagnosticData diagnosticData) {
		SignatureWrapper signature = diagnosticData.getSignatureById(diagnosticData.getFirstSignatureId());
		XmlPDFRevision pdfRevision = signature.getPDFRevision();
		
		XmlModificationDetection modificationDetection = pdfRevision.getModificationDetection();
		assertNotNull(modificationDetection);
		
		List<XmlModification> visualDifferences = modificationDetection.getVisualDifference();
		assertEquals(1, visualDifferences.size());
		assertEquals(1, visualDifferences.get(0).getPage().intValue());

		XmlObjectModifications objectModifications = modificationDetection.getObjectModifications();
		assertNotNull(objectModifications);
		assertTrue(Utils.isCollectionNotEmpty(objectModifications.getUndefined()));
	}
	
	@Override
	protected void checkSigningCertificateValue(DiagnosticData diagnosticData) {
		SignatureWrapper signature = diagnosticData.getSignatureById(diagnosticData.getFirstSignatureId());
		assertFalse(signature.isSigningCertificateIdentified());
	}

	@AfterAll
	public static void clean() {
		PdfModificationDetectionUtils pdfModificationDetectionUtils = PdfModificationDetectionUtils.getInstance();
		pdfModificationDetectionUtils.setPdfDifferencesFinder(new DefaultPdfDifferencesFinder());
		pdfModificationDetectionUtils.setPdfObjectModificationsFinder(new DefaultPdfObjectModificationsFinder());
	}

}
