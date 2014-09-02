package org.kuali.kfs.fp.dataaccess;

import static org.kuali.kfs.sys.fixture.UserNameFixture.hschrein;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.ReportQueryByCriteria;
import org.kuali.kfs.fp.businessobject.CapitalAssetInformation;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.dataaccess.CapitalAssetInformationDao;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.DocumentTestUtils;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.DocumentService;
import org.springmodules.orm.ojb.PersistenceBrokerTemplate;

@ConfigureContext(session = hschrein)
public class CapitalAssetInformationDaoTest extends KualiTestBase {
    protected Logger LOG = Logger.getLogger(CapitalAssetInformationDaoTest.class);

    protected CapitalAssetInformationDao capitalAssetInformationDao;
    protected DocumentService documentService;
    protected BusinessObjectService businessObjectService;
    protected PersistenceBrokerTemplate persistenceBrokerTemplate;

    public static final int numberOfDocumentsWithoutCapitalAssetInformationsToTest = 1;
    public static final int numberOfDocumentsWithCapitalAssetInformationsToTest = 10;

    @Override
    protected void setUp() throws Exception {
        capitalAssetInformationDao = SpringContext.getBean(CapitalAssetInformationDao.class);
        documentService = SpringContext.getBean(DocumentService.class);
        businessObjectService = SpringContext.getBean(BusinessObjectService.class);
        persistenceBrokerTemplate = ((PlatformAwareDaoBaseOjb)capitalAssetInformationDao).getPersistenceBrokerTemplate();
        createCapitalAssetInfos(Arrays.asList(new Integer[]{5,4,3,2,0}));
    }

    /**
     * This function creates the requested number of DV Documents. No data
     * are added to the documents such as payee info or accounting lines.
     *
     * @throws Exception
     */
    protected List<String> createDVs (int numberOfDvs) throws Exception{
        List<String> disbursementVoucherDocuments = new ArrayList<String>(numberOfDvs);
        for (int disbursementVoucherDocumentIndex = 0; disbursementVoucherDocumentIndex < numberOfDvs; ++disbursementVoucherDocumentIndex){
            DisbursementVoucherDocument newDisbursementVoucherDocument = DocumentTestUtils.createDocument(documentService, DisbursementVoucherDocument.class);
            LOG.debug(String.format("Created new DisbursementVoucherDocument with document number %s", newDisbursementVoucherDocument.getDocumentNumber()));
            documentService.saveDocument(newDisbursementVoucherDocument);
            disbursementVoucherDocuments.add(newDisbursementVoucherDocument.getDocumentNumber());
        }
        return disbursementVoucherDocuments;
    }

    /**
     * This function creates new CapitalAssetInformations attached to some newly created DVs.
     * @param numlines For each number, one DV is created and that number of
     *   capital asset informations is created and associated with the DV.
     */
    protected void createCapitalAssetInfos (Collection<Integer> numbersOfCapitalAssetInformations) throws Exception{
        for (Integer numberOfCapitalAssetInformations : numbersOfCapitalAssetInformations){
            /*risking out of bounds exception, but hey, what's life without some risks?*/
            String disbursementVoucherDocumentNumber = createDVs(1).get(0);

            for (int capitalAssetInformationIndex = 0; capitalAssetInformationIndex < numberOfCapitalAssetInformations; ++capitalAssetInformationIndex){
                CapitalAssetInformation currentCapitalAssetInformation = new CapitalAssetInformation();
                currentCapitalAssetInformation.setDocumentNumber(disbursementVoucherDocumentNumber);
                currentCapitalAssetInformation.setCapitalAssetLineNumber(capitalAssetInformationIndex);
                LOG.debug(String.format("Saving capital asset informaiton %s which has line number %d and document number %s",
                        currentCapitalAssetInformation, capitalAssetInformationIndex, disbursementVoucherDocumentNumber));
                businessObjectService.save(currentCapitalAssetInformation);
            }
        }
    }

    /**
     * Retrieves all the capital asset information line numbers associated
     * with the given document number.
     *
     * @param dvdocnum
     * @return We return an Iterator so that records can be lazily retrieved. We are
     *     retrieving a potentially large number of documents, but we hopefully won't need
     *     terribly many of them.
     */
    protected Iterator<Object[]> getCapitalAssetInfoLines(String financialDocumentNumber){
        LOG.debug(String.format("Working on document %s", financialDocumentNumber));
        Criteria criterion = new Criteria();
        criterion.addEqualTo("fdoc_nbr", financialDocumentNumber);
        ReportQueryByCriteria reportQuery = new ReportQueryByCriteria(CapitalAssetInformation.class, new String[]{"cptlast_line_nbr"},criterion);
        return persistenceBrokerTemplate.getReportQueryIteratorByQuery(reportQuery);
    }

