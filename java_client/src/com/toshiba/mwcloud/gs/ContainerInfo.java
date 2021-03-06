/*
   Copyright (c) 2017 TOSHIBA Digital Solutions Corporation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.toshiba.mwcloud.gs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.toshiba.mwcloud.gs.common.GSErrorCode;
import com.toshiba.mwcloud.gs.common.RowMapper;

/**
 * Represents the information about a specific Container.
 *
 * <p>It does not guarantee the validity of values e.g. notation of the Container name,
 * and the existence of TimeSeries option related to its Container type.</p>
 */
public class ContainerInfo {

	private String name;

	private ContainerType type;

	private List<ColumnInfo> columnInfoList;

	private List<IndexInfo> indexInfoList;

	private boolean rowKeyAssigned;

	private TimeSeriesProperties timeSeriesProperties;

	private boolean columnOrderIgnorable;

	private List<TriggerInfo> triggerInfoList;

	private String dataAffinity;

	/**
	 * Container information is created by specifying information on the column layout.
	 *
	 * @param name Container name. Not set when {@code null} is specified.
	 * @param type Container type. Not set when {@code null} is specified.
	 * @param columnInfoList List of column information. {@code null} cannot be specified.
	 * @param rowKeyAssigned Presence or absence of a column corresponding to a row key.
	 * {@code true} if you have a row key, and {@code false} if you don’t have one.
	 *
	 * @throws NullPointerException when {@code null} is specified as argument
	 *

	 */
	public ContainerInfo(String name, ContainerType type,
			List<ColumnInfo> columnInfoList, boolean rowKeyAssigned) {
		try {
			this.name = name;
			this.type = type;
			this.columnInfoList = new ArrayList<ColumnInfo>(columnInfoList);
			this.indexInfoList = Collections.emptyList();
			this.rowKeyAssigned = rowKeyAssigned;
			this.triggerInfoList = Collections.emptyList();
		}
		catch (NullPointerException e) {
			throw GSErrorCode.checkNullParameter(
					columnInfoList, "columnInfoList", e);
		}
	}

	/**
	 * Creates an empty ContainerInfo.
	 */
	public ContainerInfo() {
		this.columnInfoList = Collections.emptyList();
		this.indexInfoList = Collections.emptyList();
		this.triggerInfoList = Collections.emptyList();
	}

	/**
	 * Duplicates the information about the specified Container.
	 *
	 * @param containerInfo Copied Container information. {@code null} cannot be specified.
	 *
	 * @throws NullPointerException when {@code null} is specified as argument
	 *
	 */
	public ContainerInfo(ContainerInfo containerInfo) {
		this();

		setName(containerInfo.getName());
		setType(containerInfo.getType());

		final int columnCount = containerInfo.getColumnCount();
		if (columnCount > 0) {
			final List<ColumnInfo> columnInfoList =
					new ArrayList<ColumnInfo>();
			for (int i = 0; i < columnCount; i++) {
				columnInfoList.add(containerInfo.getColumnInfo(i));
			}
			setColumnInfoList(columnInfoList);
		}

		setRowKeyAssigned(containerInfo.isRowKeyAssigned());
		setIndexInfoList(containerInfo.getIndexInfoList());
		setTimeSeriesProperties(containerInfo.getTimeSeriesProperties());
		setTriggerInfoList(containerInfo.getTriggerInfoList());

		setDataAffinity(containerInfo.getDataAffinity());
	}

	/**
	 * Returns the name of the specified Container.
	 *
	 * @return A Container name, or {@code null} if unspecified.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the Container type of the specified Container.
	 *
	 * @return A Container type, or {@code null} if unspecified.
	 *
	 * @see ContainerType
	 */
	public ContainerType getType() {
		return type;
	}

	/**
	 * Returns the number of Columns of the specified Container.
	 *
	 * @return A number of Columns, or {@code 0} if Column layout is unspecified.
	 *
	 */
	public int getColumnCount() {
		return columnInfoList.size();
	}

