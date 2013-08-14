package org.vaadin.data.collectioncontainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;

/**
 * Container implementation for java.util.Collection.
 * 
 * This container can be used to wrap Java collections into Vaadin data
 * container. It can be in two ways:
 * <ul>
 * <li>A list of basic types like <code>Integer</code> or <code>String</code> (default).
 * <li>A list of JavaBeans.
 * </ul>
 * In the first case, container contains only single property. The id of the
 * property is <code>CollectionContainer.ITEM_PROPERTY_ID</code>.
 * 
 * In the later case a <code>BeanItem</code> is created for each collection
 * element, and the bean properties are used as container properties.
 * 
 * The BeanItems are instantiated on-demand, but to initialize the the container
 * properties the first item is created at construction time.
 * 
 * Only the properties of the bean (or the <code>ITEM_PROPERTY_ID</code> in the
 * simple case) are included as container properties. Furthermore, the
 * CollectionContainer does not allow addition or removal of container
 * properties.
 * 
 */
public class CollectionContainer implements Container, Container.Ordered,
		Container.Sortable {

	private static final long serialVersionUID = 8723750722557170944L;

	/** Use index of item as item id. */
	public static final int ITEM_ID_MODE_INDEX = 1;

	/** Use property value as item id. */
	public static final int ITEM_ID_MODE_PROPERTY = 2;

	/** Automatically create new unique id. */
	public static final int ITEM_ID_MODE_UNIQUE = 3;

	/** Use objects in collection itself as item id. */
	public static final int ITEM_ID_MODE_OBJECT = 4;

	/** The property id of collection objects if they are not treated as beans. */
	public static final String ITEM_PROPERTY_ID_OBJECT = "OBJECT";

	/** Mapping from id to instantiated item */
	private final Map<Object, Object> items = new HashMap<Object, Object>();

	/** Mapping from id to original object */
	private final Map<Object, Object> objects = new HashMap<Object, Object>();

	/** The collection which is being wrapped */
	private Collection<?> collection = null;

	/** List of properties in this data source */
	private final Collection<Object> propertyIds = new ArrayList<Object>();

	/** Mapping from container (and item) property ids to type */
	private final HashMap<Object, Class<?>> types = new HashMap<Object, Class<?>>();

	/** Should a bean item be instantiated for each item in collection. */
	private boolean createBeanItems = false;

	/** Should a collection item be instantiated for each item in collection. */
	private boolean createCollectionItems = false;

	/** Storage for generated item IDs */
	private ArrayList<Object> itemIds = null;

	/** The id mode of the container. */
	private int itemIdMode = ITEM_ID_MODE_UNIQUE;

	/** Internal counter for unique item id generation */
	private int counter = 0;

	private final Object idPropertyId;

	private boolean itemIdsInitialized;

	/**
	 * Create new CollectionContainer of simple types or JavaBeans. Properties
	 * are initialized from the first item.
	 * 
	 * @param collection
	 *            The collection to be wrapped.
	 * @param createBeanItems
	 *            Should the items be treated as JavaBeans
	 * @param itemIdMode
	 *            One of the ITEM_ID_MODE_* constants.
	 */
	public CollectionContainer(final Collection<?> collection,
								final boolean createBeanItems, final int itemIdMode) {
		if (!createBeanItems && itemIdMode == ITEM_ID_MODE_PROPERTY)
			throw new IllegalArgumentException(
					"Invalid Item id mode: ITEM_ID_MODE_PROPERTY.");
		this.itemIdMode = itemIdMode;
		this.collection = collection;
		this.itemIds = new ArrayList<Object>(this.collection.size());
		this.createBeanItems = createBeanItems;
		this.idPropertyId = null;
		initializePropertiesFromFirstItem();
	}

	/**
	 * Create new CollectionContainer of two dimensional array of primitive
	 * types. Properties are initialized from the first item, so the collection
	 * cannot be empty.
	 * 
	 * Note that the ITEM_ID_MODE_INDEX is used.
	 * 
	 * @param list
	 *            The collection of lists to be wrapped.
	 * @param collectionItem
	 *            Should a collection item be used
	 */
	public CollectionContainer(final List<Object[]> list, final int idIndex) {
		this.itemIdMode = idIndex < 0 ? ITEM_ID_MODE_INDEX
				: ITEM_ID_MODE_PROPERTY;
		this.idPropertyId = idIndex;
		this.collection = list;
		this.itemIds = new ArrayList<Object>(this.collection.size());
		this.createBeanItems = false;
		this.createCollectionItems = true;
		initializePropertiesFromFirstItem();
	}

	/**
	 * See {@link #CollectionContainer(Collection, boolean, int)}
	 * 
	 * @param items
	 * @param createBeanItems
	 * @param itemIdMode
	 */
	public CollectionContainer(final Object[] items, final boolean createBeanItems,
								final int itemIdMode) {
		this(Arrays.asList(items), createBeanItems, itemIdMode);
	}

	/**
	 * Create new CollectionContainer of JavaBeans and use property as id. This
	 * implies ITEM_ID_MODE_PROPERTY and creteBeanitems = true.
	 * 
	 * @param collectionOfBeans
	 *            The collection to be wrapped.
	 * @param idPropertyId
	 *            The property name of the bean property used as id.
	 */
	public CollectionContainer(final Collection<?> collectionOfBeans,
								final Object idPropertyId) {
		this.itemIdMode = ITEM_ID_MODE_PROPERTY;
		this.createBeanItems = true;
		this.collection = collectionOfBeans;
		this.itemIds = new ArrayList<Object>(this.collection.size());
		this.idPropertyId = idPropertyId;
		initializePropertiesFromFirstItem();
	}

	/**
	 * See {@link #CollectionContainer(Collection, Object)}.
	 * 
	 * @param arrayOfBeans
	 * @param idPropertyId
	 */
	public CollectionContainer(final Object[] arrayOfBeans, final Object idPropertyId) {
		this(Arrays.asList(arrayOfBeans), idPropertyId);
	}

	/**
	 * Create new CollectionContainer of simple types. This is useful for
	 * collections of String, Integer, Float, etc. Basically when the toString
	 * returns something meaningful to user. The itemIdMode is set to
	 * ITEM_ID_MODE_INDEX.
	 * 
	 * @param collection
	 *            The collection to be wrapped.
	 */
	public static CollectionContainer fromPrimitives(final Object[] primitives) {
		return fromPrimitives(Arrays.asList(primitives), false);
	}

	/**
	 * Create an indexed collection container from two dimensional data.
	 * 
	 * @param primitives
	 * @return
	 */
	public static CollectionContainer fromPrimitives(final Object[][] primitives,
			final int idIndex) {
		return fromPrimitives(Arrays.asList(primitives), idIndex);
	}

	public static CollectionContainer fromPrimitives(final Object[] primitives,
			final boolean indexed) {
		return fromPrimitives(Arrays.asList(primitives), indexed);
	}

	public static CollectionContainer fromPrimitives(final Collection<?> primitives,
			final boolean indexed) {
		return new CollectionContainer(primitives, false,
				indexed ? ITEM_ID_MODE_INDEX : ITEM_ID_MODE_OBJECT);
	}

	public static CollectionContainer fromPrimitives(final List<Object[]> primitives,
			final int idIndex) {
		return new CollectionContainer(primitives, idIndex);
	}

	public static CollectionContainer fromBeans(final Object[] beans) {
		return fromBeans(Arrays.asList(beans), false);
	}

	public static CollectionContainer fromBeans(final Object[] beans, final boolean indexed) {
		return fromBeans(Arrays.asList(beans), indexed);
	}

	public static CollectionContainer fromBeans(final Object[] beans,
			final Object idProperty) {
		return fromBeans(Arrays.asList(beans), idProperty);
	}

	public static Container fromBeans(final Collection<?> beans) {
		return fromBeans(beans, false);
	}

	public static CollectionContainer fromBeans(final Collection<?> beans,
			final boolean indexed) {
		return new CollectionContainer(beans, true,
				indexed ? ITEM_ID_MODE_INDEX : ITEM_ID_MODE_OBJECT);
	}

	public static CollectionContainer fromBeans(final Collection<?> beans,
			final Object idProperty) {
		return new CollectionContainer(beans, idProperty);
	}

	/**
	 * Initializes the Container's properties from the first item in collection.
	 * 
	 */
	private void initializePropertiesFromFirstItem() {
		propertyIds.clear();
		types.clear();

		final Item item = getItem(firstItemId());
		if (item != null) {
			for (final Object propertyId : item.getItemPropertyIds()) {
				final Property property = item.getItemProperty(propertyId);
				propertyIds.add(propertyId);
				types.put(propertyId, property.getType());
			}
		}
	}

	/*
	 * @see com.vaadin.data.Container#getItem(java.lang.Object)
	 */
	@Override
	public Item getItem(final Object id) {

		// Handle null
		if (id == null) { return null; }

		// Check Item cache
		Item i = (Item)this.items.get(id);
		if (i != null || this.itemIdMode == ITEM_ID_MODE_PROPERTY) { return i; }

		Object obj = null;
		if (this.itemIdMode == ITEM_ID_MODE_INDEX) {
			if (this.collection instanceof List<?>) {
				final int idx = ((Integer)id).intValue();
				if (idx >= 0 && idx < this.collection.size())
					obj = ((List<?>)this.collection).get(idx);
			} else {
				final int idx = 0;
				final int intId = ((Integer)id).intValue();

				for (final Object current : this.collection) {
					if (idx == intId) {
						obj = current;
					}
				}
			}

		} else if (this.itemIdMode == ITEM_ID_MODE_UNIQUE) {
			obj = this.objects.get(id);
		} else if (this.itemIdMode == ITEM_ID_MODE_OBJECT) {
			obj = id;
		}

		if (obj == null)
			return null;

		// Create item for data
		i = createItem(obj);
		this.items.put(id, i);
		return i;

	}

	/**
	 * Creates new item to this container.
	 * 
	 * @param obj
	 *            The object to be used as base.
	 */
	protected Item createItem(final Object obj) {
		if (this.createCollectionItems) {
			if (obj instanceof Item) {
				return (Item)obj;
			} else {
				return new ArrayItem((Object[])obj);
			}
		} else if (this.createBeanItems) {
			if (obj instanceof Item) {
				return (Item)obj;
			} else {
				return new BeanItem<Object>(obj);
			}
		} else return new PrimitiveItem(obj);
	}

	/*
	 * 
	 * @see com.vaadin.data.Container#getContainerPropertyIds()
	 */
	@Override
	public Collection<?> getContainerPropertyIds() {
		return propertyIds;
	}

	/**
	 * Try to avoid calling this, as it instantiates all item ids.
	 * 
	 * 
	 * @see com.vaadin.data.Container#getItemIds()
	 */
	@Override
	public Collection<?> getItemIds() {

		// initialize IDs
		if (!this.itemIdsInitialized) {

			this.itemIds.clear();
			this.objects.clear();
			this.items.clear();

			int idx = 0;
			for (final Object obj : collection) {
				initializeItem(idx++, obj);
			}
			this.itemIdsInitialized = true;
		}
		return this.itemIds;
	}

	/**
	 * Creates new id for given collection object. The result depends on current
	 * itemIdMode.
	 * 
	 * @param index
	 *            The index of collection object.
	 * @param obj
	 *            the collection object.
	 * @return New item id.
	 */
	private synchronized Object getIdForObject(final int index, final Object obj) {
		if (this.itemIdMode == ITEM_ID_MODE_INDEX) {
			return new Integer(index);
		} else if (this.itemIdMode == ITEM_ID_MODE_OBJECT) {
			return obj;
		} else if (this.itemIdMode == ITEM_ID_MODE_UNIQUE) {
			return new Integer(this.counter++);
		} else if (this.itemIdMode == ITEM_ID_MODE_PROPERTY
				&& obj instanceof Item) {
			final Item item = (Item)obj;
			final Property p = item.getItemProperty(this.idPropertyId);
			if (p == null)
				throw new IllegalArgumentException("Id property '"
						+ this.idPropertyId + "' not found in object (" + index
						+ "): " + obj);
			final Object value = p.getValue();
			if (value == null)
				throw new IllegalArgumentException("Value of id property '"
						+ this.idPropertyId + "' is null in object (" + index
						+ "): " + obj);
			return value;
		}

		return null;
	}

	/**
	 * Get container property for itemId and propertyId. This is equal to:
	 * <code>getItem(itemId).getItemPropertyId(propertyId);</code>
	 * 
	 * 
	 * @see com.vaadin.data.Container#getContainerProperty(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Property getContainerProperty(final Object itemId, final Object propertyId) {
		final Item item = getItem(itemId);
		if (item != null) { return item.getItemProperty(propertyId); }
		return null;
	}

	/*
	 * @see com.vaadin.data.Container#getType(java.lang.Object)
	 */
	@Override
	public Class<?> getType(final Object propertyId) {
		return this.types.get(propertyId);
	}

	/**
	 * Override the default type. Use this with caution.
	 * 
	 * @param propertyId
	 * @param type
	 */
	public void setType(final Object propertyId, final Class<?> type) {
		this.types.put(propertyId, type);
	}

	/*
	 * @see com.vaadin.data.Container#size()
	 */
	@Override
	public int size() {
		if (!itemIdsInitialized) {
			getItemIds();
		}
		return this.itemIds.size();
	}

	/*
	 * @see com.vaadin.data.Container#containsId(java.lang.Object)
	 */
	@Override
	public boolean containsId(final Object id) {
		if (this.itemIdMode == ITEM_ID_MODE_INDEX && id instanceof Integer) {
			final int i = ((Integer)id).intValue();
			return (i >= 0 && i < this.collection.size());
		} else if (this.itemIdMode == ITEM_ID_MODE_OBJECT) {
			return this.collection.contains(id);
		} else if (this.itemIdsInitialized) {
			return this.itemIds.contains(id);
		} else if (this.itemIds != null && this.itemIds.contains(id)) {
			return true;
		} else {
			return getItemIds().contains(id);
		}
	}

	/**
	 * This is not supported.
	 * 
	 * @see com.vaadin.data.Container#addItem(java.lang.Object)
	 */
	@Override
	public Item addItem(final Object object) throws UnsupportedOperationException {
		throw new UnsupportedOperationException(
				"Cannot add items CollectionContainer. Update the underlying collection instead.");
	}

	/**
	 * This is not supported.
	 * 
	 * @see com.vaadin.data.Container#addItem()
	 */
	@Override
	public Object addItem() throws UnsupportedOperationException {
		throw new UnsupportedOperationException(
				"Cannot instantiate items to collection.");
	}

	/*
	 * @see com.vaadin.data.Container#removeItem(java.lang.Object)
	 */
	@Override
	public boolean removeItem(final Object id) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Item removal not supported");
	}

	public boolean removeObject(final Object id) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Object removal not supported");
	}

	/**
	 * This is not supported.
	 * 
	 * @see com.vaadin.data.Container#addContainerProperty(java.lang.Object, java.lang.Class, java.lang.Object)
	 */
	@Override
	public boolean addContainerProperty(final Object arg0, final Class<?> arg1, final Object arg2)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException(
				"Cannot add container properties to CollectionContainer.");
	}

	/**
	 * This is not supported.
	 * 
	 * @see com.vaadin.data.Container#removeContainerProperty(java.lang.Object)
	 */
	@Override
	public boolean removeContainerProperty(final Object arg0)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException(
				"Cannot remove container properties from CollectionContainer.");
	}

	/*
	 * @see com.vaadin.data.Container#removeAllItems()
	 */
	@Override
	public boolean removeAllItems() throws UnsupportedOperationException {

		this.itemIds.clear();
		this.objects.clear();
		this.items.clear();
		this.collection.clear();
		return true;
	}

	/*
	 * @see com.vaadin.data.Container.Ordered#nextItemId(java.lang.Object)
	 */
	@Override
	public Object nextItemId(final Object id) {
		/*
		 * Four cases: ITEM_ID_MODE_INDEX; ITEM_ID_MODE_OBJECT;
		 * ITEM_ID_MODE_PROPERTY; ITEM_ID_MODE_UNIQUE;
		 */
		if (this.itemIdMode == ITEM_ID_MODE_INDEX && id instanceof Integer) {
			final int idx = (this.itemIdsInitialized ? itemIds.indexOf(id)
					: ((Integer)id).intValue()) + 1;
			if (idx >= 0 && idx < this.collection.size()) {
				if (this.itemIdsInitialized)
					return this.itemIds.get(idx);
				return new Integer(idx);
			}
		} else if (this.itemIdMode == ITEM_ID_MODE_UNIQUE
				|| this.itemIdMode == ITEM_ID_MODE_PROPERTY
				|| this.itemIdMode == ITEM_ID_MODE_OBJECT) {
			if (id != null && this.itemIdsInitialized) {
				final int index = this.itemIds.indexOf(id);
				if (index >= 0 && index < (this.itemIds.size() - 1))
					return this.itemIds.get(index + 1);
				return null;
			} else if (id != null && this.collection != null
					&& !this.collection.isEmpty()) {

				// First check the already initialized item ids
				final int index = this.itemIds.indexOf(id);
				if (index >= 0 && index < (this.itemIds.size() - 1)) { return this.itemIds.get(index + 1); }

				// Find the next and create ID for it.
				final Object obj = this.objects.get(id);
				int idx = 0;
				for (final Iterator<?> i = this.collection.iterator(); i.hasNext(); idx++) {
					Object cur = i.next();
					if (cur != null && cur.equals(obj) && i.hasNext()) {
						cur = i.next();
						return initializeItem(idx, cur);
					}

				}
			}
		}
		return null;
	}

	/*
	 * @see com.vaadin.data.Container.Ordered#prevItemId(java.lang.Object)
	 */
	@Override
	public Object prevItemId(final Object id) {
		if (this.itemIdMode == ITEM_ID_MODE_INDEX && id instanceof Integer) {
			final int idx = ((Integer)id).intValue() - 1;
			if (idx >= 0 && idx < this.collection.size()) {
				if (this.itemIdsInitialized)
					return this.itemIds.get(idx);
				return new Integer(idx);
			}
		} else if (this.itemIdMode == ITEM_ID_MODE_UNIQUE
				|| this.itemIdMode == ITEM_ID_MODE_PROPERTY
				|| this.itemIdMode == ITEM_ID_MODE_OBJECT) {
			if (id != null && this.itemIdsInitialized) {
				final int index = this.itemIds.indexOf(id);
				if (index > 0 && index < (this.itemIds.size()))
					return this.itemIds.get(index - 1);
				return null;
			}
		}
		return null;
	}

	/*
	 * @see com.vaadin.data.Container.Ordered#firstItemId()
	 */
	@Override
	public Object firstItemId() {
		if (this.itemIdsInitialized) {
			return this.itemIds.get(0);
		} else if (this.collection != null && !this.collection.isEmpty()) {
			final Object obj = this.collection.iterator().next();
			return initializeItem(0, obj);
		}
		return null;
	}

	/**
	 * Initialize item and store it into mappings.
	 * 
	 * @param obj
	 *            the collection object
	 * @return id of item.
	 */
	private Object initializeItem(final int index, final Object obj) {

		if (this.itemIdMode == ITEM_ID_MODE_PROPERTY) {
			final Item item = createItem(obj);
			final Object id = getIdForObject(index, item);
			if (!this.itemIds.contains(id))
				this.itemIds.add(id);
			this.objects.put(id, obj);
			this.items.put(id, item);
			return id;
		} else {
			final Object id = getIdForObject(index, obj);
			if (!this.itemIds.contains(id))
				this.itemIds.add(id);
			this.objects.put(id, obj);
			return id;
		}
	}

	/*
	 * @see com.vaadin.data.Container.Ordered#lastItemId()
	 */
	@Override
	public Object lastItemId() {
		if (this.itemIdsInitialized) {
			return this.itemIds.get(itemIds.size() - 1);
		} else {

			if (this.collection instanceof List<?>) {
				final List<?> list = (List<?>)this.collection;
				final int lastIndex = list.size() - 1;
				return initializeItem(lastIndex, list.get(lastIndex));
			} else if (this.collection != null && !this.collection.isEmpty()) {
				Object last = null;
				int ind = 0;
				for (final Object obj : this.collection) {
					last = obj;
					ind++;
				}
				return initializeItem(ind, last);
			}
		}
		return null;
	}

	/*
	 * @see com.vaadin.data.Container.Ordered#isFirstId(java.lang.Object)
	 */
	@Override
	public boolean isFirstId(final Object id) {
		final Object first = firstItemId();
		return (id != null && id.equals(first));
	}

	/*
	 * @see com.vaadin.data.Container.Ordered#isLastId(java.lang.Object)
	 */
	@Override
	public boolean isLastId(final Object id) {
		final Object last = lastItemId();
		return (id != null && id.equals(last));
	}

	/**
	 * This is not supported.
	 * 
	 * @see com.vaadin.data.Container.Ordered#addItemAfter(java.lang.Object)
	 */
	@Override
	public Object addItemAfter(final Object id) throws UnsupportedOperationException {
		throw new UnsupportedOperationException(
				"Operation not supported by underlying collection.");
	}

	/**
	 * This is not supported.
	 * 
	 * @see com.vaadin.data.Container.Ordered#addItemAfter(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Item addItemAfter(final Object afterThis, final Object id)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException(
				"addItemAfter: Operation not supported.");
	}

	/**
	 * Simple Item wrapper for Java beans / pojo objects.
	 * 
	 */
	public class BeanItem<BT> extends com.vaadin.data.util.BeanItem<BT> {

		private static final long serialVersionUID = 2735024676610840048L;

		private BeanItem(final BT bean) {
			super(bean);
		}
	}

	/**
	 * Simple Item wrapper for Java primitive objects.
	 * 
	 */
	public class PrimitiveItem implements Item {

		private static final long serialVersionUID = 2655977009734367009L;

		private Property property = null;

		/**
		 * Create new item from a primitive object.
		 * 
		 * @param object
		 * @param propertyId
		 */
		private PrimitiveItem(final Object object) {
			this.property = new ObjectProperty(object);
		}

		/*
		 * 
		 * @see com.vaadin.data.Item#getItemProperty(java.lang.Object)
		 */
		@Override
		public Property getItemProperty(final Object propertyId) {
			if (propertyId != null
					&& propertyId.equals(ITEM_PROPERTY_ID_OBJECT))
				return this.property;
			return null;
		}

		/*
		 * @see com.vaadin.data.Item#getItemPropertyIds()
		 */
		@Override
		public Collection<?> getItemPropertyIds() {
			return Arrays.asList(new Object[] { ITEM_PROPERTY_ID_OBJECT });
		}

		/**
		 * This is not supported.
		 * 
		 * @see com.vaadin.data.Item#addItemProperty(java.lang.Object, com.vaadin.data.Property)
		 */
		@Override
		public boolean addItemProperty(final Object id, final Property property)
				throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Operation not supported.");
		}

		/**
		 * This is not supported.
		 * 
		 * @see com.vaadin.data.Item#removeItemProperty(java.lang.Object)
		 */
		@Override
		public boolean removeItemProperty(final Object id)
				throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Operation not supported.");
		}
	}

	/**
	 * Simple Item wrapper for Java primitive objects.
	 * 
	 */
	public class ArrayItem implements Item {

		private static final long serialVersionUID = 6894488972928864826L;

		private final Object[] properties;

		private ArrayList<Integer> propIds;

		/**
		 * Create new item from a primitive object array.
		 * 
		 * @param object
		 * @param idIndex
		 */
		private ArrayItem(final Object[] properties) {
			this.properties = properties;
		}

		@Override
		public boolean addItemProperty(final Object id, final Property property)
				throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Operation not supported.");
		}

		@Override
		public Property getItemProperty(final Object id) {
			if (id instanceof Integer) {
				final int i = (Integer)id;
				if (i >= 0 && i < properties.length) { return new ObjectProperty(properties[i]); }
			}
			return null;
		}

		@Override
		public Collection<?> getItemPropertyIds() {
			if (propIds == null) {
				propIds = new ArrayList<Integer>();
				for (int i = 0; i < properties.length; i++) {
					propIds.add(i);
				}
			}
			return propIds;
		}

		@Override
		public boolean removeItemProperty(final Object id)
				throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Operation not supported.");
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.data.Container.Sortable#sort(java.lang.Object[],
	 * boolean[])
	 */
	@Override
	public void sort(final Object[] propertyId, final boolean[] ascending) {
		if (!isSortable())
			return;

		// Initialize the itemIds array
		getItemIds();

		// Sort
		Collections.sort(this.itemIds,
				new ItemComparator(propertyId, ascending));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.data.Container.Sortable#getSortableContainerPropertyIds()
	 */
	@Override
	public Collection<?> getSortableContainerPropertyIds() {
		if (!isSortable()) return null;
		else return getContainerPropertyIds();
	}

	/**
	 * Is this container sortable.
	 * 
	 * @return true
	 */
	private boolean isSortable() {
		return true;
	}

	public class ItemComparator implements Comparator<Object> {

		private final Object[] propertyIds;

		private final boolean[] ascending;

		public ItemComparator(final Object[] propertyIds, final boolean[] ascending) {
			this.propertyIds = propertyIds;
			this.ascending = ascending;
		}

		@Override
		@SuppressWarnings("unchecked")
		public int compare(final Object o1, final Object o2) {
			if (propertyIds != null && ascending != null) {
				for (int i = 0; i < propertyIds.length; i++) {
					final Object pid = propertyIds[i];
					final int sign = ascending[i] ? 1 : -1;
					final Item i1 = getItem(o1);
					final Item i2 = getItem(o2);
					if (i1 != null && i2 != null) {
						final Property p1 = i1.getItemProperty(pid);
						final Property p2 = i2.getItemProperty(pid);
						if (p1 != null && p2 != null) {
							final Object v1 = p1.getValue();
							final Object v2 = p2.getValue();
							if (v1 != null && v2 != null) {
								if (v1 instanceof Comparable<?>) {
									final int diff = sign
											* ((Comparable<Object>)v1)
													.compareTo(v2);
									if (diff != 0)
										return diff;
								}
							} else if (v1 != null) {
								// The first value is not null, so its "bigger"
								return sign;
							} else if (v2 != null) {
								// The second value is not null, so its "bigger"
								return -sign;
							}
						} else if (p1 != null) {
							// The first property is not null, so its "bigger"
							return sign;
						} else if (p2 != null) {
							// The second property is not null, so its "bigger"
							return -sign;
						}
					} else if (i1 != null) {
						// The first item is not null, so its "bigger"
						return sign;
					} else if (i2 != null) {
						// The second item is not null, so its "bigger"
						return -sign;
					}
				}
			}
			return 0;
		}

	}

	/**
	 * Get object from the underlying container by id. If createBeanItems is
	 * true, this is shortcut for ((BeanItem)getitem(id)).getBean()
	 * 
	 * @param id
	 * @return The POJO associated to the given id.
	 */
	public Object getObject(final Object id) {
		final Item i = getItem(id);
		if (i instanceof BeanItem<?>) {
			return ((BeanItem<?>)i).getBean();
		} else if (i instanceof PrimitiveItem) {
			return ((PrimitiveItem)i).property.getValue();
		} else if (this.createBeanItems) { return i; }
		return null;
	}

	/**
	 * See {@link #getObject(Object)}.
	 * 
	 * @param id
	 * @return
	 */
	public Object getBean(final Object id) {
		return getObject(id);
	}

	/** Returns the underlying collection. */
	public Collection<?> getCollection() {
		return collection;
	}

}