/*
 * Copyright 2008 The Kuali Foundation
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
package org.kuali.kfs.module.cab.batch.dataaccess;

import java.util.Collection;
import java.util.List;

import org.kuali.kfs.gl.businessobject.Entry;
import org.kuali.kfs.module.cab.businessobject.BatchParameters;
import org.kuali.kfs.module.purap.businessobject.CreditMemoAccountRevision;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestAccountRevision;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderAccount;

public interface ExtractDao {
    /**
     * This method implementation should find all GL Entries matching the parameters listed in the batch parameters
     *
     * @param batchParameters Batch Parameters
     * @return GL Entries
     */
    Collection<Entry> findMatchingGLEntries(BatchParameters batchParameters);


    /**
     * This method implementation should find valid Credit Memo account line changes as per the batch parameters
     *
     * @param batchParameters Batch Parameters
     * @return List of Credit memo account history records
     */
    Collection<CreditMemoAccountRevision> findCreditMemoAccountRevisions(BatchParameters batchParameters);

    /**
     * This method implementation should find valid Payment Request account line changes as per the batch parameters
     *
     * @param batchParameters Batch Parameters
     * @return List of Payment Request account history records
     */
    Collection<PaymentRequestAccountRevision> findPaymentRequestAccountRevisions(BatchParameters batchParameters);

    /**
     * @deprecated This method should not be used as it is not working properly after moving getDocumentsNumbersAwaitingPurchaseOrderOpenStatus
     *             method to the service layer.
     */
    @Deprecated
    Collection<PurchaseOrderAccount> findPreTaggablePOAccounts(BatchParameters batchParameters);

    /**
     * This method implementation should retrieve all eligible pretaggable PO account lines from Purchasing module
     *
     * @param batchParameters Batch Parameters
     * @param docNumbersAwaitingPurchaseOrderStatus Document numbers awaiting PO Open status
     * @return List of pre-taggable purchase order account lines
     */
    Collection<PurchaseOrderAccount> findPreTaggablePOAccounts(BatchParameters batchParameters, List<String> docNumbersAwaitingPurchaseOrderStatus);

}
