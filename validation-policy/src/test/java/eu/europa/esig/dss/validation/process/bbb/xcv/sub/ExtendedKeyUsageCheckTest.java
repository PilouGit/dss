package eu.europa.esig.dss.validation.process.bbb.xcv.sub;

import eu.europa.esig.dss.detailedreport.jaxb.XmlConstraint;
import eu.europa.esig.dss.detailedreport.jaxb.XmlStatus;
import eu.europa.esig.dss.detailedreport.jaxb.XmlSubXCV;
import eu.europa.esig.dss.diagnostic.CertificateWrapper;
import eu.europa.esig.dss.diagnostic.jaxb.XmlCertificate;
import eu.europa.esig.dss.diagnostic.jaxb.XmlOID;
import eu.europa.esig.dss.enumerations.Context;
import eu.europa.esig.dss.enumerations.ExtendedKeyUsage;
import eu.europa.esig.dss.policy.SubContext;
import eu.europa.esig.dss.policy.jaxb.Level;
import eu.europa.esig.dss.policy.jaxb.MultiValuesConstraint;
import eu.europa.esig.dss.validation.process.bbb.AbstractTestCheck;
import eu.europa.esig.dss.validation.process.bbb.xcv.sub.checks.ExtendedKeyUsageCheck;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExtendedKeyUsageCheckTest extends AbstractTestCheck {

    @Test
    public void valid() {
        List<XmlOID> extendedKeyUsages = new ArrayList<>();
        XmlOID xmlOID = new XmlOID();
        xmlOID.setDescription(ExtendedKeyUsage.TIMESTAMPING.getDescription());
        extendedKeyUsages.add(xmlOID);

        MultiValuesConstraint constraint = new MultiValuesConstraint();
        constraint.setLevel(Level.FAIL);
        constraint.getId().add(ExtendedKeyUsage.TIMESTAMPING.getDescription());

        XmlCertificate xc = new XmlCertificate();
        xc.setExtendedKeyUsages(extendedKeyUsages);

        XmlSubXCV result = new XmlSubXCV();
        ExtendedKeyUsageCheck ekuc = new ExtendedKeyUsageCheck(i18nProvider, result, new CertificateWrapper(xc),
                Context.TIMESTAMP, SubContext.SIGNING_CERT, constraint);
        ekuc.execute();

        List<XmlConstraint> constraints = result.getConstraint();
        assertEquals(1, constraints.size());
        assertEquals(XmlStatus.OK, constraints.get(0).getStatus());
    }

    @Test
    public void invalid() {
        List<XmlOID> extendedKeyUsages = new ArrayList<>();
        XmlOID xmlOID = new XmlOID();
        xmlOID.setDescription(ExtendedKeyUsage.TIMESTAMPING.getDescription());
        extendedKeyUsages.add(xmlOID);

        MultiValuesConstraint constraint = new MultiValuesConstraint();
        constraint.setLevel(Level.FAIL);
        constraint.getId().add("invalidKey");

        XmlCertificate xc = new XmlCertificate();
        xc.setExtendedKeyUsages(extendedKeyUsages);

        XmlSubXCV result = new XmlSubXCV();
        ExtendedKeyUsageCheck ekuc = new ExtendedKeyUsageCheck(i18nProvider, result, new CertificateWrapper(xc),
                Context.TIMESTAMP, SubContext.SIGNING_CERT, constraint);
        ekuc.execute();

        List<XmlConstraint> constraints = result.getConstraint();
        assertEquals(1, constraints.size());
        assertEquals(XmlStatus.NOT_OK, constraints.get(0).getStatus());
    }

    @Test
    public void multiValuesCheck() {
        List<XmlOID> extendedKeyUsages = new ArrayList<>();

        XmlOID xmlOIDOne = new XmlOID();
        xmlOIDOne.setDescription(ExtendedKeyUsage.OCSP_SIGNING.getDescription());
        extendedKeyUsages.add(xmlOIDOne);

        XmlOID xmlOIDTwo = new XmlOID();
        xmlOIDTwo.setDescription(ExtendedKeyUsage.TSL_SIGNING.getDescription());
        extendedKeyUsages.add(xmlOIDTwo);

        MultiValuesConstraint constraint = new MultiValuesConstraint();
        constraint.setLevel(Level.FAIL);
        constraint.getId().add(ExtendedKeyUsage.OCSP_SIGNING.getDescription());

        XmlCertificate xc = new XmlCertificate();
        xc.setExtendedKeyUsages(extendedKeyUsages);

        XmlSubXCV result = new XmlSubXCV();
        ExtendedKeyUsageCheck ekuc = new ExtendedKeyUsageCheck(i18nProvider, result, new CertificateWrapper(xc),
                Context.REVOCATION, SubContext.SIGNING_CERT, constraint);
        ekuc.execute();

        List<XmlConstraint> constraints = result.getConstraint();
        assertEquals(1, constraints.size());
        assertEquals(XmlStatus.OK, constraints.get(0).getStatus());
    }

}