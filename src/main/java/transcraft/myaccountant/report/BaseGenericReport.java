/**
 * Created on 11-Sep-2005
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
package transcraft.myaccountant.report;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import transcraft.BookKeeper.service.BOMService;

/**
 * 
 * Base class for a all reports
 * @author david.tran@transcraft.co.uk
 */
public abstract class BaseGenericReport<T> implements JRDataSource, Cloneable {

    public static final String OF_PDF = "pdf"; //$NON-NLS-1$
    public static final String OF_HTML = "html"; //$NON-NLS-1$
    public static final String OF_XML = "xml"; //$NON-NLS-1$

    public static final String DST_SCREEN = "screen"; //$NON-NLS-1$
    public static final String DST_FILE = "file"; //$NON-NLS-1$
    
    private static final String [] OUTPUT_FORMATS = new String [] {
            BaseGenericReport.OF_HTML, BaseGenericReport.OF_PDF, BaseGenericReport.OF_XML
    };

    private static final String [] DESTINATIONS = new String [] {
            BaseGenericReport.DST_SCREEN, BaseGenericReport.DST_FILE
    };
    
    /*
     * need to return one row even when there is no data, or Jasper
     * will not fill in non-row related values
     */
    private transient boolean firstTime = true;
    
    protected String reportTemplate;
    protected String outputFormat = BaseGenericReport.OF_HTML;
    protected String name;
    protected String description;
    protected String destination = BaseGenericReport.DST_SCREEN;
    
    transient protected BOMService bomService;
    transient protected List<T> reportRows = Lists.newArrayList();
    transient protected Object currentRow;
    transient protected Iterator<T> rowIterator;
    
    public BaseGenericReport(String name) {
        this.name = name;
        this.description = name;
    }

    /* (non-Javadoc)
     * @see net.sf.jasperreports.engine.JRDataSource#next()
     */
    @Override
    public boolean next() throws JRException {
        if (this.reportRows.size() == 0) {
            if (this.firstTime) {
                /*
                 * make sure at least one row is returned even when there is
                 * no data, or jasper will not fill in the non-row related
                 * fields
                 */
                this.firstTime = false;
                return true;
            }
            return false;
        }
        boolean more = this.rowIterator.hasNext();
        if (more) {
            this.currentRow = this.rowIterator.next();
        }
        return more;
    }
    
    public BaseGenericReport<T>	makeCopy(String name) {
        try {
			@SuppressWarnings("unchecked")
			BaseGenericReport<T> report = (BaseGenericReport<T>)this.clone();
            report.name = name;
            report.description = name;
            return report;
        } catch (Exception e) {
            throw new RuntimeException("Can not make copy of myself"); //$NON-NLS-1$
        }
    }
    
    public static final String[]	getOutputFormats() {
        return OUTPUT_FORMATS;
    }
    
    public static final String[] getDestinations() {
    	return DESTINATIONS;
    }
    
    /**
     * @return Returns the reportTemplate.
     */
    public String getReportTemplate() {
        return reportTemplate;
    }
    
    /**
     * @param reportTemplate The reportTemplate to set.
     */
    protected void setReportTemplate(String reportTemplate) {
        this.reportTemplate = reportTemplate;
    }
    
    /**
     * @return Returns the outputFormat.
     * this is an enumeration of the output formats
     * @see GenericReport_* enums
     */
    public String getOutputFormat() {
        return outputFormat;
    }
    
    /**
     * @param outputFormat The outputFormat to set.
     * this is an enumeration of the output formats
     * @see GenericReport_* enums
     */
    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }
    
    /**
     * @return Returns the destination.
     * returns the destination to be used for the generated report
     * e.g. to file or to screen. This is an enumeration
     * @see GenericReport.DST_* enums
     */
    public String getDestination() {
        return destination;
    }
    
    /**
     * @param destination The destination to set.
     * returns the destination to be used for the generated report
     * e.g. to file or to screen. This is an enumeration
     * @see GenericReport.DST_* enums
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }
    
    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return this.description != null ? this.description : this.name;
    }
    
    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.name != null ? this.name : Messages.getString("BaseGenericReport.0"); //$NON-NLS-1$
    }
    
    public String dump() {
    	return String.format("name=%s,template=%s,description=%s,output=%s",
    			this.name, this.reportTemplate, this.description, this.outputFormat);
    }
    
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * @return Returns the bomService.
     */
    public BOMService getBomService() {
        return bomService;
    }
    
    /**
     * @param bomService The bomService to set.
     */
    protected void setBomService(BOMService bomService) {
        this.bomService = bomService;
        this.firstTime = true;
        this.rowIterator = null;
        this.reportRows = Lists.newArrayList();
    }
    
    /* (non-Javadoc)
     * @see net.sf.jasperreports.engine.JRDataSource#getFieldValue(net.sf.jasperreports.engine.JRField)
     */
    public abstract Object getFieldValue(JRField field) throws JRException;
    
    public abstract void run();
    public abstract Map<String, Object> getParameters() throws JRException;
    
    public void	run(BOMService bomService) {
        this.setBomService(bomService);
        this.run();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object arg0) {
        try {
            return this.name.equals(((BaseGenericReport<?>)arg0).name);
        } catch (Exception e) {}
        return false;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        try {
            return this.name.hashCode();
        } catch (Exception e) {}
        return super.hashCode();
    }    
}
