/*
 * Copyright 2007 The Kuali Foundation
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

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.kuali.kfs.fp.batch.ProcurementCardInputFileType;
import org.kuali.kfs.fp.batch.ProcurementCardLoadStep;
import org.kuali.kfs.fp.businessobject.ProcurementCardTransaction;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.krad.service.BusinessObjectService;

/**
 * Tests the PcdoLoadStep. DEPENDENCIES: Procurement card xml file transaction1.xml must be in /opt/kuali/dev/staging/PCDO/ this
 * file can be obtained by running the project's ant dist-local, or copying from build/externalConfigDirectory/static/staging/PCDO/
 */
@ConfigureContext
public class PcdoLoadStepTest extends KualiTestBase {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PcdoLoadStepTest.class);
    private static final String DATA_FILE_PATH = "test/unit/src/org/kuali/kfs/fp/batch/fixture/transactionsUnit.xml";
    private String batchDirectory;

    public PcdoLoadStepTest() {
        super();
    }

    /**
     * Creats .done file for test input file.
     *
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        //make sure we have a batch directory
        batchDirectory =  SpringContext.getBean(ProcurementCardInputFileType.class).getDirectoryPath();
        File batchDirectoryFile = new File(batchDirectory);
        batchDirectoryFile.mkdir();

        //copy the data file into place
        File dataFileSrc = new File(DATA_FILE_PATH);
        File dataFileDest = new File(batchDirectory + "/transactionsUnit.xml");
        FileUtils.copyFile(dataFileSrc, dataFileDest);

        //create .done file
        String doneFileName = batchDirectory + "/transactionsUnit.done";
        File doneFile = new File(doneFileName);
        if (!doneFile.exists()) {
            LOG.info("Creating done file: " + doneFile.getAbsolutePath());
            doneFile.createNewFile();
        }
    }

    /**
     * Removes the directory for the batch files to go in
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    public void tearDown() throws Exception {
        /*File batchDirectoryFile = new File(batchDirectory);
        for (File f : batchDirectoryFile.listFiles()) {
            f.delete();
        }
        batchDirectoryFile.delete();*/
    }

    /**
     * Tests the whole step completes successfully.
     */
    public void testAll() throws Exception {
        assertTrue("hold until figure out staging dir!", true);

         ProcurementCardLoadStep pcdoLoadStep = SpringContext.getBean(ProcurementCardLoadStep.class);
         ProcurementCardInputFileType pcdoFileType = SpringContext.getBean(ProcurementCardInputFileType.class);
         pcdoFileType.setSchemaLocation("http://testdrive.kfs.kuali.org/kfs-ptd/static/xsd/fp/procurementCard.xsd");
         //pcdoFileType.setSchemaLocation("file:///" + System.getProperty("user.dir")+ "/build/project/xsd/fp/procurementCard.xsd");

         pcdoLoadStep.setBatchInputFileType(pcdoFileType);

         boolean goodExit = pcdoLoadStep.execute("test", null);

         assertTrue("pcdo load step did not exit with pass", goodExit);

         Collection<ProcurementCardTransaction> loadedTransactions =
                 SpringContext.getBean(BusinessObjectService.class).findAll(ProcurementCardTransaction.class);
         assertNotNull("no transactions loaded ", loadedTransactions);
         assertEquals("incorrect number of transactions loaded ",20,loadedTransactions.size());
    }
}
