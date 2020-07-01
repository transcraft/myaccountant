/**
 * Created on 21-Oct-2005
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
package transcraft.myaccountant.meta;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import transcraft.BookKeeper.bom.TaxBandEntry;
import transcraft.myaccountant.utils.Formatters;

/**
 * tax bands meta provider
 * 
 * @author david.tran@transcraft.co.uk
 */
public class TaxBandMetaProvider extends BaseMetaProvider<TaxBandEntry> {

    public static final String CODE_PROP = "code"; //$NON-NLS-1$
    public static final String DESC_PROP = "description"; //$NON-NLS-1$
    public static final String PCT_PROP = "percentage"; //$NON-NLS-1$
    public static final String DEFBAND_PROP = "defaultBand"; //$NON-NLS-1$
    
    private static final TaxBandEntry [] defaultTaxBands = new TaxBandEntry[] {
            new TaxBandEntry("E", Messages.getString("TaxBandMetaProvider.5"), 0, false), //$NON-NLS-1$ //$NON-NLS-2$
            new TaxBandEntry("S", Messages.getString("TaxBandMetaProvider.7"), .175, true), //$NON-NLS-1$ //$NON-NLS-2$
            new TaxBandEntry("Z", Messages.getString("TaxBandMetaProvider.9"), 0, false), //$NON-NLS-1$ //$NON-NLS-2$
            new TaxBandEntry("1", Messages.getString("TaxBandMetaProvider.11"), .075, false), //$NON-NLS-1$ //$NON-NLS-2$
    };
    
    public static final List<TaxBandEntry>	getDefaultTaxBands() {
        return Arrays.asList(defaultTaxBands);
    }

    public TaxBandMetaProvider() {
        this.columns = new MetaColumn[] {
                new MetaColumn<String>(CODE_PROP, Messages.getString("TaxBandMetaProvider.12"), String.class, 50), //$NON-NLS-1$
                new MetaColumn<String>(DESC_PROP, Messages.getString("TaxBandMetaProvider.13"), String.class, 200), //$NON-NLS-1$
                new MetaColumn<Double>(PCT_PROP, Messages.getString("TaxBandMetaProvider.14"), Double.class, 200), //$NON-NLS-1$
                new MetaColumn<Boolean>(DEFBAND_PROP, Messages.getString("TaxBandMetaProvider.15"), Boolean.class, 30, Arrays.asList(new String[] { Messages.getString("TaxBandMetaProvider.16"), Messages.getString("TaxBandMetaProvider.17")})), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        };
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.meta.MetaProvider#getPrimaryKey()
     */
    @Override
    public String getPrimaryKey() {
        return "description"; //$NON-NLS-1$
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.meta.MetaProvider#getValue(java.lang.Object, java.lang.String, java.lang.String)
     */
    public Object getValue(TaxBandEntry entry, String columnName, String selector) {
        if (CODE_PROP.equals(columnName)) {
            return Formatters.emptyIfNull(entry.getCode());
        } else if (DESC_PROP.equals(columnName)) {
            return Formatters.emptyIfNull(entry.getDescription());
        } else if (PCT_PROP.equals(columnName)) {
            return Formatters.format(entry.getRate(), 4);
        } else if (DEFBAND_PROP.equals(columnName)) {
            return entry.isDefaultBand() ? Messages.getString("TaxBandMetaProvider.19") : Messages.getString("TaxBandMetaProvider.20"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return null;
    }
    /* (non-Javadoc)
     * @see transcraft.myaccountant.meta.MetaProvider#setValue(java.lang.Object, java.lang.String, java.lang.String, java.lang.Object)
     */
    public void setValue(TaxBandEntry entry, String selector, String columnName,
            Object value) throws ParseException {
        if (CODE_PROP.equals(columnName)) {
            entry.setCode(value.toString());
        } else if (DESC_PROP.equals(columnName)) {
            entry.setDescription(value.toString());
        } else if (PCT_PROP.equals(columnName)) {
            entry.setRate(Double.parseDouble(value.toString()));
        } else if (DEFBAND_PROP.equals(columnName)) {
            entry.setDefaultBand(value.toString().equalsIgnoreCase(Messages.getString("TaxBandMetaProvider.4"))); //$NON-NLS-1$
        }
    }
    /**
     * helper method for supporting PreferencePage
     * @return
     */
    public String	getDefaultTaxBandsAsPref() {
        return this.storeToPreference(defaultTaxBands);
    }
}