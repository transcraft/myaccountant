/**
 * Created on 22-Oct-2005
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

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import transcraft.BookKeeper.bom.TaxBandEntry;
import transcraft.myaccountant.meta.TaxBandMetaProvider;

/**
 * convenience methods for tax bands
 * @author david.tran@transcraft.co.uk
 */
public class TaxBandHelper {

    /**
     * 
     */
    public TaxBandHelper() {
    }

    /**
     * returns the TaxBandEntry objects for a given tax codes
     * 
     * @param code
     * @return
     */
    public TaxBandEntry	[] getTaxBandsForCodes(String [] codes) {
        List<TaxBandEntry> list = Lists.newArrayList();
        List<String> codesList = Arrays.asList(codes);
        String taxBandPref = PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.TAX_BANDS);
        Object [] entries = new TaxBandMetaProvider().loadFromPreference(TaxBandEntry.class, taxBandPref);
        for (int i = 0; i < entries.length; i++) {
            TaxBandEntry entry = (TaxBandEntry)entries[i];
            if (codesList.contains(entry.getCode())) {
                list.add(entry);
            }
        }
        // Java 8 compatibility mode, so TaxBandEntry[]::new is not available
        return list.toArray(new TaxBandEntry[list.size()]);
    }
    
    /**
     * return the default tax band
     * @return
     */
    public TaxBandEntry	getDefaultBand() {
        String taxBandPref = PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.TAX_BANDS);
        Object [] entries = new TaxBandMetaProvider().loadFromPreference(TaxBandEntry.class, taxBandPref);
        for (int i = 0; i < entries.length; i++) {
            TaxBandEntry entry = (TaxBandEntry)entries[i];
            if (entry.isDefaultBand()) {
                return entry;
            }
        }
        return entries.length > 0 ? (TaxBandEntry)entries[0] : (TaxBandEntry)null;
    }
    
    /**
     * returns the TaxBandEntry object for a particular tax code
     * 
     * @param code
     * @return
     */
    public TaxBandEntry	getTaxBandForCode(String code) {
        if (isBlank(code)) {
            return null;
        }
        TaxBandEntry [] entries = this.getTaxBandsForCodes(new String [] { code });
        return entries.length > 0 ? entries[0] : null;
    }
    
    /**
     * return the list of configured tax codes
     * 
     * @return
     */
    public String []	getTaxCodes() {
        String taxBandPref = PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.TAX_BANDS);
        Object [] entries = new TaxBandMetaProvider().loadFromPreference(TaxBandEntry.class, taxBandPref);
        List<String> list = Lists.newArrayList();
        for (int i = 0; i < entries.length; i++) {
            TaxBandEntry entry = (TaxBandEntry)entries[i];
            list.add(entry.getCode());
        }
        // Java 8 compatibility mode, so String[]::new is not available
        return list.toArray(new String[list.size()]);
    }
    /**
     * calculates the tax amount for this tax band
     * @param amount
     * @param code
     * @return
     */
    public double	calculateTax(double amount, String [] codes) {
        TaxBandEntry [] entries = this.getTaxBandsForCodes(codes);
        return calculateTax(amount, entries);
    }
    /**
     * calculates the tax amount for this tax band
     * @param amount
     * @param entry
     * @return
     */
    public double	calculateTax(double amount, TaxBandEntry [] entries) {
        double tax = 0;
        for (int i = 0; i < entries.length; i++) {
            tax += Math.abs(amount * entries[i].getRate());
        }
        return tax * (amount < 0 ? -1 : 1);
    }
    /**
     * calculates the gross amount for this tax band
     * @param amount
     * @param code
     * @return
     */
    public double	calculateGross(double amount, String [] codes) {
        TaxBandEntry [] entries = this.getTaxBandsForCodes(codes);
        return calculateGross(amount, entries);
    }
    /**
     * calculates the gross amount for this tax band
     * @param amount
     * @param entry
     * @return
     */
    public double	calculateGross(double amount, TaxBandEntry [] entries) {
        return amount + this.calculateTax(amount, entries);
    }
    /**
     * calculates the gross amount for this tax band
     * @param amount
     * @param code
     * @return
     */
    public double	calculateNet(double amount, String [] codes) {
        TaxBandEntry [] entries = this.getTaxBandsForCodes(codes);
        return calculateNet(amount, entries);
    }
    /**
     * calculates the gross amount for this tax band
     * @param amount
     * @param entry
     * @return
     */
    public double	calculateNet(double amount, TaxBandEntry [] entries) {
        return amount - this.calculateTax(amount, entries);
    }
}
