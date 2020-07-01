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
package transcraft.myaccountant.meta;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * base class for all meta providers
 * @author david.tran@transcraft.co.uk
 */
public abstract class BaseMetaProvider<T> implements MetaProvider<T> {
	private static final Logger LOG = LoggerFactory.getLogger(BaseMetaProvider.class);
	
    protected MetaColumn<?> [] columns = new MetaColumn<?>[0];
    
    /**
     * 
     */
    public BaseMetaProvider() {
    }

    /* (non-Javadoc)
     * @see transcraft.myaccountant.meta.MetaProvider#getColumns()
     */
    @Override
    public MetaColumn<?>[] getColumns() {
        return this.columns;
    }

    /* (non-Javadoc)
     * @see transcraft.myaccountant.meta.MetaProvider#getColumn(java.lang.String)
     */
    @Override
    public MetaColumn<?> getColumn(String name) {
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].getName().equals(name)) {
                return columns[i];
            }
        }
        return null;
    }

    @Override
    public String []	getColumnTitles() {
        List<String> list = Lists.newArrayList();
        for (int i = 0; i < this.columns.length; i++) {
            list.add(this.columns[i].getTitle());
        }
        // Java 8 compatibility mode, so String[]::new is not available
        return list.toArray(new String[list.size()]);
    }

    @Override
    public MetaColumn<?>	getColumnForTitle(String title) {
        for (int i = 0; i < this.columns.length; i++) {
            if (this.columns[i].getTitle().equals(title)) {
                return this.columns[i];
            }
        }
        return this.columns[0];
    }
    
    protected void	extendColumns(int count) {
        MetaColumn<?> [] newColumns = new MetaColumn[this.columns.length + count];
        System.arraycopy(this.columns, 0, newColumns, 0, this.columns.length);
    }    
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.meta.MetaProvider#load()
     */
    @SuppressWarnings("unchecked")
	@Override
    public T [] loadFromPreference(Class<T> prototype, String data) {
        if (isBlank(data)) {
            return (T[])new Object[0];
        }
        if (data.charAt(0) == '[')  {
            data = data.substring(1);
        }
        if (data.charAt(data.length() - 1) == ']') {
            data = data.substring(0, data.length() - 1);
        }
        List<Object> list = Lists.newArrayList();
        String [] rows = data.split("\\},\\s?\\{"); //$NON-NLS-1$
        for (int i = 0; i < rows.length; i++) {
            T obj = null;
            try {
                obj = prototype.getConstructor().newInstance();                
	            String row = rows[i].trim();
	            if (row.charAt(0) == '{')  {
	                row = row.substring(1);
	            }
	            if (row.charAt(row.length() - 1) == '}') {
	                row = row.substring(0, row.length() - 1);
	            }
	            String [] kvs = row.split(","); //$NON-NLS-1$
	            for (int j = 0; j < kvs.length; j++) {
	                String [] kv = kvs[j].split("="); //$NON-NLS-1$
	                this.setValue(obj, null, kv[0].trim(), kv[1].trim());
	            }
	            list.add(obj);
            } catch (Exception e) {
                LOG.error(String.format("load(%s,%s)", prototype, data), e);
            }
        }
        return list.toArray((T[])new Object[list.size()]);
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.meta.MetaProvider#store()
     */
    @Override
    public String storeToPreference(T [] values) {
        List<Object> list = Lists.newArrayList();
        for (int i = 0; i < values.length; i++) {
            Map<String, Object> map = Maps.newHashMap();
            for (int j = 0; j < this.columns.length; j++) {
                String k = this.columns[j].getName();
                Object v = this.getValue(values[i], k, null);
                if (k.length() > 0) {
                    map.put(k, v);
                }
            }
            list.add(map);
        }
        return list.toString();
    }    
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.meta.MetaProvider#getValue(java.lang.Object, java.lang.String, java.lang.String)
     */
    @Override
    public abstract Object getValue(T data, String columnName, String selector);

    /* (non-Javadoc)
     * @see transcraft.myaccountant.meta.MetaProvider#setValue(java.lang.Object, java.lang.String, java.lang.String, java.lang.Object)
     */
    @Override
    public abstract void setValue(T data, String selector, String columnName,
            Object value) throws ParseException;

    /* (non-Javadoc)
     * @see transcraft.myaccountant.meta.MetaProvider#getPrimaryKey()
     */
    @Override
    public abstract String getPrimaryKey();

}
