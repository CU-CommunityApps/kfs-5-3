/*
 * Copyright 2007 The Kuali Foundation.
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

 function loadTransactionRestrictionCodeDesc(transactionRestrictionCodeFieldName){
	var elPrefix = findElPrefix( transactionRestrictionCodeFieldName.name );
	var transactionRestrictionCodeDescFieldName = elPrefix + ".transactionRestriction.name";
 	setTransactionRestrictionCodeDesc(transactionRestrictionCodeFieldName, transactionRestrictionCodeDescFieldName);
 }
 
 function setTransactionRestrictionCodeDesc( transactionRestrictionCodeFieldName, transactionRestrictionCodeDescFieldName ){
 
	var transactionRestrictionCode = dwr.util.getValue( transactionRestrictionCodeFieldName );

	if (transactionRestrictionCode =='') {
		clearRecipients(transactionRestrictionCodeDescFieldName, "");
	} else {
		transactionRestrictionCode = transactionRestrictionCode.toUpperCase();
		var dwrReply = {
			callback:function(data) {
			if ( data != null && typeof data == 'object' ) {
				setRecipientValue( transactionRestrictionCodeDescFieldName, data.name );
			} else {
				setRecipientValue( transactionRestrictionCodeDescFieldName, wrapError( "Transaction Restriction Code not found" ), true );			
			} },
			errorHandler:function( errorMessage ) { 
				setRecipientValue( purposeCodeDescFieldName, wrapError( "purpose code not found" ), true );
			}
		};
		TransactionRestrictionCodeService.getByPrimaryKey(transactionRestrictionCode, dwrReply );
	}
}