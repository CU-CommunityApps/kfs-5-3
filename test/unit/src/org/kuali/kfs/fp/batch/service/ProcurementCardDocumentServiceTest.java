/*
 * Copyright 2006 The Kuali Foundation
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
package org.kuali.kfs.fp.batch.service;

import static org.kuali.kfs.sys.fixture.UserNameFixture.kfs;

import org.kuali.kfs.fp.businessobject.ProcurementCardTransaction;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.krad.service.BusinessObjectService;

/**
 * This class tests the services used to create ProcurementCard documents.
 */
@ConfigureContext(session = kfs)
public class ProcurementCardDocumentServiceTest extends KualiTestBase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        ProcurementCardTransaction transaction = new ProcurementCardTransaction();
        transaction.setTransactionCreditCardNumber("999911112222333");
        transaction.setFinancialDocumentTotalAmount("11.55");
        transaction.setTransactionDebitCreditCode("D");
        transaction.setChartOfAccountsCode("BL");
        transaction.setAccountNumber("4631654");
        transaction.setFinancialObjectCode("4190");
        transaction.setTransactionCycleStartDate("2006-02-20");
        transaction.setTransactionCycleEndDate("2006-02-20");
        transaction.setCardHolderName("Molecular Structure Center");
        transaction.setTransactionDate(java.sql.Date.valueOf("2006-02-17"));
        transaction.setTransactionReferenceNumber("554173460487304813890");
        transaction.setTransactionMerchantCategoryCode("7622");
        transaction.setTransactionPostingDate(java.sql.Date.valueOf("2006-02-17"));
        transaction.setVendorName("STANSIFER RADIO CO");
        SpringContext.getBean(BusinessObjectService.class).save(transaction);

    }

    public void testCreatePCardDocuments() throws Exception {
        boolean documentsCreated = SpringContext.getBean(ProcurementCardCreateDocumentService.class).createProcurementCardDocuments();
        assertTrue("problem creating documents", documentsCreated);
    }

    public void testRoutePCardDocuments() throws Exception {
        boolean routeSuccessful = SpringContext.getBean(ProcurementCardCreateDocumentService.class).routeProcurementCardDocuments();
    }

}

