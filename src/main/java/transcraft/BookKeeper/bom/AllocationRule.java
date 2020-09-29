/**
 * Created on 12-Jul-2005
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
package transcraft.BookKeeper.bom;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

/**
 * @author david.tran@transcraft.co.uk
 */
public class AllocationRule implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3567989702529835478L;

	private static final Logger LOG = getLogger(AllocationRule.class);
	
    private String name;
    /**
     * make sure this is not pre-initialised because it will end up being created
     * in the database unnecessarily. Only create the list when it is needed
     */
    protected List<Allocation> allocation;
    
    protected AllocationRule() {
        
    }

    public AllocationRule(String name) {
        this.name = name;
    }
    
    public AllocationRule(AllocationRule rule) {
        this.copyRule(rule);
    }
    
    public void	copyRule(AllocationRule rule) {
        this.name = rule.name;
        this.copyAllocations(rule);
    }
    
    /**
     * post persistence op to dynamically optimise existing database population. The
     * aim is to not store the empty ArrayList into the database unnecessarily
     */
    public void optimiseForDb() {
    	if (CollectionUtils.isNotEmpty(this.allocation)) {
	    	List<Allocation> alloc = Lists.newArrayList(this.allocation)
	    			.stream()
	    			.filter(a -> a.account != null && a.getPercentage() != 0.0)
	    			.collect(Collectors.toList());
	    	if (alloc.size() != this.allocation.size()) {
	    		LOG.info("{} optimised", this); //$NON-NLS-1$
	    		this.allocation.clear();
	    		this.allocation.addAll(alloc);
	    	}
	    	if (CollectionUtils.isEmpty(alloc)) {
	    		LOG.info("{} empty, cleared", this); //$NON-NLS-1$
	    		// de-allocate any empty list. They are a waste of space in the database
	    		this.allocation = null;
	    	}
    	} else {
    		// de-allocate any empty list. They are a waste of space in the database
    		this.allocation = null;
    	}
    }
    
    protected void	copyAllocations(AllocationRule rule) {
    	if (rule == null || rule == this) {
    		return;
    	}
    	
    	if (this.allocation == null) {
    		this.allocation = Lists.newArrayList();
    	} else {
    		this.allocation.clear();
    	}

    	if (CollectionUtils.isEmpty(rule.allocation)) {
    		return;
    	}

    	this.allocation.addAll(rule.allocation
			.stream()
			.map(a -> new Allocation(a))
			.collect(Collectors.toList())
		);
    	optimiseForDb();
    }
    
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("%s%s", this.getName(), this.allocation);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object arg0) {
    	if (arg0 == null || !(arg0 instanceof AllocationRule)) {
    		return false;
    	}
    	AllocationRule rule = (AllocationRule)arg0;
        return this.name.equals(rule.name) &&
        		CollectionUtils.isNotEmpty(this.allocation) &&
        		CollectionUtils.isNotEmpty(rule.allocation) &&
        	CollectionUtils.isEqualCollection(this.allocation, rule.allocation);
    }
    
    public void	addAllocation(Allocation alloc) {
        this.addAllocation(alloc.getAccount(), alloc.getPercentage(), alloc.getDescription());
    }
    
    public void	addAllocation(String account, double percentage) {
        this.addAllocation(account, percentage, null);
    }
    
    public void	addAllocation(String account, double percentage, String description) {
        if (isBlank(account)) {
        	LOG.info("addAllocation({}):account is blank", account); //$NON-NLS-1$
            return;
        }
        if (this.allocation == null) {
        	// create list only when needed
        	this.allocation = Lists.newArrayList();
        }
        double totalPercent = Math.abs(percentage);
        for (Allocation alloc : this.allocation) {
            totalPercent += (alloc.account.equals(account)) ? 0.0 :
                Math.abs(alloc.getPercentage());
        }
        if (totalPercent <= 1) {
            Allocation alloc = new Allocation(account, percentage);
            if (description != null) {
                alloc.setDescription(description);
            }
            LOG.debug("Before addAllocation({}) we have {}", alloc, this); //$NON-NLS-1$
            int idx = this.allocation.indexOf(alloc);
            if (idx < 0) {
            	LOG.debug("addAllocation({}) added", alloc); //$NON-NLS-1$
                this.allocation.add(alloc);
            } else {
            	LOG.debug("addAllocation({}) at {}", alloc, idx); //$NON-NLS-1$
                this.allocation.set(idx, alloc);
            }
            LOG.debug("After addAllocation({}) we have {}", alloc, this); //$NON-NLS-1$
        } else {
        	LOG.info("Percentage {} exceeds allocation total", percentage); //$NON-NLS-1$
            //throw new RuntimeException("Percentage " + percentage + " exceeds allocation total");
        }
        this.allocation = this.allocation.stream()
        		.sorted()
        		.collect(Collectors.toList());
    }
    
    public void removeAllocation(String account, double percentage) {
    	if (CollectionUtils.isNotEmpty(this.allocation)) {
            this.allocation.remove(new Allocation(account, percentage));
    	}
    }
    
    public double	getTotalAllocation() {
    	double totalPercent = 0.0;
    	if (CollectionUtils.isNotEmpty(this.allocation)) {
    		for (Allocation alloc : this.allocation) {
    			totalPercent += Math.abs(alloc.getPercentage());
    		}
    	}
    	return totalPercent;
    }
    
    public boolean	isEmpty() {
        return CollectionUtils.isEmpty(this.allocation);
    }
    
    public void	reset() {
    	if (CollectionUtils.isNotEmpty(this.allocation)) {
    		this.allocation.clear();
    	}
    }
    
    public Allocation [] getAllocations(boolean withDummyEntry) {
    	List<Allocation> list = CollectionUtils.isNotEmpty(this.allocation) ?
    			Lists.newArrayList(this.allocation) : Lists.newArrayList();
    	if (list.size() > 1) {
        	/*
        	 * not sure why, but there are empty elements
        	 */
    		list = list
    				.stream()
    				.filter(alloc -> alloc.account != null)
    				.collect(Collectors.toList());
    	}
        if (withDummyEntry) {
        	list.add(new Allocation("", 0)); //$NON-NLS-1$
        }
        // Java 8 compatibility mode, so Allocation[]::new is not available
        return list.toArray(new Allocation[list.size()]);
    }
}