	/**
	 * Returns the information about the specified Column.
	 *
	 * @param column An index of a Column, from {@code 0} to number of Columns minus one.
	 *
	 * @return The information of the Column corresponding to the specified index.
	 *
	 * @throws IllegalArgumentException If the specified Column number is out of range.
	 *
	 * @see RowField#columnNumber()
	 */
	public ColumnInfo getColumnInfo(int column) {
		try {
			return columnInfoList.get(column);
		}
		catch (IndexOutOfBoundsException e) {
			throw new IllegalArgumentException(
					"Column number out of range (column=" + column + ", " +
					"reason=" + e.getMessage() + ")", e);
		}
	}

	/**
	 * Checks if a Column is assigned as a Row key.
	 *
	 * <p>If the Container has a Row key, the number of its corresponding Column is {@code 0}. </p>
	 *
	 * @return {@code true} If a Row key is assigned, otherwise {@code false}.
	 */
	public boolean isRowKeyAssigned() {
		return rowKeyAssigned;
	}

	/**
	 * Checks if the order of Columns can be ignored.
	 *
	 * @return {@code true} if yes, otherwise {@code false}.
	 */
	public boolean isColumnOrderIgnorable() {
		return columnOrderIgnorable;
	}

	/**
	 * Sets whether the order of Columns can be ignored.
	 *
	 * <p>Default value cannot be ignored ({@code false}).</p>
	 *
	 * @param ignorable If the order of Columns can be ignored
	 *
	 * @see GridStore#putContainer(String, ContainerInfo, boolean)
	 */
	public void setColumnOrderIgnorable(boolean ignorable) {
		this.columnOrderIgnorable = ignorable;
	}

	/**
	 * Sets a name of the Container.
	 *
	 * @param name A name of the Container. For {@code null}, the setting is cancelled.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets a type of the Container.
	 *
	 * @param type A type of the Container. For {@code null}, the setting is cancelled.
	 */
	public void setType(ContainerType type) {
		this.type = type;
	}

	/**
	 * Set information of all Columns all at once.
	 *
	 * <p>The order of the Columns of the Container corresponds to the order of
	 * information of the Columns, if its can be ignored.</p>
	 *
	 * <p>It can be undefined, regardless of the existence of a Row key Column.</p>
	 *
	 * <p>Updates of the specified object after this function is specified will not change
	 * the object.</p>
	 *
	 * @param columnInfoList List of the information of Columns.
	 * For {@code null} or an empty list, the setting is cancelled.
	 *
	 * @see #setColumnOrderIgnorable(boolean)
	 */
	public void setColumnInfoList(List<ColumnInfo> columnInfoList) {
		if (columnInfoList == null) {
			this.columnInfoList = Collections.emptyList();
			return;
		}

		this.columnInfoList = new ArrayList<ColumnInfo>(columnInfoList);
	}

	/**
	 * Sets a Row key Column is assigned or not.
	 *
	 * <p>A Row key is assigned default.</p>
	 *
	 * <p>This function is available regardless of the layout of Columns.</p>
	 *
	 * @param assigned A Row key Column is assigned or not. {@code true} for assigned, otherwise {@code false}.
	 */
	public void setRowKeyAssigned(boolean assigned) {
		this.rowKeyAssigned = assigned;
	}

	/**
	 * Get a list of index information.
	 *
	 * <p>{@link UnsupportedOperationException} may occur if the returned value is changed.
	 * Also, the operation on this object does not change the contents of the returned object. </p>
	 *
	 * @return List of index information.
	 *
	 */
	public List<IndexInfo> getIndexInfoList() {
		return indexInfoList;
	}

	/**
	 * Set a list of index information.
	 *
	 * <p> Even if you change the contents of the specified object after calling,
	 * the contents of this object will not change. </p>
	 *
	 * @param indexInfoList List of index information. Not set in the case of {@code null} or empty list.
	 *
	 */
	public void setIndexInfoList(List<IndexInfo> indexInfoList) {
		if (indexInfoList == null || indexInfoList.isEmpty()) {
			this.indexInfoList = Collections.emptyList();
			return;
		}

		final List<IndexInfo> dest =
				new ArrayList<IndexInfo>(indexInfoList.size());
		for (IndexInfo info : indexInfoList) {
			dest.add(info.toUnmodifiable());
		}

		this.indexInfoList = Collections.unmodifiableList(dest);
	}

