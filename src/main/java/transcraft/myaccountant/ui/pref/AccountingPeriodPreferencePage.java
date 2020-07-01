/**
 * Created on 09-Oct-2005
 *
 * Copyrights (c) Transcraft Trading Limited 2003-2005. All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose, without fee, and without a written
 * agreement, is hereby granted, provided that the above copyright notice, 
 * this paragraph and the following two paragraphs appear in all copies, 
 * modifications, and distributions.
 * 
 * IN NO EVENT SHALL WE BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * WE HAVE BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * WE SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE. THE SOFTWARE AND ACCOMPANYING DOCUMENTATION, IF
 * ANY, PROVIDED HEREUNDER IS PROVIDED "AS IS". WE HAVE NO OBLIGATION
 * TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS.
 *
 * Unless otherwise specified below by individual copyright and usage information,
 * source code on this page is covered by the above Copyrights Notice.
 * 
 */
package transcraft.myaccountant.ui.pref;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * manages preferences for accounting period
 * 
 * @author david.tran@transcraft.co.uk
 */
public class AccountingPeriodPreferencePage extends BasePreferencePage {

    private String [][] months = 
        new String[][] {
            {Messages.getString("AccountingPeriodPreferencePage.0"), "1"}, //$NON-NLS-1$ //$NON-NLS-2$
            {Messages.getString("AccountingPeriodPreferencePage.2"), "2"}, //$NON-NLS-1$ //$NON-NLS-2$
            {Messages.getString("AccountingPeriodPreferencePage.4"), "3"}, //$NON-NLS-1$ //$NON-NLS-2$
            {Messages.getString("AccountingPeriodPreferencePage.6"), "4"}, //$NON-NLS-1$ //$NON-NLS-2$
            {Messages.getString("AccountingPeriodPreferencePage.8"), "5"}, //$NON-NLS-1$ //$NON-NLS-2$
            {Messages.getString("AccountingPeriodPreferencePage.10"), "6"}, //$NON-NLS-1$ //$NON-NLS-2$
            {Messages.getString("AccountingPeriodPreferencePage.12"), "7"}, //$NON-NLS-1$ //$NON-NLS-2$
            {Messages.getString("AccountingPeriodPreferencePage.14"), "8"}, //$NON-NLS-1$ //$NON-NLS-2$
            {Messages.getString("AccountingPeriodPreferencePage.16"), "9"}, //$NON-NLS-1$ //$NON-NLS-2$
            {Messages.getString("AccountingPeriodPreferencePage.18"), "10"}, //$NON-NLS-1$ //$NON-NLS-2$
            {Messages.getString("AccountingPeriodPreferencePage.20"), "11"}, //$NON-NLS-1$ //$NON-NLS-2$
            {Messages.getString("AccountingPeriodPreferencePage.22"), "12"}, //$NON-NLS-1$ //$NON-NLS-2$
    	};
    /**
     *
     */
    public AccountingPeriodPreferencePage() {
        super(Messages.getString("AccountingPeriodPreferencePage.24"), ImageDescriptor.createFromFile(AccountingPeriodPreferencePage.class, //$NON-NLS-1$
        "/images/clock.gif"), FieldEditorPreferencePage.GRID); //$NON-NLS-1$
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    protected void createFieldEditors() {
        ComboFieldEditor finYearEditor = new ComboFieldEditor(PreferenceFactory.FINYEAR_START, Messages.getString("AccountingPeriodPreferencePage.26"), //$NON-NLS-1$
                months,
                this.getFieldEditorParent());
        this.addField(finYearEditor);
        ComboFieldEditor vatYearEditor = new ComboFieldEditor(PreferenceFactory.VATYEAR_START, Messages.getString("AccountingPeriodPreferencePage.27"), //$NON-NLS-1$
                months,
                this.getFieldEditorParent());
        this.addField(vatYearEditor);
        ComboFieldEditor companyYearEditor = new ComboFieldEditor(PreferenceFactory.COMPANYYEAR_START, Messages.getString("AccountingPeriodPreferencePage.28"), //$NON-NLS-1$
                months,
                this.getFieldEditorParent());
        this.addField(companyYearEditor);
    }
}