    /**
     * This test ensures that the CapitalAssetInformationDao returns a unique line number for a
     * new CapitalAssetInformation. The only way this test can fail is if a line number is suggested
     * that is already in use, or if an uncaught exception is thrown.
     *
     * The test is broken up into two parts: testing documents that have capital asset information
     * lines associated with them, and testing those that don't.
     */
    public void testGetNextCapitalAssetLineNumber () {
        Criteria criterion = new Criteria();
        ReportQueryByCriteria disbursementVoucherDocumentQuery = new ReportQueryByCriteria(DisbursementVoucherDocument.class, new String[]{"fdoc_nbr"}, criterion);
        Iterator<Object[]> disbursementVoucherDocumentNumbers = persistenceBrokerTemplate.getReportQueryIteratorByQuery(disbursementVoucherDocumentQuery);
        int documentsWithoutCapitalAssetInformationsTested = 0;
        int documentsWithCapitalAssetInformationsTested = 0;
        while (disbursementVoucherDocumentNumbers.hasNext()){
            String currentDocumentNumber = (String)disbursementVoucherDocumentNumbers.next()[0];
            LOG.debug(String.format("testing document number %s", currentDocumentNumber));
            Iterator<Object[]> capitalAssetInformationLineNumbers = getCapitalAssetInfoLines(currentDocumentNumber);
            if (capitalAssetInformationLineNumbers.hasNext()) {
                LOG.debug("  There are lines in this document.");
                if (documentsWithCapitalAssetInformationsTested < numberOfDocumentsWithCapitalAssetInformationsToTest){
                    testGetNextCapitalAssetLineNumberWithCapitalAssetInformations(currentDocumentNumber, capitalAssetInformationLineNumbers);
                    ++documentsWithCapitalAssetInformationsTested;
                }
            } else {
                LOG.debug("  There are no lines in this document.");
                if (documentsWithoutCapitalAssetInformationsTested < numberOfDocumentsWithoutCapitalAssetInformationsToTest){
                    testGetNextCapitalAssetLineNumberWithoutCapitalAssetInformations(currentDocumentNumber);
                    ++documentsWithoutCapitalAssetInformationsTested;
                }
            }
            if (documentsWithoutCapitalAssetInformationsTested >= numberOfDocumentsWithoutCapitalAssetInformationsToTest
                    && documentsWithCapitalAssetInformationsTested > numberOfDocumentsWithCapitalAssetInformationsToTest){
                LOG.debug("Finished testing all documents.");
                break;
            }
        }
        LOG.debug(String.format("Finished testing %d with and %d without capital asset lines.", documentsWithoutCapitalAssetInformationsTested, documentsWithCapitalAssetInformationsTested));
    }

    /**
     * This test checks getNextCapitalAssetLineNumber function on a document that doesn't have any
     * capitalAssetInformations already. Any number returned should be fine. Currently, the implementation
     * returns -1 in this case, but any other number should be fine, too.
     *
     * @param docnum The document that doesn't have any CapitalAssetInformations.
     */
    protected void testGetNextCapitalAssetLineNumberWithoutCapitalAssetInformations (String financialDocumentNumber) {
        LOG.debug(String.format("Checking document number %s without lines", financialDocumentNumber));
        int newCapitalAssetInformationNumberFromDao = capitalAssetInformationDao.getNextCapitalAssetLineNumber(financialDocumentNumber);
        /*I don't think there can be any validation... the value can't be null because it's a "primitive", and
        I think any value is allowed here as primary key (not just >= 0 for instance).*/
        LOG.debug(String.format("Got value: %d which must be okay because it's an int.", newCapitalAssetInformationNumberFromDao));
    }

    /**
     * This test checks getNextCapitalAssetLineNumber function on a document that already has one or
     * more capitalAssetInformations. The function should be free to return any number except one that
     * is already in use. Currently the implementation finds the biggest capital asset information line
     * number and returns one more than that, but it should be free to do something more clever if that
     * is desired later.
     *
     * @param docnum
     * @param lineNumbers
     */
    protected void testGetNextCapitalAssetLineNumberWithCapitalAssetInformations (String docnum, Iterator<Object[]> capitalAssetInformationLineNumbers) {
        LOG.debug(String.format("Checking document number %s with lines", docnum));
        if (!capitalAssetInformationLineNumbers.hasNext()){
            LOG.error("A document doesn't have capital asset info lines that we really thought should have some. Continuing");
            return;
        }
        int newCapitalAssetInformationNumberFromDao = capitalAssetInformationDao.getNextCapitalAssetLineNumber(docnum.toString());
        while (capitalAssetInformationLineNumbers.hasNext()){
            int currentCapitalAssetInformationNumber = ((BigDecimal)capitalAssetInformationLineNumbers.next()[0]).intValueExact();
            LOG.debug(String.format("Checking %d against %d for uniqueness.", newCapitalAssetInformationNumberFromDao, currentCapitalAssetInformationNumber));
            assertTrue(String.format("%s equals %s. This means that the retrieved line number" +
                " is not unique!.", currentCapitalAssetInformationNumber, newCapitalAssetInformationNumberFromDao),
                (currentCapitalAssetInformationNumber) != newCapitalAssetInformationNumberFromDao);
        }
        LOG.debug(String.format("Looks %d is fine.", newCapitalAssetInformationNumberFromDao));
    }

}