	/**
	 * Returns the optional properties of TimeSeries.
	 *
	 * <p>If the contents of the returned object is changed after it has been invoked,
	 * it is not defined whether the contents of this content will be changed or not.
	 * Moreover, it is not defined whether the contents of the returned object
	 * will be changed or not by operating this object.</p>
	 *
	 * @return The optional properties of TimeSeries, or {@code null} if undefined.
	 */
	public TimeSeriesProperties getTimeSeriesProperties() {
		return timeSeriesProperties;
	}

	/**
	 * Sets the optional properties of TimeSeries.
	 *
	 * <p>This function is available for any Container type.</p>
	 *
	 * <p>Updates of the specified object after this function is invoked will not change
	 * the object.</p>
	 *
	 * @param props The optional properties of TimeSeries.
	 * For {@code null}, the setting is cancelled.
	 */
	public void setTimeSeriesProperties(TimeSeriesProperties props) {
		if (props == null) {
			timeSeriesProperties = null;
			return;
		}

		timeSeriesProperties = props.clone();
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public Set<IndexType> getIndexTypes(int column) {
		throw new Error("Not supported on any version except for V1.5 beta");
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public void setIndexTypes(int column, Set<IndexType> indexTypes) {
		throw new Error("Not supported on any version except for V1.5 beta");
	}

	/**
	 * Returns a list of the trigger information.
	 *
	 * <p>{@link UnsupportedOperationException} can occur when the returned value is updated.
	 * After objects are returned, updates of this object do not change the returned object.</p>
	 *
	 * @return A list of the trigger information.
	 */
	public List<TriggerInfo> getTriggerInfoList() {
		return triggerInfoList;
	}

	/**
	 * Sets all information of triggers.
	 *
	 * @param triggerInfoList A list of trigger information.
	 * For {@code null}, the setting is cancelled.
	 */
	public void setTriggerInfoList(List<TriggerInfo> triggerInfoList) {
		if (triggerInfoList == null) {
			this.triggerInfoList = Collections.emptyList();
			return;
		}

		this.triggerInfoList = Collections.unmodifiableList(
				new ArrayList<TriggerInfo>(triggerInfoList));
	}

	/**
	 * Returns a data affinity string of the Container.
	 *
	 * @return A data affinity string, or {@code null} as default.
	 *
	 * @see #setDataAffinity(String)
	 */
	public String getDataAffinity() {
		return dataAffinity;
	}

	/**
	 *
	 * Sets a string to represent similarity between containers
	 * (data affinity). The string is used for optimizing the data
	 * allocation.
	 *
	 * <p>A data affinity string is for optimizing the arrangement of Containers
	 * among the nodes of the cluster.</p>
	 *
	 * <p>Containers which have the same data affinity may be stored
	 * near each other. Therefore the efficiency for the expiration of Rows
	 * may be improved by using the same data affinity string for TimeSeries Containers
	 * which includes Rows with similar elapsed time periods.</p>
	 *
	 * <p>There are the limitations, allowed characters and maximum
	 * length, for the data affinity string. See GridDB Technical
	 * Reference for the details. All the limitations may not be
	 * checked when setting the string. The data affinity string
	 * is case-sensitive unless otherwise noted.</p>
	 *
	 * @param dataAffinity A string to represent similarity
	 * between containers. If {@code null} is specified, the
	 * Container will be stored as usual. There are the cases that
	 * string against the limitations cannot be specified.
	 *
	 * @throws IllegalArgumentException If the specified string is not proper.
	 *
	 */
	public void setDataAffinity(String dataAffinity) {
		if (dataAffinity != null) {
			try {
				RowMapper.checkSymbol(dataAffinity, "data affinity");
			}
			catch (GSException e) {
				throw new IllegalArgumentException(e);
			}
		}

		this.dataAffinity = dataAffinity;
	}
}
