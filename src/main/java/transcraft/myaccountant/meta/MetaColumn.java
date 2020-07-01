/**
 * Created on 14-Jul-2005
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

import java.util.List;
import java.util.Optional;

import org.eclipse.swt.SWT;

public class MetaColumn<T> {
	// immutable values
    private final String name;
    private final String title;
    private final Class<T> prototype;
    
    //optional values
    private Optional<List<String>> list = Optional.empty();
    private Optional<String[]> array = Optional.empty();
    
    // mutable values
    private int alignment = SWT.LEFT;
    private int width = 300;
    private boolean enabled = true;
    private boolean visible = true;
    private int style = SWT.NONE;
    private String image;
    private boolean sortable = false;
    
    public MetaColumn(String name, String title, Class<T> prototype) {
        this.name = name;
        this.title = title;
        this.prototype = prototype;
    }
    
    public MetaColumn(String name, String title, Class<T> prototype, int width) {
        this(name, title, prototype);
        this.width = width;
    }
    
    public MetaColumn(String name, String title, Class<T> prototype, int width, boolean enabled, boolean visible) {
        this(name, title, prototype, width);
        this.enabled = enabled;
        this.visible = visible;
    }
    
    public MetaColumn(String name, String title, Class<T> prototype, int width, List<String> list) {
        this(name, title, prototype, width);
        this.list = Optional.of(list);
        this.style = SWT.READ_ONLY;
    }
    
    public MetaColumn(String name, String title, Class<T> prototype, int width, List<String> list, boolean enabled) {
        this(name, title, prototype, width, list);
        this.enabled = enabled;
    }
    
    public MetaColumn(String name, String title, Class<T> prototype, int width, List<String> list, boolean enabled, int style) {
        this(name, title, prototype, width, list, enabled);
        this.style = style;
    }
    
    public MetaColumn(String name, String title, Class<T> prototype, int width, int alignment) {
        this(name, title, prototype, width);
        this.alignment = alignment;
    }
    
    public MetaColumn(String name, String title, Class<T> prototype, boolean enabled, int height, int style) {
        this(name, title, prototype, height);
        this.enabled = enabled;
        this.style = style;
    }
    
    public MetaColumn(String name, String title, Class<T> prototype, int width, int alignment, boolean enabled) {
        this(name, title, prototype, width, alignment);
        this.enabled = enabled;
    }
    
    public MetaColumn(String name, String title, Class<T> prototype, boolean enabled) {
        this(name, title, prototype);
        this.enabled = enabled;
    }
    
    public MetaColumn(String name, String title, Class<T> prototype, boolean enabled, boolean visible) {
        this(name, title, prototype, enabled);
        this.visible = visible;
    }
    
    public MetaColumn(String name, String title, Class<T> prototype, List<String> list) {
        this(name, title, prototype);
        this.list = Optional.of(list);
        this.style = SWT.READ_ONLY;
    }
    
    public MetaColumn(String name, String title, Class<T> prototype, List<String> list, boolean enabled) {
        this(name, title, prototype, list);
        this.enabled = enabled;
    }
    
    public MetaColumn(String name, String title, Class<T> prototype, List<String> list, boolean enabled, int style) {
        this(name, title, prototype, list, enabled);
        this.style = style;
    }
    
    public MetaColumn(String name, String title, Class<T> prototype, int width, String[] arr) {
        this(name, title, prototype, width);
        this.array = Optional.of(arr);
        this.style = SWT.READ_ONLY;
    }

    public MetaColumn(String name, String title, Class<T> prototype, int width, String[] arr, boolean enabled) {
        this(name, title, prototype, width, arr);
        this.enabled = enabled;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * @return Returns the image.
     */
    public String getImage() {
        return image;
    }
    
    /**
     * @param image The image to set.
     */
    public void setImage(String image) {
        this.image = image;
    }
    
    /**
     * @return Returns the prototype.
     */
    public Class<T> getPrototype() {
        return prototype;
    }
    
    /**
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * @return Returns the style.
     */
    public int getStyle() {
        return style;
    }
    
    /**
     * @return Returns the visible.
     */
    public boolean isVisible() {
        return visible;
    }
    
    /**
     * @param visible The visible to set.
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    /**
     * @return Returns the list.
     */
    public Optional<List<String>> getList() {
        return list;
    }
    
    public String [] getListAsArray() {
        // Java 8 compatibility mode, so String[]::new is not available
    	return this.list.map(l -> l.toArray(new String[l.size()])).orElse(new String[0]);
    }
    
    /**
     * @return Returns the sortable.
     */
    public boolean isSortable() {
        return sortable;
    }
    
    /**
     * @param sortable The sortable to set.
     */
    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }
    
    /**
     * @param list The list to set.
     */
    public void setList(List<String> list) {
        this.list = Optional.of(list);
    }
    
    /**
     * convenience method for use in ComboBoxCellEditor which expects the
     * index of the selection instead of the value itself
     * 
     * @param value
     * @return
     */
    public int	getListSelectionIndex(Object value) {
    	if (list.isPresent()) {
            int idx = this.list.get().indexOf(value);
            return idx >= 0 ? idx : 0;
        }
        return 0;
    }
    
    public Optional<String[]> getArray() {
		return array;
	}

	/**
     * @return Returns the alignment.
     */
    public int getAlignment() {
        return alignment;
    }
    
    /**
     * @param alignment The alignment to set.
     */
    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }
    
    /**
     * @return Returns the width.
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * @param width The width to set.
     */
    public void setWidth(int width) {
        this.width = width;
    }
    
    /**
     * @return Returns the enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * @param enabled The enabled to set.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isMultiSelect() {
        return (this.getStyle() & SWT.MULTI) != 0;
    }
    public boolean isRadioGroup() {
        return (this.getStyle() & SWT.RADIO) != 0;
    }
    public boolean isCombo() {
        return ! this.isMultiSelect() && ! this.isRadioGroup() ;
    }
}