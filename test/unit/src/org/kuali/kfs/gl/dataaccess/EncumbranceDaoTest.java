package org.kuali.kfs.gl.dataaccess;

import java.util.HashMap;
import java.util.Map;

import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;


/**
 * EncumbranceDao test cases
 */
@ConfigureContext
public class EncumbranceDaoTest extends KualiTestBase {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(EncumbranceDaoTest.class);

    protected EncumbranceDao encumbranceDao;

    @Override
    protected void setUp() throws Exception {
        encumbranceDao = SpringContext.getBean(EncumbranceDao.class);
    }

    public void testGetSummarizedOpenEncumbranceRecordCount_true() {
        Map<String, String> pkMap = new HashMap<String, String>();
        pkMap.put(KFSPropertyConstants.ACCOUNT_NUMBER, "2231459");
        pkMap.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, "2009");

        assertEquals(true, encumbranceDao.hasSummarizedOpenEncumbranceRecords(pkMap, true));
    }

    public void testGetSummarizedOpenEncumbranceRecordCount_false() {
        Map<String, String> pkMap = new HashMap<String, String>();
        pkMap.put(KFSPropertyConstants.ACCOUNT_NUMBER, "2231459");
        pkMap.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, "2009");

        assertEquals(true, encumbranceDao.hasSummarizedOpenEncumbranceRecords(pkMap, false));
    }
}
