/*
 * Copyright 2014 The Kuali Foundation.
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.module.purap.batch;

import static org.kuali.kfs.sys.fixture.UserNameFixture.appleton;
import static org.kuali.kfs.sys.fixture.UserNameFixture.ferland;
import static org.kuali.kfs.sys.fixture.UserNameFixture.khuntley;
import static org.kuali.kfs.sys.fixture.UserNameFixture.parke;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.document.ContractManagerAssignmentDocumentTest;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.PaymentRequestDocumentTest;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.fixture.PaymentRequestDocumentFixture;
import org.kuali.kfs.module.purap.service.PdpExtractService;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocumentTestUtils;
import org.kuali.kfs.sys.document.workflow.WorkflowTestUtils;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.DocumentService;

/**
 * This class tests the services used to create Pdp Extract .
 */
@ConfigureContext(session = khuntley)
public class PdpExtractServiceTest extends KualiTestBase {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PdpExtractServiceTest.class);
    private PdpExtractService pdpExtractService;
    private DocumentService documentService;
    private BusinessObjectService businessObjectService;
    private static final String SUB_ACCOUNT_REVIEW = "SubAccount";
    private static final String ACCOUNT_REVIEW = "Account";
    private static final String ORG_REVIEW = "AccountingOrganizationHierarchy";
    private static final String PAYEE_OWNER_CD = "CP";
    /**
     * Sets up the test by initializing several properties
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        pdpExtractService = SpringContext.getBean(PdpExtractService.class);
        documentService = SpringContext.getBean(DocumentService.class);
        businessObjectService = SpringContext.getBean(BusinessObjectService.class);

    }

    public void testExtractImmediatePaymentsOnly()throws Exception {
        PaymentRequestDocument preq = getPaymentRequestDocument();
        pdpExtractService.extractImmediatePaymentsOnly();
        Collection<PaymentGroup> paymentGroups = businessObjectService.findAll(PaymentGroup.class);
        for (PaymentGroup group : paymentGroups) {
            assertTrue("Payee Owner code should be CP.", StringUtils.equals(PAYEE_OWNER_CD, group.getPayeeOwnerCd()));
        }
    }



    @ConfigureContext(session = parke, shouldCommitTransactions = true)
    public  PaymentRequestDocument getPaymentRequestDocument() throws Exception {
        // 1. use the ACM document to create the REQ and PO
        ContractManagerAssignmentDocumentTest acmDocTest = new ContractManagerAssignmentDocumentTest();
        String reqNumber = acmDocTest.testRouteDocument2();
        RequisitionDocument reqDoc = (RequisitionDocument) documentService.getByDocumentHeaderId(reqNumber);
        String poNumber = reqDoc.getRelatedViews().getRelatedPurchaseOrderViews().get(0).getDocumentNumber();
        PurchaseOrderDocument poDoc = (PurchaseOrderDocument) documentService.getByDocumentHeaderId(poNumber);
        poDoc.setReceivingDocumentRequiredIndicator(false);
        // approve the PO
        poDoc.setPurchaseOrderVendorChoiceCode("LPRC");
        // submit then approve the PO
        documentService.routeDocument(poDoc, "Test routing as parke", null);

        poDoc = (PurchaseOrderDocument) documentService.getByDocumentHeaderId(poNumber);

        // 3 use the PO number to create a Payment Request and have it go final
        PaymentRequestDocument preqDoc = routePREQDocumentToFinal(poDoc);
        LOG.info("Requisition document: " + reqDoc.getDocumentNumber());
        LOG.info("PO document: " + poDoc.getDocumentNumber());
        LOG.info("PREQ document: " + preqDoc.getDocumentNumber());


        return preqDoc;
    }



    @ConfigureContext(session = appleton, shouldCommitTransactions=true)
    public final PaymentRequestDocument routePREQDocumentToFinal(PurchaseOrderDocument POdoc) throws Exception {
        PaymentRequestDocumentTest preqDocTest = new PaymentRequestDocumentTest();
        PaymentRequestDocument paymentRequestDocument = preqDocTest.createPaymentRequestDocument(PaymentRequestDocumentFixture.PREQ_APPROVAL_REQUIRED,
                POdoc, true, new KualiDecimal[] {new KualiDecimal(100)});
        paymentRequestDocument.setImmediatePaymentIndicator(true);

        final String docId = paymentRequestDocument.getDocumentNumber();
        LOG.info("Vendor::: " + paymentRequestDocument.getVendorHeaderGeneratedIdentifier());
        AccountingDocumentTestUtils.routeDocument(paymentRequestDocument, documentService);
        WorkflowTestUtils.waitForNodeChange(paymentRequestDocument.getDocumentHeader().getWorkflowDocument(), ACCOUNT_REVIEW);
        changeCurrentUser(ferland);
        paymentRequestDocument = (PaymentRequestDocument) documentService.getByDocumentHeaderId(docId);
        assertTrue("At incorrect node.", WorkflowTestUtils.isAtNode(paymentRequestDocument,
                ACCOUNT_REVIEW));
        assertTrue("Document should be enroute.", paymentRequestDocument.getDocumentHeader().getWorkflowDocument().isEnroute());
        assertTrue("ferland should have an approve request.", paymentRequestDocument.getDocumentHeader().getWorkflowDocument().isApprovalRequested());
        documentService.approveDocument(paymentRequestDocument, "Test approving as ferland", null);

        WorkflowTestUtils.waitForDocumentApproval(paymentRequestDocument.getDocumentNumber());

        paymentRequestDocument = (PaymentRequestDocument) documentService.getByDocumentHeaderId(docId);
        assertTrue("Document should now be final.", paymentRequestDocument.getDocumentHeader().getWorkflowDocument().isFinal());
        return paymentRequestDocument;
    }

    private UserNameFixture getInitialUserName() {
        return khuntley;
    }

    protected UserNameFixture getTestUserName() {
        return khuntley;
    }





}
